package agentes;

import java.time.Instant;

public class RequestRecord {
    private final String ip;
    private final Instant timestamp;

    public RequestRecord(String ip, Instant timestamp) {
        this.ip = ip;
        this.timestamp = timestamp;
    }

    public String getIp() {
        return ip;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
