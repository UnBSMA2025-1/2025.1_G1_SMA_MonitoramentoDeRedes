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


public class MonitorAgent extends Agent implements MonitorGateway {
    private Node node;
    private final List<RequestRecord> recentRequests = new LinkedList<>();

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
    @Override
    public void receiveRequest(String ip) {
        node.registerRequest(ip);

        synchronized (recentRequests) {
            recentRequests.add(new RequestRecord(ip, Instant.now()));
            if (recentRequests.size() > 1000) {
                recentRequests.remove(0);
            }
        }
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