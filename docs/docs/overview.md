# Visão Geral
Este projeto simula agentes que monitoram rotas HTTP e verificam e bloqueiam possíveis ataques DOS. Todo esse processo é feito de forma acoplada a um servidor (Nesse caso, o Spark), permitindo maior modularidade do projeto. Além disso, há uma resistência a falhas, pois caso um agente crítico pare de responder, ele é derrubado e reiniciado.


