package core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStore {
    private static DataStore instance;
    
    public final Map<String, Integer> requestsPerIP = new ConcurrentHashMap<>();
    public final Set<String> blockedIPs = ConcurrentHashMap.newKeySet();
    public final List<String> logs = new CopyOnWriteArrayList<>();
    public final List<RequestEntry> recentRequests = new CopyOnWriteArrayList<>();
    public final Map<String, Integer> attackAttempts = new ConcurrentHashMap<>();
    public final Map<String, String> attackPayloads = new ConcurrentHashMap<>();
    
    // Padrões de ataque
    private final List<AttackPattern> maliciousPatterns = Arrays.asList(
        new AttackPattern("SQL", ".*([';]+|(--)+).*"),
        new AttackPattern("SQL", ".*(OR\\s+1=1).*"),
        new AttackPattern("XSS", "<script>.*</script>")
    );

    private DataStore() {} 

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public void logRequest(String ip) {
        requestsPerIP.merge(ip, 1, Integer::sum);
        recentRequests.add(new RequestEntry(ip, System.currentTimeMillis()));
        if (recentRequests.size() > 1000) {
            recentRequests.remove(0);
        }
    }

    public void logAlert(String message) {
        String logEntry = String.format("[%s] ALERTA: %s", 
            new Date().toString(), message);
        logs.add(logEntry);
        if (logs.size() > 1000) logs.remove(0);
    }

    public void logAttackAttempt(String ip, String payload, String type) {
        attackAttempts.merge(ip, 1, Integer::sum);
        attackPayloads.put(ip, payload);
        
        String logEntry = String.format("[%s] ATAQUE %s - IP: %s | Payload: %s", 
            new Date().toString(), type, ip, payload);
        logs.add(logEntry);
    }

    public boolean isIpBlocked(String ip) {
        return blockedIPs.contains(ip) || attackAttempts.getOrDefault(ip, 0) >= 3;
    }

    public boolean isSqlInjectionAttempt(String input) {
        return maliciousPatterns.stream()
            .anyMatch(pattern -> input.matches(pattern.pattern()));
    }

    public int getAttackAttempts(String ip) {
        return attackAttempts.getOrDefault(ip, 0);
    }

    public void logSuspiciousActivity(String ip, String activity) {
        String logEntry = String.format("[%s] SUSPEITA - IP: %s - %s", 
            new Date().toString(), ip, activity);
        logs.add(logEntry);
    }

    public void logError(String ip, Exception e) {
        String logEntry = String.format("[%s] ERRO - IP: %s - %s: %s", 
            new Date().toString(), ip, e.getClass().getSimpleName(), e.getMessage());
        logs.add(logEntry);
    }

 public void cleanOldRequests() {
    long now = System.currentTimeMillis();
    recentRequests.removeIf(entry -> (now - entry.timestamp()) > 3600000); 
}
    public boolean isKnownAttacker(String ip) {
    return attackAttempts.getOrDefault(ip, 0) > 0 || blockedIPs.contains(ip);
}

public boolean isPotentialDos(String ip) {
    return requestsPerIP.getOrDefault(ip, 0) > 100; 
}

    public record RequestEntry(String ip, long timestamp) {}
    public record AttackPattern(String type, String pattern) {}
}