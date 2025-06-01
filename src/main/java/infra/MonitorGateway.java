package infra;
import core.RequestRecord;
import java.util.List;
import java.util.Map;

public interface MonitorGateway {
    void receiveRequest(String ip);
    Map<String, Integer> getRequestCountsPerIp();
    List<RequestRecord> getRecentRequests();
}