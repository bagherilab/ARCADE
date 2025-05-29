package arcade.patch.sim.output;

import com.google.gson.Gson;
import sim.engine.SimState;
import arcade.core.env.component.Component;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.patch.env.component.PatchComponentSitesGraph;
import arcade.patch.sim.PatchSimulation;

/** Custom saver for patch-specific serialization. */
public final class PatchOutputSaver extends OutputSaver {

    /** {@code true} to save graph components, {@code false} otherwise. */
    public boolean saveGraph;

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

    public void saveComponents(int tick) {
        for (String componentKey : ((PatchSimulation) sim).getComponentKeys()) {
            Component component = sim.getComponent(componentKey);
            if (component instanceof PatchComponentSitesGraph) {
                String json =
                        gson.toJson(
                                (PatchComponentSitesGraph) component,
                                PatchComponentSitesGraph.class);
                String path = prefix + String.format("_%06d." + componentKey + ".GRAPH.json", tick);
                write(path, format(json, FORMAT_ELEMENTS));
            }
        }
    }

    @Override
    public void save(int tick) {
        super.save(tick);
        if (saveGraph) {
            saveComponents(tick);
        }
    }

    @Override
    public void step(SimState simstate) {
        super.step(simstate);
        int tick = (int) simstate.schedule.getTime();
        save(tick);
    }
}
