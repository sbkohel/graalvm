import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RunScriptJSTest {

    private Context context;
    private Method runScriptMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Initialize GraalVM context for JavaScript
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
    void testRunScript_validJavaScript_executesSuccessfully() throws Exception {
        // Arrange
        String jsCode = "console.log('Hello from JS test'); 'test result';";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, jsCode, context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("test result", result.asString());
    }

    @Test
    void testRunScript_simpleJSExpression_returnsValue() throws Exception {
        // Arrange
        String jsCode = "5 + 3";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, jsCode, context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(8, result.asInt());
    }

    @Test
    void testRunScript_jsWithVariables_executesCorrectly() throws Exception {
        // Arrange
        String jsCode = "var x = 10; var y = 20; x * y;";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, jsCode, context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isNumber());
        assertEquals(200, result.asInt());
    }

    @Test
    void testRunScript_jsFunction_returnsCorrectValue() throws Exception {
        // Arrange
        String jsCode = "function greet(name) { return 'Hello ' + name; } greet('World');";

        // Act
        Value result = (Value) runScriptMethod.invoke(null, jsCode, context, "js");

        // Assert
        assertNotNull(result);
        assertTrue(result.isString());
        assertEquals("Hello World", result.asString());
    }
}
