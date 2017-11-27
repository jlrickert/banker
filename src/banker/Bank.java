package banker;

import java.util.*;
import banker.errors.*;


/**
 * Bank handles managing how resources are allocated and determines if it is
 * safe to do so. The bank is initialized with a preset amount of resources and
 * with a set of the maximum that each customer will ever request. As requests
 * comes each one is checked against the available resources, the maximum that
 * each customer could potentially request, and how many resources each customer
 * currently has. This is essentially the bankers algorithm.
 */
public class Bank {
    public static final int MAX_RESOURCE = 10;
    public static final int MIN_RESOURCE = 1;
    public final int resourceCount;
    public final int customerCount;
    private final int[] resources;
    private final int[] maxResources;
    private final int[][] maximum;
    private int[][] allocation;
    private int[][] need;

    /**
     * Build manages constructing the bank. It requires that a set of methods
     * are called in the correct order.
     *
     * The first requirement is that either <b>randomResources</b> or
     * <b>resources</b> is called first. Where <b>randomResources</b> auto
     * generates a set for you while <b>resources</b> is the resources to be
     * manually set. This is mutually exclusive.
     *
     * The second member that needs to be called is either <b>randomMaximum</b>
     * or <b>maximum</b>. Where <b>randomMaximum</b> auto generates a valid set
     * for where the maximum for a customer cannot exceed the resources set from
     * the previously called method. while <b>maximum</b> is the resources to be
     * manually set. This is mutually exclusive.
     */
    public static class Builder {
        private int resourceCount;
        private int customerCount;
        private int[] resources;
        private int[][] maximum;

        public Builder() {
            this.customerCount = -1;
            this.resourceCount = -1;
        }

        /**
         * Sets the resources to n random values between <b>Bank.MIN_RESOURCE</b>
         * and <b>Bank.MAX_RESOURCE</b>.
         *
         * @param resourceCount the number of different resource types that a
         * customer could potentially request.
         *
         * @return Builder returns itself
         */
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

        /**
         * Manually set the resources.
         *
         * @param resources an array of numbers where each element is the number
         * of available resources corresponding to each type.
         *
         * @return Builder returns itself
         */
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

        /**
         * Manually set the maximum. This assumes that maximum has the correct
         * dimensions that aligns up with the number of customers and resources.
         * Also assumes that each cell is less than or equal to its associated
         * resource type.
         *
         * @param max  an array of numbers where each element is the number
         *             of available resources corresponding to each type.
         *
         * @return Builder returns itself
         */
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

        /**
         * Generates a valid matrix for the maximum number of resources that
         * each customer may request. This assumes that resources are already
         * setup and that the <b>resourceCount</b> is the same as the number of
         * resources previously set.
         *
         * @param customerCount the number of customers
         * @param resourceCount the number of resource types
         *
         * @return Builder returns itself
         */
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

        /**
         * Finalized building the bank. This assumes that methods have been
         * called in the correct order have been called.
         *
         * @return Builder returns itself
         */
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
        this.need = new int[this.customerCount][this.resourceCount];
        this.printInital();
    }

    /**
     * Returns the maximum that a customer may request.
     *
     * @param customerId id of the customer
     * @return an array of the maximum of each resource that the customer may
     *         request.
     */
    public int[] getMaximum(int customerId) {
        return this.maximum[customerId];
    }

    /**
     * Prints the available resources and maximum for each customer to screen.
     */
    public void printInital() {
        Util.log("Bank: Initial Resources Available:");
        this.printResources();
        this.printMaximum();
    }

    /**
     * Handles a request from a customer. This first checks if the request
     * request is safe. If it is not safe it returns false. Otherwise it
     * continues on and allocates the resources, and then prints the results.
     *
     * @param customer  the customer making the request
     * @param id        the id of the of transaction
     * @param request   the request to be processed
     * @return true if a request has been processed. false if the the request was not safe and is not allocated.
     */
    public synchronized boolean request(Customer customer, int id, int[] request) {
        if (!this.isSafe(request)) {
            Util.log("Bank: Not safe");
            return false;
        }

        this.addToAllocationMatrix(customer.id, request);
        {  // print request granted
            String str = "Customer ";
            str += String.valueOf(customer.id);
            str += " request ";
            str += String.valueOf(id);
            str += " granted";
            Util.log(str);
        }
        this.printAllocationMatrix();
        this.allocateRequest(customer, id, request);
        return true;
    }

    private synchronized void allocateRequest(Customer customer, int id, int[] request) {
        String customerId = String.valueOf(customer.id);
        String str = "Allocating for customer ";
        str += String.valueOf(customer.id);
        Util.log("Customer "+customerId+" allocating resources");
        for (int i = 0; i < this.resourceCount;) {
            if (this.resources[i] < request[i]) {
                try {
                    this.wait();
                    System.out.println("Customer "+customerId+" Waiting");
                } catch (InterruptedException e) {
                    Util.log("Error " + e.getMessage());
                    e.printStackTrace();
                }
                continue;
            }
            this.resources[i] -= request[i];
            i += 1;
        }
        Util.log("Customer "+customerId+" allocating finished");

        this.printResources();
    }

    /**
     * Releases the resources of the given request
     *
     * @param customer  the customer making the request
     * @param id        the id of the of transaction
     * @param request   the request to be processed
    */
    public synchronized void release(Customer customer, int id, int[] request) {
        this.removeFromAllocation(customer.id, request);
        this.printAllocationMatrix();
        for (int i = 0; i < this.resourceCount; i += 1) {
            this.resources[i] += request[i];
        }
        this.printResources();
        this.notify();
    }

    private boolean isSafe(int[] request) {
        this.updateNeed();
        int[] avail = this.copyResources();

        boolean[] finish = new boolean[this.customerCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            finish[i] = false;
        }

        boolean check = true;
        while (check) {
            check = false;
            for (int customer = 0; customer < this.customerCount; customer += 1) {
                if (!finish[customer]) {
                    int r = 0;
                    for (; r < this.resourceCount; r += 1) {
                        if (need[customer][r] > avail[r]) {
                            break;
                        }
                    }

                    if (r == this.resourceCount) {
                        for (r = 0; r < this.resourceCount; r += 1) {
                            avail[r] += this.allocation[customer][r];
                        }
                        finish[customer] = true;
                        check = true;
                        // sequence += customer;
                    }
                }
            }
        }
        int i = 0;
        for (;i < this.customerCount; i += 1) {
            if (!finish[i]) {
                break;
            }
        }

        if (i != this.customerCount) {
            return false;
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

    private int[] copyResources() {
        int[] resources = new int[this.resourceCount];
        for (int i = 0; i < this.resourceCount; i += 1) {
            resources[i] = this.resources[i];
        }
        return resources;
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

    /*
     * prints out the current available resources to standard out
     */
    public synchronized void printResources() {
        String str = "Available Resources: ";
        str += Util.stringify(this.resources);
        Util.log(str);
    }

    /*
     * prints out a matrix of the max resources that a customer may ever request
     * to standard out.
     */
    public synchronized void printMaximum() {
        Util.log("Bank - Max");
        for (int row = 0; row < this.customerCount; row += 1) {
            String str = "\t";
            str += Util.stringify(this.maximum[row]);
            Util.log(str);
        }
    }

    /*
     * prints out a matrix of that each customer has requested to standard out.
     */
    public synchronized void printAllocationMatrix() {
        Util.log("Bank - Allocation: ");
        for (int row = 0; row < this.customerCount; row += 1) {
            String str = "\t";
            str += Util.stringify(this.allocation[row]);
            Util.log(str);
        }
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
