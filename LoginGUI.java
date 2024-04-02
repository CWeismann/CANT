import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.Timer;


public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private LoginDBManager databaseManager;
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

        databaseManager = new LoginDBManager();

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                password = new String(passwordField.getPassword());

                // Check if the username exists in the dictionary and if the password matches
                if (databaseManager.checkCredentials(username, password)) {
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

                    // Here you can add code to register the new user
                    databaseManager.addLoginCredentials(username, password, 70); // default salt is 70
                    // For now, let's just print them to console
                    System.out.println("New Username: " + username);
                    System.out.println("New Password: " + password);

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
    public String getUser(){ 
        return username; 
    }

    public Boolean getSuccess(){ 
        return success;
    }
    // Delete this
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginGUI();
        }
        });
    }
}
