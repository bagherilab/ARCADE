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

public class PatchComponentDose implements Component {
    private final ArrayList<DoseLayer> layers;
    private final int latticeHeight;
    private final int latticeLength;
    private final int latticeWidth;
    private final double mediaAmount;
    private final double mediaVolume;
    private final double latticePatchVolume;
    private final double latticePatchArea;
    private final double doseStart;
    private final double doseDuration;
    private final double doseInterval;
    private final double doseEnd;

    public PatchComponentDose(Series series, MiniBox parameters) {
        layers = new ArrayList<>();

        latticeLength = series.length;
        latticeWidth = series.width;
        latticeHeight = series.height;

        // Set loaded parameters.
        mediaAmount = parameters.getDouble("MEDIA_AMOUNT");
        doseStart = parameters.getDouble("DOSE_START");
        doseDuration = parameters.getDouble("DOSE_DURATION");
        doseInterval = parameters.getDouble("DOSE_INTERVAL");
        doseEnd = parameters.getDouble("DOSE_END");

        // Set patch parameters.
        MiniBox patch = ((PatchSeries) series).patch;
        latticePatchVolume = patch.getDouble("LATTICE_VOLUME");
        latticePatchArea = patch.getDouble("LATTICE_AREA");

        mediaVolume = latticePatchArea * latticeLength * latticeWidth * mediaAmount;
    }

    protected static class DoseLayer {
        final String name;
        final double[][][] current;
        final SiteLayer siteLayer;
        final double initialConcentration;
        double currentAmount;

        DoseLayer(String name, SiteLayer siteLayer, PatchOperationGenerator generator) {
            this.name = name;
            this.siteLayer = siteLayer;
            current = generator.latticeCurrent;
            initialConcentration = generator.concentration;
            currentAmount = 0;
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(doseStart, Ordering.FIRST_COMPONENT.ordinal(), this);
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
            DoseLayer doseLayer =
                    new DoseLayer(layer, siteLayer, (PatchOperationGenerator) generator);
            layers.add(doseLayer);
        }
    }

    @Override
    public void step(SimState simstate) {
        double tick = simstate.schedule.getTime();

        double doseAmount = 400000;
        for (DoseLayer layer : layers) {
            if (tick > doseEnd) {
                layer.currentAmount = 0;
                layer.siteLayer.concentration = 0;
                return;
            }

            if (tick >= doseStart && (tick - doseStart) % doseInterval < doseDuration) {
            layer.currentAmount += doseAmount;
            } else {
                layer.currentAmount = 0;
            }

            layer.siteLayer.concentration = layer.currentAmount / mediaVolume;

        }
    }
}
