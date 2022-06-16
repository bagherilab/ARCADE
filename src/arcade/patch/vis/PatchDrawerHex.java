package arcade.patch.vis;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.util.gui.ColorMap;
import arcade.core.vis.Drawer;
import arcade.core.vis.Panel;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.loc.PatchLocation;
import arcade.patch.env.loc.PatchLocationContainer;
import arcade.patch.env.loc.PatchLocationFactory;
import arcade.patch.env.loc.PatchLocationFactoryHex;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.sim.PatchSeries;

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
     * Expands an array to a triangular representation.
     *
     * @param _to  the new empty triangular array
     * @param _from  the original array of values
     * @param length  the length of the original array
     * @param width  the width of the original array
     */
    private static void expand(double[][] _to, double[][] _from, int length, int width) {
        for (int i = 0; i < length; i ++) {
            for (int j = 0; j < width; j++) {
                expand(_to, i, j, _from[i][j]);
            }
        }
    }
    
    /**
     * Draws a triangle for a given location with the given value.
     *
     * @param arr  the target array
     * @param i  the coordinate of the triangle in the x direction
     * @param j  the coordinate of the triangle in the y direction
     * @param val  the value for the triangle
     */
    private static void expand(double[][] arr, int i, int j, double val) {
        int dir = ((i + j) & 1) == 0 ? 0 : 2;
        arr[i * 3 + 2][j * 3] = val;
        arr[i * 3 + 2][j * 3 + 1] = val;
        arr[i * 3 + 2][j * 3 + 2] = val;
        arr[i * 3 + 1][j * 3 + 1] = val;
        arr[i * 3 + 3][j * 3 + 1] = val;
        arr[i * 3][j * 3 + dir] = val;
        arr[i * 3 + 1][j * 3 + dir] = val;
        arr[i * 3 + 3][j * 3 + dir] = val;
        arr[i * 3 + 4][j * 3 + dir] = val;
    }
    
    /**
     * Extension of {@link PatchDrawer} for drawing hexagonal cells.
     */
    public static class PatchCells extends PatchDrawerHex {
        /** Length of the lattice (x direction) */
        private final int LENGTH;
        
        /** Width of the lattice (y direction) */
        private final int WIDTH;
        
        /** Drawing view. */
        private final View view;
        
        /**
         * Creates a {@link PatchDrawer} for drawing hexagonal cells.
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
        public PatchCells(Panel panel, String name,
                          int length, int width, int depth,
                          ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, 3*length + 2, 3*width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            String[] split = name.split(":");
            view = View.valueOf(split[1]);
        }
        
        @Override
        public void step(SimState state) {
            PatchSimulation sim = (PatchSimulation) state;
            double[][] arr = array.field;
            double[][] temp = new double[LENGTH][WIDTH];
            double[][] counter = new double[LENGTH][WIDTH];
            
            PatchCell cell;
            PatchLocation location;
            
            switch (view) {
                case STATE: case AGE:
                    for (int i = 0; i < LENGTH; i++) {
                        for (int j = 0; j < WIDTH; j++) {
                            temp[i][j] = map.defaultValue();
                        }
                    }
                    break;
                case VOLUME: case HEIGHT: case COUNTS:
                    array.setTo(0);
                    break;
                default:
                    break;
            }
            
            HashMap<Integer, Integer> positions = new HashMap<>();
            
            for (Object obj : sim.getGrid().getAllObjects()) {
                cell = (PatchCell)obj;
                location = (PatchLocation) cell.getLocation();
                
                if (location.getGridZ() == 0) {
                    int[][] locs = location.getLatLocations();
                    
                    int hash = location.hashCode();
                    int position = -1;
                    if (positions.containsKey(hash)) { position = positions.get(hash); }
                    positions.put(hash, ++position);
                    
                    switch (view) {
                        case STATE:
                            temp[locs[position][0]][locs[position][1]] = cell.getState().ordinal();
                            break;
                        case AGE:
                            temp[locs[position][0]][locs[position][1]] = cell.getAge();
                            break;
                        case COUNTS:
                            for (int[] loc : locs) {
                                temp[loc[0]][loc[1]]++;
                            }
                            break;
                        case VOLUME:
                            double volume = cell.getVolume();
                            for (int[] loc : locs) {
                                temp[loc[0]][loc[1]] += volume;
                                counter[loc[0]][loc[1]]++;
                            }
                        case HEIGHT:
                            double height = cell.getHeight();
                            for (int[] loc : locs) {
                                temp[loc[0]][loc[1]] += height;
                                counter[loc[0]][loc[1]]++;
                            }
                        default:
                            break;
                    }
                }
            }
    
            switch (view) {
                case VOLUME:
                case HEIGHT:
                    for (int i = 0; i < LENGTH; i++) {
                        for (int j = 0; j < WIDTH; j++) {
                            temp[i][j] /= counter[i][j];
                        }
                    }
                    break;
                default:
                    break;
            }
            
            expand(arr, temp, LENGTH, WIDTH);
        }
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
        
        private ArrayList<PatchLocation> locations;
        
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
            
            if (locations == null) {
                locations = new ArrayList<>();
                PatchSeries series = (PatchSeries) sim.getSeries();
                PatchLocationFactory factory = new PatchLocationFactoryHex();
                ArrayList<int[]> coordinates = factory.getCoordinates(series.radius, series.depth);
                
                for (int[] coordinate : coordinates) {
                    PatchLocationContainer container = new PatchLocationContainer(0, coordinate);
                    PatchLocation location = (PatchLocation) container.convert(factory, null);
                    locations.add(location);
                }
            }
            
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
            
            // Draw hexagonal agent locations.
            for (PatchLocation loc : locations) {
                int[] xy = loc.getLatLocation();
                for (int i = 0; i < 6; i++) {
                    add(field, graph, 2,
                        xy[0] + OFFSETS[i][0], xy[1] + OFFSETS[i][1],
                        xy[0] + OFFSETS[(i + 1)%6][0], xy[1] + OFFSETS[(i + 1)%6][1]);
                }
            }
            
            // Draw border.
            int radius = ((PatchSeries) sim.getSeries()).radius;
            int ind, r;
            for (PatchLocation loc : locations) {
                int[] xy = loc.getLatLocation();
                int[] uvw = loc.getGridLocation();
                
                r = (int)((Math.abs(uvw[0]) + Math.abs(uvw[1]) + Math.abs(uvw[2]))/2.0) + 1;
                
                if (r == radius) {
                    if (uvw[0] == radius - 1) { ind = 1; }
                    else if (uvw[0] == 1 - radius) { ind = 4; }
                    else if (uvw[1] == radius - 1) { ind = 5; }
                    else if (uvw[1] == 1 - radius) { ind = 2; }
                    else if (uvw[2] == radius - 1) { ind = 3; }
                    else if (uvw[2] == 1 - radius) { ind = 0; }
                    else { ind = 0; }
                    
                    add(field, graph, 3,
                        xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
                        xy[0] + OFFSETS[(ind + 1)%6][0], xy[1] + OFFSETS[(ind + 1)%6][1]);
                    add(field, graph, 3,
                        xy[0] + OFFSETS[(ind + 1)%6][0], xy[1] + OFFSETS[(ind + 1)%6][1],
                        xy[0] + OFFSETS[(ind + 2)%6][0], xy[1] + OFFSETS[(ind + 2)%6][1]);
                    
                    if (uvw[0] == 0 || uvw[1] == 0 || uvw[2] == 0) {
                        if (uvw[0] == 0 && uvw[1] == radius - 1) { ind = 1; }
                        else if (uvw[0] == 0 && uvw[2] == radius - 1) { ind = 4; }
                        else if (uvw[1] == 0 && uvw[0] == radius - 1) { ind = 0; }
                        else if (uvw[1] == 0 && uvw[2] == radius - 1) { ind = 3; }
                        else if (uvw[2] == 0 && uvw[0] == radius - 1) { ind = 3; }
                        else if (uvw[2] == 0 && uvw[1] == radius - 1) { ind = 0; }
                        add(field, graph, 3,
                            xy[0] + OFFSETS[ind][0], xy[1] + OFFSETS[ind][1],
                            xy[0] + OFFSETS[(ind + 1)%6][0], xy[1] + OFFSETS[(ind + 1)%6][1]);
                    }
                }
            }
        }
    }
}