package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatGroup;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Controller implements Initializable {

    @FXML
    public TextArea inputArea;
    @FXML
    ListView<Message> chatContentList;

    @FXML
    ListView<User> chatList;
    @FXML
    Label onlineNumber;
    @FXML
    Label whoOnline;

    boolean canLogin;

    boolean readMessageFinish = false;

    //聊天列表
    ArrayList<User> chatWith = new ArrayList<>();

    HashMap<String, ChatGroup> groups = new HashMap<>();

    //消息记录
    String[] online = new String[0];

    HashMap<String, List<Message>> allMessage = new HashMap<>();
    String username;

    boolean isOnline = true;

    ObservableList<String> users = FXCollections.observableArrayList();


    int port;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Random random = new Random();
        int port = random.nextInt(1000) + 1000;
        this.port = port;

        //给两天列表添加监听器

        while (!canLogin) {
            Label login = new Label("账号");
            Label pwd = new Label("密码");
            TextField textField = new TextField();
            PasswordField passwordField = new PasswordField();
            Dialog<ButtonType> dialog1 = new Dialog<>();
            dialog1.getDialogPane().setContent(new VBox(login, textField, pwd, passwordField));
            dialog1.getDialogPane().getButtonTypes().add(ButtonType.OK);
            ButtonType zc = new ButtonType("注册");
            ButtonType end = new ButtonType(("exit"));
            dialog1.getDialogPane().getButtonTypes().add(zc);
            dialog1.getDialogPane().getButtonTypes().add(end);


            Optional<ButtonType> result = dialog1.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    ServerSocket ss = new ServerSocket(port);
                    String mid = textField.getText();
                    String pw = passwordField.getText();
                    sendLoginRe(mid, pw);
                    Socket socket = ss.accept();
                    InputStream in = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int len = in.read(bytes);
                    Message me = Message.getMessage(
                        new String(bytes, 0, len, StandardCharsets.UTF_8));
                    if (me.getData().equals("true")) {
                        canLogin = true;
                        username = mid;
                        System.out.println("登录成功: " + username);
                        ss.close();
                        socket.close();
                        continue;
                    }
                    Platform.runLater(() -> {
                        System.out.println("登录失败");
                        TanChuang("登录失败", "用户已经在线或密码错误");
                    });

                    ss.close();
                    socket.close();

                } catch (Exception e) {
                    TanChuang("登录失败","服务器脱机");
                }
            } else if (result.isPresent() && result.get() == zc) {
                Message me = new Message(7, System.currentTimeMillis(), textField.getText(),
                    "service", passwordField.getText());
                sendMess(me);
            } else {
                Platform.exit();
                System.exit(0);
                return;
            }
        }

        chatList.getSelectionModel().selectedItemProperty().addListener(
            (observableValue, selectionMode, t1) -> {
                if (t1 == null) {
                    return;
                }
                if (t1.isGroup()) {
                    chatContentList.getItems().clear();
                    groups.get(t1.getName()).getChatMessage().stream()
                        .sorted(Comparator.comparingLong(Message::getTimestamp)).
                        forEach(t -> chatContentList.getItems().add(t));
                } else {
                    chatContentList.getItems().clear();
                    allMessage.get(t1.getName()).stream()
                        .sorted(Comparator.comparingLong(Message::getTimestamp))
                        .forEach(t -> chatContentList.getItems().add(t));
                }
                updateView();
            }
        );

        try {
            Message loginMe = new Message(1, System.currentTimeMillis(), username, "service",
                "服务器数据");
            loginMe.setPort(port);
            sendMess(loginMe);
        } catch (Exception e) {
            TanChuang("连接失败", "服务器不在线");
        }

        updateMessage();
        inputArea.onKeyPressedProperty().addListener(e -> doSendMessage());
        chatContentList.setCellFactory(new MessageCellFactory());
    }

    @FXML
    public void createPrivateChat() {
        sentOnlineRe();

        Platform.runLater(() -> {
            AtomicReference<String> user = new AtomicReference<>();
            Stage stage = new Stage();

            ComboBox<String> userSel = new ComboBox<>();
            userSel.getItems().addAll(online);

            Button okBtn = new Button("OK");
            okBtn.setOnAction(e -> {
                user.set(userSel.getSelectionModel().getSelectedItem());
                for (User t : chatWith) {
                    if (t.getName().equals(user.get())) {
                        chatList.getSelectionModel().select(new User(user.get()));
                        stage.close();
                        return;
                    }
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
        sentOnlineRe();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            ComboBox<String> userSel = new ComboBox<>();
            userSel.getItems().addAll(online);
            List<String> list = Arrays.stream(online).collect(Collectors.toList());
            ListView<String> on = new ListView<>();
            list.forEach(t -> on.getItems().add(t));

            stage.setScene(new Scene(on));
            MultipleSelectionModel<String> mu = on.getSelectionModel();
            mu.setSelectionMode(SelectionMode.MULTIPLE);
            ObservableList<String> ob = mu.getSelectedItems();
            Button okBtn = new Button("OK");
            okBtn.setOnAction(e -> {
                List<String> list1 = ob.stream().sorted().collect(Collectors.toList());
                list1.add(username);
                list1.sort(String::compareTo);
                ChatGroup chatGroup = new ChatGroup(list1);
                System.out.println(chatGroup);
                groups.put(chatGroup.getGroupName(), new ChatGroup(list1));
                addNewChat(new User(chatGroup));
                stage.close();
            });
            HBox box = new HBox(100);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(20, 20, 20, 20));
            box.getChildren().addAll(on, okBtn);
            stage.setScene(new Scene(box));
            stage.showAndWait();
        });

    }

    @FXML
    public void viewOnline() {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            ComboBox<String> userSel = new ComboBox<>();
            userSel.getItems().addAll(online);
            ListView<String> on = new ListView<>();
            on.setItems(users);
            stage.setScene(new Scene(on));
            Button okBtn = new Button("OK");
            HBox box = new HBox(100);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(20, 20, 20, 20));
            box.getChildren().addAll(on, okBtn);
            stage.setScene(new Scene(box));
            stage.showAndWait();
        });
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
        int type;
        User sendU = chatList.getSelectionModel().getSelectedItem();

        if (sendU == null || sendU.getName() == null) {
            Platform.runLater(() -> TanChuang("警告", "没有选中聊天对象，真可怜"));

            return;
        }
        String sendTo = sendU.getName();
        if (!sendU.isGroup()) {
            type = 0;
        } else {
            type = 4;
        }

        if (inputArea.getText().equals("")) {
            return;
        }
        Message messageSent = new Message(type, System.currentTimeMillis(), username, sendTo,
            inputArea.getText());
        System.out.println(messageSent);
        if (!sendMess(messageSent)) {
            return;
        }
        if (type == 0) {
            allMessage.get(sendTo).add(messageSent);
            chatContentList.getItems().clear();
            allMessage.get(messageSent.getSendTo()).stream().sorted(
                    Comparator.comparingLong(Message::getTimestamp))
                .forEach(t -> chatContentList.getItems().add(t));
        }
        inputArea.clear();
        updateView();
    }

    public boolean sendMess(Message me) {
        try {
            if (!isOnline) {
                return false;
            }
            if (me.getData() == null && me.getType() == 2) {
                return false;
            }
            Socket s = new Socket("localhost", 1234);
            OutputStream outputStream = s.getOutputStream();
            outputStream.write(me.getJson().getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (Exception e) {
            if (me.getType() != 2) {
                Platform.runLater(() -> TanChuang("发送失败", "可能正在脱机工作"));

            } else if (isOnline) {
                isOnline = false;
                Platform.runLater(() -> TanChuang("下线通知", "服务器炸了，快给服主捐点吧"));

            }
            return false;
        }
        return true;
    }

    public void getOnlineAccept(Message me) {
        if (me.getData().equals("")) {
            online = new String[0];
        } else {
            online = me.getData().split("/-/-/");
        }
        Platform.runLater(() -> {
            users.clear();
            users.addAll(Arrays.asList(online));
            users.add(username);
        });

    }

    public void addNewChat(String userSel) {
        if (userSel == null || userSel.isEmpty()) {
            return;
        }
        //聊天列表里添加聊天对象
        User user = new User(userSel);
        chatWith.add(user);
        allMessage.put(userSel, new ArrayList<>());
        Platform.runLater(() ->
        {

            //添加聊天信息

            //放到显示列表里
            chatList.getItems().add(user);
            chatList.getSelectionModel().select(user);
        });

    }

    public void addNewChat(User user) {
        chatWith.add(user);
        Platform.runLater(() -> {

            //放到显示列表里
            chatList.getItems().add(user);
            chatList.getSelectionModel().select(user);
            if (!groups.containsKey(user.getName())) {
                groups.put(user.getName(), user.group());
            }
        });

    }

    public void sentOnlineRe() {
        try {
            Message mes = new Message(2, System.currentTimeMillis(), username, "service",
                "在线请求");
            mes.setPort(port);
            sendMess(mes);
        } catch (Exception e) {
            System.out.println("请求失败");
        }
    }

    public void sendLoginRe(String username, String pwd) {
        Message me = new Message(6, System.currentTimeMillis(), username, "service", pwd);
        me.setPort(port);
        sendMess(me);
    }

    /**
     * 弹窗代码
     */
    public void TanChuang(String type, String s) {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle(type);
                alert.setContentText(s);
                alert.showAndWait();
            } catch (Exception e) {
                System.out.println("弹窗错误");
            }

        });

    }

    public void updateMessage() {
        new Thread(() -> {
            while (true) {
                sentOnlineRe();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> {

                    whoOnline.setText(username);
                    if (!isOnline) {
                        onlineNumber.setText("不在线");
                    } else {
                        onlineNumber.setText("online: " + (online.length + 1));
                    }
                });
            }
        }).start();
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
