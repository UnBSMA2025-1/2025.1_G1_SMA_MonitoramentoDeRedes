package agentes;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class UserAgent extends Agent {
    private String generateUserIp() {
        int part3 = (int)(Math.random() * 256);
        int part4 = (int)(Math.random() * 256);
        return "192.168." + part3 + "." + part4;
    }
    private final Random rnd = new Random();

    private static final String[] USERNAMES = {"admin", "user"};
    private static final String[] PASSWORDS = {"1234", "senha"};

//----------------------------------------------------------------------------------------------------------------------------------

    protected void setup() {
        String fakeIp = generateUserIp();
        String user = USERNAMES[rnd.nextInt(USERNAMES.length)];
        String senha = PASSWORDS[rnd.nextInt(PASSWORDS.length)];
System.out.println("[USER] Agente usuário " + getLocalName() + 
                           " usando IP: " + fakeIp + 
                           " | user: " + user + 
                           " | senha: " + senha);
        addBehaviour(new TickerBehaviour(this, 3000) {
            protected void onTick() {
                try {
                    System.out.println("[USER] Tentando enviar requisição...");
                    URL url = new URL("http://localhost:8080/");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("X-Real-IP", fakeIp);
                    con.setDoOutput(true);

                    String json = "{\"username\":\"" + user + "\", \"password\":\"" + senha + "\"}";
                    OutputStream os = con.getOutputStream();
                    os.write(json.getBytes());
                    os.flush();
                    os.close();

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