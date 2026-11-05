package arcade.potts.agent.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import static arcade.potts.util.PottsEnums.Category;

/**
 * Extension of {@link PottsProcess} for uptake from lattice layers.
 *
 * <p>Each tick, the cell takes up each layer at the Michaelis-Menten rate V c / (K + c). The
 * maximum rate {@code RATE_[LAYER]} (amount per second, where amount is layer units times um^3) is
 * spread evenly over the cell's voxels and added to the lattice sink with {@link
 * PottsLattice#addSink(List, double)}. The diffuser of the layer solves the uptake inside its
 * substeps, with K set by the diffuser {@code HALF_SATURATION} parameter. Uptake needs a diffuser,
 * so a positive rate for a layer without one throws an exception.
 *
 * <p>For oxygen stored in mmHg, a cell uptake of 20 amol/s with a solubility of 1.31e-3 amol/(um^3
 * mmHg) is a rate of about 15270.
 */
public class PottsProcessUptake extends PottsProcess {
    /** Maximum uptake rate for each layer [amount/s]. */
    final TreeMap<String, Double> rates;

    /**
     * Creates an uptake process for the given {@link PottsCell}.
     *
     * <p>Loaded parameters include, for each layer:
     *
     * <ul>
     *   <li>{@code RATE_[LAYER]} = maximum uptake rate [amount/s]
     * </ul>
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    public PottsProcessUptake(PottsCell cell) {
        super(cell);
        rates = new TreeMap<>();

        Parameters parameters = cell.getParameters();
        MiniBox box = parameters.filter("uptake");

        // Layer names are keys RATE_[LAYER]. Keys that extend another layer key (for example the
        // RATE_[LAYER]_MU key of a distribution) are not layers.
        List<String> names = new ArrayList<>();
        for (String key : box.getKeys()) {
            if (key.startsWith("RATE_")) {
                names.add(key.substring("RATE_".length()));
            }
        }
        for (String layer : names) {
            boolean extendsOther = false;
            for (String other : names) {
                if (!other.equals(layer) && layer.startsWith(other + "_")) {
                    extendsOther = true;
                }
            }
            if (extendsOther) {
                continue;
            }
            double rate = parameters.getDouble("uptake/RATE_" + layer);
            if (!(rate >= 0)) {
                throw new IllegalArgumentException(
                        "uptake rate for layer [ " + layer + " ] must be >= 0");
            }
            rates.put(layer, rate);
        }
    }

    /**
     * Gets the layers taken up by the process.
     *
     * @return a read-only copy of the set of layer ids
     */
    public Set<String> getLayers() {
        return Set.copyOf(rates.keySet());
    }

    /**
     * Gets the maximum uptake rate for a layer.
     *
     * @param layer the layer id
     * @return the maximum uptake rate [amount/s], or {@code null} if the layer is not taken up
     */
    public Double getRate(String layer) {
        return rates.get(layer);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        List<Voxel> voxels = location.getVoxels();
        if (voxels.isEmpty()) {
            return;
        }

        for (String layer : rates.keySet()) {
            double rate = rates.get(layer);
            if (rate == 0) {
                continue;
            }

            PottsLattice lattice = (PottsLattice) sim.getLattice(layer);
            if (lattice == null || lattice.getOperation(Category.DIFFUSER) == null) {
                throw new IllegalStateException(
                        "uptake from layer [ " + layer + " ] requires a layer with a diffuser");
            }

            double ds = lattice.getDs();
            lattice.addSink(voxels, rate / (voxels.size() * ds * ds * ds));
        }
    }
}
