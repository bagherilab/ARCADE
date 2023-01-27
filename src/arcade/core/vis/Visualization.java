package arcade.core.vis;

import java.awt.geom.Rectangle2D;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.Inspector;

/**
 * Extension of {@code GUIState} wrapper of simulations for visualization.
 * <p>
 * {@code Visualization} organizes the visualization into
 * {@link arcade.core.vis.Panel} objects, on which visualizations are drawn, and
 * {@link arcade.core.vis.Drawer} objects, which draw the visualization through
 * methods provided by the
 * <a href="https://cs.gmu.edu/~eclab/projects/mason/">MASON</a> library.
 */

public abstract class Visualization extends GUIState {
    /** List of panels in the visualization. */
    protected Panel[] panels;
    
    /** List of drawers in the visualization. */
    protected Drawer[] drawers;
    
    /**
     * Creates a {@code Visualization} for the given simulation.
     *
     * @param state  the simulation state instance
     */
    protected Visualization(SimState state) { super(state); }
    
    /**
     * Sets the inspector to get state property methods.
     *
     * @return  the MASON simulation state
     */
    @Override
    public Object getSimulationInspectedObject() { return state; }
    
    /**
     * Remove the model inspector.
     *
     * @return  a {@code null} inspector
     */
    @Override
    public Inspector getInspector() { return null; }
    
    /**
     * Creates panels for the visualization.
     *
     * @return  the list of panels
     */
    protected abstract Panel[] createPanels();
    
    /**
     * Creates drawers for the visualization.
     *
     * @return  the list of drawers
     */
    protected abstract Drawer[] createDrawers();
    
    /**
     * Creates a bounding box for panels.
     *
     * @param x  the x position of the bounding box
     * @param y  the y position of the bounding box
     * @param h  the horizontal size of the bounding box
     * @param v  the vertical size of the bounding box
     * @return  the bounding box
     */
    protected static Rectangle2D.Double getBox(int x, int y, int h, int v) {
        return new Rectangle2D.Double(x, y, h, v);
    }
    
    /**
     * Starts a visualization.
     */
    @Override
    public void start() {
        super.start();
        setup();
    }
    
    /**
     * Loads a visualization from the given simulation.
     *
     * @param state  the MASON simulation state
     */
    @Override
    public void load(SimState state) {
        super.load(state);
        setup();
    }
    
    /**
     * Quits the visualization.
     */
    @Override
    public void quit() {
        super.quit();
        for (Panel panel : panels) {
            panel.remove();
        }
    }
    
    /**
     * Initializes the visualization.
     *
     * @param controller  the controller
     */
    @Override
    public void init(Controller controller) {
        super.init(controller);
        panels = createPanels();
        drawers = createDrawers();
        for (Panel panel : panels) {
            panel.register(controller);
        }
    }
    
    /**
     * Sets up and schedules portrayals.
     */
    public void setup() {
        for (Drawer drawer : drawers) {
            this.scheduleRepeatingImmediatelyAfter(drawer);
        }
        
        for (Panel panel : panels) {
            panel.reset();
        }
    }
}
