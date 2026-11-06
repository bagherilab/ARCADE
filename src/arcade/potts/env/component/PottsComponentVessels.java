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
 * Implementation of {@link Component} for a 3D vessel network around the tissue slice.
 *
 * <p>Perfused segments ({@link PottsVesselPerfusion}) supply each registered layer: every tick,
 * each voxel with exchange weight w ({@link PottsVesselDelivery#exchangeWeights}) receives an
 * exchange {@code P_O2} w ({@code BLOOD_O2} - c) through {@link PottsLattice#addExchange(List,
 * double, double)}. The layer must have a diffuser, which solves the exchange. Recruitment sites
 * within {@code D_RECRUIT} of the slice are available from {@link #getSiteWeights(Simulation)}.
 *
 * <p>Perfusion, exchange weights, and sites are computed on first use and cached, because the
 * network does not change in V1. Call {@link #refresh()} after the network changes. The lattice
 * size and voxel size are taken from the first registered layer. The slice is 2D.
 */
public class PottsComponentVessels implements Component {
    /** Exchange rate per unit weight [1/s]. */
    private final double permeability;

    /** Blood value the exchange relaxes toward [layer units]. */
    private final double bloodValue;

    /** O2 diffusion length along z [um]. */
    private final double lambda;

    /** Distance from the slice that recruitment sites come from [um]. */
    private final double recruitDistance;

    /** Ids of the registered layers. */
    private final ArrayList<String> layers = new ArrayList<>();

    /** Vessel network. */
    private PottsVesselNetwork network;

    /** {@code true} if the caches are up to date, {@code false} otherwise. */
    private boolean prepared;

    /** Cached voxels with a positive exchange weight, each as a one-voxel list. */
    private final ArrayList<List<Voxel>> exchangeVoxels = new ArrayList<>();

    /** Cached exchange weights, matching {@link #exchangeVoxels}. */
    private final ArrayList<Double> exchangeWeights = new ArrayList<>();

    /** Cached recruitment site weights, indexed [x][y]. */
    private double[][] siteWeights;

    /**
     * Creates a {@link Component} for a vessel network.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code P_O2} = exchange rate per unit weight [1/s]
     *   <li>{@code BLOOD_O2} = blood value the exchange relaxes toward [layer units]
     *   <li>{@code LAMBDA} = O2 diffusion length along z [um]
     *   <li>{@code D_RECRUIT} = distance from the slice that recruitment sites come from [um]
     * </ul>
     *
     * @param parameters the component parameters dictionary
     */
    public PottsComponentVessels(MiniBox parameters) {
        permeability = parameters.getDouble("P_O2");
        bloodValue = parameters.getDouble("BLOOD_O2");
        lambda = parameters.getDouble("LAMBDA");
        recruitDistance = parameters.getDouble("D_RECRUIT");
        if (!(permeability >= 0) || !(lambda > 0) || !(recruitDistance >= 0)) {
            throw new IllegalArgumentException(
                    "vessels require P_O2 >= 0, LAMBDA > 0, and D_RECRUIT >= 0");
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
     * Sets the vessel network and marks the caches as out of date.
     *
     * @param network the vessel network
     */
    public void setNetwork(PottsVesselNetwork network) {
        this.network = network;
        prepared = false;
    }

    /**
     * Gets the vessel network.
     *
     * @return the vessel network, or {@code null} if not set
     */
    public PottsVesselNetwork getNetwork() {
        return network;
    }

    /** Marks the caches as out of date, so they are recomputed on next use. */
    public void refresh() {
        prepared = false;
    }

    /**
     * Gets a registered layer, checking that it exists and has a diffuser.
     *
     * @param sim the simulation instance
     * @param layer the layer id
     * @return the lattice
     */
    private PottsLattice getLayer(Simulation sim, String layer) {
        Lattice lattice = sim.getLattice(layer);
        if (lattice == null || lattice.getOperation(Category.DIFFUSER) == null) {
            throw new IllegalStateException(
                    "vessel layer [ " + layer + " ] requires a layer with a diffuser");
        }
        return (PottsLattice) lattice;
    }

    /**
     * Updates perfusion and the cached exchange weights and sites, if out of date.
     *
     * @param sim the simulation instance
     */
    void prepare(Simulation sim) {
        if (prepared) {
            return;
        }
        if (network == null) {
            throw new IllegalStateException("vessel component has no network");
        }
        if (layers.isEmpty()) {
            throw new IllegalStateException("vessel component has no registered layer");
        }

        PottsLattice lattice = getLayer(sim, layers.get(0));
        int length = lattice.getLength();
        int width = lattice.getWidth();
        double ds = lattice.getDs();

        PottsVesselPerfusion.update(network);

        exchangeVoxels.clear();
        exchangeWeights.clear();
        double[][] weights =
                PottsVesselDelivery.exchangeWeights(network, length, width, ds, lambda);
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (weights[i][j] > 0) {
                    exchangeVoxels.add(List.of(new Voxel(i, j, 0)));
                    exchangeWeights.add(weights[i][j]);
                }
            }
        }

        siteWeights = PottsVesselDelivery.siteWeights(network, length, width, ds, recruitDistance);
        prepared = true;
    }

    /**
     * Gets the first registered layer, which sets the lattice size, voxel size, and tick length.
     *
     * @param sim the simulation instance
     * @return the lattice
     */
    public PottsLattice getReferenceLattice(Simulation sim) {
        if (layers.isEmpty()) {
            throw new IllegalStateException("vessel component has no registered layer");
        }
        return getLayer(sim, layers.get(0));
    }

    /**
     * Gets the recruitment site weights.
     *
     * @param sim the simulation instance
     * @return the 2D site weights (integrity times vessel length [um]), indexed [x][y]
     */
    public double[][] getSiteWeights(Simulation sim) {
        prepare(sim);
        return siteWeights;
    }

    @Override
    public void step(SimState simstate) {
        if (permeability == 0) {
            return;
        }
        Simulation sim = (Simulation) simstate;
        prepare(sim);
        for (String layer : layers) {
            PottsLattice lattice = getLayer(sim, layer);
            for (int k = 0; k < exchangeVoxels.size(); k++) {
                lattice.addExchange(
                        exchangeVoxels.get(k), permeability * exchangeWeights.get(k), bloodValue);
            }
        }
    }
}
