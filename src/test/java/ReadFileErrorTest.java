import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ReadFileErrorTest {

    private Method readFileMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Use reflection to access private readFile method
        readFileMethod = Application.class.getDeclaredMethod("readFile", String.class);
        readFileMethod.setAccessible(true);
    }

    @Test
    void testReadFile_nonExistentFile_throwsIOException() {
        // Arrange
        String nonExistentPath = "/path/that/does/not/exist/file.txt";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, nonExistentPath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testReadFile_nullPath_throwsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            readFileMethod.invoke(null, (String) null);
        });
    }

    @Test
    void testReadFile_invalidPath_throwsIOException() {
        // Arrange
        String invalidPath = "\0invalid\0path";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, invalidPath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testReadFile_directoryInsteadOfFile_throwsIOException() {
        // Arrange
        String directoryPath = System.getProperty("java.io.tmpdir");

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, directoryPath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }
}
