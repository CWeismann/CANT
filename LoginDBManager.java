// SQL database:
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Timestamp
import java.time.LocalDateTime;

class LoginDBManager{
    private int numLogins;

    public LoginDBManager() {
        this.numLogins = 0;
        File logindb  = new File("login.db");
        // Make DB file
        try{
            boolean created = logindb.createNewFile();
            if (!created){ // file already existed
                logindb.delete();
                logindb.createNewFile();
            }
        } catch (IOException e){
            System.err.println("Error creating login db: " + e.getMessage());
        }
        // Create table of login information
        try (
            Connection login_conn = DriverManager.getConnection("jdbc:sqlite:login.db");
            Statement login_statement = login_conn.createStatement();
        ){
            login_statement.executeUpdate("CREATE TABLE login (id integer, username string, password string, salt integer, timestamp string, valid integer)");
            login_conn.close();
        } catch (SQLException e){
            e.printStackTrace(System.err);
        }
    }

    public void addLoginCredentials(String username, String password, int salt){

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
            System.out.println("Failed to insert login info: " + e.getMessage());
        }
    }


    public boolean checkCredentials(String username, String password){
        /**
         * Checks if the username and pw are correct
         */
        String sql = "SELECT * FROM login WHERE username='" + username +"'";
        System.out.println(sql);
        try  (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:login.db");
            Statement stmt = connection.createStatement();
        ) {

            // Execute update to the db
            System.out.println("trying to execute statement");
            ResultSet loginData = stmt.executeQuery(sql);
            System.out.println("executed statement");
            if (loginData.next()){
                // String db_username = loginData.getString("username");
                String db_password = loginData.getString("password");
                System.out.println("found correct pw");

                connection.close();
                return db_password.equals(password); // check if pw is correct
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Failed to select login info: " + e.getMessage());
            return false;
        }
    
    }

    // public static String getUsernameById(int clientId) {
    //     String sql = "SELECT username FROM login WHERE id = ?";
    //     try (
    //         Connection connection = DriverManager.getConnection("jdbc:sqlite:login.db");
    //         PreparedStatement ps = connection.prepareStatement(sql);
    //     ) {
    //         ps.setInt(1, clientId);
    //         ResultSet rs = ps.executeQuery();
    //         if (rs.next()) {
    //             String username = rs.getString("username");
    //             return username;
    //         }
    //     } catch (SQLException e) {
    //         System.out.println("Failed to retrieve username: " + e.getMessage());
    //     }
    //     return "";
    // }

}