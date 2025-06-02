# Uma Abordagem Baseada em Agentes para Detecção e Mitigação de Ataques DoS em Ambientes Web
**Disciplina**: FGA0053 - Sistemas Multiagentes <br>
**Nro do Grupo (de acordo com a Planilha de Divisão dos Grupos)**: 01<br>
**Frente de Pesquisa**: Monitoramento e Mitigação de Ataques DoS com Sistemas Multiagentes<br>

## Alunos
| Matrícula  | Aluno                   |
|------------|-------------------------|
| 24/1025480 | Yan Rodrigues           |
| 24/1025523 | Gabriel Alves de Araujo |
| 24/10XXXXX | XX XXXX XXXX XXXX       |
| 24/10XXXXX | XX XXXX XXXX XXXX       |
| 24/10XXXXX | XX XXXX XXXX XXXX       |
| 24/10XXXXX | XX XXXX XXXX XXXX       |
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
│     └── core/
│       ├── DataStore.java
│       ├── Node.java
│       └── RequestRouter.java
│     └── infra/
│       ├── MonitorGateway.java
│       └── MonitoringAPI.java
│     └── Main.java
│   └── resources/
│     └── public/
│       └── site/
│         └── index.html
├── pom.xml
├── README.md
├── run.bat
└── run.sh
```


## 🛠️ Pré-requisitos

- Java 8+
- Apache Maven

## Screenshots (TODO)

## Instalação 
**Linguagens**: Java 8+ e Shell Script (para scripts auxiliares)<br>
**Tecnologias**: Maven e JADE v4.5.0<br>

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
Então, o Maven deve cuidar de todo o resto do processo de build e o sistema será iniciado. <br>

## Uso 
Para utilizar o sistema, basta rodá-lo e acessar o link para o dashboard. Nele, encontramos informações sobre IPs bloqueados, requisições feitas por IPs, e o log dos agentes. <br>
Mais informações sobre como funciona o projeto e os agentes estão presentes [aqui]().


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
Este projeto é escalável, pois permite que sejam adicionadas outras rotas para que sejam monitoradas. É necessário apenas poucas linhas de código para acoplar o `MonitorAgent` ao roteador.

### Lições Aprendidas
Nesse projeto, aprendemos muito sobre o paradigma de Multiagentes, e suas aplicações em aplicativos que podem ser utilizados na vida real, fora de simulações. Também, pudemos aprender como gerenciar os recursos do sistema de forma a priorizar a performance, pois os agentes acabam tornando-se muito pesados para a máquina.
- Paradigma de Sistemas Multiagentes
- 

### Percepções
- 
### Fragilidades do Sistema
- Não reconhece outras formas de ataque fora o DoS e DDoS. 
- Por limitações do JADE, o sistema não convém com os protocolos FIPA (FIPA Contract Net Interaction), pois ele não permite que sejam feitas muitas Calls for Proposal (CFP) simultaneamente para vários destinatários;
- Percebe-se que o JADE possui algumas ressalvas quanto à perfomance, podendo afetar a robustez do servidor.
  Foram tomadas algumas medidas para evitar isso, como por exemplo a utilização de `ConcurrentHashMap`, que é thread-safe, e `ExecutorService` para rodar o recebimento de requisições do `MonitorAgent` de forma assíncrona, `FixedThreadPool()` para limitar a concorrência e evitar a sobrecarga nas rotas, etc...
- Também, foi vísivel alguns comportamentos emergentes no `SupervisorAgent`. Esses comportamentos se dão pelo fato de as mensagens serem enviadas no meio de alguma outra tarefa que o agente destinatário estava executando, fazendo com que ele interprete incorretamente que o agente está morto, e reinicia-o.

### Trabalhos Futuros
- Implementação dos agentes baseados no OWASP Top 10, de forma a reconhecer diferentes formas de ataque.
- Melhorar a robustez geral do sistema, para conseguir suportar diversos ataques simultâneos no mesmo sistema.

## Fontes
Referencie, adequadamente, as referências utilizadas.
Indique ainda sobre fontes de leitura complementares.
