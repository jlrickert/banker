package banker;
import banker.core.*;
import banker.errors.*;

class Program {
    private int resouceCount;
    private int threadCount;

    public static void main(String args[]) {
        Program prog = new Program();
        prog.run(args);
    }

    public Program() {}

    public void run(String args[]) {
        System.out.println("Hello World");
        if (!this.parseArguments(args)) {
            this.printHelp();
            System.exit(1);
        }
        BankerMatrix matrix = new BankerMatrix();
    }

    private boolean parseArguments(String args[]) {
        return true;
    }

    private void printHelp() {
    }
}
