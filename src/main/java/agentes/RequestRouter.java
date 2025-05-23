package agentes;

import javax.management.monitor.Monitor;

import static spark.Spark.*;
import java.util.HashSet;
import java.util.Set;

public class RequestRouter {
    private static MonitorGateway monitor;
    private static final Set<String> blockedIps = new HashSet<>();

    public static void registerMonitor(MonitorGateway m) {
        monitor = m;
    }

    public static void blockIp(String ip) {
        blockedIps.add(ip);
    }

    public static void startServer() {
        port(8080);
        post("/", (req, res) -> {
            String ip = req.headers("X-Real-IP");
            if (ip == null) ip = req.ip();

            if (blockedIps.contains(ip)) {
                res.status(403);
                return "Blocked";
            }

            if (monitor != null) {
                monitor.receiveRequest(ip);
            }
            return "Request send to MonitorAgent";
        });
    }
}