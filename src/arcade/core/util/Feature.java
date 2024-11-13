package arcade.core.util;

import java.util.Arrays;
import java.util.HashSet;

public enum Feature {
    /** Placeholder feature. */
    PLACEHOLDER;

    /** Map of activated features. */
    public static final HashSet<Feature> activationMap = new HashSet<>();

    /**
     * Check if feature is activated.
     *
     * @return {@code true} if feature is active, {@code false} otherwise
     */
    public boolean isActive() {
        return activationMap.contains(this);
    }

    /** Activate the feature. */
    public void activate() {
        activationMap.add(this);
    }

    /** Activate all features. */
    public static void activateAll() {
        activationMap.addAll(Arrays.asList(values()));
    }

    /** Deactivate the feature. */
    public void deactivate() {
        activationMap.remove(this);
    }

    /** Deactivate all features. */
    public static void deactivateAll() {
        activationMap.clear();
    }
}
