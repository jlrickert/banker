import banker.*;
import banker.errors.*;

class Program {
    private int resourceCount;
    private int threadCount;
    private Matrix matrix;

    public static void main(String args[]) {
        // try {
        //     Program prog = new Program(args);
        //     int customers = 5;
        //     prog.run(customers);
        // } catch (Err1 or Err2 e) {
        //     System.out.println("Error " + e.getMessage());
        //     Program.printHelp()
        //     System.out.exit(0);
        // }

        Program prog = new Program(args);
        int customers = 5;
        prog.run(customers);
    }

    // this could throw some errors specified in banker.errors package
    public Program(String args[]) {
        System.out.println("Hello World");
        this.parseArguments(args);
        matrix = new Matrix(this.resourceCount, this.threadCount);
    }

    public void run(int customers) {
        // for (int i = 0; i < customers; i += 1){
        //     Transaction txn = this.waitForNextTxn(i);
        // };
        // matrix.newTransaction(this.wait)
    }

    // prints the help message to standard output
    public static void printHelp() {
    }

    // maybe throw error on problem
    private void parseArguments(String args[]) {
        this.resourceCount = 0;
        this.threadCount = 0;
    }


    // private Transcation waitForNextTxn(int id) {
    //     Thread.sleep(3);
    //     return Transaction(id, [1, 2, 3, 4 ,5]);
    // }
}
