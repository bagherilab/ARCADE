package arcade.potts.agent.module;

import java.util.ArrayList;
import sim.util.Double3D;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellFactory;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
import arcade.core.util.distributions.UniformDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Phase;
import arcade.potts.util.PottsEnums.State;
import static arcade.potts.agent.cell.PottsCellFlyStem.StemType;

/**
 * Extension of {@link PottsModuleProliferationSimple} with a custom addCell method for fly stem
 * cell behavior.
 */
public class PottsModuleProliferationFlyStem extends PottsModuleProliferation {

    /** Threshold for critical volume size checkpoint. */
    static final double SIZE_CHECKPOINT = 0.95;

    /**
     * Target ratio of critical volume for division size checkpoint (cell must reach CRITICAL_VOLUME
     * * SIZE_TARGET * SIZE_CHECKPOINT to divide).
     */
    double sizeTarget;

    /** Event rate for G1 phase (steps/tick). */
    double rateG1;

    /** Event rate for S phase (steps/tick). */
    double rateS;

    /** Event rate for G2 phase (steps/tick). */
    double rateG2;

    /** Event rate for M phase (steps/tick). */
    double rateM;

    /** Steps for G1 phase (steps). */
    final int stepsG1;

    /** Steps for S phase (steps). */
    final int stepsS;

    /** Steps for G2 phase (steps). */
    final int stepsG2;

    /** Steps for M phase (steps). */
    final int stepsM;

    /**
     * Overall growth rate for cell (voxels/tick) when growth rate is not dynamic. Max growth rate
     * when growth rate is dynamic
     */
    final double cellGrowthRateMax;

    double cellGrowthRate;

    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;

    /** Fraction of nuclear volume when condensed. */
    double nucleusCondFraction;

    public static final double EPSILON = 1e-8;

    /** Distribution that determines rotational offset of cell's division plane. */
    final NormalDistribution splitDirectionDistribution;

    /** Ruleset for determining which daughter cell is the GMC. Can be `volume` or `location`. */
    final String differentiationRuleset;

    final String apicalAxisRuleset;

    final Distribution apicalAxisRotationDistribution;

    final boolean dynamicGrowthRateVolume;

    final double dynamicGrowthRateMultiplier;

    final boolean volumeBasedCriticalVolume;

    final double volumeBasedCriticalVolumeMultiplier;

    /**
     * Range of values considered equal when determining daughter cell identity. ex. if ruleset is
     * location, range determines the distance between centroid y values that is considered equal.
     */
    final double range;

    /**
     * Creates a simple proliferation {@code Module} for the given {@link PottsCellFlyStem}.
     *
     * @param cell the {@link PottsCellFlyStem} the module is associated with
     */
    public PottsModuleProliferationFlyStem(PottsCellFlyStem cell) {
        super(cell);

        if (cell.hasRegions()) {
            throw new UnsupportedOperationException(
                    "Regions are not yet implemented for fly cells");
        }

        Parameters parameters = cell.getParameters();

        sizeTarget = parameters.getDouble("proliferation/SIZE_TARGET");
        rateG1 = parameters.getDouble("proliferation/RATE_G1");
        rateS = parameters.getDouble("proliferation/RATE_S");
        rateG2 = parameters.getDouble("proliferation/RATE_G2");
        rateM = parameters.getDouble("proliferation/RATE_M");
        stepsG1 = parameters.getInt("proliferation/STEPS_G1");
        stepsS = parameters.getInt("proliferation/STEPS_S");
        stepsG2 = parameters.getInt("proliferation/STEPS_G2");
        stepsM = parameters.getInt("proliferation/STEPS_M");
        cellGrowthRateMax = parameters.getDouble("proliferation/CELL_GROWTH_RATE");
        basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
        nucleusCondFraction = parameters.getDouble("proliferation/NUCLEUS_CONDENSATION_FRACTION");

        splitDirectionDistribution =
                (NormalDistribution)
                        parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION");
        differentiationRuleset = parameters.getString("proliferation/DIFFERENTIATION_RULESET");
        range = parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE");
        apicalAxisRuleset = parameters.getString("proliferation/APICAL_AXIS_RULESET");
        apicalAxisRotationDistribution =
                (Distribution)
                        parameters.getDistribution(
                                "proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION");

        dynamicGrowthRateVolume =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME") != 0);
        dynamicGrowthRateMultiplier = parameters.getDouble("proliferation/GROWTH_RATE_MULTIPLIER");
        updateVolumeBasedGrowthRate();

        volumeBasedCriticalVolume =
                (parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME") != 0);
        volumeBasedCriticalVolumeMultiplier =
                parameters.getDouble("proliferation/VOLUME_BASED_CRITICAL_VOLUME_MULTIPLIER");
    }

    void updateVolumeBasedGrowthRate() {
        if (dynamicGrowthRateVolume == false) {
            cellGrowthRate = cellGrowthRateMax;
        } else {
            cellGrowthRate =
                    cellGrowthRateMax
                            * dynamicGrowthRateMultiplier
                            * (cell.getLocation().getVolume() / cell.getCriticalVolume());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cell increases in size toward a target of twice its critical size at a rate of {@code
     * CELL_GROWTH_RATE}. Cell will transition to S phase after completing {@code STEPS_G1} steps at
     * an average rate of {@code RATE_G1}. At each tick, cell may randomly apoptosis at a basal rate
     * of {@code BASAL_APOPTOSIS_RATE}.
     */
    @Override
    void stepG1(MersenneTwisterFast random) {
        // Update growth rate.
        updateVolumeBasedGrowthRate();
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, sizeTarget);

        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateG1, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsG1) {
            setPhase(Phase.PROLIFERATIVE_S);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cell increases in size toward a target of twice its critical size at a rate of {@code
     * CELL_GROWTH_RATE}. Cell will transition to G2 phase after completing {@code STEPS_S} steps at
     * an average rate of {@code RATE_S}.
     */
    @Override
    void stepS(MersenneTwisterFast random) {
        // Update growth rate.
        updateVolumeBasedGrowthRate();
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, sizeTarget);

        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateS, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsS) {
            setPhase(Phase.PROLIFERATIVE_G2);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cell increases in size toward a target of twice its critical size at a rate of {@code
     * CELL_GROWTH_RATE}. Cell will transition to M phase after completing {@code STEPS_G2} steps at
     * an average rate of {@code RATE_G2}. At each tick, cell may randomly apoptosis at a basal rate
     * of {@code BASAL_APOPTOSIS_RATE}.
     */
    @Override
    void stepG2(MersenneTwisterFast random) {
        updateVolumeBasedGrowthRate();
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, sizeTarget);
        boolean sizeCheck =
                cell.getVolume() >= SIZE_CHECKPOINT * sizeTarget * cell.getCriticalVolume();

        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateG2, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsG2 && sizeCheck) {
            setPhase(Phase.PROLIFERATIVE_M);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cell increases in size toward a target of twice its critical size at a rate of {@code
     * CELL_GROWTH_RATE}. Cell will complete cell division after completing {@code STEPS_M} steps at
     * an average rate of {@code RATE_M}. Cell must be greater than {@code SIZE_CHECKPOINT} times
     * the critical size.
     */
    @Override
    void stepM(MersenneTwisterFast random, Simulation sim) {
        // Update growth rate.
        updateVolumeBasedGrowthRate();
        // Increase size of cell.
        cell.updateTarget(cellGrowthRate, sizeTarget);

        // Check for phase transition.
        Poisson poisson = poissonFactory.createPoisson(rateM, random);
        currentSteps += poisson.nextInt();
        if (currentSteps >= stepsM) {
            addCell(random, sim);
            setPhase(Phase.PROLIFERATIVE_G1);
        }
    }

    @Override
    void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;

        Plane divisionPlane = chooseDivisionPlane(flyStemCell);
        PottsLocation2D parentLoc = (PottsLocation2D) cell.getLocation();
        PottsLocation daughterLoc = (PottsLocation) parentLoc.split(random, divisionPlane);

        boolean isDaughterStem = daughterStem(parentLoc, daughterLoc);

        if (isDaughterStem) {
            makeDaughterStemCell(daughterLoc, sim, potts, random);
        } else {
            makeDaughterGMC(parentLoc, daughterLoc, sim, potts, random);
        }
    }

    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double offset = sampleDivisionPlaneOffset();

        if (flyStemCell.getStemType() == StemType.WT
                || (flyStemCell.getStemType() == StemType.MUDMUT && Math.abs(offset) < 45)) {
            return getWTDivisionPlaneWithRotationalVariance(flyStemCell, offset);
        } else {
            return getMUDDivisionPlane(flyStemCell);
        }
    }

    private void makeDaughterStemCell(
            PottsLocation daughterLoc, Simulation sim, Potts potts, MersenneTwisterFast random) {
        cell.reset(potts.ids, potts.regions);
        int newID = sim.getID();
        // TODO
        double criticalVol;
        if (volumeBasedCriticalVolume) {
            criticalVol =
                    daughterLoc.getVolume() * sizeTarget * volumeBasedCriticalVolumeMultiplier;
            cell.setCriticalVolume(
                    cell.getLocation().getVolume()
                            * sizeTarget
                            * volumeBasedCriticalVolumeMultiplier);
            System.out.println("Daughter critical volume = " + criticalVol);
            System.out.println("Parent critical volume = " + cell.getCriticalVolume());
        } else {
            criticalVol = cell.getCriticalVolume();
        }
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(newID, State.PROLIFERATIVE, random, cell.getPop(), criticalVol);
        scheduleNewCell(container, daughterLoc, sim, potts, random);
    }

    private void makeDaughterGMC(
            PottsLocation parentLoc,
            PottsLocation daughterLoc,
            Simulation sim,
            Potts potts,
            MersenneTwisterFast random) {
        Location gmcLoc = determineGMCLocation(parentLoc, daughterLoc);

        if (parentLoc == gmcLoc) {
            PottsLocation.swapVoxels(parentLoc, daughterLoc);
        }

        cell.reset(potts.ids, potts.regions);
        int newID = sim.getID();
        int newPop = ((PottsCellFlyStem) cell).getLinks().next(random);
        double criticalVolume =
                calculateGMCDaughterCellCriticalVolume((PottsLocation) gmcLoc, sim, newPop);
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(newID, State.PROLIFERATIVE, random, newPop, criticalVolume);
        scheduleNewCell(container, daughterLoc, sim, potts, random);
    }

    private Location determineGMCLocation(PottsLocation parentLoc, PottsLocation daughterLoc) {
        switch (differentiationRuleset) {
            case "volume":
                return getSmallerLocation(parentLoc, daughterLoc);
            case "location":
                return getBasalLocation(
                        parentLoc, daughterLoc, ((PottsCellFlyStem) cell).getApicalAxis());
            default:
                throw new IllegalArgumentException(
                        "Invalid differentiation ruleset: " + differentiationRuleset);
        }
    }

    private void scheduleNewCell(
            PottsCellContainer container,
            PottsLocation daughterLoc,
            Simulation sim,
            Potts potts,
            MersenneTwisterFast random) {
        PottsCell newCell =
                (PottsCell) container.convert(sim.getCellFactory(), daughterLoc, random);
        if (newCell.getClass() == PottsCellFlyStem.class) {
            ((PottsCellFlyStem) newCell).setApicalAxis(getDaughterCellApicalAxis(random));
        }
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());
    }

    public boolean daughterStem(PottsLocation loc1, PottsLocation loc2) {
        if (((PottsCellFlyStem) cell).getStemType() == StemType.WT) {
            return false;
        } else if (((PottsCellFlyStem) cell).getStemType() == StemType.MUDMUT) {
            if (differentiationRuleset.equals("volume")) {
                double vol1 = loc1.getVolume();
                double vol2 = loc2.getVolume();
                if (Math.abs(vol1 - vol2) < range) {
                    return true;
                } else {
                    return false;
                }
            } else if (differentiationRuleset.equals("location")) {
                double[] centroid1 = loc1.getCentroid();
                double[] centroid2 = loc2.getCentroid();
                if (centroidsWithinRangeAlongApicalAxis(
                        centroid1,
                        centroid2,
                        ((PottsCellFlyStem) cell).getApicalAxis(),
                        range)) { // TODO: change this to just return
                    // centroidsWIthinRangeAlongApicalAxis
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new IllegalArgumentException(
                "Invalid differentiation ruleset: " + differentiationRuleset);
    }

    /**
     * Determines if the distance between two centroids, projected along the apical axis, is less
     * than or equal to the given range.
     *
     * @param centroid1 First centroid position.
     * @param centroid2 Second centroid position.
     * @param apicalAxis Unit {@link Vector} defining the apical-basal direction.
     * @param range Maximum allowed distance along the apical axis.
     * @return true if the centroids are within the given range along the apical axis.
     */
    static boolean centroidsWithinRangeAlongApicalAxis(
            double[] centroid1, double[] centroid2, Vector apicalAxis, double range) {

        Vector c1 = new Vector(centroid1[0], centroid1[1], centroid1.length > 2 ? centroid1[2] : 0);
        Vector c2 = new Vector(centroid2[0], centroid2[1], centroid2.length > 2 ? centroid2[2] : 0);

        double proj1 = Vector.dotProduct(c1, apicalAxis);
        double proj2 = Vector.dotProduct(c2, apicalAxis);

        double distanceAlongAxis = Math.abs(proj1 - proj2);

        return distanceAlongAxis - range <= EPSILON;
    }

    protected double calculateGMCDaughterCellCriticalVolume(
            PottsLocation gmcLoc, Simulation sim, int newpop) {
        double max_crit_vol =
                ((PottsCellFlyStem) cell).getCriticalVolume()
                        * ((PottsCellFlyStem) cell)
                                .getStemType()
                                .daughterCellCriticalVolumeProportion;
        if (volumeBasedCriticalVolume) {
            CellFactory factory = sim.getCellFactory();
            MiniBox newPopParameters =
                    factory.getParameters(
                            newpop); // TODO: Make factory.getParameters return parameters object
            // not minibox
            double sizeTarget = newPopParameters.getDouble("proliferation/SIZE_TARGET");
            System.out.println(
                    "gmc critical volume: "
                            + (gmcLoc.getVolume()
                                    * sizeTarget
                                    * volumeBasedCriticalVolumeMultiplier));
            return gmcLoc.getVolume() * sizeTarget * volumeBasedCriticalVolumeMultiplier;
        } else {
            return max_crit_vol;
        }
    }

    /**
     * Gets the division plane for the cell after rotating the plane according to
     * splitDirectionDistribution. This follows WT division rules. The plane is rotated around the
     * XY plane.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @param rotationOffset the angle to rotate the plane
     * @return the division plane for the cell
     */
    public Plane getWTDivisionPlaneWithRotationalVariance(
            PottsCellFlyStem cell, double rotationOffset) {
        Vector apical_axis = cell.getApicalAxis();
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        apical_axis, Direction.XY_PLANE.vector, rotationOffset);
        Voxel splitVoxel = getCellSplitVoxel(StemType.WT, cell);
        return new Plane(
                new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), rotatedNormalVector);
    }

    /**
     * Gets the division plane for the cell. This follows MUDMUT division rules. The division plane
     * is not rotated.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @return the division plane for the cell
     */
    public Plane getMUDDivisionPlane(PottsCellFlyStem cell) {
        Vector defaultNormal =
                Vector.rotateVectorAroundAxis(
                        cell.getApicalAxis(),
                        Direction.XY_PLANE.vector,
                        StemType.MUDMUT.splitDirectionRotation);
        Voxel splitVoxel = getCellSplitVoxel(StemType.MUDMUT, cell);
        return new Plane(new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), defaultNormal);
    }

    /**
     * Gets the rotation offset for the division plane according to splitDirectionDistribution.
     *
     * @return the rotation offset for the division plane
     */
    double sampleDivisionPlaneOffset() {
        return splitDirectionDistribution.nextDouble();
    }

    public Vector getDaughterCellApicalAxis(MersenneTwisterFast random) {
        switch (apicalAxisRuleset) {
            case "uniform":
                if (!(apicalAxisRotationDistribution instanceof UniformDistribution)) {
                    throw new IllegalArgumentException(
                            "apicalAxisRotationDistribution must be a UniformDistribution under the uniform apical axis ruleset.");
                }
                Vector newRandomApicalAxis =
                        Vector.rotateVectorAroundAxis(
                                ((PottsCellFlyStem) cell).getApicalAxis(),
                                Direction.XY_PLANE.vector,
                                apicalAxisRotationDistribution.nextDouble());
                return newRandomApicalAxis;
            case "global":
                return ((PottsCellFlyStem) cell).getApicalAxis();
            case "rotation":
                if (!(apicalAxisRotationDistribution instanceof NormalDistribution)) {
                    throw new IllegalArgumentException(
                            "apicalAxisRotationDistribution must be a NormalDistribution under the rotation apical axis ruleset.");
                }
                Vector newRotatedApicalAxis =
                        Vector.rotateVectorAroundAxis(
                                ((PottsCellFlyStem) cell).getApicalAxis(),
                                Direction.XY_PLANE.vector,
                                apicalAxisRotationDistribution.nextDouble());
                return newRotatedApicalAxis;
            default:
                throw new IllegalArgumentException(
                        "Invalid apical axis ruleset: " + apicalAxisRuleset);
        }
    }

    /**
     * Gets the voxel location the cell's plane of division will pass through.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division location for
     * @return the voxel location where the cell will split
     */
    public static Voxel getCellSplitVoxel(StemType stemType, PottsCellFlyStem cell) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(stemType.splitOffsetPercentX);
        splitOffsetPercent.add(stemType.splitOffsetPercentY);
        return ((PottsLocation2D) cell.getLocation())
                .getOffsetInApicalFrame2D(splitOffsetPercent, cell.getApicalAxis());
    }

    /**
     * Gets the smaller location with fewer voxels and returns it.
     *
     * @param loc1 the {@link PottsLocation} to compare to location2.
     * @param loc2 {@link PottsLocation} to compare to location1.
     * @return the smaller location.
     */
    public static PottsLocation getSmallerLocation(PottsLocation loc1, PottsLocation loc2) {
        return (loc1.getVolume() < loc2.getVolume()) ? loc1 : loc2;
    }

    /**
     * Gets the location that is lower along the apical axis.
     *
     * @param loc1 {@link PottsLocation} to compare.
     * @param loc2 {@link PottsLocation} to compare.
     * @param apicalAxis Unit {@link Vector} defining the apical-basal direction.
     * @return the basal location (lower along the apical axis).
     */
    public static PottsLocation getBasalLocation(
            PottsLocation loc1, PottsLocation loc2, Vector apicalAxis) {
        double[] centroid1 = loc1.getCentroid();
        double[] centroid2 = loc2.getCentroid();
        Vector c1 = new Vector(centroid1[0], centroid1[1], centroid1.length > 2 ? centroid1[2] : 0);
        Vector c2 = new Vector(centroid2[0], centroid2[1], centroid2.length > 2 ? centroid2[2] : 0);

        double proj1 = Vector.dotProduct(c1, apicalAxis);
        double proj2 = Vector.dotProduct(c2, apicalAxis);

        return (proj1 < proj2) ? loc2 : loc1; // higher projection = more basal
    }
}
