package arcade.potts.util;

import ec.util.MersenneTwisterFast;

public final class PottsFlyEnums {
    
    /** Stem daughter direction options for fly stem cells in potts simulations. */
    public enum StemDaughter {
        APICAL,
        BASAL,
        LEFT,
        RIGHT,
        RANDOM;
        
        /**
         * Get a random {@code StemDaughter}.
         *
         * @param rng  the random number generator
         * @return  a random {@code StemDaughter}
         */
        public static StemDaughter random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length)];
        }
    }
}
