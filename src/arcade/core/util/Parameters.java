package arcade.core.util;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.core.util.distributions.DegenerateDistribution;
import arcade.core.util.distributions.Distribution;

/**
 * Container for parameters and parameter distributions.
 *
 * <p>{@code Parameters} objects contain a {@link MiniBox} of population parameters along with
 * parameter distributions. Utility methods are provided to return parameters as specific types.
 */
public class Parameters {
    /** Population parameters. */
    final MiniBox popParameters;

    /** Map of parameter names to distributions. */
    final HashMap<String, Distribution> distributions;

    /**
     * Creates a cell {@code Parameters} instance.
     *
     * <p>Parameters with distributions are tagged as {@code DISTRIBUTION} in the population
     * parameters dictionary. Each of these parameters are converted to a {@link Distribution}
     * instance. If a parent cell parameters is passed, the parent parameter distributions are used
     * to generate the {@link Distribution} instance instead.
     *
     * @param popParameters the cell population parameters
     * @param cellParameters the parent cell parameters
     * @param random the random number generator
     */
    public Parameters(
            MiniBox popParameters, Parameters cellParameters, MersenneTwisterFast random) {
        this.popParameters = popParameters;
        distributions = new HashMap<>();

        MiniBox distributionsBox = popParameters.filter("(DISTRIBUTION)");
        for (String key : distributionsBox.getKeys()) {
            Distribution distribution;
            System.out.println("Key: " + key);
            if (cellParameters != null && cellParameters.distributions.containsKey(key)) {
                distribution = cellParameters.distributions.get(key).rebase(random);
            } else if (popParameters.contains(key + "_IC")) {
                System.out.println("Key: " + key + "_IC");
                distribution = new DegenerateDistribution(key, popParameters, random);
            } else {
                distribution = popParameters.getDistribution(key, random);
            }

            distributions.put(key, distribution);
        }
    }

    /**
     * Gets the parameter value as a double.
     *
     * <p>The parameter value is pulled from the distribution object, if it exists. Otherwise, the
     * parameter is pulled from the population parameter dictionary.
     *
     * @param key the parameter key
     * @return the parameter value as a double
     */
    public double getDouble(String key) {
        if (distributions.containsKey(key)) {
            return distributions.get(key).getDoubleValue();
        } else if (popParameters.contains(key)) {
            return popParameters.getDouble(key);
        } else {
            throw new InvalidParameterException();
        }
    }

    /**
     * Gets the parameter value as an integer.
     *
     * <p>The parameter value is pulled from the distribution object, if it exists. Otherwise, the
     * parameter is pulled from the population parameter dictionary.
     *
     * @param key the parameter key
     * @return the parameter value as an integer
     */
    public int getInt(String key) {
        if (distributions.containsKey(key)) {
            return distributions.get(key).getIntValue();
        } else if (popParameters.contains(key)) {
            return popParameters.getInt(key);
        } else {
            throw new InvalidParameterException();
        }
    }

    /**
     * Gets the parameter value as a string.
     *
     * @param key the parameter key
     * @return the parameter value as a string
     */
    public String getString(String key) {
        if (popParameters.contains(key)) {
            return popParameters.get(key);
        } else {
            throw new InvalidParameterException();
        }
    }

    /**
     * Gets the parameter value as a distribution, if it exists.
     *
     * @param key the parameter key
     * @return the parameter value as a distribution
     */
    public Distribution getDistribution(String key) {
        if (distributions.containsKey(key)) {
            return distributions.get(key);
        } else {
            throw new InvalidParameterException();
        }
    }

    /**
     * Filters parameters by the given code.
     *
     * @param code the code to filter by
     * @return the filtered box
     */
    public MiniBox filter(String code) {
        return popParameters.filter(code);
    }

    /**
     * Compares two {@code Parameters} instances.
     *
     * <p>Instances are equal if (1) both parameters have the same population parameters and (2)
     * both parameters have the same distributions. Note that distributions are the same if they
     * have the same distribution parameters; the value pulled from each distribution is ignored.
     *
     * @param parameters the {@code Parameters} to compare to
     * @return {@code true} if contents match, {@code false} otherwise
     */
    public boolean compare(Parameters parameters) {
        HashSet<String> allKeys = new HashSet<>();
        allKeys.addAll(distributions.keySet());
        allKeys.addAll(parameters.distributions.keySet());

        for (String key : allKeys) {
            if (!distributions.containsKey(key)) {
                return false;
            } else if (!parameters.distributions.containsKey(key)) {
                return false;
            } else if (!distributions
                    .get(key)
                    .getParameters()
                    .compare(parameters.distributions.get(key).getParameters())) {
                return false;
            }
        }

        return popParameters.compare(parameters.popParameters);
    }
}
