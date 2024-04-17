import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CantClient extends JFrame implements ActionListener {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton clearButton;
    private PrintWriter out;
    private JComboBox<String> clientDropdown;
    private String clientID;
    private Map<String, ArrayList<String>> conversations; // Map to store messages for each conversation
    private String currentRecipient; // Currently selected recipient
    private boolean authenticated;
    private String clientName; 
    private String clientpw;
    private boolean registeredUser = false;
    private SSLSocket socket;
    private JLabel messageLabel; 


     public CantClient() throws IOException{ //String username, String password, boolean newUser) {
        
        socket = connectToServer();
        JPanel s = loginGUI();
        
        // socket.close();
            
        // ClientName = loginScreen.getUser();
        
    }

    public void clientGUI(){ 


        clientID = generateClientId();
        setTitle("CANT Client: " + clientName);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        conversations = new HashMap<>();
        currentRecipient = null;

        JPanel panel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());

        inputField = new JTextField();
        inputField.addActionListener(this);
        inputPanel.add(inputField, BorderLayout.CENTER);

        clientDropdown = new JComboBox<>();
        inputPanel.add(clientDropdown, BorderLayout.WEST);

        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        inputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        clearButton = new JButton("Clear History");
        clearButton.addActionListener(this);
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.add(clearButton);
        panel.add(topRightPanel, BorderLayout.NORTH);

        add(panel);

        clientDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
                String selectedRecipient = (String) comboBox.getSelectedItem();
                handleDropdownSelectionChange(selectedRecipient);
            }
        });
        // authenticated = false;
        // authenticated = true; //temporary
        // clientName = username;
        // clientpw = password;
        // registerUser = newUser;

        loadConversationsFromFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveConversationsToFile();
        }));
        
        setVisible(true); 
        // loginScreen = new LoginGUI();
        // startLogin();
    }


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

    public JPanel loginGUI(){ 
        
        setTitle("Login Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

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

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                clientName = usernameField.getText();
                clientpw = new String(passwordField.getPassword());
                if (socket.isClosed()){
                    socket = connectToServer();
                }
                startLogin(false);
                
                if (authenticated){
                    dispose(); 

                    System.out.println("starting client");
                    clientGUI(); 
                    // SwingUtilities.invokeLater(() -> {                
                    startClient();
                }
                // dispose(); 

            }

        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clientName  = usernameField.getText();
                clientpw = new String(passwordField.getPassword());
                startLogin(true);

                System.out.println("New Username: " + clientName);
                System.out.println("New Password: " + clientpw);

                // Close the dialog
                // dispose();
            }
            });

        add(panel);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
        // return success;
        return panel;
    }

    private void startLogin(Boolean reg){
       
        try {   
            out = new PrintWriter(socket.getOutputStream(), true);
            if (reg){
                out.println(clientName + ":" + clientpw + ":register");
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
                            switch(loginCode){
                                case 0:
                                    authenticated = true;
                                    messageLabel.setText("User Authenticated");
                                    // setVisible(false); 
                                    //dispose(); 
                                    //.setVisible(false);
                                    // socket.close(); 
                                    return;
                                case 1:
                                    messageLabel.setText("Sucessfully Registered New User. Please login again");
                                    registeredUser = true; 
                                    socket.close();
                                    return;

                                    // System.exit(0);    
                                case 2:  
                                    messageLabel.setText("Incorrect Password. Please login again");
                                    socket.close();
                                    // System.exit(0);  
                                    return;
                                case 3:
                                    messageLabel.setText("Registration Error. Username already taken");
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
    }


    private void startClient() {
        // Read incoming messages in a separate thread to avoid blocking the EDT
        new Thread(() -> {
            try {
                // receive messages
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("CLIENT_LIST:")) {
                        updateClientDropdown(message.substring("CLIENT_LIST:".length()).split(","));
                    } else {
                        String sender = message.split(": ",2)[0];
                        if (!conversations.containsKey(sender))
                            conversations.put(sender, new ArrayList<String>());
                        conversations.get(sender).add(message);
                        if (sender.equals(currentRecipient))
                            appendToChatArea(message, false);
                    }
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        // } catch (IOException e){
        //     e.printStackTrace();
        // }

    }

    

    public void actionPerformed(ActionEvent e) {
        // send messages
        if (authenticated){
            if (e.getSource() == sendButton || e.getSource() == inputField) {
                String recipient = (String) clientDropdown.getSelectedItem();
                if (recipient != null) {
                    String message = inputField.getText();
                    if (!message.isEmpty()) {
                        if (!conversations.containsKey(recipient))
                            conversations.put(recipient, new ArrayList<String>());
                        conversations.get(recipient).add("You: " + message);
                        out.println(" :" + recipient + ":" + clientName + ":" + message); // Send message with recipient's ID
                        // add timestamp to front eventually?
                        appendToChatArea(message, true); // Mark the message as sent
                        inputField.setText("");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No recipient selected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getSource() == clearButton) {
                clearConversationHistory();
            }
        } else {
            appendToChatArea("User must be authenticated before sending messages!", true);
        }
        
    }

    public void updateClientDropdown(String[] clients) {
        SwingUtilities.invokeLater(() -> {
            clientDropdown.removeAllItems();
            for (String client : clients) {
                clientDropdown.addItem(client);
            }
        });        
    }

    private void appendToChatArea(String message, boolean sent) {
        SwingUtilities.invokeLater(() -> {
            if (sent) {
                chatArea.append("You: " + message + "\n");
            } else {
                chatArea.append(message + "\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void handleDropdownSelectionChange(String selectedRecipient) {
        // Clear chat area
        chatArea.setText("");

        // Update current recipient
        currentRecipient = selectedRecipient;

        // Display messages for the selected conversation
        ArrayList<String> messages = conversations.getOrDefault(selectedRecipient, new ArrayList<>());
        for (String message : messages) {
            appendToChatArea(message, false);
        }
    }
    
    public static void main(String[] args) {
        // if (args.length == 2){
        //     // username and password
        //     SwingUtilities.invokeLater(() -> {
        //     CantClient c = new CantClient(args[0], args[1], false);
        // });
        // } else if (args.length == 3){
        SwingUtilities.invokeLater(() -> {
            try {
                CantClient c = new CantClient();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }//args[0], args[1], true);
        });
        // } else { // Invalid Input
        //     System.exit(1);
        // }
        

    }

    private String generateClientId() {
        return "Client" + System.currentTimeMillis(); // Temporary client ID generation
        // return ClientName;
    }
    private String getClientName(){
        return clientName;
    }

    public boolean getAuth(){
        return authenticated;
    }

    public String getClientId() {
        return clientID;
    }

    private void clearConversationHistory() {
        conversations.clear();
        chatArea.setText("");
    }

    private void loadConversationsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(clientName + "_conversations.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line to extract sender and message
                String[] parts = line.split(": ", 2);
                String sender = parts[0];
                String message = parts[1];

                // Add message to sender's conversation
                if (!conversations.containsKey(sender))
                    conversations.put(sender, new ArrayList<String>());
                conversations.get(sender).add(message);
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    // Method to save conversations to a text file
    private void saveConversationsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clientName + "_conversations.txt"))) {
            for (Map.Entry<String, ArrayList<String>> entry : conversations.entrySet()) {
                String sender = entry.getKey();
                for (String message : entry.getValue()) {
                    // Write sender and message to the file
                    writer.write(sender + ": " + message);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDisconnectMessage() {
        System.out.println("disconnecting");
        if (socket != null && !socket.isClosed()) {
            try {
                out.println("DISCONNECT"); // Send a disconnect message to the server
                socket.close(); // Close the socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // @Override
    
    public void dispose1() {
        System.out.println("disposing");
        sendDisconnectMessage(); // Send a disconnect message when the client window is closed
        super.dispose();
    }



}

