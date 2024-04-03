import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Map;

import javax.crypto.*;
import javax.crypto.spec.*;

public class ClientStorage {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String SECRET_KEY_SPEC = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256; // In bits
    private static final byte[] INITIALIZATION_VECTOR = new byte[16]; // Initialization vector (IV)

    public static void saveConversations(Map<String, ArrayList<String>> conversations, String filename, SecretKey secretKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // Serialize the conversations HashMap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(conversations);
        byte[] serializedData = baos.toByteArray();
        
        // Encrypt the serialized data
        Cipher cipher = Cipher.getInstance(SECRET_KEY_SPEC);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(INITIALIZATION_VECTOR));
        byte[] encryptedData = cipher.doFinal(serializedData);

        // Write the encrypted data to a file
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(encryptedData);
        fos.close();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ArrayList<String>> loadConversations(String filename, SecretKey secretKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, ClassNotFoundException, IllegalBlockSizeException, BadPaddingException {
        // read encrypted file daya
        FileInputStream fis = new FileInputStream(filename);
        byte[] encryptedData = fis.readAllBytes();
        fis.close();

        // decrypt
        Cipher cipher = Cipher.getInstance(SECRET_KEY_SPEC);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(INITIALIZATION_VECTOR));
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // deserialize decrypted data into a HashMap
        ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Map<String, ArrayList<String>>) ois.readObject();
    }
}
