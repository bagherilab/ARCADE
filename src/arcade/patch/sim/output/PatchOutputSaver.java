package arcade.patch.sim.output;

import com.google.gson.Gson;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;

/** Custom saver for patch-specific serialization. */
public final class PatchOutputSaver extends OutputSaver {
    /**
     * Creates an {@code PatchOutputSaver} for the series.
     *
     * @param series the simulation series
     */
    public PatchOutputSaver(Series series) {
        super(series);
    }

    @Override
    protected Gson makeGSON() {
        return PatchOutputSerializer.makeGSON();
    }
}
