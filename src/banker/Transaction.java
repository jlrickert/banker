package banker;

public class Transaction {
    public final int id;
    public final Customer owner;
    public final int[] request;
    public Transaction(Customer owner, int id, int[] request) {
        this.id = id;
        this.owner = owner;
        this.request = request;
    }
}
