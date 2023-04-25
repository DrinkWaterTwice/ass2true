package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class Database {

    String url = "jdbc:postgresql://localhost:5432/t";
    String user = "postgres";
    String pwd = "Qq1754479362";
    private static Connection con = null;

    public static void main(String[] args) {
        Database database = new Database();
        /*Message me = new Message(0, System.currentTimeMillis(), "罗启航", "张三", "额");
        Message me2 = new Message(0, System.currentTimeMillis(), "罗启航", "张三", "你好");
        Message me3 = new Message(0, System.currentTimeMillis(), "罗启航", "张三", "不好了");
        database.creatUser("罗启航", "123456");
        System.out.println(database.checkUser("罗启航", "123456"));
        System.out.println(database.addMessage(me));
        database.addMessage(me2);
        database.addMessage(me3);*/
        ArrayList<Message> mes = database.getMessages("罗启航");
        mes.forEach(System.out::println);

    }

    public Database() {
        getConnection();
    }

    private boolean getConnection() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
            return false;
        }
        try {
            if (con == null) {
                con = DriverManager.getConnection(url, user, pwd);
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed,in courier");
            System.err.println(e.getMessage());
            System.exit(1);
            return false;
        }
        return true;
    }

    public boolean addMessage(Message message) {
        String me = message.getJson();
        String sql =
            "insert into mess (userName, getTime, messages) values " + "('" + message.getSentBy()
                + "','" + message.getTimestamp() + "','" + me + "')";
        String sql1 =
            "insert into mess (userName, getTime, messages) values " + "('" + message.getSendTo()
                + "','" + message.getTimestamp() + "','" + me + "')";
        System.out.println(sql);
        try {
            Statement sta = con.createStatement();
            sta.execute(sql);
            if (message.getType() == 0) {
                sta.execute(sql1);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addMessageG(Message message) {

        String sql =
            "insert into g (groud, mess) values " + "('"
                + message.getSendTo()
                + "','" + message.getJson() + "')";
        System.out.println(sql);
        try {
            Statement sta = con.createStatement();
            sta.execute(sql);
        } catch (Exception e) {

        }
    }


    public boolean creatUser(String user, String pass) {
        String sql = "select * from users where userName = '" + user + "'";
        try {
            Statement sta = con.createStatement();
            ResultSet re = sta.executeQuery(sql);
            if (!re.next()) {
                String s =
                    "insert into users (userName, pass) values ('" + user + "','" + pass + "')";
                sta.executeUpdate(s);
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public boolean checkUser(String user, String pwd) {
        String sql = "select * from users where userName = '" + user + "'";
        try {
            Statement sta = con.createStatement();
            ResultSet re = sta.executeQuery(sql);
            if (!re.next()) {
                return false;
            }
            if (re.getString(2).equals(pwd)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public ArrayList<Message> getMessages(String userName) {
        System.out.println("尝试获取数据");
        String sql = "select * from mess where userName = '" + userName + "'";
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Statement sta = con.createStatement();
            ResultSet re = sta.executeQuery(sql);
            while (re.next()) {
                messages.add(Message.getMessage(re.getString("messages")));
            }
            HashSet<String> me = new HashSet<>();
            ArrayList<Message> mid = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                Message t = messages.get(i);
                if (t.getType() == 4 && !me.contains(t.getSendTo())) {
                    me.add(t.getSendTo());
                    try {
                        String sql1 = "select * from g where groud = '" + t.getSendTo() + "'";
                        ResultSet re1 = sta.executeQuery(sql1);
                        while (re1.next()) {
                            Message message = Message.getMessage(re1.getString(2));
                            if (!Objects.equals(message.getTimestamp(), t.getTimestamp())) {
                                mid.add(message);
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }
            messages.addAll(mid);

        } catch (Exception e) {
            e.printStackTrace();
        }
        messages.forEach(System.out::println);
        System.out.println("获取的数据量" + messages.size());
        return messages;
    }

    public boolean signIn(Message message) {
        String sql = "select * from users where userName = '" + user + "'";
        try {
            Statement sta = con.createStatement();
            ResultSet re = sta.executeQuery(sql);
            if (re.next()) {
                return false;
            }
            sql = "insert into users (username, pass) VALUES ('" + message.getSentBy() + "','"
                + message.getData() + "')";
            sta.execute(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
