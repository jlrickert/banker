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
    private final int[] maxResources;
    private final int[][] maximum;
    private int[][] allocation;
    private int[][] need;

    public Bank(int customerCount, int resourceCount) {
        this.customerCount = customerCount;
        this.resourceCount = resourceCount;
        this.maxResources = this.initMaxResources(resourceCount);
        this.resources = this.initResources(this.maxResources, resourceCount);
        this.maximum = this.initMaximum(this.maxResources, customerCount, resourceCount);
        this.need = new int[this.customerCount][this.resourceCount];
        this.allocation = this.initAllocation(customerCount, resourceCount);
        this.printInital();
    }

    public int resourceLength() {
        return this.resources.length;
    }

    public int[] getMaximum(int i) {
        return this.maximum[i];
    }

    public void printInital() {
        System.out.println("Bank: Initial Resources Available:");
        this.printResources();
        this.printMaximum();
        System.out.println();
    }

    public boolean request(Customer customer, int id, int[] request) {
        if(!this.isSafe(request)) {
            System.out.println("Bank: Not safe");
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

        this.printResources();
        return true;
    }

    public void release(Customer customer, int id, int[] request) {
        this.removeFromAllocation(customer.id, request);
        this.printAllocationMatrix();
    }

    public synchronized boolean isSafe(int[] request) {
        this.updateNeed();

        int[] avail = new int[this.resourceCount];
        for (int i = 0; i < this.resourceCount; i += 1) {
            avail[i] = this.resources[i].availablePermits();
        }

        boolean[] running = new boolean[this.customerCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            running[i] = true;
        }

        while (this.runningCount(running) > 0) {
            boolean atLeastOneAllocated = true;
            for (int customer = 0; customer < this.customerCount; customer += 1) {
                if (running[customer]) {
                    boolean flag = true;
                    for (int r = 0; r < this.resourceCount; r += 1) {
                        int resource = avail[r] - this.need[customer][r];
                        if (resource < 0) {
                            flag = false;
                        }
                    }

                    if (flag) {
                        running[customer] = false;
                        atLeastOneAllocated = true;
                        for (int i = 0; i < this.resourceCount; i += 1) {
                            avail[i] += this.allocation[customer][i];
                        }
                    }
                }
            }
            if (!atLeastOneAllocated) {
                return false;
            }
        }

        return true;
    }

    private int runningCount(boolean[] running) {
        int count = 0;
        for (int i = 0; i < running.length; i += 1) {
            if (running[i]) {
                count += 1;
            }
        }
        return count;
    }

    private void updateNeed() {
        for (int row = 0; row < this.customerCount; row += 1) {
            for (int col = 0; col < this.resourceCount; col += 1) {
                int max = this.maximum[row][col];
                int alloc = this.allocation[row][col];
                this.need[row][col] = max - alloc;
            }
        }
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

    private int[] initMaxResources(int resourceCount) {
        int[] resources = new int[resourceCount];
        for (int i = 0; i < this.resourceCount; i += 1) {
            int n = Util.randomIntRange(this.MIN_RESOURCE, this.MAX_RESOURCE);
            resources[i] = n;
        }
        return resources;
    }

    private Semaphore[] initResources(int[] resources, int resourceCount) {
        Semaphore[] res = new Semaphore[resourceCount];
        for (int i = 0; i < resourceCount; i += 1) {
            res[i] = new Semaphore(resources[i]);
        }
        return res;
    }

    private int[][] initMaximum(int[] resources, int customerCount, int resourceCount) {
        int[][] maximum = new int[customerCount][resourceCount];
        for (int row = 0; row < customerCount; row += 1) {
            for (int col = 0; col < resourceCount; col += 1) {
                int count = resources[col];
                int n = Util.randomIntRange(this.MIN_RESOURCE, count);
                maximum[row][col] = n;
            }
        }
        return maximum;
    }

    private int[][] initAllocation(int customerCount, int resourceCount) {
        int[][] allocation = new int[customerCount][resourceCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            allocation[i] = new int[resourceCount];
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
