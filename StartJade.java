
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.lang.Thread;

public class StartJade {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true"); // Ativa interface gráfica JADE

        ContainerController mainContainer = rt.createMainContainer(p);
        String[] ips = {"192.168.1.100"};
        Object[] abcd = new Object[1];
        abcd[0] = ips;

//        try {
//            Thread.sleep(5000); // pausa por 1000 ms = 1 segundo
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            // Agente Monitorador
            AgentController monitorador = mainContainer.createNewAgent(
                    "monitorador1", // nome do agente
                    "MonitorAgent", // caminho completo da classe
                    abcd
            );
            monitorador.start();

            // Agente Mitigador
            AgentController mitigador = mainContainer.createNewAgent(
                    "mitigador1",
                    "MitigatorAgent",
                    null
            );
            mitigador.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
