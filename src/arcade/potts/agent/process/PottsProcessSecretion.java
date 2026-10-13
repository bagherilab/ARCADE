package arcade.potts.agent.process;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;

/**
 * Extension of {@link PottsProcess} for secretion into lattice layers.
 *
 * <p>Each tick, the cell secretes {@code RATE_[LAYER]} (amount per tick) into each layer. The rate
 * can be changed during the simulation with {@link #setRate(String, double)}. The amount is spread
 * evenly over the cell's own voxels ({@code INSIDE}) or the voxels adjacent to the cell that the
 * cell does not own ({@code BOUNDARY}), as set by {@code REGION_[LAYER]}. Amounts are added to the
 * lattice with {@link PottsLattice#addDelta(List, double)}, so they are added to the field at the
 * next lattice step.
 */
public class PottsProcessSecretion extends PottsProcess {
    /** Voxels that secreted amounts are added to. */
    public enum Placement {
        /** Voxels adjacent to the cell that the cell does not own. */
        BOUNDARY,

        /** Voxels owned by the cell. */
        INSIDE
    }

    /** Offsets to adjacent voxels in x, y, and z. */
    private static final int[][] OFFSETS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
    };

    /** Secretion rate for each layer [amount/tick]. */
    final TreeMap<String, Double> rates;

    /** Secretion placement for each layer. */
    final TreeMap<String, Placement> placements;

    /**
     * Creates a secretion process for the given {@link PottsCell}.
     *
     * <p>Loaded parameters include, for each layer:
     *
     * <ul>
     *   <li>{@code RATE_[LAYER]} = secretion rate [amount/tick]
     *   <li>{@code REGION_[LAYER]} = secretion placement ({@code BOUNDARY} or {@code INSIDE})
     * </ul>
     *
     * @param cell the {@link PottsCell} the process is associated with
     */
    public PottsProcessSecretion(PottsCell cell) {
        super(cell);
        rates = new TreeMap<>();
        placements = new TreeMap<>();

        Parameters parameters = cell.getParameters();
        MiniBox box = parameters.filter("secretion");
        for (String key : box.getKeys()) {
            if (!key.startsWith("REGION_")) {
                continue;
            }
            String layer = key.substring("REGION_".length());
            double rate = parameters.getDouble("secretion/RATE_" + layer);
            if (!(rate >= 0)) {
                throw new IllegalArgumentException(
                        "secretion rate for layer [ " + layer + " ] must be >= 0");
            }
            rates.put(layer, rate);
            placements.put(layer, Placement.valueOf(box.get(key)));
        }
    }

    /**
     * Gets the secretion rate for a layer.
     *
     * @param layer the layer id
     * @return the secretion rate [amount/tick], or {@code null} if the layer is not secreted
     */
    public Double getRate(String layer) {
        return rates.get(layer);
    }

    /**
     * Sets the secretion rate for a layer.
     *
     * @param layer the layer id
     * @param rate the secretion rate [amount/tick]
     */
    public void setRate(String layer, double rate) {
        if (!rates.containsKey(layer)) {
            throw new IllegalArgumentException("layer [ " + layer + " ] is not secreted");
        }
        if (!(rate >= 0)) {
            throw new IllegalArgumentException(
                    "secretion rate for layer [ " + layer + " ] must be >= 0");
        }
        rates.put(layer, rate);
    }

    /**
     * Gets the secretion placement for a layer.
     *
     * @param layer the layer id
     * @return the secretion placement, or {@code null} if the layer is not secreted
     */
    public Placement getPlacement(String layer) {
        return placements.get(layer);
    }

    /**
     * Gets voxels adjacent to the cell that the cell does not own.
     *
     * @param ids the potts array of cell ids
     * @return the list of unique boundary voxels within the array
     */
    List<Voxel> getBoundaryVoxels(int[][][] ids) {
        int id = cell.getID();
        LinkedHashSet<Voxel> boundary = new LinkedHashSet<>();
        for (Voxel voxel : location.getVoxels()) {
            for (int[] offset : OFFSETS) {
                int x = voxel.x + offset[0];
                int y = voxel.y + offset[1];
                int z = voxel.z + offset[2];
                if (z < 0
                        || z >= ids.length
                        || x < 0
                        || x >= ids[z].length
                        || y < 0
                        || y >= ids[z][x].length) {
                    continue;
                }
                if (ids[z][x][y] != id) {
                    boundary.add(new Voxel(x, y, z));
                }
            }
        }
        return new ArrayList<>(boundary);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        List<Voxel> boundary = null;

        for (String layer : rates.keySet()) {
            double amount = rates.get(layer);
            if (amount == 0) {
                continue;
            }

            List<Voxel> voxels;
            if (placements.get(layer) == Placement.INSIDE) {
                voxels = location.getVoxels();
            } else {
                if (boundary == null) {
                    boundary = getBoundaryVoxels(((PottsSimulation) sim).getPotts().ids);
                }
                voxels = boundary;
            }
            if (voxels.isEmpty()) {
                continue;
            }

            PottsLattice lattice = (PottsLattice) sim.getLattice(layer);
            double ds = lattice.getDs();
            lattice.addDelta(voxels, amount / (voxels.size() * ds * ds * ds));
        }
    }
}
