package pivot.beans;

public class PropertyNotFoundException extends RuntimeException {
    public static final long serialVersionUID = 0;

    public PropertyNotFoundException() {
        this(null, null);
    }

    public PropertyNotFoundException(String message) {
        this(message, null);
    }

    public PropertyNotFoundException(Throwable cause) {
        this(null, cause);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
