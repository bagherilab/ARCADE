package arcade.core.util.exceptions;

/** Exception thrown when a specification is missing. */
public class MissingSpecificationException extends RuntimeException {
    /**
     * Constructs an {@code MissingSpecificationException} with the specified detail message.
     *
     * @param missing the expected specification
     */
    public MissingSpecificationException(String missing) {
        super("The input file must contain the following specification: ");
    }
}
