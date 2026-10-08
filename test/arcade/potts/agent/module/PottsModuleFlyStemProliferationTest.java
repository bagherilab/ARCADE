package arcade.potts.agent.module;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sim.util.Bag;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.NormalDistribution;
import arcade.core.util.distributions.UniformDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums;
import arcade.potts.util.PottsEnums.Phase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.State;

public class PottsModuleFlyStemProliferationTest {
    PottsCellFlyStem stemCell;

    PottsModuleFlyStemProliferation module;

    PottsLocation2D stemLoc;

    PottsLocation daughterLoc;

    Parameters parameters;

    PottsSimulation sim;

    Potts potts;

    Grid grid;

    PottsCellFactory factory;

    MersenneTwisterFast random;

    NormalDistribution dist;

    NormalDistribution reorientationDist;

    public static final double EPSILON = 1e-6f;

    int stemCellPop;

    @BeforeEach
    public final void setup() {
        // Core mocks
        stemCell = mock(PottsCellFlyStem.class);
        parameters = mock(Parameters.class);
        dist = mock(NormalDistribution.class);
        sim = mock(PottsSimulation.class);
        potts = mock(Potts.class);
        grid = mock(Grid.class);
        factory = mock(PottsCellFactory.class);
        random = mock(MersenneTwisterFast.class);

        // Location mocks
        stemLoc = mock(PottsLocation2D.class);
        daughterLoc = mock(PottsLocation.class);

        // Wire simulation
        when(((PottsSimulation) sim).getPotts()).thenReturn(potts);
        potts.ids = new int[1][1][1];
        potts.regions = new int[1][1][1];
        when(sim.getGrid()).thenReturn(grid);
        when(sim.getCellFactory()).thenReturn(factory);
        when(sim.getSchedule()).thenReturn(mock(sim.engine.Schedule.class));
        when(sim.getID()).thenReturn(42);

        // Wire cell
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemCell.getParameters()).thenReturn(parameters);
        when(stemLoc.split(eq(random), any(Plane.class))).thenReturn(daughterLoc);

        // Default centroid and volume values (sometimes overridden in tests)
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(stemCell.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.6, 0});

        // Parameter stubs (sometimes overridden in tests)
        when(parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION"))
                .thenReturn(dist);
        when(dist.nextDouble()).thenReturn(0.1);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET")).thenReturn("size");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT"))
                .thenReturn(60.0);
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("TRUE");
        reorientationDist = mock(NormalDistribution.class);
        when(reorientationDist.nextDouble()).thenReturn(0.0);
        when(parameters.getDistribution("proliferation/APICAL_AXIS_REORIENTATION_DISTRIBUTION"))
                .thenReturn(reorientationDist);
        when(parameters.getInt("proliferation/WT_DIVISION_SPLIT_OFFSET_PERCENT_Y")).thenReturn(93);
        when(parameters.getDouble("proliferation/GMC_CRITICAL_VOLUME_OVERRIDE")).thenReturn(0.0);
        when(parameters.getDouble("CRITICAL_VOLUME")).thenReturn(100.0);
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("threshold");
        when(parameters.getDouble("proliferation/DIV_OFFSET_RAMP_SATURATION_ANGLE"))
                .thenReturn(90.0);
        when(parameters.getInt("proliferation/DIV_OFFSET_RAMP_MIN_PERCENT_Y")).thenReturn(50);
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_CENTER_ANGLE")).thenReturn(75.0);
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH")).thenReturn(5.0);

        // Link selection
        GrabBag links = mock(GrabBag.class);
        when(stemCell.getLinks()).thenReturn(links);
        when(links.next(random)).thenReturn(2);

        // Other defaults
        stemCellPop = 3;
        when(stemCell.getPop()).thenReturn(stemCellPop);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
    }

    @AfterEach
    final void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    // Constructor tests

    @Test
    public void constructor_setsParameters() {
        when(parameters.getDouble("proliferation/BASAL_APOPTOSIS_RATE")).thenReturn(0.04);
        when(parameters.getDouble("proliferation/PROSPERO_RATE")).thenReturn(0.895);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(parameters.getDistribution("proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION"))
                .thenReturn(dist);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_SELF_REPRESSION"))
                .thenReturn(0);

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(0.04, module.basalApoptosisRate, EPSILON);
        assertEquals(0.895, module.prosperoRate, EPSILON);
        assertNotNull(module.splitDirectionDistribution);
        assertEquals("volume", module.differentiationRuleset);
        assertEquals(0.5, module.range, EPSILON);
        assertEquals("global", module.apicalAxisRuleset);
        assertNotNull(module.apicalAxisRotationDistribution);
        assertFalse(module.volumeBasedCriticalVolume);
        assertFalse(module.dynamicGrowthRateNBSelfRepression);
    }

    @Test
    public void constructor_smallerGmcRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.42);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertNotNull(module.splitDirectionDistribution);
        assertEquals("smaller_gmc", module.differentiationRuleset);
        assertEquals(0.42, module.range, EPSILON);
        assertEquals(arcade.potts.util.PottsEnums.Phase.UNDEFINED, module.phase);
    }

    @Test
    public void constructor_defaultRuleset_setsThreshold() {
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals("threshold", module.divOffsetRuleset);
        assertEquals(90.0, module.divOffsetRampSaturationAngle, EPSILON);
        assertEquals(50, module.divOffsetRampMinPercentY);
    }

    @Test
    public void constructor_linearRampRuleset_setsLinearRamp() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("linear_ramp");

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals("linear_ramp", module.divOffsetRuleset);
    }

    @Test
    public void constructor_invalidDivOffsetRuleset_throwsException() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("nonsense");

        assertThrows(
                InvalidParameterException.class,
                () -> new PottsModuleFlyStemProliferation(stemCell));
    }

    @Test
    public void constructor_switchRampRuleset_readsLogisticParameters() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("switch_ramp");
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_CENTER_ANGLE")).thenReturn(75.0);
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH")).thenReturn(5.0);

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals("switch_ramp", module.divOffsetRuleset);
        assertEquals(75.0, module.divOffsetSwitchCenterAngle, EPSILON);
        assertEquals(5.0, module.divOffsetSwitchWidth, EPSILON);
    }

    @Test
    public void constructor_switchRampZeroWidth_throwsException() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("switch_ramp");
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH")).thenReturn(0.0);

        assertThrows(
                InvalidParameterException.class,
                () -> new PottsModuleFlyStemProliferation(stemCell));
    }

    // Split offset ramp tests

    /** Builds a module using the switch_ramp ruleset with the given logistic shape. */
    private void useSwitchRamp(double center, double width) {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("switch_ramp");
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_CENTER_ANGLE"))
                .thenReturn(center);
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH")).thenReturn(width);
        module = new PottsModuleFlyStemProliferation(stemCell);
    }

    @Test
    public void computeSplitOffsetPercentY_switchRampAtCenterAngle_returnsMidpointOffset() {
        when(dist.getExpected()).thenReturn(0.0);
        useSwitchRamp(75.0, 5.0);

        assertEquals(71.5, module.computeSplitOffsetPercentY(75.0), 1e-6);
        assertEquals(71.5, module.computeSplitOffsetPercentY(-75.0), 1e-6);
    }

    @Test
    public void computeSplitOffsetPercentY_switchRampAtMean_returnsMaxOffset() {
        when(dist.getExpected()).thenReturn(0.0);
        useSwitchRamp(75.0, 5.0);

        // 50 + 43/(1 + exp(-15)) = 92.99999987
        assertEquals(93.0, module.computeSplitOffsetPercentY(0.0), 1e-4);
    }

    @Test
    public void computeSplitOffsetPercentY_switchRampFarBeyondCenter_approachesMinOffset() {
        when(dist.getExpected()).thenReturn(0.0);
        useSwitchRamp(75.0, 5.0);

        assertEquals(50.0, module.computeSplitOffsetPercentY(150.0), 1e-3);
    }

    @Test
    public void computeSplitOffsetPercentY_switchRamp_symmetricAboutDistributionMean() {
        when(dist.getExpected()).thenReturn(20.0);
        useSwitchRamp(75.0, 5.0);

        assertEquals(
                module.computeSplitOffsetPercentY(60.0),
                module.computeSplitOffsetPercentY(-20.0),
                1e-9);
    }

    @Test
    public void computeSplitOffsetPercentY_switchRampNarrowWidth_nearlyReachesMinAtMudAngle() {
        when(dist.getExpected()).thenReturn(0.0);
        useSwitchRamp(75.0, 3.0);

        // 50 + 43/(1 + exp(5)) = 50.29; a narrow transition has all but finished by 90 degrees,
        // where the division plane coincides with the MUD plane.
        assertEquals(50.29, module.computeSplitOffsetPercentY(90.0), 0.01);
    }

    @Test
    public void computeSplitOffsetPercentY_switchRampWideWidth_staysAboveMinAtMudAngle() {
        when(dist.getExpected()).thenReturn(0.0);
        useSwitchRamp(75.0, 8.0);

        // 50 + 43/(1 + exp(1.875)) = 55.72; a wide transition is only two thirds complete at 90
        // degrees, so it does not reproduce the MUD split there.
        assertEquals(55.72, module.computeSplitOffsetPercentY(90.0), 0.01);
    }

    @Test
    public void computeSplitOffsetPercentY_linearRamp_unchangedAfterSwitchRampRefactor() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("linear_ramp");
        when(dist.getExpected()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(93.0, module.computeSplitOffsetPercentY(0.0), EPSILON);
        assertEquals(71.5, module.computeSplitOffsetPercentY(45.0), EPSILON);
        assertEquals(50.0, module.computeSplitOffsetPercentY(90.0), EPSILON);
    }

    @Test
    public void computeSplitOffsetPercentY_atMean_returnsMaxOffset() {
        when(dist.getExpected()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(93.0, module.computeSplitOffsetPercentY(0.0), EPSILON);
    }

    @Test
    public void computeSplitOffsetPercentY_atSaturation_returnsMinOffset() {
        when(dist.getExpected()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(50.0, module.computeSplitOffsetPercentY(90.0), EPSILON);
        assertEquals(50.0, module.computeSplitOffsetPercentY(-90.0), EPSILON);
    }

    @Test
    public void computeSplitOffsetPercentY_beyondSaturation_clampsToMinOffset() {
        when(dist.getExpected()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(50.0, module.computeSplitOffsetPercentY(150.0), EPSILON);
        assertEquals(50.0, module.computeSplitOffsetPercentY(-150.0), EPSILON);
    }

    @Test
    public void computeSplitOffsetPercentY_midRange_interpolatesLinearly() {
        when(dist.getExpected()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        // 93 - 43 * (45/90) = 71.5
        assertEquals(71.5, module.computeSplitOffsetPercentY(45.0), EPSILON);
        // 93 - 43 * (75/90) = 57.166...
        assertEquals(93.0 - 43.0 * (75.0 / 90.0), module.computeSplitOffsetPercentY(75.0), EPSILON);
    }

    @Test
    public void computeSplitOffsetPercentY_isSymmetricAboutMean() {
        when(dist.getExpected()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        for (double angle : new double[] {15.0, 30.0, 60.0, 88.0}) {
            assertEquals(
                    module.computeSplitOffsetPercentY(angle),
                    module.computeSplitOffsetPercentY(-angle),
                    EPSILON);
        }
    }

    @Test
    public void computeSplitOffsetPercentY_nonZeroMean_measuresDeviationFromMean() {
        when(dist.getExpected()).thenReturn(36.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        // deviation 0 from a mean of 36
        assertEquals(93.0, module.computeSplitOffsetPercentY(36.0), EPSILON);
        // deviation 45 from a mean of 36
        assertEquals(71.5, module.computeSplitOffsetPercentY(81.0), EPSILON);
    }

    @Test
    public void buildDivisionPlane_explicitOffset_usesThatOffset() {
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.buildDivisionPlane(stemCell, new Vector(0, 1, 0), 0.0, 57.0);

        ArrayList<Integer> expected = new ArrayList<>();
        expected.add(50);
        expected.add(57);
        verify(stemLoc).getOffsetInApicalFrame(eq(expected), any(Vector.class));
    }

    @Test
    public void rampEndpoints_matchStemTypeEnumCorners() {
        module = new PottsModuleFlyStemProliferation(stemCell);

        // The ramp interpolates between the two StemType corners; if the enum or the parameter
        // defaults drift apart, the "interpolates WT -> MUD" claim silently stops being true.
        assertEquals(
                PottsCellFlyStem.StemType.WT.splitOffsetPercentY,
                module.wtDivisionSplitOffsetPercentY);
        assertEquals(
                PottsCellFlyStem.StemType.MUDMUT.splitOffsetPercentY,
                module.divOffsetRampMinPercentY);
        assertEquals(
                Math.abs(PottsCellFlyStem.StemType.MUDMUT.splitDirectionRotation),
                module.divOffsetRampSaturationAngle,
                EPSILON);
    }

    @Test
    public void chooseDivisionPlane_linearRampMudmut_takesRampPathNotMudPath() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("linear_ramp");
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(dist.nextDouble()).thenReturn(120.0); // beyond the 75-degree threshold
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.chooseDivisionPlane(stemCell);

        // Ramp saturates, so the offset is the MUD corner rather than the 93 the old WT path used.
        assertEquals(50.0, module.lastSplitOffsetPercentY, EPSILON);
    }

    @Test
    public void chooseDivisionPlane_switchRampMudmut_takesRampPathNotMudPath() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("switch_ramp");
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_CENTER_ANGLE")).thenReturn(75.0);
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH")).thenReturn(5.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(dist.nextDouble()).thenReturn(120.0); // beyond the 75-degree centre
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.chooseDivisionPlane(stemCell);

        // 50 + 43/(1 + exp(9)) = 50.005; the logistic has saturated, so the recorded offset must be
        // the ramped value and not the 93 the threshold path would record.
        assertEquals(50.005, module.lastSplitOffsetPercentY, 0.01);
    }

    @Test
    public void chooseDivisionPlane_switchRampWtSmallAngle_recordsNearWtOffset() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("switch_ramp");
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_CENTER_ANGLE")).thenReturn(75.0);
        when(parameters.getDouble("proliferation/DIV_OFFSET_SWITCH_WIDTH")).thenReturn(5.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(dist.nextDouble()).thenReturn(20.0); // a typical WT draw
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.chooseDivisionPlane(stemCell);

        // Flat region of the logistic: 50 + 43/(1 + exp(-11)) = 92.9993.
        assertEquals(93.0, module.lastSplitOffsetPercentY, 0.01);
    }

    @Test
    public void chooseDivisionPlane_thresholdMudmutBeyondThreshold_stillUsesMudPlane() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(dist.nextDouble()).thenReturn(120.0);
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        Plane result = module.chooseDivisionPlane(stemCell);
        Plane mudPlane = module.getMUDDivisionPlane(stemCell);

        assertEquals(
                mudPlane.getUnitNormalVector().getY(),
                result.getUnitNormalVector().getY(),
                EPSILON);
        assertEquals(93.0, module.lastSplitOffsetPercentY, EPSILON);
    }

    @Test
    public void chooseDivisionPlane_thresholdRuleset_recordsWtOffset() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(dist.nextDouble()).thenReturn(10.0);
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.chooseDivisionPlane(stemCell);

        assertEquals(93.0, module.lastSplitOffsetPercentY, EPSILON);
    }

    @Test
    public void daughterStem_wtRuleBasedSymmetricSplit_returnsTrue() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(2.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(9.5); // difference 0.5 < range 2.0
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertTrue(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
    }

    @Test
    public void daughterStem_wtRuleBasedAsymmetricSplit_returnsFalse() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(2.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(3.0); // difference 7.0 > range 2.0
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertFalse(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
    }

    @Test
    public void daughterStem_wtDeterministic_alwaysReturnsFalse() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("TRUE");
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        Plane mudPlane = module.getMUDDivisionPlane(stemCell);

        // Even a plane matching the MUD normal exactly must not make a WT daughter a stem cell.
        assertFalse(module.daughterStem(stemLoc, daughterLoc, mudPlane));
    }

    @Test
    public void calculateGMCCriticalVolume_thresholdRuleset_usesWtOffset() {
        module = new PottsModuleFlyStemProliferation(stemCell);
        module.lastSplitOffsetPercentY = 57.0; // must be ignored on the threshold path

        double result = module.calculateGMCDaughterCellCriticalVolume(daughterLoc);

        // critVol 100.0 * sizeTarget 1.2 * (1 - 0.93) = 8.4
        assertEquals(8.4, result, EPSILON);
    }

    @Test
    public void calculateGMCCriticalVolume_linearRamp_usesPerDivisionOffset() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("linear_ramp");
        module = new PottsModuleFlyStemProliferation(stemCell);
        module.lastSplitOffsetPercentY = 57.0;

        double result = module.calculateGMCDaughterCellCriticalVolume(daughterLoc);

        // critVol 100.0 * sizeTarget 1.2 * (1 - 0.57) = 51.6
        assertEquals(51.6, result, EPSILON);
    }

    @Test
    public void makeDaughterStemCell_volumeBased_parentKeepsItsOwnVolume() {
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("linear_ramp");
        when(stemLoc.getVolume()).thenReturn(57.0); // parent retained
        when(daughterLoc.getVolume()).thenReturn(43.0); // daughter received
        when(parameters.getDouble("CRITICAL_VOLUME")).thenReturn(100.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(anyInt(), any(), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        module = new PottsModuleFlyStemProliferation(stemCell);
        module.makeDaughterStemCell(daughterLoc, sim, potts, random);

        // The parent must take its own retained volume, not the daughter's 43.0.
        verify(stemCell).setCriticalVolume(57.0);
        verify(stemCell).make(anyInt(), any(), any(), anyInt(), eq(43.0));
    }

    @Test
    public void makeDaughterStemCell_thresholdRuleset_parentKeepsItsOwnVolume() {
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(stemLoc.getVolume()).thenReturn(53.0); // parent retained
        when(daughterLoc.getVolume()).thenReturn(47.0); // daughter received
        when(parameters.getDouble("CRITICAL_VOLUME")).thenReturn(100.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(anyInt(), any(), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        module = new PottsModuleFlyStemProliferation(stemCell);
        module.makeDaughterStemCell(daughterLoc, sim, potts, random);

        // Each cell takes its own birth volume on the threshold path too. MUD (NB-NB) divisions
        // run on this path, and a nominally 50/50 MUD split is not exactly equal in voxel count,
        // so the parent must not inherit the daughter's threshold.
        verify(stemCell).setCriticalVolume(53.0);
        verify(stemCell).make(anyInt(), any(), any(), anyInt(), eq(47.0));
    }

    @Test
    public void computeEquilibriumVolume_thresholdRuleset_unchanged() {
        module = new PottsModuleFlyStemProliferation(stemCell);

        // sizeTarget 1.2 * critVol 100.0 = 120.0 ; 120.0 * (0.93 + 1) / 2 = 115.8
        assertEquals(115.8, module.computeEquilibriumVolume(), EPSILON);
    }

    @Test
    public void computeEquilibriumVolume_anyRuleset_usesImposedWtOffset() {
        // Every ruleset must satisfy the same WT calibration, which pins the WT mean offset at the
        // imposed value. V_ref therefore does not track the ruleset's own offset distribution.
        for (String ruleset : new String[] {"threshold", "linear_ramp", "switch_ramp"}) {
            when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn(ruleset);
            MiniBox distParams = new MiniBox();
            distParams.put("MU", 0.0);
            distParams.put("SIGMA", 50.0);
            when(dist.getParameters()).thenReturn(distParams);
            module = new PottsModuleFlyStemProliferation(stemCell);

            // sizeTarget 1.2 * critVol 100.0 = 120.0 ; 120.0 * (0.93 + 1) / 2 = 115.8
            assertEquals(115.8, module.computeEquilibriumVolume(), EPSILON);
        }
    }

    @Test
    public void linearRamp_atSaturation_matchesMudDivisionPlaneAndOffset() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("linear_ramp");
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.getExpected()).thenReturn(0.0);
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        // The offset at saturation equals the MUDMUT stem type's own split offset.
        assertEquals(
                PottsCellFlyStem.StemType.MUDMUT.splitOffsetPercentY,
                module.computeSplitOffsetPercentY(-90.0),
                EPSILON);

        // The plane orientation at saturation equals the MUD plane's orientation.
        Plane ramped = module.buildDivisionPlane(stemCell, stemCell.getApicalAxis(), -90.0, 50.0);
        Plane mud = module.getMUDDivisionPlane(stemCell);

        assertEquals(
                mud.getUnitNormalVector().getX(), ramped.getUnitNormalVector().getX(), EPSILON);
        assertEquals(
                mud.getUnitNormalVector().getY(), ramped.getUnitNormalVector().getY(), EPSILON);
        assertEquals(
                mud.getUnitNormalVector().getZ(), ramped.getUnitNormalVector().getZ(), EPSILON);
    }

    @Test
    public void constructor_basalGmcRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("basal_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.99);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertNotNull(module.splitDirectionDistribution);
        assertEquals("basal_gmc", module.differentiationRuleset);
        assertEquals(0.99, module.range, EPSILON);
        assertEquals(arcade.potts.util.PottsEnums.Phase.UNDEFINED, module.phase);
    }

    @Test
    public void constructor_conflictingDynamicGrowthRateFlags_throwsInvalidParameterException() {
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(1);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_SELF_REPRESSION"))
                .thenReturn(1);

        assertThrows(
                InvalidParameterException.class,
                () -> new PottsModuleFlyStemProliferation(stemCell));
    }

    @Test
    public void
            constructor_invalidHasDeterministicDifferentiationString_throwsInvalidParameterException() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("yes");

        assertThrows(
                InvalidParameterException.class,
                () -> new PottsModuleFlyStemProliferation(stemCell));
    }

    @Test
    public void constructor_cellHasRegions_throwsUnsupportedOperationException() {
        when(stemCell.hasRegions()).thenReturn(true);

        assertThrows(
                UnsupportedOperationException.class,
                () -> new PottsModuleFlyStemProliferation(stemCell));
    }

    // Static method tests

    @Test
    public void getSmallerLocation_locationsDifferentSizes_returnsSmallerLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getVolume()).thenReturn(5.0);
        when(loc2.getVolume()).thenReturn(10.0);

        PottsLocation result = PottsModuleFlyStemProliferation.getSmallerLocation(loc1, loc2);
        assertEquals(loc1, result);
    }

    @Test
    public void getSmallerLocation_locationsSameSize_returnsSecondLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getVolume()).thenReturn(10.0);
        when(loc2.getVolume()).thenReturn(10.0);

        PottsLocation result = PottsModuleFlyStemProliferation.getSmallerLocation(loc1, loc2);
        assertEquals(loc2, result);
    }

    @Test
    public void getBasalLocation_centroidsDifferent_returnsBasalCentroid() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 1, 0});
        Vector apicalAxis = new Vector(0, 1, 0);

        PottsLocation result =
                PottsModuleFlyStemProliferation.getBasalLocation(loc1, loc2, apicalAxis);
        assertEquals(loc1, result);
    }

    @Test
    public void getBasalLocation_centroidsSame_returnsFirstLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 2, 0});
        Vector apicalAxis = new Vector(0, 1, 0);

        PottsLocation result =
                PottsModuleFlyStemProliferation.getBasalLocation(loc1, loc2, apicalAxis);
        assertEquals(loc1, result);
    }

    @Test
    public void getApicalLocation_centroidsDifferent_returnsApicalCentroid() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 1, 0});
        Vector apicalAxis = new Vector(0, 1, 0);

        PottsLocation result =
                PottsModuleFlyStemProliferation.getApicalLocation(loc1, loc2, apicalAxis);
        assertEquals(loc2, result);
    }

    @Test
    public void getApicalLocation_centroidsSame_returnsSecondLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 2, 0});
        Vector apicalAxis = new Vector(0, 1, 0);

        PottsLocation result =
                PottsModuleFlyStemProliferation.getApicalLocation(loc1, loc2, apicalAxis);
        assertEquals(loc2, result);
    }

    @Test
    public void determineGMCLocation_random_nextBooleanTrue_returnsParent() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("random");
        module = new PottsModuleFlyStemProliferation(stemCell);
        when(random.nextBoolean()).thenReturn(true);

        Location result =
                module.determineGMCLocation(
                        stemLoc, (PottsLocation) daughterLoc, new Vector(0, 1, 0), random);
        assertEquals(stemLoc, result);
    }

    @Test
    public void determineGMCLocation_random_nextBooleanFalse_returnsDaughter() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("random");
        module = new PottsModuleFlyStemProliferation(stemCell);
        when(random.nextBoolean()).thenReturn(false);

        Location result =
                module.determineGMCLocation(
                        stemLoc, (PottsLocation) daughterLoc, new Vector(0, 1, 0), random);
        assertEquals(daughterLoc, result);
    }

    @Test
    public void determineGMCLocation_apicalGmc_returnsApicalLocation() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("apical_gmc");
        module = new PottsModuleFlyStemProliferation(stemCell);

        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 1, 0});
        Vector apicalAxis = new Vector(0, 1, 0);

        Location result = module.determineGMCLocation(loc1, loc2, apicalAxis, random);
        assertEquals(loc2, result);
    }

    @Test
    public void centroidsWithinRangeAlongApicalAxis_withinRange_returnsTrue() {
        double[] centroid1 = new double[] {0, 1.0, 0};
        double[] centroid2 = new double[] {0, 1.3, 0};
        Vector apicalAxis = new Vector(0, 1, 0); // projecting along y-axis
        double range = 0.5;

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result =
                PottsModuleFlyStemProliferation.centroidsWithinRangeAlongApicalAxis(
                        centroid1, centroid2, apicalAxis, range);

        assertTrue(result);
    }

    @Test
    public void centroidsWithinRangeAlongApicalAxis_equalToRange_returnsTrue() {
        double[] centroid1 = new double[] {0, 1.0, 0};
        double[] centroid2 = new double[] {0, 1.5, 0};
        Vector apicalAxis = new Vector(0, 1, 0);
        double range = 0.5;

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result =
                PottsModuleFlyStemProliferation.centroidsWithinRangeAlongApicalAxis(
                        centroid1, centroid2, apicalAxis, range);

        assertTrue(result);
    }

    @Test
    public void centroidsWithinRangeAlongApicalAxis_outsideRange_returnsFalse() {
        double[] centroid1 = new double[] {0, 1.0, 0};
        double[] centroid2 = new double[] {0, 1.6, 0};
        Vector apicalAxis = new Vector(0, 1, 0);
        double range = 0.5;

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result =
                PottsModuleFlyStemProliferation.centroidsWithinRangeAlongApicalAxis(
                        centroid1, centroid2, apicalAxis, range);

        assertFalse(result);
    }

    @Test
    public void centroidsWithinRangeAlongApicalAxis_nonYAxis_returnsCorrectly() {
        double[] centroid1 = new double[] {1.0, 0.0, 0.0};
        double[] centroid2 = new double[] {1.6, 0.0, 0.0};
        Vector apicalAxis = new Vector(1, 0, 0); // projecting along x-axis
        double range = 0.6;

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result =
                PottsModuleFlyStemProliferation.centroidsWithinRangeAlongApicalAxis(
                        centroid1, centroid2, apicalAxis, range);

        assertTrue(result);
    }

    // Split location tests

    @Test
    public void getCellSplitVoxel_WT_callsLocationOffsetWithCorrectParams() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT.splitOffsetPercentX
        expectedOffset.add(93); // WT.splitOffsetPercentY

        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class)))
                .thenReturn(new Voxel(0, 0, 0));

        PottsModuleFlyStemProliferation.getCellSplitVoxel(
                PottsCellFlyStem.StemType.WT, stemCell, stemCell.getApicalAxis(), 50, 93);
        verify(stemLoc).getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class));
    }

    @Test
    public void getCellSplitVoxel_MUDMUT_callsLocationOffsetWithCorrectParams() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT.splitOffsetPercentX
        expectedOffset.add(50); // MUDMUT.splitOffsetPercentY

        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class)))
                .thenReturn(new Voxel(0, 0, 0));

        PottsModuleFlyStemProliferation.getCellSplitVoxel(
                PottsCellFlyStem.StemType.MUDMUT, stemCell, stemCell.getApicalAxis(), 50, 50);
        verify(stemLoc).getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class));
    }

    // Division plane tests

    @Test
    public void getWTDivisionPlane_rotatesCorrectlyAndReturnsPlane() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        double baseRotation = PottsCellFlyStem.StemType.WT.splitDirectionRotation; // 90
        double offsetRotation = -5.0;

        Voxel splitVoxel = new Voxel(3, 4, 5);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT x offset percent
        expectedOffset.add(93); // WT y offset percent

        module = new PottsModuleFlyStemProliferation(stemCell);

        // Apply both rotations manually to get expected result
        Vector afterBaseRotation =
                Vector.rotateVectorAroundAxis(apicalAxis, new Vector(0, 0, 1), baseRotation);
        Vector expectedNormal =
                Vector.rotateVectorAroundAxis(
                        afterBaseRotation, new Vector(0, 0, 1), offsetRotation);

        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), eq(expectedNormal)))
                .thenReturn(splitVoxel);

        Plane result = module.getWTDivisionPlane(stemCell, offsetRotation);

        Double3D refPoint = result.getReferencePoint();
        assertEquals(3.0, refPoint.x, EPSILON);
        assertEquals(4.0, refPoint.y, EPSILON);
        assertEquals(5.0, refPoint.z, EPSILON);

        Vector resultNormal = result.getUnitNormalVector();
        assertEquals(expectedNormal.getX(), resultNormal.getX(), EPSILON);
        assertEquals(expectedNormal.getY(), resultNormal.getY(), EPSILON);
        assertEquals(expectedNormal.getZ(), resultNormal.getZ(), EPSILON);
    }

    @Test
    public void getWTDivisionPlane_MUDMUT_withOffset50_uses50PercentY() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getInt("proliferation/WT_DIVISION_SPLIT_OFFSET_PERCENT_Y")).thenReturn(50);

        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT.splitOffsetPercentX
        expectedOffset.add(50); // overridden y offset, not WT's 93

        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class)))
                .thenReturn(new Voxel(1, 2, 3));

        module = new PottsModuleFlyStemProliferation(stemCell);
        module.getWTDivisionPlane(stemCell, 0.0);

        verify(stemLoc).getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class));
    }

    @Test
    public void getWTDivisionPlane_WT_withOffset50_uses50PercentY() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getInt("proliferation/WT_DIVISION_SPLIT_OFFSET_PERCENT_Y")).thenReturn(50);

        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // x
        expectedOffset.add(50); // y override

        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class)))
                .thenReturn(new Voxel(1, 2, 3));

        module = new PottsModuleFlyStemProliferation(stemCell);
        module.getWTDivisionPlane(stemCell, 0.0);

        verify(stemLoc).getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class));
    }

    @Test
    public void computeEquilibriumVolume_withOffset50_returnsExpected() {
        // fRetain = 50/100 = 0.50; V_div = 1.2 * 100 = 120
        // V_ref = 120 * (0.50 + 1) / 2 = 90.0
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getInt("proliferation/WT_DIVISION_SPLIT_OFFSET_PERCENT_Y")).thenReturn(50);
        module = new PottsModuleFlyStemProliferation(stemCell);
        assertEquals(90.0, module.computeEquilibriumVolume(), EPSILON);
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_withOffset50_usesOverrideProportion() {
        // GMC proportion = 1 - 50/100 = 0.50
        // expected = critVol * sizeTarget * 0.50 = 100 * 1.2 * 0.50 = 60.0
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(parameters.getInt("proliferation/WT_DIVISION_SPLIT_OFFSET_PERCENT_Y")).thenReturn(50);

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(60.0, module.calculateGMCDaughterCellCriticalVolume(daughterLoc), EPSILON);
    }

    @Test
    public void getCellSplitVoxel_explicitOffsets_callsLocationOffsetWithThoseOffsets() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(11);
        expectedOffset.add(22);

        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class)))
                .thenReturn(new Voxel(0, 0, 0));

        PottsModuleFlyStemProliferation.getCellSplitVoxel(
                11, 22, stemCell, stemCell.getApicalAxis());

        verify(stemLoc).getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class));
    }

    @Test
    public void getWTDivisionPlane_measuresRotationFromApicalAxis() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        double offset = 45.0;
        Vector expectedNormal =
                Vector.rotateVectorAroundAxis(apicalAxis, Direction.XY_PLANE.vector, offset);
        module.getWTDivisionPlane(stemCell, offset);

        verify(stemLoc).getOffsetInApicalFrame(any(), eq(expectedNormal));
    }

    @Test
    public void reorientApicalAxis_zeroAngle_leavesApicalAxisUnchanged() {
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(reorientationDist.nextDouble()).thenReturn(0.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.reorientApicalAxis(stemCell);

        verify(stemCell, never()).setApicalAxis(any());
    }

    @Test
    public void reorientApicalAxis_nonzeroAngle_rotatesAndStoresApicalAxis() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);
        when(reorientationDist.nextDouble()).thenReturn(30.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.reorientApicalAxis(stemCell);

        verify(stemCell)
                .setApicalAxis(
                        Vector.rotateVectorAroundAxis(apicalAxis, Direction.XY_PLANE.vector, 30.0));
    }

    @Test
    public void reorientApicalAxis_repeated_accumulatesAcrossDivisions() {
        Vector[] stored = {new Vector(0, 1, 0)};
        when(stemCell.getApicalAxis()).thenAnswer(inv -> stored[0]);
        doAnswer(
                        inv -> {
                            stored[0] = inv.getArgument(0);
                            return null;
                        })
                .when(stemCell)
                .setApicalAxis(any());
        when(reorientationDist.nextDouble()).thenReturn(20.0, 25.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.reorientApicalAxis(stemCell);
        module.reorientApicalAxis(stemCell);

        Vector expected =
                Vector.rotateVectorAroundAxis(new Vector(0, 1, 0), Direction.XY_PLANE.vector, 45.0);
        assertEquals(expected.getX(), stored[0].getX(), EPSILON);
        assertEquals(expected.getY(), stored[0].getY(), EPSILON);
    }

    @Test
    public void chooseDivisionPlane_usesCurrentApicalAxisAndDivisionOrientationDraw() {
        Vector reoriented =
                Vector.rotateVectorAroundAxis(new Vector(0, 1, 0), Direction.XY_PLANE.vector, 40.0);
        when(stemCell.getApicalAxis()).thenReturn(reoriented);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(dist.nextDouble()).thenReturn(8.0);
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(new Voxel(0, 0, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        module.chooseDivisionPlane(stemCell);

        Vector expectedNormal =
                Vector.rotateVectorAroundAxis(reoriented, Direction.XY_PLANE.vector, 8.0);
        verify(stemLoc).getOffsetInApicalFrame(any(), eq(expectedNormal));
    }

    @Test
    public void switchRamp_offsetDependsOnlyOnDivisionOrientationDraw() {
        when(parameters.getString("proliferation/DIV_OFFSET_RULESET")).thenReturn("switch_ramp");
        when(dist.getExpected()).thenReturn(0.0);
        when(reorientationDist.nextDouble()).thenReturn(80.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        // a large reorientation of the polarity axis does not enter the offset: only the
        // division orientation (the angle to the current apical axis) does
        assertEquals(module.computeSplitOffsetPercentY(0.0), 93.0, 0.5);
    }

    @Test
    public void getMUDDivisionPlane_returnsRotatedPlaneWithCorrectNormal() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        Vector expectedNormal = new Vector(1.0, 0.0, 0.0);

        Voxel splitVoxel = new Voxel(7, 8, 9);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT x offset percent
        expectedOffset.add(50); // MUDMUT y offset percent
        // MUD plane keeps the StemType offsets; it is not affected by
        // WT_DIVISION_SPLIT_OFFSET_PERCENT_Y.
        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any())).thenReturn(splitVoxel);

        module = new PottsModuleFlyStemProliferation(stemCell);
        Plane result = module.getMUDDivisionPlane(stemCell);

        assertEquals(new Double3D(7, 8, 9), result.getReferencePoint());
        Vector resultNormal = result.getUnitNormalVector();
        assertEquals(expectedNormal.getX(), resultNormal.getX(), EPSILON);
        assertEquals(expectedNormal.getY(), resultNormal.getY(), EPSILON);
        assertEquals(expectedNormal.getZ(), resultNormal.getZ(), EPSILON);
    }

    @Test
    public void sampleDivisionPlaneOffset_callsNextDoubleOnDistribution() {
        when(dist.nextDouble()).thenReturn(12.34);

        module = new PottsModuleFlyStemProliferation(stemCell);
        double offset = module.sampleDivisionPlaneOffset();

        assertEquals(12.34, offset, EPSILON);
    }

    @Test
    public void chooseDivisionPlane_WT_callsWTVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(dist.nextDouble()).thenReturn(12.0); // this can be any value

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane).when(module).getWTDivisionPlane(stemCell, 12.0);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getWTDivisionPlane(stemCell, 12.0);
        verify(module, never()).getMUDDivisionPlane(any());
    }

    @Test
    public void chooseDivisionPlane_MUDMUT_withLowOffset_callsWTVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(dist.nextDouble()).thenReturn(10.0); // abs(offset) < 75 → WT logic

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane).when(module).getWTDivisionPlane(stemCell, 10.0);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getWTDivisionPlane(stemCell, 10.0);
        verify(module, never()).getMUDDivisionPlane(any());
    }

    @Test
    public void chooseDivisionPlane_MUDMUT_withHighOffset_callsMUDVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(dist.nextDouble()).thenReturn(80.0); // abs(offset) > 75 → MUD logic

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane).when(module).getMUDDivisionPlane(stemCell);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getMUDDivisionPlane(stemCell);
        verify(module, never()).getWTDivisionPlane(any(), anyDouble());
    }

    // Step tests
    @Test
    public void step_volumeBelowCheckpoint_updatesTargetdoesNotDividePhaseStaysUndefined() {
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemLoc.getVolume()).thenReturn(50.0); // 50 < 1.2 * 100 → below checkpoint

        module = new PottsModuleFlyStemProliferation(stemCell);

        module.step(random, sim);

        verify(stemCell).updateTarget(eq(4.0), anyDouble());
        // Checking functions within addCell are never called
        // (checking addCell directly would require making module a mock)
        verify(sim, never()).getPotts();
        verify(grid, never()).addObject(any(), any());
        verify(potts, never()).register(any());
        assertEquals(Phase.UNDEFINED, module.phase);
    }

    @Test
    public void step_volumeAtCheckpoint_callsAddCellPhaseStaysUndefined() {
        // Trigger division
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getVolume()).thenReturn(120.0); // ≥ 1.2 * 100

        // Needed by calculateGMCDaughterCellCriticalVolume(...)
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);

        // Plane/voxel path (chooseDivisionPlane -> WT ->
        // getWTDivisionPlane)
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemLoc.getOffsetInApicalFrame(any(), any(Vector.class)))
                .thenReturn(new Voxel(1, 2, 3));

        // Differentiation rule

        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);

        // Cell creation path used by scheduleNewCell(...)
        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(anyInt(), eq(State.PROLIFERATIVE), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        // split(...) inside addCell
        when(stemLoc.split(eq(random), any(Plane.class))).thenReturn(daughterLoc);

        module = new PottsModuleFlyStemProliferation(stemCell);
        module.step(random, sim);

        verify(stemCell).updateTarget(eq(4.0), anyDouble());
        verify(stemLoc).split(eq(random), any(Plane.class)); // addCell ran
        verify(grid).addObject(any(), isNull()); // scheduled new cell
        verify(potts).register(any()); // registered new cell
        assertEquals(Phase.UNDEFINED, module.phase); // remains UNDEFINED
    }

    @Test
    public void step_incrementsProspero_prosperoIsUpdated() {
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(parameters.getDouble("proliferation/PROSPERO_RATE")).thenReturn(1.0);
        module = new PottsModuleFlyStemProliferation(stemCell);
        when(stemCell.getVolume()).thenReturn(0.0); // we don't want addCell to be called
        when(stemCell.getProspero()).thenReturn(5.0);
        module.step(random, sim);
        verify(stemCell).setProspero(6.0);
    }

    // Apical axis rule tests

    @Test
    public void getDaughterCellApicalAxis_global_returnsApicalAxis() {
        Vector expectedAxis = new Vector(1.0, 2.0, 3.0);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(expectedAxis);

        module = new PottsModuleFlyStemProliferation(stemCell);
        Vector result = module.getDaughterCellApicalAxis(random);

        assertEquals(expectedAxis.getX(), result.getX(), EPSILON);
        assertEquals(expectedAxis.getY(), result.getY(), EPSILON);
        assertEquals(expectedAxis.getZ(), result.getZ(), EPSILON);
    }

    @Test
    public void getDaughterCellApicalAxis_rotation_returnsRotatedAxis() {
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("normal");

        NormalDistribution rotDist = mock(NormalDistribution.class);
        when(rotDist.nextDouble()).thenReturn(30.0); // rotation angle
        when(parameters.getDistribution("proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION"))
                .thenReturn(rotDist);

        Vector originalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(originalAxis);

        module = new PottsModuleFlyStemProliferation(stemCell);
        Vector result = module.getDaughterCellApicalAxis(random);

        Vector expected = Vector.rotateVectorAroundAxis(originalAxis, new Vector(0, 0, 1), 30.0);
        assertEquals(expected.getX(), result.getX(), EPSILON);
        assertEquals(expected.getY(), result.getY(), EPSILON);
        assertEquals(expected.getZ(), result.getZ(), EPSILON);
    }

    @Test
    public void getDaughterCellApicalAxis_rotationwithInvalidDistribution_throwsException() {
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("rotation");
        when(parameters.getDistribution("proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION"))
                .thenReturn(mock(UniformDistribution.class));

        module = new PottsModuleFlyStemProliferation(stemCell);
        assertThrows(
                IllegalArgumentException.class, () -> module.getDaughterCellApicalAxis(random));
    }

    @Test
    public void getDaughterCellApicalAxis_normalRulesetWithUniformDistribution_throwsException() {
        // "rotation" hits the default case; this test exercises the instanceof guard inside
        // "normal"
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("normal");
        when(parameters.getDistribution("proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION"))
                .thenReturn(mock(UniformDistribution.class));

        module = new PottsModuleFlyStemProliferation(stemCell);
        assertThrows(
                IllegalArgumentException.class, () -> module.getDaughterCellApicalAxis(random));
    }

    @Test
    public void getDaughterCellApicalAxis_uniform_returnsRotatedAxis() {
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("uniform");

        UniformDistribution rotDist = mock(UniformDistribution.class);
        when(rotDist.nextDouble()).thenReturn(200.0); // rotation angle
        when(parameters.getDistribution("proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION"))
                .thenReturn(rotDist);

        Vector originalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(originalAxis);

        module = new PottsModuleFlyStemProliferation(stemCell);
        Vector result = module.getDaughterCellApicalAxis(random);

        Vector expected = Vector.rotateVectorAroundAxis(originalAxis, new Vector(0, 0, 1), 200.0);
        assertEquals(expected.getX(), result.getX(), EPSILON);
        assertEquals(expected.getY(), result.getY(), EPSILON);
        assertEquals(expected.getZ(), result.getZ(), EPSILON);
    }

    @Test
    public void getDaughterCellApicalAxis_uniformwithInvalidDistribution_throwsException() {
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("uniform");
        when(parameters.getDistribution("proliferation/APICAL_AXIS_ROTATION_DISTRIBUTION"))
                .thenReturn(mock(NormalDistribution.class));

        module = new PottsModuleFlyStemProliferation(stemCell);
        assertThrows(
                IllegalArgumentException.class, () -> module.getDaughterCellApicalAxis(random));
    }

    // Critical volume calculation tests

    @Test
    public void calculateGMCDaughterCellCriticalVolume_volumeBasedOff_returnsMaxCritVol() {
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        // GMC proportion = 1 - WT_DIVISION_SPLIT_OFFSET_PERCENT_Y/100 = 1 - 0.93 = 0.07

        module = new PottsModuleFlyStemProliferation(stemCell);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITVOL")).thenReturn(0);

        double result = module.calculateGMCDaughterCellCriticalVolume(daughterLoc);
        assertEquals((100 * .07 * 1.2), result, EPSILON); // 100 * 0.07 * 1.2
    }

    @Test
    public void addCell_nbDaughterVolumeBasedCritVol_aboveFloor_usesBirthVolume() {
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(daughterLoc.getVolume()).thenReturn(25.0);
        // floor = populationCriticalVolume * 0.20 = 100.0 * 0.20 = 20.0
        // expected = max(25.0, 20.0) = 25.0

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(stemCellPop), eq(25.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        Plane dummyPlane = mock(Plane.class);
        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane).when(module).chooseDivisionPlane(stemCell);
        doReturn(true).when(module).daughterStem(any(), any(), any());

        module.addCell(random, sim);

        verify(stemCell)
                .make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(stemCellPop), eq(25.0));
    }

    @Test
    public void addCell_nbDaughterVolumeBasedCritVol_belowFloor_usesPopCritVolFloor() {
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(daughterLoc.getVolume()).thenReturn(5.0);
        // floor = populationCriticalVolume * 0.20 = 100.0 * 0.20 = 20.0
        // expected = max(5.0, 20.0) = 20.0 — the floor, not the birth volume

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(stemCellPop), eq(20.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        Plane dummyPlane = mock(Plane.class);
        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane).when(module).chooseDivisionPlane(stemCell);
        doReturn(true).when(module).daughterStem(any(), any(), any());

        module.addCell(random, sim);

        verify(stemCell)
                .make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(stemCellPop), eq(20.0));
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_withGMCOverride_returnsOverrideValue() {
        // GMC_CRITICAL_VOLUME_OVERRIDE=200 and VCV=0 → returns 200 regardless of the formula,
        // which would otherwise give 100 * 1.2 * 0.07 = 8.4
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(0);
        when(parameters.getDouble("proliferation/GMC_CRITICAL_VOLUME_OVERRIDE")).thenReturn(200.0);

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(200.0, module.calculateGMCDaughterCellCriticalVolume(daughterLoc), EPSILON);
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_withGMCOverrideAndVCVOn_ignoresOverride() {
        // GMC_CRITICAL_VOLUME_OVERRIDE=200 but VCV=1 → override ignored, birth volume used
        PottsLocation gmcLoc = mock(PottsLocation.class);
        when(gmcLoc.getVolume()).thenReturn(50.0);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(parameters.getDouble("proliferation/GMC_CRITICAL_VOLUME_OVERRIDE")).thenReturn(200.0);

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals(50.0, module.calculateGMCDaughterCellCriticalVolume(gmcLoc), EPSILON);
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_volumeBasedOn_returnsLocVolume() {
        PottsLocation gmcLoc = mock(PottsLocation.class);
        when(gmcLoc.getVolume()).thenReturn(50.0);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(parameters.getDouble("proliferation/VOLUME_BASED_CRITICAL_VOLUME_MULTIPLIER"))
                .thenReturn(1.5);

        module = new PottsModuleFlyStemProliferation(stemCell);

        double result = module.calculateGMCDaughterCellCriticalVolume(gmcLoc);
        assertEquals(50.0, result, EPSILON);
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_volumeBasedOnVerySmallVolume_returnsFloor() {
        // @BeforeEach: stemLoc.getVolume()=10.0, so initialSize=10.0 → floor = 10.0 * 0.1 = 1.0
        // gmcLoc.getVolume()=0.5 < 1.0, so Math.max picks the floor
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);

        module = new PottsModuleFlyStemProliferation(stemCell);

        PottsLocation gmcLoc = mock(PottsLocation.class);
        when(gmcLoc.getVolume()).thenReturn(0.5);

        double result = module.calculateGMCDaughterCellCriticalVolume(gmcLoc);
        assertEquals(1.0, result, EPSILON); // initialSize * 0.1 = 10.0 * 0.1
    }

    // addCell integration tests

    @Test
    public void addCell_WTVolumeSwap_swapsVoxelsAndCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.0);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(0);

        // parent smaller than daughter -> rule-based 'volume' says parent is GMC ->
        // triggers swap
        when(stemLoc.getVolume()).thenReturn(5.0);
        when(daughterLoc.getVolume()).thenReturn(10.0);

        Plane dummyPlane = mock(Plane.class);
        when(dummyPlane.getUnitNormalVector()).thenReturn(new Vector(1, 0, 0));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newStemCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newStemCell);

        PottsModuleFlyStemProliferation spyModule =
                spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(0.0).when(spyModule).sampleDivisionPlaneOffset();
        doReturn(dummyPlane).when(spyModule).getWTDivisionPlane(eq(stemCell), anyDouble());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            spyModule.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }

        verify(newStemCell).schedule(any());
    }

    @Test
    public void addCell_WTVolumeNoSwap_doesNotSwapVoxelsAndCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.0);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(0);

        // Set up the condition that parent volume > daughter volume → no swap
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);

        Voxel v1 = new Voxel(0, 0, 0);
        Voxel v2 = new Voxel(1, 0, 0);
        when(daughterLoc.getVoxels()).thenReturn(new ArrayList<>(List.of(v1)));
        when(stemLoc.getVoxels()).thenReturn(new ArrayList<>(List.of(v2)));

        // Stub division plane
        Plane dummyPlane = mock(Plane.class);
        when(dummyPlane.getUnitNormalVector()).thenReturn(new Vector(1, 0, 0));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        // Stub cell creation. A WT division always yields a GMC daughter, so the daughter's
        // population comes from getLinks(), not the parent's population.
        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newDaughterCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random)))
                .thenReturn(newDaughterCell);

        // Spy and override division plane logic
        PottsModuleFlyStemProliferation spyModule =
                spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane).when(spyModule).getWTDivisionPlane(eq(stemCell), anyDouble());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            spyModule.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(any(), any()), never());
        }
        verify(newDaughterCell).schedule(any());
    }

    @Test
    public void addCell_MUDMUTOffsetAboveThreshold_createsStemCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.nextDouble()).thenReturn(80.0); // triggers MUD plane (abs(offset) > 75)

        sim = mock(PottsSimulation.class);
        potts = mock(Potts.class);
        factory = mock(PottsCellFactory.class);
        grid = mock(Grid.class);
        when(sim.getPotts()).thenReturn(potts);
        when(sim.getGrid()).thenReturn(grid);
        when(sim.getCellFactory()).thenReturn(factory);
        when(sim.getSchedule()).thenReturn(mock(sim.engine.Schedule.class));
        when(sim.getID()).thenReturn(42);
        potts.ids = new int[1][1][1];
        potts.regions = new int[1][1][1];

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(stemCellPop), eq(100.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getPop()).thenReturn(stemCellPop);

        PottsModuleFlyStemProliferation spyModule =
                spy(new PottsModuleFlyStemProliferation(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(spyModule).getMUDDivisionPlane(eq(stemCell));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(true).when(spyModule).daughterStem(any(), any(), any());

        spyModule.addCell(random, sim);

        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_MUDMUTOffsetBelowThreshold_createsGMCWithVolumeSwap() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.nextDouble()).thenReturn(10.0); // below 75 threshold

        when(stemLoc.getVolume()).thenReturn(5.0);
        when(daughterLoc.getVolume()).thenReturn(10.0); // triggers swap

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getPop()).thenReturn(stemCellPop);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getWTDivisionPlane(eq(stemCell), anyDouble());
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(false).when(module).daughterStem(any(), any(), any());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            mocked.when(
                            () ->
                                    PottsLocation.getDirectionalVoxelSubset(
                                            any(), anyDouble(), any(), any(), any()))
                    .thenReturn(new Bag());

            module.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }

        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_reorientsApicalAxisOnceBeforeChoosingDivisionPlane() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.0);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(0);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);

        Plane dummyPlane = mock(Plane.class);
        when(dummyPlane.getUnitNormalVector()).thenReturn(new Vector(0, 1, 0));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(anyInt(), any(), eq(random), anyInt(), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane).when(module).chooseDivisionPlane(stemCell);

        module.addCell(random, sim);

        InOrder order = inOrder(module);
        order.verify(module).reorientApicalAxis(stemCell);
        order.verify(module).chooseDivisionPlane(stemCell);
        verify(module, times(1)).reorientApicalAxis(stemCell);
    }

    @Test
    public void getNBNeighbors_withTwoUniqueStemNeighbors_returnsCorrectSet() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // Stem voxels (two positions)
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        // Unique IDs returned by Potts per voxel
        HashSet<Integer> idsVoxel1 = new HashSet<>(Arrays.asList(10, 11));
        HashSet<Integer> idsVoxel2 = new HashSet<>(Arrays.asList(11, 12)); // 11 repeats
        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(idsVoxel1);
        when(potts.getUniqueIDs(1, 0, 0)).thenReturn(idsVoxel2);

        // Neighbors
        PottsCellFlyStem nb10 = mock(PottsCellFlyStem.class);
        PottsCellFlyStem nb11 = mock(PottsCellFlyStem.class);
        PottsCell nb12OtherPop = mock(PottsCell.class);

        when(nb10.getID()).thenReturn(10);
        when(nb11.getID()).thenReturn(11);
        when(nb12OtherPop.getID()).thenReturn(12);

        // Stem pop matches 3
        when(stemCell.getPop()).thenReturn(stemCellPop);
        when(nb10.getPop()).thenReturn(stemCellPop);
        when(nb11.getPop()).thenReturn(stemCellPop);
        when(nb12OtherPop.getPop()).thenReturn(99); // no match

        when(grid.getObjectAt(10)).thenReturn(nb10);
        when(grid.getObjectAt(11)).thenReturn(nb11);
        when(grid.getObjectAt(12)).thenReturn(nb12OtherPop);

        when(stemCell.getID()).thenReturn(42);

        HashSet<PottsCellFlyStem> neighbors = module.getNBNeighbors(sim);

        assertEquals(2, neighbors.size(), "Should contain 2 unique matching neighbors (10 and 11)");
        assertTrue(neighbors.contains(nb10));
        assertTrue(neighbors.contains(nb11));
    }

    @Test
    public void getNBNeighbors_noMatchingNeighbors_returnsEmptySet() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        HashSet<Integer> ids = new HashSet<>(Arrays.asList(50));
        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(ids);

        PottsCell nonStemNeighbor = mock(PottsCell.class);
        when(nonStemNeighbor.getPop()).thenReturn(99); // not stem pop
        when(nonStemNeighbor.getID()).thenReturn(50);
        when(grid.getObjectAt(50)).thenReturn(nonStemNeighbor);

        when(stemCell.getPop()).thenReturn(3);
        when(stemCell.getID()).thenReturn(42);

        HashSet<PottsCellFlyStem> neighbors = module.getNBNeighbors(sim);

        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty(), "No neighbors should be returned when pops do not match.");
    }

    @Test
    public void getNBNeighbors_doesNotIncludeSelf() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        // Potts returns this cell's own ID
        when(stemCell.getID()).thenReturn(42);
        when(stemCell.getPop()).thenReturn(3);

        HashSet<Integer> ids = new HashSet<>(Arrays.asList(42));
        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(ids);

        when(grid.getObjectAt(42)).thenReturn(stemCell);

        HashSet<PottsCellFlyStem> neighbors = module.getNBNeighbors(sim);
        assertTrue(neighbors.isEmpty(), "Self should not be included as a neighbor");
    }

    @Test
    public void getNBNeighbors_nullNeighborInGrid_skipsNullAndContinues() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        // Two IDs: one resolves to null, the other to a valid matching neighbor
        HashSet<Integer> ids = new HashSet<>(Arrays.asList(7, 8));
        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(ids);

        when(grid.getObjectAt(7)).thenReturn(null);

        PottsCellFlyStem validNeighbor = mock(PottsCellFlyStem.class);
        when(validNeighbor.getID()).thenReturn(8);
        when(validNeighbor.getPop()).thenReturn(stemCellPop);
        when(grid.getObjectAt(8)).thenReturn(validNeighbor);

        when(stemCell.getPop()).thenReturn(stemCellPop);
        when(stemCell.getID()).thenReturn(42);

        HashSet<PottsCellFlyStem> neighbors = module.getNBNeighbors(sim);

        assertEquals(
                1, neighbors.size(), "Null grid entry should be skipped; valid neighbor returned");
        assertTrue(neighbors.contains(validNeighbor));
    }

    // computeEquilibriumVolume tests

    @Test
    public void computeEquilibriumVolume_WT_returnsExpectedMidpoint() {
        // V_div = sizeTarget * critVol = 1.2 * 100 = 120
        // fRetain = WT_DIVISION_SPLIT_OFFSET_PERCENT_Y / 100 = 93 / 100 = 0.93
        // V_ref = 120 * (0.93 + 1) / 2 = 120 * 0.965 = 115.8
        module = new PottsModuleFlyStemProliferation(stemCell);
        assertEquals(115.8, module.computeEquilibriumVolume(), EPSILON);
    }

    @Test
    public void computeEquilibriumVolume_MUDMUT_usesWTFRetain() {
        // fRetain comes from WT_DIVISION_SPLIT_OFFSET_PERCENT_Y (0.93) regardless of cell type,
        // since MUDMUT cells also divide by WT rules within MUDMUT_WT_DIVISION_ANGLE_THRESHOLD.
        // V_ref = 120 * (0.93 + 1) / 2 = 115.8
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        module = new PottsModuleFlyStemProliferation(stemCell);
        assertEquals(115.8, module.computeEquilibriumVolume(), EPSILON);
    }

    @Test
    public void computeEquilibriumVolume_differentSizeTarget_scalesCorrectly() {
        // V_div = 2.0 * 50 = 100; V_ref = 100 * (0.93 + 1) / 2 = 96.5
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(2.0);
        when(parameters.getDouble("CRITICAL_VOLUME")).thenReturn(50.0);
        module = new PottsModuleFlyStemProliferation(stemCell);
        assertEquals(96.5, module.computeEquilibriumVolume(), EPSILON);
    }

    @Test
    public void computeEquilibriumVolume_called_usesPopulationValue() {
        // The cell's own critVol (200) differs from the population CRITICAL_VOLUME (100).
        // V_ref must follow the population value: 1.2 * 100 * (0.93 + 1) / 2 = 115.8,
        // not the per-cell value, which would give 231.6.
        when(parameters.getDouble("CRITICAL_VOLUME")).thenReturn(100.0);
        when(stemCell.getCriticalVolume()).thenReturn(200.0);
        module = new PottsModuleFlyStemProliferation(stemCell);
        assertEquals(115.8, module.computeEquilibriumVolume(), EPSILON);
    }

    @Test
    public void computeEquilibriumVolume_differentPopulationCritVol_scalesCorrectly() {
        // V_div = 1.0 * 200 = 200; V_ref = 200 * (0.93 + 1) / 2 = 193.0
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.0);
        when(parameters.getDouble("CRITICAL_VOLUME")).thenReturn(200.0);
        module = new PottsModuleFlyStemProliferation(stemCell);
        assertEquals(193.0, module.computeEquilibriumVolume(), EPSILON);
    }

    // updateGrowthRate dispatch tests

    @Test
    public void updateGrowthRate_volumeDynamic_callsVolumeBasedMethod() {
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(1);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doNothing().when(module).updateVolumeBasedGrowthRate(any());

        module.updateGrowthRate(sim);

        verify(module, times(1)).updateVolumeBasedGrowthRate(sim);
        verify(module, never()).updateGrowthRateBasedOnOtherNBs(any());
    }

    @Test
    public void updateGrowthRate_nbSelfRepression_callsNBMethod() {
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_SELF_REPRESSION"))
                .thenReturn(1);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doNothing().when(module).updateGrowthRateBasedOnOtherNBs(any());

        module.updateGrowthRate(sim);

        verify(module, times(1)).updateGrowthRateBasedOnOtherNBs(sim);
        verify(module, never()).updateVolumeBasedGrowthRate(any());
    }

    @Test
    public void updateGrowthRate_noFlags_setsCellGrowthRateToBase() {
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(7.5);

        module = new PottsModuleFlyStemProliferation(stemCell);
        module.updateGrowthRate(sim);

        assertEquals(module.cellGrowthRateBase, module.cellGrowthRate, EPSILON);
        assertEquals(7.5, module.cellGrowthRate, EPSILON);
    }

    @Test
    public void updateVolumeBasedGrowthRate_called_usesCellVolumeAndEquilibriumRef() {
        when(stemLoc.getVolume()).thenReturn(42.5);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // NB mocks
        PottsCellFlyStem nbA = mock(PottsCellFlyStem.class);
        PottsCellFlyStem nbB = mock(PottsCellFlyStem.class);
        PottsCellFlyStem nbC = mock(PottsCellFlyStem.class);

        // Location mocks for each NB
        PottsLocation locA = mock(PottsLocation.class);
        PottsLocation locB = mock(PottsLocation.class);
        PottsLocation locC = mock(PottsLocation.class);

        when(nbA.getLocation()).thenReturn(locA);
        when(nbB.getLocation()).thenReturn(locB);
        when(nbC.getLocation()).thenReturn(locC);

        // Volumes: 10, 20, 40 -> avg = 70/3
        when(locA.getVolume()).thenReturn(10.0);
        when(locB.getVolume()).thenReturn(20.0);
        when(locC.getVolume()).thenReturn(40.0);

        // Critical volumes: 90, 110, 100 -> avg = 300/3 = 100
        when(nbA.getCriticalVolume()).thenReturn(90.0);
        when(nbB.getCriticalVolume()).thenReturn(110.0);
        when(nbC.getCriticalVolume()).thenReturn(100.0);

        HashSet<PottsCellFlyStem> allNBs = new HashSet<>(Arrays.asList(nbA, nbB, nbC));

        doReturn(allNBs).when(module).getNBsInSimulation(sim);
        doNothing().when(module).updateCellVolumeBasedGrowthRate(anyDouble(), anyDouble());

        module.updateVolumeBasedGrowthRate(sim);

        // V_ref = sizeTarget * critVol * (WT_DIVISION_SPLIT_OFFSET_PERCENT_Y/100 + 1) / 2
        //       = 1.2 * 100 * (0.93 + 1) / 2 = 115.8
        // Computed with the same operation order as the implementation, since eq() on a double is
        // an exact match and 120.0 * 1.93 / 2.0 is 115.80000000000001.
        double expectedVRef = 1.2 * 100.0 * (0.93 + 1.0) / 2.0;
        verify(module, times(1)).updateCellVolumeBasedGrowthRate(eq(42.5), eq(expectedVRef));
    }

    @Test
    public void updateGrowthRateBasedOnOtherNBs_pdeLikeFalse_usesNeighborsBranch() {
        // pdeLike = 0 → neighbors branch
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(1);

        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(4.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(12.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // N = 4 neighbors (K = 4, n = 2 → repression 0.5 → 12 * 0.5 = 6)
        HashSet<PottsCellFlyStem> four = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            PottsCellFlyStem n = mock(PottsCellFlyStem.class);
            when(n.getID()).thenReturn(100 + i);
            four.add(n);
        }
        doReturn(four).when(module).getNBNeighbors(sim);
        // Make sure population path is not used
        doReturn(new HashSet<PottsCellFlyStem>()).when(module).getNBsInSimulation(sim);

        module.updateGrowthRateBasedOnOtherNBs(sim);

        assertEquals(6.0, module.cellGrowthRate, 1e-6);
        verify(module, times(1)).getNBNeighbors(sim);
        verify(module, never()).getNBsInSimulation(sim);
    }

    @Test
    public void updateGrowthRateBasedOnOtherNBs_pdeLikeTrue_usesPopulationBranch() {
        // pdeLike = 1 and dynamicGrowthRateNBContact = 0 to avoid constructor exception
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(1);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(0);

        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(3.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(20.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // N = 6 in-simulation (K = 3, n = 2 → 9/(9+36)=0.2 → 4.0)
        HashSet<PottsCellFlyStem> six = new HashSet<>();
        for (int i = 0; i <= 6; i++) {
            PottsCellFlyStem n = mock(PottsCellFlyStem.class);
            when(n.getID()).thenReturn(200 + i);
            six.add(n);
        }
        doReturn(new HashSet<PottsCellFlyStem>()).when(module).getNBNeighbors(sim);
        doReturn(six).when(module).getNBsInSimulation(sim);

        module.updateGrowthRateBasedOnOtherNBs(sim);

        assertEquals(4.0, module.cellGrowthRate, 1e-6);
        verify(module, times(1)).getNBsInSimulation(sim);
        verify(module, never()).getNBNeighbors(sim);
    }

    @Test
    public void updateGrowthRateBasedOnOtherNBs_KZeroandZeroNeighbors_returnsBase() {

        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(0.0); // K = 0
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(10.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // N = 0 → with your guard, repression = 1.0 when K=0 & N=0
        doReturn(new HashSet<PottsCellFlyStem>()).when(module).getNBNeighbors(sim);

        module.updateGrowthRateBasedOnOtherNBs(sim);

        assertEquals(10.0, module.cellGrowthRate, 1e-6);
    }

    @Test
    public void updateGrowthRateBasedOnOtherNBs_KZeroandPositiveNeighbors_returnsZero() {

        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(0.0); // K = 0
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(10.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // N > 0 → with your guard, repression = 0.0 when K=0 & N>0
        HashSet<PottsCellFlyStem> one = new HashSet<>();
        PottsCellFlyStem n = mock(PottsCellFlyStem.class);
        when(n.getID()).thenReturn(999);
        one.add(n);
        doReturn(one).when(module).getNBNeighbors(sim);

        module.updateGrowthRateBasedOnOtherNBs(sim);

        assertEquals(0.0, module.cellGrowthRate, 1e-9);
    }

    @Test
    public void updateGrowthRateBasedOnOtherNBs_hillExponentOne_linearCase() {

        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(4.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(1.0); // linear
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(10.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // N = 2 → R = K/(K+N) = 4/(4+2) = 2/3
        HashSet<PottsCellFlyStem> two = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            PottsCellFlyStem nn = mock(PottsCellFlyStem.class);
            when(nn.getID()).thenReturn(300 + i);
            two.add(nn);
        }
        doReturn(two).when(module).getNBNeighbors(sim);

        module.updateGrowthRateBasedOnOtherNBs(sim);

        assertEquals(10.0 * (2.0 / 3.0), module.cellGrowthRate, 1e-6);
    }

    @Test
    public void updateGrowthRateBasedOnOtherNBs_largeNeighbors_approachesZero() {

        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(5.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(3.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(7.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // N = 100 >> K = 5 → repression ~ 0
        HashSet<PottsCellFlyStem> many = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            PottsCellFlyStem nn = mock(PottsCellFlyStem.class);
            when(nn.getID()).thenReturn(400 + i);
            many.add(nn);
        }
        doReturn(many).when(module).getNBNeighbors(sim);

        module.updateGrowthRateBasedOnOtherNBs(sim);

        assertTrue(module.cellGrowthRate < 0.01, "Growth should be ~0 with very large N.");
    }

    @Test
    public void daughterStem_deterministicWT_returnsFalse() {
        // A WT division always yields a GMC daughter, even when the division plane normal happens
        // to match the expected MUD normal exactly — which is the case constructed here.
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));

        Plane plane = mock(Plane.class);
        when(plane.getUnitNormalVector()).thenReturn(new Vector(1.0, 0, 0));

        module = new PottsModuleFlyStemProliferation(stemCell);

        assertFalse(
                module.daughterStem(stemLoc, daughterLoc, plane),
                "Expected WT deterministic differentiation to always return false.");
    }

    @Test
    public void daughterStem_deterministicTruematchingNormalVector_returnsTrue() {
        // hasDeterministicDifferentiation=true is already set in @BeforeEach
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        // Compute the MUDMUT expected normal the same way daughterStemDeterministic does:
        // rotateVectorAroundAxis(apicalAxis, XY_PLANE, MUDMUT.splitDirectionRotation=-90) → (1,0,0)
        Vector expectedNormal =
                Vector.rotateVectorAroundAxis(
                        apicalAxis,
                        new Vector(0, 0, 1), // Direction.XY_PLANE.vector
                        PottsCellFlyStem.StemType.MUDMUT.splitDirectionRotation);

        Plane plane = mock(Plane.class);
        when(plane.getUnitNormalVector()).thenReturn(expectedNormal);

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc, plane);

        assertTrue(
                result, "Expected true when division plane normal matches MUDMUT expected normal");
    }

    @Test
    public void daughterStem_deterministicTrueNonMatchingNormalVector_returnsFalse() {
        // hasDeterministicDifferentiation=true is already set in @BeforeEach
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        // Use a normal that does NOT match the MUDMUT expected normal (1,0,0)
        Plane plane = mock(Plane.class);
        when(plane.getUnitNormalVector()).thenReturn(new Vector(0, 1, 0));

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc, plane);

        assertFalse(
                result,
                "Expected false when division plane normal does not match MUDMUT expected normal");
    }

    @Test
    void daughterStem_smallerGmc_decisionIsScaleInvariant() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT"))
                .thenReturn(71.5);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        module = new PottsModuleFlyStemProliferation(stemCell);

        // The same split ratio at three very different cell sizes must decide the same way.
        // 70/30 is more even than 71.5 (symmetric); 75/25 is less even (asymmetric). With an
        // absolute tolerance the 1236-voxel cell would decide differently from the 248-voxel one,
        // which is exactly why no single value could serve noreg, vol_abm and WT at once.
        for (double total : new double[] {248.0, 424.0, 1236.0}) {
            when(stemLoc.getVolume()).thenReturn(0.70 * total);
            when(daughterLoc.getVolume()).thenReturn(0.30 * total);
            assertTrue(
                    module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)),
                    "70/30 split must be symmetric at total=" + total);

            when(stemLoc.getVolume()).thenReturn(0.75 * total);
            when(daughterLoc.getVolume()).thenReturn(0.25 * total);
            assertFalse(
                    module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)),
                    "75/25 split must be asymmetric at total=" + total);
        }
    }

    @Test
    void daughterStem_volumeRuleBased_true() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT"))
                .thenReturn(75.0); // 10/(10+5) = 66.7% split, below 75 -> symmetric

        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        module = new PottsModuleFlyStemProliferation(stemCell);

        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class));

        assertTrue(result, "Expected true since the 66.7/33.3 split is more even than 75");
    }

    @Test
    void daughterStem_volumeRuleBased_false() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT"))
                .thenReturn(60.0); // 10/(10+5) = 66.7% split, above 60 -> asymmetric

        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        module = new PottsModuleFlyStemProliferation(stemCell);

        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class));

        assertFalse(result, "Expected false since the 66.7/33.3 split is less even than 60");
    }

    @Test
    public void daughterStem_ruleBasedWT_usesSameGeometricRuleAsMudmut() {
        // The WT early-return was removed in 953d9f01 so that WT uses the same geometric ruleset
        // as MUDMUT; whether WT can divide symmetrically is controlled by
        // HAS_DETERMINISTIC_DIFFERENTIATION, not by a hardcoded genotype check. This test
        // previously asserted "WT always returns false" and passed only because the default mock
        // volumes happened to sit outside the tolerance.
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT"))
                .thenReturn(71.5);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        module = new PottsModuleFlyStemProliferation(stemCell);

        when(stemLoc.getVolume()).thenReturn(70.0);
        when(daughterLoc.getVolume()).thenReturn(30.0); // 70/30 split, more even than 71.5
        assertTrue(
                module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)),
                "a symmetric enough WT division yields two neuroblasts");

        when(stemLoc.getVolume()).thenReturn(93.0);
        when(daughterLoc.getVolume()).thenReturn(7.0); // 93/7 split, less even than 71.5
        assertFalse(
                module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)),
                "the calibrated 93/7 WT split yields a GMC");
    }

    @Test
    public void daughterStem_ruleBasedMUDMUTApicalAxis_withinRange_returnsTrue() {
        // @BeforeEach sets: stemLoc centroid=(0,1.0,0), daughterLoc centroid=(0,1.6,0)
        // With apical axis (0,1,0), distance along axis = |1.6 - 1.0| = 0.6
        // range=1.0 > 0.6 → within range → true
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET"))
                .thenReturn("apical_axis");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(1.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class));

        assertTrue(
                result,
                "Expected true: centroid distance 0.6 is within range 1.0 along apical axis");
    }

    @Test
    public void daughterStem_ruleBasedMUDMUTApicalAxis_outsideRange_returnsFalse() {
        // @BeforeEach sets: stemLoc centroid=(0,1.0,0), daughterLoc centroid=(0,1.6,0)
        // With apical axis (0,1,0), distance along axis = |1.6 - 1.0| = 0.6
        // range=0.5 < 0.6 → outside range → false
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET"))
                .thenReturn("apical_axis");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class));

        assertFalse(
                result,
                "Expected false: centroid distance 0.6 exceeds range 0.5 along apical axis");
    }

    @Test
    public void daughterStem_ruleBasedMUDMUTInvalidSymmetricRuleset_throwsException() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET"))
                .thenReturn("invalid");
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        module = new PottsModuleFlyStemProliferation(stemCell);

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
        assertTrue(e.getMessage().contains("symmetric division ruleset"));
    }

    @Test
    public void constructor_readsSymmetricDivisionRuleset() {
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET"))
                .thenReturn("apical_axis");
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertEquals("apical_axis", module.symmetricDivisionRuleset);
    }

    @Test
    public void daughterStem_sizeRulesetWithBasalGmc_usesVolumesNotCentroids() {
        // Default centroids are 0.6 apart and range 0.5, so the apical_axis test would say
        // "asymmetric" for both splits below. The size test must decide instead.
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("basal_gmc");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_OFFSET_PERCENT"))
                .thenReturn(71.5);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        module = new PottsModuleFlyStemProliferation(stemCell);

        when(stemLoc.getVolume()).thenReturn(70.0);
        when(daughterLoc.getVolume()).thenReturn(30.0); // 70% < 71.5% → symmetric
        assertTrue(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));

        when(stemLoc.getVolume()).thenReturn(93.0);
        when(daughterLoc.getVolume()).thenReturn(7.0); // 93% >= 71.5% → asymmetric
        assertFalse(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
    }

    @Test
    public void daughterStem_apicalAxisRulesetWithSmallerGmc_usesCentroidsNotVolumes() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET"))
                .thenReturn("smaller_gmc");
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET"))
                .thenReturn("apical_axis");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(1.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemLoc.getVolume()).thenReturn(93.0);
        when(daughterLoc.getVolume()).thenReturn(7.0); // size test would say asymmetric
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertTrue(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
    }

    @Test
    public void daughterStem_emergentWithRandomIdentity_noLongerThrows() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("random");
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(stemLoc.getVolume()).thenReturn(93.0);
        when(daughterLoc.getVolume()).thenReturn(7.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertFalse(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
    }

    @Test
    public void daughterStem_imposedWT_ignoresSymmetricDivisionRuleset() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("TRUE");
        when(parameters.getString("proliferation/SYMMETRIC_DIVISION_RULESET"))
                .thenReturn("invalid"); // must never be read on the imposed path
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemLoc.getVolume()).thenReturn(50.0);
        when(daughterLoc.getVolume()).thenReturn(45.0);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertFalse(module.daughterStem(stemLoc, daughterLoc, mock(Plane.class)));
    }
}
