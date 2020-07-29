package com.example.Messenger.messenger;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.net.*;
import static java.lang.System.out;

public class Server {

    private static ArrayList<String> users = new ArrayList<>();//list of users
    private static ArrayList<MessagingThread> clients = new ArrayList<>();//list of active threads

    public static void main(String[]args) throws Exception {
      ServerSocket server = new ServerSocket(80, 10);
        out.println("Now Server Is Running");//print in the console

        
        //either create from here and delete every time project is run or directly from database
        //DbOperations.createUsersTable("users");
        //DbOperations.createChatTable("chat_backup");
        while (true) {
            Socket client = server.accept();
            MessagingThread thread = new MessagingThread(client);//constructor is called
            clients.add(thread);
            thread.start();
        }
    }

    public static void sendToAll(String user, String message) {

        for (MessagingThread c : clients) {
            if (!c.getUser().equals(user)) {
                c.sendMessage(user, message);
            }else{
                c.sendToMe(user, message);
            }
        }
    }

    static class MessagingThread extends Thread {

        String user = "";
        BufferedReader input;
        PrintWriter output;

        public MessagingThread(Socket client) throws Exception {

            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);

            user = input.readLine();
            users.add(user);

            DbOperations.addUserInDB(user);
        }

        public void sendMessage(String chatUser, String msg) {
            output.println(chatUser + ": " + msg);
        }

        public void sendToMe(String chatUser, String msg){
            output.println("You: " + msg);
        }

        public String getUser() {
            return user;
        }

        public void saveInDB(String chatUser, String msg) throws SQLException {
            String msg_id = chatUser + "_" + System.currentTimeMillis();
            DbOperations.chatBackUp(user, msg_id, msg);
        }

        @Override
        public void run() {
            String msg;
            try {
                while (true) {
                    msg = input.readLine();
                    if (msg.equals("end")) {
                        clients.remove(this);
                        users.remove(user);
                        break;
                    }else {
                        sendToAll(user, msg);
                        saveInDB(user, msg);
                    }
                }
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
