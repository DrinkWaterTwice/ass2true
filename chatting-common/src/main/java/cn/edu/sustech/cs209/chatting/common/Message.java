package cn.edu.sustech.cs209.chatting.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


/**
 * type 0是私聊 1是服务器信息 2是信息请求 3是看看在不在线 4是群发信息 5下线通知 6是登录请求 9是读取完成 10是退出登录
 */
public class Message {

    int type;
    private final Long timestamp;

    private final String sentBy;

    private final String sendTo;

    private final String data;

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
