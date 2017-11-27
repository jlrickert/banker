package banker;

import java.math.*;

/**
 * Random utility functions such as random numbers and string handling
 */
public class Util {
    /**
     * Generates a random integer between min and mix inclusive.
     *
     * @param min  minimum number that may be returned
     * @param max  maximum number that may be returned
     * @return random number between min and max inclusive
     */
    public static int randomIntRange(int min, int max) {
        return min + (int)(Math.random() * (max - min + 1));
    }

    /**
     * Returns a string representation of an array of integers
     */
    public static String stringify(int[] xs) {
        String str = "[";
        for (int i = 0; i < xs.length; i += 1) {
            str += xs[i];
            if (i < xs.length - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }

    /**
     * Log output to the screen
     */
    public synchronized static void log(String msg) {
        System.out.println(msg);
    }
}
