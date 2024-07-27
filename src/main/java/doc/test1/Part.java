package doc.test1;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Part {

    String name;
    Path file;
    String text;
    String[] require;

    Part prev, next;

    static Pattern pattern = Pattern.compile("require\\s+'([^']*)'");

    public Part(String n, Path f, String t, Path root) {
        name = n;
        file = root.relativize(f);
        HashSet<String> req = new HashSet<>();
        StringBuffer buffer = new StringBuffer(t.length());
        Matcher matcher = pattern.matcher(t);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
            String require = matcher.group(1);
            Path other = root.resolve(require);
            if (!other.toFile().exists()) {
                throw new RuntimeException(String.format("Файл из директивы require не найден %s", other));
            }
            String otherName = other.getFileName().toString();
            if (otherName.equals(n)) {
                throw new RuntimeException(String.format("Файл содержит директиву require сам на себя %s", f));
            }
            req.add(otherName);
        }
        matcher.appendTail(buffer);
        text = buffer.toString();
        require = req.stream().sorted().toArray(size -> new String[size]);
    }

    public Part append(Part part) {
        next = part;
        part.prev = this;
        return part;
    }

    public boolean isBefore(Part part) {
        while ((part = part.prev) != null) {
            if (part.name.equals(name)) return true;
        }
        return false;
    }

    public boolean isRequire(Part part) {
        return Arrays.binarySearch(require, part.name) >= 0;
    }

    public void moveBefore(Part part) {
        remove();
        if (part.prev != null) {
            part.prev.append(this);
        }
        this.append(part);
    }

    public void remove() {
        if (prev != null) {
            if (next != null) {
                prev.append(next);
                next = null;
            }
            else {
                prev.next = null;
            }
            prev = null;
        }
        else {
            if (next != null) {
                next.prev = null;
                next = null;
            }
        }
    }
}
