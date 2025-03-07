package arcade.core.util.exceptions;

/** Exception thrown when parameter value is not equal to single expected value. */
public class IncorrectlySetParameterException extends RuntimeException {
    /**
     * Constructs an {@code InvalidParameterException} with the specified detail message.
     *
     * @param given the given parameter value
     * @param expected the expected parameter value
     */
    public IncorrectlySetParameterException(double given, double expected) {
        super(String.format("Parameter value [ %f ] must be equal to [ %f ]", given, expected));
    }
}
