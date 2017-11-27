import banker.*;
import banker.errors.*;

import java.util.*;
import java.io.*;

public class Program {
    private int resourceCount;
    private int customerCount;

    private Bank bank;
    private Customer[] customers;

    public static void main(String args[]) {
        try {
            Program prog = new Program(args);
            prog.start();
        } catch (MissingArgErr e) {
            System.out.println("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (ParseErr e) {
            System.out.println("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (InvalidArgumentErr e) {
            System.out.println("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        }
    }

    // prints the help message to standard output
    public static void printHelp() {
        String programName = "banker";
        String str = "";
        str += "Usage: " + programName;
        str += " resources customers";
        System.out.println(str);
    }

    public Program(String args[]) throws ParseErr, MissingArgErr, InvalidArgumentErr {
        this.parseArguments(args);
        this.bank = this.initBank();
        this.initCustomers(this.bank);
    }

    public void start() {
        Thread[] pids = new Thread[this.customerCount];

        // spin up all customer threads
        for (int i = 0; i < this.customerCount; i += 1) {
            pids[i] = new Thread(this.customers[i]);
            pids[i].start();
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
    private void parseArguments(String args[]) throws ParseErr, MissingArgErr, InvalidArgumentErr {
        if (args.length < 2) {
            throw new MissingArgErr();
        }
        try {
            this.resourceCount = Integer.valueOf(args[0]);
            int min = Bank.MIN_RESOURCE;
            int max = Bank.MAX_RESOURCE;
            if (this.resourceCount < min ||
                this.resourceCount > max) {
                String low = String.valueOf(min);
                String high = String.valueOf(max);
                throw new InvalidArgumentErr(args[0] + " needs to be between " + low + " and " + high);
            }
        } catch (NumberFormatException e) {
            throw new ParseErr(args[0] + " is not a valid number of resources");
        }

        try {
            this.customerCount = Integer.valueOf(args[1]);
        } catch (NumberFormatException e) {
            throw new ParseErr(args[1] + " is not a valid number of customers");
        }
    }

    private Bank initBank() {
        Bank bank = new Bank.Builder()
            .randomResources(this.resourceCount)
            .randomMaximum(this.customerCount, this.resourceCount)
            .build();
        return bank;
    }

    private void initCustomers(Bank bank) {
        this.customers = new Customer[this.customerCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            this.customers[i] = new Customer(i, bank);
            int n = Util.randomIntRange(Customer.MIN_REQUESTS, 5);
            this.customers[i].newRandomRequests(3);
            this.customers[i].close();
        }
        for (int i = 0; i < this.customerCount; i += 1) {
        }
    }
}
