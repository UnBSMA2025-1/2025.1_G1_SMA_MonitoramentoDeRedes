package agentes;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AttackAgent extends Agent {
    private String generateAttackIp() {
        int part3 = (int)(Math.random() * 256);
        int part4 = (int)(Math.random() * 256);
        return "10.0." + part3 + "." + part4;
    }

    protected void setup() {
        String fakeIp = generateAttackIp();
        System.out.println("[ATTACK] Agente de ataque" + getLocalName() + " usando IP: " + fakeIp);
        addBehaviour(new TickerBehaviour(this, 200) {
            protected void onTick() {
                try {
                    System.out.println("[ATTACK] Enviando requisição...");
                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("X-Real-IP", fakeIp);
                    int code = con.getResponseCode();

                    if (code == 403) {
                        System.out.println("[ATTACK] Agente de ataque bloqueado. Parando ataques.");
                        stop();
                    }

                    InputStream in = con.getInputStream();
                    in.close();
                } catch (Exception e) {
                    System.out.println("[ATTACK] Erro no agente de ataque " + getLocalName() + ": " + e.getMessage());
                }
            }
        });
    }
}