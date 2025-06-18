import core.RequestRouter;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. Criar runtime e perfil JADE com GUI habilitada
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");  // habilita GUI

        // 2. Criar container principal com GUI
        ContainerController container = runtime.createMainContainer(profile);

        // 3. Configurar container para RequestRouter
        RequestRouter.setContainer(container);

        // 4. Iniciar servidor HTTP
        RequestRouter.startServer();

        // 5. Criar agentes principais dentro do container
        container.createNewAgent("monitor", "agentes.MonitorAgent", null).start();
        container.createNewAgent("mitigator", "agentes.MitigatorAgent", null).start();
        container.createNewAgent("supervisor", "agentes.SupervisorAgent", null).start();
        container.createNewAgent("CreateAttackAgent", "agentes.CreateAttackAgent", null).start();
        container.createNewAgent("attack0", "agentes.AttackAgent", null).start();
                // container.createNewAgent("attack2", "agentes.AttackAgent", null).start();
        // container.createNewAgent("user1", "agentes.UserAgent", null).start();
        // container.createNewAgent("user2", "agentes.UserAgent", null).start();
    }
}
