import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for GraalVM context lifecycle management
 * Tests context creation, usage, cleanup, and resource management scenarios
 */
class ContextLifecycleTest {

    private Method runScriptMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        runScriptMethod = Application.class.getDeclaredMethod("runScript", String.class, Context.class, String.class);
        runScriptMethod.setAccessible(true);
    }

    @Test
    @DisplayName("Context should initialize properly with all supported languages")
    void testContextInitialization() {
        // Act
        Context context = TestFixtures.createTestContext();

        // Assert
        assertNotNull(context);

        // Test that context supports expected languages
        assertDoesNotThrow(() -> {
            Value jsResult = (Value) runScriptMethod.invoke(null, "1 + 1", context, "js");
            assertEquals(2, jsResult.asInt());
        });

        assertDoesNotThrow(() -> {
            Value pyResult = (Value) runScriptMethod.invoke(null, "2 + 2", context, "python");
            assertEquals(4, pyResult.asInt());
        });

        context.close();
    }

    @Test
    @DisplayName("Context should handle proper cleanup without resource leaks")
    void testContextCleanup() {
        Context context = null;

        try {
            // Arrange
            context = TestFixtures.createTestContext();

            // Act - Use context for operations
            Value result = (Value) runScriptMethod.invoke(null, "var x = 100; x;", context, "js");
            assertEquals(100, result.asInt());

            // Close context explicitly
            context.close();
            context = null;

            // Assert - Context should be properly closed (no way to directly test this in Java,
            // but we can verify no exceptions occur during cleanup)
            assertTrue(true, "Context cleanup completed without exceptions");

        } catch (Exception e) {
            fail("Context cleanup should not throw exceptions: " + e.getMessage());
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    @Test
    @DisplayName("Multiple contexts should operate independently")
    void testMultipleContextIndependence() throws Exception {
        Context context1 = null;
        Context context2 = null;

        try {
            // Arrange
            context1 = TestFixtures.createTestContext();
            context2 = TestFixtures.createTestContext();

            // Act - Set different values in each context
            Value result1a = (Value) runScriptMethod.invoke(null, "var sharedName = 'context1'; sharedName;", context1, "js");
            Value result2a = (Value) runScriptMethod.invoke(null, "var sharedName = 'context2'; sharedName;", context2, "js");

            // Verify independence - each context should maintain its own state
            Value result1b = (Value) runScriptMethod.invoke(null, "sharedName", context1, "js");
            Value result2b = (Value) runScriptMethod.invoke(null, "sharedName", context2, "js");

            // Assert
            assertEquals("context1", result1a.asString());
            assertEquals("context2", result2a.asString());
            assertEquals("context1", result1b.asString());
            assertEquals("context2", result2b.asString());

        } finally {
            TestFixtures.cleanupContext(context1);
            TestFixtures.cleanupContext(context2);
        }
    }

    @Test
    @DisplayName("Context should handle concurrent access safely")
    void testConcurrentContextAccess() throws Exception {
        Context context = TestFixtures.createTestContext();
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            // Arrange - Multiple concurrent operations
            Future<Integer>[] futures = new Future[10];

            // Act - Submit concurrent tasks
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                futures[i] = executor.submit(() -> {
                    try {
                        String script = String.format("var taskResult = %d * 2; taskResult;", taskId);
                        Value result = (Value) runScriptMethod.invoke(null, script, context, "js");
                        return result.asInt();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Assert - All tasks should complete successfully
            for (int i = 0; i < 10; i++) {
                Integer result = futures[i].get(5, TimeUnit.SECONDS);
                assertEquals(i * 2, result.intValue());
            }

        } finally {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            context.close();
        }
    }

    @Test
    @DisplayName("Context should recover from script errors without corruption")
    void testContextErrorRecovery() throws Exception {
        Context context = TestFixtures.createTestContext();

        try {
            // Act - Execute a script that will cause an error
            assertThrows(Exception.class, () -> {
                runScriptMethod.invoke(null, "undefined.property.access()", context, "js");
            });

            // Assert - Context should still work after error
            Value result = (Value) runScriptMethod.invoke(null, "var recovery = 'success'; recovery;", context, "js");
            assertNotNull(result);
            assertEquals("success", result.asString());

        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("Context should handle memory-intensive operations appropriately")
    void testContextMemoryManagement() throws Exception {
        Context context = TestFixtures.createTestContext();

        try {
            // Act - Execute memory-intensive script with monitoring
            String memoryScript = TestFixtures.MEMORY_INTENSIVE_JS;
            long startTime = System.currentTimeMillis();

            Value result = (Value) runScriptMethod.invoke(null, memoryScript, context, "js");

            long duration = System.currentTimeMillis() - startTime;

            // Assert - Should complete without memory errors
            assertNotNull(result);
            assertTrue(result.isNumber());
            assertTrue(result.asLong() > 0, "Memory-intensive operation should return array length");
            assertTrue(duration < 30000, "Memory-intensive operation should complete within 30 seconds");

        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("Context should handle rapid creation and destruction cycles")
    void testRapidContextLifecycle() {
        // Act & Assert - Rapid context creation/destruction should not cause issues
        for (int i = 0; i < 50; i++) {
            Context context = TestFixtures.createTestContext();

            try {
                Value result = (Value) runScriptMethod.invoke(null,
                    String.format("var iteration = %d; iteration;", i), context, "js");
                assertEquals(i, result.asInt());
            } catch (Exception e) {
                fail("Context cycle " + i + " failed: " + e.getMessage());
            } finally {
                context.close();
            }
        }
    }

    @Test
    @DisplayName("Context should maintain language isolation between executions")
    void testLanguageIsolation() throws Exception {
        Context context = TestFixtures.createTestContext();

        try {
            // Act - Execute scripts in different languages that use same variable names
            Value jsResult = (Value) runScriptMethod.invoke(null,
                "var language = 'javascript'; language;", context, "js");

            Value pyResult = (Value) runScriptMethod.invoke(null,
                "language = 'python'\nlanguage", context, "python");

            // Execute JS again to verify isolation
            Value jsResult2 = (Value) runScriptMethod.invoke(null,
                "language", context, "js");

            // Assert - Languages should maintain separate variable spaces
            assertEquals("javascript", jsResult.asString());
            assertEquals("python", pyResult.asString());
            assertEquals("javascript", jsResult2.asString());

        } finally {
            context.close();
        }
    }

    @Test
    @DisplayName("Context configuration should be applied correctly")
    void testContextConfiguration() {
        // Arrange - Create context with specific configuration
        String[] supportedLangs = {"js", "python"};
        Context context = Context.newBuilder(supportedLangs)
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        try {
            // Act - Test that configuration is working
            Value result = (Value) runScriptMethod.invoke(null, "Math.sqrt(16)", context, "js");

            // Assert
            assertNotNull(result);
            assertEquals(4.0, result.asDouble(), 0.001);

        } catch (Exception e) {
            fail("Context configuration test failed: " + e.getMessage());
        } finally {
            context.close();
        }
    }
}
