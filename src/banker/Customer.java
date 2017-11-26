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

    public synchronized void finished() {
        this.finished = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public synchronized boolean hasUnfinishedRequest() {
        boolean hasWaiting = this.status.contains(RequestStatus.WAITING);
        boolean hasPending = this.status.contains(RequestStatus.PENDING);
        return hasWaiting || hasPending;
    }

    public void run() {
        boolean hasUnfinished = this.hasUnfinishedRequest();
        while (!this.isFinished() && hasUnfinished) {
            if (hasUnfinished) {
                try {
                    int requestIndex = this.getUnfinishedRandomRequest();
                    this.handleTransact(requestIndex);
                    Thread.sleep(100 * Util.randomIntRange(10, 50));
                } catch (InterruptedException e) {
                    System.out.println("Error " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                Thread.yield();
            }
            hasUnfinished = this.hasUnfinishedRequest();
        }
    }

    private synchronized int getUnfinishedRandomRequest() {
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

    private void handleTransact(int index) {
        RequestStatus status = this.status.get(index);
        int[] request = this.requests.get(index);
        if (status == RequestStatus.WAITING) {
            this.printRequest(index, request);
            this.bank.request(this, request);
        } else {
            this.printRelease(index, request);
            this.bank.release(this, request);
        }
    }

    private void printRequest(int id, int[] request) {
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " requesting ";
        str += Util.stringify(request);
        System.out.println(str);
    }

    private void printRelease(int id, int[] request) {
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " releasing ";
        str += Util.stringify(request);
        System.out.println(str);
    }
}
