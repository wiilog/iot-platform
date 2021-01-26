package com.wiilog.iot.iot_platform.utils.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFormatter
{
    public static void log(LogType logType, String log) {
        System.out.println(LogFormatter.calcDate() + " - " + logType.getType() + " : " + log);
    }

    private static String calcDate() {
        return "[" +  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]";
    }
}