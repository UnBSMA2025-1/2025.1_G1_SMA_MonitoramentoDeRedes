package agentes;

import core.Node;
import core.DataStore;
import core.RequestRouter;
import infra.MonitorGateway;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.HashSet;


public class MonitorAgent extends Agent implements MonitorGateway {
    private Node node;
    private DataStore store;
    private final Set<String> notifiedIPs = new HashSet<>(); 

    protected void setup() {
        this.node = new Node();
        this.store = DataStore.getInstance();
        RequestRouter.registerMonitor(this);
        
        System.out.println("[MONITOR] Agente iniciado - AID: " + getAID().getName());

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchContent("ping-monitor"));
                if (msg != null) {
                    ACLMessage reply = msg.createReply();
                    reply.setContent("pong-monitor");
                    send(reply);
                    store.logRequest(msg.getSender().getAddressesArray()[0]);
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                monitorarRequisicoes();
                verificarAtaques();
            }
        });

        addBehaviour(new TickerBehaviour(this, 60000) {
            protected void onTick() {
                limparRegistros();
            }
        });
    }

    private void monitorarRequisicoes() {
        Map<String, Integer> snapshot = node.getRequestSnapshot();
        snapshot.forEach((ip, count) -> {
            if (count > 5) {
                // Verificar se já foi notificado
                if (!store.isIpBlocked(ip) && !notifiedIPs.contains(ip)) {
                    store.logAttackAttempt(ip, "DoS", "Taxa: " + count + " reqs/5s");
                    
                    if (store.getAttackAttempts(ip) >= 1) { // deixei 1 para nao permitir 'margem de erro'
                        System.out.println("[Monitor] DoS confirmado - IP: " + ip);
                        notifyMitigator(ip);
                        notifiedIPs.add(ip); // Registrar IP notificado
                    }
                }
            }
        });
    }

    private void verificarAtaques() {   //somente caso queira adicionar verificação de bloqueios repetidos (mesmo ip, ex:bloqueado->desbloqueado->bloqueado)
        store.attackAttempts.forEach((ip, attempts) -> {
            if (attempts >= 2 && !store.blockedIPs.contains(ip)) {
                System.out.println("[ALERTA] Ataque persistente - IP: " + ip);
                notifyMitigator(ip);
            }
        });
    }

    private void limparRegistros() {
        System.out.println("[MANUTENÇÃO] Limpando registros antigos");
        store.cleanOldRequests();
    }

    private void notifyMitigator(String ip) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                try {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("mitigation");
                    template.addServices(sd);

                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    
                    if (result.length > 0) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(result[0].getName());
                        msg.setContent(ip);
                        msg.setConversationId("mitigation-request");
                        send(msg);
                        
                        store.logAlert("Mitigação solicitada para IP: " + ip);
                    }
                } catch (FIPAException e) {
                    store.logError(ip, e);
                }
            }
        });
    }

    @Override
    public boolean receiveRequest(String ip, String User_Password) {
        node.registerRequest(ip);
        store.logRequest(ip);
        
        if (store.isKnownAttacker(ip)) {
            System.out.println("[DETECÇÃO] Ataque conhecido: " + ip);
            notifyMitigator(ip);
            return false;
        }
        if (User_Password != null && store.isSqlInjectionAttempt(User_Password)) {
            store.logAttackAttempt(ip, User_Password, "SQLi");

            if (store.getAttackAttempts(ip) >= 1) {
                System.out.println("[MONITOR] Notificando mitigador {SQL Injection attack}: " + ip);
                // RequestRouter.blockIp(ip);
            }
            notifyMitigator(ip);
            return false;
        }
        return true;
    }

    protected void takeDown() {
        store.logAlert("MonitorAgent sendo encerrado");
        System.out.println("[MONITOR] Agente encerrado");
    }
}