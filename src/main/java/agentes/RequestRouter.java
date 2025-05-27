package agentes;

import static spark.Spark.*;
import java.util.Set;
import java.util.concurrent.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


import java.util.UUID;


public class RequestRouter {
    private static ContainerController container;

    public RequestRouter(ContainerController c) {
        container = c;
    }

    public static void criarAgente() throws Exception {
        String userName = UUID.randomUUID().toString().replace("-", ""); // Para garantir que não vai dar problema no Jade
        AgentController user = container.createNewAgent("User" + userName, "agentes.AttackAgent", null);
        user.start();
        System.out.println("[RequestRouter] Agente criado: User" + userName);
    }

    private static MonitorGateway monitor;

    public static void setContainer(ContainerController cont) {
        container = cont;
    }

    private static final Set<String> blockedIps = ConcurrentHashMap.newKeySet();

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void registerMonitor(MonitorGateway m) {
        monitor = m;
    }

    public static void blockIp(String ip) {
        blockedIps.add(ip);
        System.out.println("[ROUTER] IP bloqueado: " + ip);

        try {
            criarAgente();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
