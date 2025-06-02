package core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger; 

public class Node {
    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 5000; 

    public void registerRequest(String ip) {
        long now = System.currentTimeMillis();
        requestTimestamps.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
        
        Deque<Long> timestamps = requestTimestamps.get(ip);
        timestamps.add(now);
        
        // Remover registros fora da janela
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
            timestamps.pollFirst();
        }
    }

    public int getRequestCount(String ip) {
        Deque<Long> timestamps = requestTimestamps.get(ip);
        return timestamps != null ? timestamps.size() : 0;
    }

    public Map<String, Integer> getRequestSnapshot() {
        Map<String, Integer> snapshot = new HashMap<>();
        long now = System.currentTimeMillis();
        
        requestTimestamps.forEach((ip, timestamps) -> {
            // Limpar registros antigos
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }
            snapshot.put(ip, timestamps.size());
        });
        
        return snapshot;
    }
}