package cn.edu.sustech.cs209.chatting.client;
import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class Main extends Application {


    public static void main(String[] args) {
        launch();

        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));

        Controller controller = fxmlLoader.getController();
        stage.setTitle(controller.username);
        //接受消息的线程
        Task<String> task1 = new Task<String>() {
            Message me;
            @Override
            protected String call() {
                try {
                    ServerSocket ss = new ServerSocket(controller.port);
                    while (true) {
                        Socket socket = ss.accept();
                        InputStream in = socket.getInputStream();
                        byte[] bytes = new byte[1024];
                        int len = in.read(bytes);
                        me = Message.getMessage(new String(bytes, 0, len, StandardCharsets.UTF_8));
                        if (me.getType() == 0) {
                            if (!controller.allMessage.containsKey(me.getSentBy())){
                                controller.addNewChat(me.getSentBy());
                            }
                            controller.allMessage.get(me.getSentBy()).add(me);
                            Platform.runLater(() -> {
                                if (controller.chatList.getSelectionModel().getSelectedItem()
                                    .equals(me.getSentBy())) {
                                    controller.chatContentList.getItems().clear();
                                    controller.allMessage.get(me.getSentBy())
                                        .forEach(t -> controller.chatContentList.getItems().add(t));
                                }
                            });
                        }
                        if (me.getType() == 2) {
                            controller.getOnlineAccept(me);
                        }
                        in.close();
                        socket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("接受失败");
                }
                return "ok";
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(task1);
        stage.show();
        stage.setOnCloseRequest(e -> {
            Message message = new Message(10, System.currentTimeMillis(), controller.username,
                "service", "退出登录");
            controller.sendMess(message);
            System.out.println("结束成功");
            Platform.exit();
        });
    }
}
