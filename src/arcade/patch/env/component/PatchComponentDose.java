package arcade.patch.env.component;

import java.util.ArrayList;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.env.component.Component;
import arcade.core.sim.Series;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.sim.PatchSeries;
import static arcade.patch.env.component.PatchComponentSites.SiteLayer;
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
    private final double doseDelay;

    public PatchComponentDose(Series series, MiniBox parameters) {
        layers = new ArrayList<>();

        latticeLength = series.length;
        latticeWidth = series.width;
        latticeHeight = series.height;
        
        // Set loaded parameters.
        mediaAmount = parameters.getDouble("MEDIA_AMOUNT");
        DOSE_DELAY = parameters.getDouble("DOSE_DELAY");
        
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
            this.current = generator.latticeCurrent;
            this.initialConcentration = generator.concentration;
            this.currentAmount = 0;
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(this, Ordering.FIRST.ordinal() - 2);
        schedule.scheduleOnce(this, Ordering.FIRST_COMPONENT.ordinal(), DOSE_DELAY);
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
            DoseLayer doseLayer = new DoseLayer(layer, siteLayer, 
                (PatchOperationGenerator) generator);
            layers.add(doseLayer);
        }
    }

    @Override
    public void step(SimState simstate) {
        for (DoseLayer layer : layers) {
            double delta = 0;
            for (int k = 0; k < latticeHeight; k++) {
                for (int i = 0; i < latticeLength; i++) {
                    for (int j = 0; j < latticeWidth; j++) {
                        delta += latticePatchVolume * (layer.current[k][i][j] - layer.siteLayer.concentration);
                    }
                }
            }
            
            layer.currentAmount = Math.max(0, layer.currentAmount - delta);
            layer.siteLayer.concentration = layer.currentAmount / mediaVolume;
            
            if (simstate.schedule.getTime() >= DOSE_DELAY) {
                layer.currentAmount = layer.initialConcentration * mediaVolume;
                layer.siteLayer.concentration = layer.initialConcentration;
            }
        }
    }
}