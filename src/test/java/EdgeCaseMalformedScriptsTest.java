import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseMalformedScriptsTest {

    @TempDir
    Path tempDir;

    private Context context;
    private Method runScriptMethod;
    private Method readFileMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Initialize GraalVM context
        String[] supportedLangs = { "js", "python", "R" };
        context = Context.newBuilder(supportedLangs)
                .allowAllAccess(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build();

        // Use reflection to access private methods
        runScriptMethod = Application.class.getDeclaredMethod("runScript", String.class, Context.class, String.class);
        runScriptMethod.setAccessible(true);

        readFileMethod = Application.class.getDeclaredMethod("readFile", String.class);
        readFileMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void testMalformedJavaScript_unclosedString_throwsException() {
        // Arrange
        String malformedJS = "console.log('unclosed string";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedJS, context, "js");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testMalformedJavaScript_unclosedBrace_throwsException() {
        // Arrange
        String malformedJS = "function test() { console.log('missing closing brace');";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedJS, context, "js");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testMalformedPython_invalidIndentation_throwsException() {
        // Arrange
        String malformedPython = "def test():\nprint('invalid indentation')";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedPython, context, "python");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testMalformedPython_invalidSyntax_throwsException() {
        // Arrange
        String malformedPython = "print('test' invalid syntax";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedPython, context, "python");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testReadAndRunMalformedScriptFile_throwsException() throws Exception {
        // Arrange
        Path malformedJSFile = tempDir.resolve("malformed.js");
        Files.writeString(malformedJSFile, "console.log('unclosed string");

        // Act - read the malformed file
        String malformedContent = (String) readFileMethod.invoke(null, malformedJSFile.toString());

        // Assert - running the malformed content should throw exception
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            runScriptMethod.invoke(null, malformedContent, context, "js");
        });

        assertTrue(exception.getCause() instanceof PolyglotException);
    }

    @Test
    void testEmptyScript_handledGracefully() throws Exception {
        // Arrange
        String emptyScript = "";

        // Act - empty script should not throw exception but return null/undefined
        Object result = runScriptMethod.invoke(null, emptyScript, context, "js");

        // Assert
        assertNotNull(result); // GraalVM should return some Value object
    }

    @Test
    void testScriptWithOnlyWhitespace_handledGracefully() throws Exception {
        // Arrange
        String whitespaceScript = "   \n\t   \n   ";

        // Act
        Object result = runScriptMethod.invoke(null, whitespaceScript, context, "js");

        // Assert
        assertNotNull(result); // Should return some Value object
    }
}
