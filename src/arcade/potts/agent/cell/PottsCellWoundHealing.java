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
import arcade.potts.agent.module.PottsModuleApoptosisSimple;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationWoundHealing;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.agent.module.PottsModuleSenescence;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
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
 * <p>Wound healing cells can become senescent through bystander routes: on contact with a senescent
 * cell ({@code BYSTANDER_CONTACT}) or when the mean INF over the cell is above {@code
 * INF_BYSTANDER} ({@code BYSTANDER_PARACRINE}).
 */
public abstract class PottsCellWoundHealing extends PottsCell {
    /** Offsets to adjacent voxels in x, y, and z. */
    private static final int[][] OFFSETS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1},
    };

    /** Minimum free fraction of the boundary for proliferation. */
    final double contactFreeFraction;

    /** Probability of spontaneous senescence (per tick). */
    final double spontaneousSenescence;

    /** Probability of senescence on contact with a senescent cell (per tick). */
    final double bystanderContact;

    /** Probability of senescence when INF is above the threshold (per tick). */
    final double bystanderParacrine;

    /** Threshold on mean INF for paracrine senescence. */
    final double infBystander;

    /** Boundary voxels for the current step, or {@code null} if not yet computed. */
    private List<Voxel> boundaryVoxels;

    /**
     * Creates a wound healing {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CONTACT_FREE_FRACTION} = minimum free fraction of the boundary for proliferation
     *   <li>{@code SPONTANEOUS_SENESCENCE} = probability of spontaneous senescence
     *   <li>{@code BYSTANDER_CONTACT} = probability of senescence on contact with a senescent cell
     *   <li>{@code BYSTANDER_PARACRINE} = probability of senescence when INF is above threshold
     *   <li>{@code INF_BYSTANDER} = threshold on mean INF for paracrine senescence
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
        spontaneousSenescence = parameters.getDouble("SPONTANEOUS_SENESCENCE");
        bystanderContact = parameters.getDouble("BYSTANDER_CONTACT");
        bystanderParacrine = parameters.getDouble("BYSTANDER_PARACRINE");
        infBystander = parameters.getDouble("INF_BYSTANDER");
    }

    /**
     * Checks if the cell can enter or remain in the proliferative state.
     *
     * @param sim the simulation instance
     * @return {@code true} if the cell can proliferate, {@code false} otherwise
     */
    public abstract boolean canProliferate(Simulation sim);

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

        // Apply rules for the cell type to quiescent and proliferative cells.
        State state = (State) getState();
        if (state == State.QUIESCENT || state == State.PROLIFERATIVE) {
            if (!stepRules(simstate.random, sim)) {
                return;
            }
        }

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
                break;
            case NECROTIC:
                module = new PottsModuleNecrosis(this);
                break;
            case AUTOTIC:
                module = new PottsModuleAutosis(this);
                break;
            default:
                // State must be one of the above cases.
                module = null;
                break;
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
                && touchesSenescent(sim)
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
     * Gets the fraction of boundary voxels that are empty and inside the domain.
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
        return (double) getFreeVoxels(ids, boundary).size() / boundary.size();
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
     * Checks if the cell touches a senescent cell.
     *
     * @param sim the simulation instance
     * @return {@code true} if a boundary voxel belongs to a senescent cell, {@code false} otherwise
     */
    boolean touchesSenescent(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        for (Voxel v : getBoundaryVoxels(potts.ids)) {
            PottsCell neighbor = potts.getCell(potts.ids[v.z][v.x][v.y]);
            if (neighbor != null && neighbor.getState() == State.SENESCENT) {
                return true;
            }
        }
        return false;
    }
}
