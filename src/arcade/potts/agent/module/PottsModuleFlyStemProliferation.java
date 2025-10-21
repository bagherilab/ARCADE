package arcade.potts.agent.module;

import java.util.ArrayList;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.Distribution;
import arcade.core.util.distributions.NormalDistribution;
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
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsEnums.Phase;

public class PottsModuleFlyStemProliferation extends PottsModuleProliferationVolumeBasedDivision {

    /** Threshold for critical volume size checkpoint. */
    static final double SIZE_CHECKPOINT = 0.95;

    /**
     * Target ratio of critical volume for division size checkpoint (cell must reach CRITICAL_VOLUME
     * * SIZE_TARGET * SIZE_CHECKPOINT to divide).
     */
    double sizeTarget;

    /** Base growth rate for stem cells. */
    final double cellGrowthRateBase;

    /** Current growth rate for stem cells. */
    double cellGrowthRate;

    /** Basal rate of apoptosis (ticks^-1). */
    final double basalApoptosisRate;

    /** Distribution that determines rotational offset of cell's division plane. */
    final NormalDistribution splitDirectionDistribution;

    /** Ruleset for determining which daughter cell is the GMC. Can be `volume` or `location`. */
    final String differentiationRuleset;

    final String apicalAxisRuleset;

    final Distribution apicalAxisRotationDistribution;

    final boolean dynamicGrowthRateVolume;

    final boolean volumeBasedCriticalVolume;

    final double volumeBasedCriticalVolumeMultiplier;

    final double growthRateVolumeSensitivity;

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

        sizeTarget = parameters.getDouble("proliferation/SIZE_TARGET");
        cellGrowthRateBase = parameters.getDouble("proliferation/CELL_GROWTH_RATE");
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

        dynamicGrowthRateVolume =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME") != 0);
        updateGrowthRate();

        volumeBasedCriticalVolume =
                (parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME") != 0);

        volumeBasedCriticalVolumeMultiplier =
                (parameters.getDouble("proliferation/VOLUME_BASED_CRITICAL_VOLUME_MULTIPLIER"));
        growthRateVolumeSensitivity =
                parameters.getDouble("proliferation/GROWTH_RATE_VOLUME_SENSITIVITY");

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
     * Updates the cell growth rate based on the current parameter settings.
     *
     * <p>If {@code dynamicGrowthRateVolume} is {@code false}, the growth rate is fixed at the base
     * value specified in {@code cellGrowthRateBase}. If {@code dynamicGrowthRateVolume} is {@code
     * true}, the growth rate is scaled according to a power-law relationship between the current
     * cell volume and its critical volume. As the cell volume increases relative to the critical
     * volume, the growth rate accelerates proportionally:
     *
     * <pre>
     *   growthRate = baseGrowthRate * (volume / criticalVolume) ^ sensitivity
     * </pre>
     *
     * This allows larger cells to grow faster, capturing volume-dependent growth dynamics.
     */
    void updateGrowthRate() {
        if (dynamicGrowthRateVolume == false) {
            cellGrowthRate = cellGrowthRateBase;
        } else if (dynamicGrowthRateVolume == true) {
            double volume = cell.getLocation().getVolume();
            double Ka = cell.getCriticalVolume();
            cellGrowthRate =
                    cellGrowthRateBase * Math.pow((volume / Ka), growthRateVolumeSensitivity);
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
}
