package src.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.HashSet;
import java.util.Set;

public class MitigatorAgent extends Agent {

    private Set<String> blacklist = new HashSet<>();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new RegistroDFBehavior());
        addBehaviour(new AguardarBloqueiosBehavior());
    }

    // 1. Registra este agente como "mitigador" nas Páginas Amarelas
    private class RegistroDFBehavior extends OneShotBehaviour {
        public void action() {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("mitigador");
            sd.setName("ServicoMitigador");
            dfd.addServices(sd);

            try {
                DFService.register(myAgent, dfd);
                System.out.println(getLocalName() + " registrado no DF como 'mitigador'.");
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    // 2. Recebe mensagens com IPs a serem bloqueados
    private class AguardarBloqueiosBehavior extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null && msg.getConversationId() != null && msg.getConversationId().equals("bloquear-ip")) {
                String ip = msg.getContent();
                if (!blacklist.contains(ip)) {
                    blacklist.add(ip);
                    System.out.println("[MITIGADOR] IP bloqueado: " + ip);
                } else {
                    System.out.println("[MITIGADOR] IP já estava bloqueado: " + ip);
                }
            } else {
                block();
            }
        }
    }

    public boolean isBloqueado(String ip) {
        return blacklist.contains(ip);
    }
}
