import core.RequestRouter;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class Main {
    public static void main(String[] args) throws Exception {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");  

        ContainerController container = runtime.createMainContainer(profile);
        
        RequestRouter.startServer();

        container.createNewAgent("monitor", "agentes.MonitorAgent", null).start();
        container.createNewAgent("mitigator", "agentes.MitigatorAgent", null).start();
        container.createNewAgent("supervisor", "agentes.SupervisorAgent", null).start();
        container.createNewAgent("CreateAttackAgent", "agentes.CreateAttackAgent", null).start();
        container.createNewAgent("attack0", "agentes.AttackAgent", null).start();
                // container.createNewAgent("attack2", "agentes.AttackAgent", null).start();
        container.createNewAgent("user1", "agentes.UserAgent", null).start();
        container.createNewAgent("user2", "agentes.UserAgent", null).start();
    }
}
