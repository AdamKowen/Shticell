package loader;

public class SheetLoadingException extends Exception {
    public SheetLoadingException(String message) {
        super(message);
    }

    public SheetLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
