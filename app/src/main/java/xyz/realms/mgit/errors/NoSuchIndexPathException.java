package xyz.realms.mgit.errors;

public class NoSuchIndexPathException extends Exception {

    public NoSuchIndexPathException() {
        super();
    }

    public NoSuchIndexPathException(String message) {
        super(message);
    }

    public NoSuchIndexPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchIndexPathException(Throwable cause) {
        super(cause);
    }
}
