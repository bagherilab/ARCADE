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
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.agent.cell.PottsCellFlyStem.StemType;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.Phase;
import arcade.potts.util.PottsEnums.State;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

/**
 * Implementation of {@link PottsModuleProliferationVolumeBasedDivision} for fly stem agents. Each
 * division produces two daughters: one stem cell and one that is either stem or GMC depending on
 * division geometry and rules. This module determines the division plane (affecting morphology) and
 * assigns daughter cell identity.
 */
public class PottsModuleFlyStemProliferation extends PottsModuleProliferationVolumeBasedDivision {

    /** Threshold for critical volume size checkpoint. */
    static final double SIZE_CHECKPOINT = 0.95;

    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;

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

    /**
     * Boolean flag for whether the daughter cell's differentiation is determined deterministically.
     */
    final boolean hasDeterministicDifferentiation;

    /** The cell's initial size/volume (in voxels). */
    final double initialSize;

    /** Epsilon. */
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
    public void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;

        Plane divisionPlane = chooseDivisionPlane(flyStemCell);
        PottsLocation2D parentLoc = (PottsLocation2D) cell.getLocation();
        PottsLocation daughterLoc = (PottsLocation) parentLoc.split(random, divisionPlane);

        boolean isDaughterStem = daughterStem(parentLoc, daughterLoc, divisionPlane);

        if (isDaughterStem) {
            makeDaughterStemCell(daughterLoc, sim, potts, random);
        } else {
            makeDaughterGMC(
                    parentLoc,
                    daughterLoc,
                    sim,
                    potts,
                    random,
                    divisionPlane.getUnitNormalVector());
        }
    }

    /**
     * Updates the effective growth rate according to the ruleset indicated in parameters.
     *
     * @param sim the simulation
     */
    public void updateGrowthRate(Simulation sim) {
        if (dynamicGrowthRateVolume) {
            updateVolumeBasedGrowthRate(sim);
        } else if (dynamicGrowthRateNBSelfRepression) {
            updateGrowthRateBasedOnOtherNBs(sim);
        } else {
            cellGrowthRate = cellGrowthRateBase;
        }
    }

    /**
     * Updates growth rate based on cell volume relative to a reference volume.
     *
     * <p>Growth is regulated by comparing a volume signal to an equilibrium reference. The source
     * of the volume signal depends on the simulation mode:
     *
     * <ul>
     *   <li><b>Local mode:</b> uses this cell's volume
     *   <li><b>PDE-like mode:</b> uses the average volume of all NBs in the simulation (global
     *       coupling)
     * </ul>
     *
     * The resulting signal is passed to the volume-based growth function, which adjusts growth rate
     * relative to the equilibrium volume.
     *
     * @param sim the simulation
     */
    public void updateVolumeBasedGrowthRate(Simulation sim) {
        double vRef = computeEquilibriumVolume();
        if (!pdeLike) {
            updateCellVolumeBasedGrowthRate(cell.getLocation().getVolume(), vRef);
        } else {
            HashSet<PottsCellFlyStem> nbsInSimulation = getNBsInSimulation(sim);
            double volSum = 0.0;
            for (PottsCellFlyStem nb : nbsInSimulation) {
                volSum += nb.getLocation().getVolume();
            }
            double avgVolume = volSum / nbsInSimulation.size();
            updateCellVolumeBasedGrowthRate(avgVolume, vRef);
        }
    }

    /**
     * Computes the expected average NB volume from structural parameters, using a rectangular
     * approximation for the post-division volume retained by the NB.
     *
     * <p>For a NB growing at constant rate, the time-averaged volume over one cell cycle equals the
     * arithmetic midpoint between birth volume and division volume:
     *
     * <pre>
     *   V_ref = (V_birth + V_div) / 2
     *         = sizeTarget * critVol * (f_retain + 1) / 2
     * </pre>
     *
     * where {@code f_retain = splitOffsetPercentY / 100} approximates the fraction of the
     * pre-division volume retained by the NB after asymmetric division.
     *
     * <p>This reference volume is used as the normalization denominator in the volume-based growth
     * rate formula, ensuring that at the average NB volume the effective growth rate equals {@code
     * cellGrowthRateBase}.
     *
     * @return the expected average NB volume
     */
    double computeEquilibriumVolume() {
        double vDiv = sizeTarget * cell.getCriticalVolume();
        double fRetain = ((PottsCellFlyStem) cell).getStemType().splitOffsetPercentY / 100.0;
        return vDiv * (fRetain + 1.0) / 2.0;
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

    /**
     * Updates the neuroblast (NB) contact-dependent growth rate.
     *
     * <p>Growth is repressed as a function of NB contact using a Hill function. The number of
     * interacting NBs is determined in one of two ways:
     *
     * <ul>
     *   <li><b>PDE-like mode:</b> uses the total number of NBs in the simulation (global coupling)
     *   <li><b>Local mode:</b> uses only this cell's neighboring NBs (local coupling)
     * </ul>
     *
     * The resulting repression factor scales the base growth rate, reducing growth as NB contact
     * increases.
     *
     * @param sim the simulation
     */
    protected void updateGrowthRateBasedOnOtherNBs(Simulation sim) {
        int nbsInContact;
        if (pdeLike) {
            int nbsInSim = getNBsInSimulation(sim).size();
            nbsInContact = nbsInSim - 1;
        } else {
            nbsInContact = getNBNeighbors(sim).size();
        }
        double neighborSignal = Math.max(0.0, (double) nbsInContact);

        double kHalfMaxPowN = Math.pow(nbContactHalfMax, nbContactHillN);
        double neighborSignalPowN = Math.pow(neighborSignal, nbContactHillN);

        double hillRepression;
        if (kHalfMaxPowN == 0.0) {
            hillRepression = (neighborSignal == 0.0) ? 1.0 : 0.0;
        } else {
            hillRepression = kHalfMaxPowN / (kHalfMaxPowN + neighborSignalPowN);
        }

        cellGrowthRate = cellGrowthRateBase * hillRepression;
    }

    /**
     * Chooses the division plane according to the type of stem cell this module is attached to.
     *
     * @param flyStemCell the stem cell this module is attached to
     * @return the plane along which this cell should divide
     */
    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double offset = sampleDivisionPlaneOffset();

        if (flyStemCell.getStemType() == StemType.WT
                || (flyStemCell.getStemType() == StemType.MUDMUT
                        && (Math.abs(offset - splitDirectionDistribution.getExpected()) <= 45))) {
            return getWTDivisionPlaneWithRotationalVariance(flyStemCell, offset);
        } else {
            return getMUDDivisionPlane(flyStemCell);
        }
    }

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
    public Plane getWTDivisionPlaneWithRotationalVariance(
            PottsCellFlyStem cell, double rotationOffset) {
        Vector apicalAxis = cell.getApicalAxis();
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        apicalAxis, Direction.XY_PLANE.vector, rotationOffset);
        Voxel splitVoxel = getCellSplitVoxel(StemType.WT, cell, rotatedNormalVector);
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
        Voxel splitVoxel = getCellSplitVoxel(StemType.MUDMUT, cell, defaultNormal);
        return new Plane(new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), defaultNormal);
    }

    /**
     * Computes the voxel through which the division plane passes for a given cell.
     *
     * <p>The split position is determined from the stem-type-specific offset percentages,
     * interpreted in the cell's apical frame. The supplied normal vector is assumed to already
     * reflect any rule-based rotation of the division plane.
     *
     * @param stemType the {@link StemType} providing the split offset percentages
     * @param cell the {@link PottsCellFlyStem} whose division position is being computed
     * @param rotatedNormalVector the division plane normal after any rule-based rotation
     * @return the voxel used as the anchor point for the division plane
     */
    public static Voxel getCellSplitVoxel(
            StemType stemType, PottsCellFlyStem cell, Vector rotatedNormalVector) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(stemType.splitOffsetPercentX);
        splitOffsetPercent.add(stemType.splitOffsetPercentY);
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
    private boolean daughterStemRuleBasedDifferentiation(PottsLocation loc1, PottsLocation loc2) {
        if (((PottsCellFlyStem) cell).getStemType() == StemType.WT) {
            return false;
        } else if (((PottsCellFlyStem) cell).getStemType() == StemType.MUDMUT) {
            if (differentiationRuleset.equals("volume")) {
                double vol1 = loc1.getVolume();
                double vol2 = loc2.getVolume();
                if (Math.abs(vol1 - vol2) < range) {
                    return true;
                }
                return false;
            } else if (differentiationRuleset.equals("location")) {
                double[] centroid1 = loc1.getCentroid();
                double[] centroid2 = loc2.getCentroid();
                return (centroidsWithinRangeAlongApicalAxis(
                        centroid1, centroid2, ((PottsCellFlyStem) cell).getApicalAxis(), range));
            }
        }
        throw new IllegalArgumentException(
                "Invalid differentiation ruleset: " + differentiationRuleset);
    }

    /**
     * Determines whether the daughter cell should be a neuroblast or a GMC according to the
     * orientation. This is deterministic.
     *
     * @param divisionPlane the plane the cell will divide along
     * @return {@code true} if the daughter should be a stem cell; {@code false} if the daughter
     *     should be a GMC
     */
    private boolean daughterStemDeterministic(Plane divisionPlane) {

        Vector normalVector = divisionPlane.getUnitNormalVector();

        Vector apicalAxis = ((PottsCellFlyStem) cell).getApicalAxis();
        Vector expectedMUDNormalVector =
                Vector.rotateVectorAroundAxis(
                        apicalAxis,
                        Direction.XY_PLANE.vector,
                        StemType.MUDMUT.splitDirectionRotation);
        // If TRUE, the daughter should be stem. Otherwise, should be GMC
        return Math.abs(normalVector.getX() - expectedMUDNormalVector.getX()) <= EPSILON
                && Math.abs(normalVector.getY() - expectedMUDNormalVector.getY()) <= EPSILON
                && Math.abs(normalVector.getZ() - expectedMUDNormalVector.getZ()) <= EPSILON;
    }

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
            PottsLocation2D parentsLoc, PottsLocation daughterLoc, Plane divisionPlane) {
        return hasDeterministicDifferentiation
                ? daughterStemDeterministic(divisionPlane)
                : daughterStemRuleBasedDifferentiation(parentsLoc, daughterLoc);
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
     * Makes a daughter NB cell.
     *
     * @param daughterLoc the location of the daughter NB cell
     * @param sim the simulation
     * @param potts the potts instance for this simulation
     * @param random the random number generator
     */
    private void makeDaughterStemCell(
            PottsLocation daughterLoc, Simulation sim, Potts potts, MersenneTwisterFast random) {
        int newID = sim.getID();
        double criticalVol;
        if (volumeBasedCriticalVolume) {
            criticalVol = Math.max(daughterLoc.getVolume(), initialSize * .5);
            cell.setCriticalVolume(criticalVol);
        } else {
            criticalVol = cell.getCriticalVolume();
        }
        cell.reset(potts.ids, potts.regions);
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(newID, State.PROLIFERATIVE, random, cell.getPop(), criticalVol);
        scheduleNewCell(container, daughterLoc, sim, potts, random);
    }

    /**
     * Makes a daughter GMC cell.
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
            Vector divisionPlaneNormal) {
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
        scheduleNewCell(container, daughterLoc, sim, potts, random);
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
                            "apicalAxisRotationDistribution must be a UniformDistribution"
                                    + "under the uniform apical axis ruleset.");
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
                            "apicalAxisRotationDistribution must be a NormalDistribution"
                                    + "under the rotation apical axis ruleset.");
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
            default:
                throw new IllegalArgumentException(
                        "Invalid differentiation ruleset: " + differentiationRuleset);
        }
    }

    /**
     * Calculates the critical volume of a GMC daughter cell.
     *
     * @param gmcLoc the location of the GMC daughter cell
     * @return the critical volume of the GMC daughter cell
     */
    protected double calculateGMCDaughterCellCriticalVolume(PottsLocation gmcLoc) {
        double criticalVol;
        if (volumeBasedCriticalVolume) {
            criticalVol = Math.max(gmcLoc.getVolume(), initialSize * .1);
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

    /**
     * Gets all cell objects in the simulation that are Neuroblasts.
     *
     * @param sim the simulation
     * @return a HashSet of all PottsCellFlyStem cell objects in the simulation
     */
    public HashSet<PottsCellFlyStem> getNBsInSimulation(Simulation sim) {
        HashSet<PottsCellFlyStem> nbsInSimulation = new HashSet<>();
        Bag simObjects = sim.getGrid().getAllObjects();
        for (int i = 0; i < simObjects.numObjs; i++) {
            Object o = simObjects.objs[i];
            if (!(o instanceof PottsCell)) {
                continue; // skip non-cell objects
            }
            PottsCell cellInSim = (PottsCell) o;
            if (cell.getPop() == cellInSim.getPop() && o instanceof PottsCellFlyStem) {
                nbsInSimulation.add((PottsCellFlyStem) o);
            }
        }
        return nbsInSimulation;
    }
}
