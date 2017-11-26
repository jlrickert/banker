import banker.*;
import banker.errors.*;

import java.util.*;
import java.io.*;

class Program {
    private int resourceCount;
    private int customerCount;

    private Bank bank;
    private Customer[] customers;

    public static void main(String args[]) {
        try {
            Program prog = new Program(args);
            prog.start();
        } catch (MissingArgErr e) {
            System.out.println("Error " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (ParseErr e) {
            System.out.println("Error " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        }
    }

    // prints the help message to standard output
    public static void printHelp() {
        System.out.println("wassup");
    }

    public Program(String args[]) throws ParseErr, MissingArgErr {
        this.parseArguments(args);
        this.initBank();
        this.initRandomCustomers();
    }

    public void start() {
        Thread[] pids = new Thread[this.customerCount];

        // spin up all customer threads
        for (int i = 0; i < this.customerCount; i += 1) {
            pids[i] = new Thread(this.customers[i]);
            pids[i].start();
        }

        {   // load requests
            for (int i = 0; i < this.customerCount; i += 1) {
                int count = 0; // count of requests added
                int num = 50;  // percent chance to get included after 3
                               // requests
                while (true) {
                    int n = Util.randomIntRange(1, 100);
                    if (count <= Customer.MIN_REQUESTS || n < num) {
                        this.customers[i].newRandomRequest();
                        count += 1;
                        num >>= 2;
                    } else {
                        break;
                    }
                }

                // tell the customer thread there is no more incoming requests
                this.customers[i].finished();
            }
        }

        // Wait for all customer threads to be done
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < pids.length; i += 1 ) {
                if (pids[i].isAlive()) {
                    flag = true;
                    Thread.yield();
                }
            }
        }
    }

    // maybe throw error on problem
    private void parseArguments(String args[]) throws ParseErr, MissingArgErr {
        this.resourceCount = 7;
        this.customerCount = 4;
    }

    private void initBank() {
        this.bank = new Bank(this.resourceCount, this.customerCount);
    }

    private void initRandomCustomers() {
        this.customers = new Customer[this.customerCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            this.customers[i] = new Customer(i, this.bank);
        }
    }
}
