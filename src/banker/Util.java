package banker;

import java.math.*;

public class Util {
    public static int randomIntRange(int min, int max) {
        return min + (int)(Math.random() * (max - min + 1));
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
