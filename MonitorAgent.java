
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import java.util.*;

public class MonitorAgent extends Agent {
    private List<String> ipsParaBloquear = new ArrayList<>();
    private Map<String, String> ipPorConversationId = new HashMap<>();
    private MessageTemplate mt;

    protected void setup() {
//        // Adiciona IPs fictícios para teste
////        ipsParaBloquear.add("192.168.1.100");
////        ipsParaBloquear.add("10.0.0.45");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Se o primeiro argumento for um array de String (como no código)
            if (args[0] instanceof String[]) {
                String[] ipsRecebidos = (String[]) args[0];
                for (String ip : ipsRecebidos) {
                    ipsParaBloquear.add(ip);
                    System.out.println("IP adicionado para bloqueio (array): " + ip);
                }
            }
            // Se os argumentos forem Strings individuais (ex: args = ["192.168.1.100", "10.0.0.45"])
            else {
                for (Object arg : args) {
                    if (arg instanceof String) {
                        ipsParaBloquear.add((String) arg);
                        System.out.println("IP adicionado para bloqueio (args simples): " + arg);
                    }
                }
            }
        } else {
            System.out.println("Nenhum IP recebido via argumento.");
        }



        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                if (ipsParaBloquear.isEmpty() == false) {
                    addBehaviour(new BloquearIP());
                }
            }
        });

        addBehaviour(new ReceberResposta());
    }

    private class BloquearIP extends OneShotBehaviour {
        public void action() {
            try {
                // Busca Mitigadores registrados
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("mitigacao-ataques");
                template.addServices(sd);

                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0 && !ipsParaBloquear.isEmpty()) {
                    String ip = ipsParaBloquear.get(0);  // tenta o primeiro
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    for (DFAgentDescription dfad : result) {
                        msg.addReceiver(dfad.getName());
                    }

                    msg.setContent(ip);
                    msg.setConversationId("Ip-Block");
                    String replyWith = "block-request-" + System.currentTimeMillis();
                    msg.setReplyWith(replyWith);
                    msg.setProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_REQUEST);

                    ipPorConversationId.put(replyWith, ip);  // associa IP com ID da conversa

                    send(msg);
                    System.out.println("[Monitorador] Pedido de bloqueio enviado para: " + ip);

                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId("Ip-Block"),
                            MessageTemplate.MatchProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_REQUEST)
                    );
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceberResposta extends CyclicBehaviour {
        public void action() {
            ACLMessage reply = myAgent.receive(mt);
            if (reply != null) {
                String conversationId = reply.getInReplyTo();
                String ip = ipPorConversationId.get(conversationId);

                if (reply.getPerformative() == ACLMessage.AGREE) {
                    System.out.println("[Monitorador] Mitigador aceitou bloquear IP: " + ip);
                } else if (reply.getPerformative() == ACLMessage.REFUSE) {
                    System.out.println("[Monitorador] Mitigador recusou bloqueio do IP: " + ip);
                } else if (reply.getPerformative() == ACLMessage.INFORM) {
                    System.out.println("[Monitorador] IP bloqueado com sucesso: " + ip);
                    ipsParaBloquear.remove(ip);  // remove da lista
                }

                ipPorConversationId.remove(conversationId); // limpa o mapeamento
            } else {
                block();
            }
        }
    }
}
