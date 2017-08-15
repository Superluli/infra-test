package com.superluli.infra.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
    
    //AES Spec
    private static final String CIPHER_PROVIDER_AES = "BC";
    private static final String CIPHER_TRANSFORMATION_AES = "AES/CBC/PKCS7Padding";
    private static final String CIPHER_ALGORITHM_AES = "AES";
    
    //RSA Spec 
    private static final String CIPHER_TRANSFORMATION_RSA = "RSA/NONE/PKCS1Padding";
    
    //Secure Random Spec
    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String SECURE_RANDOM_PROVIDER = "SUN";
    
    //Key Derivation Spec
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";

    static {
        //Add BouncyCastleProvider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    private EncryptUtils() {

    }

    /**
     * connect arrays, any one of the input array could be empty array but cannot be null
     * 
     * @param arrays
     * @return result array by connecting input arrays one by one, may be empty
     * @throws IOException
     */
    public static byte[] concatArray(byte[]... arrays) throws IOException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (byte[] array : arrays) {
                out.write(array);
            }
            return out.toByteArray();
        }
    }

    public static byte[] deriveKeyPBKDF2(String password, byte[] salt, int iterations, int length)
            throws Exception {

        SecretKeyFactory skf = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, length);
        return skf.generateSecret(spec).getEncoded();
    }

    public static byte[] hashSha256(byte[] data) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    public static byte[] nextSecureRandomBytes(int length) throws Exception {
        byte[] next = new byte[length];
        SecureRandom sr = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SECURE_RANDOM_PROVIDER);
        sr.nextBytes(next);
        return next;
    }

    public static byte[] encryptAES(byte[] sourceBytes, byte[] aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_AES, CIPHER_PROVIDER_AES);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, CIPHER_ALGORITHM_AES),
                new IvParameterSpec(iv));
        return cipher.doFinal(sourceBytes);
    }

    public static byte[] decryptAES(byte[] encryptedBytes, byte[] aesKey, byte[] iv)
            throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_AES, CIPHER_PROVIDER_AES);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, CIPHER_ALGORITHM_AES),
                new IvParameterSpec(iv));
        return cipher.doFinal(encryptedBytes);
    }

    public static byte[] encryptRSA(byte[] sourceBytes, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(sourceBytes);
    }
    
    public static byte[] decryptRSA(byte[] encryptedBytes, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_RSA);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedBytes);
    }
    
    public static byte[] decodeBase64(String source) {
        return Base64.getDecoder().decode(source);
    }

    public static String encodeBase64(byte[] source) {
        return Base64.getEncoder().encodeToString(source);
    }
}
