package banker;

import banker.errors.*;
import java.util.*;


public class Customer implements Runnable {
    public enum Status {
        PENDING,
        COMPLETED,
    }

    Status status;
    public final int id;

    private LinkedList<int[]> requests;
    private int[] maxRequests;
    private Bank bank;

    public Customer(int id, Bank bank) {
        this.id = id;
        this.bank = bank;
        this.maxRequests = bank.getMaximum(this.id);
    }

    public synchronized void request(int[] requests) throws Err {
        if (this.check(requests)) {
            throw new Err("This exceeds maximum aloud allocation of resources");
        }
        // this.bank.request(request);
    }

    public boolean check(int[] requests) {
        return true;
    }

    public void run() {
    }
}
