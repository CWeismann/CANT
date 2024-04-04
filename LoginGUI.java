import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;


public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private PrintWriter out;
    private boolean authenticated;
    //private String clientName; 
    //private String clientpw;
    private boolean registerUser;

    // private LoginDBManager databaseManager;
    private static Boolean success = false; 
    String username;
    String password;

    public LoginGUI() {

        setTitle("Login Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        messageLabel = new JLabel("");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.NORTH);

        // databaseManager = new LoginDBManager();

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                password = new String(passwordField.getPassword());
                checkLoginDB(username, password, false);
                // Check if the username exists in the dictionary and if the password matches
                if (authenticated) {
                    messageLabel.setText("Login Successful!");
                    success = true; 
                    try {
                        Thread.sleep(1000); // Sleep for 1 second
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    SwingUtilities.invokeLater(() -> {
                        new CantClient(username, password); 
                    });
                    // /setVisible(false);
                    dispose(); // Close the login page
                    // open chat? 
                } else {
                    messageLabel.setText("Invalid username or password!");
                }
            }

            
        });
        

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Open registration dialog
                new RegisterDialog(LoginGUI.this);
            }
        });

        add(panel);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
        // return success;
    }

    // Inner class for registration dialog
    class RegisterDialog extends JDialog {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public RegisterDialog(JFrame parent) {
            super(parent, "Register New User", true);
            setSize(300, 150);
            setLocationRelativeTo(parent);

            JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel usernameLabel = new JLabel("Username:");
            usernameField = new JTextField();
            JLabel passwordLabel = new JLabel("Password:");
            passwordField = new JPasswordField();

            JButton registerButton = new JButton("Register");
            registerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    checkLoginDB(username, password, true);
                    //databaseManager.addLoginCredentials(username, password, 70); // default salt is 70
                    // For now, let's just print them to console
                    System.out.println("New Username: " + username);
                    System.out.println("New Password: " + password);
                    // messageLabel.setText("Invalid username or password!");

                    // Close the dialog
                    dispose();
                }
            });

            panel.add(usernameLabel);
            panel.add(usernameField);
            panel.add(passwordLabel);
            panel.add(passwordField);
            panel.add(registerButton);

            add(panel);
            setVisible(true);
        }
    }

    public Boolean checkLoginDB(String clientName, String clientpw, Boolean reg){ 
        try {
            SSLSocket socket = connectToServer();
            out = new PrintWriter(socket.getOutputStream(), true);
            if (reg){
                System.out.println ("register");
                out.println(clientName + ":" + clientpw + ":" + "register");
            } else{
                out.println(clientName + ":" + clientpw + ":login");
            }
            new Thread(() -> {
                try {
                    // receive messages
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("Login:")){
                            // 3 cases: 
                            // 0: Successful login
                            // 1: Successful registration
                            // 2: Unsuccessful login
                            String[] parts = message.split(":", 2);
                            int loginCode = Integer.parseInt(parts[1]);
                            if (loginCode == 0){
                                authenticated = true;
                                // messageLabel.setText("user authenticated");
                                System.out.println("User Authenticated");
                                return;
                            } else if (loginCode == 1){
                                messageLabel.setText("successfully registered new user");
                                registerUser = true; 
                                // appendToChatArea("Sucessfully Registered New User. Please login again", true);
                                try {
                                    Thread.sleep(2000); // Sleep for 1 second
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                socket.close();
                                return;
                                //System.exit(0);
                            } else if (loginCode == 2){
                                messageLabel.setText("Incorrect Password. Please login again");
                                // appendToChatArea("Incorrect Password. Please login again", true);
                                socket.close();
                                return;
                                // System.exit(0);
                            }
                        } 
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Error with Server Connections!");
            e.printStackTrace();
        }

        return true;
        

    }

    // public Boolean registerinDB(String clientUsername, String clientpassword){ 
        
    //     return true; 
    // }

    private SSLSocket connectToServer(){
        try{
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream("client.truststore"), "AlamoStaffedDerivative".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket("localhost", 12345);
            return socket;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException |
                CertificateException | KeyManagementException e) {
            System.out.println("Error with Server Connections!");
            e.printStackTrace();
            return null;
        }

    }

    public String getUser(){ 
        return username; 
    }

    public Boolean getSuccess(){ 
        return success;
    }
    
    public static void main(String[] args){
        new LoginGUI();
    }
}
