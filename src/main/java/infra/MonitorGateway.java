package infra;

public interface MonitorGateway {
    boolean receiveRequest(String ip, String User_Password);
}