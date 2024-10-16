package arcade.potts.sim.hamiltonian;

import java.util.EnumMap;
import arcade.potts.agent.cell.PottsCell;
import static arcade.potts.util.PottsEnums.Region;

/** Configuration for {@link AdhesionHamiltonian} parameters. */
class AdhesionHamiltonianConfig {
    /** Associated {@link PottsCell} instance. */
    final PottsCell cell;

    /** Adhesion values for cell. */
    final double[] adhesion;

    /** Adhesion values for cell by region. */
    final EnumMap<Region, EnumMap<Region, Double>> adhesionRegion;

    /** {@code true} if the cell has regions, {@code false} otherwise. */
    final boolean hasRegions;

    /**
     * Creates parameter configuration for {@code AdhesionHamiltonian}.
     *
     * @param cell the associated cell instance
     * @param adhesion the list of adhesion values
     * @param adhesionRegion the map of adhesion values for regions
     */
    AdhesionHamiltonianConfig(
            PottsCell cell,
            double[] adhesion,
            EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
        this.cell = cell;
        this.adhesion = adhesion.clone();
        this.hasRegions = (adhesionRegion != null) && (adhesionRegion.keySet().size() > 0);

        if (hasRegions) {
            this.adhesionRegion = new EnumMap<>(Region.class);
            for (Region region : adhesionRegion.keySet()) {
                this.adhesionRegion.put(region, adhesionRegion.get(region).clone());
            }
        } else {
            this.adhesionRegion = null;
        }
    }

    /**
     * Gets the adhesion to a cell of the given population.
     *
     * @param target the target cell population
     * @return the adhesion value
     */
    public double getAdhesion(int target) {
        return adhesion[target];
    }

    /**
     * Gets the adhesion between two regions.
     *
     * @param region1 the first region
     * @param region2 the second region
     * @return the adhesion value
     */
    public double getAdhesion(Region region1, Region region2) {
        return (hasRegions
                        && adhesionRegion.containsKey(region1)
                        && adhesionRegion.containsKey(region2)
                ? adhesionRegion.get(region1).get(region2)
                : Double.NaN);
    }
}
