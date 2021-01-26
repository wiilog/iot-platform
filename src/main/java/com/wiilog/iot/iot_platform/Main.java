package com.wiilog.iot.iot_platform;


import com.wiilog.iot.iot_platform.dispatch_center.DispatchCenter;
import com.wiilog.iot.iot_platform.http_endpoint.HTTPEndpoint;
import com.wiilog.iot.iot_platform.worker.Worker;

public class Main {

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : Constant.MODE_ERROR;
        switch (mode) {
            case Constant.MODE_DISPATCH_CENTER:
                new DispatchCenter();
                break;
            case Constant.MODE_WORKER:
                new Worker();
                break;
            case Constant.MODE_HTTP_ENDPOINT:
                new HTTPEndpoint();
                break;
            default:
                System.err.println("Invalid mode for IOT platform command");
                System.err.println("Usage:");
                System.err.println("    java -jar output/iot-platform.jar <" + Constant.MODE_HTTP_ENDPOINT + "|" + Constant.MODE_WORKER + "|" + Constant.MODE_DISPATCH_CENTER + ">");
                break;
        }
    }
}
