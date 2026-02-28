package diameter.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileReader {
    public static List<String> getLinesFromFile(String[] args) {
        try {
            Path csvPath = Path.of(args[0]);
            return Files.readAllLines(csvPath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
