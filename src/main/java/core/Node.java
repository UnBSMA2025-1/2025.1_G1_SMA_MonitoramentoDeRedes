package core;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private final Map<String,Integer> requests = new HashMap<>();

    public synchronized void registerRequest(String ip) {
        requests.put(ip, requests.getOrDefault(ip, 0) + 1);
        DataStore.getInstance().logRequest(ip);
    }

    /** NOVO: devolve a contagem para um IP específico */
    public synchronized int getRequestCount(String ip) {
        return requests.getOrDefault(ip, 0);
    }

    public synchronized Map<String,Integer> getRequestSnapshot() {
        return new HashMap<>(requests);
    }

    public synchronized void resetRequests() {
        requests.clear();
    }
}
