package arcade.potts.sim.output;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import arcade.core.env.component.Component;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.potts.env.component.PottsComponentVessels;
import arcade.potts.sim.PottsSimulation;

/** Custom saver for potts-specific serialization. */
public final class PottsOutputSaver extends OutputSaver {
    /** {@code true} to save lattices, {@code false} otherwise. */
    public boolean saveLattice;

    /** Offset added to output tick labels. */
    public int tickOffset;

    /** Plain (non-pretty) Gson for full-precision layer arrays. */
    private static final Gson LAYER_GSON = new Gson();

    /** Version of the vessel network at the last save, or -1 if not saved. */
    private int savedVesselVersion = -1;

    /**
     * Creates a {@code PottsOutputSaver} for the series.
     *
     * @param series the simulation series
     */
    public PottsOutputSaver(Series series) {
        super(series);
    }

    @Override
    public void save(int tick) {
        int labeled = tick + tickOffset;
        saveCells(labeled);
        saveLocations(labeled);
        if (saveLattice) {
            saveLayers(labeled);
        }
        saveVessels(labeled);
    }

    /**
     * Saves the vessel network, if there is one and it changed since the last save.
     *
     * <p>At most one vessel component is supported.
     *
     * @param tick the labeled tick
     */
    public void saveVessels(int tick) {
        if (sim.getSeries() == null || sim.getSeries().components == null) {
            return;
        }
        List<PottsComponentVessels> vessels = new ArrayList<>();
        for (String key : sim.getSeries().components.keySet()) {
            Component component = sim.getComponent(key);
            if (component instanceof PottsComponentVessels) {
                vessels.add((PottsComponentVessels) component);
            }
        }
        if (vessels.isEmpty()) {
            return;
        }
        if (vessels.size() > 1) {
            throw new IllegalStateException("at most one vessel component is supported");
        }
        PottsComponentVessels component = vessels.get(0);
        if (component.getNetwork() == null || component.getVersion() == savedVesselVersion) {
            return;
        }
        String path = prefix + String.format("_%06d.VESSELS.json", tick);
        write(path, component.getNetwork().toJson());
        savedVesselVersion = component.getVersion();
    }

    /**
     * Saves all lattice fields at full precision.
     *
     * @param tick the labeled tick
     */
    public void saveLayers(int tick) {
        String path = prefix + String.format("_%06d.LAYERS.json", tick);
        write(path, LAYER_GSON.toJson(((PottsSimulation) sim).getLayers()));
    }

    @Override
    protected Gson makeGSON() {
        return PottsOutputSerializer.makeGSON();
    }
}
