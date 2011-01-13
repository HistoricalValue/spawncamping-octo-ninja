package sample.util;

public class AssertionError extends RuntimeException {

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
