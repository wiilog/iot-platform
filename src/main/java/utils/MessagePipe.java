package utils;

import com.rabbitmq.client.Delivery;
import utils.log.LogFormatter;
import utils.log.LogType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class MessagePipe {

    protected String message;
    protected Delivery delivery;

    public void onMessage(String consumer, Delivery delivery) throws IOException {
        this.message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        this.delivery = delivery;
        LogFormatter.log(LogType.INFO, "Received message '" + this.message + "' forwarding....");
        this.forward();
    }

    public abstract void forward() throws IOException;
}
