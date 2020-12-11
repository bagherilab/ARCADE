package arcade.core.util;

import ec.util.MersenneTwisterFast;

public final class Enums {
    protected Enums() {
        throw new UnsupportedOperationException();
    }
    
    /** Cell state codes. */
    public enum State {
        /** Code for undefined state. */
        UNDEFINED,
        
        /** Code for quiescent cells. */
        QUIESCENT,
        
        /** Code for proliferative cells. */
        PROLIFERATIVE,
        
        /** Code for apoptotic cells. */
        APOPTOTIC,
        
        /** Code for necrotic cells. */
        NECROTIC,
        
        /** Code for autotic cells. */
        AUTOTIC;
        
        /**
         * Randomly selects a {@code State}.
         * 
         * @param rng  the random number generator
         * @return  a random {@code State}
         */
        public static State random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
    
    /** Cell region codes. */
    public enum Region {
        /** Undefined region. */
        UNDEFINED,
        
        /** Region for cytoplasm. */
        DEFAULT,
        
        /** Region for nucleus. */
        NUCLEUS;
        
        /**
         * Randomly selects a {@code Region}.
         * 
         * @param rng  the random number generator
         * @return  a random {@code Region}
         */
        public static Region random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
}
