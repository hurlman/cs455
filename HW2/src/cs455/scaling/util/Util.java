package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Util {
    private static Random rand = new Random();

    public static final int DATA_SIZE = 1024 * 8;
    public static final int SERVER_BUFFER_SIZE = 1024 * 128;
    public static final int CLIENT_BUFFER_SIZE = 1024;
    public static final int REPORT_INTERVAL = 20;

    public static int randInt(int min, int max) {

        return rand.nextInt((max - min) + 1) + min;
    }

    public static byte[] randBytes(int size){

        byte[] b = new byte[size];
        rand.nextBytes(b);
        return b;
    }

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
}
