package com.wiilog.iot.iot_platform.http_endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.wiilog.iot.iot_platform.Constant;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.json.JSONException;
import org.json.JSONObject;
import com.wiilog.iot.iot_platform.utils.rabbitmq.RabbitMQConnectionManager;
import com.wiilog.iot.iot_platform.utils.log.LogFormatter;
import com.wiilog.iot.iot_platform.utils.log.LogType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class HTTPEndpoint {

    private Map<String, String> devicesToGroup;
    private Channel channel;
    private String appropriateQueue;

    public HTTPEndpoint() throws IOException, TimeoutException {
        this.channel = RabbitMQConnectionManager.initConnection().createChannel();
        this.appropriateQueue = System.getenv(Constant.RABBITMQ_QUEUE);
        this.devicesToGroup = new ObjectMapper()
                .readValue(System.getenv("DEVICES_TO_GROUP"), new TypeReference<Map<String, String>>() {});

        Javalin app = Javalin.create().start(80);
        app.post(System.getenv(Constant.HTTP_ENDPOINT_PATH), this::post);
    }

    private void post(Context context) throws IOException {
        String device = context.header("X-Auth-Token");
        String group = devicesToGroup.get(device);
        ObjectNode response = new ObjectMapper().createObjectNode();
        response.put("result", "failed");
        if (group != null) {
            this.channel.queueDeclare(this.appropriateQueue, true, false, false, null);
            try {
                JSONObject message = new JSONObject(context.body());
                JSONObject deviceProperties = new JSONObject();
                deviceProperties.put("device", device);
                deviceProperties.put(Constant.RABBITMQ_DEVICE_SELECTOR_LABEL, group);
                message.put(Constant.RABBITMQ_DEVICE_PROPERTIES_LABEL, deviceProperties);
                LogFormatter.log(LogType.INFO, "SENT to " + this.appropriateQueue + " : " + message);
                this.channel.basicPublish("", this.appropriateQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes(StandardCharsets.UTF_8));
                response.put("result", "ok");
            } catch (JSONException jsonException) {
                LogFormatter.log(LogType.CRITICAL, "JSON ERROR : " + jsonException.getMessage());
            }
        }
        context.json(response);
    }
}
