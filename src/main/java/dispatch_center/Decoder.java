package dispatch_center;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
