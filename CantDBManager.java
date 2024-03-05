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

class CantDBManager {

    // private final String loginDBName;// = "login.db";
    // private final String messagesDBName;// = "messages.db";
    private int numLogins;
    private int numMessages;
    // private final hash_function = hash function

    public CantDBManager(){
        // this.loginDBName = "login.db";
        // this.messagesDBName = "messages.db"; // never actually used
        this.numLogins = 0;
        this.numMessages = 0;
        // Create new files
        File logindb  = new File("login.db");
        try{
            boolean created = logindb.createNewFile();
            if (!created){ // file already existed
                logindb.delete();
                logindb.createNewFile();
            }
        } catch (IOException e){
            System.err.println("Error creating login db: " + e.getMessage());
        }

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
            Connection login_conn = DriverManager.getConnection("jdbc:sqlite:login.db");
            Statement login_statement = login_conn.createStatement();
        ){
            login_statement.executeUpdate("create table login (id integer, username string, password string, salt integer, timestamp string, valid integer)");
            login_conn.close();
        } catch (SQLException e){
            e.printStackTrace(System.err);
        }
        // 
        try (
            Connection messages_conn = DriverManager.getConnection("jdbc:sqlite:messages.db");
            Statement messages_statement = messages_conn.createStatement();
        ){
            messages_statement.executeUpdate("create table messages (id integer, sender string, recipient string, timestamp string, msg String, visible integer)");
            messages_conn.close();
        } catch (SQLException e){
            e.printStackTrace(System.err);
        }

    }

    // private class LoginDBEntry {
    //     public int id;
    //     public String username;
    //     public String password;
    //     public int salt;
    //     public String timestamp;// = LocalDateTime.now();
    //     public int valid;

    //     public LoginDBEntry(String username, String password, int salt,int valid){
    //         this.id = numLogins;
    //         this.username = username;
    //         this.password = password;
    //         this.salt = salt;
    //         this.timestamp = LocalDateTime.now().toString();
    //         this.valid = valid;
    //     }
    // }

    // private class MessageDBEntry{
    //     public int id;
    //     public String sender;
    //     public String recipient;
    //     public String timestamp;
    //     public String message; // Encrypted Message
    //     public int visible;

    //     public MessageDBEntry(String sender, String recipient, String message, int visible){
    //         this.id = numMessages;
    //         this.sender = sender;
    //         this.recipient = recipient;
    //         this.message = message;
    //         this.timestamp = LocalDateTime.now().toString();
    //         this.visible = visible;
    //     }
    // }

    public void addLoginCredentials(String username, String password, int salt){
        // LoginDBEntry entry = new loginEntry(username, password, salt, true); // New accts always valid
        // ++numLogins;

        String sql = "INSERT INTO login (id, username, password, salt, timestamp, valid) VALUES (?,?,?,?,?,?)";

        try  (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:login.db");
            PreparedStatement ps = connection.prepareStatement(sql);
        ) {
            String timestamp = LocalDateTime.now().toString();
            ps.setInt(1, numLogins);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setInt(4, salt);
            ps.setString(5, timestamp);
            ps.setInt(6,1);

            // Execute update to the db
            ps.executeUpdate();
            ++numLogins;
        } catch (SQLException e) {
            System.out.println("Failed to insert data: " + e.getMessage());
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
            System.out.println("Failed to insert data: " + e.getMessage());
        }
    }

} 
