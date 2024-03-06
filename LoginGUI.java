import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private static HashMap<String, String> registeredUsers;
    private LoginDBManager databaseManager;
    private static Boolean success = false; 

    public LoginGUI() {
        // Initialize the dictionary
        // registeredUsers = new HashMap<>();
        // Add some dummy users (you can replace these with your actual users)
        // registeredUsers.put("user1", "password1");
        // registeredUsers.put("user2", "password2");

        setTitle("Login Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

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
        inputPanel.add(loginButton);
        inputPanel.add(registerButton);

        panel.add(inputPanel, BorderLayout.CENTER);

        messageLabel = new JLabel("");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.SOUTH);

        databaseManager = new LoginDBManager();

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Check if the username exists in the dictionary and if the password matches
                //registeredUsers.containsKey(username) && registeredUsers.get(username).equals(password)
                if (databaseManager.checkCredentials(username, password)) {
                    messageLabel.setText("Login Successful!");
                    success = true; 
                    // dispose(); // Close the login page
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
    }

    // Inner class for registration dialog
    class RegisterDialog extends JDialog {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public RegisterDialog(JFrame parent) {
            super(parent, "Register New User", true);
            setSize(300, 150);
            setLocationRelativeTo(parent);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1));

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
    public static HashMap<String, String> getUsers(){ 
        return registeredUsers; 
    }

    public static Boolean getSuccess(){ 
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
