package src.agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class UserTemplete extends Agent {

    private String ip;
    private String monitorTarget;
    private long intervaloEnvio; // em ms

    @Override
    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length == 3) {
            ip = (String) args[0];                      // Ex: "192.168.0.10"
            monitorTarget = (String) args[1];           // Ex: "monitor1"
            intervaloEnvio = Long.parseLong((String) args[2]); // Ex: "500"
        } else {
            System.out.println("Parâmetros esperados: <ip> <nomeMonitor> <intervalo(ms)>");
            doDelete();
            return;
        }

        System.out.println(getLocalName() + " iniciado. IP: " + ip + ", destino: " + monitorTarget + ", intervalo: " + intervaloEnvio + "ms");

        addBehaviour(new EnviarRequisicoesBehavior(this, intervaloEnvio));
    }

    // Comportamento para enviar requisições periodicamente ao monitor
    private class EnviarRequisicoesBehavior extends TickerBehaviour {
        public EnviarRequisicoesBehavior(Agent a, long interval) {
            super(a, interval);
        }

        protected void onTick() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(monitorTarget, AID.ISLOCALNAME));
            msg.setContent(ip);
            myAgent.send(msg);
            System.out.println(getLocalName() + " enviou requisição para " + monitorTarget + " com IP: " + ip);
        }
    }
}
