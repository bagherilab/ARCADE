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
    final int LENGTH;
    
    /** Width of the array (y direction) */
    final int WIDTH;
    
    /** Height of the array (z direction) */
    final int HEIGHT;
    
    /** Color maps for the simulation */
    final PottsColorMaps maps;
    
    /** Horizontal size of the panels */
    final int HORIZONTAL;
    
    /** Vertical size of the panels */
    final int VERTICAL;
    
    public PottsVisualization(Simulation sim) {
        super((SimState)sim);
        
        // Update sizes.
        Series series = sim.getSeries();
        LENGTH = series._length;
        WIDTH = series._width;
        HEIGHT = series._height;
        
        // Get color maps.
        maps = new PottsColorMaps(series);
        
        // Calculate sizing of panels.
        int horizontal, vertical;
        
        if (LENGTH != WIDTH) {
            horizontal = MAX_HORIZONTAL;
            vertical = (int)Math.round((WIDTH + .0)/LENGTH*MAX_HORIZONTAL);
            
            if (vertical > MAX_VERTICAL) {
                double frac = (MAX_VERTICAL + .0)/vertical;
                horizontal = (int)Math.round(horizontal * frac);
                vertical = (int)Math.round(vertical * frac);
            }
        } else {
            horizontal = MAX_HORIZONTAL;
            vertical = MAX_HORIZONTAL;
        }
    
        HORIZONTAL = horizontal;
        VERTICAL = vertical;
    }
    
    public Drawer[] createDrawers() {
        if (HEIGHT == 1) { return create2DDrawers(); }
        else { return create3DDrawers(); }
    }
    
    Drawer[] create2DDrawers() {
        int h = HORIZONTAL/2;
        int v = VERTICAL/2;
        
        return new Drawer[] {
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm",
                        LENGTH, WIDTH, HEIGHT, MAP_CYTOPLASM, null),
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus",
                        LENGTH, WIDTH, HEIGHT, MAP_NUCLEUS, null),
                new PottsDrawer.PottsGrid(panels[0], "grid",
                        LENGTH, WIDTH, HEIGHT, null),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state",
                        LENGTH, WIDTH, HEIGHT, MAP_STATE, getBox(0, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:population",
                        LENGTH, WIDTH, HEIGHT, MAP_POPULATION, getBox(h, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_VOLUME, getBox(0, v, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_SURFACE, getBox(h, v, h, v)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(0, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h, 0, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(0, v, h, v)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h, v, h, v)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        LENGTH, WIDTH, HEIGHT, getBox(0, 0, h, v)),
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        LENGTH, WIDTH, HEIGHT, getBox(h, 0, h, v)),
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        LENGTH, WIDTH, HEIGHT, getBox(0, v, h, v)),
                new PottsDrawer.PottsGrid(panels[1], "grid",
                        LENGTH, WIDTH, HEIGHT, getBox(h, v, h, v)),
        };
    }
    
    Drawer[] create3DDrawers() {
        int h = HORIZONTAL/2;
        int v = VERTICAL/2;
        
        int hh = (int)Math.round((LENGTH + .0)/(LENGTH + HEIGHT)*h);
        int vv = (int)Math.round((WIDTH + .0)/(WIDTH + HEIGHT)*v);
        
        int hx = (int)Math.round((HEIGHT + .0)/(LENGTH + HEIGHT)*h);
        int vx = (int)Math.round((HEIGHT + .0)/(WIDTH + HEIGHT)*v);
        
        return new Drawer[] {
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm:z",
                        LENGTH, WIDTH, HEIGHT, MAP_CYTOPLASM, getBox(0, 0, hh*2, vv*2)),
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm:x",
                        LENGTH, WIDTH, HEIGHT, MAP_CYTOPLASM, getBox(hh*2, 0, hx*2, vv*2)),
                new PottsDrawer.PottsCells(panels[0], "agents:cytoplasm:y",
                        LENGTH, WIDTH, HEIGHT, MAP_CYTOPLASM, getBox(0, vv*2, hh*2, vx*2)),
                
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus:z",
                        LENGTH, WIDTH, HEIGHT, MAP_NUCLEUS, getBox(0, 0, hh*2, vv*2)),
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus:x",
                        LENGTH, WIDTH, HEIGHT, MAP_NUCLEUS, getBox(hh*2, 0, hx*2, vv*2)),
                new PottsDrawer.PottsCells(panels[0], "agents:nucleus:y",
                        LENGTH, WIDTH, HEIGHT, MAP_NUCLEUS, getBox(0, vv*2, hh*2, vx*2)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state:z",
                        LENGTH, WIDTH, HEIGHT, MAP_STATE, getBox(0, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:population:z",
                        LENGTH, WIDTH, HEIGHT, MAP_POPULATION, getBox(h, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume:z",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_VOLUME, getBox(0, v, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface:z",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_SURFACE, getBox(h, v, hh, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(0, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h, 0, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(0, v, hh, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:z",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h, v, hh, vv)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        LENGTH, WIDTH, HEIGHT, getBox(0, 0, hh, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        LENGTH, WIDTH, HEIGHT, getBox(h, 0, hh, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        LENGTH, WIDTH, HEIGHT, getBox(0, v, hh, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:z",
                        LENGTH, WIDTH, HEIGHT, getBox(h, v, hh, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state:x",
                        LENGTH, WIDTH, HEIGHT, MAP_STATE, getBox(hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:population:x",
                        LENGTH, WIDTH, HEIGHT, MAP_POPULATION, getBox(h + hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume:x",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_VOLUME, getBox(hh, v, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface:x",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_SURFACE, getBox(h + hh, v, hx, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h + hh, 0, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(hh, v, hx, vv)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:x",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h + hh, v, hx, vv)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        LENGTH, WIDTH, HEIGHT, getBox(hh, 0, hx, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        LENGTH, WIDTH, HEIGHT, getBox(h + hh, 0, hx, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        LENGTH, WIDTH, HEIGHT, getBox(hh, v, hx, vv)),
                new PottsDrawer.PottsGrid(panels[1], "grid:x",
                        LENGTH, WIDTH, HEIGHT, getBox(h + hh, v, hx, vv)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:state:y",
                        LENGTH, WIDTH, HEIGHT, MAP_STATE, getBox(0, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:population:y",
                        LENGTH, WIDTH, HEIGHT, MAP_POPULATION, getBox(h, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:volume:y",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_VOLUME, getBox(0, v + vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:surface:y",
                        LENGTH, WIDTH, HEIGHT, maps.MAP_SURFACE, getBox(h, v + vv, hh, vx)),
                
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(0, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h, vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(0, v + vv, hh, vx)),
                new PottsDrawer.PottsCells(panels[1], "agents:overlay:y",
                        LENGTH, WIDTH, HEIGHT, MAP_OVERLAY, getBox(h, v + vv, hh, vx)),
                
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        LENGTH, WIDTH, HEIGHT, getBox(0, vv, hh, vx)),
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        LENGTH, WIDTH, HEIGHT, getBox(h, vv, hh, vx)),
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        LENGTH, WIDTH, HEIGHT, getBox(0, v + vv, hh, vx)),
                new PottsDrawer.PottsGrid(panels[1], "grid:y",
                        LENGTH, WIDTH, HEIGHT, getBox(h, v + vv, hh, vx)),
        };
    }
    
    public Panel[] createPanels() {
        return new Panel[]{
                new Panel.Panel2D("POTTS", 100, 50, HORIZONTAL, VERTICAL, this),
                new Panel.Panel2D("POTTS", HORIZONTAL + 120, 50, HORIZONTAL, VERTICAL, this),
        };
    }
}
