package dst.ass3.messaging.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.ConsumerDetails;
import com.rabbitmq.http.client.domain.MessageStats;
import com.rabbitmq.http.client.domain.QueueInfo;
import dst.ass3.messaging.Constants;
import dst.ass3.messaging.IWorkloadMonitor;
import dst.ass3.messaging.Region;
import com.rabbitmq.http.client.Client;
import dst.ass3.messaging.WorkerResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static dst.ass3.messaging.Constants.*;

public class WorkloadMonitor implements IWorkloadMonitor {

    private final Client client;
    private Connection connection;
    private Channel channel;
    private String monitoringQueue;
    private Map<Region, List<Long>> processingTimesMap = new HashMap<>();

    public WorkloadMonitor(Client client) {
        this.client = client;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(RMQ_USER);
        factory.setPassword(RMQ_PASSWORD);
        factory.setVirtualHost(RMQ_VHOST);
        factory.setHost(RMQ_HOST);
        factory.setPort(Integer.parseInt(RMQ_PORT));

        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        try {
            channel = connection.createChannel();

            // Create queue for monitoring requests
            monitoringQueue = channel.queueDeclare("", false, false, false, null).getQueue();
            for (Region region : Region.values()) {
                String queueName = "dst." + region.toString().toLowerCase();
                // Bind to topics for monitoring requests to monitoring queue
                String routingKey = "requests" + queueName.substring(queueName.indexOf("."));
                channel.queueBind(monitoringQueue, TOPIC_EXCHANGE, routingKey);
            }

            // TODO delete one implementation of the consumer
            // Create consumer callback for monitoring queue
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                String routingKey = delivery.getEnvelope().getRoutingKey();
//                Region region = Region.valueOf(routingKey.substring(routingKey.indexOf(".") + 1).toUpperCase());
//                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//
//                // Process message concurrently
//                processMessage(region, message);
//            };
//            channel.basicConsume(monitoringQueue, true, deliverCallback, consumerTag -> { });

//            // Create consumer for monitoring queue
            channel.basicConsume(monitoringQueue, true,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException
                        {
                            String routingKey = envelope.getRoutingKey();
                            Region region = Region.valueOf(routingKey.substring(routingKey.indexOf(".") + 1).toUpperCase());
                            String message = new String(body, StandardCharsets.UTF_8);

                            // Process message concurrently
                            processMessage(region, message);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized private void processMessage(Region region, String message) {
        ObjectMapper mapper = new ObjectMapper();
        WorkerResponse response;
        try {
            response = mapper.readValue(message, WorkerResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        processingTimesMap.putIfAbsent(region, new LinkedList<>());
        List<Long> processingTimesList = processingTimesMap.get(region);
        processingTimesList.add(response.getProcessingTime());

        while (processingTimesList.size() > 10) {
            processingTimesList.remove(0);
        }
    }

    @Override
    public Map<Region, Long> getRequestCount() {
        Map<Region, Long> requestCountMap = new HashMap<>();

        for (Region region : Region.values()) {
            String queueName = "dst." + region.toString().toLowerCase();
            try {
                QueueInfo queueInfo = client.getQueue("/", queueName);
                Long requestCount = queueInfo.getMessageStats().getBasicPublish();
                requestCountMap.put(region, requestCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return requestCountMap;
    }

    @Override
    public Map<Region, Long> getWorkerCount() {
        Map<Region, Long> workerCountMap = new HashMap<>();

        for (Region region : Region.values()) {
            String queueName = "dst." + region.toString().toLowerCase();
            try {
                QueueInfo queueInfo = client.getQueue("/", queueName);
                Long workerCount = (long) queueInfo.getConsumerDetails().size();
                workerCountMap.put(region, workerCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return workerCountMap;
    }

    @Override
    public Map<Region, Double> getAverageProcessingTime() {
        Map<Region, Double> avgProcessingTimeMap = new HashMap<>();

        for (Map.Entry<Region, List<Long>> entry : processingTimesMap.entrySet()) {
            Region region = entry.getKey();
            List<Long> processingTimes = entry.getValue();

            if (!processingTimes.isEmpty()) {
                double sum = 0;
                for (Long time : processingTimes) {
                    sum += time;
                }
                double average = sum / processingTimes.size();
                avgProcessingTimeMap.put(region, average);
            } else {
                avgProcessingTimeMap.put(region, 0.0); // If there are no processing times, set average to 0
            }
        }

        return avgProcessingTimeMap;
    }

    @Override
    public void close() throws IOException {
        client.deleteQueue("/", monitoringQueue);
        connection.close();
    }
}
