import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final String KEYSTORE_LOCATION = "server_keystore.jks";
    private static final String KEYSTORE_PASSWORD = "serverpassword";
    private static final String TRUSTSTORE_LOCATION = "server_truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "serverpassword";

    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static Map<String, Password> users = new HashMap<>();

    public static void main(String[] args) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(KEYSTORE_LOCATION), KEYSTORE_PASSWORD.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());
            
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(new FileInputStream(TRUSTSTORE_LOCATION), TRUSTSTORE_PASSWORD.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(PORT);

            System.out.println("Server started. Waiting for clients...");

            while (true) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final SSLSocket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String username;

        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                // Authenticate or register the user
                boolean isAuthenticated = false;
                while (!isAuthenticated) {
                    // writer.println("LOGIN or REGISTER?");
                    String line = reader.readLine();
                    if (line.equalsIgnoreCase("LOGIN")) {
                        isAuthenticated = login();
                    } else if (line.equalsIgnoreCase("REGISTER")) {
                        register();
                    } else if (line.equalsIgnoreCase("FORGOT")) {
                        forgotPassword();
                    } else {
                        writer.println("Invalid option. Please enter LOGIN or REGISTER.");
                    }
                }

                // Start handling messages
                handleMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    clients.remove(username);
                    // users.remove(username);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean login() throws IOException {
            String username = reader.readLine();
            Password storedPassword = users.get(username);
            String hashedPassword = Password.hashPassword(reader.readLine(), storedPassword.getSalt());

            // Check if the user exists and the password matches
            if (users.containsKey(username) && storedPassword.getHashedPassword().equals(hashedPassword)) {
                this.username = username;
                clients.put(username, writer);
                writer.println("LOGIN_SUCCESS");
                return true;
            } else {
                writer.println("LOGIN_FAILED");
                return false;
            }
        }

        private boolean register() throws IOException {
            // writer.println("Enter your desired username:");
            String username = reader.readLine();
            // writer.println("Enter your desired password:");
            String password = reader.readLine();
        
            // Check if the username is available
            if (!users.containsKey(username)) {
                Password hashedPassword = new Password(password);
                users.put(username, hashedPassword);
                writer.println("REGISTRATION_SUCCESS");
                return true;
            } else {
                writer.println("REGISTRATION_FAILED");
                return false;
            }
        }

        private void forgotPassword() throws IOException {
            // doesn't work yet but looks cool
            String username = reader.readLine();
        
            if (users.containsKey(username)) {
                String smtpServer = "your_smtp_server";
                int port = 25; // Default SMTP port

                String senderEmail = "noreply@cant.com";
                String recipientEmail = username;
                String subject = "Password Recovery";
                String messageBody = "Bla Bla Bla"; // replace with temp password

                try {
                    // Connect to the SMTP server
                    Socket emailSocket = new Socket(smtpServer, port);
                    PrintWriter out = new PrintWriter(emailSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(emailSocket.getInputStream()));

                    // Send email commands
                    out.println("HELO " + smtpServer);
                    out.println("MAIL FROM:<" + senderEmail + ">");
                    out.println("RCPT TO:<" + recipientEmail + ">");
                    out.println("DATA");
                    out.println("Subject: " + subject);
                    out.println(); // Empty line to separate headers from message body
                    out.println(messageBody);
                    out.println(".");
                    out.println("QUIT");

                    // Read server response
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("Server: " + response);
                        if (response.startsWith("250 ")) {
                            // 250 code indicates successful command execution
                            break;
                        }
                    }

                    // Close the connection
                    out.close();
                    in.close();
                    emailSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.println("A recovery password has been sent to your email.");
        }
        
        private void handleMessages() throws IOException {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("@")) {
                    // Direct message
                    int spaceIndex = message.indexOf(" ");
                    if (spaceIndex != -1) {
                        String recipient = message.substring(1, spaceIndex);
                        String directMessage = message.substring(spaceIndex + 1);
                        sendMessage(username, recipient, directMessage);
                    } else {
                        writer.println("Invalid direct message format. Use '@username message'");
                    }
                } else {
                    // Broadcast message
                    broadcastMessage(username + ": " + message);
                }
            }
        }

        private void sendMessage(String sender, String recipient, String message) {
            PrintWriter recipientWriter = clients.get(recipient);
            if (recipientWriter != null) {
                recipientWriter.println("(Direct from " + sender + "): " + message);
            } else {
                writer.println("User '" + recipient + "' is not online. Message will be delivered when they are back online.");
                // Save message to be delivered later
                // Implement message persistence logic here
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter client : clients.values()) {
                client.println(message);
            }
        }
    }
}
