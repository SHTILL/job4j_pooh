package pooh;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Immutable
public class Req {
    private static final Pattern[] PATTERNS = {
            Pattern.compile("^POST\\s+?/queue/[\\w]+?.+$"),
            Pattern.compile("^POST\\s+?/topic/[\\w]+?.+$"),
            Pattern.compile("^GET\\s+?/queue/[\\w]+?.+$"),
            Pattern.compile("^GET\\s+?/topic/[\\w]+?/[\\d]+?.+$")
    };

    private final String mode;
    private final String method;
    private final Map<String, String> values = new HashMap<>();

    public Req(String text) throws IllegalArgumentException {
        boolean matched = false;
        String localMode = null;
        String localMethod = null;

        String[] lines = text.split("\\r?\\n");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Incorrect Request Format");
        }

        for (Pattern p: PATTERNS) {
            Matcher m = p.matcher(lines[0]);
            if (m.matches()) {
                String[] tokensMethod = lines[0].split("\\s+?");
                localMethod = tokensMethod[0];
                String[] tokensQueue = tokensMethod[1].substring(1).split("/");
                localMode = tokensQueue[0];
                values.put("name", tokensQueue[1]);
                if (tokensQueue.length == 3) {
                    values.put("id", tokensQueue[2]);
                }
                matched = true;
                break;
            }
        }
        if (!matched) {
            throw new IllegalArgumentException("Request doesn't match patterns");
        }

        values.put("text", lines[lines.length-1]);
        mode = localMode;
        method = localMethod;
    }

    @Nullable
    public String valueOf(String key) {
        return values.get(key);
    }

    @Nullable
    public String mode() {
        return mode;
    }

    @Nullable
    public String method() {
        return method;
    }

    @Override
    public String toString() {
        return "Req{" +
                "mode='" + mode + '\'' +
                ", method='" + method + '\'' +
                ", values=" + values +
                '}';
    }
}
