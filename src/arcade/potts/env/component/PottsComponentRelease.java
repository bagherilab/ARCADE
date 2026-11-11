package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.List;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.env.component.Component;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Component} for release into a layer in proportion to a source layer.
 *
 * <p>Each tick, every voxel of each registered layer gains {@code RATE} s dt, where s is the value
 * of the {@code SOURCE} layer at that voxel and dt is the tick length [hours]. The source layer is
 * read every tick, so release follows the source as it changes (for example, a clot that is broken
 * down). The amount is added through {@link PottsLattice#addDelta(List, double)}, so it enters the
 * field at the next lattice step, before diffusion and decay.
 *
 * <p>Example: injury chemokines released from the provisional matrix ({@code SOURCE} = {@code
 * WOUND_MATRIX}) into a diffusing, decaying {@code INJURY_SIGNAL}. With decay rate k, the layer
 * settles near {@code RATE} s / k where the source is wide compared with the diffusion length.
 */
public class PottsComponentRelease implements Component {
    /** Release rate per unit source [layer units/hour per source unit]. */
    private final double rate;

    /** Id of the source layer. */
    private final String source;

    /** Ids of the registered layers. */
    private final ArrayList<String> layers = new ArrayList<>();

    /**
     * Creates a {@link Component} for release in proportion to a source layer.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code RATE} = release rate per unit source [layer units/hour per source unit]
     *   <li>{@code SOURCE} = id of the source layer
     * </ul>
     *
     * @param parameters the component parameters dictionary
     */
    public PottsComponentRelease(MiniBox parameters) {
        rate = parameters.getDouble("RATE");
        source = parameters.get("SOURCE");
        if (!(rate >= 0)) {
            throw new IllegalArgumentException("release requires RATE >= 0");
        }
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("release requires a SOURCE layer");
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.COMPONENTS.ordinal(), 1);
    }

    @Override
    public void register(Simulation sim, String layer) {
        layers.add(layer);
    }

    /**
     * Gets a layer, or throws if it does not exist.
     *
     * @param sim the simulation instance
     * @param id the layer id
     * @param role the role of the layer, for the message
     * @return the layer
     */
    static PottsLattice getLayer(Simulation sim, String id, String role) {
        Lattice lattice = sim.getLattice(id);
        if (lattice == null) {
            throw new IllegalStateException("release " + role + " layer [ " + id + " ] not found");
        }
        return (PottsLattice) lattice;
    }

    @Override
    public void step(SimState simstate) {
        if (rate == 0) {
            return;
        }
        Simulation sim = (Simulation) simstate;
        double[][][] values = getLayer(sim, source, "source").getField();

        for (String layer : layers) {
            PottsLattice lattice = getLayer(sim, layer, "target");
            if (lattice.getOperation(Category.DIFFUSER) == null
                    && lattice.getOperation(Category.DECAYER) == null) {
                throw new IllegalStateException(
                        "release layer [ " + layer + " ] needs a diffuser or decayer");
            }
            double perTick = rate * lattice.getDt();
            for (int k = 0; k < values.length; k++) {
                for (int i = 0; i < values[k].length; i++) {
                    for (int j = 0; j < values[k][i].length; j++) {
                        double value = values[k][i][j];
                        if (value > 0) {
                            lattice.addDelta(List.of(new Voxel(i, j, k)), perTick * value);
                        }
                    }
                }
            }
        }
    }
}
