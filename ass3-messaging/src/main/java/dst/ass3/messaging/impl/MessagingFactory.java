package dst.ass3.messaging.impl;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import dst.ass3.messaging.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class MessagingFactory implements IMessagingFactory {

    @Override
    public IQueueManager createQueueManager() {
        return new QueueManager();
    }

    @Override
    public IRequestGateway createRequestGateway() {
        return new RequestGateway();
    }

    @Override
    public IWorkloadMonitor createWorkloadMonitor() {
        try {
            Client client = new Client(
                    new ClientParameters()
                            .url(Constants.RMQ_API_URL)
                            .username(Constants.RMQ_USER)
                            .password(Constants.RMQ_PASSWORD));
            return new WorkloadMonitor(client);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        // implement if needed
    }
}
