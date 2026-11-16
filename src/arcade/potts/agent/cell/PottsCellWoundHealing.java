package arcade.potts.agent.cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.agent.process.Process;
import arcade.core.env.lattice.Lattice;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.agent.module.PottsModuleApoptosisSimple;
import arcade.potts.agent.module.PottsModuleProliferationWoundHealing;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.agent.module.PottsModuleSenescence;
import arcade.potts.agent.process.PottsProcessSecretion;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.Domain;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

/**
 * Abstract extension of {@link PottsCell} for wound healing cells.
 *
 * <p>Each tick, a wound healing cell increases its age, applies the rules for its cell type (which
 * may change its state), steps its processes, and then steps the module for its state. Rules are
 * only applied to quiescent and proliferative cells.
 *
 * <p>Wound healing cells enter the proliferative state when {@link #canProliferate(Simulation)} is
 * true. Proliferative cells return to the quiescent state if it becomes false during G1 (see {@link
 * PottsModuleProliferationWoundHealing}). Senescent cells use {@link PottsModuleSenescence}.
 *
 * <p>Quiescent cells die (become apoptotic) with probability {@code BASAL_DEATH} each tick, before
 * the rules are applied. Proliferative cells die through the basal apoptosis of the proliferation
 * module, and senescent cells through clearance. Apoptotic cells secrete nothing: all secretion
 * rates are set to 0 when a cell becomes apoptotic.
 *
 * <p>If {@code O2_PROLIF_MIN} is above 0, cells cannot enter or remain in the proliferative state
 * when the mean O2 over the cell is below it.
 *
 * <p>Wound healing cells can become senescent through bystander routes: on contact with an
 * early-phase senescent cell ({@code BYSTANDER_CONTACT}) or when the mean INF over the cell is
 * above {@code INF_BYSTANDER} ({@code BYSTANDER_PARACRINE}). Contact induction is NOTCH1–JAG1
 * juxtacrine signalling, active only while cells enter senescence (Hoare et al. 2016; Teo et al.
 * 2019), so late-phase senescent cells act only through the paracrine route.
 *
 * <p>If {@code GF_UPTAKE} is above 0, quiescent and proliferative cells take up GF: each tick,
 * after the rules, the cell adds a first-order clearance at this rate to the voxels it occupies
 * (receptor-mediated endocytosis by target cells; Zhou et al. 2018, Adler et al. 2020). The GF
 * diffuser applies it inside its substeps, so clearance grows with the number of cells. Senescent
 * and apoptotic cells take up nothing.
 */
public abstract class PottsCellWoundHealing extends PottsCell {
    /**
     * Layer of signals from injured tissue (the wound and its cut edge). Its decay is clearance of
     * the signal, not repair of the tissue.
     */
    static final String INJURY_SIGNAL_LAYER = "INJURY_SIGNAL";

    /**
     * Layer of provisional wound matrix (fibrin clot and fibronectin) that fills a wound before
     * collagen ({@code ECM}) replaces it.
     */
    static final String WOUND_MATRIX_LAYER = "WOUND_MATRIX";

    /** Offsets to adjacent voxels in x, y, and z. */
    private static final int[][] OFFSETS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
    };

    /** Minimum free fraction of the boundary for proliferation. */
    final double contactFreeFraction;

    /** ECM at or above which an empty boundary voxel is not free, or 0 for no limit. */
    final double contactEcmMax;

    /** ECM + wound matrix below which an empty boundary voxel is not free, or 0 for no limit. */
    final double contactMatrixMin;

    /** Probability of spontaneous senescence (per tick). */
    final double spontaneousSenescence;

    /** Probability of senescence on contact with a senescent cell (per tick). */
    final double bystanderContact;

    /** Probability of senescence when INF is above the threshold (per tick). */
    final double bystanderParacrine;

    /** Threshold on mean INF for paracrine senescence. */
    final double infBystander;

    /** Minimum mean O2 for proliferation [mmHg], or 0 for no O2 gate. */
    final double o2ProlifMin;

    /** Probability of basal death for quiescent cells (per tick). */
    final double basalDeath;

    /** Layer taken up by {@code GF_UPTAKE}. */
    static final String GF_LAYER = "GF";

    /** First-order GF uptake rate (per tick), or 0 for none. */
    final double gfUptake;

    /** Boundary voxels for the current step, or {@code null} if not yet computed. */
    private List<Voxel> boundaryVoxels;

    /**
     * Creates a wound healing {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CONTACT_FREE_FRACTION} = minimum free fraction of the boundary for proliferation
     *   <li>{@code CONTACT_ECM_MAX} = ECM at or above which an empty voxel is not free (0 for no
     *       limit)
     *   <li>{@code CONTACT_MATRIX_MIN} = ECM + wound matrix below which an empty voxel is not free
     *       (0 for no limit)
     *   <li>{@code SPONTANEOUS_SENESCENCE} = probability of spontaneous senescence
     *   <li>{@code BYSTANDER_CONTACT} = probability of senescence on contact with a senescent cell
     *   <li>{@code BYSTANDER_PARACRINE} = probability of senescence when INF is above threshold
     *   <li>{@code INF_BYSTANDER} = threshold on mean INF for paracrine senescence
     *   <li>{@code O2_PROLIF_MIN} = minimum mean O2 for proliferation (0 for no O2 gate)
     *   <li>{@code BASAL_DEATH} = probability of basal death for quiescent cells
     *   <li>{@code GF_UPTAKE} = first-order GF uptake rate in occupied voxels (0 for none)
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellWoundHealing(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        contactFreeFraction = parameters.getDouble("CONTACT_FREE_FRACTION");
        contactEcmMax = parameters.getDouble("CONTACT_ECM_MAX");
        contactMatrixMin = parameters.getDouble("CONTACT_MATRIX_MIN");
        if (!(contactEcmMax >= 0)) {
            throw new IllegalArgumentException("CONTACT_ECM_MAX must be >= 0");
        }
        if (!(contactMatrixMin >= 0)) {
            throw new IllegalArgumentException("CONTACT_MATRIX_MIN must be >= 0");
        }
        if (contactEcmMax > 0 && contactMatrixMin >= contactEcmMax) {
            throw new IllegalArgumentException("CONTACT_MATRIX_MIN must be below CONTACT_ECM_MAX");
        }
        spontaneousSenescence = parameters.getDouble("SPONTANEOUS_SENESCENCE");
        bystanderContact = parameters.getDouble("BYSTANDER_CONTACT");
        bystanderParacrine = parameters.getDouble("BYSTANDER_PARACRINE");
        infBystander = parameters.getDouble("INF_BYSTANDER");
        o2ProlifMin = parameters.getDouble("O2_PROLIF_MIN");
        if (!(o2ProlifMin >= 0)) {
            throw new IllegalArgumentException("O2_PROLIF_MIN must be >= 0");
        }
        basalDeath = parameters.getDouble("BASAL_DEATH");
        if (!(basalDeath >= 0)) {
            throw new IllegalArgumentException("BASAL_DEATH must be >= 0");
        }
        gfUptake = parameters.getDouble("GF_UPTAKE");
        if (!(gfUptake >= 0)) {
            throw new IllegalArgumentException("GF_UPTAKE must be >= 0");
        }
    }

    /**
     * Adds the GF uptake of the cell to the GF lattice for the current tick.
     *
     * <p>Only quiescent and proliferative cells take up GF. The rate per tick is converted to a
     * rate per second for the diffuser.
     *
     * @param sim the simulation instance
     */
    void addGfUptake(Simulation sim) {
        if (gfUptake == 0) {
            return;
        }
        State state = (State) getState();
        if (state != State.QUIESCENT && state != State.PROLIFERATIVE) {
            return;
        }
        PottsLattice lattice = (PottsLattice) sim.getLattice(GF_LAYER);
        if (lattice == null) {
            throw new IllegalStateException("GF_UPTAKE requires layer [ " + GF_LAYER + " ]");
        }
        List<Voxel> voxels = ((PottsLocation) getLocation()).getVoxels();
        lattice.addClearance(voxels, gfUptake / (lattice.getDt() * 3600.0));
    }

    /**
     * Checks if the cell can enter or remain in the proliferative state.
     *
     * @param sim the simulation instance
     * @return {@code true} if the cell can proliferate, {@code false} otherwise
     */
    public abstract boolean canProliferate(Simulation sim);

    /**
     * Checks for basal death, and makes the cell apoptotic if it dies.
     *
     * <p>A random draw is only made if {@code BASAL_DEATH} is above 0.
     *
     * @param random the random number generator
     * @return {@code true} if the cell dies, {@code false} otherwise
     */
    boolean checkBasalDeath(MersenneTwisterFast random) {
        if (basalDeath > 0 && random.nextDouble() < basalDeath) {
            setState(State.APOPTOTIC);
            return true;
        }
        return false;
    }

    /**
     * Checks if the mean O2 over the cell is high enough for proliferation.
     *
     * <p>Severe hypoxia blocks proliferation; there is no O2 multiplier on the cell cycle, since
     * low O2 can stimulate dermal fibroblast proliferation (Falanga et al. 1993).
     *
     * @param sim the simulation instance
     * @return {@code true} if there is no O2 gate or mean O2 is at least {@code O2_PROLIF_MIN}
     */
    boolean hasOxygenToProliferate(Simulation sim) {
        if (o2ProlifMin <= 0) {
            return true;
        }
        Lattice o2 = sim.getLattice("O2");
        if (o2 == null) {
            throw new IllegalStateException("O2_PROLIF_MIN requires layer [ O2 ]");
        }
        return o2.getAverageValue(getLocation()) >= o2ProlifMin;
    }

    /**
     * Applies the rules for the cell type.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     * @return {@code true} if the cell is still in the simulation, {@code false} otherwise
     */
    abstract boolean stepRules(MersenneTwisterFast random, Simulation sim);

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;

        // Clear boundary voxels from the previous step.
        boundaryVoxels = null;

        // Increase age of cell.
        age++;

        // Check basal death for quiescent cells. Proliferative cells die through the proliferation
        // module's basal apoptosis, and senescent cells through clearance.
        State state = (State) getState();
        if (state == State.QUIESCENT && checkBasalDeath(simstate.random)) {
            state = State.APOPTOTIC;
        }

        // Apply rules for the cell type to quiescent and proliferative cells.
        if (state == State.QUIESCENT || state == State.PROLIFERATIVE) {
            if (!stepRules(simstate.random, sim)) {
                return;
            }
        }

        // Take up GF (after the rules, so a cell that has just become senescent does not).
        addGfUptake(sim);

        // Step processes.
        for (Process process : processes.values()) {
            process.step(simstate.random, sim);
        }

        // Step the module for the cell state.
        module.step(simstate.random, sim);
    }

    @Override
    public PottsCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
        divisions++;

        int newPop = links == null ? pop : links.next(random);

        return new PottsCellContainer(
                newID,
                id,
                newPop,
                age,
                divisions,
                newState,
                null,
                0,
                (hasRegions ? new EnumMap<>(Region.class) : null),
                criticalVolume,
                criticalHeight,
                criticalRegionVolumes,
                criticalRegionHeights);
    }

    @Override
    void setStateModule(CellState newState) {
        switch ((State) newState) {
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            case PROLIFERATIVE:
                module = new PottsModuleProliferationWoundHealing(this);
                break;
            case SENESCENT:
                module = new PottsModuleSenescence(this);
                break;
            case APOPTOTIC:
                module = new PottsModuleApoptosisSimple(this);
                stopSecretion();
                break;
            default:
                throw new IllegalArgumentException(
                        "wound healing cells do not support state [ " + newState + " ]");
        }
    }

    /**
     * Sets every secretion rate to 0.
     *
     * <p>Apoptotic cells secrete nothing: without this, a senescent cell keeps its last SASP rates
     * and a macrophage its phenotype rates until it is removed.
     */
    void stopSecretion() {
        if (!(getProcess(Domain.SECRETION) instanceof PottsProcessSecretion secretion)) {
            return;
        }
        for (String layer : secretion.getLayers()) {
            secretion.setRate(layer, 0);
        }
    }

    /**
     * Checks for spontaneous senescence.
     *
     * <p>A random draw is only made if the probability is non-zero.
     *
     * @param random the random number generator
     * @return {@code true} if the cell became senescent, {@code false} otherwise
     */
    boolean checkSpontaneousSenescence(MersenneTwisterFast random) {
        if (spontaneousSenescence > 0 && random.nextDouble() < spontaneousSenescence) {
            setState(State.SENESCENT);
            return true;
        }
        return false;
    }

    /**
     * Checks for bystander senescence by contact or by paracrine signaling.
     *
     * <p>Random draws are only made for routes with a non-zero probability.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     * @return {@code true} if the cell became senescent, {@code false} otherwise
     */
    boolean checkBystanderSenescence(MersenneTwisterFast random, Simulation sim) {
        if (bystanderContact > 0
                && touchesEarlySenescent(sim)
                && random.nextDouble() < bystanderContact) {
            setState(State.SENESCENT);
            return true;
        }
        if (bystanderParacrine > 0
                && getMean(sim, "INF") > infBystander
                && random.nextDouble() < bystanderParacrine) {
            setState(State.SENESCENT);
            return true;
        }
        return false;
    }

    /**
     * Checks if the cell is contact inhibited.
     *
     * @param freeFraction the free fraction of the boundary
     * @return {@code true} if the cell is contact inhibited, {@code false} otherwise
     */
    boolean isContactInhibited(double freeFraction) {
        return contactFreeFraction > 0 && freeFraction < contactFreeFraction;
    }

    /**
     * Gets the mean value of a layer over the cell.
     *
     * @param sim the simulation instance
     * @param layer the layer id
     * @return the mean value, or 0 if the layer does not exist
     */
    double getMean(Simulation sim, String layer) {
        Lattice lattice = sim.getLattice(layer);
        return lattice == null ? 0 : lattice.getAverageValue(getLocation());
    }

    /**
     * Gets voxels adjacent to the cell that the cell does not own.
     *
     * <p>The voxels are computed once per step. Potts updates happen before cells step, so the
     * cell's voxels do not change between the rules and the module in the same step.
     *
     * @param ids the potts array of cell ids
     * @return the read-only list of unique boundary voxels within the array
     */
    List<Voxel> getBoundaryVoxels(int[][][] ids) {
        if (boundaryVoxels == null) {
            boundaryVoxels = Collections.unmodifiableList(computeBoundaryVoxels(ids));
        }
        return boundaryVoxels;
    }

    /**
     * Computes voxels adjacent to the cell that the cell does not own.
     *
     * @param ids the potts array of cell ids
     * @return the list of unique boundary voxels within the array
     */
    private List<Voxel> computeBoundaryVoxels(int[][][] ids) {
        LinkedHashSet<Voxel> boundary = new LinkedHashSet<>();
        for (Voxel voxel : ((PottsLocation) getLocation()).getVoxels()) {
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

    /**
     * Gets boundary voxels that are empty and inside the domain.
     *
     * <p>The potts array has a one-voxel border that is always empty but is not part of the domain.
     *
     * @param ids the potts array of cell ids
     * @param boundary the boundary voxels
     * @return the list of free boundary voxels
     */
    static List<Voxel> getFreeVoxels(int[][][] ids, List<Voxel> boundary) {
        List<Voxel> free = new ArrayList<>();
        for (Voxel v : boundary) {
            boolean inside =
                    v.x >= 1
                            && v.y >= 1
                            && v.x <= ids[v.z].length - 2
                            && v.y <= ids[v.z][v.x].length - 2
                            && (ids.length == 1 || (v.z >= 1 && v.z <= ids.length - 2));
            if (inside && ids[v.z][v.x][v.y] == 0) {
                free.add(v);
            }
        }
        return free;
    }

    /**
     * Gets the fraction of boundary voxels that are free: empty, inside the domain, and with matrix
     * in range.
     *
     * <p>If {@code CONTACT_ECM_MAX} is above 0, an empty voxel with ECM at or above it is not free
     * (dense collagen leaves no room to grow). If {@code CONTACT_MATRIX_MIN} is above 0, an empty
     * voxel with ECM + wound matrix below it is not free (nothing to attach to). Missing layers
     * read 0.
     *
     * @param sim the simulation instance
     * @return the free fraction, or 0 if the cell has no boundary voxels
     */
    double getFreeFraction(Simulation sim) {
        int[][][] ids = ((PottsSimulation) sim).getPotts().ids;
        List<Voxel> boundary = getBoundaryVoxels(ids);
        if (boundary.isEmpty()) {
            return 0;
        }
        List<Voxel> free = getFreeVoxels(ids, boundary);
        if (contactEcmMax > 0 || contactMatrixMin > 0) {
            double[][][] ecm = getField(sim, "ECM");
            double[][][] matrix = getField(sim, WOUND_MATRIX_LAYER);
            free.removeIf(
                    v -> {
                        double e = ecm == null ? 0 : ecm[v.z][v.x][v.y];
                        double m = matrix == null ? 0 : matrix[v.z][v.x][v.y];
                        return (contactEcmMax > 0 && e >= contactEcmMax)
                                || (contactMatrixMin > 0 && e + m < contactMatrixMin);
                    });
        }
        return (double) free.size() / boundary.size();
    }

    /**
     * Gets the field of a layer.
     *
     * @param sim the simulation instance
     * @param layer the layer id
     * @return the field, or {@code null} if the layer does not exist
     */
    static double[][][] getField(Simulation sim, String layer) {
        Lattice lattice = sim.getLattice(layer);
        return lattice == null ? null : lattice.getField();
    }

    /**
     * Gets the mean value of a layer over the free boundary voxels.
     *
     * @param sim the simulation instance
     * @param layer the layer id
     * @return the mean value, or 0 if the layer does not exist or there are no free voxels
     */
    double getFreeMean(Simulation sim, String layer) {
        Lattice lattice = sim.getLattice(layer);
        int[][][] ids = ((PottsSimulation) sim).getPotts().ids;
        List<Voxel> free = getFreeVoxels(ids, getBoundaryVoxels(ids));
        if (lattice == null || free.isEmpty()) {
            return 0;
        }
        double[][][] field = lattice.getField();
        double sum = 0;
        for (Voxel v : free) {
            sum += field[v.z][v.x][v.y];
        }
        return sum / free.size();
    }

    /**
     * Checks if the cell touches a macrophage.
     *
     * @param sim the simulation instance
     * @return {@code true} if a boundary voxel belongs to a macrophage, {@code false} otherwise
     */
    public boolean touchesMacrophage(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        for (Voxel v : getBoundaryVoxels(potts.ids)) {
            if (potts.getCell(potts.ids[v.z][v.x][v.y]) instanceof PottsCellMacrophage) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the cell touches an early-phase senescent cell.
     *
     * <p>Only early-phase senescent cells induce senescence on contact: contact induction is
     * NOTCH1–JAG1 juxtacrine signalling, active only while cells enter senescence (Hoare et al.
     * 2016; Teo et al. 2019). Late-phase cells act through the paracrine route instead.
     *
     * @param sim the simulation instance
     * @return {@code true} if a boundary voxel belongs to an early-phase senescent cell, {@code
     *     false} otherwise
     */
    boolean touchesEarlySenescent(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        for (Voxel v : getBoundaryVoxels(potts.ids)) {
            PottsCell neighbor = potts.getCell(potts.ids[v.z][v.x][v.y]);
            if (neighbor != null
                    && neighbor.getState() == State.SENESCENT
                    && neighbor.getModule() instanceof PottsModule module
                    && module.getPhase() == Phase.SENESCENT_EARLY) {
                return true;
            }
        }
        return false;
    }
}
