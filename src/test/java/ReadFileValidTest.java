import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ReadFileValidTest {

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
    void testReadFile_validFilePath_returnsContent() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("test.txt");
        String expectedContent = "Hello World!\nSecond line\n";
        Files.writeString(testFile, expectedContent);

        // Act
        String actualContent = (String) readFileMethod.invoke(null, testFile.toString());

        // Assert
        assertNotNull(actualContent);
        assertTrue(actualContent.contains("Hello World!"));
        assertTrue(actualContent.contains("Second line"));
    }

    @Test
    void testReadFile_emptyFile_returnsEmptyString() throws Exception {
        // Arrange
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        // Act
        String content = (String) readFileMethod.invoke(null, emptyFile.toString());

        // Assert
        assertNotNull(content);
        assertEquals("", content.trim());
    }

    @Test
    void testReadFile_fileWithSpecialCharacters_handledCorrectly() throws Exception {
        // Arrange
        Path testFile = tempDir.resolve("special.txt");
        String specialContent = "Special chars: àáâãäåæçèéêë\nUnicode: 🚀🎯⭐\n";
        Files.writeString(testFile, specialContent);

        // Act
        String content = (String) readFileMethod.invoke(null, testFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Special chars:"));
        assertTrue(content.contains("Unicode:"));
    }

    @Test
    void testReadFile_largeFile_readCompletely() throws Exception {
        // Arrange
        Path largeFile = tempDir.resolve("large.txt");
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("Line ").append(i).append("\n");
        }
        Files.writeString(largeFile, largeContent.toString());

        // Act
        String content = (String) readFileMethod.invoke(null, largeFile.toString());

        // Assert
        assertNotNull(content);
        assertTrue(content.contains("Line 0"));
        assertTrue(content.contains("Line 999"));
    }
}
