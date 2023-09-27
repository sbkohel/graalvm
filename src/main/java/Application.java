import org.graalvm.polyglot.*;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

class Application {
    public static void main(String[] args) throws IOException {
        final String rootDir = System.getProperty("user.dir");

        String[] supportedLangs = { "js", "python", "R" };
        Context context = Context.newBuilder(supportedLangs).allowAllAccess(true).build();

        String jsFile = readFile(rootDir + "/src/main/scripts/helloWorld.js");
        runScript(jsFile, context, "js");

        String pyFile = readFile(rootDir + "/src/main/scripts/helloWorld.py");
        runScript(pyFile, context, "python");

//        String rFile = readFile(rootDir + "/src/main/scripts/helloWorld.R");
//        runScript(rFile, context, "R");
    }

    private static Value runScript(String script, Context context, String language) {
        return context.eval(language, script);
    }

    private static String readFile(String pathname) throws IOException {
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }
}