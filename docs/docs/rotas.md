# Roteamento HTTP

Para o funcionamento do sistema, é necessário que haja um servidor configurado. No caso, o servidor escolhido foi o [Spark](https://github.com/perwendel/spark), que permite configurar rotas rapidamente. Porém, os agentes podem ser acoplados a qualquer tipo de servidor que forneça roteamento HTTP.

## Como é feito o acoplamento entre servidor e agente

Na classe `RequestRouter`, localizada no pacote `core`, existem alguns atributos e métodos cruciais para a integração com os agentes do sistema:

- `MonitorGateway monitor`: permite que as requisições possam ser enviadas ao monitor por meio de uma interface.
- `blockedIps`: guarda os IPs bloqueados previamente pelo sistema.
- `ExecutorService executor`: permite fixar o número de threads, limitando a concorrência e tornando o servidor mais estável.
- Método `public static void registerMonitor(MonitorGateway m)`: permite registrar um agente monitor `m` ao `MonitorGateway`. Esse método é chamado no `setup` do `MonitorAgent`.
- Método `public static void blockIp(String ip)`: adiciona o IP bloqueado ao `blockedIps`. Esse método é chamado pelo `MitigatorAgent`.

## Como configurar rotas no sistema

A configuração das rotas se torna uma tarefa muito fácil quando usamos o Spark. Precisamos apenas configurar o tipo do método HTTP (`GET`, `POST`, `DELETE`, `PUT`) e a rota que deseja criar. Dentro da rota, é crucial que haja o seguinte bloco de código:

```java
post("/", (req, res) -> {
    String ip = req.ip();

    if (blockedIps.contains(ip)) {
        res.status(403);
        return "Blocked"; // Ou outro retorno, pode ser uma página HTML
    }

    if (monitor != null) {
        final String ipCopy = ip;
        executor.submit(() -> {
            try {
                monitor.receiveRequest(ipCopy);
            } catch (Exception e) {
                System.err.println("[ROUTER] Erro ao enviar IP ao monitor: " + e.getMessage());
            }
        });
    }

    // Insira a lógica do endpoint aqui
});
```

Com isso, podemos acoplar o sistema a qualquer rota HTTP de um servidor.

## Fluxo de Dados e Armazenamento

O sistema utiliza duas principais estruturas para armazenamento e gerenciamento de dados:

### 1. `Node` (pacote `core`)

A classe `Node` atua como um ponto de coleta e armazenamento de requisições por IP. Ela mantém um `ConcurrentHashMap<String, List<RequestRecord>>` chamado `ipRequests`, que armazena listas de requisições (`RequestRecord`) associadas a cada IP. Essa estrutura permite que os agentes monitorem e analisem o comportamento de cada IP individualmente.

### 2. `DataStore` (pacote `core`)

A classe `DataStore` serve como um repositório centralizado para os dados coletados pelos agentes. Ela também utiliza um `ConcurrentHashMap<String, List<RequestRecord>>` para armazenar as requisições, permitindo uma visão geral do tráfego na rede. Além disso, o `DataStore` fornece métodos para acessar e manipular esses dados, facilitando a análise e a geração de relatórios.

### 3. `LogStore` (pacote `core`)

A classe `LogStore` é responsável por armazenar os logs gerados pelos agentes. Ela utiliza uma lista sincronizada para armazenar mensagens de log, permitindo que o sistema mantenha um histórico das ações e eventos ocorridos durante a execução.

### 4. `RequestRecord` (pacote `core`)

A classe `RequestRecord` representa uma requisição individual feita por um IP. Ela armazena informações como o IP de origem e o timestamp da requisição, permitindo que os agentes analisem padrões de comportamento e detectem possíveis ataques.

## Integração com a API de Monitoramento

A classe `MonitoringAPI`, localizada no pacote `infra`, fornece endpoints HTTP para acessar os dados armazenados no sistema. Ela permite que usuários e administradores consultem informações como:

- Lista de IPs bloqueados.
- Requisições feitas por cada IP.
- Logs gerados pelos agentes.

Esses endpoints são úteis para monitorar o estado atual da rede e tomar decisões informadas sobre ações de mitigação.
