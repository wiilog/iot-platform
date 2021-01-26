package dispatch_center;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.json.JSONObject;
import utils.ConnectionManager;
import utils.log.LogFormatter;
import utils.log.LogType;
import utils.MessagePipe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DispatchCenter extends MessagePipe {

    private String appropriateQueue;

    private Channel channel;

    private Decoder decoder;


    public DispatchCenter() throws Exception {
        this.decoder = new Decoder();

        this.channel = ConnectionManager.initConnection().createChannel();

        // Allow dispatch of message if worker is busy.
        this.channel.confirmSelect();
        // Number of message a worker is allowed to process at the same time.
        this.channel.basicQos(1);

        this.appropriateQueue = System.getenv("QUEUE");
        this.channel.queueDeclare(this.appropriateQueue, true, false, false, null);
        this.channel.basicConsume(this.appropriateQueue, false, this::onMessage, consumerTag -> {
        });
        this.channel.queueBind(this.appropriateQueue, "amq.topic", this.appropriateQueue + ".*");

        LogFormatter.log(LogType.INFO, "Waiting for messages on queue '" + this.appropriateQueue + "'");
    }

    public static void main(String[] argv) throws Exception {
        new DispatchCenter();
    }

    @Override
    public void forward() throws IOException {
        JSONObject parsedMessage = new JSONObject(this.message);
        String devicePropertiesLabel = "device_properties";
        String selectorLabel = "group";
        if (parsedMessage.has(devicePropertiesLabel)) {
            final JSONObject messageJSON = parsedMessage.getJSONObject(devicePropertiesLabel);
            if (messageJSON.has(selectorLabel)) {
                final String appropriateRedirectQueue = messageJSON.getString(selectorLabel);
                this.message = this.decoder.decode(this.message);
                this.channel.queueDeclare(appropriateRedirectQueue, true, false, false, null);
                this.channel.basicPublish("", appropriateRedirectQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, this.message.getBytes(StandardCharsets.UTF_8));
                LogFormatter.log(LogType.INFO, "Sent message '" + this.message + "' to queue '" + appropriateRedirectQueue + "'");
            }
            else {
                this.logGroupError();
            }
        } else {
            this.logGroupError();
        }
        this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

    private void logGroupError() {
        LogFormatter.log(LogType.CRITICAL, "No group in message : " + this.message);
    }

}
