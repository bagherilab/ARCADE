package arcade.patch.util;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.agent.process.ProcessDomain;
import arcade.core.env.operation.OperationCategory;

/**
 * Container class for patch-specific enums.
 * <p>
 * Implemented enums include:
 * <ul>
 *     <li>{@code Ordering} defining simulation stepping order</li>
 *     <li>{@code State} defining cell states</li>
 *     <li>{@code Domain} defining domain for a given process</li>
 *     <li>{@code Flag} defining state change flags</li>
 *     <li>{@code Category} defining operation categories</li>
 * </ul>
 */

public final class PatchEnums {
    /**
     * Hidden utility class constructor.
     */
    protected PatchEnums() {
        throw new UnsupportedOperationException();
    }
    
    /** Stepping order for patch simulations. */
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
    
    /** Cell state codes for patch simulations. */
    public enum State implements CellState {
        /** Code for undefined state. */
        UNDEFINED,
        
        /** Code for quiescent cells. */
        QUIESCENT,
        
        /** Code for proliferative cells. */
        PROLIFERATIVE,
        
        /** Code for migratory cells. */
        MIGRATORY,
        
        /** Code for apoptotic cells. */
        APOPTOTIC,
        
        /** Code for necrotic cells. */
        NECROTIC,
        
        /** Code for senescent cells. */
        SENESCENT;
        
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
    
    /** Process domain codes for patch simulations. */
    public enum Domain implements ProcessDomain {
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
    
    /** State change flags for patch simulations. */
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
    
    /** Operation category codes for patch simulations. */
    public enum Category implements OperationCategory {
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
}
