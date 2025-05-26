package agentes;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class SupervisorAgent extends Agent {
    private static final String MONITOR_NAME = "monitor";
    private static final String MITIGATOR_NAME = "mitigator";
    private boolean monitorAlive = true;
    private boolean mitigatorAlive = true;

    protected void setup() {
        System.out.println("[SUPERVISOR] Supervisor iniciado.");

        // Envia ping para monitor
        addBehaviour(new TickerBehaviour(this, 7000) {
            protected void onTick() {
                ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
                ping.addReceiver(new jade.core.AID(MONITOR_NAME, AID.ISLOCALNAME));
                ping.setContent("ping-monitor");
                ping.setConversationId("ping-monitor");
                send(ping);
                monitorAlive = false;
            }
        });

        // Envia ping pra mitigador
        addBehaviour(new TickerBehaviour(this, 14000) {
            protected void onTick() {
                ACLMessage ping = new ACLMessage(ACLMessage.REQUEST);
                ping.addReceiver(new jade.core.AID(MITIGATOR_NAME, AID.ISLOCALNAME));
                ping.setContent("ping-mitigator");
                ping.setConversationId("ping-mitigator");
                send(ping);
                mitigatorAlive = false;
            }
        });

        // Checa se monitor tá vivo
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                if (!monitorAlive) {
                    System.out.println("[SUPERVISOR] Agente monitor não respondeu. Reiniciando...");
                    try {
                        ContainerController cc = getContainerController();
                        AgentController existing = null;
                        try {
                            existing = cc.getAgent(MONITOR_NAME);
                        } catch (ControllerException e) {

                        }

                        if (existing != null) {
                            try {
                                existing.kill();
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                System.err.println("[SUPERVISOR] Falha ao matar agente monitor: " + e.getMessage());
                            }
                        }
                        AgentController newMonitor = cc.createNewAgent(MONITOR_NAME, "agentes.MonitorAgent", null);
                        newMonitor.start();
                        monitorAlive = true;
                        System.out.println("[SUPERVISOR] Agente monitor reiniciado com sucesso.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Checa se mitigador tá vivo
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                if (!mitigatorAlive) {
                    System.out.println("[SUPERVISOR] Agente mitigador não respondeu. Reiniciando...");
                    try {
                        ContainerController cc = getContainerController();
                        AgentController existing = null;
                        try {
                            existing = cc.getAgent(MITIGATOR_NAME);
                        } catch (ControllerException e) {

                        }

                        if (existing != null) {
                            try {
                                existing.kill();
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                System.err.println("[SUPERVISOR] Falha ao matar agente mitigador: " + e.getMessage());
                            }
                        }
                        AgentController newMonitor = cc.createNewAgent(MITIGATOR_NAME, "agentes.MitigatorAgent", null);
                        newMonitor.start();
                        mitigatorAlive = true;
                        System.out.println("[SUPERVISOR] Agente mitigador reiniciado com sucesso.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                MessageTemplate mt = MessageTemplate.MatchContent("pong-mitigator");
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    mitigatorAlive = true;
                    System.out.println("[SUPERVISOR] Agente mitigador está vivo.");
                }
            }
        });

        // Pong do monitor
        addBehaviour(new TickerBehaviour(this, 500) {
            protected void onTick() {
                MessageTemplate mt = MessageTemplate.MatchContent("pong-monitor");
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    monitorAlive = true;
                    System.out.println("[SUPERVISOR] Agente monitor está vivo.");
                }
            }
        });
    }
}
