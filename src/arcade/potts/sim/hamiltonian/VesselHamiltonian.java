package arcade.potts.sim.hamiltonian;

import java.util.HashSet;
import java.util.Set;
import arcade.potts.agent.cell.PottsCell;

/**
 * Implementation of {@link Hamiltonian} that holds vessel cells on their footprints.
 *
 * <p>Each vessel cell may have an anchor, the set of voxels it should occupy, and a guide, a set of
 * voxels it is drawn toward. A cell with no anchor has no energy. With anchor weight {@code
 * lambdaA} and guide weight {@code lambdaG}, a flip of voxel v from source to target changes the
 * energy by the sum of:
 *
 * <ul>
 *   <li>target gains v: {@code +lambdaA} if v is not in its anchor, {@code -lambdaA} if it is, and
 *       {@code -lambdaG} if v is in its guide;
 *   <li>source loses v: {@code +lambdaA} if v is in its anchor, {@code -lambdaA} if not, and {@code
 *       +lambdaG} if v is in its guide.
 * </ul>
 *
 * <p>A positive change makes the flip less likely. Ids at or below 0 (medium) and cells without an
 * anchor contribute 0. The lattice is treated as 2D: z is ignored. Cells are not registered by the
 * term; a component sets the anchor and guide of each cell with {@link #setAnchor} and {@link
 * #setGuide}.
 *
 * <p>A component may also mark cells as tip cells ({@link #setTip}) or vessel cells ({@link
 * #setVessel}). A tip cell cannot gain a voxel from a vessel cell: such a flip has an infinite
 * energy change, so it is never accepted. Tip cells migrate into tissue, not through the vessel
 * wall. Vessel cells may take voxels from tip cells as usual.
 */
public class VesselHamiltonian implements Hamiltonian {
    /** Anchor voxel keys by cell id. */
    final IdMap<Set<Long>> anchors = new IdMap<>();

    /** Anchor weight by cell id. */
    final IdMap<Double> anchorLambdas = new IdMap<>();

    /** Guide voxel keys by cell id. */
    final IdMap<Set<Long>> guides = new IdMap<>();

    /** Guide weight by cell id. */
    final IdMap<Double> guideLambdas = new IdMap<>();

    /** Ids of tip cells. */
    final Set<Integer> tipIds = new HashSet<>();

    /** Ids of vessel cells. */
    final Set<Integer> vesselIds = new HashSet<>();

    /** Creates the vessel energy term for the {@code Potts} Hamiltonian. */
    public VesselHamiltonian() {}

    /**
     * Packs a 2D voxel into a key.
     *
     * <p>The x coordinate fills the high 32 bits and the y coordinate the low 32 bits, so distinct
     * voxels never share a key.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the key
     */
    public static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    /**
     * Sets the anchor of a cell, replacing any previous one.
     *
     * @param id the cell id
     * @param voxelKeys the keys of the voxels (see {@link #key}) the cell should occupy
     * @param lambdaAnchor the anchor weight
     */
    public void setAnchor(int id, Set<Long> voxelKeys, double lambdaAnchor) {
        anchors.put(id, new HashSet<>(voxelKeys));
        anchorLambdas.put(id, lambdaAnchor);
    }

    /**
     * Sets the guide of a cell, replacing any previous one.
     *
     * @param id the cell id
     * @param voxelKeys the keys of the voxels (see {@link #key}) the cell is drawn toward
     * @param lambdaGuide the guide weight
     */
    public void setGuide(int id, Set<Long> voxelKeys, double lambdaGuide) {
        guides.put(id, new HashSet<>(voxelKeys));
        guideLambdas.put(id, lambdaGuide);
    }

    /**
     * Removes the anchor and guide of a cell; its tip or vessel mark is kept.
     *
     * @param id the cell id
     */
    public void clear(int id) {
        anchors.remove(id);
        anchorLambdas.remove(id);
        guides.remove(id);
        guideLambdas.remove(id);
    }

    /**
     * Marks a cell as a tip cell.
     *
     * @param id the cell id
     */
    public void setTip(int id) {
        vesselIds.remove(id);
        tipIds.add(id);
    }

    /**
     * Marks a cell as a vessel cell.
     *
     * @param id the cell id
     */
    public void setVessel(int id) {
        tipIds.remove(id);
        vesselIds.add(id);
    }

    /**
     * Removes the anchor, guide, and tip or vessel mark of a cell.
     *
     * @param id the cell id
     */
    public void forget(int id) {
        clear(id);
        tipIds.remove(id);
        vesselIds.remove(id);
    }

    /** {@inheritDoc} A cell has energy only after {@link #setAnchor}. */
    @Override
    public void register(PottsCell cell) {}

    /** {@inheritDoc} */
    @Override
    public void deregister(PottsCell cell) {}

    /**
     * {@inheritDoc}
     *
     * <p>A tip cell gaining a voxel from a vessel cell gives {@link Double#POSITIVE_INFINITY}, so
     * the flip is never accepted.
     */
    @Override
    public double getDelta(int sourceID, int targetID, int x, int y, int z) {
        if (tipIds.contains(targetID) && vesselIds.contains(sourceID)) {
            return Double.POSITIVE_INFINITY;
        }
        long key = key(x, y);
        return gain(targetID, key) + loss(sourceID, key);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Vessel energy is zero for changes between regions of a cell.
     */
    @Override
    public double getDelta(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
        return 0;
    }

    /**
     * Gets the energy change for a cell gaining a voxel.
     *
     * @param id the cell id
     * @param key the voxel key
     * @return the energy change
     */
    double gain(int id, long key) {
        Set<Long> anchor = id > 0 ? anchors.get(id) : null;
        if (anchor == null) {
            return 0;
        }
        double delta = anchor.contains(key) ? -anchorLambdas.get(id) : anchorLambdas.get(id);
        Set<Long> guide = guides.get(id);
        if (guide != null && guide.contains(key)) {
            delta -= guideLambdas.get(id);
        }
        return delta;
    }

    /**
     * Gets the energy change for a cell losing a voxel.
     *
     * @param id the cell id
     * @param key the voxel key
     * @return the energy change
     */
    double loss(int id, long key) {
        Set<Long> anchor = id > 0 ? anchors.get(id) : null;
        if (anchor == null) {
            return 0;
        }
        double delta = anchor.contains(key) ? anchorLambdas.get(id) : -anchorLambdas.get(id);
        Set<Long> guide = guides.get(id);
        if (guide != null && guide.contains(key)) {
            delta += guideLambdas.get(id);
        }
        return delta;
    }
}
