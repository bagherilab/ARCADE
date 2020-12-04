package arcade.potts.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import arcade.core.vis.Panel;
import sim.engine.*;
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
import arcade.potts.sim.Potts;
import arcade.core.agent.cell.Cell;
import arcade.core.env.grid.Grid;
import arcade.core.vis.*;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.sim.PottsSimulation;
import static arcade.core.util.Enums.Region;

public abstract class PottsDrawer extends Drawer {
    DoubleGrid2D array;
    Network graph;
    Continuous2D field;
    
    PottsDrawer(Panel panel, String name,
            int length, int width, int depth,
            ColorMap map, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }
    
    PottsDrawer(Panel panel, String name,
            int length, int width, int depth, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }
    
    public Portrayal makePort() {
        String[] split = name.split(":");
        
        switch(split[0]) {
            case "grid":
                graph = new Network(true);
                field = new Continuous2D(1.0,1,1);
                SimpleEdgePortrayal2D sep = new SimpleEdgePortrayal2DGridWrapper();
                NetworkPortrayal2D gridPort = new NetworkPortrayal2D();
                gridPort.setField(new SpatialNetwork2D(field, graph));
                gridPort.setPortrayalForAll(sep);
                return gridPort;
            case "agents":
                String plane = "";
                if (split.length == 3) { plane = split[2]; }
                
                switch (plane) {
                    case "x":
                        array = new DoubleGrid2D(height, width, map.defaultValue());
                        break;
                    case "y":
                        array = new DoubleGrid2D(length, height, map.defaultValue());
                        break;
                    case "z": default:
                        array = new DoubleGrid2D(length, width, map.defaultValue());
                        break;
                }
                
                ValueGridPortrayal2D valuePort = new FastValueGridPortrayal2D();
                valuePort.setField(array);
                valuePort.setMap(map);
                return valuePort;
        }
        
        return null;
    }
    
    private static class SimpleEdgePortrayal2DGridWrapper extends SimpleEdgePortrayal2D {
        private final Color color = new Color(0, 0, 0);
        
        SimpleEdgePortrayal2DGridWrapper() { setScaling(NEVER_SCALE); }
        
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            shape = SHAPE_THIN_LINE;
            fromPaint = color;
            toPaint = color;
            super.draw(object, graphics, info);
        }
        
        protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge)object;
            return (Integer)edge.getInfo();
        }
    }
    
    private static void add(Continuous2D field, Network graph, int weight,
                            int x1, int y1, int x2, int y2) {
        Double2D a = new Double2D(x1, y1);
        Double2D b = new Double2D(x2, y2);
        field.setObjectLocation(a, a);
        field.setObjectLocation(b, b);
        graph.addEdge(a, b, weight);
    }
    
    static final int PLANE_Z = 0;
    static final int PLANE_X = 1;
    static final int PLANE_Y = 2;
    
    static int[][] getSlice(int[][][] array, int plane, int length, int width, int height) {
        switch(plane) {
            case PLANE_X:
                int[][] planex = new int[height][width];
                for (int k = 0; k < height; k++) {
                    for (int j = 0; j < width; j++) {
                        planex[k][j] = array[k][(length - 1)/2][j];
                    }
                }
                return planex;
            case PLANE_Y:
                int[][] planey = new int[length][height];
                for (int k = 0; k < height; k++) {
                    for (int i = 0; i < length; i++) {
                        planey[i][k] = array[k][i][(width - 1)/2];
                    }
                }
                return planey;
            default: case PLANE_Z:
                return array[(height - 1)/2];
        }
    }
    
    public static class PottsCells extends PottsDrawer {
        private static final int DRAW_CYTOPLASM = -1;
        private static final int DRAW_NUCLEUS = -2;
        private static final int DRAW_OVERLAY = 1;
        private static final int DRAW_POPULATION = 2;
        private static final int DRAW_STATE = 3;
        private static final int DRAW_VOLUME = 4;
        private static final int DRAW_SURFACE = 5;
        
        private final int LENGTH;
        private final int WIDTH;
        private final int HEIGHT;
        private final int CODE;
        private final int PLANE;
        
        public PottsCells(Panel panel, String name,
                         int length, int width, int depth,
                         ColorMap map, Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, map, bounds);
            LENGTH = length;
            WIDTH = width;
            HEIGHT = depth;
            
            String[] split = name.split(":");
            
            switch (split[1]) {
                case "cytoplasm": CODE = DRAW_CYTOPLASM; break;
                case "nucleus": CODE = DRAW_NUCLEUS; break;
                case "overlay": CODE = DRAW_OVERLAY; break;
                case "state": CODE = DRAW_STATE; break;
                case "population": CODE = DRAW_POPULATION; break;
                case "volume": CODE = DRAW_VOLUME; break;
                case "surface": CODE = DRAW_SURFACE; break;
                default: CODE = 0;
            }
            
            if (split.length == 3) {
                switch (split[2]) {
                    case "x": PLANE = PLANE_X; break;
                    case "y": PLANE = PLANE_Y; break;
                    case "z":  default: PLANE = PLANE_Z;
                }
            } else { PLANE = PLANE_Z; }
        }
        
        public void step(SimState state) {
            PottsSimulation sim = (PottsSimulation)state;
            Grid grid = sim.getGrid();
            Potts potts = sim.getPotts();
            Cell cell;
            
            double[][] to = array.field;
            int[][] ids = getSlice(potts.IDS, PLANE, LENGTH, WIDTH, HEIGHT);
            int[][] regions = getSlice(potts.REGIONS, PLANE, LENGTH, WIDTH, HEIGHT);
            
            int aa, bb, cc;
            switch(PLANE) {
                case PLANE_X:
                    aa = HEIGHT;
                    bb = WIDTH;
                    cc = LENGTH;
                    break;
                case PLANE_Y:
                    aa = LENGTH;
                    bb = HEIGHT;
                    cc = WIDTH;
                    break;
                default: case PLANE_Z:
                    aa = LENGTH;
                    bb = WIDTH;
                    cc = HEIGHT;
            }
            
            for (int a = 0; a < aa; a++) {
                for (int b = 0; b < bb; b++) {
                    if (ids[a][b] == 0) { cell = null; }
                    else { cell = (Cell)grid.getObjectAt(ids[a][b]); }
                    
                    switch(CODE) {
                        case DRAW_OVERLAY:
                            to[a][b] = (regions[a][b] > 0 ? regions[a][b] - 1 : 0);
                            break;
                        case DRAW_POPULATION:
                            to[a][b] = cell == null ? 0 : cell.getPop();
                            break;
                        case DRAW_STATE:
                            to[a][b] = cell == null ? 0 :
                                    ((PottsModule)cell.getModule()).getPhase().ordinal();
                            break;
                        case DRAW_VOLUME:
                            to[a][b] = cell == null ? 0 : cell.getVolume();
                            break;
                        case DRAW_SURFACE:
                            to[a][b] = cell == null ? 0 : cell.getSurface();
                            break;
                    }
                }
            }
            
            if (CODE == DRAW_CYTOPLASM) {
                for (int a = 0; a < aa; a++) {
                    for (int b = 0; b < bb; b++) {
                        if (HEIGHT == 1) { to[a][b] = ids[a][b] > 0 ? 0.75 : 0; }
                        else {
                            to[a][b] = 0;
                            for (int c = 0; c < cc; c++) {
                                int id;
                                
                                switch (PLANE) {
                                    case PLANE_X:
                                        id = potts.IDS[a][c][b];
                                        break;
                                    case PLANE_Y:
                                        id = potts.IDS[b][a][c];
                                        break;
                                    default: case PLANE_Z:
                                        id = potts.IDS[c][a][b];
                                }
                                
                                to[a][b] += (id > 0 ? 1./cc : 0);
                                
                                switch (PLANE) {
                                    case PLANE_X:
                                        if (id != 0 && c > 0 && c < cc - 1
                                                && potts.IDS[a][c + 1][b] == id
                                                && potts.IDS[a][c - 1][b] == id
                                        ) { to[a][b] -= 1./cc; }
                                        break;
                                    case PLANE_Y:
                                        if (id != 0 && c > 0 && c < cc - 1
                                                && potts.IDS[b][a][c + 1] == id
                                                && potts.IDS[b][a][c - 1] == id
                                        ) { to[a][b] -= 1./cc; }
                                        break;
                                    default: case PLANE_Z:
                                        if (id != 0 && c > 0 && c < cc - 1
                                                && potts.IDS[c + 1][a][b] == id
                                                && potts.IDS[c - 1][a][b] == id
                                        ) { to[a][b] -= 1./cc; }
                                }
                            }
                        }
                    }
                }
            }
            else if (CODE == DRAW_NUCLEUS) {
                int nucleus = Region.NUCLEUS.ordinal();
                for (int a = 0; a < aa; a++) {
                    for (int b = 0; b < bb; b++) {
                        if (HEIGHT == 1) { to[a][b] = (regions[a][b] == nucleus ? 0.75 : 0); }
                        else {
                            to[a][b] = 0;
                            for (int c = 0; c < cc; c++) {
                                int id;
                                int region;
                                
                                switch (PLANE) {
                                    case PLANE_X:
                                        id = potts.IDS[a][c][b];
                                        region = potts.REGIONS[a][c][b];
                                        break;
                                    case PLANE_Y:
                                        id = potts.IDS[b][a][c];
                                        region = potts.REGIONS[b][a][c];
                                        break;
                                    default: case PLANE_Z:
                                        id = potts.IDS[c][a][b];
                                        region = potts.REGIONS[c][a][b];
                                }
                                
                                to[a][b] += (region == Region.NUCLEUS.ordinal() ? 1./cc : 0);
                                
                                switch (PLANE) {
                                    case PLANE_X:
                                        if (id != 0 && c > 0 && c < cc - 1
                                                && potts.REGIONS[a][c + 1][b] == nucleus
                                                && potts.REGIONS[a][c - 1][b] == nucleus
                                        ) { to[a][b] -= 1./cc; }
                                        break;
                                    case PLANE_Y:
                                        if (id != 0 && c > 0 && c < cc - 1
                                                && potts.REGIONS[b][a][c + 1] == nucleus
                                                && potts.REGIONS[b][a][c - 1] == nucleus
                                        ) { to[a][b] -= 1./cc; }
                                        break;
                                    default: case PLANE_Z:
                                        if (id != 0 && c > 0 && c < cc - 1
                                                && potts.REGIONS[c + 1][a][b] == nucleus
                                                && potts.REGIONS[c - 1][a][b] == nucleus
                                        ) { to[a][b] -= 1./cc; }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static class PottsGrid extends PottsDrawer {
        private final int LENGTH;
        private final int WIDTH;
        private final int HEIGHT;
        private final int PLANE;
        
        PottsGrid(Panel panel, String name,
                  int length, int width, int depth, Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, bounds);
            LENGTH = length;
            WIDTH = width;
            HEIGHT = height;
            
            String[] split = name.split(":");
            
            if (split.length == 2) {
                switch (split[1]) {
                    case "x": PLANE = PLANE_X; break;
                    case "y": PLANE = PLANE_Y; break;
                    case "z":  default: PLANE = PLANE_Z;
                }
            } else { PLANE = PLANE_Z; }
            
            switch(PLANE) {
                case PLANE_X:
                    field.width = HEIGHT;
                    field.height = WIDTH;
                    break;
                case PLANE_Y:
                    field.width = LENGTH;
                    field.height = HEIGHT;
                    break;
                default: case PLANE_Z:
                    field.width = LENGTH;
                    field.height = WIDTH;
            }
        }
        
        public void step(SimState state) {
            PottsSimulation sim = (PottsSimulation)state;
            field.clear();
            graph.clear();
            
            int[][] ids = getSlice(sim.getPotts().IDS, PLANE, LENGTH, WIDTH, HEIGHT);
            
            int aa, bb;
            switch(PLANE) {
                case PLANE_X:
                    aa = HEIGHT;
                    bb = WIDTH;
                    break;
                case PLANE_Y:
                    aa = LENGTH;
                    bb = HEIGHT;
                    break;
                default: case PLANE_Z:
                    aa = LENGTH;
                    bb = WIDTH;
            }
            
            for (int a = 0; a < aa - 1; a++) {
                for (int b = 0; b < bb - 1; b++) {
                    if (ids[a][b] != ids[a][b + 1]) {
                        add(field, graph, 1, a, b + 1, a + 1, b + 1);
                    }
                    if (ids[a][b] != ids[a + 1][b]) {
                        add(field, graph, 1, a + 1, b, a + 1, b + 1);
                    }
                }
            }
        }
    }
}
