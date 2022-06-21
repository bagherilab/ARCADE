package arcade.potts.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.DoubleGrid2D;
import sim.field.network.Network;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.util.Double2D;
import sim.util.gui.ColorMap;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.vis.*;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.core.util.Enums.Region;

/**
 * Container for potts-specific {@link Drawer} classes.
 */

public abstract class PottsDrawer extends Drawer {
    /** Array holding values. */
    DoubleGrid2D array;
    
    /** Graph holding edges. */
    Network graph;
    
    /** Field holding nodes. */
    Continuous2D field;
    
    /** Planes for visualization. */
    enum Plane { Z, X, Y }
    
    /** View options. */
    enum View { CYTOPLASM, NUCLEUS, OVERLAY, STATE, POPULATION, VOLUME, HEIGHT }
    
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
    PottsDrawer(Panel panel, String name, int length, int width, int depth,
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
    PottsDrawer(Panel panel, String name, int length, int width, int depth,
                Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }
    
    @Override
    public Portrayal makePort() {
        String[] split = name.split(":");
        
        switch (split[0]) {
            case "grid":
                graph = new Network(true);
                field = new Continuous2D(1.0, 1, 1);
                SimpleEdgePortrayal2D sep = new SimpleEdgePortrayal2DGridWrapper();
                NetworkPortrayal2D gridPort = new NetworkPortrayal2D();
                gridPort.setField(new SpatialNetwork2D(field, graph));
                gridPort.setPortrayalForAll(sep);
                return gridPort;
            case "agents":
                Plane plane = (split.length == 3 ? Plane.valueOf(split[2]) : Plane.Z);
                
                switch (plane) {
                    case X:
                        array = new DoubleGrid2D(height, width, map.defaultValue());
                        break;
                    case Y:
                        array = new DoubleGrid2D(length, height, map.defaultValue());
                        break;
                    case Z: default:
                        array = new DoubleGrid2D(length, width, map.defaultValue());
                        break;
                }
                
                ValueGridPortrayal2D valuePort = new FastValueGridPortrayal2D();
                valuePort.setField(array);
                valuePort.setMap(map);
                return valuePort;
            default:
                return null;
        }
    }
    
    /**
     * Wrapper for MASON class that modifies edge color.
     */
    private static class SimpleEdgePortrayal2DGridWrapper extends SimpleEdgePortrayal2D {
        /** Color for edges. */
        private final Color color = new Color(0, 0, 0);
        
        /**
         * Creates {@code SimpleEdgePortrayal2D} wrapper with no scaling.
         */
        SimpleEdgePortrayal2DGridWrapper() { setScaling(NEVER_SCALE); }
        
        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            shape = SHAPE_THIN_LINE;
            fromPaint = color;
            toPaint = color;
            super.draw(object, graphics, info);
        }
        
        @Override
        protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge) object;
            return (Integer) edge.getInfo();
        }
    }
    
    /**
     * Transposes the array for the given plane.
     *
     * @param array  the array to transpose
     * @param plane  the plane
     * @return  the transposed array
     */
    private static int[][][] transpose(int[][][] array, Plane plane) {
        int height = array.length;
        int length = array[0].length;
        int width = array[0][0].length;
        
        switch (plane) {
            case X:
                int[][][] xarr = new int[length][height][width];
                for (int k = 0; k < height; k++) {
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            xarr[i][k][j] = array[k][i][j];
                        }
                    }
                }
                return xarr;
            case Y:
                int[][][] yarr = new int[width][length][height];
                for (int k = 0; k < height; k++) {
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            yarr[j][i][k] = array[k][i][j];
                        }
                    }
                }
                return yarr;
            case Z: default:
                return array;
        }
    }
    
    /**
     * Gets slice of a array for the given plane.
     *
     * @param array  the array to slice
     * @param plane  the plane to slice along
     * @return  a slice of the array
     */
    private static int[][] slice(int[][][] array, Plane plane) {
        int height = array.length;
        int length = array[0].length;
        int width = array[0][0].length;
        
        switch (plane) {
            case X:
                int[][] planeX = new int[height][width];
                for (int k = 0; k < height; k++) {
                    for (int j = 0; j < width; j++) {
                        planeX[k][j] = array[k][(length - 1) / 2][j];
                    }
                }
                return planeX;
            case Y:
                int[][] planeY = new int[length][height];
                for (int k = 0; k < height; k++) {
                    for (int i = 0; i < length; i++) {
                        planeY[i][k] = array[k][i][(width - 1) / 2];
                    }
                }
                return planeY;
            default: case Z:
                return array[(height - 1) / 2];
        }
    }
    
    /**
     * Adds edges to graph.
     *
     * @param field  the field to add nodes to
     * @param graph  the graph to add edges to
     * @param weight  the edge weight
     * @param x1  the x position of the from node
     * @param y1  the y position of the from node
     * @param x2  the x position of the to node
     * @param y2  the y position of the to node
     */
    private static void add(Continuous2D field, Network graph, int weight,
                            int x1, int y1, int x2, int y2) {
        Double2D a = new Double2D(x1, y1);
        Double2D b = new Double2D(x2, y2);
        field.setObjectLocation(a, a);
        field.setObjectLocation(b, b);
        graph.addEdge(a, b, weight);
    }
    
    /**
     * Extension of {@link PottsDrawer} for drawing cells.
     */
    public static class PottsCells extends PottsDrawer {
        /** Drawing view. */
        private final View view;
        
        /** Drawing plane. */
        private final Plane plane;
        
        /**
         * Creates a {@link PottsDrawer} for drawing cells.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param map  the color map for the array
         * @param bounds  the size of the drawer within the panel
         */
        public PottsCells(Panel panel, String name,
                         int length, int width, int depth,
                         ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, map, bounds);
            String[] split = name.split(":");
            view = View.valueOf(split[1]);
            plane = (split.length == 3 ? Plane.valueOf(split[2]) : Plane.Z);
        }
        
        @Override
        public void step(SimState state) {
            PottsSimulation sim = (PottsSimulation) state;
            Grid grid = sim.getGrid();
            Potts potts = sim.getPotts();
            
            double[][] arr = array.field;
            
            int[][][] ids = transpose(potts.ids, plane);
            int[][][] regions = transpose(potts.regions, plane);
            int index = (ids.length - 1) / 2;
            
            switch (view) {
                case CYTOPLASM:
                    drawCytoplasm(arr, ids);
                    break;
                case NUCLEUS:
                    drawNucleus(arr, ids, regions);
                    break;
                case STATE: case POPULATION: case VOLUME: case HEIGHT:
                    drawSlice(arr, ids[index], grid);
                    break;
                case OVERLAY:
                    drawOverlay(arr, regions[index]);
                default:
                    break;
            }
        }
        
        /**
         * Counts the number of non-target values in the 3x3 neighborhood.
         *
         * @param arr  the array of values
         * @param target  the target value
         * @param a  the position along the first axis
         * @param b  the position along the second axis
         * @return  the count of matching neighbors
         */
        private int count(int[][] arr, int target, int a, int b) {
            int n = 9;
            
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    n -= (arr[a + i - 1][b + j - 1] == target ? 1 : 0);
                }
            }
            
            return n;
        }
        
        /**
         * Draws voxels along edges of cytoplasm.
         *
         * @param arr  the target array
         * @param ids  the array of ids
         */
        private void drawCytoplasm(double[][] arr, int[][][] ids) {
            double normalize = 0;
            
            int cc = ids.length;
            int aa = ids[0].length;
            int bb = ids[0][0].length;
            
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    arr[a][b] = 0;
                    
                    if (height == 1) {
                        int id = ids[0][a][b];
                        if (id == 0) { continue; }
                        arr[a][b] += count(ids[0], id, a, b);
                    } else {
                        for (int c = 1; c < cc - 1; c++) {
                            int id  = ids[c][a][b];
                            if (id == 0) { continue; }
                            arr[a][b] += count(ids[c], id, a, b);
                        }
                    }
                    
                    normalize = Math.max(normalize, arr[a][b]);
                }
            }
            
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    arr[a][b] /= normalize;
                }
            }
        }
        
        /**
         * Draws voxels along edges of nucleus.
         *
         * @param arr  the target array
         * @param ids  the array of ids
         * @param regions  the array of regions
         */
        private void drawNucleus(double[][] arr, int[][][] ids, int[][][] regions) {
            double normalize = 0;
            int nucleus = Region.NUCLEUS.ordinal();
            
            int cc = ids.length;
            int aa = ids[0].length;
            int bb = ids[0][0].length;
            
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    arr[a][b] = 0;
                    
                    if (height == 1) {
                        int id = ids[0][a][b];
                        int region = regions[0][a][b];
                        if (id == 0 || region != nucleus) { continue; }
                        arr[a][b] += count(regions[0], region, a, b);
                    } else {
                        for (int c = 1; c < cc - 1; c++) {
                            int id  = ids[c][a][b];
                            int region = regions[c][a][b];
                            if (id == 0 || region != nucleus) { continue; }
                            arr[a][b] += count(regions[c], region, a, b);
                        }
                    }
                    
                    normalize = Math.max(normalize, arr[a][b]);
                }
            }
            
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    arr[a][b] /= normalize;
                }
            }
        }
    
        /**
         * Draws voxels for region overlay.
         *
         * @param arr  the target array
         * @param regions  the array of regions
         */
        private void drawOverlay(double[][] arr, int[][] regions) {
            int aa = arr.length;
            int bb = arr[0].length;
        
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    arr[a][b] = (regions[a][b] > 0 ? regions[a][b] - 1 : 0);
                }
            }
        }
        
        /**
         * Draws voxels for given slice and view type.
         *
         * @param arr  the target array
         * @param ids  the array of ids
         * @param grid  the grid for cell objects
         */
        private void drawSlice(double[][] arr, int[][] ids, Grid grid) {
            int aa = arr.length;
            int bb = arr[0].length;
            
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    arr[a][b] = 0;
                    
                    if (ids[a][b] == 0) { continue; }
                    
                    Cell cell = (Cell) grid.getObjectAt(ids[a][b]);
                    
                    switch (view) {
                        case STATE:
                            int state = ((PottsModule) cell.getModule()).getPhase().ordinal();
                            arr[a][b] = state;
                            break;
                        case POPULATION:
                            arr[a][b] = cell.getPop();
                            break;
                        case VOLUME:
                            arr[a][b] = cell.getVolume();
                            break;
                        case HEIGHT:
                            arr[a][b] = cell.getHeight();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * Extension of {@link PottsDrawer} for drawing outlines.
     */
    public static class PottsGrid extends PottsDrawer {
        /** Drawing plane. */
        private final Plane plane;
        
        /**
         * Creates a {@link PottsDrawer} for drawing outlines.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param length  the length of array (x direction)
         * @param width  the width of array (y direction)
         * @param depth  the depth of array (z direction)
         * @param bounds  the size of the drawer within the panel
         */
        PottsGrid(Panel panel, String name,
                  int length, int width, int depth, Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, bounds);
            String[] split = name.split(":");
            plane = (split.length == 2 ? Plane.valueOf(split[1]) : Plane.Z);
            
            switch (plane) {
                case X:
                    field.width = depth;
                    field.height = width;
                    break;
                case Y:
                    field.width = length;
                    field.height = depth;
                    break;
                default: case Z:
                    field.width = length;
                    field.height = width;
            }
        }
        
        @Override
        public void step(SimState state) {
            PottsSimulation sim = (PottsSimulation) state;
            field.clear();
            graph.clear();
            
            int[][] arr = slice(sim.getPotts().ids, plane);
            
            int aa = arr.length;
            int bb = arr[0].length;
            
            for (int a = 1; a < aa - 1; a++) {
                for (int b = 1; b < bb - 1; b++) {
                    if (arr[a][b] != arr[a][b + 1]) {
                        add(field, graph, 1, a, b + 1, a + 1, b + 1);
                    }
                    if (arr[a][b] != arr[a + 1][b]) {
                        add(field, graph, 1, a + 1, b, a + 1, b + 1);
                    }
                }
            }
        }
    }
}
