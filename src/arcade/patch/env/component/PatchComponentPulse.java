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
import arcade.patch.sim.PatchSeries;
import static arcade.patch.env.component.PatchComponentSites.SiteLayer;
import static arcade.patch.util.PatchEnums.Category;
import static arcade.patch.util.PatchEnums.Ordering;

/**
 * Implementation of {@link Component} for pulsing sources.
 *
 * <p>This component can only be used with {@link PatchComponentSitesSource}. Multiple pulsed
 * molecules are tracked by a list of {@link PulseLayer} objects.
 *
 * <p>The amount of media ({@code MEDIA_AMOUNT}) is used to determine the total amount of molecule
 * available given total simulation area. The molecule concentration is updated each step as the
 * molecule is consumed or otherwise removed from the environment. At the specified pulse interval
 * ({@code PULSE_INTERVAL}), a "pulse" of media is introduced, updating the total amount of molecule
 * available.
 */
public class PatchComponentPulse implements Component {
    /** List of pulse layers. */
    private final ArrayList<PulseLayer> layers;

    /** Height of the array (z direction). */
    private final int latticeHeight;

    /** Length of the array (x direction). */
    private final int latticeLength;

    /** Width of the array (y direction). */
    private final int latticeWidth;

    /** Interval between pulses [min]. */
    private final double pulseInterval;

    /** Media volume per area [um<sup>3</sup>/um<sup>2</sup>]. */
    private final double mediaAmount;

    /** Total media volume [um<sup>3</sup>]. */
    private final double mediaVolume;

    /** Volume of individual lattice patch [um<sup>3</sup>]. */
    private final double latticePatchVolume;

    /** Area of individual lattice patch [um<sup>2</sup>]. */
    private final double latticePatchArea;

    /**
     * Creates a {@code Component} object for representing source site pulses.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code PULSE_INTERVAL} = interval between pulses
     *   <li>{@code MEDIA_AMOUNT} = media volume per area
     * </ul>
     *
     * @param series the simulation series
     * @param parameters the component parameters dictionary
     */
    public PatchComponentPulse(Series series, MiniBox parameters) {
        layers = new ArrayList<>();

        latticeLength = series.length;
        latticeWidth = series.width;
        latticeHeight = series.height;

        // Set loaded parameters.
        pulseInterval = parameters.getDouble("PULSE_INTERVAL");
        mediaAmount = parameters.getDouble("MEDIA_AMOUNT");

        // Set patch parameters.
        MiniBox patch = ((PatchSeries) series).patch;
        latticePatchVolume = patch.getDouble("LATTICE_VOLUME");
        latticePatchArea = patch.getDouble("LATTICE_AREA");

        // Calculate media volume.
        mediaVolume = latticePatchArea * latticeLength * latticeWidth * mediaAmount;
    }

    /** Specification of arrays and parameters for {@link PatchComponentPulse}. */
    protected static class PulseLayer {
        /** Unique name for layer. */
        final String name;

        /** Array holding current concentration values. */
        final double[][][] current;

        /** Array holding previous concentration values. */
        final double[][][] previous;

        /** Corresponding site layer instance. */
        final SiteLayer siteLayer;

        /** Initial concentration. */
        final double initialConcentration;

        /** Current amount. */
        double currentAmount;

        /**
         * Creates a {@code PulseLayer} object.
         *
         * @param name the layer name
         * @param siteLayer the associated site layer instance
         * @param generator the associated generator operation instance
         */
        PulseLayer(String name, SiteLayer siteLayer, PatchOperationGenerator generator) {
            this.name = name;
            this.siteLayer = siteLayer;
            previous = generator.latticePrevious;
            current = generator.latticeCurrent;
            initialConcentration = generator.concentration;
            currentAmount = 0;
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
        SiteLayer siteLayer =
                sites.layers.stream()
                        .filter(sl -> sl.name.equalsIgnoreCase(layerSplit[1]))
                        .findFirst()
                        .orElse(null);

        if (siteLayer != null) {
            PulseLayer pulseLayer =
                    new PulseLayer(layer, siteLayer, (PatchOperationGenerator) generator);
            layers.add(pulseLayer);
        }
    }

    @Override
    public void step(SimState simstate) {
        double tick = simstate.schedule.getTime();

        for (PulseLayer layer : layers) {
            double[][][] previous = layer.previous;
            double[][][] current = layer.current;
            double delta = 0;

            // Get total consumption.
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        delta += (previous[k][i][j] - current[k][i][j]) * latticePatchVolume;
                    }
                }
            }

            // Update available concentrations.
            layer.currentAmount = Math.max(0, layer.currentAmount - delta);
            layer.siteLayer.concentration = layer.currentAmount / mediaVolume;

            // Pulse returns concentration to initial value.
            if (tick % pulseInterval == 0) {
                layer.currentAmount = layer.initialConcentration * mediaVolume;
                layer.siteLayer.concentration = layer.initialConcentration;
            }
        }
    }
}
