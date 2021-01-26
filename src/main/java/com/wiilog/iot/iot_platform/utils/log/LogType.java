package com.wiilog.iot.iot_platform.utils.log;

public enum LogType {
    INFO("INFO"),
    CRITICAL("CRITICAL");

    private String type;

    LogType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
