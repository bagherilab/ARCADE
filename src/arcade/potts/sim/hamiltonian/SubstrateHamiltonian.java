package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import java.util.Set;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

/** Implementation of {@link Hamiltonian} for substrate energy. */
public class SubstrateHamiltonian implements Hamiltonian {
    /** Number of neighbors considered for substrate adhesion. */
    static final int NUMBER_NEIGHBORS = 9;

    /** Scaling at threshold height. */
    static final double THRESHOLD_FRACTION = 0.01;

    /** Map of hamiltonian config objects. */
    final HashMap<Integer, SubstrateHamiltonianConfig> configs;

    /** Map of population to substrate adhesion values. */
    final HashMap<Integer, Double> popToSubstrate;

    /** Grid tracking substrate values. */
    final int[][] substrates;

    /** Power for scaling substrate energy. */
    double power;

    /**
     * Creates the substrate energy term for the {@code Potts} Hamiltonian.
     *
     * @param series the associated Series instance
     * @param potts the associated Potts instance
     */
    public SubstrateHamiltonian(PottsSeries series, Potts potts) {
        configs = new HashMap<>();
        popToSubstrate = new HashMap<>();
        initialize(series);

        // Create substrate array.
        substrates = createSubstrate(potts.length + 2, potts.width + 2);
    }

    @Override
    public void register(PottsCell cell) {
        int pop = cell.getPop();
        double substrate = popToSubstrate.get(pop);
        SubstrateHamiltonianConfig config = new SubstrateHamiltonianConfig(substrate);
        configs.put(cell.getID(), config);
    }

    @Override
    public void deregister(PottsCell cell) {
        configs.remove(cell.getID());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Substrate energy is calculated by summing across substrate voxels below the given voxel.
     * Change in adhesion energy is taken as the difference in substrate energies for the source and
     * target IDs.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        double source = getSubstrate(sourceID, x, y, z);
        double target = getSubstrate(targetID, x, y, z);
        return target - source;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Substrate energy is set to zero. Region voxels cannot adhere to substrate.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
    }

    /**
     * Gets substrate energy for a given voxel.
     *
     * <p>Substrate is assumed to be located at z = 0. Media (id = 0) returns zero for substrate
     * energy. Substrate energy is scaled by distance from z = 0 by a power function.
     *
     * @param id the voxel id
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the energy
     */
    double getSubstrate(int id, int x, int y, int z) {
        if (id <= 0) {
            return 0;
        }

        double substrate = configs.get(id).getSubstrate();
        double substrateEnergy = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                substrateEnergy -= substrates[i][j];
            }
        }

        substrateEnergy = Math.pow(z, power) * substrateEnergy / NUMBER_NEIGHBORS * substrate;
        return substrateEnergy;
    }

    /**
     * Initializes parameters for substrate hamiltonian term.
     *
     * @param series the series instance
     */
    void initialize(PottsSeries series) {
        if (series.populations == null) {
            return;
        }

        Set<String> keySet = series.populations.keySet();
        MiniBox parameters = series.potts;

        for (String key : keySet) {
            MiniBox population = series.populations.get(key);
            int pop = population.getInt("CODE");

            // Get substrate adhesion value.
            double substrate = parameters.getDouble("substrate/ADHESION" + TARGET_SEPARATOR + key);
            popToSubstrate.put(pop, substrate);
        }

        // Set term parameters.
        double thresholdHeight = parameters.getDouble("substrate/HEIGHT_THRESHOLD");
        power = Math.log(THRESHOLD_FRACTION) / Math.log(thresholdHeight);
    }

    /**
     * Creates array representing substrate.
     *
     * @param length the length (x direction) of potts array
     * @param width the width (y direction) of potts array
     * @return the substrate array
     */
    int[][] createSubstrate(int length, int width) {
        int[][] arr = new int[length][width];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                arr[i][j] = 1;
            }
        }

        return arr;
    }
}
