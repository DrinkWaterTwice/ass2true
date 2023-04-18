package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;


public class Main {

    public static HashMap<String, Integer> userAndPort = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1234);
        while (true) {
            try {
                System.out.println("waiting for message");
                Socket socket = ss.accept();
                InputStream inputStream = socket.getInputStream();
                System.out.println("success");
                byte[] buf = new byte[1024];
                int readLen = 0;
                readLen = inputStream.read(buf);
                Message me = Message.getMessage(
                    new String(buf, 0, readLen, StandardCharsets.UTF_8));
                System.out.println(me);
                if (me.getType() == 1) {
                    userAndPort.put(me.getSentBy(), me.getPort());
                    continue;
                }
                if (me.getType() == 2) {
                    getOnline(me);
                    continue;
                }
                if (me.getType() == 10) {
                    deleteOnline(me);
                }
                if (userAndPort.containsKey(me.getSendTo())) {
                    sendTo(me, userAndPort.get(me.getSendTo()));
                }
                System.out.println(me.getData());
                inputStream.close();
                socket.close();
                System.out.println(me);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static boolean sendTo(Message message, int port) {
        if (!userAndPort.containsKey(message.getSendTo())) {
            return false;
        }
        System.out.println("send" + message);
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
}
