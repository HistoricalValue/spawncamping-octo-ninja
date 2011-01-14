package sample.util;

public class AssertionError extends RuntimeException {

    private final static long serialVersionUID = 0x8746;

    public AssertionError(Throwable cause) {
        super(cause);
    }

    public AssertionError(String message, Throwable cause) {
        super(message, cause);
    }

    public AssertionError(String message) {
        super(message);
    }

    public AssertionError() {
    }

}
