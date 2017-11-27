package banker;

class Logger {
    public synchronized static void log(String msg) {
        System.out.println(msg);
    }
    private String module;
    public static boolean debug = false;
    public Logger(String module) {
        this.module = module;
    }

    public synchronized void info(String msg) {
        System.out.println(msg);
    }

    public synchronized void debug(String msg) {
        if (Logger.debug) {
            System.out.println("INFO:" + module + ":" + msg);
        }
    }

    public synchronized void error(String msg) {
        System.out.println("INFO:" + module + ":" + msg);
    }

    public synchronized void warning(String msg) {
        System.out.println("INFO:" + module + ":" + msg);
    }
}
