package agentes;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserAgent extends Agent {
    private String generateUserIp() {
        int part3 = (int)(Math.random() * 256);
        int part4 = (int)(Math.random() * 256);
        return "192.168." + part3 + "." + part4;
    }
    protected void setup() {
        String fakeIp = generateUserIp();
        System.out.println("[USER] Agente usuário " + getLocalName() + " usando IP: " + fakeIp);

        addBehaviour(new TickerBehaviour(this, 3000) {
            protected void onTick() {
                try {
                    System.out.println("[USER] Tentando enviar requisição...");
                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("X-Real-IP", fakeIp);
                    int code = con.getResponseCode();
                    if (code == 403) {
                        System.out.println("[DEBUG] Aconteceu algo de errado, Agente usuário foi bloqueado.");
                    } else {
                        System.out.println("[USER] Agente usuário enviou requisição com sucesso.");
                    }
                    InputStream in = con.getInputStream();
                    in.close();
                } catch (Exception e) {
                    System.out.println("[USER] Erro no agente de usuário " + getLocalName() + ": " + e.getMessage());
                }
            }
        });
    }
}