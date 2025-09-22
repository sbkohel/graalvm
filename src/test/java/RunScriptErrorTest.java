import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class RunScriptErrorTest {

    private Context context;
    private Method runScriptMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Initialize GraalVM context
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
    void testRunScript_malformedJavaScript_throwsException() {
        // Arrange
        String malformedJS = "console.log('unclosed string";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedJS, context, "js");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testRunScript_malformedPython_throwsException() {
        // Arrange
        String malformedPython = "print('test' invalid syntax";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedPython, context, "python");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testRunScript_jsRuntimeError_throwsException() {
        // Arrange
        String jsWithRuntimeError = "var obj = null; obj.nonExistentProperty.call();";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, jsWithRuntimeError, context, "js");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testRunScript_pythonRuntimeError_throwsException() {
        // Arrange
        String pythonWithRuntimeError = "x = 10 / 0";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, pythonWithRuntimeError, context, "python");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testRunScript_nullScript_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            runScriptMethod.invoke(null, null, context, "js");
        });
    }

    @Test
    void testRunScript_unsupportedLanguage_throwsException() {
        // Arrange
        String code = "some code";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, code, context, "unsupported");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testRunScript_nullContext_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            runScriptMethod.invoke(null, "console.log('test')", null, "js");
        });
    }
}
