package arcade.potts.util;

import ec.util.MersenneTwisterFast;

public final class PottsEnums {
    protected PottsEnums() {
        throw new UnsupportedOperationException();
    }
    
    /** Stepping order for simulation. */
    public enum Ordering {
        /** Stepping order for potts. */
        POTTS,
        
        /** Stepping order for cells. */
        CELLS
    }
    
    /** Potts energy terms. */
    public enum Term {
        /** Code for volume term. */
        VOLUME,
        
        /** Code for surface term. */
        SURFACE
    }
    
    /** Module phase codes. */
    public enum Phase {
        /** Code for undefined phase. */
        UNDEFINED,
        
        /** Code for proliferative G1 phase. */
        PROLIFERATIVE_G1,
        
        /** Code for proliferative S phase. */
        PROLIFERATIVE_S,
        
        /** Code for proliferative G2 phase. */
        PROLIFERATIVE_G2,
        
        /** Code for proliferative M phase. */
        PROLIFERATIVE_M,
        
        /** Code for early apoptosis phase. */
        APOPTOTIC_EARLY,
        
        /** Code for late apoptosis phase. */
        APOPTOTIC_LATE,
        
        /** Code for apoptosed cell. */
        APOPTOSED;
        
        /**
         * Randomly selects a {@code Phase}.
         * 
         * @param rng  the random number generator
         * @return  a random {@code Phase}
         */
        public static Phase random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
    
    /** Location split directions. */
    public enum Direction {
        /** Unspecified direction. */
        UNDEFINED,
        
        /** Direction along the yz plane (y = 0, z = 0). */
        YZ_PLANE,
        
        /** Direction along the zx plane (z = 0, x = 0). */
        ZX_PLANE,
        
        /** Direction along the xy plane (x = 0, y = 0). */
        XY_PLANE,
        
        /** Direction along the positive xy axis (x = y, z = 0). */
        POSITIVE_XY,
        
        /** Direction along the negative xy axis (x = -y, z = 0). */
        NEGATIVE_XY,
        
        /** Direction along the positive yz axis (y = z, x = 0). */
        POSITIVE_YZ,
        
        /** Direction along the negative yz axis (y = -z, x = 0). */
        NEGATIVE_YZ,
        
        /** Direction along the positive zx axis (z = x, y = 0). */
        POSITIVE_ZX,
        
        /** Direction along the negative zx axis (z = -x, y = 0). */
        NEGATIVE_ZX;
        
        /**
         * Randomly selects a {@code Direction}.
         * 
         * @param rng  the random number generator
         * @return  a random {@code Direction}
         */
        public static Direction random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
}
