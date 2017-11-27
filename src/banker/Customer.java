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
    private int[] maximum;
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
        this.maximum = bank.getMaximum(this.id);
    }

    public void addRequest(int[] requests) {
        this.requests.add(requests);
        this.status.add(RequestStatus.WAITING);
    }

    public void newRandomRequests(int requestCount) {
        int[][] request = new int[requestCount][this.maximum.length];
        for (int i = 0; i < requestCount; i += 1) {
            for (int j = 0; j < this.maximum.length; j += 1) {
                request[i][j] = Util.randomIntRange(0, this.maximum[j]);
            }
        }

        for (int col = 0; col < this.maximum.length; col += 1) {
            boolean done = false;
            while (!done) {
                int max = this.maximum[col];
                if (max == 0) {
                    break;
                } else if (max <= requestCount) {
                    max = requestCount;
                }

                int n = Util.randomIntRange(0, max / requestCount);
                int row = Util.randomIntRange(0, requestCount - 1);
                if (request[row][col] < n) {
                    request[row][col] = 0;
                } else {
                    request[row][col] -= n;
                }

                int sum = 0;
                for (int i = 0; i < requestCount; i += 1) {
                    sum += request[i][col];
                }

                if (sum <= this.maximum[col]) {
                    if (sum == 0) {
                        n = Util.randomIntRange(0, this.maximum[col]);
                        row = Util.randomIntRange(0, requestCount - 1);
                        request[row][col] = n;
                    }
                    done = true;
                }
            }
        }

        for (int i = 0; i < requestCount; i += 1) {
            this.addRequest(request[i]);
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
                Logger.log("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " finished";
        Logger.log(str);
    }

    private int getUnfinishedRandomRequest() {
        while (true) {
            int n = Util.randomIntRange(0, this.status.size() - 1);
            if (this.status.get(n) != RequestStatus.FINISHED) {
                return n;
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
            Logger.log("This shouldn't happen");
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
        Logger.log(str);
    }

    private void printRelease(int id, int index, int[] request) {
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " releasing ";
        str += Util.stringify(request);
        Logger.log(str);
    }
}
