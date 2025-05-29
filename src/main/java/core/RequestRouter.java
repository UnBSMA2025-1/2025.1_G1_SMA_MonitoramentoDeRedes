package core;

import infra.MonitorGateway;

import static spark.Spark.*;
import java.util.Set;
import java.util.concurrent.*;

import infra.MonitoringAPI;

public class RequestRouter {
    private static MonitorGateway monitor;

    private static final Set<String> blockedIps = ConcurrentHashMap.newKeySet();

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void registerMonitor(MonitorGateway m) {
        monitor = m;
    }

    public static void blockIp(String ip) {
        blockedIps.add(ip);

        //registra no DataStore
        DataStore.getInstance().blockedIPs.add(ip);
        DataStore.getInstance().logAlert("[ROUTER] IP bloqueado: " + ip);

        System.out.println("[ROUTER] IP bloqueado: " + ip);
    }

    public static void startServer() {
        port(8080);
        staticFiles.location("/public");
        MonitoringAPI.init();

        post("/", (req, res) -> {
            String ip = req.headers("X-Real-IP");
            if (ip == null) ip = req.ip();

            if (blockedIps.contains(ip)) {
                res.status(403);
                return "Blocked";
            }

            if (monitor != null) {
                final String ipCopy = ip;
                executor.submit(() -> {
                    try {
                        monitor.receiveRequest(ipCopy);
                    } catch (Exception e) {
                        System.err.println("[ROUTER] Erro ao enviar IP ao monitor: " + e.getMessage());
                    }
                });
            }

            return "Request sent to MonitorAgent";
        });

        get("/", (req, res) -> {
            res.redirect("/site/index.html");
            return null;
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
