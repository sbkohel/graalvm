import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RunScriptPythonTest {

    private Context context;
    private Method runScriptMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Initialize GraalVM context for Python
        String[] supportedLangs = { "js", "python", "R" };
        context = Context.newBuilder(supportedLangs)
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        // Use reflection to access private runScript method
        runScriptMethod = Application.class.getDeclaredMethod("runScript", String.class, Context.class, String.class);
        runScriptMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void testRunScript_validPython_executesSuccessfully() throws Exception {
        // Arrange
        String pythonCode = "print('Hello from Python test')\n'test result'";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, pythonCode, context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("test result", result.asString());
    }

    @Test
    void testRunScript_simplePythonExpression_returnsValue() throws Exception {
        // Arrange
        String pythonCode = "7 + 13";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, pythonCode, context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(20, result.asInt());
    }

    @Test
    void testRunScript_pythonWithVariables_executesCorrectly() throws Exception {
        // Arrange
        String pythonCode = "x = 15\ny = 25\nx * y";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, pythonCode, context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(375, result.asInt());
    }

    @Test
    void testRunScript_pythonFunction_returnsCorrectValue() throws Exception {
        // Arrange
        String pythonCode = "def greet(name):\n    return f'Hello {name}'\n\ngreet('Python')";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, pythonCode, context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("Hello Python", result.asString());
    }

    @Test
    void testRunScript_pythonListOperation_returnsCorrectValue() throws Exception {
        // Arrange
        String pythonCode = "numbers = [1, 2, 3, 4, 5]\nsum(numbers)";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, pythonCode, context, "python");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(15, result.asInt());
    }
}
