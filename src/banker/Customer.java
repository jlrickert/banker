package banker;


import banker.errors.*;
import java.util.*;

/**
 * Customer is an entity that is able to have requests loaded and be able to
 * make requests to its associated bank.
 */
public class Customer implements Runnable {
    private enum RequestStatus {
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

    /*
     * Constructs a customer for a specific bank
     *
     * @param id  customer identification
     * @param bank  bank customer is associated with
     */
    public Customer(int id, Bank bank) {
        this.id = id;
        this.bank = bank;
        this.requests = new LinkedList<int[]>();
        this.status = new LinkedList<RequestStatus>();
        this.maximum = bank.getMaximum(this.id);
    }

    /**
     * Add request to the associated pool of requests that the customer will
     * make
     *
     * @param request  an array resources that the request will ask to borrow
     *                 from the bank
     */
    public void addRequest(int[] request) {
        this.requests.add(request);
        this.status.add(RequestStatus.WAITING);
    }

    /**
     * Generates a set of valid requests where the sum of the requests will not
     * exceed the maximum.
     *
     * @param requestCount  number of requests to be generated for this
     *                      customer.
     */
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

    /**
     * Test if the customer has any unfinished requests
     *
     * @return true if closed and has an unfinished request
     */
    public boolean isFinished() {
        return this.closed && this.finished == this.status.size();
    }

    /**
     * Closes the request addition window. Letting the thread know that there
     * will be no additions to the request pool.
     */
    public void close() {
        this.closed = true;
    }

    /**
     * Start the process of running requests in the request set.
     *
     * This will continuously select a non finished request randomly every 1 to
     * 5 second. The selected resource will either request or release resources
     * from the bank depending on the state of the transaction. This process
     * will run until all requests have moved from the WAITING state, to the
     * PENDING state, and then to the FINISHED state.
     */
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
                Util.log("Error " + e.getMessage());
                e.printStackTrace();
            }
        }
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " finished";
        Util.log(str);
    }

    private int getUnfinishedRandomRequest() {
        int count = 0;
        while (count < this.status.size()) {
            int n = Util.randomIntRange(0, this.status.size() - 1);
            if (this.status.get(n) != RequestStatus.FINISHED) {
                return n;
            }
            count += 1;
        }

        for (int i = 0; i < this.status.size(); i += 1) {
            if (this.status.get(i) != RequestStatus.FINISHED) {
                return i;
            }
        }
        return -1;
    }

    private void handleTransact(int index) {
        if (index < 0) {
            System.out.print("THIS SHOULD NOT HAPPEND");
            System.out.println(this.hasUnfinishedRequest());
            return;
        }
        RequestStatus status = this.status.get(index);
        int[] request = this.requests.get(index);
        if (status == RequestStatus.WAITING) {
            this.printRequest(this.id, index, request);
            this.handleRequest(index, request);
        } else if (status == RequestStatus.PENDING){
            this.printRelease(this.id, index, request);
            this.handleRelease(index, request);
        } else {
            Util.log("This shouldn't happen");
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
        Util.log(str);
    }

    private void printRelease(int id, int index, int[] request) {
        String str = "Customer ";
        str += String.valueOf(this.id);
        str += " releasing ";
        str += Util.stringify(request);
        Util.log(str);
    }
}
