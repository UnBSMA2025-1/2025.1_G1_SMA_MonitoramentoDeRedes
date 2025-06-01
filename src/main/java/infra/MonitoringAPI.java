package infra;

import static spark.Spark.*;
import com.google.gson.Gson;
import core.DataStore;

public class MonitoringAPI {

    public static void init() {
        Gson gson = new Gson();
        DataStore store = DataStore.getInstance();

        get("/api/stats", (req, res) -> {
            res.type("application/json");
            return gson.toJson(store.requestsPerIP);
        });

        get("/api/blocked", (req, res) -> {
            res.type("application/json");
            return gson.toJson(store.blockedIPs);
        });

        get("/api/requests", (req, res) -> {
            res.type("application/json");
            return gson.toJson(store.recentRequests);
        });

        get("/api/logs", (req, res) -> {
            res.type("application/json");
            return gson.toJson(store.logs);
        });
    }
}