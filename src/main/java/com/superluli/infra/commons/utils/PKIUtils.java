package com.superluli.infra.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.LinkedList;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PKIUtils {

	
    final static String BEGIN = "-----BEGIN CERTIFICATE-----";
    final static String END = "-----END CERTIFICATE-----";
    final static String BEGIN_PRIVATE = "-----BEGIN RSA PRIVATE KEY-----";
    final static String END_PRIVATE = "-----END RSA PRIVATE KEY-----";

    private static final String DEFAULT_CHAR_SET = "UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(PKIUtils.class);

    static {
        // Add BouncyCastleProvider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private PKIUtils() {

    }

    /**
     * Get X509Certificates object from PEM format source string
     * 
     * @param source
     * @return LinkedList of mapped X509Certificate, or empty list of no valid certificate was found
     */
    public static LinkedList<X509Certificate> getCertsFromPem(String source) {
        String delimiter = END + "\n";
        LinkedList<X509Certificate> certList = new LinkedList<X509Certificate>();
        String[] certsPemArr = source.split(delimiter);
        for (String certPem : certsPemArr) {
            if (certPem.contains(BEGIN)) {
                certPem += delimiter;
                try {
                    X509Certificate cert = PKIUtils.getCertFromPem(certPem);
                    certList.add(cert);
                } catch (IllegalArgumentException e) {
                    // Only parse valid cert, ignore failures
                }
            }
        }
        return certList;
    }

    /**
     * Get X509Certificate object from pem format source string
     * 
     * @param source
     * @return certificate generated from the input source
     * @throws IllegalArgumentException
     */
    public static X509Certificate getCertFromPem(String source) throws IllegalArgumentException {
        // DO NOT REMOVE : PEM READER NEED CERTIFICATE IN PROPER FORMAT.
        source =
                source.replaceAll("\n", "").replaceAll(BEGIN, BEGIN + "\n")
                        .replaceAll(END, "\n" + END)
                        .replaceAll(BEGIN_PRIVATE, BEGIN_PRIVATE + "\n")
                        .replaceAll(END_PRIVATE, "\n" + END_PRIVATE);
        byte[] pemBytes;
        try {
            pemBytes = source.getBytes(DEFAULT_CHAR_SET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot get cert from source string : " + source, e);
        }

        X509Certificate cert =
                readFromPem(new ByteArrayInputStream(pemBytes), X509Certificate.class);
        if (cert == null) {
            throw new IllegalArgumentException("Cannot get cert from source string : " + source);
        }
        return cert;
    }

    /**
     * get X509Certificate instance from DER encoded bytes of a certificate
     */
    public static X509Certificate getCertFromDER(byte[] source) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
                    new ByteArrayInputStream(source));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get key pair from source input stream
     * 
     * @param source
     * @return key pair or null if anything wrong
     * @throws IllegalArgumentException
     */
    public static KeyPair getKeyPairFromPem(String source) throws IllegalArgumentException {

        byte[] pemBytes;
        try {
            pemBytes = source.getBytes(DEFAULT_CHAR_SET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(
                    "Cannot get key pair from source string : " + source, e);
        }

        KeyPair kp = readFromPem(new ByteArrayInputStream(pemBytes), KeyPair.class);
        if (kp == null) {
            throw new IllegalArgumentException("Cannot get key pair from input source");
        }
        return kp;
    }

    /**
     * a general function used to read a pem format input source and maps to desired object. other
     * get functions in this class actually use this function and are more convenient, consider
     * using them first
     * 
     * @param source
     * @param clazz , class of desired type
     * @return desired object or null
     */
    public static <T> T readFromPem(InputStream source, Class<T> clazz) {

        PEMReader pr = null;
        try {
            pr = new PEMReader(new InputStreamReader(source, "UTF-8"));
            return clazz.cast(pr.readObject());
        } catch (Exception e) {
            logger.error("Failed at converting pem bytes to " + clazz.getSimpleName(), e);
            return null;
        } finally {
            if (pr != null) {
                try {
                    pr.close();
                } catch (IOException e) {
                    logger.error("Failed at closing resource", e);
                }
            }
        }
    }

    /**
     * write an object to pem format
     * 
     * @param o object to write
     * @return byte array of resulting pem format or null if object cannot be mapped
     */
    public static byte[] asPemBytes(Object o) {
        PEMWriter pw = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            pw = new PEMWriter(new OutputStreamWriter(bout, "UTF-8"));
            pw.writeObject(o);
            pw.flush();

            return bout.toByteArray();
        } catch (Exception e) {
            logger.error("Failed at converting object to pem bytes", e);
            return null;
        } finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (IOException e) {
                    logger.error("Failed at closing resource", e);
                }
            }
        }
    }

    /**
     * generate a new key RSA pair
     * 
     * @return
     * @throws Exception
     */
    public static KeyPair generateRSAKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
        kpGen.initialize(keySize, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    /**
     * Sign source bytes with private key, using SHA256WithRSAandMGF1
     * 
     * @param source
     * @param privateKey
     * @throws IllegalArgumentException
     * @throws RuntimeException
     * @return signature bytes
     */
    public static byte[] sign(byte[] source, PrivateKey privateKey) {
        if (source == null || privateKey == null) {
            throw new IllegalArgumentException("source or privateKey is invalid");
        }
        try {
            Signature signature = Signature.getInstance("SHA256WithRSAandMGF1", "BC");
            signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec(
                    "SHA-256"), 32, 1));
            signature.initSign(privateKey);
            signature.update(source);
            return signature.sign();
        } catch (Exception e) {
            logger.error("Failed at signing", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * verify the signature of source string, using SHA256WithRSAandMGF1
     * 
     * @param source
     * @param signatureString
     * @param publicKey
     * @return result of verification or false if there is anything wrong during the verification
     *         e.g. invalid private key
     */
    public static boolean verify(byte[] source, byte[] signatureBytes, PublicKey publicKey) {

        try {
            Signature signature = Signature.getInstance("SHA256WithRSAandMGF1", "BC");
            signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec(
                    "SHA-256"), 32, 1));
            signature.initVerify(publicKey);
            signature.update(source);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            logger.error("Failed at verifying", e);
            return false;
        }
    }
}
