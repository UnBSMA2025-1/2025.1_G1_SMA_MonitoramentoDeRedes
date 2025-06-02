package agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.core.behaviours.OneShotBehaviour;


import jade.wrapper.AgentController;
import java.util.UUID;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.behaviours.SimpleBehaviour;




public class CreateAttackAgent extends Agent {

    private static  int userName = 1;
    
protected void setup() {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Create-Attack");
    sd.setName(getLocalName());
    dfd.addServices(sd);
    try { DFService.register(this, dfd); }
    catch (FIPAException e) { e.printStackTrace(); }

    MessageTemplate mt = MessageTemplate.and(
        MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
        MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
    );

    addBehaviour(new CyclicBehaviour(this) {
        public void action() {
            final ACLMessage msg = receive(mt);
            if (msg != null) {
                System.out.println(getLocalName() + ": criando agente pedido.");
                addBehaviour(new WakerBehaviour(myAgent, 3000) {
                    protected void onWake() {
                        boolean sucesso = CreateAgent();
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(sucesso ? ACLMessage.INFORM : ACLMessage.FAILURE);
                        reply.setContent(sucesso ? "Agente criado com sucesso" : "Falha ao criar agente");
                        myAgent.send(reply);
                    }
                });
            } else {
                block();
            }
        }
    });
}

protected void takeDown() {
    try {
        DFService.deregister(this);
    } catch (FIPAException fe) { fe.printStackTrace(); }
    System.out.println(getLocalName() + " encerrado.");
}


    public boolean CreateAgent() {
        try {
            AgentController user = getContainerController().createNewAgent("ATTACK_" + userName, "agentes.AttackAgent", null);
            user.start();
            System.out.println("[CreateAttackAgent] Agente Attack criado: ATTACK_" + userName);
            userName += 1;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}