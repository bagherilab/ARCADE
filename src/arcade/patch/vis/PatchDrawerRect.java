package arcade.patch.vis;

import java.util.ArrayList;
import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.util.gui.ColorMap;
import arcade.core.vis.Drawer;
import arcade.core.vis.Panel;
import arcade.patch.env.loc.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.sim.PatchSeries;

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
         * Creates a {@code PatchGrid} drawer.
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
            
            // Draw rectangular agent locations.
            int radius = ((PatchSeries) sim.getSeries()).radius;
            ArrayList<PatchLocation> locs = sim.getPatches().getLocations(radius, 1);
            for (PatchLocation loc : locs) {
                int[] xy = loc.getLatLocation();
                for (int i = 0; i < 4; i++) {
                    add(field, graph, 2,
                        xy[0] + OFFSETS[i][0], xy[1] + OFFSETS[i][1],
                        xy[0] + OFFSETS[(i + 1)%4][0], xy[1] + OFFSETS[(i + 1)%4][1]);
                }
            }
            
            // Draw border.
            int ind, r;
            for (PatchLocation loc : locs) {
                int[] xyz = loc.getGridLocation();
                int[] xy = loc.getLatLocation();
                
                r = Math.max(Math.abs(xyz[0]), Math.abs(xyz[1])) + 1;
                
                if (r == radius) {
                    if (xyz[0] == radius - 1) { ind = 1; }
                    else if (xyz[0] == 1 - radius) { ind = 3; }
                    else if (xyz[1] == radius - 1) { ind = 2; }
                    else if (xyz[1] == 1 - radius) { ind = 0; }
                    else { ind = 0; }
                    
                    add(field, graph, 3,
                        xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
                        xy[0] + OFFSETS[(ind + 1)%4][0], xy[1] + OFFSETS[(ind + 1)%4][1]);
                    
                    if (Math.abs(xyz[0]) + 1 == r && Math.abs(xyz[1]) + 1 == r) {
                        if (xyz[0] == radius - 1 && xyz[1] == radius - 1) { ind = 2; }
                        else if (xyz[0] == 1 - radius && xyz[1] == radius - 1) { ind = 2; }
                        else if (xyz[0] == radius - 1 && xyz[1] == 1 - radius) { ind = 0; }
                        else if (xyz[0] == 1 - radius && xyz[1] == 1 - radius) { ind = 0; }
                        
                        add(field, graph, 3,
                            xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
                            xy[0] + OFFSETS[(ind + 1)%4][0], xy[1] + OFFSETS[(ind + 1)%4][1]);
                    }
                }
            }
        }
    }
}