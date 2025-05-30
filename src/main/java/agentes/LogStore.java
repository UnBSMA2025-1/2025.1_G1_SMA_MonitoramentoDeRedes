package agentes;

import java.util.LinkedList;
import java.util.List;

public class LogStore {
    private static final List<String> logs = new LinkedList<>();

    public static synchronized void add(String msg) {
        logs.add(msg);
        if (logs.size() > 500) {
            logs.remove(0);
        }
    }

    public static synchronized List<String> getLastLogs() {
        return new LinkedList<>(logs);
    }
}
