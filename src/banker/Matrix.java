package banker;

import java.util.*;

public class Matrix {
    public static final int MIN_RESOURCE = 1;
    public static final int MAX_RESOUCE = 10;
    private int[] resources;
    private int[][] maximum;
    private int[][] allocation;
    // private int[][] need;

    private int resourceCount;
    private int threadCount;

    // private Deque<Transaction> UTxns;       // unspent transactions -
    //                                         // transactions waiting for
    //                                         // available resources
    // private Deque<Transaction> pendingTxns; // active transactions these are the
    //                                         // transactions that are currently
    //                                         // happening

    public Matrix(int resourceCount, int threadCount) {
        // this.pendingTxns = new LinkedList<Transaction>();
        this.resourceCount = resourceCount;
        this.threadCount = threadCount;
        this.resources = this.initResources(resourceCount);
        this.maximum = this.initMaximum(resourceCount, threadCount);
    }

    // public void newTransaction(Transaction txn) {
    //     // this.pendingTxns.push(txn);
    //     // txn.run();
    // }

    private int[] initResources(int resourceCount) {
        return new int[resourceCount];
    }

    private int[][] initMaximum(int resourceCount, int threadCount) {
        return new int[resourceCount][threadCount];
    }
}
