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
     *
     * @param array  array of integers
     * @return string representation of array
     */
    public static String stringify(int[] array) {
        String str = "[";
        for (int i = 0; i < array.length; i += 1) {
            str += array[i];
            if (i < array.length - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }

    /**
     * Log output to the screen
     *
     * @param msg  message to be printed to screen
     */
    public synchronized static void log(String msg) {
        System.out.println(msg);
    }
}
