package arcade.patch.vis;

import java.awt.geom.Rectangle2D;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import arcade.core.sim.Simulation;
import arcade.agent.cell.Cell;

/**
 * {@link arcade.core.vis.Drawer} for agent grids in 3D.
 * <p>
 * {@code AgentDrawer3D} converts agents in a {@link arcade.core.env.grid.Grid} into
 * a 2D array representation by calculating density in the z direction.
 */

public abstract class AgentDrawer3D extends Drawer {
    /** Serialization version identifier */
    private static final long serialVersionUID = 0;
    
    /** Array of values */
    DoubleGrid2D array;
    
    /**
     * Creates a {@link arcade.core.vis.Drawer} for drawing 3D agent grids.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param map  the color map for the array
     * @param bounds  the size of the drawer within the panel
     */
    AgentDrawer3D(Panel panel, String name,
                  int length, int width, int depth,
                  ColorMap map, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }
    
    public Portrayal makePort() {
        ValueGridPortrayal2D port = new FastValueGridPortrayal2D();
        array = new DoubleGrid2D(length, width, map.defaultValue());
        port.setField(array);
        port.setMap(map);
        return port;
    }
    
    /** {@link arcade.vis.AgentDrawer3D} for drawing hexagonal agents */
    public static class Hexagonal extends AgentDrawer3D {
        /** Serialization version identifier */
        private static final long serialVersionUID = 0;
        
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /** Height of the lattice (z direction) */
        private final int HEIGHT;
        
        /**
         * Creates a {@code Hexagonal} agent drawer.
         * <p>
         * Length and width of the drawer are expanded from the given length and
         * width of the simulation so each index can be drawn as a 3x3 triangle.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param map  the color map for the array
         * @param bounds  the size of the drawer within the panel
         */
        public Hexagonal(Panel panel, String name,
                         int length, int width, int depth,
                         ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, 3*length + 2, 3*width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            HEIGHT = 3 * (depth - 2);
        }
        
        /**
         * Steps the drawer to populate the array with values.
         */
        public void step(SimState state) {
            Simulation sim = (Simulation)state;
            double[][] _to = array.field;
            double[][] _from = new double[LENGTH][WIDTH];
            Cell c;
            
            // Reset old fields.
            array.setTo(0);
            
            for (Object obj : sim.getAgents().getAllObjects()) {
                c = (Cell)obj;
                int[][] locs = c.getLocation().getLatLocations();
                for (int[] loc : locs) { _from[loc[0]][loc[1]] += 1./HEIGHT; }
            }
            
            Drawer.toTriangular(_to, _from, LENGTH, WIDTH);
        }
    }
    
    /** {@link arcade.vis.AgentDrawer3D} for drawing rectangular agents */
    public static class Rectangular extends AgentDrawer3D {
        /** Serialization version identifier */
        private static final long serialVersionUID = 0;
        
        /** Height of the lattice (z direction) */
        private final int HEIGHT;
        
        /**
         * Creates a {@code Rectangular} agent drawer.
         * <p>
         * Length and width of the drawer are the same as the length and width
         * of the simulation.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param map  the color map for the array
         * @param bounds  the size of the drawer within the panel
         */
        public Rectangular(Panel panel, String name,
                           int length, int width, int depth,
                           ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, map, bounds);
            HEIGHT = 3 * (depth - 2);
        }
        
        /**
         * Steps the drawer to populate the array with values.
         */
        public void step(SimState state) {
            Simulation sim = (Simulation)state;
            double[][] _to = array.field;
            Cell c;
            
            // Reset old fields.
            array.setTo(0);
            
            for (Object obj : sim.getAgents().getAllObjects()) {
                c = (Cell)obj;
                int[][] locs = c.getLocation().getLatLocations();
                for (int[] loc : locs) { _to[loc[0]][loc[1]] += 1./HEIGHT; }
            }
        }
    }
}
