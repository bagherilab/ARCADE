package arcade.patch.sim.output;

import com.google.gson.Gson;
import sim.engine.SimState;
import arcade.core.env.component.Component;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.patch.env.component.PatchComponentSitesGraph;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.sim.PatchSimulation.PATCH_LAYER_TYPE;

/** Custom saver for patch-specific serialization. */
public final class PatchOutputSaver extends OutputSaver {

    /** {@code true} to save graph components, {@code false} otherwise. */
    public boolean saveGraph;

    /** {@code true} to save lattices, {@code false} otherwise. */
    public boolean saveLattice;

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

    /**
     * Save a list of {@link arcade.patch.env.component.PatchComponentSitesGraph} to a JSON.
     *
     * @param tick the simulation tick
     */
    public void saveComponents(int tick) {
        for (String componentKey : ((PatchSimulation) sim).getComponentKeys()) {
            Component component = sim.getComponent(componentKey);
            if (component instanceof PatchComponentSitesGraph && saveGraph) {
                String json =
                        gson.toJson(
                                (PatchComponentSitesGraph) component,
                                PatchComponentSitesGraph.class);
                String path = prefix + String.format("_%06d." + componentKey + ".GRAPH.json", tick);
                write(path, format(json, FORMAT_ELEMENTS));
            }
        }
    }

    /**
     * Save the collection of {@link arcade.patch.env.lattice.PatchLattice} to a JSON, by location.
     *
     * @param tick the simulation tick
     */
    public void saveLayers(int tick) {
        String json = gson.toJson(((PatchSimulation) sim).getLayers(), PATCH_LAYER_TYPE);
        String patch = prefix + String.format("_%06d.LAYERS.json", tick);
        write(patch, format(json, FORMAT_ELEMENTS));
    }

    @Override
    public void save(int tick) {
        super.save(tick);
        if (saveGraph) {
            saveComponents(tick);
        }
        if (saveLattice) {
            saveLayers(tick);
        }
    }

    @Override
    public void step(SimState simstate) {
        super.step(simstate);
        int tick = (int) simstate.schedule.getTime();
        save(tick);
    }
}
