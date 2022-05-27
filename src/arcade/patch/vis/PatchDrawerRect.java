package arcade.patch.vis;

import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.util.gui.ColorMap;
import arcade.core.vis.Drawer;
import arcade.core.vis.Panel;
import arcade.patch.sim.PatchSimulation;

/**
 * Container for patch-specific {@link Drawer} classes for rectangular patches.
 */

public abstract class PatchDrawerRect extends PatchDrawer {
    /**
     * Creates a {@link Drawer} for potts simulations.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param map  the color map for the array
     * @param bounds  the size of the drawer within the panel
     */
    PatchDrawerRect(Panel panel, String name, int length, int width, int depth,
                    ColorMap map, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }
    
    /**
     * Creates a {@link Drawer} for potts simulations.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param bounds  the size of the drawer within the panel
     */
    PatchDrawerRect(Panel panel, String name, int length, int width, int depth,
                    Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }
    
    /**
     * Extension of {@link PatchDrawer} for drawing rectangular patches.
     */
    public static class PatchGrid extends PatchDrawer {
        /** Offsets for rectangles */
        private static final int[][] OFFSETS = { { 0, 0 }, { 2, 0 }, { 2, 2 }, { 0, 2 } };
        
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /**
         * Creates a {@code RectGrid} drawer.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param bounds  the size of the drawer within the panel
         */
        PatchGrid(Panel panel, String name,
                int length, int width, int depth, Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, bounds);
            LENGTH = length;
            WIDTH = width;
            field.width = LENGTH;
            field.height = WIDTH;
        }
        
        /**
         * Steps the drawer to draw rectangular grid.
         */
        public void step(SimState state) {
            PatchSimulation sim = (PatchSimulation)state;
            field.clear();
            graph.clear();

            // Draw rectangular grid.
            for (int i = 0; i <= WIDTH; i++) { add(field, graph, 1, 0, i, LENGTH, i); }
            for (int i = 0; i <= LENGTH; i++) { add(field, graph, 1, i, 0, i, WIDTH); }
        }
    }
}