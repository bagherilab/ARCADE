package arcade.potts.env.component;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
 * Implementation of {@link Component} for supply from implicit vasculature.
 *
 * <p>Each tick, every voxel with a positive mask value m receives an exchange P m (T - c) toward a
 * blood value T in each registered layer, through {@link PottsLattice#addExchange(List, double,
 * double)}. The diffuser of the layer solves the exchange inside its substeps. The mask is a layer
 * with no operations (for example, a supply map loaded from the initial layers file), where 0 is no
 * supply and 1 is full supply; values in between scale the supply (for example, a lower capillary
 * density). The mask is read once, on the first step.
 *
 * <p>The component only sets where supply comes from. Low values elsewhere (for example, hypoxia in
 * a wound) are not imposed; they come from diffusion and uptake.
 */
public class PottsComponentOxygenSource implements Component {
    /** Exchange rate at full supply [1/s]. */
    private final double permeability;

    /** Blood value the exchange relaxes toward [layer units]. */
    private final double bloodValue;

    /** Id of the mask layer. */
    private final String mask;

    /** Ids of the registered layers. */
    private final ArrayList<String> layers = new ArrayList<>();

    /** Supplied voxels grouped by mask value, read on the first step. */
    private TreeMap<Double, List<Voxel>> supplied;

    /**
     * Creates a {@link Component} for supply from implicit vasculature.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code PERMEABILITY} = exchange rate at full supply [1/s]
     *   <li>{@code BLOOD_O2} = blood value the exchange relaxes toward [layer units]
     *   <li>{@code MASK} = id of the mask layer
     * </ul>
     *
     * @param parameters the component parameters dictionary
     */
    public PottsComponentOxygenSource(MiniBox parameters) {
        permeability = parameters.getDouble("PERMEABILITY");
        bloodValue = parameters.getDouble("BLOOD_O2");
        mask = parameters.get("MASK");
        if (!(permeability >= 0)) {
            throw new IllegalArgumentException("oxygen source requires PERMEABILITY >= 0");
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.COMPONENTS.ordinal(), 1);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The registered layer receives the supply and must have a diffuser.
     */
    @Override
    public void register(Simulation sim, String layer) {
        layers.add(layer);
    }

    /**
     * Groups the voxels with a positive mask value by value.
     *
     * @param sim the simulation instance
     * @return the map of mask value to voxels
     */
    TreeMap<Double, List<Voxel>> readMask(Simulation sim) {
        Lattice maskLattice = sim.getLattice(mask);
        if (maskLattice == null) {
            throw new IllegalStateException("oxygen source mask layer [ " + mask + " ] not found");
        }
        PottsLattice lattice = (PottsLattice) maskLattice;
        for (Category category : Category.values()) {
            if (lattice.getOperation(category) != null) {
                throw new IllegalStateException(
                        "oxygen source mask layer [ " + mask + " ] must have no operations");
            }
        }

        TreeMap<Double, List<Voxel>> groups = new TreeMap<>();
        double[][][] field = lattice.getField();
        for (int k = 0; k < field.length; k++) {
            for (int i = 0; i < field[k].length; i++) {
                for (int j = 0; j < field[k][i].length; j++) {
                    double value = field[k][i][j];
                    if (value < 0) {
                        throw new IllegalStateException(
                                "oxygen source mask layer [ " + mask + " ] has negative values");
                    }
                    if (value > 0) {
                        groups.computeIfAbsent(value, v -> new ArrayList<>())
                                .add(new Voxel(i, j, k));
                    }
                }
            }
        }
        return groups;
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        if (permeability == 0) {
            return;
        }
        if (supplied == null) {
            supplied = readMask(sim);
        }

        for (String layer : layers) {
            PottsLattice lattice = (PottsLattice) sim.getLattice(layer);
            if (lattice == null || lattice.getOperation(Category.DIFFUSER) == null) {
                throw new IllegalStateException(
                        "oxygen source layer [ " + layer + " ] requires a layer with a diffuser");
            }
            for (double value : supplied.keySet()) {
                lattice.addExchange(supplied.get(value), permeability * value, bloodValue);
            }
        }
    }
}
