package arcade.patch.vis;

import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.util.gui.ColorMap;
import arcade.core.vis.Drawer;
import arcade.core.vis.Panel;
import arcade.patch.sim.PatchSimulation;

/**
 * Container for patch-specific {@link Drawer} classes for hexagonal patches.
 */

public abstract class PatchDrawerHex extends PatchDrawer {
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
    PatchDrawerHex(Panel panel, String name, int length, int width, int depth,
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
    PatchDrawerHex(Panel panel, String name, int length, int width, int depth,
                   Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }
    
    /**
     * Extension of {@link PatchDrawer} for drawing hexagonal patches.
     */
    public static class PatchGrid extends PatchDrawerHex {
        /** Offsets for hexagons */
        private static final int[][] OFFSETS = { { 0, 0 }, { 2, 0 }, { 3, 1 }, { 2, 2 }, { 0, 2 }, { -1, 1 } };
        
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /**
         * Creates a {@code PatchGrid} drawer.
         * <p>
         * Length and width of the drawer are expanded from the given length and
         * width of the simulation.
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
            super(panel, name, 3*length + 2, 3*width, depth, bounds);
            LENGTH = length + 1;
            WIDTH = width;
            field.width = LENGTH;
            field.height = WIDTH;
        }
        
        /**
         * Steps the drawer to draw triangular grid.
         */
        public void step(SimState state) {
            PatchSimulation sim = (PatchSimulation)state;
            field.clear();
            graph.clear();
            
            // Draw triangular grid.
            for (int i = 0; i <= WIDTH; i++) {
                add(field, graph, 1,
                    (i % 2 == 0 ? 0 : 1), i,
                    (i % 2 == 0 ? LENGTH : LENGTH - 1), i);
            }
            
            for (int i = 0; i <= LENGTH - 1; i += 2) {
                for (int j = 0; j < WIDTH; j++) {
                    add(field, graph, 1,
                        (j % 2 == 0 ? i : i + 1), j,
                        (j % 2 == 0 ? i + 1 : i), j + 1);
                }
            }
            
            for (int i = 1; i <= LENGTH; i += 2) {
                for (int j = 0; j < WIDTH; j++) {
                    add(field, graph, 1,
                        (j % 2 == 0 ? i + 1 : i), j,
                        (j % 2 == 0 ? i : i + 1), j + 1);
                }
            }
        }
    }
}