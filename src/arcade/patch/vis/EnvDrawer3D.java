package arcade.patch.vis;

import java.awt.geom.Rectangle2D;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import arcade.core.sim.Simulation;

/**
 * {@link arcade.core.vis.Drawer} for environment lattices in 3D.
 * <p>
 * {@code EnvDrawer3D} copies values in a {@link arcade.core.env.lat.Lattice} array
 * into a 2D array representation by averaging across the z direction.
 */

public abstract class EnvDrawer3D extends Drawer {
    /** Serialization version identifier */
    private static final long serialVersionUID = 0;
    
    /** Array of values */
    DoubleGrid2D array;
    
    /**
     * Creates a {@link arcade.core.vis.Drawer} for drawing 3D environment lattices.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param map  the color map for the array
     * @param bounds  the size of the drawer within the panel
     */
    EnvDrawer3D(Panel panel, String name,
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
    
    /** {@link arcade.vis.EnvDrawer3D} for drawing rectangular grid. */
    public static class Rectangular extends EnvDrawer3D {
        /** Serialization version identifier */
        private static final long serialVersionUID = 0;
        
        /** Height of the lattice (z direction) */
        private final int HEIGHT;
        
        /**
         * Creates a {@code Rectangular} environment drawer.
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
            HEIGHT = depth;
        }
        
        /**
         * Steps the drawer to populate rectangular array.
         */
        public void step(SimState state) {
            Simulation sim = (Simulation)state;
            array.setTo(0);
            
            for (double[][] layer : sim.getEnvironment(name).getField()) {
                for (int i = 0; i < layer.length; i++) {
                    for (int j = 0; j < layer[i].length; j++) {
                        array.field[i][j] += layer[i][j] / HEIGHT;
                    }
                }
            }
        }
    }
    
    /** {@link arcade.vis.EnvDrawer3D} for drawing triangular grid. */
    public static class Triangular extends EnvDrawer3D {
        /** Serialization version identifier */
        private static final long serialVersionUID = 0;
        
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /** Height of the lattice (z direction) */
        private final int HEIGHT;
        
        /**
         * Creates a {@code Triangular} environment drawer.
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
        public Triangular(Panel panel, String name,
                          int length, int width, int depth,
                          ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, 3*length + 2, 3*width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            HEIGHT = depth;
        }
        
        /**
         * Steps the drawer to populate triangular array.
         */
        public void step(SimState state) {
            Simulation sim = (Simulation)state;
            array.setTo(0);
            double[][] _to = array.field;
            double[][] _from = new double[_to.length][_to[0].length];
            
            for (double[][] layer : sim.getEnvironment(name).getField()) {
                for (int i = 0; i < layer.length; i++) {
                    for (int j = 0; j < layer[i].length; j++) {
                        _from[i][j] += layer[i][j] / HEIGHT;
                    }
                }
            }
            
            Drawer.toTriangular(_to, _from, LENGTH, WIDTH);
        }
    }
}