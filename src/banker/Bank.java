package banker;

import java.util.*;
import java.util.concurrent.Semaphore;

import banker.errors.*;


public class Bank {
    private class Handler {
        public Handler() {
        }
    }
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
        this.printInitalResources();
        this.printMaximum();
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
            maximum[row] = new int[this.resourceCount];
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

    public void printInitalResources() {
        System.out.println("Bank - Initial Resources Available:");

        String str = "\t[";
        for (int i = 0; i < this.resourceCount; i += 1) {
            int count = this.resources[i].availablePermits();
            str += String.valueOf(count);
            if (i < this.resources.length - 1) {
                str += ", ";
            }
        }
        str += "]";
        System.out.println(str);
    }

    private void printMaximum() {
        System.out.println("Bank - Max");
        for (int row = 0; row < this.customerCount; row += 1) {
            String str = "\t[";
            for (int col = 0; col < this.resourceCount; col += 1) {
                str += this.maximum[row][col];
                if (col < this.resourceCount - 1) {
                    str += ", ";
                }
            }
            str += "]";
            System.out.println(str);
        }
    }

    public void printAllocated() {
        System.out.println("Bank - Allocation");
        for (int row = 0; row < this.customerCount; row += 1) {
            String str = "\t[";
            for (int col = 0; col < this.resourceCount; col += 1) {
                str += this.allocation[row][col];
                if (col < this.resourceCount - 1) {
                    str += ", ";
                }
            }
            str += "]";
            System.out.println(str);
        }
    }
}
