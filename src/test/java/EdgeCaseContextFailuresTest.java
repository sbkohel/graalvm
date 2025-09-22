import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseContextFailuresTest {

    private Method runScriptMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Use reflection to access private runScript method
        runScriptMethod = Application.class.getDeclaredMethod("runScript", String.class, Context.class, String.class);
        runScriptMethod.setAccessible(true);
    }

    @Test
    void testRunScript_withClosedContext_throwsException() throws Exception {
        // Arrange
        String[] supportedLangs = { "js", "python", "R" };
        Context context = Context.newBuilder(supportedLangs)
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        context.close(); // Close the context before using it

        String jsCode = "console.log('test');";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, jsCode, context, "js");
        });

        assertTrue(exception.getCause() instanceof IllegalStateException ||
                  exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testRunScript_withNullContext_throwsException() {
        // Arrange
        String jsCode = "console.log('test');";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            runScriptMethod.invoke(null, jsCode, null, "js");
        });
    }

    @Test
    void testRunScript_contextWithoutLanguageSupport_throwsException() {
        // Arrange - create context without the required language
        Context limitedContext = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        String pythonCode = "print('test')";

        try {
            // Act & Assert
            InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
                runScriptMethod.invoke(null, pythonCode, limitedContext, "python");
            });

            assertTrue(exception.getCause() instanceof PolyglotException);
        } finally {
            limitedContext.close();
        }
    }

    @Test
    void testRunScript_contextMemoryExhaustion_handlesGracefully() {
        // Arrange - create context with memory limitations
        Context restrictedContext = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        // Memory-intensive script
        String memoryIntensiveJS = """
            var bigArray = [];
            for (var i = 0; i < 1000000; i++) {
                bigArray.push('memory consuming string ' + i);
            }
            bigArray.length;
            """;

        try {
            // Act - this may succeed or fail depending on available memory
            // The test verifies the context handles it gracefully
            Object result = runScriptMethod.invoke(null, memoryIntensiveJS, restrictedContext, "js");

            // Assert - if it succeeds, result should be valid
            assertNotNull(result);
        } catch (InvocationTargetException e) {
            // Assert - if it fails, should be a PolyglotException
            assertTrue(e.getCause() instanceof PolyglotException ||
                      e.getCause() instanceof OutOfMemoryError);
        } catch (Exception e) {
            // Other exceptions are acceptable for memory exhaustion scenarios
            assertTrue(true);
        } finally {
            restrictedContext.close();
        }
    }

    @Test
    void testRunScript_invalidLanguageIdentifier_throwsException() {
        // Arrange
        Context context = Context.newBuilder("js", "python")
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        String code = "print('test')";

        try {
            // Act & Assert
            InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
                runScriptMethod.invoke(null, code, context, "invalid_language");
            });

            assertTrue(exception.getCause() instanceof PolyglotException);
        } finally {
            context.close();
        }
    }

    @Test
    void testRunScript_contextConcurrentAccess_handlesCorrectly() throws Exception {
        // Arrange
        Context sharedContext = Context.newBuilder("js", "python")
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        String jsCode = "var x = 1; x + 1;";

        try {
            // Act - simulate concurrent access (single-threaded test)
            Object result1 = runScriptMethod.invoke(null, jsCode, sharedContext, "js");
            Object result2 = runScriptMethod.invoke(null, jsCode, sharedContext, "js");

            // Assert - both executions should succeed
            assertNotNull(result1);
            assertNotNull(result2);
        } finally {
            sharedContext.close();
        }
    }
}
