package arcade.patch.sim.output;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import sim.engine.SimState;
import arcade.core.env.component.Component;
import arcade.core.env.location.Location;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSaver;
import arcade.patch.env.component.PatchComponentSitesGraph;
import arcade.patch.sim.PatchSimulation;

/** Custom saver for patch-specific serialization. */
public final class PatchOutputSaver extends OutputSaver {

    /** {@code true} to save graph components, {@code false} otherwise. */
    public boolean saveGraph;

    /** {@code true} to save lattices, {@code false} otherwise. */
    public boolean saveLayers;

    /** Set of all possible locations in the simulation. */
    private Set<Location> possibleLocations;

    /** Hidden utility object type for gson implementation. */
    static final Type CUSTOM_LAYER_TYPE =
            new TypeToken<HashMap<Location, HashMap<String, Double>>>() {}.getType();

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
        PatchSimulation patchSim = (PatchSimulation) sim;
        if (!saveLayers) {
            return;
        }

        if (possibleLocations == null) {
            possibleLocations = patchSim.getAllLocations();
        }

        HashMap<Location, HashMap<String, Double>> layers = new HashMap<>();
        for (Location loc : possibleLocations) {
            for (String key : patchSim.getLatticeKeys()) {
                if (layers.containsKey(loc)) {
                    layers.get(loc).put(key, sim.getLattice(key).getAverageValue(loc));
                    continue;
                }
                layers.put(loc, new HashMap<>());
                layers.get(loc).put(key, sim.getLattice(key).getAverageValue(loc));
            }
        }
        String json = gson.toJson(layers, CUSTOM_LAYER_TYPE);
        String patch = prefix + String.format("_%06d.LAYERS.json", tick);
        write(patch, format(json, FORMAT_ELEMENTS));
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
