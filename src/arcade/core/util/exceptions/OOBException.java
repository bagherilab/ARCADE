package arcade.core.util.exceptions;

/** Exception thrown when a parameter is out of bounds. */
public class OOBException extends RuntimeException {
    /**
     * Constructs an {@code OOBException} with the specified detail message.
     *
     * @param message the detail message
     */
    public OOBException(String message) {
        super(message);
    }
}
