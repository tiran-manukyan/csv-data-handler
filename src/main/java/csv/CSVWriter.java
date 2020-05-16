package csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CSVWriter {

    private final boolean overwrite;

    private final char delimiter;

    public CSVWriter(boolean overwrite, char delimiter) {
        this.overwrite = overwrite;
        this.delimiter = delimiter;
    }

    public void write(Path path, List<String[]> data) throws IOException {
        if (!overwrite) {
            if (Files.exists(path)) {
                throw new FileAlreadyExistsException("File already exists: " + path);
            }
        }

        try (PrintWriter pw = new PrintWriter(path.toFile())) {
            for (String[] line : data) {
                pw.println(String.join(String.valueOf(delimiter), line));
            }
        }
    }
}
