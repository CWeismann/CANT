import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

        clients = new CopyOnWriteArrayList<>();

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
            // clientListBuilder.append(client.getClientId()).append(",");
            if (!client.getClientName().equals(""))
                clientListBuilder.append(client.getClientName()).append(",");
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
        private String clientId;
        private String username;

        public ClientHandler(SSLSocket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            try {
                System.out.println("CLIENTHANDLER");

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientId = "Client" + System.currentTimeMillis(); // Assign unique client ID
                username = clientId;
            } catch (IOException e) {
                System.out.println("error");

                e.printStackTrace();
                clientSocket.close();
            }
            broadcastClientList();
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientName(String newName) {
            System.out.println(newName);
            this.username = newName;
        }

        public String getClientName() {
            return username;
        }

        @Override
        public void run() {
            try {
                String message = in.readLine();
                System.out.println("message = " + message);
                while (message != null) {
                    String[] parts = message.split(":", 4); // Split message into recipient and content
                    if (parts.length == 1) {
                        System.out.println("CLOSING IN HANDLER");
                        in.close();
                        out.close();
                        clientSocket.close();
                        clients.remove(this); // Remove client from the list upon disconnection
                        broadcastClientList();
                    } else if (parts.length == 4) {
                        String timestamp = parts[0]; // placeholder currently space
                        String recipient = parts[1];
                        String sender = parts[2];
                        String content = parts[3];
                        // String sender = this.getClientId();
                        // Log the message to the database
                        databaseManager.addToMessageDB(sender,recipient,content);
                        // Send message to the intended recipient
                        for (ClientHandler client : clients) {
                            // if (client.getClientId().equals(recipientId)) {
                            if (client.getClientName().equals(recipient)) {
                                // client.sendMessage(clientId + ": " + content);
                                System.out.print("recipient: " + recipient + ", ");
                                System.out.print("sender: " + sender + "\n");
                                client.sendMessage(sender + ": " + content);
                                break;
                            }
                        }
                    } else if (parts.length == 3){
                        parts = message.split(":", 3);
                        String clientUsername = parts[0];
                        String clientPw = parts[1];
                        String reg = parts[2];
                        Boolean loggedin = false;
 
                        // Having a map of client id to client handlers would be nice. 
                        for (ClientHandler client : clients) {
                            loggedin = false;
                            if (client.getClientId().equals(this.getClientId())) {
                                if (reg.equals("register")){
                                    if (logindb.addLoginCredentials(clientUsername, clientPw)){
                                        client.sendMessage("Login:1");
                                    } else {
                                        client.sendMessage("Login:3");
                                    }
                                    
                                } else {
                                    if (logindb.checkCredentials(clientUsername, clientPw)){
                                        client.sendMessage("Login:0");
                                        this.setClientName(clientUsername);
                                        loggedin = true;
                                    } else {
                                        client.sendMessage("Login:2");
                                    }
                                }
                                if(loggedin) {
                                    // try {
                                    //     in.close();
                                    //     out.close();
                                    // } catch (IOException e) {
                                    //     e.printStackTrace();
                                    // }
                                    broadcastClientList();
                                    break;
                                } else {
                                    // System.out.println("closing!!");
                                    try {
                                        in.close();
                                        out.close();
                                        clientSocket.close();
                                        clients.remove(this); // Remove client from the list upon disconnection
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    broadcastClientList();
                                    return;
                                }
                            }
                        }
                        continue;
                    } else {
                        System.out.println(parts[0]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            //     try {
            //         in.close();
            //         out.close();
            //         clientSocket.close();
            //         clients.remove(this); // Remove client from the list upon disconnection
                broadcastClientList(); // Broadcast updated client list
            //     } catch (IOException e) {
            //         e.printStackTrace();
            //     }
            // }
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
