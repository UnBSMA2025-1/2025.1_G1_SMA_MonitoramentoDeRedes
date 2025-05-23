package agentes;

public class Main {
    public static void main(String[] args) {
        RequestRouter.startServer();

        jade.Boot.main(new String[]{
                "-gui",
                "monitor:agentes.MonitorAgent;" +
                        "mitigator:agentes.MitigatorAgent;" +
                        "att1:agentes.AttackAgent;" +
                        "att2:agentes.AttackAgent;" +
                        "att3:agentes.AttackAgent;" +
                        "user1:agentes.UserAgent;" +
                        "user2:agentes.UserAgent"
        });

    }
}
