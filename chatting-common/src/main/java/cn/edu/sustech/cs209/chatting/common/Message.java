package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Message {

    int type;
    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private int port = -1;

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
        if((int)json.get("port") != -1){
            me.port = (int) json.get("port");
        }
        return me;
    }

    public String
    getJson() {
        JSONObject object = new JSONObject();
        object.put("type", type);
        object.put("timestamp", timestamp);
        object.put("sentBy", sentBy);
        object.put("sendTo", sendTo);
        object.put("data", data);
        object.put("port", port);
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

    @Override
    public String toString() {
        return "Message{" +
            "type=" + type +
            ", timestamp=" + timestamp +
            ", sentBy='" + sentBy + '\'' +
            ", sendTo='" + sendTo + '\'' +
            ", data='" + data + '\'' +
            ", port=" + port +
            '}';
    }

    public String getData() {
        return data;
    }

    public int getType(){
        return type;
    }

    public void setPort(int port){
        this.port = port;
    }

    public int getPort(){
        return port;
    }


}
