package infra;
import core.RequestRecord;
import java.util.List;
import java.util.Map;

public interface MonitorGateway {
    void receiveRequest(String ip, String payload);
    Map<String, Integer> getRequestCountsPerIp();
    List<RequestRecord> getRecentRequests();
    public boolean possivelSQLInjection(String ip, String body);    

}