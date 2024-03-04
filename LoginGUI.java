import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginGUI extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private String password;
    private String username;
    
    private Boolean loginSuccess = false;

    public LoginGUI() {
        setTitle("Messaging App Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        
        JLabel usernameLabel = new JLabel("Username:");
        panel.add(usernameLabel);
        
        usernameField = new JTextField();
        panel.add(usernameField);
        
        JLabel passwordLabel = new JLabel("Password:");
        panel.add(passwordLabel);
        
        passwordField = new JPasswordField();
        panel.add(passwordField);
        
        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        panel.add(loginButton);
        
        add(panel);
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            username = usernameField.getText();
            password = new String(passwordField.getPassword());
            loginSuccess = true;

            // JOptionPane.showMessageDialog(this, "Username: " + username + "\nPassword: " + password);
        }
    }

    protected boolean isLoginButtonPressed() {
        return loginSuccess;
    }
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginGUI login = new LoginGUI();
                
                while (!login.isLoginButtonPressed()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // Retrieve the username and password
                String username = login.getUsername();
                String password = login.getPassword();
                System.out.print(username + " " + password);

            }
        });
    }

}
