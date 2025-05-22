package arcade.patch.sim.output;

import com.google.gson.Gson;
import sim.engine.SimState;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;

/** Custom saver for patch-specific serialization. */
public final class PatchOutputSaver extends OutputSaver {
    /**
     * Creates an {@code PatchOutputSaver} for the series.
     *
     * @param series the simulation series
     */
    public boolean saveGraph;

    public PatchOutputSaver(Series series) {
        super(series);
    }

    @Override
    protected Gson makeGSON() {
        return PatchOutputSerializer.makeGSON();
    }

    public void saveGraphComponents(int tick) {
        LOGGER.info("Saving Graph Sites!");
    }

    @Override
    public void step(SimState simstate) {
        super.step(simstate);
        int tick = (int) simstate.schedule.getTime();
        if (saveGraph) {
            saveGraphComponents(tick);
        }
    }
}
