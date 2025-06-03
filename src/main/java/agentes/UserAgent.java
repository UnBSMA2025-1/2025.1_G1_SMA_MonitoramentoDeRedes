package agentes;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;


public class UserAgent extends Agent {
    private static final String[] USERNAMES = {"alice", "bob", "charlie", "david", "eve"};
    private static final String[] PASSWORDS = {"password123", "qwerty", "123456", "letmein", "admin123"};
    private static final String TARGET_URL = "http://localhost:8080/login";
    
    private final String fakeIp;
    private final Random random = new Random();

    public UserAgent() {
        this.fakeIp = String.format("192.168.%d.%d", random.nextInt(256), random.nextInt(256));
    }

    protected void setup() {
        System.out.println("[USER] Agente " + getLocalName() + " iniciado com IP: " + fakeIp);

        // Comportamento para simular requisições de usuário normal
        addBehaviour(new TickerBehaviour(this, 5000 + random.nextInt(5000)) { // Intervalo entre 5-10s
            protected void onTick() {
                try {
                    // Seleciona credenciais aleatórias
                    String username = USERNAMES[random.nextInt(USERNAMES.length)];
                    String password = PASSWORDS[random.nextInt(PASSWORDS.length)];
                    if (getLocalName().equals("user1")){
                        username = "TestUser";
                        password = "12345";
                    }
                    URL url = new URL(TARGET_URL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("X-Real-IP", fakeIp);
                    con.setConnectTimeout(3000);
                    con.setReadTimeout(3000);
                    con.setDoOutput(true);

                    // Cria payload JSON válido
                    String json = String.format(
                        "{\"username\":\"%s\",\"password\":\"%s\"}", 
                        username, password
                    );

                    // Envia a requisição
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    // Processa a resposta
                    int code = con.getResponseCode();
                    if (code == 403) {
                        System.out.println("[USER] " + getLocalName() + " bloqueado (IP: " + fakeIp + ")");
                        stop(); // Para o comportamento se for bloqueado
                    } 
                    else if (code == 429) {
                        System.out.println("[USER] " + getLocalName() + " limitado (IP: " + fakeIp + ")");
                    }
                    else {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                            String response = br.readLine();
                            System.out.println("[USER] " + getLocalName() + " recebeu resposta: " + response);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[USER] Erro em " + getLocalName() + ": " + e.getMessage());
                }
            }
        });
    }

}
