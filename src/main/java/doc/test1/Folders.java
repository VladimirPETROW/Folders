package doc.test1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;

public class Folders {

    Path root;
    Path out;
    HashMap<String, Part> parts;

    public static void main(String[] args) {
        Path root = Paths.get(args[0]);
        Path out = Paths.get(args[1]);
        Folders folders = new Folders(root, out);
        folders.readFiles();
    }

    public Folders(Path r, Path o) {
        root = r;
        out = o;
        parts = new HashMap<>();
    }

    public void readFiles() {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = file.getFileName().toString();
                    if (parts.containsKey(name)) {
                        throw new RuntimeException(String.format("Файл с таким именем уже существует: %s", file));
                    }
                    try {
                        List<String> lines = Files.readAllLines(file);
                        Part part = new Part(name, file, lines);
                        parts.put(part.name, part);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
