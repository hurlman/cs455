package cs455.scaling.util;

import java.util.Random;

public class Util {
    private static Random rand = new Random();

    public static final int BUFFER_SIZE = 8192;

    public static int randInt(int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
