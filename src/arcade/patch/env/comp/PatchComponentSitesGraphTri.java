package arcade.patch.env.comp;

import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSitesGraph} for triangular geometry.
 * <p>
 * For pattern layout, the graph is given by:
 * <pre>
 *                         ___ ___
 *                       /         \
 *                      /           \
 *             ___ ___ /             \ ___ ___
 *           /         \             /         \
 *          /           \           /           \
 * ___ ___ /             \ ___ ___ /             \ ___ ___
 *         \             /         \             /
 *          \           /           \           /
 *           \ ___ ___ /             \ ___ ___ /
 *                     \             /
 *                      \           /
 *                       \ ___ ___ /
 * </pre>
 * <p>
 * For root layouts, each node has six possible orientations for the edge: left,
 * right, up left, up right, down left, and down right. When initializing roots
 * from a border, only certain orientations are possible:
 * <ul>
 *     <li>left border = right, up right, down right</li>
 *     <li>right border = left, up left, down left</li>
 *     <li>top border = down right, down left</li>
 *     <li>bottom border = up right, up left</li>
 * </ul>
 */

public abstract class PatchComponentSitesGraphTri extends PatchComponentSitesGraph {
    /**
     * Creates a {@link PatchComponentSitesGraph} for triangular geometry.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesGraphTri(Series series, MiniBox parameters) {
        super(series, parameters);
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphTri} for simple
     * hemodynamics.
     */
    public static class Simple extends PatchComponentSitesGraphTri {
        /**
         * Creates a {@link PatchComponentSitesGraphTri} with simple step.
         *
         * @param series  the simulation series
         * @param parameters  the component parameters dictionary
         */
        public Simple(Series series, MiniBox parameters) {
            super(series, parameters);
        }
        
        @Override
        public void step(SimState state) {
            super.simpleStep();
        }
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphTri} for complex
     * hemodynamics.
     */
    public static class Complex extends PatchComponentSitesGraphTri {
        /**
         * Creates a {@link PatchComponentSitesGraphTri} with complex step.
         *
         * @param series  the simulation series
         * @param parameters  the component parameters dictionary
         */
        public Complex(Series series, MiniBox parameters) {
            super(series, parameters);
        }
        
        @Override
        public void step(SimState state) {
            super.complexStep(state.random);
        }
    }
}
