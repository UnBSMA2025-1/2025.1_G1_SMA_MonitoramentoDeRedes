package core;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private final Map<String, Integer> requests = new HashMap<>();

    public synchronized void registerRequest(String ip) {
        requests.put(ip, requests.getOrDefault(ip, 0) + 1);
        // Empurra para o DataStore
        DataStore.getInstance().logRequest(ip);
    }

    public synchronized Map<String, Integer> getRequestSnapshot() {
        return new HashMap<>(requests);
    }

    public synchronized void resetRequests() {
        requests.clear();
    }
}