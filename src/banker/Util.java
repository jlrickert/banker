package banker;

import java.math.*;

public class Util {
    public static int randomIntRange(int minimum, int maximum) {
        return minimum + (int)(Math.random() * maximum);
    }
}
