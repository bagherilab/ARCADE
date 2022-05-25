package arcade.patch.vis;

import sim.engine.*;
import arcade.core.sim.Simulation;
import arcade.core.sim.Series;
import static arcade.vis.ColorMaps.*;

/** 
 * Extension of {@link arcade.core.vis.Visualization} for 3D simulations.
 * <p>
 * {@code GrowthVisualization3D} creates two panels:
 * <ul>
 *     <li><em>agents</em> shows cell density, averaged across z</li>
 *     <li><em>environment</em> shows the concentrations of the molecules
 *     averaged across z</li>
 * </ul>
 */

public abstract class GrowthVisualization3D extends Visualization {
    /** Length of the lattice (x direction) */
    int LENGTH;
    
    /** Width of the lattice (y direction) */
    int WIDTH;
    
    /** Depth of the lattice (z direction) */
    int DEPTH;
    
    /** Color maps for the visualization */
    final ColorMaps maps;
    
    /**
     * Creates a {@link arcade.core.vis.Visualization} for 3D simulations.
     *
     * @param sim  the simulation instance
     */
    public GrowthVisualization3D(Simulation sim) {
        super((SimState)sim);
        
        // Get Series object and initialize color maps object.
        Series series = sim.getSeries();
        maps = new ColorMaps(series);
    }
    
    /** {@link arcade.vis.GrowthVisualization3D} for hexagonal simulations */
    public static class Hexagonal extends GrowthVisualization3D {
        /**
         * Creates a {@link arcade.vis.GrowthVisualization3D} for hexagonal simulations.
         * <p>
         * Constructor uses the simulation object to determine the sizing of the
         * arrays, which will then be used to rescale into pixel space for display.
         *
         * @param sim  the simulation instance
         */
        public Hexagonal(Simulation sim) {
            super(sim);
            
            Series series = sim.getSeries();
            LENGTH = 6*series._radiusBounds - 3;
            WIDTH = 4*series._radiusBounds - 2;
            DEPTH = 2*series._heightBounds - 1;
        }
        
        public Drawer[] createDrawers() {
            return new Drawer[] {
                    new AgentDrawer3D.Hexagonal(panels[0], "count",
                            LENGTH, WIDTH, DEPTH, MAP_DENSITY, null),
                    new AuxDrawer2D.TriGrid(panels[0], "grid", LENGTH, WIDTH, DEPTH, null),
                    new AuxDrawer2D.Label(panels[0], "label", 0, 0, "CELLS", false),
                    new AuxDrawer2D.Label(panels[0], "label", 0, 94, "", true),
                    new EnvDrawer3D.Triangular(panels[1], "glucose",
                            LENGTH, WIDTH, DEPTH, maps.MAP_GLUC, getBox(0,0,200)),
                    new EnvDrawer2D.Triangular(panels[1], "oxygen",
                            LENGTH, WIDTH, DEPTH, maps.MAP_OXY, getBox(200,0,200)),
                    new EnvDrawer2D.Triangular(panels[1], "tgfa",
                            LENGTH, WIDTH, DEPTH, maps.MAP_TGF, getBox(400,0,200)),
                    new AuxDrawer2D.TriGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(0,0,200)),
                    new AuxDrawer2D.TriGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(200,0,200)),
                    new AuxDrawer2D.TriGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(400,0,200)),
                    new AuxDrawer2D.Label(panels[1], "label", 0, 0, "[GLUCOSE]", false),
                    new AuxDrawer2D.Label(panels[1], "label", 33, 0, "[OXYGEN]", false),
                    new AuxDrawer2D.Label(panels[1], "label", 67, 0, "[TGFα]", false),
            };
        }
    }
    
    /** {@link arcade.vis.GrowthVisualization3D} for rectangular simulations */
    public static class Rectangular extends GrowthVisualization3D {
        /**
         * Creates a {@link arcade.vis.GrowthVisualization3D} for rectangular simulations.
         * <p>
         * Constructor uses the simulation object to determine the sizing of the
         * arrays, which will then be used to rescale into pixel space for display.
         *
         * @param sim  the simulation instance
         */
        public Rectangular(Simulation sim) {
            super(sim);
            
            Series series = sim.getSeries();
            LENGTH = 4*series._radiusBounds - 2;
            WIDTH = 4*series._radiusBounds - 2;
            DEPTH = 2*series._heightBounds - 1;
        }
        
        public Drawer[] createDrawers() {
            return new Drawer[] {
                    new AgentDrawer3D.Rectangular(panels[0], "count",
                            LENGTH, WIDTH, DEPTH, MAP_DENSITY, null),
                    new AuxDrawer2D.RectGrid(panels[0], "grid", LENGTH, WIDTH, DEPTH, null),
                    new AuxDrawer2D.Label(panels[0], "label", 0, 0, "CELLS", false),
                    new AuxDrawer2D.Label(panels[0], "label", 0, 94, "", true),
                    new EnvDrawer3D.Rectangular(panels[1], "glucose",
                            LENGTH, WIDTH, DEPTH, maps.MAP_GLUC, getBox(0,0,200)),
                    new EnvDrawer2D.Rectangular(panels[1], "oxygen",
                            LENGTH, WIDTH, DEPTH, maps.MAP_OXY, getBox(200,0,200)),
                    new EnvDrawer2D.Rectangular(panels[1], "tgfa",
                            LENGTH, WIDTH, DEPTH, maps.MAP_TGF, getBox(400,0,200)),
                    new AuxDrawer2D.RectGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(0,0,200)),
                    new AuxDrawer2D.RectGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(200,0,200)),
                    new AuxDrawer2D.RectGrid(panels[1], "grid", LENGTH, WIDTH, DEPTH, getBox(400,0,200)),
                    new AuxDrawer2D.Label(panels[1], "label", 0, 0, "[GLUCOSE]", false),
                    new AuxDrawer2D.Label(panels[1], "label", 33, 0, "[OXYGEN]", false),
                    new AuxDrawer2D.Label(panels[1], "label", 67, 0, "[TGFα]", false),
            };
        }
    }
    
    public Panel[] createPanels() {
        return new Panel[]{
                new Panel("[3D] Agents", 10, 10, 400, 400, this),
                new Panel("[3D] Environment", 500, 10, 600, 200, this),
        };
    }
}