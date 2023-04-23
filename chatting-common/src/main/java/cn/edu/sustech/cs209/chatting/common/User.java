package cn.edu.sustech.cs209.chatting.common;

public class User {

    String name;

    boolean isGroup;

    ChatGroup group;

    public User(String name) {
        this.name = name;
        this.isGroup = false;
        this.group = null;
    }

    public User(ChatGroup group) {
        this.name = null;
        this.isGroup = true;
        this.group = group;
    }
    @Override
    public String toString(){
        if (isGroup){
            return group.getObName();
        }
        else {
            return name;
        }
    }

    public String getName(){
        if (isGroup){
            return group.getGroupName();
        }else {
            return name;
        }
    }

    public boolean isGroup(){
        return isGroup;
    }

    public ChatGroup group(){
        return group;
    }


}
