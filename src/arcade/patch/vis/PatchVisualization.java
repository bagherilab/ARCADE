package arcade.patch.vis;

import java.util.ArrayList;
import java.util.Arrays;
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
    
    /** {@code true} if simulation uses graph sites, {@code false} otherwise. */
    final boolean hasGraph;
    
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
        
        // Check simulation sites.
        hasGraph = series.components.get("SITES").get("CLASS").contains("graph");
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
        
        ArrayList<Drawer> drawers = new ArrayList<>(Arrays.asList(
                new PatchDrawerHex.PatchCells(panels[0], "agents:STATE",
                        length, width, height, MAP_STATE, null),
                new PatchDrawerHex.PatchCells(panels[0], "agents:AGE",
                        length, width, height, maps.mapAge, null),
                new PatchDrawerHex.PatchGrid(panels[0], "grid",
                        length, width, height, null),
                new PatchDrawer.Label(panels[0], "label", 0, 0, "CELLS"),
                new PatchDrawer.Label(panels[0], "label", 0, 94, null),
                
                new PatchDrawerHex.PatchLayers(panels[1], "environment:CONCENTRATION:GLUCOSE",
                        length, width, height, maps.mapGlucose, getBox(0, 0, h, v)),
                new PatchDrawerHex.PatchLayers(panels[1], "environment:CONCENTRATION:OXYGEN",
                        length, width, height, maps.mapOxygen, getBox(0, v, h, v)),
                new PatchDrawerHex.PatchLayers(panels[1], "environment:CONCENTRATION:TGFA",
                        length, width, height, maps.mapTGFa, getBox(0, 2 * v, h, v)),
                new PatchDrawer.Label(panels[1], "label", 0, 0, "GLUCOSE"),
                new PatchDrawer.Label(panels[1], "label", 0, 33, "OXYGEN"),
                new PatchDrawer.Label(panels[1], "label", 0, 67, "TGFα"),
                
                new PatchDrawerHex.PatchCells(panels[2], "agents:VOLUME",
                        length, width, height, maps.mapVolume, getBox(0, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:HEIGHT",
                        length, width, height, maps.mapHeight, getBox(h, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:COUNTS",
                        length, width, height, MAP_COUNTS, getBox(2 * h, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:POPULATION",
                        length, width, height, MAP_POPULATION, getBox(3 * h, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:ENERGY",
                        length, width, height, maps.mapEnergy, getBox(4 * h, 0, h, v)),
                new PatchDrawerHex.PatchCells(panels[2], "agents:DIVISIONS",
                        length, width, height, maps.mapDivisions, getBox(5 * h, 0, h, v)),
                new PatchDrawer.Label(panels[2], "label", 0, 0, "VOLUME"),
                new PatchDrawer.Label(panels[2], "label", 17, 0, "HEIGHT"),
                new PatchDrawer.Label(panels[2], "label", 33, 0, "COUNTS"),
                new PatchDrawer.Label(panels[2], "label", 50, 0, "POPULATION"),
                new PatchDrawer.Label(panels[2], "label", 67, 0, "ENERGY"),
                new PatchDrawer.Label(panels[2], "label", 83, 0, "DIVISIONS")));
        
        if (hasGraph) {
            // TODO: add views for nodes (pressure) + edges (radius, wall, type)
        } else {
            drawers.add(new PatchDrawerHex.PatchLayers(panels[1], "environment:SITES",
                    length, width, height, MAP_SITES, getBox(h, 0, h, v)));
            drawers.add(new PatchDrawerHex.PatchLayers(panels[1], "environment:DAMAGE",
                    length, width, height, MAP_DAMAGE, getBox(h, v, h, v)));
            drawers.add(new PatchDrawer.Label(panels[1], "label", 33, 0, "SITES"));
            drawers.add(new PatchDrawer.Label(panels[1], "label", 33, 33, "DAMAGE"));
        }
        
        return drawers.toArray(new Drawer[0]);
    }
    
    /**
     * Creates drawers for visualizing simulations with rectangular geometry.
     *
     * @return  a list of {@link Drawer} instances
     */
    Drawer[] createRectDrawers() {
        int h = 200;
        int v = 200;
        
        ArrayList<Drawer> drawers = new ArrayList<>(Arrays.asList(
                new PatchDrawerRect.PatchCells(panels[0], "agents:STATE",
                        length, width, height, MAP_STATE, null),
                new PatchDrawerRect.PatchCells(panels[0], "agents:AGE",
                        length, width, height, maps.mapAge, null),
                new PatchDrawerRect.PatchGrid(panels[0], "grid",
                        length, width, height, null),
                new PatchDrawer.Label(panels[0], "label", 0, 0, "CELLS"),
                new PatchDrawer.Label(panels[0], "label", 0, 94, null),
                
                new PatchDrawerRect.PatchLayers(panels[1], "environment:CONCENTRATION:GLUCOSE",
                        length, width, height, maps.mapGlucose, getBox(0, 0, h, v)),
                new PatchDrawerRect.PatchLayers(panels[1], "environment:CONCENTRATION:OXYGEN",
                        length, width, height, maps.mapOxygen, getBox(0, v, h, v)),
                new PatchDrawerRect.PatchLayers(panels[1], "environment:CONCENTRATION:TGFA",
                        length, width, height, maps.mapTGFa, getBox(0, 2 * v, h, v)),
                new PatchDrawer.Label(panels[1], "label", 0, 0, "GLUCOSE"),
                new PatchDrawer.Label(panels[1], "label", 0, 33, "OXYGEN"),
                new PatchDrawer.Label(panels[1], "label", 0, 67, "TGFα"),
                
                new PatchDrawerRect.PatchCells(panels[2], "agents:VOLUME",
                        length, width, height, maps.mapVolume, getBox(0, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:HEIGHT",
                        length, width, height, maps.mapHeight, getBox(h, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:COUNTS",
                        length, width, height, MAP_COUNTS, getBox(2 * h, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:POPULATION",
                        length, width, height, MAP_POPULATION, getBox(3 * h, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:ENERGY",
                        length, width, height, maps.mapEnergy, getBox(4 * h, 0, h, v)),
                new PatchDrawerRect.PatchCells(panels[2], "agents:DIVISIONS",
                        length, width, height, maps.mapDivisions, getBox(5 * h, 0, h, v)),
                new PatchDrawer.Label(panels[2], "label", 0, 0, "VOLUME"),
                new PatchDrawer.Label(panels[2], "label", 17, 0, "HEIGHT"),
                new PatchDrawer.Label(panels[2], "label", 33, 0, "COUNTS"),
                new PatchDrawer.Label(panels[2], "label", 50, 0, "POPULATION"),
                new PatchDrawer.Label(panels[2], "label", 67, 0, "ENERGY"),
                new PatchDrawer.Label(panels[2], "label", 83, 0, "DIVISIONS")));
        
        if (hasGraph) {
            // TODO: add views for nodes (pressure) + edges (radius, wall, type)
        } else {
            drawers.add(new PatchDrawerRect.PatchLayers(panels[1], "environment:SITES",
                    length, width, height, MAP_SITES, getBox(h, 0, h, v)));
            drawers.add(new PatchDrawerRect.PatchLayers(panels[1], "environment:DAMAGE",
                    length, width, height, MAP_DAMAGE, getBox(h, v, h, v)));
            drawers.add(new PatchDrawer.Label(panels[1], "label", 33, 0, "SITES"));
            drawers.add(new PatchDrawer.Label(panels[1], "label", 33, 33, "DAMAGE"));
        }
        
        return drawers.toArray(new Drawer[0]);
    }
    
    @Override
    public Panel[] createPanels() {
        return new Panel[] {
                new Panel("[PATCH] Agents", 10, 10, 600, 600, this),
                new Panel("[PATCH] Environment", 700, 10, 600, 600, this),
                new Panel("[PATCH] Auxiliary", 10, 700, 1200, 200, this),
        };
    }
}
