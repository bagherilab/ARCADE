package arcade.potts.util;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.agent.process.ProcessDomain;
import arcade.core.util.Vector;

/**
 * Container class for potts-specific enums.
 *
 * <p>Implemented enums include:
 *
 * <ul>
 *   <li>{@code Ordering} defining simulation stepping order
 *   <li>{@code State} defining cell states
 *   <li>{@code Domain} defining domain for a given process
 *   <li>{@code Region} defining subcellular regions
 *   <li>{@code Term} defining different potts energy terms
 *   <li>{@code Phase} defining phase for a given state
 *   <li>{@code Direction} defining directions in the voxel environment
 * </ul>
 */
public final class PottsEnums {
    /** Hidden utility class constructor. */
    protected PottsEnums() {
        throw new UnsupportedOperationException();
    }

    /** Stepping order for potts simulations. */
    public enum Ordering {
        /** Stepping order for potts. */
        POTTS,

        /** Stepping order for cells. */
        CELLS
    }

    /** Cell state codes for potts simulations. */
    public enum State implements CellState {
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
         * @param rng the random number generator
         * @return a random {@code State}
         */
        public static State random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }

    /** Process domain codes for potts simulations. */
    public enum Domain implements ProcessDomain {
        /** Code for undefined domain. */
        UNDEFINED
    }

    /** Cell region codes for potts simulations. */
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
         * @param rng the random number generator
         * @return a random {@code Region}
         */
        public static Region random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }

    /** Potts energy terms for potts simulations. */
    public enum Term {
        /** Code for undefined term. */
        UNDEFINED,

        /** Code for adhesion term. */
        ADHESION,

        /** Code for volume term. */
        VOLUME,

        /** Code for surface term. */
        SURFACE,

        /** Code for height term. */
        HEIGHT,

        /** Code for junction term. */
        JUNCTION,

        /** Code for substrate term. */
        SUBSTRATE,

        /** Code for persistence term. */
        PERSISTENCE;

        /**
         * Randomly selects a {@code Term}.
         *
         * @param rng the random number generator
         * @return a random {@code Term}
         */
        public static Term random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }

    /** Module phase codes for potts simulations. */
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
         * @param rng the random number generator
         * @return a random {@code Phase}
         */
        public static Phase random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }

    /** Location split directions for potts simulations. */
    public enum Direction {
        /** Unspecified direction. */
        UNDEFINED(null),

        /** Direction along the yz plane (y = 0, z = 0). */
        YZ_PLANE(new Vector(1, 0, 0)),

        /** Direction along the zx plane (z = 0, x = 0). */
        ZX_PLANE(new Vector(0, 1, 0)),

        /** Direction along the xy plane (x = 0, y = 0). */
        XY_PLANE(new Vector(0, 0, 1)),

        /** Direction along the positive xy axis (x = y, z = 0). */
        POSITIVE_XY(new Vector(-1, 1, 0)),

        /** Direction along the negative xy axis (x = -y, z = 0). */
        NEGATIVE_XY(new Vector(-1, -1, 0)),

        /** Direction along the positive yz axis (y = z, x = 0). */
        POSITIVE_YZ(new Vector(0, -1, 1)),

        /** Direction along the negative yz axis (y = -z, x = 0). */
        NEGATIVE_YZ(new Vector(0, -1, -1)),

        /** Direction along the positive zx axis (z = x, y = 0). */
        POSITIVE_ZX(new Vector(1, 0, -1)),

        /** Direction along the negative zx axis (z = -x, y = 0). */
        NEGATIVE_ZX(new Vector(-1, 0, -1));

        /** The normal vector of the plane in this direction. */
        public final Vector vector;

        /**
         * Creates a new {@code Direction} with the given vector.
         *
         * @param vector the vector associated with this direction
         */
        Direction(Vector vector) {
            this.vector = vector;
        }

        /**
         * Randomly selects a {@code Direction}, excluding {@code UNDEFINED}.
         *
         * @param rng the random number generator
         * @return a random {@code Direction}
         */
        public static Direction random(MersenneTwisterFast rng) {
            return values()[rng.nextInt(values().length - 1) + 1];
        }
    }
}
