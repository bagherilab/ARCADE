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
import arcade.core.util.Utilities;
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

    /** Rate of Prospero change (ticks^-1). */
    final double prosperoRate;

    /**
     * Percent threshold of basal voxels considered when dividing Prospero at cell division. The sum
     * of apicalThreshold and basalThreshold should not exceed 1.
     */
    final double basalThreshold;

    /** Threshold concentration of Prospero in daughter cell to determine cell identity. */
    final double tfConcentration;

    /** Wild type stem cell split offset percent for x (0-100). */
    final int wtLikeX;

    /** Wild type stem cell split offset percent for y (0-100). */
    final int wtLikeY;

    /** Mud mutant stem cell split offset percent for x (0-100). */
    final int mmLikeX;

    /** Mud mutant stem cell split offset percent for y (0-100). */
    final int mmLikeY;

    /** Nanobody stem cell split offset percent for x (0-100). */
    final int nanoLikeX;

    /** Nanobody stem cell split offset percent for y (0-100). */
    final int nanoLikeY;

    /** Ruleset for determining which daughter cell is the GMC. Can be `volume` or `location`. */
    /**
     * Maximum angular deviation (degrees) from the expected split direction within which a MUDMUT
     * cell still divides by WT rules. Beyond this, the division uses the MUD plane.
     */
    static final double MUDMUT_WT_DIVISION_ANGLE_THRESHOLD = 75;

    /**
     * Minimum NB critical volume under {@code VOLUME_BASED_CRITICAL_VOLUME=1}, as a fraction of the
     * population critical volume.
     */
    static final double NB_CRITICAL_VOLUME_FLOOR_FRACTION = 0.20;

    /**
     * Minimum GMC critical volume under {@code VOLUME_BASED_CRITICAL_VOLUME=1}, as a fraction of
     * the parent NB's initial size.
     */
    static final double GMC_CRITICAL_VOLUME_FLOOR_FRACTION = 0.1;

    /** Rulesets for determining which daughter cell is the GMC on an asymmetric division. */
    public enum DifferentiationRuleset {
        /** The smaller daughter becomes the GMC. */
        SMALLER_GMC,

        /** The daughter on the basal side of the division plane becomes the GMC. */
        BASAL_GMC,

        /** The GMC daughter is chosen at random. */
        RANDOM,

        /** The daughter on the apical side of the division plane becomes the GMC. */
        APICAL_GMC
    }

    /** Rulesets for deciding whether a division is symmetric (both daughters stay neuroblasts). */
    public enum SymmetricDivisionRuleset {
        /** Symmetric if the larger daughter's share of the combined volume is below a threshold. */
        SIZE,

        /**
         * Symmetric if the daughters' centroids are within a range of each other along the cell's
         * apical axis.
         */
        RELATIVE_POSITION,

        /** Cell retains stem identity according to Prospero concentration */
        // TODO: add detail
        TF_CONCENTRATION
    }

    /** Rulesets for determining a daughter cell's apical axis. */
    public enum ApicalAxisRuleset {
        /** Daughter inherits the parent's apical axis. */
        GLOBAL,

        /** Parent's apical axis rotated by an angle drawn from a normal distribution. */
        NORMAL,

        /** Parent's apical axis rotated by an angle drawn from a uniform distribution. */
        UNIFORM
    }

    /** Rulesets for determining the Y split offset of a division. */
    public enum DivOffsetRuleset {
        /** Fixed offset, with the MUDMUT flip to the MUD plane beyond the angle threshold. */
        THRESHOLD,

        /** Offset ramps linearly with the drawn division angle's deviation from the mean. */
        LINEAR_RAMP,

        /** Offset follows a logistic transition centered on a switch angle. */
        SWITCH_RAMP
    }

    /** Distribution that determines rotational offset of cell's division plane. */
    final NormalDistribution splitDirectionDistribution;

    /**
     * Ruleset for determining which daughter cell is the GMC on an asymmetric division. Can be
     * `smaller_gmc`, `basal_gmc`, `random`, or `apical_gmc`. Whether a division is symmetric is
     * decided separately, by {@link #symmetricDivisionRuleset}.
     */
    final DifferentiationRuleset differentiationRuleset;

    /**
     * Ruleset deciding whether a division is symmetric (both daughters stay neuroblasts). Used only
     * when {@link #hasDeterministicDifferentiation} is false. `size`: the larger daughter's share
     * of the combined volume is below {@link #equalityOffsetPercentY}. `relative_position`: the
     * daughters' centroids are within {@link #range} along the cell's apical axis. Which daughter
     * becomes the GMC on an asymmetric division is decided separately, by {@link
     * #differentiationRuleset}.
     */
    final SymmetricDivisionRuleset symmetricDivisionRuleset;

    /**
     * Ruleset for determining how the cell determines its Apical Axis. Can be `global`, `normal`,
     * or `uniform`.
     */
    final ApicalAxisRuleset apicalAxisRuleset;

    /**
     * The distribution used to determine how apical axis should be rotated. Relevant when
     * apicalAxisRuleset is set to `normal` or `uniform`.
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
     * Distance (voxel lengths) within which two centroids count as equal under {@code
     * SYMMETRIC_DIVISION_RULESET=apical_axis}. An absolute length, because that ruleset compares a
     * separation.
     */
    final double range;

    /**
     * Split offset (%) below which a division counts as symmetric under {@code
     * SYMMETRIC_DIVISION_RULESET=size}: both daughters stay neuroblasts when the larger daughter's
     * share of the parent is less than this.
     *
     * <p>Same scale as {@link #wtDivisionSplitOffsetPercentY} and {@link
     * #divOffsetRampMinPercentY}. Because it is a ratio, the threshold doesn't depend on NB volume,
     * which varies several-fold across conditions.
     */
    final double equalityOffsetPercentY;

    /**
     * Half-max NB neighbor count for repression (K). Only relevant if {@link
     * #dynamicGrowthRateNBSelfRepression} is true.
     */
    final double nbContactHalfMax;

    /**
     * Hill coefficient for NB-contact repression (n). Only relevant if dynamicGrowthRateNBContact
     * is true.
     */
    final double nbContactHillN;

    final double prosperoHalfMax;
    final double prosperoHillN;
    final boolean dynamicGrowthRateProsperoRepression;

    /**
     * Boolean flag for whether the daughter cell's differentiation is determined deterministically.
     */
    final boolean hasDeterministicDifferentiation;

    /** The cell's initial size/volume (in voxels). */
    final double initialSize;

    /**
     * Population-level baseline critical volume in voxels, read from the population {@code
     * CRITICAL_VOLUME} parameter. Used as the fixed V_ref denominator in volume-based growth-rate
     * scaling, so V_ref stays anchored to the WT equilibrium regardless of per-cell critical
     * volumes — which matters when {@code VOLUME_BASED_CRITICAL_VOLUME=1} causes daughter critVols
     * to shrink below the population baseline.
     */
    final double populationCriticalVolume;

    /**
     * Y-axis split offset (%) used for WT-style divisions, for any cell type. Defaults to {@code
     * StemType.WT.splitOffsetPercentY} (93), giving the normal 93/7 NB/GMC asymmetry. Set to 50 in
     * a setup file (WT or MUDMUT) to give WT-style divisions a symmetric 50/50 volume split.
     *
     * <p>This applies wherever the WT division path is taken, including for MUDMUT cells dividing
     * within {@link #MUDMUT_WT_DIVISION_ANGLE_THRESHOLD}. The {@code StemType} offsets still apply
     * to the MUD division plane.
     */
    final int wtDivisionSplitOffsetPercentY;

    /**
     * Fixed GMC daughter critical volume override (voxels, after {@code DS^-3} conversion). When
     * greater than zero and {@code VOLUME_BASED_CRITICAL_VOLUME=0}, this replaces the
     * formula-derived value in {@link #calculateGMCDaughterCellCriticalVolume}, allowing GMC (and
     * therefore neuron) size to be set independently of the division offset. Zero disables the
     * override. Ignored entirely when {@code VOLUME_BASED_CRITICAL_VOLUME=1}, where critVol comes
     * from the daughter's birth volume.
     */
    final double gmcCriticalVolumeOverride;

    /**
     * Distribution of the angle (degrees) the NB's apical (polarity) axis is rotated by at the
     * start of each of its divisions, before the division plane is chosen. The rotated axis is
     * stored on the cell, so reorientations accumulate across a cell's divisions: a nonzero mean
     * gives persistent turning, a zero mean a random walk. The division plane is then the updated
     * apical axis rotated by a draw from {@link #splitDirectionDistribution}, which does not
     * accumulate.
     */
    final Distribution apicalAxisReorientationDistribution;

    /**
     * Y split offset (%) used by the most recent call to {@link #chooseDivisionPlane}. Under the
     * {@code threshold} ruleset this is always {@link #wtDivisionSplitOffsetPercentY}; under {@code
     * linear_ramp} it is the ramped value for that division. Read by the critical volume
     * calculations so the split geometry and the resulting thresholds agree within one division.
     */
    double lastSplitOffsetPercentY;

    /**
     * Ruleset determining the Y split offset for a division. Either {@code threshold} (fixed
     * offset, with the 75-degree MUDMUT flip to the MUD plane), {@code linear_ramp} (offset ramps
     * from the WT offset toward {@link #divOffsetRampMinPercentY} as the drawn division angle moves
     * away from the distribution mean), or {@code switch_ramp} (logistic transition centered on
     * {@link #divOffsetSwitchCenterAngle}).
     */
    final DivOffsetRuleset divOffsetRuleset;

    /**
     * Angle (degrees) from the division distribution mean at which the {@code linear_ramp} offset
     * reaches {@link #divOffsetRampMinPercentY}. Beyond this the offset is clamped.
     */
    final double divOffsetRampSaturationAngle;

    /** Minimum Y split offset (%) approached by the {@code linear_ramp} ruleset. */
    final int divOffsetRampMinPercentY;

    /**
     * Angle (degrees) from the division distribution mean at which the {@code switch_ramp} offset
     * sits midway between {@link #wtDivisionSplitOffsetPercentY} and {@link
     * #divOffsetRampMinPercentY}. Defaults to 75, the angle at which the {@code threshold} ruleset
     * flips to the MUD plane.
     */
    final double divOffsetSwitchCenterAngle;

    /**
     * Width (degrees) of the {@code switch_ramp} logistic transition. Smaller values are more
     * switch-like; the limit as this approaches zero is the {@code threshold} ruleset.
     */
    final double divOffsetSwitchWidth;

    /** Epsilon. */
    public static final double EPSILON = 1e-8;

    /**
     * Reads a lowercase ruleset parameter value (e.g. {@code basal_gmc}) and parses it into its
     * enum constant.
     *
     * @param <E> the enum type
     * @param enumClass the enum class to parse into
     * @param parameters the parameters to read the value from
     * @param key the parameter key
     * @return the matching enum constant
     * @throws InvalidParameterException if the value does not match any constant
     */
    static <E extends Enum<E>> E parseRuleset(
            Class<E> enumClass, Parameters parameters, String key) {
        String value = parameters.getString(key);
        ArrayList<String> validValues = new ArrayList<>();
        for (E constant : enumClass.getEnumConstants()) {
            String name = constant.name().toLowerCase();
            if (name.equals(value)) {
                return constant;
            }
            validValues.add(name);
        }
        throw new InvalidParameterException(
                key + " must be one of " + validValues + " but was " + value);
    }

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
        basalThreshold = parameters.getDouble("proliferation/BASAL_THRESHOLD");
        tfConcentration = parameters.getDouble("proliferation/TF_CONCENTRATION");
        wtLikeX = parameters.getInt("proliferation/WT_LIKE_X");
        wtLikeY = parameters.getInt("proliferation/WT_LIKE_Y");
        mmLikeX = parameters.getInt("proliferation/MM_LIKE_X");
        mmLikeY = parameters.getInt("proliferation/MM_LIKE_Y");

        if (parameters.getInt("proliferation/NANO_LIKE_X") == -1) {
            nanoLikeX = 100 - wtLikeX;
        } else {
            nanoLikeX = parameters.getInt("proliferation/NANO_LIKE_X");
        }
        if (parameters.getInt("proliferation/NANO_LIKE_Y") == -1) {
            nanoLikeY = 100 - wtLikeY;
        } else {
            nanoLikeY = parameters.getInt("proliferation/NANO_LIKE_Y");
        }

        splitDirectionDistribution =
                (NormalDistribution)
                        parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION");
        differentiationRuleset =
                parseRuleset(
                        DifferentiationRuleset.class,
                        parameters,
                        "proliferation/DIFFERENTIATION_RULESET");
        symmetricDivisionRuleset =
                parseRuleset(
                        SymmetricDivisionRuleset.class,
                        parameters,
                        "proliferation/SYMMETRIC_DIVISION_RULESET");
        range = parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE");
        equalityOffsetPercentY =
                parameters.getDouble(
                        "proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT");
        apicalAxisRuleset =
                parseRuleset(
                        ApicalAxisRuleset.class, parameters, "proliferation/APICAL_AXIS_RULESET");
        apicalAxisRotationDistribution =
                (Distribution)
                        parameters.getDistribution(
                                "proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION");

        if (apicalAxisRuleset == ApicalAxisRuleset.NORMAL
                && !(apicalAxisRotationDistribution instanceof NormalDistribution)) {
            throw new InvalidParameterException(
                    "APICAL_AXIS_ROTATION_DISTRIBUTION must be a NormalDistribution "
                            + "under the normal apical axis ruleset");
        }
        if (apicalAxisRuleset == ApicalAxisRuleset.UNIFORM
                && !(apicalAxisRotationDistribution instanceof UniformDistribution)) {
            throw new InvalidParameterException(
                    "APICAL_AXIS_ROTATION_DISTRIBUTION must be a UniformDistribution "
                            + "under the uniform apical axis ruleset");
        }

        volumeBasedCriticalVolume =
                (parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME") != 0);

        dynamicGrowthRateNBSelfRepression =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_SELF_REPRESSION") != 0);

        dynamicGrowthRateProsperoRepression =
                (parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_PROSPERO_REPRESSION") != 0);

        // TODO: do this for prospero-based growth rate too?
        if (dynamicGrowthRateVolume && dynamicGrowthRateNBSelfRepression) {
            throw new InvalidParameterException(
                    "Dynamic growth rate can be either volume-based or NB-contact-based, not both.");
        }

        if (basalThreshold > 1 || basalThreshold < 0) {
            throw new InvalidParameterException(
                    "Basal threshold should be nonnegative and not greater than 1");
        }
        nbContactHalfMax = parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX");
        nbContactHillN = parameters.getDouble("proliferation/NB_CONTACT_HILL_N");
        prosperoHalfMax = parameters.getDouble("proliferation/PROSPERO_HALF_MAX");
        prosperoHillN = parameters.getDouble("proliferation/PROSPERO_HILL_N");

        String hasDeterministicDifferentiationString =
                parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION");
        if (!hasDeterministicDifferentiationString.equals("TRUE")
                && !hasDeterministicDifferentiationString.equals("FALSE")) {
            throw new InvalidParameterException(
                    "hasDeterministicDifferentiation must be either TRUE or FALSE");
        }
        hasDeterministicDifferentiation = hasDeterministicDifferentiationString.equals("TRUE");

        initialSize = cell.getVolume();
        populationCriticalVolume = parameters.getDouble("CRITICAL_VOLUME");

        wtDivisionSplitOffsetPercentY =
                parameters.getInt("proliferation/WT_DIVISION_SPLIT_OFFSET_PERCENT_Y");

        gmcCriticalVolumeOverride =
                parameters.getDouble("proliferation/GMC_CRITICAL_VOLUME_OVERRIDE");

        apicalAxisReorientationDistribution =
                parameters.getDistribution("proliferation/APICAL_AXIS_REORIENTATION_DISTRIBUTION");

        divOffsetRuleset =
                parseRuleset(
                        DivOffsetRuleset.class, parameters, "proliferation/DIV_OFFSET_RULESET");
        divOffsetSwitchCenterAngle =
                parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_CENTER_ANGLE");
        divOffsetSwitchWidth = parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH");
        if (divOffsetRuleset == DivOffsetRuleset.SWITCH_RAMP && divOffsetSwitchWidth <= 0) {
            throw new InvalidParameterException("divOffsetSwitchWidth must be greater than zero");
        }
        divOffsetRampSaturationAngle =
                parameters.getDouble("proliferation/DIV_OFFSET_RAMP_SATURATION_ANGLE");
        divOffsetRampMinPercentY = parameters.getInt("proliferation/DIV_OFFSET_RAMP_MIN_PERCENT_Y");
        lastSplitOffsetPercentY = wtDivisionSplitOffsetPercentY;

        setPhase(Phase.UNDEFINED);
    }

    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        super.step(random, sim);
        ((PottsCellFly) cell).setProspero(((PottsCellFly) cell).getProspero() + prosperoRate);
        System.out.println(
                "Stem ID " + cell.getID() + " prospero: " + ((PottsCellFly) cell).getProspero());
    }

    @Override
    public void addCell(MersenneTwisterFast random, Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        PottsCellFlyStem flyStemCell = (PottsCellFlyStem) cell;

        reorientApicalAxis(flyStemCell);
        Plane divisionPlane = chooseDivisionPlane(flyStemCell);

        PottsLocation2D parentLoc = (PottsLocation2D) cell.getLocation();

        // TODO: All of the prospero logic here should be moved to a helper.
        ArrayList<Voxel> voxels = ((PottsLocation) cell.getLocation()).getVoxels();
        double[] centroid = ((PottsLocation) cell.getLocation()).getCentroid();
        Vector apicalAxis = flyStemCell.getApicalAxis();

        ArrayList<Voxel> borderVoxels = ((PottsLocation) cell.getLocation()).getBorderVoxels();
        Bag basalVoxels =
                PottsLocation.getDirectionalVoxelSubset(
                        Side.BASAL, basalThreshold, borderVoxels, centroid, apicalAxis);

        PottsLocation daughterLoc = (PottsLocation) parentLoc.split(random, divisionPlane);

        double basalFrac =
                Utilities.collectionFraction(
                        Utilities.asCollection(basalVoxels, Voxel.class), daughterLoc.getVoxels());

        double parentProspero = ((PottsCellFly) cell).getProspero();

        double daughterProspero = parentProspero * basalFrac;

        boolean isDaughterStem =
                daughterStem(parentLoc, daughterLoc, divisionPlane, daughterProspero);

        if (isDaughterStem) {
            makeDaughterStemCell(daughterLoc, sim, potts, random, daughterProspero);

        } else {
            makeDaughterGMC(
                    parentLoc,
                    daughterLoc,
                    sim,
                    potts,
                    random,
                    divisionPlane.getUnitNormalVector(),
                    daughterProspero);
        }
    }

    /**
     * Updates the effective growth rate according to the ruleset indicated in parameters.
     *
     * @param sim the simulation
     */
    @Override
    public void updateGrowthRate(Simulation sim) {
        if (dynamicGrowthRateVolume == true) {
            updateVolumeBasedGrowthRate(sim);
        } else if (dynamicGrowthRateNBSelfRepression == true) {
            updateGrowthRateBasedOnOtherNBs(sim);
        } else if (dynamicGrowthRateProsperoRepression == true) {
            updateGrowthRateBasedOnProspero();
        } else {
            cellGrowthRate = cellGrowthRateBase;
        }
    }

    /**
     * Updates growth rate based on cell volume relative to a reference volume.
     *
     * <p>Growth is regulated by comparing this cell's volume to an equilibrium reference. The
     * difference is passed to the volume-based growth function, which adjusts growth rate relative
     * to the equilibrium volume.
     *
     * @param sim the simulation
     */
    public void updateVolumeBasedGrowthRate(Simulation sim) {
        double vRef = computeEquilibriumVolume();
        updateCellVolumeBasedGrowthRate(cell.getLocation().getVolume(), vRef);
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
     *         = sizeTarget * populationCriticalVolume * (f_retain + 1) / 2
     * </pre>
     *
     * where {@code f_retain = WT_DIVISION_SPLIT_OFFSET_PERCENT_Y / 100} approximates the fraction
     * of the pre-division volume retained by the NB after asymmetric division. The WT offset is
     * used for every cell type, since MUDMUT cells also divide by WT rules within {@link
     * #MUDMUT_WT_DIVISION_ANGLE_THRESHOLD}.
     *
     * <p>This reference volume is used as the normalization denominator in the volume-based growth
     * rate formula, ensuring that at the average NB volume the effective growth rate equals {@code
     * cellGrowthRateBase}.
     *
     * <p>The population {@code CRITICAL_VOLUME} is used rather than this cell's own critical
     * volume, so V_ref stays anchored to the WT equilibrium. Under {@code
     * VOLUME_BASED_CRITICAL_VOLUME=1} a daughter's critVol tracks its birth volume and can drift
     * well below the population baseline; using it here would move the reference along with the
     * cell and defeat the regulation.
     *
     * <p>{@code f_retain} is {@link #wtDivisionSplitOffsetPercentY} for every offset ruleset, not
     * just {@code threshold}. Under a ramp the per-division offset varies, but every ruleset is
     * required to satisfy the same WT calibration, which pins the WT mean offset at the imposed
     * value — a switch ramp that passes the calibration gate has a WT mean within 0.3% of 93. A
     * V_ref that tracked the ruleset's own offset distribution would let the growth setpoint drift
     * with the very quantity the calibration fixes, which defeats the purpose of a reference.
     *
     * @return the expected average NB volume
     */
    double computeEquilibriumVolume() {
        double vDiv = sizeTarget * populationCriticalVolume;
        double fRetain = wtDivisionSplitOffsetPercentY / 100.0;
        return vDiv * (fRetain + 1.0) / 2.0;
    }

    /**
     * Counts the distinct neuroblasts adjacent to this cell.
     *
     * @param sim the simulation
     * @return the number of distinct neuroblasts adjacent to this cell
     */
    protected int countNBNeighbors(Simulation sim) {
        Potts potts = ((PottsSimulation) sim).getPotts();
        HashSet<Integer> neighborIDs = new HashSet<>();
        for (Voxel v : ((PottsLocation) cell.getLocation()).getVoxels()) {
            neighborIDs.addAll(potts.getUniqueIDs(v.x, v.y, v.z));
        }
        neighborIDs.remove(cell.getID());

        int nbCount = 0;
        for (int id : neighborIDs) {
            if (sim.getGrid().getObjectAt(id) instanceof PottsCellFlyStem) {
                nbCount++;
            }
        }
        return nbCount;
    }

    /**
     * Updates the neuroblast (NB) contact-dependent growth rate.
     *
     * <p>Growth is repressed by contact with neighboring NBs (local coupling) using a Hill
     * function:
     *
     * <pre>
     * growthRate = baseGrowthRate * K ^ n / (K ^ n + N ^ n)
     * </pre>
     *
     * <p>Here {@code N} is the number of distinct NBs adjacent to this cell (see {@link
     * #countNBNeighbors}), {@code K} is {@link #nbContactHalfMax}, the neighbor count giving 50%
     * repression, and {@code n} is {@link #nbContactHillN}, the Hill exponent controlling how
     * steeply growth falls off. When {@code K = 0}, growth is unrepressed if {@code N = 0} and
     * fully repressed otherwise.
     *
     * @param sim the simulation
     */
    protected void updateGrowthRateBasedOnOtherNBs(Simulation sim) {
        int nbsInContact = countNBNeighbors(sim);
        double np = Math.max(0.0, (double) nbsInContact);

        cellGrowthRate = cellGrowthRateBase * hillRepression(np, nbContactHalfMax, nbContactHillN);
    }

    protected void updateGrowthRateBasedOnProspero() {
        double concentration =
                prosperoConcentration(
                        ((PottsCellFly) cell).getProspero(), (PottsLocation) cell.getLocation());
        cellGrowthRate =
                cellGrowthRateBase * hillRepression(concentration, prosperoHalfMax, prosperoHillN);
    }

    // factored this out to use
    public static double hillRepression(double value, double halfMax, double hillN) {
        double K = Math.pow(halfMax, hillN);
        double Vn = Math.pow(value, hillN);
        if (K == 0.0) {
            return (value == 0.0) ? 1.0 : 0.0;
        }
        return K / (K + Vn);
    }

    /**
     * Whether the configured offset ruleset derives the split offset from the drawn angle.
     *
     * <p>Single source of truth for the ramp check. A new ruleset must be added here, so that it
     * cannot silently fall through to the {@code threshold} path — which is exactly what happened
     * when {@code switch_ramp} was first introduced and its logistic was never reached during a
     * simulation.
     *
     * @return {@code true} for every ruleset that ramps the offset with the drawn angle
     */
    boolean usesRampedOffset() {
        return divOffsetRuleset == DivOffsetRuleset.LINEAR_RAMP
                || divOffsetRuleset == DivOffsetRuleset.SWITCH_RAMP;
    }

    /**
     * Chooses the division plane for this fly stem cell based on the offset ruleset and whether the
     * cell is WT or MUDMUT.
     *
     * <p>A rotation offset is drawn from {@link #splitDirectionDistribution}. Under the ramped
     * offset rulesets ({@code linear_ramp}, {@code switch_ramp}), the plane is built from that
     * offset for both WT and MUDMUT cells, with the split offset from {@link
     * #computeSplitOffsetPercentY}. Under {@code threshold}, a WT cell always divides along the WT
     * plane, and a MUDMUT cell divides along the WT plane if the drawn offset is within {@link
     * #MUDMUT_WT_DIVISION_ANGLE_THRESHOLD} degrees of the distribution mean, and along the MUD
     * plane otherwise.
     *
     * @param flyStemCell the stem cell this module is attached to
     * @return the plane along which this cell should divide
     */
    protected Plane chooseDivisionPlane(PottsCellFlyStem flyStemCell) {
        double offset = sampleDivisionPlaneOffset();

        if (usesRampedOffset()) {
            lastSplitOffsetPercentY = computeSplitOffsetPercentY(offset);
            return buildDivisionPlane(
                    flyStemCell, flyStemCell.getApicalAxis(), offset, lastSplitOffsetPercentY);
        }

        lastSplitOffsetPercentY = wtDivisionSplitOffsetPercentY;
        switch (flyStemCell.getStemType()) {
            case WT:
                return getWTDivisionPlane(flyStemCell, offset);
            case MUDMUT:
                if (Math.abs(offset - splitDirectionDistribution.getExpected())
                        <= MUDMUT_WT_DIVISION_ANGLE_THRESHOLD) {
                    return getWTDivisionPlane(flyStemCell, offset);
                }
                return getMUDDivisionPlane(flyStemCell);
            default:
                throw new IllegalArgumentException(
                        "Invalid stem type: " + flyStemCell.getStemType());
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
     * Computes the Y split offset for a division from the drawn rotation offset.
     *
     * <p>Under the {@code linear_ramp} ruleset the offset falls linearly from {@link
     * #wtDivisionSplitOffsetPercentY} toward {@link #divOffsetRampMinPercentY} as the drawn angle
     * moves away from the division distribution mean, reaching the minimum at {@link
     * #divOffsetRampSaturationAngle} and clamping beyond it.
     *
     * <p>Under the {@code switch_ramp} ruleset the offset follows a logistic in the same deviation,
     * centred on {@link #divOffsetSwitchCenterAngle} with width {@link #divOffsetSwitchWidth}. It
     * is flat near the distribution mean, so typical divisions keep the calibrated asymmetry and
     * only extreme angles approach a symmetric split; as the width approaches zero it converges on
     * the {@code threshold} ruleset. A linear ramp is flat nowhere, which shifts the mean split for
     * every division and breaks the WT calibration — see {@code
     * docs/superpowers/plans/2026-09-03-graded-division-offset.md}.
     *
     * @param rotationOffset the angle drawn from the division rotation distribution
     * @return the Y split offset percentage for this division
     */
    double computeSplitOffsetPercentY(double rotationOffset) {
        double deviation = Math.abs(rotationOffset - splitDirectionDistribution.getExpected());
        double span = wtDivisionSplitOffsetPercentY - divOffsetRampMinPercentY;

        if (divOffsetRuleset == DivOffsetRuleset.SWITCH_RAMP) {
            double z = (deviation - divOffsetSwitchCenterAngle) / divOffsetSwitchWidth;
            return divOffsetRampMinPercentY + span / (1.0 + Math.exp(z));
        }

        double fraction = Math.min(deviation / divOffsetRampSaturationAngle, 1.0);
        return wtDivisionSplitOffsetPercentY - span * fraction;
    }

    /**
     * Rotates the cell's apical axis by one draw from {@link #apicalAxisReorientationDistribution}
     * and stores it on the cell. Called once at the start of every NB division, so the rotations
     * accumulate across the cell's divisions. A zero draw leaves the axis untouched.
     *
     * @param cell the {@link PottsCellFlyStem} whose apical axis is reoriented
     */
    void reorientApicalAxis(PottsCellFlyStem cell) {
        double angle = apicalAxisReorientationDistribution.nextDouble();
        if (angle == 0) {
            return;
        }
        cell.setApicalAxis(
                Vector.rotateVectorAroundAxis(
                        cell.getApicalAxis(), Direction.XY_PLANE.vector, angle));
    }

    /**
     * Builds a division plane from an explicit reference vector, rotation, and split offset.
     *
     * <p>Genotype-neutral: the two canonical divisions are corners of this function, defined by the
     * {@link StemType} entries. {@code WT(50, 93, 0)} gives rotation 0 and offset 93; {@code
     * MUDMUT(50, 50, -90)} gives rotation -90 and offset 50. The {@code linear_ramp} ruleset
     * interpolates between those corners as the drawn angle grows.
     *
     * @param cell the {@link PottsCellFlyStem} to build the plane for
     * @param referenceVector the vector the rotation is measured from
     * @param rotationOffset the angle to rotate the reference vector by
     * @param splitOffsetPercentY the Y split offset percentage to divide at
     * @return the division plane
     */
    public Plane buildDivisionPlane(
            PottsCellFlyStem cell,
            Vector referenceVector,
            double rotationOffset,
            double splitOffsetPercentY) {
        Vector rotatedNormalVector =
                Vector.rotateVectorAroundAxis(
                        referenceVector, Direction.XY_PLANE.vector, rotationOffset);
        Voxel splitVoxel =
                getCellSplitVoxel(
                        wtLikeX, (int) Math.round(splitOffsetPercentY), cell, rotatedNormalVector);
        return new Plane(
                new Double3D(splitVoxel.x, splitVoxel.y, splitVoxel.z), rotatedNormalVector);
    }

    /**
     * Gets the division plane for a WT-rules division: the reference vector rotated by the drawn
     * angle, split at {@link #wtDivisionSplitOffsetPercentY}. One corner of {@link
     * #buildDivisionPlane}.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @param rotationOffset the angle to rotate the plane
     * @return the division plane for the cell
     */
    public Plane getWTDivisionPlane(PottsCellFlyStem cell, double rotationOffset) {
        return buildDivisionPlane(
                cell, cell.getApicalAxis(), rotationOffset, wtDivisionSplitOffsetPercentY);
    }

    /**
     * Gets the division plane for a MUD-rules division: the apical axis rotated by {@link
     * StemType#splitDirectionRotation}, split symmetrically. The other corner of {@link
     * #buildDivisionPlane}. Always measured from the apical axis, never from the previous division
     * normal.
     *
     * @param cell the {@link PottsCellFlyStem} to get the division plane for
     * @return the division plane for the cell
     */
    public Plane getMUDDivisionPlane(PottsCellFlyStem cell) {
        return buildDivisionPlane(
                cell, cell.getApicalAxis(), StemType.MUDMUT.splitDirectionRotation, mmLikeY);
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
    public Voxel getCellSplitVoxel(
            StemType stemType, PottsCellFlyStem cell, Vector rotatedNormalVector) {
        int splitOffsetPercentX;
        int splitOffsetPercentY;
        switch (stemType) {
            case WT:
                splitOffsetPercentX = wtLikeX;
                splitOffsetPercentY = wtLikeY;
                break;
            case MUDMUT:
                splitOffsetPercentX = mmLikeX;
                splitOffsetPercentY = mmLikeY;
                break;
            case NANOBODY:
                splitOffsetPercentX = nanoLikeX;
                splitOffsetPercentY = nanoLikeY;
                break;
            default:
                throw new IllegalArgumentException("Stemtype must be WT, MUDMUT, or NANOBODY");
        }
        return getCellSplitVoxel(
                splitOffsetPercentX, splitOffsetPercentY, cell, rotatedNormalVector);
    }

    /**
     * Gets the voxel location the cell's plane of division will pass through, using explicit x and
     * y offsets rather than a {@link StemType}.
     *
     * @param splitOffsetPercentX percentage x offset from cell edge
     * @param splitOffsetPercentY percentage y offset from cell edge
     * @param cell the {@link PottsCellFlyStem} to get the division location for
     * @param rotatedNormalVector the normal vector of the division plane
     * @return the voxel location where the cell will split
     */
    public static Voxel getCellSplitVoxel(
            int splitOffsetPercentX,
            int splitOffsetPercentY,
            PottsCellFlyStem cell,
            Vector rotatedNormalVector) {
        ArrayList<Integer> splitOffsetPercent = new ArrayList<>();
        splitOffsetPercent.add(splitOffsetPercentX);
        splitOffsetPercent.add(splitOffsetPercentY);
        return ((PottsLocation2D) cell.getLocation())
                .getOffsetInApicalFrame(splitOffsetPercent, rotatedNormalVector);
    }

    /**
     * Determines whether the daughter cell should be a neuroblast or a GMC according to the
     * symmetric division ruleset specified in the parameters and the morphologies of the daughter
     * cell locations.
     *
     * <p>Applies to both stem types. Whether a WT cell can produce a symmetric NB-NB division is
     * controlled by {@code HAS_DETERMINISTIC_DIFFERENTIATION}: under {@code TRUE} the deterministic
     * path is used instead and a WT daughter is never a stem cell; under {@code FALSE} a WT cell
     * uses the same {@link #symmetricDivisionRuleset} as MUDMUT, so a sufficiently symmetric
     * division yields two neuroblasts. Setting the relevant threshold near zero suppresses that in
     * practice while leaving the mechanism available.
     *
     * @param loc1 one cell location post division
     * @param loc2 the other cell location post division
     * @return whether or not the daughter cell should be a stem cell
     */
    private boolean daughterStemRuleBasedDifferentiation(
            PottsLocation loc1, PottsLocation loc2, double daughterProspero) {
        StemType stemType = ((PottsCellFlyStem) cell).getStemType();
        if (stemType != StemType.WT && stemType != StemType.MUDMUT) {
            throw new IllegalArgumentException("Invalid stem type: " + stemType);
        }
        switch (symmetricDivisionRuleset) {
            case SIZE:
                return isSizeSymmetric(loc1, loc2);
            case RELATIVE_POSITION:
                return centroidsAreWithinRangeAlongApicalAxis(
                        loc1.getCentroid(),
                        loc2.getCentroid(),
                        ((PottsCellFlyStem) cell).getApicalAxis(),
                        range);
            case TF_CONCENTRATION:
                double daughterConcentration = prosperoConcentration(daughterProspero, loc2);
                return daughterConcentration <= tfConcentration;
            default:
                throw new IllegalArgumentException(
                        "Invalid symmetric division ruleset: " + symmetricDivisionRuleset);
        }
    }

    /**
     * Whether a division is symmetric by size: the larger daughter's share of the combined volume
     * is below {@link #equalityOffsetPercentY}. Relative, so the parent's volume cancels and one
     * threshold serves every condition.
     *
     * @param loc1 one cell location post division
     * @param loc2 the other cell location post division
     * @return {@code true} if both daughters should remain neuroblasts
     */
    private boolean isSizeSymmetric(PottsLocation loc1, PottsLocation loc2) {
        double vol1 = loc1.getVolume();
        double vol2 = loc2.getVolume();
        double total = vol1 + vol2;
        if (total <= 0) {
            return false;
        }
        double largerSharePercent = 100.0 * Math.max(vol1, vol2) / total;
        return largerSharePercent < equalityOffsetPercentY;
    }

    /**
     * Determines whether the daughter cell should be a neuroblast or a GMC according to the
     * orientation of the apical axis. This is deterministic.
     *
     * @param divisionPlane the plane the cell will divide along
     * @return {@code true} if the daughter should be a stem cell; {@code false} if the daughter
     *     should be a GMC
     */
    private boolean daughterStemDeterministic(Plane divisionPlane) {
        // A WT division always produces one NB and one GMC, so the daughter is never a
        // stem cell.
        // Without this, a WT division plane that happened to align with the expected
        // MUD normal
        // would be misread as a symmetric NB-NB division. This guard is what makes
        // HAS_DETERMINISTIC_DIFFERENTIATION the on/off switch for WT symmetric
        // divisions: the
        // rule-based path applies the geometric ruleset to both stem types, this path
        // never does
        // for WT.
        if (((PottsCellFlyStem) cell).getStemType() == StemType.WT) {
            return false;
        }

        Vector normalVector = divisionPlane.getUnitNormalVector();

        Vector apicalAxis = ((PottsCellFlyStem) cell).getApicalAxis();
        Vector expectedMUDNormalVector =
                Vector.rotateVectorAroundAxis(
                        apicalAxis,
                        Direction.XY_PLANE.vector,
                        StemType.MUDMUT.splitDirectionRotation);
        // The daughter is a stem cell only if the cell divided along the MUD plane,
        // i.e. this
        // plane's normal matches the expected MUD normal. Compare per component within
        // EPSILON,
        // since Vector.equals is exact and the rotations introduce floating-point
        // error.
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
            PottsLocation2D parentsLoc,
            PottsLocation daughterLoc,
            Plane divisionPlane,
            double daughterProspero) {
        return hasDeterministicDifferentiation
                ? daughterStemDeterministic(divisionPlane)
                : daughterStemRuleBasedDifferentiation(parentsLoc, daughterLoc, daughterProspero);
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
    static boolean centroidsAreWithinRangeAlongApicalAxis(
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
     * <p>Under {@code VOLUME_BASED_CRITICAL_VOLUME=1} each cell takes its own birth volume as its
     * critical volume: the parent the volume it retained, the daughter the volume it received. Both
     * are floored at {@link #NB_CRITICAL_VOLUME_FLOOR_FRACTION} of the population critical volume.
     *
     * @param daughterLoc the location of the daughter NB cell
     * @param sim the simulation
     * @param potts the potts instance for this simulation
     * @param random the random number generator
     * @param daughterProspero the amount of Prospero inherited by the daughter NB cell
     */
    void makeDaughterStemCell(
            PottsLocation daughterLoc,
            Simulation sim,
            Potts potts,
            MersenneTwisterFast random,
            double daughterProspero) {
        int newID = sim.getID();
        double daughterCriticalVol;
        if (volumeBasedCriticalVolume) {
            double floor = populationCriticalVolume * NB_CRITICAL_VOLUME_FLOOR_FRACTION;
            daughterCriticalVol = Math.max(daughterLoc.getVolume(), floor);
            cell.setCriticalVolume(Math.max(cell.getLocation().getVolume(), floor));
        } else {
            daughterCriticalVol = cell.getCriticalVolume();
        }
        cell.reset(potts.ids, potts.regions);
        PottsCellContainer container =
                ((PottsCellFlyStem) cell)
                        .make(
                                newID,
                                State.PROLIFERATIVE,
                                random,
                                cell.getPop(),
                                daughterCriticalVol);

        System.out.print("Creating daughter stem cell with prospero " + daughterProspero + ", ");
        scheduleNewCell(container, daughterLoc, sim, potts, random, daughterProspero);
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
            double daughterProspero) {
        Location gmcLoc = determineGMCLocation(parentLoc, daughterLoc, divisionPlaneNormal, random);

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

        System.out.print("Creating daughter GMC with prospero " + daughterProspero + ", ");
        scheduleNewCell(container, daughterLoc, sim, potts, random, daughterProspero);
    }

    /**
     * Converts the daughter container into a cell, assigns its apical axis if it is a neuroblast,
     * and adds it to the grid, Potts lattice, and schedule.
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
            double daughterProspero) {
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
            case GLOBAL:
                return ((PottsCellFlyStem) cell).getApicalAxis();
            case NORMAL:
            case UNIFORM:
                return Vector.rotateVectorAroundAxis(
                        ((PottsCellFlyStem) cell).getApicalAxis(),
                        Direction.XY_PLANE.vector,
                        apicalAxisRotationDistribution.nextDouble());
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
     * @param random the random number generator
     * @return the location that should be the GMC
     */
    Location determineGMCLocation(
            PottsLocation parentLoc,
            PottsLocation daughterLoc,
            Vector divisionPlaneNormal,
            MersenneTwisterFast random) {
        switch (differentiationRuleset) {
            case SMALLER_GMC:
                return PottsLocation.getSmallerLocation(parentLoc, daughterLoc);
            case BASAL_GMC: // basal = further along the division plane normal
                return PottsLocation.getLocationFurtherAlongAxis(
                        parentLoc, daughterLoc, divisionPlaneNormal);
            case RANDOM:
                return random.nextBoolean() ? parentLoc : daughterLoc;
            case APICAL_GMC:
                PottsLocation basalLoc =
                        PottsLocation.getLocationFurtherAlongAxis(
                                parentLoc, daughterLoc, divisionPlaneNormal);
                return (basalLoc == parentLoc) ? daughterLoc : parentLoc;
            default:
                throw new IllegalArgumentException(
                        "Invalid differentiation ruleset: " + differentiationRuleset);
        }
    }

    /**
     * Calculates the critical volume of a GMC daughter cell.
     *
     * <p>Under {@code VOLUME_BASED_CRITICAL_VOLUME=1} the value comes from the daughter's birth
     * volume, floored at {@link #GMC_CRITICAL_VOLUME_FLOOR_FRACTION} of the parent's initial size.
     * Otherwise it is derived from the parent's critical volume and the WT split offset unless
     * {@code GMC_CRITICAL_VOLUME_OVERRIDE} is greater than zero, in which case that fixed value is
     * used instead. The override lets GMC (and therefore neuron) size be set independently of the
     * division offset, and is ignored when {@code VOLUME_BASED_CRITICAL_VOLUME=1}.
     *
     * @param gmcLoc the location of the GMC daughter cell
     * @return the critical volume of the GMC daughter cell
     */
    protected double calculateGMCDaughterCellCriticalVolume(PottsLocation gmcLoc) {
        if (volumeBasedCriticalVolume) {
            return Math.max(gmcLoc.getVolume(), initialSize * GMC_CRITICAL_VOLUME_FLOOR_FRACTION);
        }
        if (gmcCriticalVolumeOverride > 0) {
            return gmcCriticalVolumeOverride;
        }
        // Ramped rulesets use this division's drawn offset; threshold uses the fixed WT
        // offset.
        double offsetPercentY =
                usesRampedOffset() ? lastSplitOffsetPercentY : wtDivisionSplitOffsetPercentY;
        return ((PottsCellFlyStem) cell).getCriticalVolume()
                * sizeTarget
                * (1.0 - offsetPercentY / 100.0);
    }

    public static double prosperoConcentration(double prospero, PottsLocation loc) {
        return prospero / loc.getVolume();
    }
}
