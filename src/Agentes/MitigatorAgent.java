package src.Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;

public class MitigatorAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        // Registrar serviço de mitigação no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("mitigacao-ataques");
        sd.setName("servico-mitigador");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("Mitigador registrado no DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Adiciona comportamento para responder ao protocolo FIPA_REQUEST
        MessageTemplate mt = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        addBehaviour(new AchieveREResponder(this, mt) {
            protected ACLMessage handleRequest(ACLMessage request) {
                String ip = request.getContent();
                System.out.println("Recebido pedido para bloquear IP: " + ip);

                ACLMessage reply = request.createReply();

                boolean sucesso = blockIp(ip);
                if (sucesso) {
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Bloqueio realizado");
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Falha ao tentar bloquear IP");
                }

                return reply;
            }
        });
    }

    private boolean blockIp(String ip) {
        // Aqui vai sua lógica real de mitigação
        System.out.println("Bloqueando IP: " + ip);
        return true;
    }
}
