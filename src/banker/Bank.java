package banker;

import java.util.*;
import banker.errors.*;


public class Bank {
    public static final int MAX_RESOURCE = 10;
    public static final int MIN_RESOURCE = 1;
    public final int resourceCount;
    public final int customerCount;
    private final int[] resources;
    private final int[] maxResources;
    private final int[][] maximum;
    private int[][] allocation;

    public static class Builder {
        private int resourceCount;
        private int customerCount;
        private int[] resources;
        private int[][] maximum;

        public Builder() {
            this.customerCount = -1;
            this.resourceCount = -1;
        }

        public Builder randomResources(int resourceCount) {
            if (this.resourceCount < 0) {
                this.resourceCount = resourceCount;
            }

            this.resources = new int[this.resourceCount];
            for (int i = 0; i < this.resourceCount; i += 1) {
                int n = Util.randomIntRange(Bank.MIN_RESOURCE, Bank.MAX_RESOURCE);
                resources[i] = n;
            }
            return this;
        }

        public Builder resources(int[] resources) {
            if (this.resourceCount < 0) {
                this.resourceCount = resources.length;
            }
            this.resources = new int[this.resourceCount];
            for (int i = 0; i < resources.length; i += 1) {
                this.resources[i] = resources[i];
            }
            return this;
        }

        public Builder maximum(int[][] max) {
            if (this.customerCount  < 0) {
                this.customerCount = max.length;
            }
            if (this.resourceCount < 0) {
                this.resourceCount = max[0].length;
            }
            this.maximum = new int[this.customerCount][this.resourceCount];
            for (int row = 0; row < this.customerCount; row += 1) {
                for (int col = 0; col < this.resourceCount; col += 1) {
                    this.maximum[row][col] = max[row][col];
                }
            }
            return this;
        }

        public Builder randomMaximum(int customerCount, int resourceCount) {
            if (this.customerCount < 0) {
                this.customerCount = customerCount;
            }
            if (this.resourceCount < 0) {
                this.resourceCount = resourceCount;
            }
            this.maximum = new int[this.customerCount][this.resourceCount];
            for (int row = 0; row < this.customerCount; row += 1) {
                for (int col = 0; col < this.resourceCount; col += 1) {
                    int count = this.resources[col];
                    int n = Util.randomIntRange(Bank.MIN_RESOURCE, count);
                    this.maximum[row][col] = n;
                }
            }
            return this;
        }

        public Bank build() {
            return new Bank(this);
        }
    }


    private Bank(Builder builder) {
        this.customerCount = builder.customerCount;
        this.resourceCount = builder.resourceCount;
        this.maxResources = builder.resources;
        this.resources = this.initResources(this.maxResources);
        this.maximum = builder.maximum;
        this.allocation = new int[this.customerCount][this.resourceCount];
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
        String ss = String.valueOf(customer.id);
        ss += " ";
        ss += String.valueOf(id);
        System.out.println(ss + " starting");

        if (!this.isSafe(request)) {
            System.out.println("Bank: Not safe");
            return false;
        }

        System.out.println(ss + " Finishing");

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

        this.allocateRequest(request);

        this.printResources();
        return true;
    }

    private synchronized void allocateRequest(int[] request) {
        for (int i = 0; i < this.resourceCount;) {
            if (this.resources[i] < request[i]) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
                continue;
            }
            this.resources[i] -= request[i];
            i += 1;
        }
    }

    public synchronized void release(Customer customer, int id, int[] request) {
        this.removeFromAllocation(customer.id, request);
        this.printAllocationMatrix();
        for (int i = 0; i < this.resourceCount; i += 1) {
            this.resources[i] += request[i];
        }
        this.notify();
        this.printResources();
    }

    public boolean isSafe(int[] request) {
        int[] avail = this.copyResources();
        int[][] allocation = this.copyAllocation();
        int[][] need = this.calcNeed(allocation);

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
                        int resource = avail[r] - need[customer][r];
                        if (resource < 0) {
                            flag = false;
                        }
                    }

                    if (flag) {
                        running[customer] = false;
                        atLeastOneAllocated = true;
                        for (int i = 0; i < this.resourceCount; i += 1) {
                            avail[i] += allocation[customer][i];
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

    private int[][] calcNeed(int[][] allocation) {
        int[][] need = new int[this.customerCount][this.resourceCount];
        for (int row = 0; row < this.customerCount; row += 1) {
            for (int col = 0; col < this.resourceCount; col += 1) {
                int max = this.maximum[row][col];
                int alloc = allocation[row][col];
                need[row][col] = max - alloc;
            }
        }
        return need;
    }

    private synchronized int[][] copyAllocation() {
        int[][] alloc = new int[this.customerCount][this.resourceCount];
        for (int row = 0; row < this.customerCount; row += 1) {
            for (int col = 0; col < this.resourceCount; col += 1) {
                alloc[row][col] = this.allocation[row][col];
            }
        }
        return alloc;
    }

    private synchronized int[] copyResources() {
        int[] resources = new int[this.resourceCount];
        for (int r = 0; r < this.customerCount; r += 1) {
            resources[r] = this.resources[r];
        }
        return resources;
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

    private int[] initResources(int[] resources) {
        int[] res = new int[resources.length];
        for (int i = 0; i < resources.length; i += 1) {
            res[i] = resources[i];
        }
        return res;
    }

    public synchronized void printResources() {
        String str = "Available Resources: ";
        str += Util.stringify(this.resources);
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
            int count = this.resources[col];
            str += String.valueOf(count);
            if (col < this.resources.length - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }
}
