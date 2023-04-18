package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;


public class Controller implements Initializable {

    public TextArea inputArea;
    @FXML
    ListView<Message> chatContentList;

    @FXML
    ListView<String> chatList;

    //聊天列表
    ArrayList<String> chatWith = new ArrayList<>();

    //消息记录
    String[] online;

    HashMap<String, List<Message>> allMessage = new HashMap<>();
    String username;

    ServerSocket ser;


    int port;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //给两天列表添加监听器
        chatList.getSelectionModel().selectedItemProperty().addListener(
            (observableValue, selectionMode, t1) -> {
                chatContentList.getItems().clear();
                allMessage.get(t1).stream().sorted(Comparator.comparingLong(Message::getTimestamp))
                    .forEach(t -> chatContentList.getItems().add(t));
            }
        );

        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {

            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
            username = input.get();
        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }

        Random random = new Random();
        int port = random.nextInt(1000) + 1000;
        this.port = port;
        try {
            System.out.println("发送信息");
            Message loginMe = new Message(1, System.currentTimeMillis(), username, "service",
                "服务器数据");
            loginMe.setPort(port);
            sendMess(loginMe);
            //acceptRun();
        } catch (Exception e) {
            //还得加弹窗
            System.out.println("连接失败");
        }

        chatContentList.setCellFactory(new MessageCellFactory());
    }

    @FXML
    public void createPrivateChat() {
        sentOnlineRe();





        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name. You can select
     * several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat: If there are > 3 users: display the
     * first three usernames, sorted in lexicographic order, then use ellipsis with the number of
     * users, for example: UserA, UserB, UserC... (10) If there are <= 3 users: do not display the
     * ellipsis, for example: UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed. After sending the message, you should clear the text input
     * field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
        String sendTo = chatList.getSelectionModel().getSelectedItem();
        System.out.println(sendTo);
        Message messageSent = new Message(0, System.currentTimeMillis(), username, sendTo,
            inputArea.getText());
        System.out.println(messageSent);
        if (!sendMess(messageSent)) {
            System.out.println("mistakecode 2");
            return;
        }

        allMessage.get(sendTo).add(messageSent);
        System.out.println(chatContentList.getItems().size());
        chatContentList.getItems().clear();
        allMessage.get(messageSent.getSendTo()).stream().sorted(
                Comparator.comparingLong(Message::getTimestamp))
            .forEach(t -> chatContentList.getItems().add(t));
        inputArea.clear();
        updateView();
    }

    public boolean sendMess(Message me) {
        try {
            Socket s = new Socket("localhost", 1234);
            OutputStream outputStream = s.getOutputStream();
            outputStream.write(me.getJson().getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void getOnlineAccept(Message me){
        online = me.getData().split("/-/-/");

        Platform.runLater(() -> {
            AtomicReference<String> user = new AtomicReference<>();
            Stage stage = new Stage();
            ComboBox<String> userSel = new ComboBox<>();
            userSel.getItems().addAll(online);

            Button okBtn = new Button("OK");
            okBtn.setOnAction(e -> {
                user.set(userSel.getSelectionModel().getSelectedItem());
                if (chatWith.contains(user.get())){
                    chatList.getSelectionModel().select(user.get());
                    stage.close();
                    return;
                }
                addNewChat(userSel.getSelectionModel().getSelectedItem());
                stage.close();

            });
            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(20, 20, 20, 20));
            box.getChildren().addAll(userSel, okBtn);
            stage.setScene(new Scene(box));
            stage.showAndWait();
        });

    }

    public void addNewChat(String userSel){
        //聊天列表里添加聊天对象
        chatWith.add(userSel);
        //添加聊天信息
        allMessage.put(userSel,new ArrayList<>());
        //放到显示列表里
        chatList.getItems().add(userSel);

        chatList.getSelectionModel().select(userSel);

    }

    public void sentOnlineRe(){
        Message mes = new Message(2,System.currentTimeMillis(), username, "service","在线请求");
        mes.setPort(port);
        sendMess(mes);
    }




    public void updateView() {
        chatContentList.refresh();
    }


    /**
     * You may change the cell factory if you changed the design of {@code Message} model. Hint: you
     * may also define a cell factory for the chats displayed in the left panel, or simply override
     * the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
