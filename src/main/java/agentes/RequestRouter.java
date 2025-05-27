package agentes;

import static spark.Spark.*;
import java.util.Set;
import java.util.concurrent.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RequestRouter {
    private static MonitorGateway monitor;
    private static final Set<String> blockedIps = ConcurrentHashMap.newKeySet();
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void registerMonitor(MonitorGateway m) {
        monitor = m;
    }

    public static void blockIp(String ip) {
        blockedIps.add(ip);
        System.out.println("[ROUTER] IP bloqueado: " + ip);
    }

    public static void startServer() {
        port(8080);

        // Configuração dos arquivos estáticos
        staticFiles.location("/public"); // Pasta src/main/resources/public

        get("/", (req, res) -> {
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
                        System.err.println("[ROUTER] Erro ao enviar IP ao monitor.");
                    }
                });
            }

            // Serve o arquivo HTML
            try (InputStream is = RequestRouter.class.getResourceAsStream("/public/teste.html")) {
                if (is == null) {
                    res.status(404);
                    return "Arquivo não encontrado";
                }

                res.type("text/html");
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                res.status(500);
                return "Erro ao ler o arquivo";
            }
        });

        // Restante das rotas mantido igual
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

        post("/reset", (req, res) -> {
            blockedIps.clear();
            return "Blocked IPs reset.";
        });
    }

    public static void shutdown() {
        executor.shutdown();
    }
}