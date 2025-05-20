package src.agents;
public class BinaryTree {

    public static class No {
        String nomeMonitor;
        String nomeMitigador;
        No esquerdo;
        No direito;

        public No(String nomeBase, int nivel) {
            this.nomeMonitor = "monitor_" + nomeBase;
            this.nomeMitigador = "mitigador_" + nomeBase;

            if (nivel > 0) {
                this.esquerdo = new No(nomeBase + "E", nivel - 1);
                this.direito = new No(nomeBase + "D", nivel - 1);
            }
        }
    }

    public static No construirArvore(int profundidade) {
        return new No("Raiz", profundidade);
    }

    public static void imprimir(No no) {
        if (no == null) return;
        System.out.println("Nó: " + no.nomeMonitor + " | " + no.nomeMitigador);
        imprimir(no.esquerdo);
        imprimir(no.direito);
    }

    public static void main(String[] args) {
        No raiz = construirArvore(2); // profundidade 2 => 7 nós
        imprimir(raiz);
    }
}
