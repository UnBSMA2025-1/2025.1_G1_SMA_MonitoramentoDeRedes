package agentes;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import java.util.regex.Pattern;

public class MonitorAgent extends Agent implements MonitorGateway {

    private Node node;

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
                    if (entry.getValue() > 10) {
                        String ip = entry.getKey();
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

    private String classifyAttack(int count, String payload, String header, String url) {
        if (count > 100) return "dos-flood";
        if (count > 10) return "basic-dos";
        if (header != null && header.contains("keep-alive") && count > 5) return "slowloris";
        if (url != null && Pattern.matches(".*\" *or *1=1.*", url.toLowerCase())) return "sql-injection";
        if (payload != null && Pattern.matches(".*<script>.*</script>.*", payload.toLowerCase())) return "xss";
        if (header != null && url != null && header.toLowerCase().contains("..") && url.contains("/etc/passwd")) return "path-traversal";
        if (url != null && url.contains(".php") && url.contains("?file=")) return "rfi";
        return null;
    }

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

    private void askForReport(String ip, String type) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("report");
                    template.addServices(sd);

                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    for (DFAgentDescription dfd : result) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(dfd.getName());
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void receiveRequest(String ip) {
        node.registerRequest(ip);
    }
}