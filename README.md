# Uma Abordagem Baseada em Agentes para Detecção e Mitigação de Ataques DoS em Ambientes Web
**Disciplina**: FGA0053 - Sistemas Multiagentes <br>
**Nro do Grupo (de acordo com a Planilha de Divisão dos Grupos)**: 01<br>
**Frente de Pesquisa**: Monitoramento e Mitigação de Ataques DoS com Sistemas Multiagentes<br>

## Alunos
| Matrícula  | Aluno                        |
|------------|------------------------------|
| 24/1025480 | Yan Rodrigues                |
| 24/1025523 | Gabriel Alves de Araujo      |
| 24/1025855 | Rodrigo Àtila                |
| 24/1025336 | Matheus Pinheiro             |
| 24/1025837 | Pedro Ian Guedes de Carvalho |
| 24/1025971 | Paulo victor                 |
## Sobre

Este projeto simula agentes que monitoram rotas HTTP e verificam e bloqueiam possíveis ataques DOS. Caso a rota esteja sob ataque de
Todo esse processo é feito de forma acoplada a um servidor (Nesse caso, o Spark), permitindo maior modularidade do projeto.
Além disso, há uma resistência a falhas, pois caso um agente crítico pare de responder, ele é derrubado e reiniciado. <br>

## Agentes do sistema
- 🛡️ `MonitorAgent`: Monitora requisições e detecta possíveis ataques
- 🔒 `MitigatorAgent`: Bloqueia IPs maliciosos
- 🖥️ `UserAgent`: Simula as requisições de um usuário normal.
- 🧨 `AttackAgent`: Simula um ataque DOS na rede.
- 🔧 `SupervisorAgent`: Supervisiona os agentes `MonitorAgent` e `MitigatorAgent`, de forma que garanta que sempre estarão funcionais.
## 📁 Estrutura
```bash
├── src/
│ └── main/
│   └── java/
│     └── agentes/
│       ├── MonitorAgent.java
│       ├── MitigatorAgent.java
│       ├── RequesterAgent.java
│       ├── SupervisorAgent.java
│       └── RequestRouter.java
├── pom.xml
├── README.md
├── run.bat
└── run.sh
```


## 🛠️ Pré-requisitos

- Java 8+
- Apache Mave

## Screenshots (TODO)

## Instalação 
**Linguagens**: Java 8+ e Shell Script (para scripts auxiliares)<br>
**Tecnologias**: Maven e JADE<br>

## 🔧 Como rodar

Verifique se o Maven está instalado em sua máquina.
```bash
mvn --version
```
Caso não esteja instalado, instale-o:
```bash
# Windows
choco install maven # ou
scoop install maven

# Linux
sudo apt install maven # ou
sudo dnf install maven

# macOS
brew install maven
```
Após isso, rode o script auxiliar:
```bash
# Linux
chmod +x run.sh
./run.sh

# Windows
.\run.bat

# macOS
chmod +x run.sh
./run.sh # ou
sh run.sh
```
Então, o Maven deve cuidar de todo o resto do processo. <br>

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
### Lições Aprendidas
### Percepções
### Fragilidades do Sistema
- Por limitações do JADE, o sistema não convém com os protocolos FIPA (FIPA Contract Net Interaction), pois ele não permite que sejam feitas muitas Calls for Proposal (CFP) simultaneamente para vários destinatários;
- Percebe-se que o JADE possui algumas ressalvas quanto à perfomance, podendo afetar a robustez do servidor.
  Foram tomadas algumas medidas para evitar isso, como por exemplo a utilização de `ConcurrentHashMap`, que é thread-safe, e `ExecutorService` para rodar o recebimento de requisições do
  `MonitorAgent` de forma assíncrona, `FixedThreadPool()` para limitar a concorrência e evitar a sobrecarga nas rotas, etc...

## Fontes
Referencie, adequadamente, as referências utilizadas.
Indique ainda sobre fontes de leitura complementares.
