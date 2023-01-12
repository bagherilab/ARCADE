package arcade.patch.env.comp;

import java.util.ArrayList;
import sim.engine.Schedule;
import arcade.core.env.comp.Component;
import arcade.core.env.lat.Lattice;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Series;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Abstract implementation of {@link Component} for patch sites.
 * <p>
 * The object defines the locations of sites from which molecules are generated
 * and added into the environment.
 * Multiple molecules generated from the same sites are tracked by a list of
 * {@link SiteLayer} objects, which map the component to the correct environment
 * lattices.
 */

public abstract class PatchComponentSites implements Component {
    /** Height of the array (z direction). */
    final int latticeHeight;
    
    /** Length of the array (x direction). */
    final int latticeLength;
    
    /** Width of the array (y direction). */
    final int latticeWidth;
    
    /** List of site layers. */
    ArrayList<SiteLayer> layers;
    
    /**
     * Creates a {@code PatchComponentSites} object.
     *
     * @param series  the simulation series
     */
    public PatchComponentSites(Series series) {
        latticeLength = series.length;
        latticeWidth = series.width;
        latticeHeight = series.height;
        
        layers = new ArrayList<>();
    }
    
    /**
     * Specification of arrays and parameters for {@link PatchComponentSites}.
     */
    protected class SiteLayer {
        /** Array holding current concentration values. */
        final double[][][] current;

        /** Array holding previous concentration values. */
        final double[][][] previous;
        
        /** Array holding changes in concentration values. */
        final double[][][] delta;
        
        /** Maximum concentration. */
        final double concentration;
        
        /**
         * Creates a {@code SiteLayer} for the given {@link PatchOperationGenerator}.
         *
         * @param generator  the generator operation instance
         */
        SiteLayer(PatchOperationGenerator generator) {
            delta = generator.latticeDelta;
            previous = generator.latticePrevious;
            current = generator.latticeCurrent;
            concentration = generator.concentration;
        }
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.COMPONENTS.ordinal(), 1);
    }
    
    @Override
    public void register(Lattice lattice) {
        Operation generator = lattice.getOperation(Category.GENERATOR);
        SiteLayer layer = new SiteLayer((PatchOperationGenerator) generator);
        layers.add(layer);
    }
}