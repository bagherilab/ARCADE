package arcade.patch.env.comp;

import java.util.ArrayList;
import sim.engine.Schedule;
import arcade.core.env.comp.Component;
import arcade.core.env.lat.Lattice;
import arcade.core.env.operation.Operation;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.patch.env.operation.PatchOperationGenerator;
import static arcade.core.util.Enums.Category;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Abstract implementation of {@link Component} for patch sites.
 * <p>
 * The object defines the locations of sites from which molecules are generated
 * and added into the environment. Multiple molecules generated from the same
 * sites are tracked by a list of {@link SiteLayer} objects, which map the
 * component to the correct environment lattices.
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
    protected static class SiteLayer {
        /** Unique name for layer. */
        final String name;
        
        /** Array holding current concentration values. */
        final double[][][] current;
        
        /** Array holding previous concentration values. */
        final double[][][] previous;
        
        /** Array holding changes in concentration values. */
        final double[][][] delta;
        
        /** Maximum concentration. */
        final double concentration;
        
        /** Molecule permeability. */
        final double permeability;
        
        /**
         * Creates a {@code SiteLayer} for a {@link PatchOperationGenerator}.
         *
         * @param generator  the generator operation instance
         */
        SiteLayer(String name, PatchOperationGenerator generator) {
            this.name = name;
            delta = generator.latticeDelta;
            previous = generator.latticePrevious;
            current = generator.latticeCurrent;
            concentration = generator.concentration;
            permeability = generator.permeability;
        }
    }
    
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.COMPONENTS.ordinal(), 1);
    }
    
    @Override
    public void register(Simulation sim, String layer) {
        Lattice lattice = sim.getLattice(layer);
        Operation generator = lattice.getOperation(Category.GENERATOR);
        SiteLayer siteLayer = new SiteLayer(layer, (PatchOperationGenerator) generator);
        layers.add(siteLayer);
    }
}
