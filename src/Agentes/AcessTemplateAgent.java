package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;



//2 Formas possiveis de fazer a requisição
//        Falar Seu Nome e numero de Requisições que quer
//        Falar seu Nome X vezes, sendo X o número de Requisições



public class AcessTemplateAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");
// Registrar serviço de Requisição no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("User");
        sd.setName("Requisicao");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("User registrado no DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

}
