package core;

import infra.MonitorGateway;

import static spark.Spark.*;
import java.util.Set;
import java.util.concurrent.*;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


import java.util.UUID;

import com.google.gson.Gson;

import infra.MonitoringAPI;

/* JSON (Gson) */
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/* JDBC */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class RequestRouter {
    
    private static ContainerController container;

    public static void setContainer(ContainerController cc) {
        container = cc;
    }


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

        String line = "[ROUTER] IP bloqueado: " + ip;
        System.out.println(line);
        LogStore.add(line);

    }

    public static void startServer() {
        port(8080);
        staticFiles.location("/public");
        MonitoringAPI.init();

        Gson gson = new Gson();

        post("/", (req, res) -> {
            String ip = req.headers("X-Real-IP");
            if (ip == null) ip = req.ip();

            if (blockedIps.contains(ip)) {
                res.status(403);
                return "Blocked";
            }
            String body = req.body();   // Dados enviados pelo ataque/user (usuario + senha)
            if (monitor != null) {
                final String ipCopy = ip;
                final String bodyCopy = body;
                executor.submit(() -> {
                    try {
                        monitor.receiveRequest(ipCopy, bodyCopy);   // Envia os dados para o monitor conferir
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
        
        get("/", (req, res) -> {
            res.redirect("/site/dashboard.html");
            return null;
        });
        
        post("/reset", (req, res) -> {
            blockedIps.clear();
            return "Blocked IPs reset.";
        });

        get("/api/stats", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getRequestCountsPerIp());
        });

        get("/api/blocked", (req, res) -> {
            res.type("application/json");
            return gson.toJson(blockedIps);
        });

        get("/api/requests", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getRecentRequests());
        });

        get("/api/logs", (req, res) -> {
            res.type("application/json");
            return gson.toJson(LogStore.getLastLogs());
        });
        post("/search_user", (req, res) -> {    // Nova rota criada
            res.type("text/plain");             // Obs: A entrada recebida é diferente da outra rota

            String ip = req.headers("X-Real-IP");
            if (ip == null) ip = req.ip();
            String body = req.body();

            //  Verificação direta com o monitor se é injection, tenho que fazer a verificação de DOS aqui também ainda
            if (monitor != null && monitor.possivelSQLInjection(ip, body)) {
                System.out.printf("[ROUTER] SQLi detectado em %s → %s%n", ip, body);
                res.status(403);
                return "Blocked by Monitor";
            }

            // Executa SQL (inseguro, propositalmente)
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            String filtro = obj.get("filter").getAsString();

            String sql = "SELECT username, password FROM usuarios WHERE username LIKE '%" + filtro + "%'";

            StringBuilder resposta = new StringBuilder();
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:users.db");
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    resposta.append(rs.getString("username"))
                            .append(":")
                            .append(rs.getString("password"))
                            .append("\n");
                }
            } catch (Exception e) {
                res.status(500);
                return "Erro ao consultar banco";
            }

            return resposta.toString();
        });



    }

    public static void shutdown() {
        executor.shutdown();
    }
}
