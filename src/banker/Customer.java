package banker;


import banker.errors.*;
import java.util.*;

public class Customer implements Runnable {
    enum RequestStatus {
        WAITING,  // In the queue to be wanting to allocate resource
        PENDING,  // has resources allocated
        FINISHED, // Has return resources
    }

    public static final int MIN_REQUESTS = 3;
    public final int id;
    private int[] maxRequests;
    private Bank bank;
    private List<int[]> requests;  // List off requests that the thread will make
    private List<RequestStatus> status; // current status of each thread
    private int finished;  // number of finished threads
    private boolean closed;

    public Customer(int id, Bank bank) {
        this.id = id;
        this.bank = bank;
        this.requests = new LinkedList<int[]>();
        this.status = new LinkedList<RequestStatus>();
        this.maxRequests = bank.getMaximum(this.id);
    }

    public synchronized void newRequest(int[] requests) {
        this.requests.add(requests);
        this.status.add(RequestStatus.WAITING);
        this.notify();
    }

    public void newRandomRequest() {
        int[] request = new int[this.bank.resourceCount];
        for (int i = 0; i < this.bank.resourceCount; i += 1) {
            request[i] = Util.randomIntRange(1, this.maxRequests[i] / 3 + 1);
        }
        this.newRequest(request);
    }

    private boolean hasUnfinishedRequest() {
        boolean hasWaiting = this.status.contains(RequestStatus.WAITING);
        boolean hasPending = this.status.contains(RequestStatus.PENDING);
        return hasWaiting || hasPending;
    }

    public boolean isFinished() {
        return this.closed && this.finished == this.status.size();
    }

    public synchronized void close() {
        this.closed = true;
        this.notify();
    }

    public void run() {
        boolean hasUnfinished = this.hasUnfinishedRequest();
        boolean belowMin = this.status.size() < this.MIN_REQUESTS;
        while (!this.isFinished()) {
            try {
                Thread.sleep(100 * Util.randomIntRange(10, 50));
                int requestIndex = this.getUnfinishedRandomRequest();
                this.handleTransact(requestIndex);
            } catch (InterruptedException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
            hasUnfinished = this.hasUnfinishedRequest();
        }
    }

    private synchronized int getUnfinishedRandomRequest() {
        // wait if all requests are finished and there are more incoming
        if (this.finished == this.status.size()) {
            try {
                // NOTE: this could be closed while waiting and nothing could have
                // been added resulting in return nil. Closed gets called right
                // away so this isn't a problem
                this.wait();
            } catch (InterruptedException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }

        {  // NOTE: this seems to favor the lower requests
            int unfinished = 0;
            for (int i = 0; i < this.status.size(); i += 1) {
                if (this.status.get(i) != RequestStatus.FINISHED) {
                    unfinished += 1;
                }
            }

            int index = 0;
            int begin = Util.randomIntRange(0, unfinished - 1);
            for (int i = begin; true; i += 1) {
                index = i % this.status.size();
                if (this.status.get(index) != RequestStatus.FINISHED) {
                    return index;
                }
            }
        }
    }

    private void handleTransact(int index) {
        RequestStatus status = this.status.get(index);
        int[] request = this.requests.get(index);
        if (status == RequestStatus.WAITING) {
            this.printRequest(this.id, index, request);
            this.handleRequest(index, request);
        } else if (status == RequestStatus.PENDING){
            this.printRelease(this.id, index, request);
            this.handleRelease(index, request);
        } else {
            System.out.println("This shouldn't happen");
        }
    }

    private void handleRequest(int index, int[] request) {
        Transaction trans = new Transaction(this, index, request);
        if (this.bank.request(trans)) {
            this.status.set(index, RequestStatus.PENDING);
        }
    }

    private void handleRelease(int index, int[] request) {
        Transaction trans = new Transaction(this, index, request);
        this.bank.release(trans);
        this.status.set(index, RequestStatus.FINISHED);
        this.finished += 1;
    }

    private void printRequest(int id, int index, int[] request) {
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " requesting ";
        str += Util.stringify(request);
        System.out.println(str);
    }

    private void printRelease(int id, int index, int[] request) {
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " releasing ";
        str += Util.stringify(request);
        System.out.println(str);
    }
}
