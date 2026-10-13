package arcade.potts.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.potts.sim.PottsSimulation;

/** Custom saver for potts-specific serialization. */
public final class PottsOutputSaver extends OutputSaver {
    /** {@code true} to save lattices, {@code false} otherwise. */
    public boolean saveLattice;

    /** Offset added to output tick labels. */
    public int tickOffset;

    /** Plain (non-pretty) Gson for full-precision layer arrays. */
    private static final Gson LAYER_GSON = new Gson();

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
