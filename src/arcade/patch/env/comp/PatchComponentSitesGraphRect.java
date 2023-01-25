package arcade.patch.env.comp;

import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSitesGraph} for rectangular geometry.
 * <p>
 * For pattern layout, the graph is given by:
 * <pre>
 *                         _ _ _ _
 *                       /         \
 *                      |           |
 *             _ _ _ _ /             \ _ _ _ _
 *           /         \             /         \
 *          |           |           |           |
 * _ _ _ _ /             \ _ _ _ _ /             \ _ _ _ _
 *         \             /         \             /
 *          |           |           |           |
 *           \ _ _ _ _ /             \ _ _ _ _ /
 *                     \             /
 *                      |           |
 *                       \ _ _ _ _ /
 * </pre>
 * <p>
 * For root layouts, each node has eight possible orientations for the edge:
 * left, right, up, down, up left, up right, down left, and down right. When
 * initializing roots from a border, only certain orientations are possible:
 * <ul>
 *     <li>left border = right, up right, down right</li>
 *     <li>right border = left, up left, down left</li>
 *     <li>top border = down, down right, down left</li>
 *     <li>bottom border = up, up right, up left</li>
 * </ul>
 */

public abstract class PatchComponentSitesGraphRect extends PatchComponentSitesGraph {
    /**
     * Creates a {@link PatchComponentSitesGraph} for rectangular geometry.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesGraphRect(Series series, MiniBox parameters) {
        super(series, parameters);
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphRect} for simple
     * hemodynamics.
     */
    public static class Simple extends PatchComponentSitesGraphRect {
        /**
         * Creates a {@link PatchComponentSitesGraphRect} with simple step.
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
     * Extension of {@link PatchComponentSitesGraphRect} for complex
     * hemodynamics.
     */
    public static class Complex extends PatchComponentSitesGraphRect {
        /**
         * Creates a {@link PatchComponentSitesGraphRect} with complex step.
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
