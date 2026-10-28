package arcade.core.util.exceptions;

/** Exception thrown when incompatible features are given. */
public class IncompatibleFeatureException extends RuntimeException {
    /**
     * Constructs an {@code IncompatibleFeatureException} with the specified detail message.
     *
     * @param feature the feature name
     * @param given the given associated feature
     * @param expected the expected associated feature
     */
    public IncompatibleFeatureException(String feature, String given, String expected) {
        super(
                String.format(
                        feature
                                + "is incompatible with "
                                + given
                                + ", must be associated with "
                                + expected
                                + "."));
    }
}
