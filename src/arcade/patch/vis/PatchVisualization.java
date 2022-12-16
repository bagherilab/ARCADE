package arcade.patch.vis;

import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.vis.*;
import static arcade.patch.vis.PatchColorMaps.*;

/**
 * Extension of {@link Visualization} for patch models.
 */

public final class PatchVisualization extends Visualization {
     /** Length of the array (x direction). */
     final int length;
     
     /** Width of the array (y direction). */
     final int width;
     
     /** Height of the array (z direction). */
     final int height;
     
     /** Color maps for the simulation. */
     final PatchColorMaps maps;
     
    /**
     * Creates a {@link Visualization} for patch simulations.
     *
     * @param sim  the simulation instance
     */
    public PatchVisualization(Simulation sim) {
        super((SimState) sim);
        
         // Update sizes.
         Series series = sim.getSeries();
         length = series.length;
         width = series.width;
         height = series.height;
         
         // Get color maps.
         maps = new PatchColorMaps(series);
    }
    
    @Override
    public Drawer[] createDrawers() {
         if (length == width) {
            return createRectDrawers();
         } else {
            return createHexDrawers();
         }
    }
    
    /**
     * Creates drawers for visualizing simulations with hexagonal geometry.
     *
     * @return  a list of {@link Drawer} instances
     */
    Drawer[] createHexDrawers() {
        int h = 200;
        int v = 200;
        
        return new Drawer[] {
                new PatchDrawerHex.PatchCells(panels[0], "agents:STATE",
                        length, width, height, MAP_STATE, null),
                new PatchDrawerHex.PatchCells(panels[0], "agents:AGE",
                        length, width, height, maps.mapAge, null),
                new PatchDrawerHex.PatchGrid(panels[0], "grid",
                        length, width, height, null),
                new PatchDrawer.Label(panels[0], "label", 0, 0, "CELLS"),
                new PatchDrawer.Label(panels[0], "label", 0, 94, null),
                
                new PatchDrawerHex.PatchGrid(panels[1], "grid",
                        length, width, height, getBox(h, 0, h, v)),
                new PatchDrawerHex.PatchGrid(panels[1], "grid",
                        length, width, height, getBox(h, v, h, v)),
                new PatchDrawerHex.PatchGrid(panels[1], "grid",
                        length, width, height, getBox(h, 2 * v, h, v)),
                new PatchDrawer.Label(panels[1], "label", 0, 0, "[GLUCOSE]"),
                new PatchDrawer.Label(panels[1], "label", 0, 33, "[OXYGEN]"),
                new PatchDrawer.Label(panels[1], "label", 0, 67, "[TGFα]"),
                new PatchDrawer.Label(panels[1], "label", 33, 0, "Δ GLUCOSE"),
                new PatchDrawer.Label(panels[1], "label", 33, 33, "Δ OXYGEN"),
                new PatchDrawer.Label(panels[1], "label", 33, 67, "SITES"),
                new PatchDrawer.Label(panels[1], "label", 67, 0, "( RADIUS + TYPE )"),
                new PatchDrawer.Label(panels[1], "label", 67, 33, "( PRESSURE + SHEAR )"),
                new PatchDrawer.Label(panels[1], "label", 67, 67, "( FLOW + PERFUSION )"),
                
                new PatchDrawerHex.PatchCells(panels[2], "agents:VOLUME",
                        length, width, height, maps.mapVolume, getBox(0, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:HEIGHT",
                        length, width, height, maps.mapHeight, getBox(h, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:COUNTS",
                        length, width, height, MAP_COUNTS, getBox(0, v, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:POPULATION",
                        length, width, height, MAP_POPULATION, getBox(h, v, h, v)),
                new PatchDrawer.Label(panels[2], "label", 0, 0, "VOLUME"),
                new PatchDrawer.Label(panels[2], "label", 50, 0, "HEIGHT"),
                new PatchDrawer.Label(panels[2], "label", 0, 50, "COUNTS"),
                new PatchDrawer.Label(panels[2], "label", 50, 50, "POPULATION"),
        };
    }
    
    /**
     * Creates drawers for visualizing simulations with rectangular geometry.
     *
     * @return  a list of {@link Drawer} instances
     */
    Drawer[] createRectDrawers() {
        int h = 200;
        int v = 200;
        
        return new Drawer[] {
                new PatchDrawerRect.PatchCells(panels[0], "agents:STATE",
                        length, width, height, MAP_STATE, null),
                new PatchDrawerRect.PatchCells(panels[0], "agents:AGE",
                        length, width, height, maps.mapAge, null),
                new PatchDrawerRect.PatchGrid(panels[0], "grid",
                        length, width, height, null),
                new PatchDrawer.Label(panels[0], "label", 0, 0, "CELLS"),
                new PatchDrawer.Label(panels[0], "label", 0, 94, null),
                
                new PatchDrawerRect.PatchGrid(panels[1], "grid",
                        length, width, height, getBox(h, 0, h, v)),
                new PatchDrawerRect.PatchGrid(panels[1], "grid",
                        length, width, height, getBox(h, v, h, v)),
                new PatchDrawerRect.PatchGrid(panels[1], "grid",
                        length, width, height, getBox(h, 2 * v, h, v)),
                new PatchDrawer.Label(panels[1], "label", 0, 0, "[GLUCOSE]"),
                new PatchDrawer.Label(panels[1], "label", 0, 33, "[OXYGEN]"),
                new PatchDrawer.Label(panels[1], "label", 0, 67, "[TGFα]"),
                new PatchDrawer.Label(panels[1], "label", 33, 0, "Δ GLUCOSE"),
                new PatchDrawer.Label(panels[1], "label", 33, 33, "Δ OXYGEN"),
                new PatchDrawer.Label(panels[1], "label", 33, 67, "SITES"),
                new PatchDrawer.Label(panels[1], "label", 67, 0, "( RADIUS + TYPE )"),
                new PatchDrawer.Label(panels[1], "label", 67, 33, "( PRESSURE + SHEAR )"),
                new PatchDrawer.Label(panels[1], "label", 67, 67, "( FLOW + PERFUSION )"),
                
                new PatchDrawerRect.PatchCells(panels[2], "agents:VOLUME",
                        length, width, height, maps.mapVolume, getBox(0, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:HEIGHT",
                        length, width, height, maps.mapHeight, getBox(h, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:COUNTS",
                        length, width, height, MAP_COUNTS, getBox(0, v, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:POPULATION",
                        length, width, height, MAP_POPULATION, getBox(h, v, h, v)),
                new PatchDrawer.Label(panels[2], "label", 0, 0, "VOLUME"),
                new PatchDrawer.Label(panels[2], "label", 50, 0, "HEIGHT"),
                new PatchDrawer.Label(panels[2], "label", 0, 50, "COUNTS"),
                new PatchDrawer.Label(panels[2], "label", 50, 50, "POPULATION"),
        };
    }
    
    @Override
    public Panel[] createPanels() {
        return new Panel[] {
            new Panel("[PATCH] Agents", 10, 10, 400, 400, this),
            new Panel("[PATCH] Environment", 500, 10, 600, 600, this),
            new Panel("[PATCH] Auxiliary", 10, 500, 400, 400, this),
        };
    }
}
