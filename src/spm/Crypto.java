package spm;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {
    private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Encrypts a password using a given key.
     * @param key The encryption key.
     * @return The encrypted password as a Base64 encoded string.
     */
    public static String encryptPassword(byte[] key) {
        byte[] randomBytes = generateRandomBytes(16);
        byte[] data = new byte[randomBytes.length * 2];
        System.arraycopy(randomBytes, 0, data, 0, randomBytes.length);
        System.arraycopy(randomBytes, 0, data, randomBytes.length, randomBytes.length);
        byte[] iv = generateRandomBytes(16);
        byte[] encryptedData = encryptData(key, iv, data);
        return base64Encode(iv) + "," + base64Encode(encryptedData);
    }

    /**
     * Checks if a given password matches the encrypted hash.
     * @param key The encryption key.
     * @param hash The encrypted password hash.
     * @return True if the password matches, false otherwise.
     */
    public static boolean checkPassword(byte[] key, String hash) {
        String[] hashParts = hash.split(",");
        byte[] iv = base64Decode(hashParts[0]);
        byte[] encryptedData = base64Decode(hashParts[1]);
        try {
            byte[] decryptedData = decryptData(key, iv, encryptedData);
            return Arrays.equals(
                Arrays.copyOfRange(decryptedData, 0, decryptedData.length / 2),
                Arrays.copyOfRange(decryptedData, decryptedData.length / 2, decryptedData.length)
            );
        } catch (Exception e) {
            // Check failed, return false, nothing to handle
        }
        return false;
    }

    /**
     * Encrypts a string using a given key.
     * @param key The encryption key.
     * @param plaintext The string to encrypt.
     * @return The encrypted string as a Base64 encoded string.
     */
    public static String encryptString(byte[] key, String plaintext) {
        byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] iv = generateRandomBytes(16);
        byte[] encryptedData = encryptData(key, iv, data);
        return base64Encode(iv) + "," + base64Encode(encryptedData);
    }

    /**
     * Decrypts a string using a given key.
     * @param key The encryption key.
     * @param encryptedString The encrypted string to decrypt.
     * @return The decrypted string.
     */
    public static String decryptString(byte[] key, String encryptedString) {
        String[] encryptedParts = encryptedString.split(",");
        byte[] iv = base64Decode(encryptedParts[0]);
        byte[] encryptedData = base64Decode(encryptedParts[1]);
        byte[] decryptedData = decryptData(key, iv, encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Generates an MD5 hash from a character array.
     * @param input The character array to hash.
     * @return The MD5 hash as a byte array.
     */
    public static byte[] generateMd5Hash(char[] input) {
        byte[] byteArray = charArrayToByteArray(input);
        byte[] hash = generateMd5Hash(byteArray);
        Arrays.fill(byteArray, (byte) 0); // Clear sensitive data
        return hash;
    }

    /**
     * Generates a random password of length 16.
     * @return The generated password as a character array.
     */
    public static char[] generatePassword() {
        return generatePassword(16);
    }

    /**
     * Generates a random password of a given length.
     * @param length The length of the password.
     * @return The generated password as a character array.
     */
    public static char[] generatePassword(int length) {
        char[] password = new char[length];
        for (int i = 0; i < length; ++i) {
            password[i] = (char) (RANDOM.nextInt(94) + 33);
        }
        return password;
    }

    // AES encryption/decryption
    private static byte[] performAesOperation(int mode, byte[] secretKey, byte[] iv, byte[] data) {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(mode, keySpec, paramSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] encryptData(byte[] secretKey, byte[] iv, byte[] data) {
        return performAesOperation(Cipher.ENCRYPT_MODE, secretKey, iv, data);
    }

    private static byte[] decryptData(byte[] secretKey, byte[] iv, byte[] data) {
        return performAesOperation(Cipher.DECRYPT_MODE, secretKey, iv, data);
    }

    // Random byte generator
    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    // MD5 hash
    private static byte[] generateMd5Hash(byte[] input) {
        try {
            return MessageDigest.getInstance("MD5").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Convert char array to byte array
    private static byte[] charArrayToByteArray(char[] chars) {
        byte[] bytes = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; ++i) {
            bytes[i * 2] = (byte) (chars[i] >> 8);
            bytes[i * 2 + 1] = (byte) chars[i];
        }
        return bytes;
    }

    // Convert byte array to char array
    private static char[] byteArrayToCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = (char) ((bytes[i * 2] << 8) + (bytes[i * 2 + 1] & 0xFF));
        }
        return chars;
    }

    // Base64 encoding
    private static String base64Encode(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    // Base64 decoding
    private static byte[] base64Decode(String input) {
        return Base64.getDecoder().decode(input);
    }
}
