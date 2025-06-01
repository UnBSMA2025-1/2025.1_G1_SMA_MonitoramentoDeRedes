<<<<<<< HEAD:src/main/java/infra/MonitorGateway.java
package infra;
=======
package agentes;
import java.util.List;
import java.util.Map;
>>>>>>> origin/feature/DashBoard:src/main/java/agentes/MonitorGateway.java

public interface MonitorGateway {
    void receiveRequest(String ip);
    Map<String, Integer> getRequestCountsPerIp();
    List<RequestRecord> getRecentRequests();
}