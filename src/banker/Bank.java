package banker;

import java.util.*;
import java.util.concurrent.Semaphore;

import banker.errors.*;


public class Bank {
    private class Handler {
        public Handler() {
        }
    }
    public static final int MAX_RESOUCE = 10;
    public static final int MIN_RESOURCE = 1;
    public final int resourceCount;
    public final int customerCount;
    private Semaphore[] resources;
    private int[][] maximum;
    private int[][] allocation;

    public Bank(int resourceCount, int customerCount) {
        this.resourceCount = resourceCount;
        this.customerCount = customerCount;
        this.initResources();
        this.initMaximum();
        this.initAllocation();
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


    private void initResources() {
        this.resources = new Semaphore[resourceCount];
        for (int i = 0; i < this.resourceCount; i += 1) {
            this.resources[i] = new Semaphore(5);
        }
    }

    private void initMaximum() {
        this.maximum = new int[this.customerCount][this.resourceCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            this.maximum[i] = new int[this.resourceCount];
        }
    }

    private void initAllocation() {
        this.allocation = new int[this.customerCount][this.resourceCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            this.allocation[i] = new int[this.resourceCount];
        }
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
    }
}
