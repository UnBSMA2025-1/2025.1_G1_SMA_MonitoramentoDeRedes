#!/bin/bash
java -cp "out:libs/jade.jar" jade.Boot -gui \
-agents "monitor_Raiz:agents.MonitorAgent, \
mitigador_Raiz:agents.MitigatorAgent, \
ataque1:agents.UserTemplateAgent('192.168.0.66','monitor_Raiz','200'), \
usuario1:agents.UserTemplateAgent('192.168.0.2','monitor_Raiz','2000')"
