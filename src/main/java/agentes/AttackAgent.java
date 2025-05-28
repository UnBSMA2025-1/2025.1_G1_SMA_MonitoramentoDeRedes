package agentes;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;


public class AttackAgent extends Agent {

    private boolean notifiedCreate = false;


    private String generateAttackIp() {
        int part3 = (int)(Math.random() * 256);
        int part4 = (int)(Math.random() * 256);
        return "10.0." + part3 + "." + part4;
    }

    protected void setup() {
        String fakeIp = generateAttackIp();
        System.out.println("[ATTACK] Agente de ataque" + getLocalName() + " usando IP: " + fakeIp);
        addBehaviour(new TickerBehaviour(this, 350) {
            protected void onTick() {
                try {
                    System.out.println("[ATTACK] Enviando requisição...");
                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("X-Real-IP", fakeIp);
                    int code = con.getResponseCode();

                    if (code == 403) {
                        System.out.println("[ATTACK] Agente de ataque bloqueado. Encerrando agente.");   
                    }

                    InputStream in = con.getInputStream();
                    in.close();
                } catch (Exception e) {
                    System.out.println("[ATTACK] Erro no agente de ataque " + getLocalName() + ": " + e.getMessage() + " Agente sendo encerrado.");
                    doDelete();
                    return;
                }
            }
        });
    }
    protected void takeDown() {
    if (!notifiedCreate) {
        notifiedCreate = true;
        System.out.println("[ATTACK] " + getLocalName() + " sendo encerrado. Enviando mensagem ao CreateAttackAgent.");

        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Create-Attack");
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(result[0].getName());
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg.setContent("criar");
                send(msg);
                System.out.println("[ATTACK] Mensagem enviada ao CreateAttackAgent.");
            } else {
                System.out.println("[ATTACK] Nenhum agente CreateAttackAgent encontrado.");
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        
    }
    // aqui você pode fechar conexões, logs finais, etc.
    return;
}

}





