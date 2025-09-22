import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseMissingFilesTest {

    @TempDir
    Path tempDir;

    private Method readFileMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // Use reflection to access private readFile method
        readFileMethod = Application.class.getDeclaredMethod("readFile", String.class);
        readFileMethod.setAccessible(true);
    }

    @Test
    void testReadFile_completelyMissingFile_throwsIOException() {
        // Arrange
        String missingFile = tempDir.resolve("nonexistent.js").toString();

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, missingFile);
        });

        assertTrue(exception.getCause() instanceof IOException);
        assertTrue(exception.getCause().getMessage().contains("nonexistent.js") ||
                  exception.getCause().getMessage().contains("No such file"));
    }

    @Test
    void testReadFile_missingDirectoryInPath_throwsIOException() {
        // Arrange
        String missingDirPath = tempDir.resolve("missing_dir").resolve("file.py").toString();

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, missingDirPath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testReadFile_emptyPathString_throwsIOException() {
        // Arrange
        String emptyPath = "";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, emptyPath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testReadFile_relativePathToMissingFile_throwsIOException() {
        // Arrange
        String relativePath = "./missing_relative_file.js";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, relativePath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testReadFile_absolutePathToMissingFile_throwsIOException() {
        // Arrange
        String absolutePath = "/absolute/path/to/missing/file.py";

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, absolutePath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testReadFile_missingFileWithSpecialChars_throwsIOException() {
        // Arrange
        String specialCharPath = tempDir.resolve("file with spaces & special chars!.js").toString();

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> {
            readFileMethod.invoke(null, specialCharPath);
        });

        assertTrue(exception.getCause() instanceof IOException);
    }
}
