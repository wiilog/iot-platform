package com.wiilog.iot.iot_platform.utils.rabbitmq;

import com.rabbitmq.client.*;
import com.wiilog.iot.iot_platform.Constant;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnectionManager {

    public static Connection initConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(System.getenv(Constant.RABBITMQ_IP));
        factory.setUsername(System.getenv(Constant.RABBITMQ_USER));
        factory.setPassword(System.getenv(Constant.RABBITMQ_PWD));

        return factory.newConnection();
    }

}
