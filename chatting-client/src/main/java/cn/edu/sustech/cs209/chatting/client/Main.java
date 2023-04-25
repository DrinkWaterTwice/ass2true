package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatGroup;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {


    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main1.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));

        Controller controller = fxmlLoader.getController();
        stage.setTitle(controller.username);

        //接受消息的线程

        stage.show();
        stage.setOnCloseRequest(e -> {
            Message message = new Message(10, System.currentTimeMillis(), controller.username,
                "service", "退出登录");
            controller.sendMess(message);
            System.out.println("结束成功");
            Platform.exit();
        });

        Task<String> task1 = new Task<String>() {
            @Override
            synchronized protected String call() {
                try {
                    ServerSocket ss = new ServerSocket(controller.port);
                    while (true) {
                        Socket socket = ss.accept();
                        InputStream in = socket.getInputStream();
                        byte[] bytes = new byte[1024];
                        int len = in.read(bytes);
                        final Message me = Message.getMessage(
                            new String(bytes, 0, len, StandardCharsets.UTF_8));
                        if (me.getType() == 0) {
                            if (me.getSentBy().equals(controller.username)) {
                                //接受缓存的消息
                                if (!controller.allMessage.containsKey(me.getSendTo())) {
                                    controller.addNewChat(me.getSendTo());
                                }
                                controller.allMessage.get(me.getSendTo()).add(me);
                                continue;
                            }
                            //如果聊天列表没有这个窗口，则创建一个新的窗口
                            if (!controller.allMessage.containsKey(me.getSentBy())) {
                                controller.addNewChat(me.getSentBy());
                            }
                            Platform.runLater(() -> {
                                controller.allMessage.get(me.getSentBy()).add(me);
                                if (controller.chatList.getSelectionModel().getSelectedItem()
                                    .getName()
                                    .equals(me.getSentBy())) {
                                    controller.chatContentList.getItems().clear();
                                    controller.allMessage.get(me.getSentBy())
                                        .forEach(t -> controller.chatContentList.getItems().add(t));
                                } else {
                                    Platform.runLater(() -> {
                                        controller.TanChuang("收到私人消息",
                                            "来自" + me.getSentBy());
                                    });
                                }
                            });
                        }
                        if (me.getType() == 4) {
                            List<String> strings = Arrays.stream(me.getSendTo().split("/-/-/"))
                                .collect(
                                    Collectors.toList());
                            ChatGroup group = new ChatGroup(strings);
                            if (!controller.groups.containsKey(me.getSendTo())) {
                                controller.groups.put(group.getGroupName(), group);
                                controller.addNewChat(new User(group));
                            }
                            Platform.runLater(() -> {
                                Logger.getLogger("接受到群组信息");
                                controller.groups.get(group.getGroupName()).getChatMessage()
                                    .add(me);
                                if (controller.chatList.getSelectionModel().getSelectedItem()
                                    .getName().equals(group.getGroupName())) {
                                    controller.chatContentList.getItems().clear();
                                    controller.groups.get(group.getGroupName()).getChatMessage().
                                        forEach(t -> {
                                            controller.chatContentList.getItems().add(t);
                                        });
                                } else {
                                    Platform.runLater(() -> controller.TanChuang("收到群组消息",
                                        "来自" + me.getSentBy()));
                                }
                            });
                        }
                        if (me.getType() == 5) {
                            Platform.runLater(() -> {
                                controller.TanChuang("下线通知", me.getData() + "觉得你太烦拔线了");
                            });
                        }
                        if (me.getType() == 6) {
                            if (me.getData().equals("false")) {
                                controller.canLogin = false;
                            } else if (me.getData().equals("密码错误")) {
                                controller.TanChuang("登录失败", "密码错误");
                            } else {
                                controller.canLogin = true;
                            }
                        }

                        if (me.getType() == 2) {
                            controller.getOnlineAccept(me);
                        }
                        if (me.getType() == 3) {

                        }
                        in.close();
                        socket.close();
                    }
                } catch (Exception e) {
                    System.out.println("端口占用");
                    e.printStackTrace();
                }
                return "ok";
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(task1);
    }


}
