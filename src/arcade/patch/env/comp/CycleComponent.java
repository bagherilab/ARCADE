package arcade.patch.env.comp;

import java.util.logging.Logger;
import sim.engine.SimState;
import arcade.sim.Simulation;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.env.comp.SourceSites.SourceSite;
import arcade.util.MiniBox;

/**
 * Implementation of {@link arcade.env.comp.Component} for cycling sources.
 * <p>
 * This component can only be used with {@link arcade.env.comp.SourceSites}.
 * This component affects the source concentration of the specified molecule
 * ({@code CYCLE_MOLECULE}).
 * At each time point, the amount of concentration is set equal to a fraction
 * of the initial concentration, determined by a smoothed sawtooth function.
 * The cycle repeats every day, with three peaks per day.
 */

public class CycleComponent implements Component {
    /** Logger for {@code CycleComponent} */
    private static Logger LOGGER = Logger.getLogger(CycleComponent.class.getName());
    
    /** Smoothing factor for sawtooth equation */
    private static final double SMOOTHING_FACTOR = 15;
    
    /** Depth of the array (z direction) */
    int DEPTH;
    
    /** Length of the array (x direction) */
    int LENGTH;
    
    /** Width of the array (y direction) */
    int WIDTH;
    
    /** {@link arcade.env.comp.Site} object */
    private SourceSite site;
    
    /** Name of molecule to cycle */
    private final String _cycleMolecule;
    
    /** Initial concentration of molecule */
    private double initialConcentration;
    
    /** Dictionary of specifications */
    private final MiniBox specs;
    
    /**
     * Creates a {@link arcade.env.comp.Component} object for cycling.
     * <p>
     * Specifications include:
     * <ul>
     *     <li>{@code CYCLE_MOLECULE} = molecule that is being cycled</li>
     * </ul>
     *
     * @param component  the parsed component attributes
     */
    public CycleComponent(MiniBox component) {
        // Get parameters.
        _cycleMolecule = component.get("CYCLE_MOLECULE");
        
        // Get list of specifications.
        specs = new MiniBox();
        String[] specList = new String[] { "CYCLE_MOLECULE" };
        for (String spec : specList) { specs.put(spec, component.get(spec)); }
    }
    
    /**
     * Component does not have a relevant field; returns {@code null}.
     * 
     * @return  {@code null}
     */
    public double[][][] getField() { return null; }
    
    /**
     * {@inheritDoc}
     * <p>
     * This component can only be scheduled with {@link arcade.env.comp.SourceSites}.
     */
    public void scheduleComponent(Simulation sim) {
        Component comp = sim.getEnvironment("sites").getComponent("sites");
        if (!(comp instanceof SourceSites)) {
            LOGGER.warning("cannot schedule CYCLE component for non-source sites");
            return;
        }
        
        SourceSites sites = (SourceSites)comp;
        
        // Get sizing.
        Lattice lat = sim.getEnvironment("sites");
        LENGTH = lat.getLength();
        WIDTH = lat.getWidth();
        DEPTH = lat.getDepth();
        
        // Calculate total amount of molecule available.
        MiniBox molecule = sim.getMolecules().get(_cycleMolecule);
        int code = molecule.getInt("code");
        initialConcentration = molecule.getDouble("CONCENTRATION");
        
        ((SimState)sim).schedule.scheduleRepeating(1, Simulation.ORDERING_COMPONENT - 3, this);
        ((SimState)sim).schedule.scheduleOnce((state) -> {
            // Search through site list to get correct molecule.
            for (Site site : sites.siteList) {
                if (site.code == code) { this.site = (SourceSite)site; }
            }
        }, Simulation.ORDERING_COMPONENT - 3);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Cycle component does not use this method.
     */
    public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) { }
    
    /**
     * Steps through the lattice to calculate updated concentration.
     *
     * @param state  the MASON simulation state
     */
    public void step(SimState state) {
        double tick = state.schedule.getTime();
        double x = tick/60.0/24.0*3;
        double scale = (Math.tanh((x - Math.floor(x) - 0.5)*SMOOTHING_FACTOR))/
                (2*Math.tanh(0.5*SMOOTHING_FACTOR)) + Math.floor(x) + 1.5 - x;
        site.conc = scale*initialConcentration;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The JSON is formatted as:
     * <pre>
     *     {
     *         "type": "CYCLE",
     *         "specs" : {
     *             "SPEC_NAME": spec value,
     *             "SPEC_NAME": spec value,
     *             ...
     *         }
     *     }
     * </pre>
     */
    public String toJSON() {
        String format = "{ " + "\"type\": \"CYCLE\", " + "\"specs\": %s " + "}";
        return String.format(format, specs.toJSON());
    }
}