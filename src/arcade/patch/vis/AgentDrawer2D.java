package arcade.vis;

import java.awt.geom.Rectangle2D;
import sim.engine.*;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import arcade.sim.Simulation;
import arcade.agent.cell.Cell;

/**
 * {@link arcade.vis.Drawer} for agent grids in 2D.
 * <p>
 * {@code AgentDrawer2D} converts agents in a {@link arcade.env.grid.Grid} into
 * a 2D array representation.
 * The array values are the value of a selected property (such as cell type or
 * cell population).
 */

public abstract class AgentDrawer2D extends Drawer {
    /** Serialization version identifier */
    private static final long serialVersionUID = 0;
    
    /** Code for integer grid drawing */
    public static final int GRID_INTEGER = 0;
    
    /** Code for double grid drawing */
    public static final int GRID_DOUBLE = 2;
    
    /** Code for integer lattice drawing */
    public static final int LATTICE_INTEGER = 1;
    
    /** Code for double lattice drawing */
    public static final int LATTICE_DOUBLE = 3;
    
    /** Array of values */
    DoubleGrid2D array;
    
    /** Method name for populating array */
    final String method;
    
    /**
     * Creates a {@link arcade.vis.Drawer} for drawing 2D agent grids.
     * 
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param map  the color map for the array
     * @param bounds  the size of the drawer within the panel
     */
    AgentDrawer2D(Panel panel, String name,
            int length, int width, int depth,
            ColorMap map, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
        this.method = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    public Portrayal makePort() {
        ValueGridPortrayal2D port = new FastValueGridPortrayal2D();
        array = new DoubleGrid2D(length, width, map.defaultValue());
        port.setField(array);
        port.setMap(map);
        return port;
    }
    
    /** {@link arcade.vis.AgentDrawer2D} for drawing hexagonal agents */
    public static class Hexagonal extends AgentDrawer2D {
        /** Serialization version identifier */
        private static final long serialVersionUID = 0;
        
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /** Drawing code */
        private final int CODE;
        
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
         * @param code  the drawing code
         */
        public Hexagonal(Panel panel, String name,
                         int length, int width, int depth,
                         ColorMap map, Rectangle2D.Double bounds, int code) {
            super(panel, name, 3*length + 2, 3*width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            CODE = code;
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
            if (CODE == LATTICE_DOUBLE || CODE == LATTICE_INTEGER) {
                for (int i = 0; i < LENGTH; i++) {
                    for (int j = 0; j < WIDTH; j++) {
                        _from[i][j] = map.defaultValue();
                    }
                }
            } else { array.setTo(0); }
            
            double value = 0;
            for (Object obj : sim.getAgents().getAllObjects()) {
                c = (Cell)obj;
                if (c.getLocation().getGridZ() == 0) {
                    int[][] locs = c.getLocation().getLatLocations();
                    
                    switch (CODE) {
                        case LATTICE_DOUBLE: case GRID_DOUBLE:
                            value = (double)(Drawer.getValue(method, c));
                            break;
                        case LATTICE_INTEGER: case GRID_INTEGER:
                            value = (int)(Drawer.getValue(method, c));
                            break;
                    }
                    
                    switch (CODE) {
                        case LATTICE_DOUBLE: case LATTICE_INTEGER:
                            int p = c.getLocation().getPosition();
                            _from[locs[p][0]][locs[p][1]] = value;
                            break;
                        case GRID_DOUBLE: case GRID_INTEGER:
                            for (int[] loc : locs) { _from[loc[0]][loc[1]] += value; }
                            break;
                    }
                }
            }
            
            Drawer.toTriangular(_to, _from, LENGTH, WIDTH);
        }
    }
    
    /** {@link arcade.vis.AgentDrawer2D} for drawing rectangular agents */
    public static class Rectangular extends AgentDrawer2D {
        /** Serialization version identifier */
        private static final long serialVersionUID = 0;
        
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /** Drawing code */
        private final int CODE;
        
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
         * @param code  the drawing code
         */
        public Rectangular(Panel panel, String name,
                         int length, int width, int depth,
                         ColorMap map, Rectangle2D.Double bounds, int code) {
            super(panel, name, length, width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            CODE = code;
        }
        
        /**
         * Steps the drawer to populate the array with values.
         */
        public void step(SimState state) {
            Simulation sim = (Simulation)state;
            double[][] _to = array.field;
            Cell c;
            
            // Reset old fields.
            if (CODE == LATTICE_DOUBLE || CODE == LATTICE_INTEGER) {
                for (int i = 0; i < LENGTH; i++) {
                    for (int j = 0; j < WIDTH; j++) {
                        _to[i][j] = map.defaultValue();
                    }
                }
            } else { array.setTo(0); }
            
            double value = 0;
            for (Object obj : sim.getAgents().getAllObjects()) {
                c = (Cell)obj;
                if (c.getLocation().getGridZ() == 0) {
                    int[][] locs = c.getLocation().getLatLocations();
                    
                    switch (CODE) {
                        case LATTICE_DOUBLE: case GRID_DOUBLE:
                            value = (double)(Drawer.getValue(method, c));
                            break;
                        case LATTICE_INTEGER: case GRID_INTEGER:
                            value = (int)(Drawer.getValue(method, c));
                            break;
                    }
                    
                    switch (CODE) {
                        case LATTICE_DOUBLE: case LATTICE_INTEGER:
                            int p = c.getLocation().getPosition();
                            _to[locs[p][0]][locs[p][1]] = value;
                            break;
                        case GRID_DOUBLE: case GRID_INTEGER:
                            for (int[] loc : locs) { _to[loc[0]][loc[1]] += value; }
                            break;
                    }
                }
            }
        }
    }
}