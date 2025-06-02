# Roteamento HTTP
Para o funcionamento do sistema, é necessário que haja um servidor configurado. No caso, o servidor escolhido foi o [Spark](https://github.com/perwendel/spark), que permite configurar rotas rapidamente. Porém, os agentes podem ser acoplados a qualquer tipo de servidor que forneça roteamento HTTP. <br>
## Como é feito o acoplamento entre servidor e agente
Na classe `RequestRouter`, existem alguns atributos e métodos cruciais para a integração com os agentes do sistema. 

- `MonitorGateway monitor` permite que as requisições possam ser enviadas ao monitor, por meio de uma interface. 
- `blockedIps` guarda os IPs bloqueados previamente pelo sistema.
- `ExecutorService executor` permite fixar o número de threads, limitando a concorrência e tornando o servidor mais estável.
- Método `public static void registerMonitor(MonitorGateway m)`, que permite registrar um agente monitor `m` ao `MonitorGateway`. Esse método é chamado no `setup` do `MonitorAgent`.
- Método `public static void blockIp(String ip)` que adiciona o IP bloqueado ao `blockedIps`. Esse método é chamado pelo `MitigatorAgent`.

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
