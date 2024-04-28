// SQL database:
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;

import java.security.MessageDigest; // sha256
import java.security.NoSuchAlgorithmException;
import java.util.Random; // salting

// Timestamp
import java.time.LocalDateTime;

class LoginDBManager{
    private int numLogins;
    private Random rng;

    public LoginDBManager() {
        this.numLogins = 0;
        File logindb  = new File("login.db");
        this.rng = new Random(System.currentTimeMillis());
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

    /**
     * Returns boolean if the login was a success or not
     * TODO: Fix code duplication in registeruser and registeredUsername
     */
    public boolean registerUser(String username, String password){
        
        // Check if username taken
        if (registeredUsername(username)){
            return false;
        }

        // Register new user
        String sql = "INSERT INTO login (id, username, password, salt, timestamp, valid) VALUES (?,?,?,?,?,?)";
        try  (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:login.db");
            PreparedStatement ps = connection.prepareStatement(sql);
            Statement stmt = connection.createStatement();

        ) {
            String timestamp = LocalDateTime.now().toString();
            int salt = this.rng.nextInt(2147483647);
            String hashedPw = hashPassword(password, salt);
            ps.setInt(1, numLogins);
            ps.setString(2, username);
            ps.setString(3, hashedPw);
            ps.setInt(4, salt);
            ps.setString(5, timestamp);
            ps.setInt(6,1);

            // Execute update to the db
            ps.executeUpdate();
            ++numLogins;
        } catch (SQLException e) {
            System.out.println("Failed to insert login info: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean registeredUsername(String usr){
        String sql = "SELECT COUNT(1) AS count FROM login WHERE username = ?";
        try  (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:login.db");
            PreparedStatement ps = connection.prepareStatement(sql);
        ) {
            ps.setString(1, usr);

            ResultSet countSet = ps.executeQuery();

            int numAppearances = 0;
            if (countSet.next()){
                numAppearances = countSet.getInt("count");
            }
            return (numAppearances > 0);
        
        } catch (SQLException e) {
            System.out.println("Failed to check if user exists: " + e.getMessage());
            return false;
        }
    }


    public boolean checkCredentials(String username, String password){
        /**
         * Checks if the username and pw are correct
         */
        String sql = "SELECT * FROM login WHERE username='" + username +"'";
        try  (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:login.db");
            Statement stmt = connection.createStatement();
        ) {

            // Execute update to the db
            ResultSet loginData = stmt.executeQuery(sql);
            if (loginData.next()){
                String dbUsername = loginData.getString("username");
                String hashedSaltedPw = loginData.getString("password");
                int salt = loginData.getInt("salt");
                String hashedUserPw = hashPassword(password, salt);
                connection.close();
                return hashedSaltedPw.equals(hashedUserPw); // check if pw is correct
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Failed to select login info: " + e.getMessage());
            return false;
        }
    
    }


    public String hashPassword(String password, int salt ){
        try {
            // Create MessageDigest instance for SHA-256
            String inputStr = password + Integer.toString(salt);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Perform the hash computation
            byte[] hashBytes = digest.digest(inputStr.getBytes());
            
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
