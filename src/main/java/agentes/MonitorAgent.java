package agentes;

import core.Node;
import core.RequestRouter;
import infra.MonitorGateway;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.time.Instant;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import core.RequestRecord;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import core.UserStore;

public class MonitorAgent extends Agent implements MonitorGateway {
    private Node node;
    private final List<RequestRecord> recentRequests = new LinkedList<>();

    // Códigos ANSI cor
    public static final String RESET = "\u001B[0m";
    public static final String VERMELHO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String AZUL = "\u001B[34m";

    protected void setup() {
        node = new Node();
        RequestRouter.registerMonitor(this);
        System.out.println("[MONITOR] Agente monitor iniciado.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getContent().equals("ping-monitor")) {
                    ACLMessage reply = msg.createReply();
                    reply.setContent("pong-monitor");
                    send(reply);
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                Map<String, Integer> snapshot = node.getRequestSnapshot();
                for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
                    if (entry.getValue() > 5) {     // Reduzi o n° necessario para bloquear (o sistema parece estar mais lento por conta de um processo sincrono)
                        String ip = entry.getKey(); // O processo sincrono foi necessario para o sistema não permitir o injection e depois bloquear
                        System.out.println("[MONITOR] IP suspeito: " + ip);
                        notifyMitigator(ip);
                    }
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                node.resetRequests();
            }
        });
    }

//----------------------------------------------------------------------------------------------------------------------------------

    private void notifyMitigator(String ip) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("mitigation");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    for (DFAgentDescription dfd : result) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(dfd.getName());
                        msg.setContent(ip);
                        msg.setConversationId("mitigation-request");
                        System.out.println("[MONITOR] Enviando pedido de bloqueio para agente mitigador...");
                        send(msg);
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        });
    }

//----------------------------------------------------------------------------------------------------------------------------------

    public void receiveRequest(String ip, String payload) {
        node.registerRequest(ip);

        synchronized (recentRequests) {
            recentRequests.add(new RequestRecord(ip, Instant.now()));
            if (recentRequests.size() > 1000) {
                recentRequests.remove(0);
            }
        }
        try {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(payload, JsonObject.class);
        String username = obj.get("username").getAsString();
        String password = obj.get("password").getAsString();

        System.out.printf("[MONITOR] Requisição de %s: user=%s, pass=%s%n", ip, username, password);
        if (possivelSQLInjection(ip, payload)) {
            System.out.println("[MONITOR] SQL Injection detectado de: " + ip);
            notifyMitigator(ip);
            return;
        }

        if (!UserStore.isValid(username, password)) {
            System.out.println("[MONITOR]" + VERMELHO + " Tentativa inválida de login de " + ip + RESET);   // Senha e/ou Usuario errado
        }
        else {
            System.out.println("[MONITOR]" + VERDE + " Login completo  " + ip + RESET);
        }
        } 
        catch (Exception e) {
            System.err.println("[MONITOR] Erro ao processar JSON: " + e.getMessage());
        }
    }

//----------------------------------------------------------------------------------------------------------------------------------

    public boolean possivelSQLInjection(String ip, String body) {
        if (body == null) return false;
        String upper = body.toUpperCase();
        // Algun tipos de injection
        boolean found = upper.contains("\" OR")
                    || upper.contains("DROP TABLE")
                    || upper.contains("UNION SELECT")
                    || upper.contains("'--")
            // Comente aq se quiser mostrar o "roubo" dos dados
                    // || upper.contains("'%")
                    // ||upper.contains("' OR")
                    // || upper.contains("1=1")
                    // || upper.contains("--")
            // Fim
                    || upper.contains(";--");

        if (found) {
            System.out.printf("[MONITOR] SQL Injection detectado de: %s → %s%n", ip, body);
            notifyMitigator(ip);  
            return true;    // Ameaça return para o receiveRequest
        }

        return false;   // Seguro, return para o receiveRequest
    }


    @Override
    public Map<String, Integer> getRequestCountsPerIp() {
        return node.getRequestSnapshot();
    }

    @Override
    public List<RequestRecord> getRecentRequests() {
        synchronized (recentRequests) {
            return new ArrayList<>(recentRequests);
        }
    }

}