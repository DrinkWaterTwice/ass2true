package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Message {

    int type;
    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    public Message(int type, Long timestamp, String sentBy, String sendTo, String data) {
        this.type = type;
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public static Message getMessage(String jsonString) {
        JSONObject json = JSON.parseObject(jsonString);
        Message me = new Message(
            (int) json.get("type"),
            (long) json.get("timestamp"),
            (String) json.get("sentBy"),
            (String) json.get("sendTo"),
            (String) json.get("data")

        );
        return me;
    }

    public String getJson() {
        JSONObject object = new JSONObject();

        object.put("type", type);
        object.put("timestamp", timestamp);
        object.put("sentBy", sentBy);
        object.put("sendTO", sendTo);
        object.put("data", data);
        return object.toJSONString();
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }


}
