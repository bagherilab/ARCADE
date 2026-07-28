package arcade.potts.agent.module;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import sim.util.Bag;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
import arcade.core.util.distributions.UniformDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFly;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.agent.cell.PottsCellFlyStem.StemType;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Phase;
import arcade.potts.util.PottsEnums.Side;
import arcade.potts.util.PottsEnums.State;
import arcade.potts.util.PottsUtilities;

public abstract class PottsModuleFlyStemProliferation
        extends PottsModuleProliferationVolumeBasedDivision {

    /** Threshold for critical volume size checkpoint. */
    static final double SIZE_CHECKPOINT = 0.95;

    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;

    /** Rate of Prospero change (ticks^-1). */
    final double prosperoRate;

    /** Rate of Deadpan change (ticks^-1). */
    final double deadpanRate;

    /**
     * Percent threshold of apical voxels considered when dividing Deadpan at cell division. The sum
     * of apicalThreshold and basalThreshold should not exceed 1.
     */
    final double apicalThreshold;

    /**
     * Percent threshold of basal voxels considered when dividing Prospero at cell division. The sum
     * of apicalThreshold and basalThreshold should not exceed 1.
     */
    final double basalThreshold;

    /** Threshold ratio of Prospero to Deadpan in daughter cell to determine cell identity. */
    final double tfRatio;

    final int wtLikeX;

    final int wtLikeY;

    /** Distribution that determines rotational offset of cell's division plane. */
    final NormalDistribution splitDirectionDistribution;

    /** Ruleset for determining which daughter cell is the GMC. Can be `volume` or `location`. */
    final String differentiationRuleset;

    /**
     * Ruleset for determining how the cell determines its Apical Axis. Can be 'uniform', 'global',
     * or 'rotation'
     */
    final String apicalAxisRuleset;

    /**
     * The distribution used to determine how apical axis should be rotated. Relevant when
     * apicalAxisRuleset is set to 'uniform' or 'rotation'.
     */
    final Distribution apicalAxisRotationDistribution;

    /**
     * Boolean flag indicating whether or not the cell's critical volume should be affected by its
     * volume at the time it divides.
     */
    final boolean volumeBasedCriticalVolume;

    /** Boolean flag indicating whether growth rate should be regulated by NB-NB contact. */
    final boolean dynamicGrowthRateNBSelfRepression;

    final double volumeBasedCriticalVolumeMultiplier;

    /**
     * Range of values considered equal when determining daughter cell identity. ex. if ruleset is
     * location, range determines the distance between centroid y values that is considered equal.
     */
    final double range;

    /**
     * Half-max NB neighbor count for repression (K). Only relevant if dynamicGrowthRateNBContact is
     * true.
     */
    final double nbContactHalfMax;

    /**
     * Hill coefficient for NB-contact repression (n). Only relevant if dynamicGrowthRateNBContact
     * is true.
     */
    final double nbContactHillN;

    /*
     * Boolean flag for whether the daughter cell's differentiation is determined deterministically.
     */
    final boolean hasDeterministicDifferentiation;

    final double initialSize;

    public static final double EPSILON = 1e-8;

    /**
     * Boolean determining whether growth and division rates are universal across all NBs. If true
     * model behaviors is PDE-like, if false it is ABM-like.
     */
    final Boolean pdeLike;

    /**
     * Creates a proliferation {@code Module} for the given {@link PottsCellFlyStem}.
     *
     * @param cell the {@link PottsCellFlyStem} the module is associated with
     */
    public PottsModuleFlyStemProliferation(PottsCellFlyStem cell) {
        super(cell);

        if (cell.hasRegions()) {
            throw new UnsupportedOperationException(
                    "Regions are not yet implemented for fly cells");
        }

        Parameters parameters = cell.getParameters();

        basalApoptosisRate = parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE");
        prosperoRate = parameters.getDouble("proliferation/PROSPERO_RATE");
        deadpanRate = parameters.getDouble("proliferation/DEADPAN_RATE");
        apicalThreshold = parameters.getDouble("proliferation/APICAL_THRESHOLD");
        basalThreshold = parameters.getDouble("proliferation/BASAL_THRESHOLD");
        tfRatio = parameters.getDouble("proliferation/TF_RATIO");
        wtLikeX = parameters.getInt("proliferation/WT_LIKE_X");
        wtLikeY = parameters.getInt("proliferation/WT_LIKE_Y");
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

        volumeBasedCriticalVolume =
                (parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME") != 0);

        dynamicGrowthRateNBSelfRepression =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_SELF_REPRESSION") != 0);

        if (dynamicGrowthRateVolume && dynamicGrowthRateNBSelfRepression) {
            throw new InvalidParameterException(
                    "Dynamic growth rate can be either volume-based or NB-contact-based, not both.");
        }

        if (apicalThreshold + basalThreshold > 1 || apicalThreshold < 0 || basalThreshold < 0) {
            throw new InvalidParameterException(
                    "Apical and basal thresholds should be nonnegative and not overlap (check that apicalThreshold + basalThreshold <= 1)");
        }

        volumeBasedCriticalVolumeMultiplier =
                (parameters.getDouble("proliferation/VOLUME_BASED_CRITICAL_VOLUME_MULTIPLIER"));

        nbContactHalfMax = parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX");
        nbContactHillN = parameters.getDouble("proliferation/NB_CONTACT_HILL_N");

        String hasDeterministicDifferentiationString =
                parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION");
        if (!hasDeterministicDifferentiationString.equals("TRUE")
                && !hasDeterministicDifferentiationString.equals("FALSE")) {
            throw new InvalidParameterException(
                    "hasDeterministicDifferentiation must be either TRUE or FALSE");
        }
        hasDeterministicDifferentiation = hasDeterministicDifferentiationString.equals("TRUE");

        initialSize = cell.getVolume();

        pdeLike = (parameters.getInt("proliferation/PDELIKE") != 0);

        setPhase(Phase.UNDEFINED);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        super.step(random, sim);
        ((PottsCellFly) cell).setProspero(((PottsCellFly) cell).getProspero() + prosperoRate);
        ((PottsCellFly) cell).setDeadpan(((PottsCellFly) cell).getDeadpan() + deadpanRate);
        System.out.println(
                "Stem ID "
                        + cell.getID()
                        + " prospero: "
                        + ((PottsCellFly) cell).getProspero()
                        + ", deadpan: "
                        + ((PottsCellFly) cell).getDeadpan());
    }

    @Override
    public void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;

        Plane divisionPlane = chooseDivisionPlane(flyStemCell);
        PottsLocation2D parentLoc = (PottsLocation2D) cell.getLocation();

        ArrayList<Voxel> voxels = ((PottsLocation) cell.getLocation()).getVoxels();
        double[] centroid = ((PottsLocation) cell.getLocation()).getCentroid();
        Vector apicalAxis = flyStemCell.getApicalAxis();

        Bag apicalVoxels =
                PottsLocation.getDirectionalVoxelSubset(
                        Side.APICAL, apicalThreshold, voxels, centroid, apicalAxis);
        Bag basalVoxels =
                PottsLocation.getDirectionalVoxelSubset(
                        Side.BASAL, basalThreshold, voxels, centroid, apicalAxis);

        PottsUtilities.splitBagDupesRandomly(apicalVoxels, basalVoxels, random);

        PottsLocation daughterLoc = (PottsLocation) parentLoc.split(random, divisionPlane);

        double basalFrac =
                PottsUtilities.listFraction(
                        PottsUtilities.asCollection(basalVoxels, Voxel.class),
                        daughterLoc.getVoxels());
        double apicalFrac =
                PottsUtilities.listFraction(
                        PottsUtilities.asCollection(apicalVoxels, Voxel.class),
                        daughterLoc.getVoxels());

        double parentProspero = ((PottsCellFly) cell).getProspero();
        double parentDeadpan = ((PottsCellFly) cell).getDeadpan();

        double daughterProspero = parentProspero * basalFrac;
        double daughterDeadpan = parentDeadpan * apicalFrac;

        boolean isDaughterStem =
                daughterStem(
                        parentLoc, daughterLoc, divisionPlane, daughterProspero, daughterDeadpan);

        if (isDaughterStem) {
            makeDaughterStemCell(
                    daughterLoc, sim, potts, random, daughterProspero, daughterDeadpan);
        } else {
            makeDaughterGMC(
                    parentLoc,
                    daughterLoc,
                    sim,
                    potts,
                    random,
                    divisionPlane.getUnitNormalVector(),
                    daughterProspero,
                    daughterDeadpan);
        }
    }

    /**
     * Updates the effective growth rate according to the ruleset indicated in parameters.
     *
     * @param sim the simulation
     */
    public void updateGrowthRate(Simulation sim) {
        if (dynamicGrowthRateVolume == true) {
            updateVolumeBasedGrowthRate(sim);
        } else if (dynamicGrowthRateNBSelfRepression == true) {
            updateGrowthRateBasedOnOtherNBs(sim);
        } else {
            cellGrowthRate = cellGrowthRateBase;
        }
    }

    public void updateVolumeBasedGrowthRate(Simulation sim) {
        if (pdeLike == false) {
            updateCellVolumeBasedGrowthRate(
                    cell.getLocation().getVolume(), cell.getCriticalVolume());
        } else {
            HashSet<PottsCellFlyStem> nbsInSimulation = getNBsInSimulation(sim);
            double volSum = 0.0;
            double critVolSum = 0.0;
            for (PottsCellFlyStem nb : nbsInSimulation) {
                volSum += nb.getLocation().getVolume();
                critVolSum += nb.getCriticalVolume();
            }
            double avgVolume = volSum / nbsInSimulation.size();
            double avgCritVol = critVolSum / nbsInSimulation.size();
            updateCellVolumeBasedGrowthRate(avgVolume, avgCritVol);
        }
    }

    /**
     * Gets the neighbors of this cell that are unique neuroblasts.
     *
     * @param sim the simulation
     * @return the number of unique neuroblast neighbors
     */
    protected HashSet<PottsCellFlyStem> getNBNeighbors(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        ArrayList<Voxel> voxels = ((PottsLocation) cell.getLocation()).getVoxels();
        HashSet<PottsCellFlyStem> stemNeighbors = new HashSet<PottsCellFlyStem>();

        for (Voxel v : voxels) {
            HashSet<Integer> uniqueIDs = potts.getUniqueIDs(v.x, v.y, v.z);
            for (Integer id : uniqueIDs) {
                PottsCell neighbor = (PottsCell) sim.getGrid().getObjectAt(id);
                if (neighbor == null) {
                    continue;
                }
                if (cell.getPop() == neighbor.getPop()) {
                    if (neighbor.getID() != cell.getID()) {
                        stemNeighbors.add((PottsCellFlyStem) sim.getGrid().getObjectAt(id));
                    }
                }
            }
        }
        return stemNeighbors;
    }

    protected void updateGrowthRateBasedOnOtherNBs(Simulation sim) {
        int nbsInContact;
        if (pdeLike) {
            int nbsInSim = getNBsInSimulation(sim).size();
            nbsInContact = nbsInSim - 1;
        } else {
            nbsInContact = getNBNeighbors(sim).size();
        }
        double np = Math.max(0.0, (double) nbsInContact);

        double Kn = Math.pow(nbContactHalfMax, nbContactHillN);
        double Npn = Math.pow(np, nbContactHillN);

        double hillRepression;
        if (Kn == 0.0) {
            hillRepression = (np == 0.0) ? 1.0 : 0.0;
        } else {
            hillRepression = Kn / (Kn + Npn);
        }

        cellGrowthRate = cellGrowthRateBase * hillRepression;
    }

    /**
     * Chooses the division plane according to the type of stem cell this module is attached to.
     *
     * @param flyStemCell the stem cell this module is attached to
     * @return the plane along which this cell should divide
     */
    protected abstract Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell);

    /**
     * Gets the rotation offset for the division plane according to splitDirectionDistribution.
     *
     * @return the rotation offset for the division plane
     */
    double sampleDivisionPlaneOffset() {
        return splitDirectionDistribution.nextDouble();
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
    public Plane getWTLikeDivisionPlaneWithRotationalVariance(
            PottsCellFlyStem cell, double rotationOffset) {
        Vector apical_axis = cell.getApicalAxis();
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        apical_axis, Direction.XY_PLANE.vector, rotationOffset);
        Voxel splitVoxel =
                getCellSplitVoxel(StemType.WT, cell, rotatedNormalVector, wtLikeX, wtLikeY);
        return new Plane(
                new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), rotatedNormalVector);
    }

    /**
     * Gets the voxel location the cell's plane of division will pass through.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division location for
     * @return the voxel location where the cell will split
     */
    public static Voxel getCellSplitVoxel(
            StemType stemType,
            PottsCellFlyStem cell,
            Vector rotatedNormalVector,
            int likeX,
            int likeY) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(likeX);
        splitOffsetPercent.add(likeY);
        return ((PottsLocation2D) cell.getLocation())
                .getOffsetInApicalFrame(splitOffsetPercent, rotatedNormalVector);
    }

    /**
     * Determines whether the daughter cell should be a neuroblast or a GMC according to the type of
     * cell this module is attached to, the differentiation ruleset specified in the parameters, and
     * the morphologies of the daughter cell locations.
     *
     * @param loc1 one cell location post division
     * @param loc2 the other cell location post division
     * @return whether or not the daughter cell should be a stem cell
     */
    protected abstract boolean daughterStemRuleBasedDifferentiation(
            PottsLocation loc1,
            PottsLocation loc2,
            double daughterProspero,
            double daughterDeadpan);

    /*
     * Determines whether the daughter cell should be a neuroblast or a GMC according to the orientation.
     * This is deterministic.
     *
     * @param divisionPlane
     * @return {@code true} if the daughter should be a stem cell. {@code false} if the daughter should be a GMC.
     */
    protected abstract boolean daughterStemDeterministic(Plane divisionPlane);

    /**
     * Determines whether a daughter cell should remain a stem cell or differentiate into a GMC.
     *
     * <p>This method serves as a wrapper that delegates to either a deterministic or rule-based
     * differentiation mechanism depending on the value of {@code hasDeterministicDifferentiation}.
     *
     * @param parentsLoc the location of the parent cell before division
     * @param daughterLoc the location of the daughter cell after division
     * @param divisionPlane the plane of division for the daughter cell
     * @return {@code true} if the daughter should remain a stem cell; {@code false} if it should be
     *     a GMC
     */
    public boolean daughterStem(
            PottsLocation2D parentsLoc,
            PottsLocation daughterLoc,
            Plane divisionPlane,
            double daughterProspero,
            double daughterDeadpan) {
        return hasDeterministicDifferentiation
                ? daughterStemDeterministic(divisionPlane)
                : daughterStemRuleBasedDifferentiation(
                        parentsLoc, daughterLoc, daughterProspero, daughterDeadpan);
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

    /**
     * Makes a daughter NB cell
     *
     * @param daughterLoc the location of the daughter NB cell
     * @param sim the simulation
     * @param potts the potts instance for this simulation
     * @param random the random number generator
     */
    private void makeDaughterStemCell(
            PottsLocation daughterLoc,
            Simulation sim,
            Potts potts,
            MersenneTwisterFast random,
            double daughterProspero,
            double daughterDeadpan) {
        cell.reset(potts.ids, potts.regions);
        int newID = sim.getID();
        double criticalVol;
        if (volumeBasedCriticalVolume) {
            criticalVol =
                    Math.max(
                            daughterLoc.getVolume() * volumeBasedCriticalVolumeMultiplier,
                            initialSize * .5);
            cell.setCriticalVolume(criticalVol);
        } else {
            criticalVol = cell.getCriticalVolume();
        }
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(newID, State.PROLIFERATIVE, random, cell.getPop(), criticalVol);

        System.out.print(
                "Creating daughter stem cell with prospero "
                        + daughterProspero
                        + ", deadpan "
                        + daughterDeadpan
                        + ", ");
        scheduleNewCell(
                container, daughterLoc, sim, potts, random, daughterProspero, daughterDeadpan);
    }

    /**
     * Makes a daughter GMC cell
     *
     * @param parentLoc the location of the parent NB cell
     * @param daughterLoc the location of the daughter GMC cell
     * @param sim the simulation
     * @param potts the potts instance for this simulation
     * @param random the random number generator
     * @param divisionPlaneNormal the normal vector to the plane of division
     */
    private void makeDaughterGMC(
            PottsLocation parentLoc,
            PottsLocation daughterLoc,
            Simulation sim,
            Potts potts,
            MersenneTwisterFast random,
            Vector divisionPlaneNormal,
            double daughterProspero,
            double daughterDeadpan) {
        Location gmcLoc = determineGMCLocation(parentLoc, daughterLoc, divisionPlaneNormal);

        if (parentLoc == gmcLoc) {
            PottsLocation.swapVoxels(parentLoc, daughterLoc);
        }
        cell.reset(potts.ids, potts.regions);
        int newID = sim.getID();
        int newPop = ((PottsCellFlyStem) cell).getLinks().next(random);
        double criticalVolume = calculateGMCDaughterCellCriticalVolume((PottsLocation) daughterLoc);
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(newID, State.PROLIFERATIVE, random, newPop, criticalVolume);
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;

        System.out.print(
                "Creating daughter GMC with prospero "
                        + daughterProspero
                        + ", deadpan "
                        + daughterDeadpan
                        + ", ");
        scheduleNewCell(
                container, daughterLoc, sim, potts, random, daughterProspero, daughterDeadpan);
    }

    /**
     * Adds a new cell to the simulation grid and schedule. Resets the parent cell.
     *
     * @param container the daughter cell's container
     * @param daughterLoc the daughter cell's location
     * @param sim the simulation
     * @param potts the potts instance for this simulation
     * @param random the random number generator
     */
    private void scheduleNewCell(
            PottsCellContainer container,
            PottsLocation daughterLoc,
            Simulation sim,
            Potts potts,
            MersenneTwisterFast random,
            double daughterProspero,
            double daughterDeadpan) {
        PottsCell newCell =
                (PottsCell) container.convert(sim.getCellFactory(), daughterLoc, random);
        if (newCell.getClass() == PottsCellFlyStem.class) {
            ((PottsCellFlyStem) newCell).setApicalAxis(getDaughterCellApicalAxis(random));
        }
        sim.getGrid().addObject(newCell, null);
        potts.register(newCell);
        newCell.reset(potts.ids, potts.regions);
        newCell.schedule(sim.getSchedule());

        System.out.println("ID " + newCell.getID());

        ((PottsCellFly) newCell).setProspero(daughterProspero);
        ((PottsCellFly) cell).setProspero(((PottsCellFly) cell).getProspero() - daughterProspero);

        ((PottsCellFly) newCell).setDeadpan(daughterDeadpan);
        ((PottsCellFly) cell).setDeadpan(((PottsCellFly) cell).getDeadpan() - daughterDeadpan);
    }

    /**
     * Gets the apical axis of the daughter cell according to the apicalAxisRuleset specified in the
     * parameters.
     *
     * @param random the random number generator
     * @return the daughter cell's apical axis
     */
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
            case "normal":
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
     * Determines between two locations which will be the GMC and which will be the NB according to
     * differentiation rules specified in the parameters.
     *
     * @param parentLoc the parent cell location
     * @param daughterLoc the daughter cell location
     * @param divisionPlaneNormal the normal vector to the plane of division
     * @return the location that should be the GMC
     */
    private Location determineGMCLocation(
            PottsLocation parentLoc, PottsLocation daughterLoc, Vector divisionPlaneNormal) {
        switch (differentiationRuleset) {
            case "volume":
                return getSmallerLocation(parentLoc, daughterLoc);
            case "location":
                return getBasalLocation(parentLoc, daughterLoc, divisionPlaneNormal);
            case "tfRatio":
                return getSmallerLocation(
                        parentLoc,
                        daughterLoc); // TODO: Ask Sophia which location makes more biological sense
            default:
                throw new IllegalArgumentException(
                        "Invalid differentiation ruleset: " + differentiationRuleset);
        }
    }

    /**
     * Calculates the critical volume of a GMC daughter cell
     *
     * @param gmcLoc the location of the GMC daughter cell
     * @return the critical volume of the GMC daughter cell
     */
    protected double calculateGMCDaughterCellCriticalVolume(PottsLocation gmcLoc) {
        double criticalVol;
        if (volumeBasedCriticalVolume) {
            criticalVol =
                    Math.max(
                            gmcLoc.getVolume() * volumeBasedCriticalVolumeMultiplier,
                            initialSize * .2);
            return criticalVol;
        } else {
            criticalVol =
                    ((PottsCellFlyStem) cell).getCriticalVolume()
                            * sizeTarget
                            * StemType.WT.daughterCellCriticalVolumeProportion;
            return criticalVol;
        }
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

    public HashSet<PottsCellFlyStem> getNBsInSimulation(Simulation sim) {
        HashSet<PottsCellFlyStem> nbsInSimulation = new HashSet<>();
        Bag simObjects = sim.getGrid().getAllObjects();
        for (int i = 0; i < simObjects.numObjs; i++) {
            Object o = simObjects.objs[i];
            if (!(o instanceof PottsCell)) continue; // skip non-cell objects
            PottsCell cellInSim = (PottsCell) o;
            if (cell.getPop() == cellInSim.getPop() && o instanceof PottsCellFlyStem) {
                nbsInSimulation.add((PottsCellFlyStem) o);
            }
        }
        return nbsInSimulation;
    }
}
