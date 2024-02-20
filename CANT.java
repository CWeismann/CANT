// Swing GUI written by ChatGPT

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CANT extends JFrame implements ActionListener {
    private JTextField inputField;
    private JTextArea chatArea;
    private JButton sendButton;

    public CANT() {
        ImageIcon icon = new ImageIcon("icon.png");
        setIconImage(icon.getImage());

        setTitle("CANT");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        inputField = new JTextField();
        inputField.addActionListener(this);
        inputPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        inputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton || e.getSource() == inputField) {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                appendToChatArea("You: " + message);
                inputField.setText("");
            }
        }
    }

    private void appendToChatArea(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CANT();
            }
        });
    }
}
