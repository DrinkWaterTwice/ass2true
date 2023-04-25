package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class Main {

    public static HashMap<String, Integer> userAndPort = new HashMap<>();
    public static HashMap<String, ArrayList<String>> userChatWith = new HashMap<>();

    public static HashMap<String, ArrayList<String>> userChatWithG = new HashMap<>();

    public static Database database = new Database();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1234);
        checkOnline();
        while (true) {
            try {
                Socket socket = ss.accept();
                InputStream inputStream = socket.getInputStream();
                byte[] buf = new byte[1024];
                int readLen = 0;
                readLen = inputStream.read(buf);
                Message me = Message.getMessage(
                    new String(buf, 0, readLen, StandardCharsets.UTF_8));
                if (me.getType() != 2) {
                    System.out.println(me);
                }
                if (me.getType() == 1) {
                    userAndPort.put(me.getSentBy(), me.getPort());
                    readMes(me);

                    continue;
                }
                if (me.getType() == 2) {
                    getOnline(me);
                    continue;
                }
                if (me.getType() == 4) {
                    buildChatG(me.getSendTo());
                    sendToGroup(me);
                    database.addMessage(me);
                    database.addMessageG(me);
                    continue;
                }
                if (me.getType() == 6) {
                    senLogin(me);
                    continue;
                }
                if (me.getType() == 7) {
                    if (database.signIn(me)) {
                        Message message = new Message(7, System.currentTimeMillis(), "service",
                            me.getSentBy(), "true");
                        sendTo(message, me.getPort(), "注册的人");
                    } else {
                        Message message = new Message(7, System.currentTimeMillis(), "service",
                            me.getSentBy(), "false");
                        sendTo(message, me.getPort(), "注册的人");
                    }
                }
                if (me.getType() == 10) {
                    deleteChat(me.getSentBy());
                    deleteOnline(me);
                    continue;
                }
                if (me.getType() == 0) {
                    if (userAndPort.containsKey(me.getSendTo())) {
                        buildChat(me.getSentBy(), me.getSendTo());
                        sendTo(me, userAndPort.get(me.getSendTo()));
                    }
                    database.addMessage(me);
                }
                inputStream.close();
                socket.close();
            } catch (IOException e) {

            }
        }
    }

    static boolean sendTo(Message message, int port) {
        if (!userAndPort.containsKey(message.getSendTo()) && message.getType() != 6) {
            return false;
        }
        if (message.getData().isEmpty() && message.getType() != 2) {
            return false;
        }
        if (message.getType() != 2) {
            System.out.println("send" + message);
        }
        try {
            Socket socket = new Socket("localhost", port);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(message.getJson().getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void readMes(Message me) {
        String user = me.getSentBy();
        ArrayList<Message> messages = database.getMessages(me.getSentBy());
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            messages.forEach(t -> {
                if (t.getType() == 0 || t.getType() == 4) {
                    //sendTo(t, userAndPort.get(user));
                    try {
                        Socket s = new Socket("localhost", userAndPort.get(user));
                        OutputStream out = s.getOutputStream();
                        out.write(t.getJson().getBytes(StandardCharsets.UTF_8));
                        out.close();
                        s.close();
                    } catch (Exception e) {

                    }
                }

            });
        });
        thread.start();
    }


    /**
     * 群发消息使用
     *
     * @param message
     * @param port
     * @param sendTo
     * @return
     */
    static boolean sendTo(Message message, int port, String sendTo) {
        if (message.getData().isEmpty()) {
            return false;
        }
        try {
            Socket socket = new Socket("localhost", port);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(message.getJson().getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    static void sendToGroup(Message me) {
        String[] strings = me.getSendTo().split("/-/-/");
        for (String e : strings) {
            if (userAndPort.containsKey(e)) {
                System.out.println("发送群组信息" + me.toString());
                Message meToAll = new Message(4, me.getTimestamp(), me.getSentBy(), me.getSendTo(),
                    me.getData());
                sendTo(meToAll, userAndPort.get(e), e);
            }
        }
    }

    static void buildChat(String sendBy, String sentTo) {
        System.out.println("建立个人会话:\n" + sendBy + "和" + sentTo);
        if (userChatWith.containsKey(sendBy)) {
            if (!userChatWith.get(sendBy).contains(sentTo)) {
                userChatWith.get(sendBy).add(sentTo);
            }
        } else {
            userChatWith.put(sendBy, new ArrayList<>());
            userChatWith.get(sendBy).add(sentTo);
        }

        if (userChatWith.containsKey(sentTo)) {
            if (!userChatWith.get(sentTo).contains(sendBy)) {
                userChatWith.get(sentTo).add(sendBy);
            }
        } else {
            userChatWith.put(sentTo, new ArrayList<>());
            userChatWith.get(sentTo).add(sendBy);
        }
    }

    static void buildChatG(String group) {
        System.out.println("建立群组会话\n" + group);
        String[] s = group.split("/-/-/");
        Arrays.stream(s).forEach(who -> {
            if (userChatWithG.containsKey(who)) {
                userChatWithG.get(who).add(group);
            } else {
                userChatWithG.put(who, new ArrayList<>());
                userChatWithG.get(who).add(group);
            }
        });

    }

    static void deleteChat(String out) {
        //用来通知哪些人已经下线了，每个人通知一次
        HashMap<String, Integer> copy = new HashMap<>(userAndPort);
        if (userChatWith.containsKey(out)) {
            userChatWith.forEach((t, k) -> {
                if (k.contains(out) && copy.containsKey(t)) {
                    k.remove(out);
                    Message me = new Message(5, System.currentTimeMillis(), "service", t, out);
                    sendTo(me, copy.get(t));
                    copy.remove(t);
                }
            });
            userChatWith.remove(out);
        }
        if (userChatWithG.containsKey(out)) {
            userChatWithG.get(out).forEach(t -> {
                String[] chats = t.split("/-/-/");
                Arrays.stream(chats).forEach((k) -> {
                    if (copy.containsKey(k)) {
                        Message me = new Message(5, System.currentTimeMillis(), "service", k, out);
                        sendTo(me, copy.get(k));
                        copy.remove(k);
                    }
                });
            });
            userChatWithG.remove(out);
        }
        userAndPort.remove(out);

    }

    static void getOnline(Message message) {
        StringBuilder sb = new StringBuilder();
        userAndPort.forEach((k, v) -> {
            if (!Objects.equals(k, message.getSentBy())) {
                sb.append(k).append("/-/-/");
            }
        });
        Message send = new Message(2, System.currentTimeMillis(), "service", message.getSentBy(),
            sb.toString());
        sendTo(send, message.getPort());
    }

    static void deleteOnline(Message message) {
        userAndPort.remove(message.getSentBy());
    }

    static void senLogin(Message me) {
        String user = me.getSentBy();
        if (userAndPort.containsKey(user)) {
            sendTo(new Message(6, System.currentTimeMillis(), "service", me.getSentBy(), "false"),
                me.getPort());
        } else if (database.checkUser(me.getSentBy(), me.getData())) {
            sendTo((new Message(6, System.currentTimeMillis(), "service", me.getSentBy(), "true")),
                me.getPort());
        } else if (!database.checkUser(me.getSentBy(), me.getData())){
            sendTo(new Message(6, System.currentTimeMillis(), "service", me.getSentBy(), "密码错误"),
                me.getPort());
        }else {
            sendTo((new Message(6, System.currentTimeMillis(), "service", me.getSentBy(), "false")),
                me.getPort());
        }

    }

    static void checkOnline() {
        new Thread(() -> {
            userAndPort.forEach((k, v) -> {
                Message me = new Message(3, System.currentTimeMillis(), "service", k, null);
                try {
                    sendTo(me, v);
                } catch (Exception e) {
                    deleteChat(k);
                }
            });
        }).start();
    }

}
