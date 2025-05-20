# Monitoramento de Redes e mitigação de ataques DOS

**Disciplina**: FGA0053 - Sistemas Multiagentes <br>
**Nro do Grupo (de acordo com a Planilha de Divisão dos Grupos)**: 01<br>
**Frente de Pesquisa**: XXXXXXXXXX<br>

## Alunos
|Matrícula | Aluno |
| -- | -- |
| xx/xxxxxx  |  xxxx xxxx xxxxx |
| xx/xxxxxx  |  xxxx xxxx xxxxx |

## Sobre ###ALTERAR!!!!!!

Este projeto simula uma infraestrutura de rede utilizando uma árvore binária. Em cada nó, temos dois agentes:

- 🛡️ `AgenteMonitor`: monitora requisições e detecta possíveis ataques
- 🔒 `AgenteMitigador`: bloqueia IPs maliciosos

Além disso, temos agentes externos que simulam:

- 🧨 Atacantes (múltiplas requisições por segundo)
- 👤 Usuários legítimos (requisições moderadas)

## 📁 Estrutura
src/agentes/

 AgenteMonitor.java
 AgenteMitigador.java
 AgenteUsuarioTemplate.java
 AgenteAtaque.java
 AgenteAcesso.java
 InfraArvore.java

bash
Copiar
Editar

## 🛠️ Pré-requisitos

- Java 8+
- JADE (adicione `jade.jar` em `/libs`)
- Terminal ou IDE (como IntelliJ, Eclipse)

## 🔧 Compilação

```bash
# Compilar todos os arquivos
javac -d out -cp libs/jade.jar src/agentes/*.java
```
<b>
Descreva o seu projeto em linhas gerais. 
Use referências, links, que permitam conhecer um pouco mais sobre o projeto.
Capriche nessa seção, pois ela é a primeira a ser lida pelos interessados no projeto.</b>

## Screenshots
Adicione 2 ou mais screenshots do projeto em termos de interface e/ou funcionamento.

## Instalação 
**Linguagens**: xxxxxx<br>
**Tecnologias**: xxxxxx<br>
Descreva os pré-requisitos para rodar o seu projeto e os comandos necessários.
Insira um manual ou um script para auxiliar ainda mais.
Gifs animados e outras ilustrações são bem-vindos!

## Uso 
Explique como usar seu projeto.
Procure ilustrar em passos, com apoio de telas do software, seja com base na interface gráfica, seja com base no terminal.
Nessa seção, deve-se revelar de forma clara sobre o funcionamento do software.

## Vídeo
Adicione 1 ou mais vídeos com a execução do projeto.
Procure: 
(i) Introduzir o projeto;
(ii) Mostrar passo a passo o código, explicando-o, e deixando claro o que é de terceiros, e o que é contribuição real da equipe;
(iii) Apresentar particularidades do Paradigma, da Linguagem, e das Tecnologias, e
(iV) Apresentar lições aprendidas, contribuições, pendências, e ideias para trabalhos futuros.
OBS: TODOS DEVEM PARTICIPAR, CONFERINDO PONTOS DE VISTA.
TEMPO: +/- 15min

## Participações
Apresente, brevemente, como cada membro do grupo contribuiu para o projeto.
|Nome do Membro | Contribuição | Significância da Contribuição para o Projeto (Excelente/Boa/Regular/Ruim/Nula) | Comprobatórios (ex. links para commits)
| -- | -- | -- | -- |
| Fulano  |  Programação dos Fatos da Base de Conhecimento Lógica | Boa | Commit tal (com link)

## Outros 
Quaisquer outras informações sobre o projeto podem ser descritas aqui. Não esqueça, entretanto, de informar sobre:
(i) Lições Aprendidas;
(ii) Percepções;
(iii) Contribuições e Fragilidades, e
(iV) Trabalhos Futuros.

## Fontes
Referencie, adequadamente, as referências utilizadas.
Indique ainda sobre fontes de leitura complementares.
