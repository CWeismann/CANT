// SQL database:
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;

// Timestamp
import java.time.LocalDateTime;

// Hashing
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class MessageDBManager {

    private int numMessages;

    public MessageDBManager(){

        this.numMessages = 0;
        // Create new files
        

        File messagesdb  = new File("messages.db");
        try{
            boolean created = messagesdb.createNewFile();
            if (!created){ // file already existed
                messagesdb.delete();
                messagesdb.createNewFile();
            }
        } catch (IOException e){
            System.err.println("Error creating messages db: " + e.getMessage());
        }

        // Create tables in the files 
        try (
            Connection messages_conn = DriverManager.getConnection("jdbc:sqlite:messages.db");
            Statement messages_statement = messages_conn.createStatement();
        ){
            messages_statement.executeUpdate("CREATE TABLE messages (id integer, sender string, recipient string, timestamp string, msg String, visible integer)");
            messages_conn.close();
        } catch (SQLException e){
            e.printStackTrace(System.err);
        }

    }
    
    /**
     * Adds a message to the messag DB
     */
    public void addToMessageDB(String sender, String recipient, String msg){
        String sql = "INSERT INTO messages (id, sender, recipient, timestamp, msg, visible) VALUES (?,?,?,?,?,?)";
        try  (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:messages.db");
            PreparedStatement ps = connection.prepareStatement(sql);
        ) {
            String timestamp = LocalDateTime.now().toString();
            ps.setInt(1, numMessages);
            ps.setString(2, sender);
            ps.setString(3, recipient);
            ps.setString(4, timestamp);
            ps.setString(5, hashMessage(msg));
            ps.setInt(6,1);

            // Execute update to the db
            ps.executeUpdate();
            ++numMessages;
        } catch (SQLException e) {
            System.out.println("Failed to insert message data: " + e.getMessage());
        }
    }

    /**
     * Hashes a message to hide the contents. 
     */
    private String hashMessage(String msg){
    try {
            // Create MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Perform the hash computation
            byte[] hashBytes = digest.digest(msg.getBytes());
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String hash = hexString.toString();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
} 
