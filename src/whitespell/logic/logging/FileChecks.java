package whitespell.logic.logging;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by wwadewitte on 10/2/14.
 */
public class FileChecks {

    public static long folderSize(File directory) {
        if(directory == null) {
            return 0;
        }
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static String readFile(String pathname) throws IOException {

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
