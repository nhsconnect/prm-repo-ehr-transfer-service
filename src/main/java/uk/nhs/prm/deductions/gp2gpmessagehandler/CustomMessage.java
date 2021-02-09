package uk.nhs.prm.deductions.gp2gpmessagehandler;

import org.apache.logging.log4j.message.Message;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomMessage implements Message {
    private static final String TYPE = "type";
    private static final String BODY = "body";

    private final Map<String, Object> message;

    public CustomMessage(Map<String, Object> message) {
        this.message = message;
    }

    @Override
    public String getFormattedMessage() {
        JSONObject jsonBody = new JSONObject(message);
        JSONObject jsonToLog = new JSONObject(new HashMap<String, Object>() {{
            put(TYPE, "custom");
            put(BODY, jsonBody);
        }});

        return jsonToLog.toString();
    }

    @Override
    public String getFormat() {
        return message.toString();
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
