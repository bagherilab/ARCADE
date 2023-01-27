package arcade.patch.util;

import ec.util.MersenneTwisterFast;

/**
 * Container class for patch-specific enums.
 * <p>
 * Implemented enums include:
 * <ul>
 *     <li>{@code Ordering} defining simulation stepping order</li>
 *     <li>{@code Domain} defining domain for a given module</li>
 *     <li>{@code Flag} defining state change flags</li>
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
        /** First stepping order (nothing stepped before). */
        FIRST,
        
        /** Stepping order for first action. */
        FIRST_ACTION,
        
        /** Stepping order for actions. */
        ACTIONS,
        
        /** Stepping order for last action. */
        LAST_ACTION,
        
        /** Stepping order for first cell. */
        FIRST_CELL,
        
        /** Stepping order for cells. */
        CELLS,
        
        /** Stepping order for last cell. */
        LAST_CELL,
        
        /** Stepping order for first component. */
        FIRST_COMPONENT,
        
        /** Stepping order for components. */
        COMPONENTS,
        
        /** Stepping order for last component. */
        LAST_COMPONENT,
        
        /** Stepping order for first lattice. */
        FIRST_LATTICE,
        
        /** Stepping order for lattices. */
        LATTICES,
        
        /** Stepping order for last lattice. */
        LAST_LATTICE,
        
        /** Last stepping order (nothing stepped after). */
        LAST,
    }
    
    /** State change flags. */
    public enum Flag {
        /** Code for undefined flag. */
        UNDEFINED,
        
        /** Code for proliferative flag. */
        PROLIFERATIVE,
        
        /** Code for migratory flag. */
        MIGRATORY;
        
        /**
         * Randomly selects a {@code Flag}.
         *
         * @param rng  the random number generator
         * @return  a random {@code Flag}
         */
        public static Flag random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
}
