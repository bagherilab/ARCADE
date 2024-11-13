package arcade.core.util;

import java.util.Arrays;
import java.util.HashSet;

public enum Feature {
    /** Placeholder feature. */
    PLACEHOLDER,

    /** Angiogenesis for patch implementation. */
    ANGIOGENESIS,

    /** CART cell agents for patch implementation. */
    CART_CELLS,

    /** Cell cycle tracking for patch implementation. */
    CELL_CYCLE_TRACKING,

    /** Chemotherapy action for patch implementation. */
    CHEMOTHERAPY,

    /** Fly stem cell agents for potts implementation. */
    FLY_STEM_CELLS;

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
