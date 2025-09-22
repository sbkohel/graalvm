# Test Contract: Application Class Methods

## Contract: readFile(String pathname)

### Input Contract
- **Parameter**: `pathname` - String representing file path
- **Preconditions**: 
  - `pathname` must not be null
  - Path should be accessible to JVM process
  
### Output Contract
- **Return Type**: `String` - file contents
- **Postconditions**:
  - Returns complete file contents as string
  - Includes line separators as per system property
  - Throws `IOException` if file cannot be read

### Test Scenarios
- ✅ Valid file path returns content
- ✅ Non-existent file throws IOException
- ✅ Empty file returns empty string
- ✅ File with special characters handled correctly
- ✅ Large files read completely
- ✅ Different encodings supported

## Contract: runScript(String script, Context context, String language)

### Input Contract
- **Parameters**:
  - `script` - String containing executable code
  - `context` - GraalVM Context instance
  - `language` - String identifier ("js", "python", "R")
- **Preconditions**:
  - `script` must not be null
  - `context` must be initialized and active
  - `language` must be supported by context

### Output Contract
- **Return Type**: `Value` - GraalVM polyglot value
- **Postconditions**:
  - Returns polyglot Value object
  - Script execution completes or throws runtime exception
  - Context state remains valid after execution

### Test Scenarios
- ✅ Valid JavaScript executes successfully
- ✅ Valid Python executes successfully
- ✅ Malformed script throws appropriate exception
- ✅ Runtime errors properly propagated
- ✅ Return values correctly wrapped in Value object
- ✅ Context remains usable after execution

## Contract: main(String[] args)

### Input Contract
- **Parameter**: `args` - Command line arguments array
- **Preconditions**:
  - Script files must exist in expected locations
  - GraalVM context can be initialized

### Output Contract
- **Return Type**: `void`
- **Postconditions**:
  - All configured scripts executed
  - Context properly initialized and disposed
  - IOException handled if scripts missing

### Test Scenarios
- ✅ Normal execution with existing scripts
- ✅ Missing script files handled gracefully
- ✅ Context initialization and cleanup
- ✅ Multiple language execution sequence
- ✅ Error propagation from nested calls
