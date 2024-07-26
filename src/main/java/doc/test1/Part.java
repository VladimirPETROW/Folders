package doc.test1;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Part {

    String name;
    Path file;
    String text;
    ArrayList<String> require;

    static Pattern pattern = Pattern.compile("require\\s+'([^']*)'");

    public Part(String n, Path f, List<String> lines) {
        name = n;
        file = f;
        StringBuffer buffer = new StringBuffer();
        ArrayList<String> req = new ArrayList<>(lines.size());
        for (String line : lines) {
            StringBuffer lb = new StringBuffer(line.length());
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                matcher.appendReplacement(lb, "");
                String other = matcher.group(1);
                req.add(other);
            }
            matcher.appendTail(lb);
            if (lb.length() > 0) {
                buffer.append(lb);
                buffer.append("\r\n");
            }
        }
        text = buffer.toString();
        require = req;
    }
}
