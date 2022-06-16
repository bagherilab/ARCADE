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
import arcade.patch.env.loc.PatchLocationFactoryRect;
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
            super(panel, name, length, width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            String[] split = name.split(":");
            view = View.valueOf(split[1]);
        }
        
        @Override
        public void step(SimState state) {
            PatchSimulation sim = (PatchSimulation) state;
            double[][] arr = array.field;
            double[][] counter = new double[LENGTH][WIDTH];
            
            PatchCell cell;
            PatchLocation location;
            
            switch (view) {
                case STATE: case AGE:
                    for (int i = 0; i < LENGTH; i++) {
                        for (int j = 0; j < WIDTH; j++) {
                            arr[i][j] = map.defaultValue();
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
                            arr[locs[position][0]][locs[position][1]] = cell.getState().ordinal();
                            break;
                        case AGE:
                            arr[locs[position][0]][locs[position][1]] = cell.getAge();
                            break;
                        case COUNTS:
                            for (int[] loc : locs) {
                                arr[loc[0]][loc[1]]++;
                            }
                            break;
                        case VOLUME:
                            double volume = cell.getVolume();
                            for (int[] loc : locs) {
                                arr[loc[0]][loc[1]] += volume;
                                counter[loc[0]][loc[1]]++;
                            }
                        case HEIGHT:
                            double height = cell.getHeight();
                            for (int[] loc : locs) {
                                arr[loc[0]][loc[1]] += height;
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
                            arr[i][j] /= counter[i][j];
                        }
                    }
                    break;
                default:
                    break;
            }
        }
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
    
        private ArrayList<PatchLocation> locations;
        
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
            
            if (locations == null) {
                locations = new ArrayList<>();
                PatchSeries series = (PatchSeries) sim.getSeries();
                PatchLocationFactory factory = new PatchLocationFactoryRect();
                ArrayList<int[]> coordinates = factory.getCoordinates(series.radius, series.depth);
                
                for (int[] coordinate : coordinates) {
                    PatchLocationContainer container = new PatchLocationContainer(0, coordinate);
                    PatchLocation location = (PatchLocation) container.convert(factory, null);
                    locations.add(location);
                }
            }
            
            // Draw rectangular grid.
            for (int i = 0; i <= WIDTH; i++) { add(field, graph, 1, 0, i, LENGTH, i); }
            for (int i = 0; i <= LENGTH; i++) { add(field, graph, 1, i, 0, i, WIDTH); }
            
            // Draw rectangular agent locations.
            for (PatchLocation loc : locations) {
                int[] xy = loc.getLatLocation();
                for (int i = 0; i < 4; i++) {
                    add(field, graph, 2,
                        xy[0] + OFFSETS[i][0], xy[1] + OFFSETS[i][1],
                        xy[0] + OFFSETS[(i + 1)%4][0], xy[1] + OFFSETS[(i + 1)%4][1]);
                }
            }
            
            // Draw border.
            int radius = ((PatchSeries) sim.getSeries()).radius;
            int ind, r;
            for (PatchLocation loc : locations) {
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