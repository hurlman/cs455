package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Utility functions and constants.
 */
public class Util {
    private static Random rand = new Random();

    public static final int DATA_SIZE = 1024 * 8;
    public static final int SERVER_BUFFER_SIZE = 1024 * 64;
    public static final int CLIENT_BUFFER_SIZE = 1024;
    public static final int REPORT_INTERVAL = 20;

    /**
     * Generates random data for client to send.
     */
    public static byte[] randBytes(int size) {

        byte[] b = new byte[size];
        rand.nextBytes(b);
        return b;
    }

    /**
     * Computes SHA1 hash and returns representation as hex string.
     */
    public static String SHA1FromBytes(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);

        return hashInt.toString(16);
    }

    /**
     * Computes SHA1 hash and returns as byte array.
     */
    public static byte[] getSHA1(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest.digest(data);
    }
}
