import core.RequestRouter;

public class Main {
    public static void main(String[] args) {
        RequestRouter.startServer();

        jade.Boot.main(new String[]{
                "-gui",
                "monitor:agentes.MonitorAgent;" +
                        "mitigator:agentes.MitigatorAgent;" +
                        "supervisor:agentes.SupervisorAgent;" +
                        "attack:agentes.AttackAgent;" +
                        "user1:agentes.UserAgent;" +
                        "user2:agentes.UserAgent"
        });

    }
}
