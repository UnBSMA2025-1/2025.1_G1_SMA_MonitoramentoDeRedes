package src.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;

import java.util.*;

public class AgenteMonitor extends Agent {

    private static final int LIMIAR_REQUISICOES = 10;
    private static final long INTERVALO_VERIFICACAO = 5000; // 5 segundos

    private Map<String, Integer> tabelaRequisicoes = new HashMap<>();
    private AID mitigador;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado.");

        addBehaviour(new BuscarMitigadorBehavior());
        addBehaviour(new ReceberRequisicaoBehavior());
        addBehaviour(new DetectarAtaqueBehavior(this, INTERVALO_VERIFICACAO));
    }

    // 1. Comportamento para localizar agente mitigador
    private class BuscarMitigadorBehavior extends OneShotBehaviour {
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("mitigador");
            template.addServices(sd);

            try {
                DFAgentDescription[] resultado = DFService.search(myAgent, template);
                if (resultado.length > 0) {
                    mitigador = resultado[0].getName();
                    System.out.println(getLocalName() + " encontrou mitigador: " + mitigador.getLocalName());
                } else {
                    System.out.println(getLocalName() + " não encontrou mitigador.");
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    // 2. Comportamento para receber requisições de agentes externos
    private class ReceberRequisicaoBehavior extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String ip = msg.getContent();
                tabelaRequisicoes.put(ip, tabelaRequisicoes.getOrDefault(ip, 0) + 1);
                System.out.println(getLocalName() + " recebeu requisição de IP: " + ip);
            } else {
                block();
            }
        }
    }

    // 3. Comportamento periódico para detectar ataques
    private class DetectarAtaqueBehavior extends TickerBehaviour {
        public DetectarAtaqueBehavior(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            for (Map.Entry<String, Integer> entry : tabelaRequisicoes.entrySet()) {
                String ip = entry.getKey();
                int count = entry.getValue();
                if (count > LIMIAR_REQUISICOES) {
                    System.out.println("[ATAQUE DETECTADO] IP: " + ip + " - Requisições: " + count);
                    if (mitigador != null) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(mitigador);
                        msg.setContent(ip);
                        msg.setConversationId("bloquear-ip");
                        send(msg);
                        System.out.println("Solicitado bloqueio do IP: " + ip + " ao mitigador.");
                    }
                }
            }
            tabelaRequisicoes.clear(); // reset após verificação
        }
    }
}
