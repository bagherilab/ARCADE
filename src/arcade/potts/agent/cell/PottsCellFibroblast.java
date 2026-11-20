package arcade.potts.agent.cell;

import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.Cell;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

/**
 * Extension of {@link PottsCellWoundHealing} for fibroblasts.
 *
 * <p>{@code GF} is the growth-factor layer: the pro-repair growth factors (PDGF, TGF-β) secreted by
 * macrophages and senescent cells, lumped into one field.
 *
 * <p>Fibroblasts proliferate when the mean GF over the cell is above {@code GF_PROLIFERATION} and
 * they are not contact inhibited. Proliferating fibroblasts continue through G1 while GF is at
 * least half of the threshold. If {@code WOUND_MATRIX_PROLIFERATION} is above 0, proliferation also
 * needs the mean {@code WOUND_MATRIX} over the cell to be above it (at least half of it to stay
 * proliferative), so fibroblasts divide on the provisional matrix of the wound and not in intact
 * dermis far from it.
 *
 * <p>If {@code REPOPULATION_DENSITY} is above 0 (it needs the wound matrix gate and the {@code
 * MATURE_ECM} layer), a fibroblast that fails only the wound matrix gate may still proliferate when
 * it sits in new tissue (mean {@code MATURE_ECM} over the cell below {@link
 * #NEW_TISSUE_MATURE_ECM}) and the local cell density is below {@code REPOPULATION_DENSITY}. Local
 * density is the number of cell centroids within {@code REPOPULATION_RADIUS} of the cell centroid
 * in x and y, divided by the area of that disc inside the domain. So after the clot is gone,
 * fibroblasts in the wound keep dividing (while GF lasts) until the wound is back to normal
 * density, but never in intact dermis.
 *
 * <p>When GF is above {@code GF_DIFFERENTIATION} and ECM is above {@code ECM_DIFFERENTIATION},
 * fibroblasts differentiate with probability {@code DIFFERENTIATION_RATE} into a cell of the {@code
 * DIFFERENTIATION_TARGET} population, which replaces the fibroblast with the same id and location.
 * If {@code WOUND_MATRIX_DIFFERENTIATION} is above 0, differentiation also needs the mean {@code
 * WOUND_MATRIX} over the cell to be above it, so myofibroblasts form only where there is wound
 * matrix: the provisional matrix (fibrin clot and fibronectin, including the ED-A fibronectin
 * myofibroblasts need) that fills a wound before collagen replaces it. Fibroblasts can become
 * senescent spontaneously ({@code SPONTANEOUS_SENESCENCE}) or through the bystander routes.
 */
public final class PottsCellFibroblast extends PottsCellWoundHealing {
    /** Threshold on mean GF for proliferation. */
    final double gfProliferation;

    /** Threshold on mean wound matrix for proliferation (0 for no gate). */
    final double woundMatrixProliferation;

    /** Mean {@code MATURE_ECM} below which a cell sits in new tissue. */
    static final double NEW_TISSUE_MATURE_ECM = 0.5;

    /** Layer of the dermal ECM present at the start (intact, cross-linked dermis). */
    static final String MATURE_ECM_LAYER = "MATURE_ECM";

    /** Local density (cells per voxel area) below which repopulation is allowed, 0 for off. */
    final double repopulationDensity;

    /** Radius (voxels) for local density. */
    final double repopulationRadius;

    /** Threshold on mean GF for differentiation. */
    final double gfDifferentiation;

    /** Threshold on mean ECM for differentiation. */
    final double ecmDifferentiation;

    /** Threshold on mean wound matrix for differentiation, or 0 for no gate. */
    final double woundMatrixDifferentiation;

    /** Probability of differentiation when above thresholds (per tick). */
    final double differentiationRate;

    /** Population id cells differentiate into, or {@code NONE}. */
    final String differentiationTarget;

    /**
     * Creates a fibroblast {@code PottsCell} agent.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code GF_PROLIFERATION} = threshold on mean GF for proliferation
     *   <li>{@code WOUND_MATRIX_PROLIFERATION} = threshold on mean wound matrix for proliferation
     *       (0 for no gate)
     *   <li>{@code REPOPULATION_DENSITY} = local density below which fibroblasts in new tissue may
     *       proliferate without wound matrix (0 for off)
     *   <li>{@code REPOPULATION_RADIUS} = radius for local density
     *   <li>{@code GF_DIFFERENTIATION} = threshold on mean GF for differentiation
     *   <li>{@code ECM_DIFFERENTIATION} = threshold on mean ECM for differentiation
     *   <li>{@code WOUND_MATRIX_DIFFERENTIATION} = threshold on mean wound matrix for
     *       differentiation (0 for no gate)
     *   <li>{@code DIFFERENTIATION_RATE} = probability of differentiation above thresholds
     *   <li>{@code DIFFERENTIATION_TARGET} = population id cells differentiate into, or {@code
     *       NONE}
     * </ul>
     *
     * @param container the cell container
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     * @param links the map of population links
     */
    public PottsCellFibroblast(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        gfProliferation = parameters.getDouble("GF_PROLIFERATION");
        woundMatrixProliferation = readWoundMatrixProliferation(parameters);
        repopulationDensity = parameters.getDouble("REPOPULATION_DENSITY");
        repopulationRadius = parameters.getDouble("REPOPULATION_RADIUS");
        if (!(repopulationDensity >= 0)) {
            throw new IllegalArgumentException("REPOPULATION_DENSITY must be >= 0");
        }
        if (repopulationDensity > 0 && !(repopulationRadius > 0)) {
            throw new IllegalArgumentException("REPOPULATION_RADIUS must be > 0");
        }
        if (repopulationDensity > 0 && woundMatrixProliferation == 0) {
            throw new IllegalArgumentException(
                    "REPOPULATION_DENSITY needs WOUND_MATRIX_PROLIFERATION > 0");
        }
        gfDifferentiation = parameters.getDouble("GF_DIFFERENTIATION");
        ecmDifferentiation = parameters.getDouble("ECM_DIFFERENTIATION");
        woundMatrixDifferentiation = parameters.getDouble("WOUND_MATRIX_DIFFERENTIATION");
        if (!(woundMatrixDifferentiation >= 0)) {
            throw new IllegalArgumentException("WOUND_MATRIX_DIFFERENTIATION must be >= 0");
        }
        differentiationRate = parameters.getDouble("DIFFERENTIATION_RATE");
        differentiationTarget = parameters.getString("DIFFERENTIATION_TARGET");
    }

    @Override
    public boolean canProliferate(Simulation sim) {
        if (canProliferate(sim, this, gfProliferation, woundMatrixProliferation)) {
            return true;
        }
        return repopulationDensity > 0
                && isNewTissue(sim)
                && canProliferate(sim, this, gfProliferation, 0)
                && getLocalDensity(sim) < repopulationDensity;
    }

    /**
     * Checks if the cell sits in new tissue (not intact dermis).
     *
     * @param sim the simulation instance
     * @return {@code true} if mean {@code MATURE_ECM} is below {@link #NEW_TISSUE_MATURE_ECM}
     * @throws IllegalStateException if there is no {@code MATURE_ECM} layer
     */
    boolean isNewTissue(Simulation sim) {
        if (sim.getLattice(MATURE_ECM_LAYER) == null) {
            throw new IllegalStateException("REPOPULATION_DENSITY needs the MATURE_ECM layer");
        }
        return getMean(sim, MATURE_ECM_LAYER) < NEW_TISSUE_MATURE_ECM;
    }

    /**
     * Gets the local cell density around the cell.
     *
     * <p>Counts the cells (including this one) whose centroid is within the radius of this cell's
     * centroid in x and y, and divides by the number of voxel positions of that disc that lie
     * inside the domain, so density is not underestimated at the domain edges. Vessel cells are not
     * tissue: they are not counted, and voxels they own are left out of the area.
     *
     * @param sim the simulation instance
     * @return the number of cells per voxel area
     */
    double getLocalDensity(Simulation sim) {
        double[] c = ((PottsLocation) getLocation()).getCentroid();
        double r2 = repopulationRadius * repopulationRadius;
        int count = 0;
        boolean vessels = false;
        for (Object obj : sim.getGrid().getAllObjects()) {
            if (obj instanceof PottsCellVessel) {
                vessels = true;
                continue;
            }
            double[] d = ((PottsLocation) ((Cell) obj).getLocation()).getCentroid();
            double dx = d[0] - c[0];
            double dy = d[1] - c[1];
            if (dx * dx + dy * dy <= r2) {
                count++;
            }
        }
        Potts potts = ((PottsSimulation) sim).getPotts();
        int[][] plane = potts.ids[0];
        int r = (int) Math.ceil(repopulationRadius);
        int area = 0;
        for (int x = (int) Math.floor(c[0]) - r; x <= (int) Math.ceil(c[0]) + r; x++) {
            for (int y = (int) Math.floor(c[1]) - r; y <= (int) Math.ceil(c[1]) + r; y++) {
                boolean inside =
                        x >= 1 && x < plane.length - 1 && y >= 1 && y < plane[0].length - 1;
                double dx = x - c[0];
                double dy = y - c[1];
                if (inside
                        && dx * dx + dy * dy <= r2
                        && !(vessels && isVessel(potts, plane[x][y]))) {
                    area++;
                }
            }
        }
        return area == 0 ? 0 : (double) count / area;
    }

    /**
     * Checks if a voxel is owned by a vessel cell.
     *
     * @param potts the potts instance
     * @param id the id at the voxel
     * @return {@code true} if the voxel belongs to a {@link PottsCellVessel}
     */
    private static boolean isVessel(Potts potts, int id) {
        return id > 0 && potts.getCell(id) instanceof PottsCellVessel;
    }

    /**
     * Reads and checks the wound matrix threshold for proliferation.
     *
     * @param parameters the dictionary of parameters
     * @return the threshold, 0 for no gate
     */
    static double readWoundMatrixProliferation(Parameters parameters) {
        double threshold = parameters.getDouble("WOUND_MATRIX_PROLIFERATION");
        if (!(threshold >= 0)) {
            throw new IllegalArgumentException("WOUND_MATRIX_PROLIFERATION must be >= 0");
        }
        return threshold;
    }

    /**
     * Checks if a fibroblast or myofibroblast can enter or remain in the proliferative state.
     *
     * <p>A quiescent cell needs mean GF above the threshold. A proliferative cell needs mean GF at
     * least half of the threshold. If the wound matrix threshold is above 0, the mean wound matrix
     * follows the same rule. All need enough O2 and to not be contact inhibited.
     *
     * @param sim the simulation instance
     * @param cell the cell
     * @param threshold the threshold on mean GF
     * @param woundMatrixThreshold the threshold on mean wound matrix, 0 for no gate
     * @return {@code true} if the cell can proliferate, {@code false} otherwise
     */
    static boolean canProliferate(
            Simulation sim,
            PottsCellWoundHealing cell,
            double threshold,
            double woundMatrixThreshold) {
        boolean proliferative = cell.getState() == State.PROLIFERATIVE;
        double gf = cell.getMean(sim, "GF");
        boolean enoughGf = proliferative ? gf >= threshold / 2 : gf > threshold;
        boolean enoughMatrix = true;
        if (woundMatrixThreshold > 0) {
            double matrix = cell.getMean(sim, WOUND_MATRIX_LAYER);
            enoughMatrix =
                    proliferative
                            ? matrix >= woundMatrixThreshold / 2
                            : matrix > woundMatrixThreshold;
        }
        return enoughGf
                && enoughMatrix
                && cell.hasOxygenToProliferate(sim)
                && !cell.isContactInhibited(cell.getFreeFraction(sim));
    }

    @Override
    boolean stepRules(MersenneTwisterFast random, Simulation sim) {
        if (checkSpontaneousSenescence(random) || checkBystanderSenescence(random, sim)) {
            return true;
        }

        if (!differentiationTarget.equals("NONE")
                && hasWoundMatrix(sim)
                && getMean(sim, "GF") > gfDifferentiation
                && getMean(sim, "ECM") > ecmDifferentiation
                && random.nextDouble() < differentiationRate) {
            differentiate(random, sim);
            return false;
        }

        if (getState() == State.QUIESCENT && canProliferate(sim)) {
            setState(State.PROLIFERATIVE);
        }

        return true;
    }

    /**
     * Checks if differentiation is allowed by the wound matrix.
     *
     * @param sim the simulation instance
     * @return {@code true} if there is no gate, or the mean wound matrix is above the gate
     */
    boolean hasWoundMatrix(Simulation sim) {
        return woundMatrixDifferentiation == 0
                || getMean(sim, WOUND_MATRIX_LAYER) > woundMatrixDifferentiation;
    }

    /**
     * Replaces the fibroblast with a cell of the differentiation target population.
     *
     * <p>The new cell has the same id, parent, age, divisions, state, and location. A proliferative
     * cell starts the cell cycle again from G1.
     *
     * @param random the random number generator
     * @param sim the simulation instance
     */
    void differentiate(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        Location location = getLocation();
        int newPop = sim.getSeries().populations.get(differentiationTarget).getInt("CODE");

        // Remove the fibroblast from the simulation.
        sim.getGrid().removeObject(this, location);
        potts.deregister(this);
        stop();

        // Add the differentiated cell to the simulation.
        PottsCellContainer container =
                new PottsCellContainer(
                        id,
                        parent,
                        newPop,
                        age,
                        divisions,
                        getState(),
                        null,
                        0,
                        (hasRegions ? new EnumMap<>(Region.class) : null),
                        criticalVolume,
                        criticalHeight,
                        criticalRegionVolumes,
                        criticalRegionHeights);
        PottsCell cell = (PottsCell) container.convert(sim.getCellFactory(), location, random);
        sim.getGrid().addObject(cell, null);
        potts.register(cell);
        cell.initialize(potts.ids, potts.regions);
        cell.schedule(sim.getSchedule());
    }
}
