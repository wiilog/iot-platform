package com.wiilog.iot.iot_platform.dispatch_center;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.wiilog.iot.iot_platform.Constant;
import org.json.JSONObject;
import com.wiilog.iot.iot_platform.utils.rabbitmq.RabbitMQConnectionManager;
import com.wiilog.iot.iot_platform.utils.log.LogFormatter;
import com.wiilog.iot.iot_platform.utils.log.LogType;
import com.wiilog.iot.iot_platform.utils.rabbitmq.RabbitMQMessagePipe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DispatchCenter extends RabbitMQMessagePipe {

    private String appropriateQueue;

    private Channel channel;

    private Decoder decoder;

    public DispatchCenter() throws Exception {
        this.decoder = new Decoder();

        this.channel = RabbitMQConnectionManager.initConnection().createChannel();

        // Allow dispatch of message if com.wiilog.iot.iot_platform.worker is busy.
        this.channel.confirmSelect();
        // Number of message a com.wiilog.iot.iot_platform.worker is allowed to process at the same time.
        this.channel.basicQos(1);

        this.appropriateQueue = System.getenv(Constant.RABBITMQ_QUEUE);
        this.channel.queueDeclare(this.appropriateQueue, true, false, false, null);
        this.channel.basicConsume(this.appropriateQueue, false, this::onMessage, (consumerTag) -> {});
        this.channel.queueBind(this.appropriateQueue, Constant.RABBITMQ_GENERAL_TOPIC, this.appropriateQueue + System.getenv(Constant.RABBITMQ_TOPIC_SELECTOR_KEY));

        LogFormatter.log(LogType.INFO, "Waiting for messages on queue '" + this.appropriateQueue + "'");
    }

    public static void main(String[] argv) throws Exception {
        new DispatchCenter();
    }

    @Override
    public void forward() throws IOException {
        JSONObject parsedMessage = new JSONObject(this.message);
        if (parsedMessage.has(Constant.RABBITMQ_DEVICE_PROPERTIES_LABEL)) {
            final JSONObject messageJSON = parsedMessage.getJSONObject(Constant.RABBITMQ_DEVICE_PROPERTIES_LABEL);
            if (messageJSON.has(Constant.RABBITMQ_DEVICE_SELECTOR_LABEL)) {
                final String appropriateRedirectQueue = messageJSON.getString(Constant.RABBITMQ_DEVICE_SELECTOR_LABEL);
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
