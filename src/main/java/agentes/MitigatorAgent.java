package agentes;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import core.RequestRouter;

public class MitigatorAgent extends Agent {
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("mitigation");
        sd.setName("ip-blocker");
        dfd.addServices(sd);

        try {
            DFService.deregister(this);
        } catch (Exception e) {
            System.out.println("[MITIGATOR] Nenhum registro anterior para o agente mitigador no DF. Prosseguindo...");
        }
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("[MITIGATOR] Agente mitigador iniciado.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getConversationId().equals("mitigation-request")) {
                    String ip = msg.getContent();
                    System.out.println("[MITIGATOR] Bloqueando IP: " + ip);
                    RequestRouter.blockIp(ip);
                }
                else if (msg != null && msg.getContent().startsWith("ATTACK:")) {
                    String ip = msg.getContent().split(":")[1];
                    System.out.println("[MITIGATOR] Bloqueio proativo para: " + ip);
                    RequestRouter.blockIp(ip);
                } else if (msg != null && msg.getContent().equals("ping-mitigator")) {
                    ACLMessage reply = msg.createReply();
                    reply.setContent("pong-mitigator");
                    send(reply);
                } else {
                    block();
                }
            }
        });

    }
}