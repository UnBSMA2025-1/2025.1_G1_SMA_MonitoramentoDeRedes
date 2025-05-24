package Agentes;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Agent;

public class AutoUserAgent extends Agent {
    private String Gerador_Ip(){
        int part3 = (int)(Math.random() * 256);
        int part4 = (int)(Math.random() * 256);
        return "192_168_" + part3 + "_" + part4;
    }
    protected void setup() {
        Object[] args = getArguments();  // pega argumentos recebidos

        if (args != null && args.length > 0) {
            try {
                int quantidade = Integer.parseInt(args[0].toString());
                System.out.println("Vou criar " + quantidade + " Agentes User.");

                ContainerController container = getContainerController();

                for (int i = 1; i <= quantidade; i++) {
                    // Nome do agente filho vai ser "filho1", "filho2", etc
                    String User_Name = Gerador_Ip();
                    int N = (int)(Math.random() * 5);   // O que vai decidir se é de ataque ou não
                    Object[] argumento = new Object[1];
                    argumento[0] = N;
                    AgentController user = container.createNewAgent("User" + User_Name, "Agentes.AcessTemplateAgent", argumento);
                    user.start();
                }

            } catch (NumberFormatException e) {
                System.out.println("Argumento inválido! Precisa ser um número inteiro.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Nenhum argumento informado!");
        }
    }
}
