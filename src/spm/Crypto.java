/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spm;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author Alex
 */
public class Crypto {
//public
    //password validity check
    static public String encryptPassword(byte[] key) {
        byte[] tmp = rand(16);
        byte[] data = new byte[tmp.length*2];
        System.arraycopy(tmp, 0, data, 0, tmp.length);
        System.arraycopy(tmp, 0, data, tmp.length, tmp.length);
        byte[] iv = rand(16);
        byte[] enc = encode(key, iv, data);
        return b64encode(iv)+","+b64encode(enc);
    }
    static public boolean checkPassword(byte[] key, String hash) {
        String[] h = hash.split(",");
        byte[] iv = b64decode(h[0]);
        byte[] enc = b64decode(h[1]);
        try {
            byte[] dec = decode(key, iv, enc);
            return Arrays.equals(Arrays.copyOfRange(dec, 0, dec.length/2), Arrays.copyOfRange(dec, dec.length/2, dec.length));
        } catch (Exception e) {}
        return false;
    }
    
    //user-friendly encryptor-decryptor
    static public String encode(byte[] key, String str) {
        byte[] data = null;
        try {
            data = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {}
        byte[] iv = rand(16);
        byte[] enc = encode(key, iv, data);
        return b64encode(iv)+","+b64encode(enc);
    }
    static public String decode(byte[] key, String str) {
        String[] h = str.split(",");
        byte[] iv = b64decode(h[0]);
        byte[] enc = b64decode(h[1]);
        byte[] dec = decode(key, iv, enc);
        try {
            return new String(dec, "UTF-8");
        } catch (UnsupportedEncodingException ex) {}
        return null;
    }
    
    //MD5 hash
    static public byte[] md5(char[] str) {
        byte[] p = toByteArray(str);
        byte[] ret = md5(p);
        for(int i = 0; i < p.length; ++i)
            p[i] = 0;
        return ret;
    }  
    
//private
    static private final String cipher_type = "AES/CBC/PKCS5Padding";
    //AES skey, skey,iv - 16byte
    private static byte[] AES(int mode, byte[] skey, byte[] iv, byte[] data) {
        SecretKeySpec key = new SecretKeySpec(skey, "AES");
        AlgorithmParameterSpec param = new IvParameterSpec(iv);
        try {
          Cipher cipher = Cipher.getInstance(cipher_type);
          cipher.init(mode, key, param);
          return cipher.doFinal(data);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
    }
    private static byte[] encode(byte[] skey, byte[] iv, byte[] data) {
      return AES(Cipher.ENCRYPT_MODE, skey, iv, data);
    }
    private static byte[] decode(byte[] skey, byte[] iv, byte[] data) {
      return AES(Cipher.DECRYPT_MODE, skey, iv, data);
    }
    
    //random
    static SecureRandom random = new SecureRandom();
    static private byte[] rand(int n) {
        byte bytes[] = new byte[n];
        random.nextBytes(bytes);
        return bytes;
    }
    
    //md5
    static private byte[] md5(byte[] str) {
       try {
            return MessageDigest.getInstance("MD5").digest(str);
        } catch (java.security.NoSuchAlgorithmException e) {}
        return null;
    }
    static private byte[] toByteArray(char[] chars) {
        byte[] bytes = new byte[chars.length*2];
        for(int i=0; i<chars.length; ++i) {
           bytes[i*2] = (byte) (chars[i] >> 8);
           bytes[i*2+1] = (byte) chars[i];
        }
        return bytes;
    }
    static private char[] toCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length/2];
        for(int i=0; i<chars.length; ++i) 
           chars[i] = (char) ((bytes[i*2] << 8) + (bytes[i*2+1] & 0xFF));
        return chars;
    }
    
    //base64
    static private String b64encode(byte[] str) {
        return Base64.getEncoder().encodeToString(str);
    }
    static private byte[] b64decode(String str) {
        return Base64.getDecoder().decode(str);
    }
}
