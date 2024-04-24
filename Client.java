import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String TRUSTSTORE_LOCATION = "client_truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "clientpassword";

    // chat frame
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private PrintWriter writer;
    private String username;

    // login frame
    private JFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton forgotButton;
    Color backgroundColor = Color.BLACK;
    Color textColor = new Color(0, 255, 0); // Green

    public Client() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeGUI();
        connectToServer();
        showLoginOrRegisterPopup();
    }

    private void initializeGUI() {
        // Initialize the chat frame
        frame = new JFrame("CANT");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(backgroundColor);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(backgroundColor);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(backgroundColor);
        chatArea.setForeground(textColor);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);
    
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
    
        messageField = new JTextField();
        messageField.setBackground(backgroundColor);
        messageField.setForeground(textColor);
        messageField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bottomPanel.add(messageField, BorderLayout.CENTER);
    
        // Add ActionListener to messageField for sending message on Enter press
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        sendButton.setBackground(backgroundColor);
        sendButton.setForeground(textColor);
        sendButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bottomPanel.add(sendButton, BorderLayout.EAST);
    
        panel.add(bottomPanel, BorderLayout.SOUTH);
    
        frame.getContentPane().add(panel);
        frame.setVisible(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveMessagesToFile();
                frame.dispose();
            }
        });
    }    

    private void connectToServer() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(new FileInputStream(TRUSTSTORE_LOCATION), TRUSTSTORE_PASSWORD.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(SERVER_HOST, SERVER_PORT);

            writer = new PrintWriter(socket.getOutputStream(), true);

            // Start a separate thread for receiving messages
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        // Check if the message indicates successful registration
                        if (message.startsWith("LOGIN_SUCCESS")) {
                            // Close the window after successful login
                            loginFrame.dispose();
                            frame.setVisible(true);
                            openMessagesFile();
                        } else if (message.startsWith("LOGIN_FAILED")) {
                            JOptionPane.showMessageDialog(frame, "Login failed. Please try again.");
                        } else if (message.startsWith("REGISTRATION_SUCCESS")) {
                            JOptionPane.showMessageDialog(frame, "Registration successful. You can now login.");
                        } else if (message.startsWith("REGISTRATION_FAILED")) {
                            JOptionPane.showMessageDialog(frame, "Registration failed. Please try again.");
                        } else {
                            chatArea.append(message + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            if (message.startsWith("@")) {
                // Direct message
                int spaceIndex = message.indexOf(" ");
                if (spaceIndex != -1) {
                    String recipient = message.substring(1, spaceIndex);
                    String directMessage = message.substring(spaceIndex + 1);
                    chatArea.append("(Direct to " + recipient + "): " + directMessage + "\n");;
                } else {
                    writer.println("Invalid direct message format. Use '@username message'");
                }
            }
            writer.println(message);
            messageField.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoginOrRegisterPopup() {
        loginFrame = new JFrame("Login or Register");
        loginFrame.setSize(400, 150);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(4, 2));
        loginPanel.setBackground(backgroundColor);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(textColor);
        usernameLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(usernameLabel);
        usernameField = new JTextField();
        usernameField.setBackground(backgroundColor);
        usernameField.setForeground(textColor);
        usernameField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(textColor);
        passwordLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(passwordLabel);
        passwordField = new JPasswordField();
        passwordField.setBackground(backgroundColor);
        passwordField.setForeground(textColor);
        passwordField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("LOGIN");
                writer.println(usernameField.getText());
                writer.println(passwordField.getPassword());
            }
        });
        loginButton.setBackground(backgroundColor);
        loginButton.setForeground(textColor);
        loginButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(loginButton);

        forgotButton = new JButton("Forgot Password?");
        forgotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writer.println("FORGOT");
                writer.println(usernameField.getText());
            }
        });
        forgotButton.setBackground(backgroundColor);
        forgotButton.setForeground(textColor);
        forgotButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(forgotButton);

        registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = new String(passwordField.getPassword());
                Pattern pattern = Pattern.compile("\\d");
                Matcher matcher = pattern.matcher(password);
                if (isValidEmail(usernameField.getText())) {
                    JOptionPane.showMessageDialog(frame, "Username must be a valid email.");
                } else if (password.length() < 8) {
                    JOptionPane.showMessageDialog(frame, "Password must be eight characters or longer.");
                } else if (!matcher.find()) {
                    JOptionPane.showMessageDialog(frame, "Password must contain a number.");
                } else {
                    writer.println("REGISTER");
                    writer.println(usernameField.getText());
                    writer.println(passwordField.getPassword());
                }
            }
        });
        registerButton.setBackground(backgroundColor);
        registerButton.setForeground(textColor);
        registerButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
        loginPanel.add(registerButton);

        loginFrame.getContentPane().add(loginPanel);
        loginFrame.setVisible(true);
    }

    private void saveMessagesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(username + "_messages.txt"))) {
            writer.println(chatArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openMessagesFile() {
        username = usernameField.getText();
        File file = new File(username + "_messages.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    chatArea.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public static boolean isValidEmail(String email) {
        String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\n$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Client();
        });
    }
}
