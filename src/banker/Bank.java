package banker;

import java.util.*;
import java.util.concurrent.*;
import banker.errors.*;


public class Bank {
    public static final int MAX_RESOURCE = 10;
    public static final int MIN_RESOURCE = 1;
    public final int resourceCount;
    public final int customerCount;
    public final Semaphore[] resources;
    private final int[][] maximum;
    private int[][] allocation;

    private boolean isOpen;
    private TxHandler handler;
    private Thread handlerThread;

    public Bank(TxHandler handler, int resourceCount, int customerCount) {
        this.resourceCount = resourceCount;
        this.customerCount = customerCount;
        this.resources = this.initResources();
        this.maximum = this.initMaximum();
        this.allocation = this.initAllocation();

        this.handler = initHandler(handler);

        this.printInital();
        this.handler = handler;
        this.isOpen = false;
    }

    private TxHandler initHandler(TxHandler handler) {
        handler.setResources(this.resources);
        return handler;
    }

    public int resourceLength() {
        return this.resources.length;
    }

    public int[] getMaximum(int i) {
        return this.maximum[i];
    }

    public void printInital() {
        System.out.println("Bank - Initial Resources Available:");
        this.handler.printResources();
        this.printMaximum();
        System.out.println();
    }

    public synchronized boolean request(Transaction trans) {
        if (this.isSafe(trans)) {
            this.handler.add(trans);
            this.addToAllocationMatrix(trans.owner.id, trans.request);
            {  // print request granted
                String str = "Customer ";
                str += String.valueOf(trans.owner.id);
                str += " request ";
                str += String.valueOf(trans.id);
                str += " granted";
                System.out.println(str);
            }
            this.printAllocationMatrix();
        } else {
            System.out.println("Bank resources unavailable");
            return false;
        }
        return true;
    }

    public void release(Transaction trans) {
        this.removeFromAllocation(trans.owner.id, trans.request);
        this.printAllocationMatrix();
        this.handler.handleRelease(trans);
    }

    public boolean isSafe(Transaction trans) {
        boolean safe = true;

        // check if there currently are enough resources
        for (int r = 0; r < this.resourceCount; r += 1) {
            int resource = this.resources[r].availablePermits();
            if (trans.request[r] > resource) {
                safe = false;
                break;
            }
        }

        if (!safe) {
            for (int id = 0; id < this.customerCount; id += 1) {
                safe = true;
                for (int r = 0; r < this.resourceCount; r += 1) {
                    int resource = this.resources[r].availablePermits();
                    int available = resource + this.allocation[id][r];
                    if (trans.request[r] > available) {
                        safe = false;
                        break;
                    }
                }
                if (safe) {
                    return true;
                }
            }
        }

        return safe;
    }

    public void close() {
        System.out.println("Closing bank");
        this.isOpen = false;
        this.handler.stop();
    }

    public void start() {
        System.out.println("Bank is now accepting requests");
        this.isOpen = true;

        this.handlerThread = new Thread(this.handler);
        this.handlerThread.start();
    }

    private synchronized void addToAllocationMatrix(int row, int[] values) {
        for (int i = 0; i < this.resourceCount; i += 1) {
            this.allocation[row][i] += values[i];
        }
    }

    private synchronized void removeFromAllocation(int row, int[] values) {
        for (int i = 0; i < this.resourceCount; i += 1) {
            this.allocation[row][i] -= values[i];
        }
    }

    private Semaphore[] initResources() {
        Semaphore[] resources = new Semaphore[resourceCount];
        for (int i = 0; i < this.resourceCount; i += 1) {
            int n = Util.randomIntRange(this.MIN_RESOURCE, this.MAX_RESOURCE);
            resources[i] = new Semaphore(n);
        }
        return resources;
    }

    private int[][] initMaximum() {
        int[][] maximum = new int[this.customerCount][this.resourceCount];
        for (int row = 0; row < this.customerCount; row += 1) {
            for (int col = 0; col < this.resourceCount; col += 1) {
                int resourceCount = this.resources[col].availablePermits();
                int n = Util.randomIntRange(this.MIN_RESOURCE, resourceCount);
                maximum[row][col] = n;
            }
        }
        return maximum;
    }

    private int[][] initAllocation() {
        int[][] allocation = new int[this.customerCount][this.resourceCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            allocation[i] = new int[this.resourceCount];
        }
        return allocation;
    }

    public synchronized void printMaximum() {
        System.out.println("Bank - Max");
        for (int row = 0; row < this.customerCount; row += 1) {
            String str = "\t";
            str += Util.stringify(this.maximum[row]);
            System.out.println(str);
        }
    }

    public synchronized void printAllocationMatrix() {
        System.out.println("Bank - Allocation: ");
        for (int row = 0; row < this.customerCount; row += 1) {
            String str = "\t";
            str += Util.stringify(this.allocation[row]);
            System.out.println(str);
        }
        System.out.println();
    }
}
