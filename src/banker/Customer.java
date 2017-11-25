package banker;


import banker.errors.*;
import java.util.*;

public class Customer implements Runnable {
    public final int id;

    private int[] resources;
    private int[] maxRequests;
    private Bank bank;
    private List<int[]> requests;
    private List<Boolean> requestComplete;
    private boolean finished;

    public Customer(int id, Bank bank) {
        this.id = id;
        this.bank = bank;
        this.requests = new LinkedList<int[]>();
        this.requestComplete = new LinkedList<Boolean>();
        this.maxRequests = bank.getMaximum(this.id);
    }

    public synchronized void request(int[] requests) {
        // this.bank.request(request);
        this.requests.add(requests);
        this.requestComplete.add(false);
    }

    public void randomRequest() {
        int[] request = new int[this.bank.resourceCount];
        for (int i = 0; i < this.bank.resourceCount; i += 1) {
            request[i] = Util.randomIntRange(1, this.maxRequests[i]);
        }
        this.request(request);
    }

    public void finished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean hasPendingRequest() {
        return this.requestComplete.contains(true);
    }

    public void run() {
        while (!this.finished || this.hasPendingRequest()) {
            if (!this.hasPendingRequest() && this.isFinished()) {
                Thread.yield();
            } else {
                try {
                    Thread.sleep(100 * Util.randomIntRange(10, 50));
                    System.out.println("Running");
                } catch (InterruptedException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void makeRequest() {
    }
}
