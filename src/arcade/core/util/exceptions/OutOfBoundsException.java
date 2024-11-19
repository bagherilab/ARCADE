package arcade.core.util.exceptions;

/** Exception thrown when a parameter is out of bounds. */
public class OutOfBoundsException extends RuntimeException {
    /**
     * Constructs an {@code OutOfBoundsException} with the specified detail message.
     *
     * @param given the given valid parameter value
     * @param minValid the minimum valid parameter value
     * @param maxValid the maximum valid parameter value
     */
    public OutOfBoundsException(double given, double minValid, double maxValid) {
        super(
                String.format(
                        "Parameter value [ %f ] must be between [ %f ] and [ %f ]",
                        given, minValid, maxValid));
    }
}
