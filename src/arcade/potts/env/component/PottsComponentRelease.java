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
 * <p>Each tick, every voxel of each registered layer receives a continuous source {@code RATE} s,
 * where s is the value of the {@code SOURCE} layer at that voxel, through {@link
 * PottsLattice#addSource(List, double)}. The diffuser adds it in every substep, so over a tick the
 * voxel gains {@code RATE} s dt (dt = tick length [hours]) spread over the tick rather than as one
 * pulse. The source layer is read every tick, so release follows the source as it changes (for
 * example, a clot that is broken down). Each registered layer must have a diffuser; for fast decay,
 * use the diffuser's {@code DECAY_RATE} so that decay is also applied inside the substeps.
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
            if (lattice.getOperation(Category.DIFFUSER) == null) {
                throw new IllegalStateException(
                        "release layer [ " + layer + " ] requires a diffuser");
            }
            double perSecond = rate / 3600.0;
            for (int k = 0; k < values.length; k++) {
                for (int i = 0; i < values[k].length; i++) {
                    for (int j = 0; j < values[k][i].length; j++) {
                        double value = values[k][i][j];
                        if (value > 0) {
                            lattice.addSource(List.of(new Voxel(i, j, k)), perSecond * value);
                        }
                    }
                }
            }
        }
    }
}
