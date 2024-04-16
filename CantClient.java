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
    private boolean registerUser;
    private SSLSocket socket;


    public CantClient(String username, String password, boolean newUser) {
        clientID = generateClientId();
        setTitle("CANT Client: " + username);
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
        authenticated = true; //temporary
        clientName = username;
        clientpw = password;
        registerUser = newUser;

        loadConversationsFromFile();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveConversationsToFile();
        }));
        
        setVisible(true);
        // loginScreen = new LoginGUI();
        socket = connectToServer();
        startLogin();
        if(!registerUser)
            startClient();
        // ClientName = loginScreen.getUser();
        
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

    private void startLogin(){
        try {   
            out = new PrintWriter(socket.getOutputStream(), true);
            if (registerUser){
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
                                    System.out.println("User Authenticated");
                                    return;
                                case 1:
                                    System.out.println("Sucessfully Registered New User. Please login again");
                                    socket.close();
                                    System.exit(0);    
                                case 2:  
                                    System.out.println("Incorrect Password. Please login again");
                                    socket.close();
                                    System.exit(0);  
                                case 3:
                                    System.out.println("Registration Error. Username already taken");
                                    socket.close();
                                    System.exit(0);  
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
        // socket = connectToServer();
        // try{
        // out = new PrintWriter(socket.getOutputStream(), true);

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
        if (args.length == 2){
            // username and password
            SwingUtilities.invokeLater(() -> {
            CantClient c = new CantClient(args[0], args[1], false);
        });
        } else if (args.length == 3){
        SwingUtilities.invokeLater(() -> {
            CantClient c = new CantClient(args[0], args[1], true);
        });
        } else { // Invalid Input
            System.exit(1);
        }
        

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

    @Override
    public void dispose() {
        System.out.println("disposing");
        sendDisconnectMessage(); // Send a disconnect message when the client window is closed
        super.dispose();
    }

}
