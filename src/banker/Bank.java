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

    public Bank(int resourceCount, int customerCount) {
        this.resourceCount = resourceCount;
        this.customerCount = customerCount;
        this.resources = this.initResources();
        this.maximum = this.initMaximum();
        this.allocation = this.initAllocation();
        this.printInital();
    }

    public int resourceLength() {
        return this.resources.length;
    }

    public int[] getMaximum(int i) {
        return this.maximum[i];
    }

    public void printInital() {
        System.out.println("Bank - Initial Resources Available:");
        this.printResources();
        this.printMaximum();
        System.out.println();
    }

    public boolean request(Customer customer, int id, int[] request) {
        if(!this.isSafe(request)) {
            return false;
        }

        this.addToAllocationMatrix(customer.id, request);
        {  // print request granted
            String str = "Customer ";
            str += String.valueOf(customer.id);
            str += " request ";
            str += String.valueOf(id);
            str += " granted";
            System.out.println(str);
        }
        this.printAllocationMatrix();

        // this.allocateResources(request);
        this.printResources();
        return true;
    }

    public void release(Customer customer, int id, int[] request) {
        this.removeFromAllocation(customer.id, request);
        this.printAllocationMatrix();
    }

    public synchronized boolean isSafe(int[] request) {
        return true;
        // boolean[] running = new boolean[this.customerCount];
        // for (int i = 0; i , this.customerCount; i += 1) {
        //     running[i] = true;
        // }

        // while (this.runningCount(running) > 0) {
        //     boolean atLeastOneAllocated = false;
        //     for (int customer = 0; customer < this.customerCount; customer += 1) {
        //         if (running[customer]) {
        //             ok;
        //         }
        //     }
        // }


        // boolean safe = true;

        // // check if there currently are enough resources
        // for (int r = 0; r < this.resourceCount; r += 1) {
        //     int resource = this.resources[r].availablePermits();
        //     if (request[r] > resource) {
        //         safe = false;
        //         break;
        //     }
        // }
        // if (safe) {
        //     return true;
        // }

        // if (!safe) {
        //     for (int id = 0; id < this.customerCount; id += 1) {
        //         safe = true;
        //         for (int r = 0; r < this.resourceCount; r += 1) {
        //             int resource = this.resources[r].availablePermits();
        //             int available = resource + this.maximum[id][r];
        //             if (request[r] > available) {
        //                 safe = false;
        //                 break;
        //             }
        //         }
        //         if (safe) {
        //             return true;
        //         }
        //     }
        // }

        // return safe;
    }

    private int runningCount(boolean[] running) {
        int count = 0;
        for (int i = 0; i < running.length; i += 1) {
            count += 1;
        }
        return count;
    }

    private void allocateResources(int[] request) {
        for (int i = 0; i < this.resources.length; i += 1) {
            try {
                this.resources[i].acquire(request[i]);
            } catch (InterruptedException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
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

    public synchronized void printResources() {
        String str = "Available Resources: ";
        str += this.stringifiedResources();
        System.out.println(str);
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

    private String stringifiedResources() {
        String str = "[";
        for (int col = 0; col < this.resourceCount; col += 1) {
            int count = this.resources[col].availablePermits();
            str += String.valueOf(count);
            if (col < this.resources.length - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }

}
