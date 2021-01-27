package com.wiilog.iot.iot_platform;

public final class Constant {
    private Constant() {}

    public static final String MODE_ERROR = "error";
    public static final String MODE_WORKER = "worker";
    public static final String MODE_DISPATCH_CENTER = "dispatch-center";
    public static final String MODE_HTTP_ENDPOINT = "http-endpoint";

    public static final String RABBITMQ_QUEUE = "QUEUE";
    public static final String HTTP_ENDPOINT_PATH = "HTTP_ENDPOINT_PATH";
    public static final String IOT_ENDPOINT = "IOT_ENDPOINT";

    public static final String RABBITMQ_IP = "RABBITMQ_IP";
    public static final String RABBITMQ_USER = "RABBITMQ_USER";
    public static final String RABBITMQ_PWD = "RABBITMQ_PWD";
    public static final String RABBITMQ_GENERAL_TOPIC = "amq.topic";
    public static final String RABBITMQ_DEVICE_PROPERTIES_LABEL = "device_properties";
    public static final String RABBITMQ_DEVICE_SELECTOR_LABEL = "group";
    public static final String RABBITMQ_TOPIC_SELECTOR_KEY = "RABBITMQ_TOPIC_SELECTOR_KEY";
}
