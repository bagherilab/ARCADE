package arcade.core.util.exceptions;

/** Exception thrown when a parameter is out of bounds. */
public class OutOfBoundsException extends RuntimeException {
    /**
     * Constructs an {@code OutOfBoundsException} with the specified detail message.
     *
     * @param message the detail message
     */
    public OutOfBoundsException(String message) {
        super(message);
    }
}
