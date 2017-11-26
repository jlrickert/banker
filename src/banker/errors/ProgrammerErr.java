package banker.errors;


public class ProgrammerErr extends Err {
    public ProgrammerErr(String msg) {
        super("Programmer error: " + msg);
    }
}
