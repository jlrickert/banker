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

    public void addRequest(int[] requests) {
        this.requests.add(requests);
        this.status.add(RequestStatus.WAITING);
    }

    public void newRandomRequests(int n) {
        for (int i = 0; i < n; i += 1) {
            int[] request = new int[this.bank.resourceCount];

            boolean allZero = true;
            while (allZero)
                for (int j = 0; j < this.bank.resourceCount; j += 1) {
                    int rand = Util.randomIntRange(0, this.maxRequests[j]);
                    int num = rand / n;
                    if (n < Util.randomIntRange(0, n) * 10) {
                        num += 1;
                    }
                    request[j] = num;
                    if (num != 0) {
                        allZero = false;
                    }
                }
            this.addRequest(request);
        }
    }

    private boolean hasUnfinishedRequest() {
        boolean hasWaiting = this.status.contains(RequestStatus.WAITING);
        boolean hasPending = this.status.contains(RequestStatus.PENDING);
        return hasWaiting || hasPending;
    }

    public boolean isFinished() {
        return this.closed && this.finished == this.status.size();
    }

    public void close() {
        this.closed = true;
    }

    public void run() {
        while (!this.closed) {
            Thread.yield();
        }
        while (this.hasUnfinishedRequest()) {
            try {
                int duration = Util.randomIntRange(1000, 5000);
                Thread.sleep(duration);
                int requestIndex = this.getUnfinishedRandomRequest();
                this.handleTransact(requestIndex);
            } catch (InterruptedException e) {
                System.out.println("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private int getUnfinishedRandomRequest() {
        int unfinished = 0;
        while (true) {
            int n = Util.randomIntRange(0, this.status.size() - 1);
            if (this.status.get(n) != RequestStatus.FINISHED) {
                return n;
            }
        }
        // for (int i = 0; i < this.status.size(); i += 1) {
        //     if (this.status.get(i) != RequestStatus.FINISHED) {
        //         unfinished += 1;
        //     }
        // }

        // int index = 0;
        // int begin = Util.randomIntRange(0, unfinished - 1);
        // for (int i = begin; true; i += 1) {
        //     index = i % this.status.size();
        //     if (this.status.get(index) != RequestStatus.FINISHED) {
        //         return index;
        //     }
        // }
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
        if (this.bank.request(this, index, request)) {
            this.status.set(index, RequestStatus.PENDING);
        }
    }

    private void handleRelease(int index, int[] request) {
        this.bank.release(this, index, request);
        this.status.set(index, RequestStatus.FINISHED);
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
