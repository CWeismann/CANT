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

class MessageDBManager {

    private int numMessages;
    // private final hash_function = hash function

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
            ps.setString(5, msg);
            ps.setInt(6,1);

            // Execute update to the db
            ps.executeUpdate();
            ++numMessages;
        } catch (SQLException e) {
            System.out.println("Failed to insert message data: " + e.getMessage());
        }
    }

} 
