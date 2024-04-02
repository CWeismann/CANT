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
    private MessageDBManager databaseManager;

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
                    String[] parts = message.split(":", 3); // Split message into recipient and content
                    if (parts.length == 3) {
                        sender = parts[0];
                        recipient = parts[1];
                        content = parts[2];
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

        public String getClientUser() {
            return sender;
        }
       

        @Override
        public void run() {
            try {
                // String message;
                databaseManager.addToMessageDB(this.getClientUser(), recipient, content);
                // Send message to the intended recipient
                for (ClientHandler client : clients) {
                    if (client.getClientUser().equals(recipient)) {
                        client.sendMessage(recipient + ": " + content);
                        System.out.println(recipient + ": " + content);
                             break;
                    }
                }
            // } catch (IOException e) {
            //     e.printStackTrace();
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
