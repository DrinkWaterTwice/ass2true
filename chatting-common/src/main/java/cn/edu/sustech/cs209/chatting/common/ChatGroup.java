package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.List;

public class ChatGroup {

    String groupName;
    String obName;
    ArrayList<Message> chatMessage;

    public ChatGroup(List<String> list) {
        StringBuilder sb = new StringBuilder();
        StringBuilder finalSb = sb;
        list.forEach(t -> finalSb.append(t).append("/-/-/"));
        groupName = sb.toString();
        sb = new StringBuilder();
        if (list.size() > 3) {
            obName = sb.append(list.get(0)).append(",").append(list.get(1)).append(",")
                .append(list.get(2)).append("...(").append(list.size()).append(")").toString();
        }else {
            for (int i = 0;i < list.size() - 1;i++){
                sb.append(list.get(i)).append(",");
            }
            sb.append(list.get(list.size() - 1));
            obName = sb.toString();
        }
        chatMessage = new ArrayList<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getObName() {
        return obName;
    }

    public ArrayList<Message> getChatMessage() {
        return chatMessage;
    }

    @Override
    public String toString() {
        return "ChatGroup{" +
            "groupName='" + groupName + '\'' +
            ", obName='" + obName + '\'' +
            ", chatMessage=" + chatMessage +
            '}';
    }
}
