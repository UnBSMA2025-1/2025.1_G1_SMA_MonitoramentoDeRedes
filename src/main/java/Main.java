import core.RequestRouter;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

import jade.wrapper.gateway.JadeGateway;
import jade.util.leap.Properties;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        Logger jadeLogger = Logger.getLogger("jade");
        jadeLogger.setLevel(Level.WARNING);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.WARNING);
        Logger.getLogger("").addHandler(ch);

    Properties jadeProps = new Properties();

        // Inicializa o JadeGateway passando o Properties não nulo
        JadeGateway.init(
            "jade.wrapper.gateway.GatewayAgent",           
            new Object[]{}, 
            jadeProps     
        );
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");  

        ContainerController container = runtime.createMainContainer(profile);
        
        RequestRouter.startServer();

        container.createNewAgent("MonitorAgent", "agentes.MonitorAgent", null).start();
        container.createNewAgent("mitigator", "agentes.MitigatorAgent", null).start();
        container.createNewAgent("supervisor", "agentes.SupervisorAgent", null).start();
        container.createNewAgent("CreateAttackAgent", "agentes.CreateAttackAgent", null).start();
        container.createNewAgent("attack0", "agentes.AttackAgent", null).start();
                // container.createNewAgent("attack2", "agentes.AttackAgent", null).start();
        container.createNewAgent("user1", "agentes.UserAgent", null).start();
        // container.createNewAgent("user2", "agentes.UserAgent", null).start();
    }
}
