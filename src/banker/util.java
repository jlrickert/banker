package banker;

class Util {
    // wrap random into a function for the program
    // adapter pattern
    //
    // randomIntRange(5) should return a number between 0 - 4 or maybe 1 - 5 or 0 - 5
    public static int randomIntRange(int count) {
        return 0;
    }

    // This is helpful for printing arrays to standard out
    // Examples
    // >> stringifyArray<int>([1, 2, 3])
    // "[1, 2, 3]"
    // >> stringifyArray<int>([])
    // "[]"
    // >> stringifyArray<int>([1])
    // "[1]"
    public String stringifyArray(int arr[]) {
        return "[1, 2, 3]";
    }
}
