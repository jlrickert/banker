package banker;

import java.math.*;
import java.util.concurrent.*;

public class Util {
    public static int randomIntRange(int minimum, int maximum) {
        return minimum + (int)(Math.random() * maximum);
    }

    public static void initialize(int[] xs, int val) {
        for (int i = 0; i < xs.length; i += 1) {
            xs[i] = val;
        }
    }

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
}
