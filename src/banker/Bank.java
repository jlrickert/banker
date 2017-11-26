package banker;

import java.util.*;
import java.util.concurrent.*;
import banker.errors.*;


public class Bank {
    public static final int MAX_RESOURCE = 10;
    public static final int MIN_RESOURCE = 1;
    public final int resourceCount;
    public final int customerCount;
    private final Semaphore[] resources;
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

    public boolean request(Customer customer, int[] request) {
        if (this.isSafe(customer, request)) {
            this.addToAllocation(customer.id, request);
            for (int i = 0; i < this.resources.length; i += 1) {
                try {
                    this.resources[i].acquire(request[i]);
                } catch (InterruptedException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }
            this.printResources();
            this.printAllocated();
            System.out.println();
        } else {
            return false;
        }
        return true;
    }

    public void release(Customer customer, int[] request) {
        this.removeFromAllocation(customer.id, request);
        for (int i = 0; i < this.resources.length; i += 1) {
            this.resources[i].release(request[i]);
        }
        this.printAllocated();
    }

    public synchronized boolean isSafe(Customer customer, int[] request) {
        if (true) {
            return true;
        }
        System.out.println("Bank");
        return false;
    }

    private synchronized void addToAllocation(int row, int[] values) {
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

    public synchronized void printAllocated() {
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
