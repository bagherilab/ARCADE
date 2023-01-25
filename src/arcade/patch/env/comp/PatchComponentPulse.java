package arcade.patch.env.comp;

import java.util.logging.Logger;
import sim.engine.SimState;
import arcade.core.sim.Simulation;
import arcade.core.env.lat.Lattice;
import arcade.core.env.loc.Location;
import arcade.env.comp.SourceSites.SourceSite;
import arcade.core.util.MiniBox;

/**
 * Implementation of {@link arcade.core.env.comp.Component} for pulsing sources.
 * <p>
 * This component can only be used with {@link arcade.env.comp.SourceSites}.
 * This component affects the source concentration of the specified molecule
 * ({@code PULSE_MOLECULE}).
 * The amount of media for the given surface area of the simulation
 * ({@code MEDIA_AMOUNT}) is used to determine the total amount of molecule available.
 * The molecule concentration is updated each step as the molecule is consumed
 * or otherwise removed from the environment.
 * At the specified pulse interval ({@code PULSE_INTERVAL}), a "pulse" of media
 * is introduced, updating the total amount of molecule available.
 */

public class PatchComponentPulse implements Component {
    /** Logger for {@code PulseComponent} */
    private static Logger LOGGER = Logger.getLogger(PatchComponentPulse.class.getName());
    
    /** Depth of the array (z direction) */
    int DEPTH;
    
    /** Length of the array (x direction) */
    int LENGTH;
    
    /** Width of the array (y direction) */
    int WIDTH;
    
    /** {@link arcade.env.comp.SourceSites.SourceSite} object */
    private SourceSite site;
    
    /** Name of molecule to pulse */
    private final String _pulseMolecule;
    
    /** Interval between pulses (in minutes) */
    private final double _pulseInterval;
    
    /** Amount of media per surface area (in mL/um^2) */
    private final double _mediaAmount;
    
    /** Total media volume */
    private double mediaVolume;
    
    /** Volume of individual location */
    private double individualVolume;
    
    /** Initial concentration of molecule */
    private double initialConcentration;
    
    /** Total amount of molecule in media */
    private double moleculeAmount;
    
    /** Dictionary of specifications */
    private final MiniBox specs;
    
    /**
     * Creates a {@link arcade.core.env.comp.Component} object for pulsing.
     * <p>
     * Specifications include:
     * <ul>
     *     <li>{@code PULSE_MOLECULE} = molecule that is being pulsed</li>
     *     <li>{@code PULSE_INTERVAL} = interval between pulses</li>
     *     <li>{@code MEDIA_AMOUNT} = media amount per surface area</li>
     * </ul>
     *
     * @param component  the parsed component attributes
     */
    public PatchComponentPulse(MiniBox component) {
        // Get parameters.
        _pulseMolecule = component.get("PULSE_MOLECULE");
        _pulseInterval = component.getDouble("PULSE_INTERVAL")*24*60; // days -> minutes
        _mediaAmount = component.getDouble("MEDIA_AMOUNT"); // um^3/um^2
        
        // Get list of specifications.
        specs = new MiniBox();
        String[] specList = new String[] { "PULSE_MOLECULE", "PULSE_INTERVAL", "MEDIA_AMOUNT" };
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
            LOGGER.warning("cannot schedule PULSE component for non-source sites");
            return;
        }
        
        SourceSites sites = (SourceSites)comp;
        
        // Get sizing.
        Lattice lat = sim.getEnvironment("sites");
        LENGTH = lat.getLength();
        WIDTH = lat.getWidth();
        DEPTH = lat.getDepth();
        
        // Calculate total surface area of simulation.
        Location loc = sim.getRepresentation().getCenterLocation();
        double area = loc.getArea()/loc.getMax();
        double surfaceArea = LENGTH * WIDTH * area; // um^2
        
        // Calculate volume of media.
        mediaVolume = surfaceArea * _mediaAmount; // um^3
        
        // Calculate total amount of molecule available.
        MiniBox molecule = sim.getMolecules().get(_pulseMolecule);
        int code = molecule.getInt("code");
        initialConcentration = molecule.getDouble("CONCENTRATION");
        moleculeAmount = mediaVolume * initialConcentration;
        
        // Calculate volume of individual location.
        individualVolume = loc.getVolume()/loc.getMax();
        
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
     * Pulse component does not use this method.
     */
    public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) { }
    
    /**
     * Steps through the lattice to calculate updated concentration.
     *
     * @param state  the MASON simulation state
     */
    public void step(SimState state) {
        double tick = state.schedule.getTime();
        double delta = 0;
        
        // Get total consumption of nutrient.
        for (int k = 0; k < DEPTH; k++) {
            for (int i = 0; i < LENGTH; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    delta += (site.prev[k][i][j] - site.curr[k][i][j])*individualVolume;
                }
            }
        }
        
        // Update total amount of molecule available and concentration.
        moleculeAmount -= delta;
        site.conc = moleculeAmount/mediaVolume;
        
        // Pulse returns concentration to initial value.
        if (tick%_pulseInterval == 0) {
            moleculeAmount = mediaVolume*initialConcentration;
            site.conc = initialConcentration;
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The JSON is formatted as:
     * <pre>
     *     {
     *         "type": "PULSE",
     *         "specs" : {
     *             "SPEC_NAME": spec value,
     *             "SPEC_NAME": spec value,
     *             ...
     *         }
     *     }
     * </pre>
     */
    public String toJSON() {
        String format = "{ " + "\"type\": \"PULSE\", " + "\"specs\": %s " + "}";
        return String.format(format, specs.toJSON());
    }
}