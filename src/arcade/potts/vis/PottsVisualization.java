package arcade.potts.vis;

import sim.engine.*;
import arcade.core.sim.Simulation;
import arcade.core.sim.Series;
import arcade.core.vis.*;
import static arcade.potts.vis.PottsColorMaps.*;

public class PottsVisualization extends Visualization {
    /** Maximum horizontal size of panel */
    static final int MAX_HORIZONTAL = 600;
    
    /** Maximum vertical size of panel */
    static final int MAX_VERTICAL = 900;
    
    /** Length of the array (x direction) */
    final int length;
    
    /** Width of the array (y direction) */
    final int width;
    
    /** Height of the array (z direction) */
    final int height;
    
    /** Color maps for the simulation */
    final PottsColorMaps maps;
    
    /** Horizontal size of the panels */
    final int horizontal;
    
    /** Vertical size of the panels */
    final int vertical;
    
    public PottsVisualization(Simulation sim) {
        super((SimState) sim);
        
        // Update sizes.
        Series series = sim.getSeries();
        length = series.length;
        width = series.width;
        height = series.height;
        
        // Get color maps.
        maps = new PottsColorMaps(series);
        
        // Calculate sizing of panels.
        int horz, vert;
        
        if (length != width) {
            horz = MAX_HORIZONTAL;
            vert = (int) Math.round((width + .0) / length * MAX_HORIZONTAL);
            
            if (vert > MAX_VERTICAL) {
                double frac = (MAX_VERTICAL + .0) / vert;
                horz = (int) Math.round(horz * frac);
                vert = (int) Math.round(vert * frac);
            }
        } else {
            horz = MAX_HORIZONTAL;
            vert = MAX_HORIZONTAL;
        }
        
        horizontal = horz;
        vertical = vert;
    }
    
    public Drawer[] createDrawers() {
        if (height == 1) {
            return create2DDrawers();
        } else {
            return create3DDrawers();
        }
    }
    
    Drawer[] create2DDrawers() {
        int h = horizontal / 2;
        int v = vertical / 2;
        
        return new Drawer[] {
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm",
                        length, width, height, MAP_CYTOPLASM, null),
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus",
                        length, width, height, MAP_NUCLEUS, null),
                new PottsDrawer.PottsGrid(panels[0], "grid",
                        length, width, height, null),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state",
                        length, width, height, MAP_STATE, getBox(0, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:population",
                        length, width, height, MAP_POPULATION, getBox(h, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume",
                        length, width, height, maps.mapVolume, getBox(0, v, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface",
                        length, width, height, maps.mapSurface, getBox(h, v, h, v)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        length, width, height, MAP_OVERLAY, getBox(0, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        length, width, height, MAP_OVERLAY, getBox(h, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        length, width, height, MAP_OVERLAY, getBox(0, v, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        length, width, height, MAP_OVERLAY, getBox(h, v, h, v)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        length, width, height, getBox(0, 0, h, v)),
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        length, width, height, getBox(h, 0, h, v)),
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        length, width, height, getBox(0, v, h, v)),
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        length, width, height, getBox(h, v, h, v)),
        };
    }
    
    Drawer[] create3DDrawers() {
        int h = horizontal / 2;
        int v = vertical / 2;
        
        int hh = (int) Math.round((length + .0) / (length + height) * h);
        int vv = (int) Math.round((width + .0) / (width + height) * v);
        
        int hx = (int) Math.round((height + .0) / (length + height) * h);
        int vx = (int) Math.round((height + .0) / (width + height) * v);
        
        return new Drawer[] {
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm:z",
                        length, width, height, MAP_CYTOPLASM, getBox(0, 0, hh * 2, vv * 2)),
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm:x",
                        length, width, height, MAP_CYTOPLASM, getBox(hh * 2, 0, hx * 2, vv * 2)),
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm:y",
                        length, width, height, MAP_CYTOPLASM, getBox(0, vv * 2, hh * 2, vx * 2)),
                
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus:z",
                        length, width, height, MAP_NUCLEUS, getBox(0, 0, hh * 2, vv * 2)),
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus:x",
                        length, width, height, MAP_NUCLEUS, getBox(hh * 2, 0, hx * 2, vv * 2)),
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus:y",
                        length, width, height, MAP_NUCLEUS, getBox(0, vv * 2, hh * 2, vx * 2)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state:z",
                        length, width, height, MAP_STATE, getBox(0, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:population:z",
                        length, width, height, MAP_POPULATION, getBox(h, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume:z",
                        length, width, height, maps.mapVolume, getBox(0, v, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface:z",
                        length, width, height, maps.mapSurface, getBox(h, v, hh, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        length, width, height, MAP_OVERLAY, getBox(0, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        length, width, height, MAP_OVERLAY, getBox(h, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        length, width, height, MAP_OVERLAY, getBox(0, v, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        length, width, height, MAP_OVERLAY, getBox(h, v, hh, vv)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        length, width, height, getBox(0, 0, hh, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        length, width, height, getBox(h, 0, hh, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        length, width, height, getBox(0, v, hh, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        length, width, height, getBox(h, v, hh, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state:x",
                        length, width, height, MAP_STATE, getBox(hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:population:x",
                        length, width, height, MAP_POPULATION, getBox(h + hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume:x",
                        length, width, height, maps.mapVolume, getBox(hh, v, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface:x",
                        length, width, height, maps.mapSurface, getBox(h + hh, v, hx, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        length, width, height, MAP_OVERLAY, getBox(hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        length, width, height, MAP_OVERLAY, getBox(h + hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        length, width, height, MAP_OVERLAY, getBox(hh, v, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        length, width, height, MAP_OVERLAY, getBox(h + hh, v, hx, vv)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        length, width, height, getBox(hh, 0, hx, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        length, width, height, getBox(h + hh, 0, hx, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        length, width, height, getBox(hh, v, hx, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        length, width, height, getBox(h + hh, v, hx, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state:y",
                        length, width, height, MAP_STATE, getBox(0, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:population:y",
                        length, width, height, MAP_POPULATION, getBox(h, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume:y",
                        length, width, height, maps.mapVolume, getBox(0, v + vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface:y",
                        length, width, height, maps.mapSurface, getBox(h, v + vv, hh, vx)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        length, width, height, MAP_OVERLAY, getBox(0, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        length, width, height, MAP_OVERLAY, getBox(h, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        length, width, height, MAP_OVERLAY, getBox(0, v + vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        length, width, height, MAP_OVERLAY, getBox(h, v + vv, hh, vx)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        length, width, height, getBox(0, vv, hh, vx)),
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        length, width, height, getBox(h, vv, hh, vx)),
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        length, width, height, getBox(0, v + vv, hh, vx)),
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        length, width, height, getBox(h, v + vv, hh, vx)),
        };
    }
    
    public Panel[] createPanels() {
        return new Panel[]{
                new Panel.Panel2D("POTTS", 100, 50, horizontal, vertical, this),
                new Panel.Panel2D("POTTS", horizontal + 120, 50, horizontal, vertical, this),
        };
    }
}
