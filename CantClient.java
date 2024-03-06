import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class CantClient extends JFrame implements ActionListener {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private JComboBox<String> clientDropdown;
    private List<String> availableClients;
    private static LoginGUI loginScreen;
    private String ClientName; 


    public CantClient() {
        setTitle("TLS Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        add(panel);
        
        setVisible(true);
        // loginScreen = new LoginGUI();
        startClient();
        // ClientName = loginScreen.getUser();
        
        
    }

    private void startClient() {
        try {

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream("client.truststore"), "AlamoStaffedDerivative".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket("localhost", 12345);

            out = new PrintWriter(socket.getOutputStream(), true);

            // Read incoming messages in a separate thread to avoid blocking the EDT
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("CLIENT_LIST:")) {
                            updateClientDropdown(message.substring("CLIENT_LIST:".length()).split(","));
                        } else {
                            appendToChatArea(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException |
                CertificateException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton || e.getSource() == inputField) {
            String recipient = (String) clientDropdown.getSelectedItem();
            if (recipient != null) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    out.println(recipient + ":" + message); // Send message with recipient's ID
                    inputField.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No recipient selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateClientDropdown(String[] clients) {
        clientDropdown.removeAllItems();
        for (String client : clients) {
            // if (!client.equals(getClientId())) { // Exclude self from the list
            //     clientDropdown.addItem(client);
            // }
            if (!client.equals(getClientId())) { // Exclude self from the list
                clientDropdown.addItem(client);
             }
        }
    }

    private void appendToChatArea(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
                LoginGUI loginScreen = new LoginGUI();
                // new CantClient();
            
        });
    }

    private String getClientId() {
        return "Client" + System.currentTimeMillis(); // Temporary client ID generation
        // return ClientName;
    }
    private String getClientName(){
        return ClientName;
    }
}
