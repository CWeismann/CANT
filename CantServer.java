import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

public class CantServer extends JFrame {
    private JTextArea chatArea;
    private List<ClientHandler> clients;
    // private List<LoginHandler> logins;
    private MessageDBManager databaseManager;
    private LoginDBManager logindb;

    private volatile boolean running = true;

    public CantServer() {
        setTitle("CANT Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        setVisible(true);

        clients = new ArrayList<>();

        databaseManager = new MessageDBManager();
        logindb = new LoginDBManager();
        // loginScreen = new LoginGUI();

        startServer();
    }

    private void startServer() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("server.keystore"), "AlamoStaffedDerivative".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "AlamoStaffedDerivative".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(12345);

            appendToChatArea("Server started, listening on port 12345...");

            while (running) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                appendToChatArea("Client connected: " + clientSocket.getInetAddress().getHostName());

                // Handle client connection in a separate thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                // Update client list for all clients
                broadcastClientList();
            }

            serverSocket.close();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException |
                CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            running = false;
        }
    }

    private void appendToChatArea(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void broadcastClientList() {
        StringBuilder clientListBuilder = new StringBuilder();
        for (ClientHandler client : clients) {
            clientListBuilder.append(client.getClientUser()).append(",");
        }
        String clientList = clientListBuilder.toString();
        for (ClientHandler client : clients) {
            client.sendMessage("CLIENT_LIST:" + clientList);
        }
    }

    private class ClientHandler implements Runnable {
        private final SSLSocket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        // private String clientId;
        private String clientUser;
        private String sender;
        private String message; 
        private String recipient; 
        private String content; 

        public ClientHandler(SSLSocket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(":", 4); // Split message into recipient and content
                    if (parts.length == 3) {
                        sender = parts[0];
                        content = parts[1];
                        recipient = parts[2];

                    }
                    else{ 
                        parts = message.split(":", 4);
                        String clientUsername = parts[0];
                        // Panel.setText("recieved login username: " + clientUsername);
                        String clientPw = parts[1];
                        System.out.println("recieved login password: " + clientPw);
                        String reg = parts[2];
                        System.out.println("recieved reg: " + reg);

                    }
                }
                //clientId = "Client" + System.currentTimeMillis(); // Assign unique client ID
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // public String getClientId() {
        //     return clientId;
        // }
        public String getClientUser(){
            return clientUser; 
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("MESSAGE RECIEVED IN SERVER: " + message);
                    String[] parts = message.split(":"); // Split message into recipient and content

                // if (parts.length == 4){
                    parts = message.split(":");
                    String clientUsername = parts[0];
                    // Panel.setText("recieved login username: " + clientUsername);
                    String clientPw = parts[1];
                    System.out.println("recieved login password: " + clientPw);
                    String reg = parts[2];
                    System.out.println("recieved reg: " + reg);


                    // Having a map of client id to client handlers would be nice. 
                    for (ClientHandler client : clients) {
                        if (client.getClientUser().equals(this.getClientUser())) {
                            if (reg.equals("register")){
                                logindb.addLoginCredentials(clientUsername, clientPw, 70);
                                client.sendMessage("Register:1");
                            } else {
                                if (logindb.checkCredentials(clientUsername, clientPw)){
                                    client.sendMessage("Login:0");
                                } else {
                                    client.sendMessage("Login:2");
                                }
                            }
                            try {
                                in.close();
                                out.close();
                                clientSocket.close();
                                clients.remove(this); // Remove client from the list upon disconnection
                                broadcastClientList(); // Broadcast updated client list
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                // } else {
                //     System.out.println(parts[0]);
                // }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                    clients.remove(this); // Remove client from the list upon disconnection
                    broadcastClientList(); // Broadcast updated client list
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CantServer();
        });
    }
}
