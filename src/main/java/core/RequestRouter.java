package core;

import infra.MonitorGateway;
import infra.MonitoringAPI;
import static spark.Spark.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class RequestRouter {
    private static MonitorGateway monitor;
    private static final Set<String> blockedIps = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final Gson gson = new Gson();
    private static final Type mapType = new TypeToken<Map<String, String>>(){}.getType();

    public static void registerMonitor(MonitorGateway m) {
        monitor = m;
    }

    public static void blockIp(String ip) {
        if (!blockedIps.contains(ip)) {
        blockedIps.add(ip);
        DataStore.getInstance().blockedIPs.add(ip);
        DataStore.getInstance().logAlert("[ROUTER] IP bloqueado: " + ip);
        System.out.println("[ROUTER] IP bloqueado: " + ip);
        }
    }

    // Método auxiliar estático para validar JSON
    private static boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    public static void startServer() {
        port(8080);
        staticFiles.location("/public");
        MonitoringAPI.init();


        get("/", (req, res) -> {
            String ip = Optional.ofNullable(req.headers("X-Real-IP")).orElse(req.ip());
            if (blockedIps.contains(ip)) {
                res.status(403);
                return "Blocked";
            }
            if (monitor != null) monitor.receiveRequest(ip);
            return "Request sent to MonitorAgent";
        });

    
        post("/login", (req, res) -> {
    String ip = Optional.ofNullable(req.headers("X-Real-IP")).orElse(req.ip());
    DataStore store = DataStore.getInstance();
    if (monitor != null) monitor.receiveRequest(ip);

    // Verificação de IP bloqueado
    if (store.isIpBlocked(ip)) {
        res.status(403);
        return gson.toJson(Map.of(
            "status", "blocked",
            "message", "IP bloqueado por segurança"
        ));
    }
if (store.isPotentialDos(ip)) { 
    res.status(429); // Too Many Requests
    return gson.toJson(Map.of(
        "status", "error", 
        "message", "Muitas requisições. Tente novamente mais tarde."
    ));
}

    // Validação do JSON
    if (!isValidJson(req.body())) {
        store.logSuspiciousActivity(ip, "JSON inválido");
        res.status(400);
        return gson.toJson(Map.of(
            "status", "error",
            "message", "Formato JSON inválido"
        ));
    }

    try {
        Map<String, String> body = gson.fromJson(req.body(), mapType);
        String username = body.get("username");
        String password = body.get("password");

        // Validação de campos obrigatórios
        if (username == null || password == null) {
            res.status(400);
            return gson.toJson(Map.of(
                "status", "error",
                "message", "Username e password são obrigatórios"
            ));
        }

        if (store.isSqlInjectionAttempt(username) || store.isSqlInjectionAttempt(password)) {
            store.logAttackAttempt(ip, username, "SQLi");
            
            if (store.getAttackAttempts(ip) >= 3) {
                blockIp(ip);
                res.status(403);
                return gson.toJson(Map.of(
                    "status", "blocked",
                    "message", "IP bloqueado por tentativas de ataque"
                ));
            }
            
            res.status(422);
            return gson.toJson(Map.of(
                "status", "invalid",
                "message", "Credenciais inválidas"
            ));
        }

        if (authenticate(username, password)) {
            return gson.toJson(Map.of(
                "status", "success",
                "message", "Login realizado"
            ));
        } else {
            return gson.toJson(Map.of(
                "status", "invalid",
                "message", "Credenciais incorretas"
            ));
        }

    } catch (Exception e) {
        store.logError(ip, e);
        res.status(500);
        return gson.toJson(Map.of(
            "status", "error",
            "message", "Erro interno no servidor"
        ));
    }
});

        post("/reset", (req, res) -> {
            blockedIps.clear();
            DataStore.getInstance().blockedIPs.clear();
            return "Blocked IPs reset.";
        });

        init();
        awaitInitialization();
        System.out.println("[ROUTER] HTTP server started on port 8080");
    }

    private static boolean authenticate(String username, String password) {
        Map<String, String> validUsers = Map.of(        // Usuarios que podem logar
            "admin", "admin123",
            "user1", "password1",
            "TestUser", "12345"
        );
        return validUsers.containsKey(username) && 
               validUsers.get(username).equals(password);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}