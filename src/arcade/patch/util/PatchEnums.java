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
        /** Stepping order for cells. */
        CELLS,
        
        /** Stepping order for lattices. */
        LATTICES,
    }
    
    /** Process domain codes. */
    public enum Domain {
        /** Code for undefined domain. */
        UNDEFINED,
        
        /** Code for metabolism domain. */
        METABOLISM,

        /** Code for signaling domain. */
        SIGNALING;

        /**
         * Randomly selects a {@code Domain}.
         *
         * @param rng  the random number generator
         * @return  a random {@code Domain}
         */
        public static Domain random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
    
    /** Operation category codes. */
    public enum Category {
        /** Code for undefined category. */
        UNDEFINED,
        
        /** Code for metabolism category. */
        DIFFUSER,
        
        /** Code for signaling category. */
        GENERATOR;
        
        /**
         * Randomly selects a {@code Category}.
         *
         * @param rng  the random number generator
         * @return  a random {@code Operation}
         */
        public static Category random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
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
