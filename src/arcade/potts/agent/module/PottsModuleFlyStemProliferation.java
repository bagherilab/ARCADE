package arcade.potts.agent.module;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
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
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

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
    final boolean dynamicGrowthRateNBContact;

    /** Boolean flag indicating whether growth rate should be regulated by the cell's volume. */
    final double volumeBasedCriticalVolumeMultiplier;

    /**
     * Range of values considered equal when determining daughter cell identity. ex. if ruleset is
     * location, range determines the distance between centroid y values that is considered equal.
     */
    final double range;

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

        dynamicGrowthRateNBContact =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT") != 0);

        if (dynamicGrowthRateVolume && dynamicGrowthRateNBContact) {
            throw new InvalidParameterException(
                    "Dynamic growth rate can be either volume-based or NB-contact-based, not both.");
        }

        volumeBasedCriticalVolumeMultiplier =
                (parameters.getDouble("proliferation/VOLUME_BASED_CRITICAL_VOLUME_MULTIPLIER"));

        setPhase(Phase.UNDEFINED);
    }

    public static final double EPSILON = 1e-8;

    @Override
    public void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;

        Plane divisionPlane = chooseDivisionPlane(flyStemCell);
        PottsLocation2D parentLoc = (PottsLocation2D) cell.getLocation();
        PottsLocation daughterLoc = (PottsLocation) parentLoc.split(random, divisionPlane);

        boolean isDaughterStem = daughterStem(parentLoc, daughterLoc);

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
        if (dynamicGrowthRateVolume == true) {
            updateVolumeBasedGrowthRate();
        } else if (dynamicGrowthRateNBContact == true) {
            updateNBContactGrowthRate(sim);
        } else {
            cellGrowthRate = cellGrowthRateBase;
        }
    }

    /**
     * Gets the number of neighbors of this cell that are unique neuroblasts.
     *
     * @param sim the simulation
     * @return the number of unique neuroblast neighbors
     */
    protected Integer getNumNBNeighbors(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        ArrayList<Voxel> voxels = ((PottsLocation) cell.getLocation()).getVoxels();
        HashSet<PottsCell> stemNeighbors = new HashSet<PottsCell>();

        for (Voxel v : voxels) {
            HashSet<Integer> uniqueIDs = potts.getUniqueIDs(v.x, v.y, v.z);
            for (Integer id : uniqueIDs) {
                PottsCell neighbor = (PottsCell) sim.getGrid().getObjectAt(id);
                if (cell.getPop() == neighbor.getPop()) {
                    if (neighbor.getID() != cell.getID()) {
                        stemNeighbors.add((PottsCell) sim.getGrid().getObjectAt(id));
                    }
                }
            }
        }
        return stemNeighbors.size();
    }

    /**
     * Updates the cell's growth rate based on the number of neighboring neuroblasts.
     *
     * <p>This method applies a Hill-type repression function to scale the cell's base growth rate
     * according to local neuroblast density. Specifically, it counts the number of neighboring
     * neuroblasts (using {@link #getNumNBNeighbors(Simulation)}) and applies:
     *
     * <pre>
     *   hillRepression = K^n / (K^n + Np^n)
     *   cellGrowthRate = cellGrowthRateBase * hillRepression
     * </pre>
     *
     * where:
     *
     * <ul>
     *   <li><code>Np</code> is the number of neighboring neuroblasts
     *   <li><code>K</code> is the half-max parameter for repression
     *       (proliferation/NB_CONTACT_HALF_MAX)
     *   <li><code>n</code> is the Hill coefficient controlling steepness
     *       (proliferation/NB_CONTACT_HILL_N)
     *   <li><code>cellGrowthRateBase</code> is the base growth rate in the absence of neighbors
     * </ul>
     *
     * <p>This formulation ensures that when Np = 0, the cell grows at the base rate, and as the
     * number of neighbors increases, growth is repressed toward zero.
     *
     * @param sim the simulation
     */
    protected void updateNBContactGrowthRate(Simulation sim) {
        int NpRaw = getNumNBNeighbors(sim);
        double Np = Math.max(0.0, (double) NpRaw);

        // Parameters from the model
        double K = cell.getParameters().getDouble("proliferation/NB_CONTACT_HALF_MAX");
        double n = cell.getParameters().getDouble("proliferation/NB_CONTACT_HILL_N");

        double Kn = Math.pow(K, n);
        double Npn = Math.pow(Np, n);

        double hillRepression = Kn / (Kn + Npn);

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
                || (flyStemCell.getStemType() == StemType.MUDMUT && Math.abs(offset) < 45)) {
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
        Vector apical_axis = cell.getApicalAxis();
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        apical_axis, Direction.XY_PLANE.vector, rotationOffset);
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
     * Gets the voxel location the cell's plane of division will pass through.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division location for
     * @return the voxel location where the cell will split
     */
    public static Voxel getCellSplitVoxel(
            StemType stemType, PottsCellFlyStem cell, Vector rotatedNormalVector) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(stemType.splitOffsetPercentX);
        splitOffsetPercent.add(stemType.splitOffsetPercentY);
        return ((PottsLocation2D) cell.getLocation())
                .getOffsetInApicalFrame2D(splitOffsetPercent, rotatedNormalVector);
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
                return (centroidsWithinRangeAlongApicalAxis(
                        centroid1, centroid2, ((PottsCellFlyStem) cell).getApicalAxis(), range));
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

    /**
     * Makes a daughter NB cell
     *
     * @param daughterLoc the location of the daughter NB cell
     * @param sim the simulation
     * @param potts the potts instance for this simulation
     * @param random the random number generator
     */
    private void makeDaughterStemCell(
            PottsLocation daughterLoc, Simulation sim, Potts potts, MersenneTwisterFast random) {
        cell.reset(potts.ids, potts.regions);
        int newID = sim.getID();
        double criticalVol;
        if (volumeBasedCriticalVolume) {
            criticalVol = daughterLoc.getVolume() * volumeBasedCriticalVolumeMultiplier;
            cell.setCriticalVolume(
                    cell.getLocation().getVolume() * volumeBasedCriticalVolumeMultiplier);
        } else {
            criticalVol = cell.getCriticalVolume();
        }
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(newID, State.PROLIFERATIVE, random, cell.getPop(), criticalVol);
        scheduleNewCell(container, daughterLoc, sim, potts, random);
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
     * Calculates the critical volume of a GMC daughter cell
     *
     * @param gmcLoc the location of the GMC daughter cell
     * @return the critical volume of the GMC daughter cell
     */
    protected double calculateGMCDaughterCellCriticalVolume(PottsLocation gmcLoc) {
        double max_crit_vol =
                ((PottsCellFlyStem) cell).getCriticalVolume()
                        * sizeTarget
                        * ((PottsCellFlyStem) cell)
                                .getStemType()
                                .daughterCellCriticalVolumeProportion;
        if (volumeBasedCriticalVolume) {
            return gmcLoc.getVolume() * volumeBasedCriticalVolumeMultiplier;
        } else {
            return max_crit_vol;
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
}
