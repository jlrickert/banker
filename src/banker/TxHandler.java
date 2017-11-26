package banker;

import banker.errors.*;
import java.util.*;
import java.util.concurrent.*;

public class TxHandler implements Runnable {
    private boolean running;
    private Semaphore[] resources;
    private Queue<Transaction> acquires;

    public TxHandler() {
        this.running = false;
        this.acquires = new LinkedList<Transaction>();
    }

    public void setResources(Semaphore[] resources) {
        this.resources = resources;
    }

    public synchronized void add(Transaction trans) {
        String str = "TxHandler: Adding transaction ";
        str += String.valueOf(trans.id);
        str += " for customer ";
        str += String.valueOf(trans.owner.id);
        this.acquires.add(trans);
        this.notify();
    }

    public void handleRelease(Transaction trans) {
        String str = "Starting Release ";
        str += String.valueOf(trans.id);
        str += " for customer ";
        str += String.valueOf(trans.owner.id);
        for (int i = 0; i < this.resources.length; i += 1) {
            this.resources[i].release(trans.request[i]);
        }
        this.printResources();
        str = "Finished Release ";
        str += String.valueOf(trans.id);
        str += " for customer ";
        str += String.valueOf(trans.owner.id);
    }

    public void run() {
        this.running = true;
        while (running || !this.acquires.isEmpty()) {
            handleNextTransaction();
        }
        System.out.println("TxHandler exiting");
    }

    public synchronized void stop() {
        this.running = false;
        this.notify();
    }

    private synchronized void handleNextTransaction() {
        if (this.acquires.isEmpty()) {
            try {
                System.out.println("No transactions in queue: Waiting");
                this.wait();
                System.out.println("Transactions in queue: Resuming");
                if (!this.running) {
                    return;
                }
            } catch (InterruptedException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
        Transaction trans = this.acquires.remove();
        this.handleAcquire(trans);
    }

    private void handleAcquire(Transaction trans) {
        String str = "Starting Transaction ";
        str += String.valueOf(trans.id);
        str += " for customer ";
        str += String.valueOf(trans.owner.id);
        for (int i = 0; i < this.resources.length; i += 1) {
            try {
                this.resources[i].acquire(trans.request[i]);
            } catch (InterruptedException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
        str = "Finished Transaction ";
        str += String.valueOf(trans.id);
        str += " for customer ";
        str += String.valueOf(trans.owner.id);
        this.printResources();
    }

    public synchronized void printResources() {
        String str = "Available Resources: ";
        str += this.stringifiedResources();
        System.out.println(str);
    }

    private String stringifiedResources() {
        String str = "[";
        for (int col = 0; col < this.resources.length; col += 1) {
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
