import banker.*;
import banker.errors.*;

import java.util.*;
import java.io.*;

/**
 * Main executable that setups up all components of the program
 */
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
            Util.log("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (ParseErr e) {
            Util.log("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (InvalidArgumentErr e) {
            Util.log("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        }
    }

    /**
     * Prints out a help menu on basic usage of the program.
     */
    public static void printHelp() {
        String programName = "banker";
        String str = "";
        str += "Usage: " + programName;
        str += " resources customers";
        Util.log(str);
    }

    /**
     * Constructs a program from arguments given by the os. where both arguments
     * must be between 1 and 10.
     *
     * @throws ParseErr  thrown when item cannot be identified as an integer
     * @throws MissingArgErr thrown when missing a required argument
     * @throws InvalidArgumentErr thrown number is outside of the specified range
     * @param args raw arguments given by the OS.
     */
    public Program(String args[]) throws ParseErr, MissingArgErr, InvalidArgumentErr {
        this.parseArguments(args);
        this.bank = this.initBank();
        this.initCustomers(this.bank);
    }

    /**
     * Starts the program with the initialized parameters
     */
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

    /**
     * takes the given arguments for the OS and sets the appropriate attributes.
     * Throws errors if anything is invalid or missing a required parameter.
     *
     * @throws ParseErr
     * @throws MissingArgErr
     * @throws InvalidArgumentErr
     */
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

            this.customers[i].newRandomRequests(n);
            this.customers[i].close();
        }
    }
}
