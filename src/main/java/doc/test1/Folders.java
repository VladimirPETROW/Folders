package doc.test1;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

public class Folders {

    Path root;
    Path out;
    HashMap<String, Part> namePart;
    Part[] parts;

    public static void main(String[] args) {
        Path root = Paths.get(args[0]);
        Path out = Paths.get(args[1]);
        Folders folders = new Folders(root, out);
        folders.readFiles();
        folders.sort();
        folders.require();
        folders.save();
    }

    public Folders(Path r, Path o) {
        root = r;
        out = o;
        namePart = new HashMap<>();
    }

    void readFiles() {
        try {
            int buffSize = 1024;
            char[] buffer = new char[buffSize];
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = file.getFileName().toString();
                    if (namePart.containsKey(name)) {
                        throw new RuntimeException(String.format("Файл с таким именем уже существует: %s", file));
                    }
                    StringBuffer text = new StringBuffer();
                    try (FileReader reader = new FileReader(file.toFile())) {
                        int count;
                        while ((count = reader.read(buffer)) > 0) {
                            text.append(buffer, 0, count);
                        }
                    }
                    Part part = new Part(name, file, text.toString(), root);
                    namePart.put(part.name, part);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void sort() {
        parts = namePart.keySet().stream()
                .sorted()
                .map(name -> namePart.get(name))
                .toArray(size -> new Part[size]);
    }

    void require() {
        Part part = parts[0];
        for (int i = 1; i < parts.length; i++) {
            part = part.append(parts[i]);
        }

        for (int i = 0; i < parts.length; i++) {
            part = parts[i];
            for (String reqName : part.require) {
                Part reqPart = namePart.get(reqName);
                if (reqPart.isBefore(part)) continue;
                reqPart.moveBefore(part);
            }
        }

        for (int i = 0; i < parts.length; i++) {
            part = parts[i];
            for (String reqName : part.require) {
                Part reqPart = namePart.get(reqName);
                if (reqPart.isBefore(part)) continue;
                StringBuffer message = new StringBuffer();
                message.append("Обнаружена циклическая зависимость:");
                message.append(String.format("\r\n%s require %s", part.file, reqPart.file));
                ArrayList<Part> prevs = new ArrayList<>();
                prevs.add(part);
                Part np = part;
                while ((np = np.next) != reqPart.next) {
                    boolean req = false;
                    for (Part prev : prevs) {
                        if (np.isRequire(prev)) {
                            message.append(String.format("\r\n%s require %s", np.file, prev.file));
                            req = true;
                        }
                    }
                    if (req) {
                        prevs.add(np);
                    }
                }
                throw new RuntimeException(message.toString());
            }
        }

        Part first = parts[0];
        while ((part = first.prev) != null) {
            first = part;
        }
        part = first;
        for (int i = 0; i < parts.length; i++) {
            parts[i] = part;
            part = part.next;
        }
    }

    void save() {
        StringBuffer message = new StringBuffer();
        message.append("Файлы отсортированы в следующем порядке:\r\n");
        for (Part part : parts) {
            message.append(part.file);
            message.append("\r\n");
        }
        System.out.print(message);

        try (FileWriter writer = new FileWriter(out.toFile())) {
            for (Part part : parts) {
                writer.write(part.text);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
