# Arquitetura e funcionamento dos Agentes
No sistema, os agentes são divididos da seguinte forma:

- `MonitorAgent`: Monitora requisições e detecta possíveis ataques;
- `MitigatorAgent`: Bloqueia IPs maliciosos;
- `UserAgent`: Simula as requisições de um usuário normal;
- `AttackAgent`: Simula um ataque DoS na rede;
- `SupervisorAgent`: Supervisiona os agentes `MonitorAgent` e `MitigatorAgent`, de forma que garanta que sempre estarão funcionais.
- `CreateAttackAgent`: Gerencia a criação de novos agentes de ataque sempre que um AttackAgent é derrubado.


## `MonitorAgent`
O agente monitor é responsável pelo monitoramento de todas as requisições enviadas em dada rota por meio de uma interface `MonitorGateway` que permite que ele possa recebê-las. 
### Atributos
O agente monitor possui dois atributo: um node, que é a classe `Node`, que permite que o agente monitor grave e recupere as requisições passadas nos últimos 10 segundos, e uma lista `recentRequests`, que guarda as últimas 1000 requisições feitas.
### Métodos
No método `setup()`, que precisa ser sobrescrito por todas as classes que possuem `extends Agent`, ou seja, que são agentes JADE, é instanciado um `Node`, e o agente monitor é registrado no `RequestRouter`. <br>
Os comportamentos implementados no `setup` são: 

* **Comportamento Cíclico `(CyclicBehaviour)`** para recebimento do ping do `SupervisorAgent`.
* **Comportamento Ticker `(TickerBehaviour)`** a cada 5 segundos `(5000ms)`, que recupera um snapshot das requisições do `Node` (`HashMap` que possui a chave como sendo o IP do usuário e o valor como sendo o total de requisições dos últimos 10 segundos), e verifica se para cada entrada no snapshot existe alguma que, no intervalo de 10 segundos, possui mais de 10 requisições enviadas. Caso possua, envia uma notificação (`ACLMessage`) para todos os `MitigatorAgent` no DF (Directory Facilitator) e faz um log no console.
* **Comportamento Ticker `(TickerBehaviour)`** a cada 10 segundos (`10000ms`) para zerar as requisições do `Node`.
* **Método `private void notifyMitigator(String ip)`** que instancia um Comportamento Único (`OneShotBehaviour`) busca no DF por `MitigatorAgent`, enviando um pedido para bloqueio do IP especificado.
* **Método `receiveRequest(String ip)`**, que é a implementação do `MonitorGateway`, para recebimento das requisições, e adiciona ao `recentRequests` o `RequestRecord` feito pelo IP, no instante que foi feita a requisição.
* **Método `getRequestCountsByIp()`**, que também é implementação do `MonitorGateway` para obter um snapshot das requisições no `Node`.
* **Método `getRecentRequests()`**, que também é implementação do `MonitorGateway` para obter a lista `recentRequests` do agente monitor.

## `MitigatorAgent`
O agente mitigador é responsável pelo bloqueio dos IPs considerados suspeitos pelo `MonitorAgent`.
### Métodos
No método `setup()`, o mitigador se registra no DF para que possa receber mensagens do agente monitor. <br>
Os comportamentos implementados no `setup` são:

* **Comportamento Cíclico `(CyclicBehaviour)`** para recebimento de mensagens `ACLMessage`. Nesse comportamento, o agente verifica se o `ConversationId` da mensagem é `mitigation-request`: se for, ele bloqueia o IP solicitado na mensagem por meio do `RequestRouter.blockIp(ip)`. Se o `ConversatonId` for `ping-mitigator`, ele irá receber e responder o ping do `SupervisorAgent`.

## `UserAgent`
O agente usuário serve para simular requisições de usuários comuns na rota.
### Métodos
No método `setup()`, o agente usuário gera um IP falso para si mesmo, por meio da função `private String generateUserIp()`. <br>
Os comportamentos implementados no `setup` são:

- **Comportamento Ticker `(TickerBehaviour)`** a cada 3 segundos `(3000ms)`, que envia requisições à rota, tendo como reader `X-Real-IP` para que possa ser corretamente simulado os IPs randomicamente gerados.
- **Método `private String generateUserIp()`** que gera um IP aleatório de 4 partes: `"192.168." + part3 (número gerado aleatoriamente) + "." + part4 (Idem part3)`. 

## `AttackAgent`
O agente de ataque serve para simular requisições maliciosas na rota (ataque DoS).
### Métodos
No método `setup()`, o agente de ataque, assim como o `UserAgent`, gera um IP falso para si mesmo, por meio da função `private String generateAttackIp()`. <br>
Os comportamentos implementados no `setup` são:

- **Comportamento Ticker `(TickerBehaviour)`** a cada 0.2 segundos `(200ms)`, que envia requisições à rota, tendo como header `X-Real-IP` para que possa ser corretamente simulado os IPs randomicamente gerados.
- **Método `private String generateAttackIp()`** que gera um IP aleatório de 4 partes: `"10.0." + part3 (número gerado aleatoriamente) + "." + part4 (Idem part3)`. Esse método é diferente do `generateUserIp` para que, na situação de simulação, não ocorram casos onde sejam instanciados 2 agentes (`UserAgent` e `AttackAgent`) com o mesmo IP, mesmo que sejam mínimos.

Há também o método `takedown()`, que permite que o `AttackAgent`, ao ser derrubado, envie uma mensagem ao `CreateAttackAgent`, para que sejam instanciados novos agentes no lugar dele.

## `SupervisorAgent`
O agente supervisor serve para supervisionar os agentes `MonitorAgent` e `MitigatorAgent` por meio de pings periódicos.
### Atributos

- `private static final String MONITOR_NAME` que guarda o `LocalName` do agente monitor.
- `private static final String MITIGATOR_NAME` que guarda o `LocalName` do agente mitigador.
- `private boolean monitorAlive`, variável booleana do estado de vida do monitor.
- `private boolean mitigatorAlive`, variável booleana do estado de vida do mitigador.
### Métodos
No método `setup()`, o agente supervisor implementa os seguintes comportamentos:

- **Comportamento Ticker `(TickerBehaviour)`** a cada 7 segundos `(7000ms)`, que envia uma `ACLMessage` com o ConversationId `ping-monitor`, para verificar se o monitor está vivo.
- **Comportamento Ticker `(TickerBehaviour)`** a cada 0.5 segundos `(500ms)` para verificar se houve alguma resposta à mensagem com conteúdo `pong-monitor`. Se houver, seta `monitorAlive = true`.
- **Comportamento Ticker `(TickerBehaviour)`** a cada 10 segundos `(10000ms)`, que envia uma `ACLMessage` com o ConversationId `ping-mitigador`, para verificar se o mitigador está vivo.
- **Comportamento Ticker `(TickerBehaviour)`** a cada 1 segundo `(1000ms)`, para verificar se houve alguma resposta à mensagem com conteúdo `pong-mitigador`. Se houver, seta `mitigatorAlive = true`.
- **Comportamento Ticker `(TickerBehaviour)`** a cada 10 segundos `(10000ms)` em que, caso `monitorAlive = false`, ele reinicia o `MonitorAgent` da plataforma JADE.
- **Comportamento Ticker `(TickerBehaviour)`** a cada 10 segundos `(10000ms)` em que, caso `mitigatorAlive = false`, ele reinicia o `MitigatorAgent` da plataforma JADE.

## `CreateAttackAgent`
Para fazer a simulação de ataques constantes na rede de forma a testar o sistema multiagente, foi criado um Agente que envia ataques ao servidor.

### Atributos
O `CreateAttackAgent` possui apenas um atributo: um int `userName`, para contabilizar o número dos agentes ao recriá-los e nomeá-los.

### Métodos
No método `setup()`, o agente implementa os seguintes comportamentos:
- **Comportamento Cíclico `(CyclicBehaviour)`**, que recebe uma mensagem que o `AttackAgent` envia ao ser derrubado. Dentro desse comportamento, há o **Comportamento Acordador `(WakerBehaviour)`**, que inicia após 3 segundos `(3000ms)`, que cria um novo `AttackAgent` por meio do método `public boolean CreateAgent()`.
- ** Método `public boolean CreateAgent()`, que tenta criar um novo agente de ataque, com nome `ATTACK_{userName}`, incrementando o userName a cada iteração.

## Classe `Node`
A classe `Node` foi feita para facilitar os acessos às requisições pelo `MonitorAgent`. Ela inclui métodos e atributos que auxiliam na busca das requisições pelo agente monitor.
### Atributos

- `private final Map<String, Integer> requests`, que é um `HashMap` que guarda o IP e o total de requisições feitas pelo usuário.

### Métodos

- `public synchronized void registerRequest(String ip)`, método para registrar as requisições no `HashMap`.
- `public synchronized Map<String, Integer> getRequestSnapshot()`, método para obter uma snapshot do `HashMap`.
- `public synchronized void resetRequests()`, para limpar as requisições feitas. 
