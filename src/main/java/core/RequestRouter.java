package core;

import infra.MonitorGateway;
import static spark.Spark.*;
import java.util.Set;
import java.util.concurrent.*;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RequestRouter {
    private static MonitorGateway monitor;
    private static final Set<String> blockedIps = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    

    private static boolean authenticate(String username, String password) {
        Map<String, String> validUsers = Map.of(
            "admin", "admin123",
            "user1", "password1",
            "TestUser", "12345",
            "Ataque0", "123"
        );
        return validUsers.containsKey(username) && 
            validUsers.get(username).equals(password);
    }
    public static void registerMonitor(MonitorGateway m) {
        monitor = m;
    }

    public static void blockIp(String ip) {
        blockedIps.add(ip);
        System.out.println("[ROUTER] IP bloqueado: " + ip);
    }

    public static void startServer() {
        port(8080);
        staticFiles.location("/public");

        post("/login", (req, res) -> {
            String ip = req.headers("X-Real-IP");
            if (ip == null) ip = req.ip();

            if (blockedIps.contains(ip)) {
                res.status(403);
                return "Blocked";
            }

            String User_Password = req.body();
            JsonObject json;
            try {
                json = JsonParser.parseString(User_Password).getAsJsonObject();
            } catch (Exception e) {
                res.status(400);
                return "{\"status\":\"fail\", \"message\":\"Invalid JSON\"}";
            }
            String username = json.has("username") ? json.get("username").getAsString() : "";
            String password = json.has("password") ? json.get("password").getAsString() : "";
            if (monitor != null) {
                boolean allowed = monitor.receiveRequest(ip, User_Password);
                if (!allowed) {
                    res.status(403);
                    return "{\"status\":\"blocked_by_monitor\"}";
                }
            }
            if (authenticate(username, password)) {
                res.status(200);
                return "{\"status\":\"success\"}";
            } else {
                res.status(401);
                return "{\"status\":\"fail\"}";
            }

        });

        post("/reset", (req, res) -> {
            blockedIps.clear();
            return "Blocked IPs reset.";
        });
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
