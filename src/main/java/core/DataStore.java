package core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static DataStore instance;

    public final Map<String, Integer> requestsPerIP = new ConcurrentHashMap<>();
    public final Set<String> blockedIPs = Collections.synchronizedSet(new HashSet<>());
    public final List<String> logs = Collections.synchronizedList(new ArrayList<>());
    public final List<RequestEntry> recentRequests = Collections.synchronizedList(new ArrayList<>());

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public void logRequest(String ip) {
        requestsPerIP.merge(ip, 1, Integer::sum);
        recentRequests.add(new RequestEntry(ip, System.currentTimeMillis()));
        if (recentRequests.size() > 100) {
            recentRequests.remove(0);
        }
    }

    public void logAlert(String message) {
        logs.add("[" + new Date() + "] " + message);
        if (logs.size() > 100) logs.remove(0);
    }

    public record RequestEntry(String ip, long timestamp) {}
}
