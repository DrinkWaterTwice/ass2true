package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class Main {

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(1234);
            while (true) {
                System.out.println("waiting for message");
                Socket socket = ss.accept();

                InputStream inputStream = socket.getInputStream();
                System.out.println("success");
                byte[] buf = new byte[1024];
                int readLen = 0;
                readLen = inputStream.read(buf);
                Message me = Message.getMessage(new String(buf, 0, readLen));
                System.out.println(me.getData());
                inputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
