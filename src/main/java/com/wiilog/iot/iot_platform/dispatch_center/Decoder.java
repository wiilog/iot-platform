package com.wiilog.iot.iot_platform.dispatch_center;

import org.json.JSONObject;

public class Decoder {

    public String decode(String msg) {
        JSONObject parsedMessage = new JSONObject(msg);
        switch (parsedMessage.getString("profile")) {
            // our codec
            default:
                return msg;
        }
    }
}
