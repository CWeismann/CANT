import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Password {
    private String hashedPassword;
    private byte[] salt;
    public Password(String password) {
        salt = generateSalt();
        hashedPassword = hashPassword(password, salt);
    }
    public String getHashedPassword() {
        return hashedPassword;
    }
    public byte[] getSalt() {
        return salt;
    }
    protected static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 bytes = 128 bits
        random.nextBytes(salt);
        return salt;
    }

    protected static String hashPassword(String password, byte[] salt) {
        try {
            // Create SHA-256 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Append salt to the password bytes
            byte[] passwordAndSalt = concatenateByteArrays(password.getBytes(), salt);
            
            // Hash the combined password and salt bytes
            byte[] hashedBytes = digest.digest(passwordAndSalt);
            
            // Convert the hashed bytes to a Base64-encoded string
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}