package dst.ass3.messaging.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import dst.ass3.messaging.Constants;
import dst.ass3.messaging.IRequestGateway;
import dst.ass3.messaging.TripRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static dst.ass3.messaging.Constants.*;

public class RequestGateway implements IRequestGateway {

    private Connection connection;
    private Channel channel;

    @Override
    public void submitRequest(TripRequest request) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(RMQ_USER);
        factory.setPassword(RMQ_PASSWORD);
        factory.setVirtualHost(RMQ_VHOST);
        factory.setHost(RMQ_HOST);
        factory.setPort(Integer.parseInt(RMQ_PORT));

        if(connection == null) {
            try {
                connection = factory.newConnection();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }

        if(channel == null) {
            try {
                channel = connection.createChannel();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Serialize request
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = null;
        try {
            jsonRequest = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Route request
        try {
            String targetQueue = "dst." + request.getRegion().toString().toLowerCase();
            channel.basicPublish("",
                    targetQueue,
                    null,
                    jsonRequest.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
