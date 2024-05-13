package dst.ass3.messaging.impl;

import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.QueueInfo;
import dst.ass3.messaging.Constants;
import dst.ass3.messaging.IWorkloadMonitor;
import dst.ass3.messaging.Region;
import com.rabbitmq.http.client.Client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WorkloadMonitor implements IWorkloadMonitor {

    private final Client client;

    public WorkloadMonitor(Client client) {
        this.client = client;
    }

    @Override
    public Map<Region, Long> getRequestCount() {
        Map<Region, Long> requestCountMap = new HashMap<>();

        for (Region region : Region.values()) {
            String queueName = "dst." + region.toString().toLowerCase();
            try {
                QueueInfo queueInfo = client.getQueue("/", queueName);
                requestCountMap.put(region, Long.valueOf(queueInfo.toString()));
            } catch (Exception e) {
                // Handle exception, e.g., queue not found
                e.printStackTrace();
            }
        }

        return requestCountMap;
    }

    @Override
    public Map<Region, Long> getWorkerCount() {
        return Map.of();
    }

    @Override
    public Map<Region, Double> getAverageProcessingTime() {
        return Map.of();
    }

    @Override
    public void close() throws IOException {

    }
}
