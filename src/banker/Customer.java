package banker;


import banker.errors.*;
import java.util.*;

public class Customer implements Runnable {
    enum RequestStatus {
        WAITING,  // In the queue to be wanting to allocate resource
        PENDING,  // has resources allocated
        FINISHED, // Has return resources
    }

    public final int id;
    private int[] resources;
    private int[] maxRequests;
    private Bank bank;
    private List<int[]> requests;
    private List<RequestStatus> status;
    private boolean finished;  // let the thread know that no more requests will
                               // be incoming

    public Customer(int id, Bank bank) {
        this.id = id;
        this.bank = bank;
        this.requests = new LinkedList<int[]>();
        this.status = new LinkedList<RequestStatus>();
        this.maxRequests = bank.getMaximum(this.id);
    }

    public synchronized void newRequest(int[] requests) {
        // this.bank.request(request);
        this.requests.add(requests);
        this.status.add(RequestStatus.WAITING);
    }

    public void newRandomRequest() {
        int[] request = new int[this.bank.resourceCount];
        for (int i = 0; i < this.bank.resourceCount; i += 1) {
            request[i] = Util.randomIntRange(1, this.maxRequests[i]);
        }
        this.newRequest(request);
    }

    public void finished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean hasUnfinishedRequest() {
        return this.status.contains(RequestStatus.WAITING) ||
            this.status.contains(RequestStatus.PENDING);
    }

    public void run() {
        while (!this.finished || this.hasUnfinishedRequest()) {
            if (!this.hasUnfinishedRequest() && this.isFinished()) {
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
