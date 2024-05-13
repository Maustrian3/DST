package dst.ass3.messaging.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dst.ass3.messaging.Constants;
import dst.ass3.messaging.IQueueManager;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static dst.ass3.messaging.Constants.*;

public class QueueManager implements IQueueManager {

    private Connection connection;

    @Override
    public void setUp() {
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

        try (Channel channel = connection.createChannel()) {
            // Create work queues
            for (String queue : WORK_QUEUES) {
                channel.queueDeclare(queue, false, false, false, null);
            }
            // Create exchanges
            channel.exchangeDeclare(TOPIC_EXCHANGE, "direct", false);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tearDown() {
        try (Channel channel = connection.createChannel()) {
            // Delete all queues
            for (String queue : WORK_QUEUES) {
                channel.queueDelete(queue);
            }
            // Delete all exchanges
            channel.exchangeDelete(TOPIC_EXCHANGE);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
