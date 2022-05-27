package arcade.patch.util;

/**
 * Container class for potts-specific enums.
 * <p>
 * Implemented enums include:
 * <ul>
 *     <li>{@code Ordering} defining simulation stepping order</li>
 * </ul>
 */

public final class PatchEnums {
    /**
     * Hidden utility class constructor.
     */
    protected PatchEnums() {
        throw new UnsupportedOperationException();
    }
    
    /** Stepping order for simulation. */
    public enum Ordering {
        /** Stepping order for cells. */
        CELLS
    }
}
