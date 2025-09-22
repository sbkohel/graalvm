import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * MockFileSystem utility for testing file operations without external dependencies.
 * Provides in-memory file simulation for reliable and fast testing.
 */
public class MockFileSystem {

    private final Map<String, String> files = new HashMap<>();
    private final Map<String, Boolean> fileExists = new HashMap<>();

    /**
     * Creates a mock file with the given content
     * @param path File path
     * @param content File content
     */
    public void createFile(String path, String content) {
        files.put(path, content);
        fileExists.put(path, true);
    }

    /**
     * Simulates file existence check
     * @param path File path to check
     * @return true if file exists in mock system
     */
    public boolean exists(String path) {
        return fileExists.getOrDefault(path, false);
    }

    /**
     * Retrieves mock file content
     * @param path File path
     * @return File content or null if doesn't exist
     * @throws IOException if file doesn't exist
     */
    public String readFile(String path) throws IOException {
        if (!exists(path)) {
            throw new IOException("File not found: " + path);
        }
        return files.get(path);
    }

    /**
     * Removes a mock file
     * @param path File path to remove
     */
    public void deleteFile(String path) {
        files.remove(path);
        fileExists.put(path, false);
    }

    /**
     * Clears all mock files
     */
    public void clear() {
        files.clear();
        fileExists.clear();
    }

    /**
     * Gets the number of mock files
     * @return Number of files in mock system
     */
    public int getFileCount() {
        return files.size();
    }

    /**
     * Creates a real temporary file with content for integration testing
     * @param content File content
     * @param extension File extension (js, py, etc.)
     * @return Path to the created temporary file
     * @throws IOException if file creation fails
     */
    public Path createRealTempFile(String content, String extension) throws IOException {
        Path tempFile = Files.createTempFile("mock-test", "." + extension);
        Files.write(tempFile, content.getBytes());
        // Register for cleanup
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    /**
     * Sets up common test files used across multiple test classes
     */
    public void setupCommonTestFiles() {
        // Valid script files
        createFile("scripts/helloWorld.js", TestFixtures.VALID_JAVASCRIPT);
        createFile("scripts/helloWorld.py", TestFixtures.VALID_PYTHON);
        createFile("scripts/helloWorld.R", TestFixtures.VALID_R);

        // Invalid script files for error testing
        createFile("scripts/malformed.js", TestFixtures.MALFORMED_JAVASCRIPT);
        createFile("scripts/malformed.py", TestFixtures.MALFORMED_PYTHON);

        // Special test files
        createFile("scripts/empty.js", "");
        createFile("scripts/special-chars.txt", TestFixtures.SPECIAL_CHARS_CONTENT);
        createFile("scripts/large.txt", TestFixtures.generateLargeFileContent(1000));
    }

    /**
     * Simulates file access permission errors
     * @param path File path
     * @throws IOException always (simulates permission denied)
     */
    public void simulatePermissionError(String path) throws IOException {
        throw new IOException("Permission denied: " + path);
    }

    /**
     * Creates a mock directory structure
     * @param baseDir Base directory path
     */
    public void createDirectory(String baseDir) {
        // Mark directory as existing
        fileExists.put(baseDir, true);
    }

    /**
     * Checks if a path represents a directory in the mock system
     * @param path Path to check
     * @return true if path is a mock directory
     */
    public boolean isDirectory(String path) {
        return fileExists.getOrDefault(path, false) && !files.containsKey(path);
    }
}
