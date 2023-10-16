package arcade.patch.env.component;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.env.component.Component;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.patch.env.component.PatchComponentSites.SiteLayer;
import static arcade.patch.util.PatchEnums.Category;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Component} for cycling sources.
 * <p>
 * This component can only be used with {@link PatchComponentSitesSource}.
 * Multiple cycled molecules are tracked by a list of {@link CycleLayer}
 * objects. This component affects the source concentration of the specified
 * molecule ({@code CYCLE_MOLECULE}). At each time point, the amount of
 * concentration is set equal to a fraction of the initial concentration,
 * determined by a smoothed sawtooth function. The cycle repeats every day, with
 * three peaks per day.
 */

public class PatchComponentCycle implements Component {
    /** Smoothing factor for sawtooth equation. */
    private static final double SMOOTHING_FACTOR = 15;
    
    /** List of cycle layers. */
    private final ArrayList<CycleLayer> layers;
    
    /**
     * Creates a {@code Component} object for representing source site cycling.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentCycle(Series series, MiniBox parameters) {
        layers = new ArrayList<>();
    }
    
    /**
     * Specification of arrays and parameters for {@link PatchComponentCycle}.
     */
    protected static class CycleLayer {
        /** Unique name for layer. */
        final String name;
        
        /** Corresponding site layer instance. */
        final SiteLayer siteLayer;
        
        /** Initial concentration. */
        final double initialConcentration;
        
        /**
         * Creates a {@code CycleLayer} object.
         *
         * @param name  the layer name
         * @param siteLayer  the associated site layer instance
         * @param generator  the associated generator operation instance
         */
        CycleLayer(String name, SiteLayer siteLayer, PatchOperationGenerator generator) {
            this.name = name;
            this.siteLayer = siteLayer;
            initialConcentration = generator.concentration;
        }
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(this, Ordering.FIRST.ordinal() - 2);
        schedule.scheduleRepeating(this, Ordering.FIRST_COMPONENT.ordinal(), 1);
    }
    
    @Override
    public void register(Simulation sim, String layer) {
        String[] layerSplit = layer.split(":");
        Lattice lattice = sim.getLattice(layerSplit[1]);
        Operation generator = lattice.getOperation(Category.GENERATOR);
        Component component = sim.getComponent(layerSplit[0]);
        
        if (!(component instanceof PatchComponentSitesSource)) {
            return;
        }
        
        PatchComponentSitesSource sites = (PatchComponentSitesSource) component;
        SiteLayer siteLayer = sites.layers.stream()
                .filter(sl -> sl.name.equalsIgnoreCase(layerSplit[1]))
                .findFirst()
                .orElse(null);
        
        if (siteLayer != null) {
            CycleLayer cycleLayer = new CycleLayer(layer, siteLayer,
                    (PatchOperationGenerator) generator);
            layers.add(cycleLayer);
        }
    }
    
    @Override
    public void step(SimState simstate) {
        double tick = simstate.schedule.getTime();
        double x = tick / 60.0 / 24.0 * 3;
        double scale = (Math.tanh((x - Math.floor(x) - 0.5) * SMOOTHING_FACTOR))
                / (2 * Math.tanh(0.5 * SMOOTHING_FACTOR)) + Math.floor(x) + 1.5 - x;
        
        for (CycleLayer layer : layers) {
            layer.siteLayer.concentration = scale * layer.initialConcentration;
        }
    }
}
