package com.wiilog.iot.iot_platform.worker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.wiilog.iot.iot_platform.Constant;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import com.wiilog.iot.iot_platform.utils.rabbitmq.RabbitMQConnectionManager;
import com.wiilog.iot.iot_platform.utils.log.LogFormatter;
import com.wiilog.iot.iot_platform.utils.log.LogType;
import com.wiilog.iot.iot_platform.utils.rabbitmq.RabbitMQMessagePipe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Worker extends RabbitMQMessagePipe {

    private final String ERROR_QUEUE = "error-";
    private final int MAX_RETRIES = 5;

    private Channel channel;
    private String appropriateAPI;
    private String appropriateQueue;
    private HttpClient httpClient;
    private HttpPost httpPost;
    private String httpParams;
    private StringEntity httpParamsEntity;
    private ResponseHandler httpResponseHandler;

    public Worker() throws IOException, TimeoutException {
        this.channel = RabbitMQConnectionManager.initConnection().createChannel();
        this.channel.confirmSelect();

        this.appropriateAPI = System.getenv(Constant.IOT_ENDPOINT);
        this.appropriateQueue = System.getenv(Constant.RABBITMQ_QUEUE);

        this.httpClient = HttpClients.createDefault();
        this.httpPost = new HttpPost(this.appropriateAPI);
        this.httpParams = "";
        this.httpParamsEntity = new StringEntity(this.httpParams);

        this.httpResponseHandler = httpResponse -> {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                LogFormatter.log(LogType.INFO, "Successfull API response for message '" + this.message + "'...");
                channel.basicAck(this.delivery.getEnvelope().getDeliveryTag(), false);
            } else {
                treatError();
            }
            return null;
        };

        this.channel.queueDeclare(this.appropriateQueue, true, false, false, null);
        this.channel.basicConsume(this.appropriateQueue, false, this::onMessage, consumerTag -> { });
        this.channel.basicQos(1);

        LogFormatter.log(LogType.INFO, "Waiting for messages on queue '" + this.appropriateQueue + "'...");
    }

    public static void main(String[] argv) throws Exception {
        new Worker();
    }

    @Override
    public void forward() throws IOException {
        this.makeApiCall();
    }

    private void makeApiCall() throws IOException {
        this.httpParams = "{\"message\":" + this.message + "}";
        this.httpParamsEntity = new StringEntity(this.httpParams);
        this.httpPost.setEntity(this.httpParamsEntity);
        this.httpPost.setHeader("Accept", "application/json");
        this.httpPost.setHeader("Content-type", "application/json");
        this.httpPost.setHeader("x-api-key" , System.getenv("IOT_AUTH_TOKEN"));

        this.httpClient.execute(this.httpPost, this.httpResponseHandler);
    }

    private void treatError() throws IOException {
        JSONObject parsedMessage = new JSONObject(this.message);
        int tries = parsedMessage.optInt("tries", 0);

        LogFormatter.log(LogType.CRITICAL,
                "API did not send response code 200. '" + tries + "' tries, for message '" + message + "'.");

        String todaysErrorQueue = this.ERROR_QUEUE + new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        this.channel.queueDeclare(todaysErrorQueue, true, false, false, null);

        if (tries < MAX_RETRIES) {
            tries++;
            parsedMessage.put("tries", tries);
        }
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

        this.channel.basicPublish(
                "",
                tries >= MAX_RETRIES ? todaysErrorQueue : this.appropriateQueue,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                parsedMessage.toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
