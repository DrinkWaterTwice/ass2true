package cn.edu.sustech.cs209.chatting.client;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
        Scanner in = new Scanner(System.in);
        while (true) {
            try {
                Socket s = new Socket("localhost", 1234);
                OutputStream outputStream = s.getOutputStream();
                byte[] a = in.next().getBytes();
                outputStream.write(a);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
    }
}
