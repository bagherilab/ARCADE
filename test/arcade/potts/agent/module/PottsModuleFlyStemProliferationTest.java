package arcade.potts.agent.module;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
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
import arcade.potts.util.PottsEnums.Phase;
import arcade.potts.util.PottsEnums.State;
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

    float EPSILON = 1e-6f;

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
        when(daughterLoc.getVolume()).thenReturn(5.0);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.6, 0});

        // Parameter stubs (sometimes overridden in tests)
        when(parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION"))
                .thenReturn(dist);
        when(dist.nextDouble()).thenReturn(0.1);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("TRUE");

        // Link selection
        GrabBag links = mock(GrabBag.class);
        when(stemCell.getLinks()).thenReturn(links);
        when(links.next(random)).thenReturn(2);

        // Other defaults
        stemCellPop = 3;
        when(stemCell.getPop()).thenReturn(stemCellPop);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
    }

    @AfterEach
    final void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    // Constructor tests

    @Test
    public void constructor_volumeRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.42);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertNotNull(module.splitDirectionDistribution);
        assertEquals("volume", module.differentiationRuleset);
        assertEquals(0.42, module.range, EPSILON);
        assertEquals(arcade.potts.util.PottsEnums.Phase.UNDEFINED, module.phase);
    }

    @Test
    public void constructor_locationRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.99);
        module = new PottsModuleFlyStemProliferation(stemCell);

        assertNotNull(module.splitDirectionDistribution);
        assertEquals("location", module.differentiationRuleset);
        assertEquals(0.99, module.range, EPSILON);
        assertEquals(arcade.potts.util.PottsEnums.Phase.UNDEFINED, module.phase);
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
        expectedOffset.add(75); // WT.splitOffsetPercentY

        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemLoc.getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class)))
                .thenReturn(new Voxel(0, 0, 0));

        PottsModuleFlyStemProliferation.getCellSplitVoxel(
                PottsCellFlyStem.StemType.WT, stemCell, stemCell.getApicalAxis());
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
                PottsCellFlyStem.StemType.MUDMUT, stemCell, stemCell.getApicalAxis());
        verify(stemLoc).getOffsetInApicalFrame(eq(expectedOffset), any(Vector.class));
    }

    // Division plane tests

    @Test
    public void getWTDivisionPlaneWithRotationalVariance_rotatesCorrectlyAndReturnsPlane() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        double baseRotation = PottsCellFlyStem.StemType.WT.splitDirectionRotation; // 90
        double offsetRotation = -5.0;

        Voxel splitVoxel = new Voxel(3, 4, 5);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT x offset percent
        expectedOffset.add(80); // WT y offset percent

        module = new PottsModuleFlyStemProliferation(stemCell);

        // Apply both rotations manually to get expected result
        Vector afterBaseRotation =
                Vector.rotateVectorAroundAxis(apicalAxis, new Vector(0, 0, 1), baseRotation);
        Vector expectedNormal =
                Vector.rotateVectorAroundAxis(
                        afterBaseRotation, new Vector(0, 0, 1), offsetRotation);

        when(stemLoc.getOffsetInApicalFrame(any(), eq(expectedNormal))).thenReturn(splitVoxel);

        Plane result = module.getWTDivisionPlaneWithRotationalVariance(stemCell, offsetRotation);

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
    public void getMUDDivisionPlane_returnsRotatedPlaneWithCorrectNormal() {
        Vector apicalAxis = new Vector(0, 1, 0);
        when(stemCell.getApicalAxis()).thenReturn(apicalAxis);

        Vector expectedNormal = new Vector(1.0, 0.0, 0.0);

        Voxel splitVoxel = new Voxel(7, 8, 9);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT x offset percent
        expectedOffset.add(50); // MUDMUT y offset percent
        when(stemLoc.getOffsetInApicalFrame(any(), any())).thenReturn(splitVoxel);

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
        doReturn(expectedPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(stemCell, 12.0);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getWTDivisionPlaneWithRotationalVariance(stemCell, 12.0);
        verify(module, never()).getMUDDivisionPlane(any());
    }

    @Test
    public void chooseDivisionPlane_MUDMUT_withLowOffset_callsWTVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(dist.nextDouble()).thenReturn(10.0); // abs(offset) < 45 → WT logic

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(stemCell, 10.0);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getWTDivisionPlaneWithRotationalVariance(stemCell, 10.0);
        verify(module, never()).getMUDDivisionPlane(any());
    }

    @Test
    public void chooseDivisionPlane_MUDMUT_withHighOffset_callsMUDVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(dist.nextDouble()).thenReturn(60.0); // abs(offset) ≥ 45 → MUD logic

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane).when(module).getMUDDivisionPlane(stemCell);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getMUDDivisionPlane(stemCell);
        verify(module, never()).getWTDivisionPlaneWithRotationalVariance(any(), anyDouble());
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
        // getWTDivisionPlaneWithRotationalVariance)
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(stemLoc.getOffsetInApicalFrame(any(), any(Vector.class)))
                .thenReturn(new Voxel(1, 2, 3));

        // Differentiation rule
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
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
        // WT has proportion = 0.2

        module = new PottsModuleFlyStemProliferation(stemCell);
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITVOL")).thenReturn(0);

        double result = module.calculateGMCDaughterCellCriticalVolume(daughterLoc);
        assertEquals((100 * .25 * 1.2), result, EPSILON); // 100 * 0.25 * 1.2
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_volumeBasedOn_returnsScaledValue() {
        PottsLocation gmcLoc = mock(PottsLocation.class);
        when(gmcLoc.getVolume()).thenReturn(50.0);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);

        MiniBox popParametersMiniBox = mock(MiniBox.class);
        when(popParametersMiniBox.getDouble("proliferation/SIZE_TARGET")).thenReturn(2.0);

        when(sim.getCellFactory()).thenReturn(factory);
        when(factory.getParameters(stemCellPop)).thenReturn(popParametersMiniBox);

        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(1);
        when(parameters.getDouble("proliferation/VOLUME_BASED_CRITICAL_VOLUME_MULTIPLIER"))
                .thenReturn(1.5);

        module = new PottsModuleFlyStemProliferation(stemCell);

        double result = module.calculateGMCDaughterCellCriticalVolume(gmcLoc);
        assertEquals(75.0, result, EPSILON); // 50 * 1.5
    }

    // addCell integration tests

    @Test
    public void addCell_WTVolumeSwap_swapsVoxelsAndCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE"); // ⬅️ force rule-based path
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
                .thenReturn(container); // ⬅️ relax CV match
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newStemCell);

        PottsModuleFlyStemProliferation module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(0.0).when(module).sampleDivisionPlaneOffset();
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
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

        // Stub division plane
        Plane dummyPlane = mock(Plane.class);
        when(dummyPlane.getUnitNormalVector()).thenReturn(new Vector(1, 0, 0));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        // Stub cell creation
        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newStemCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(
                        eq(42), eq(State.PROLIFERATIVE), eq(random), eq(stemCellPop), anyDouble()))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newStemCell);

        // Spy and override division plane logic
        PottsModuleFlyStemProliferation module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(any(), any()), never());
        }
        verify(newStemCell).schedule(any());
    }

    @Test
    public void addCell_MUDMUTOffsetAboveThreshold_createsStemCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.nextDouble()).thenReturn(60.0); // triggers MUD plane

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

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getMUDDivisionPlane(eq(stemCell));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(true).when(module).daughterStem(any(), any(), any());

        module.addCell(random, sim);

        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_MUDMUTOffsetBelowThreshold_createsGMCWithVolumeSwap() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.nextDouble()).thenReturn(10.0); // below 45 threshold

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
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(false).when(module).daughterStem(any(), any(), any());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }

        verify(newCell).schedule(any());
    }

    @Test
    public void getNumNBNeighbors_withTwoUniqueStemNeighbors_returnsCorrectCount() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        // Unique IDs returned by potts
        HashSet<Integer> idsVoxel1 = new HashSet<>();
        idsVoxel1.add(10);
        idsVoxel1.add(11);
        HashSet<Integer> idsVoxel2 = new HashSet<>();
        idsVoxel2.add(11); // repeat → should still count neighbor 11 only once
        idsVoxel2.add(12);

        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(idsVoxel1);
        when(potts.getUniqueIDs(1, 0, 0)).thenReturn(idsVoxel2);

        // Neighbors
        PottsCell neighbor10 = mock(PottsCell.class);
        PottsCell neighbor11 = mock(PottsCell.class);
        PottsCell neighbor12 = mock(PottsCell.class);

        when(neighbor10.getID()).thenReturn(10);
        when(neighbor11.getID()).thenReturn(11);
        when(neighbor12.getID()).thenReturn(12);
        when(stemCell.getID()).thenReturn(42);

        when(neighbor10.getPop()).thenReturn(stemCellPop); // match cell.getPop
        when(neighbor11.getPop()).thenReturn(stemCellPop); // match cell.getPop
        when(neighbor12.getPop()).thenReturn(99); // no match

        when(grid.getObjectAt(10)).thenReturn(neighbor10);
        when(grid.getObjectAt(11)).thenReturn(neighbor11);
        when(grid.getObjectAt(12)).thenReturn(neighbor12);

        int numNeighbors = module.getNumNBNeighbors(sim);

        assertEquals(2, numNeighbors, "Should count 2 unique matching neighbors (10 and 11)");
    }

    @Test
    public void getNumNBNeighbors_noMatchingNeighbors_returnsZero() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        HashSet<Integer> ids = new HashSet<>();
        ids.add(50);
        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(ids);

        PottsCell nonStemNeighbor = mock(PottsCell.class);
        when(nonStemNeighbor.getPop()).thenReturn(99); // doesn't match stem pop
        when(nonStemNeighbor.getID()).thenReturn(50);
        when(grid.getObjectAt(50)).thenReturn(nonStemNeighbor);

        when(stemCell.getID()).thenReturn(42);

        int numNeighbors = module.getNumNBNeighbors(sim);

        assertEquals(0, numNeighbors, "No neighbors should be counted when pops do not match.");
    }

    @Test
    public void getNumNBNeighbors_called_doesNotCountSelf() {
        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        when(stemLoc.getVoxels()).thenReturn(voxels);

        HashSet<Integer> ids = new HashSet<>();
        ids.add(42); // same as sim.getID() mock (self)
        when(potts.getUniqueIDs(0, 0, 0)).thenReturn(ids);

        PottsCell selfCell = stemCell; // or mock another PottsCell with same pop
        when(grid.getObjectAt(42)).thenReturn(selfCell);

        int numNeighbors = module.getNumNBNeighbors(sim);
        assertEquals(0, numNeighbors, "Self should not be counted as a neighbor");
    }

    @Test
    public void updateNBContactGrowthRate_noNeighbors_returnsBaseGrowthRate() {
        // Mock parameters
        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(5.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(10.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // Mock neighbor count
        doReturn(0).when(module).getNumNBNeighbors(sim);

        module.updateNBContactGrowthRate(sim);
        assertEquals(
                10.0,
                module.cellGrowthRate,
                1e-6,
                "With 0 neighbors, hill repression should be 1.0");
    }

    @Test
    public void updateNBContactGrowthRate_halfMaxNeighbors_returnsHalfBaseGrowthRate() {
        // Mock parameters
        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(5.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(10.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // Mock neighbor count
        doReturn(5).when(module).getNumNBNeighbors(sim);

        module.updateNBContactGrowthRate(sim);
        // Hill repression = K^n / (K^n + N^n) = 25 / (25 + 25) = 0.5
        assertEquals(
                5.0,
                module.cellGrowthRate,
                1e-6,
                "With 0 neighbors, hill repression should be 1.0");
    }

    @Test
    public void updateNBContactGrowthRate_highNeighbors_returnsLowGrowthRate() {
        when(parameters.getDouble("proliferation/NB_CONTACT_HALF_MAX")).thenReturn(5.0);
        when(parameters.getDouble("proliferation/NB_CONTACT_HILL_N")).thenReturn(2.0);
        when(parameters.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(10.0);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        doReturn(20).when(module).getNumNBNeighbors(sim);

        module.updateNBContactGrowthRate(sim);

        // Hill repression = 25 / (25 + 400) = 25 / 425 ≈ 0.0588
        assertEquals(10.0 * (25.0 / 425.0), module.cellGrowthRate, 1e-6);
    }

    // TODO: Have Danielle rename and fix
    //     @Test
    //     void daughterStem_DeterministicTrue() {
    //         // Mock parameters
    //         when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
    //                 .thenReturn("TRUE");
    //
    // when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
    //         when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
    //                 .thenReturn(0.1);

    //         // Mock cell type + division plane normal vector
    //         Plane plane = mock(Plane.class);
    //         when(plane.getUnitNormalVector()).thenReturn(new Vector(1.0, 0, 0));

    //         // Construct module
    //         PottsModuleFlyStemProliferation module = new
    // PottsModuleFlyStemProliferation(stemCell);

    //         // Call
    //         boolean result = module.daughterStem(stemLoc, daughterLoc, plane);

    //         // Verify
    //         assertTrue(
    //                 result,
    //                 "Expected daughterStemWrapper to return true for deterministic orientation");
    //     }

    //     @Test
    //     void testDaughterStem_DeterministicFalse() {
    //         when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
    //                 .thenReturn("TRUE");
    //
    // when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
    //         when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
    //                 .thenReturn(0.1);

    //         Plane plane = mock(Plane.class);
    //         when(plane.getUnitNormalVector()).thenReturn(new Vector(0, 1.0, 0));

    //         PottsModuleFlyStemProliferation module = new
    // PottsModuleFlyStemProliferation(stemCell);

    //         boolean result = module.daughterStem(stemLoc, daughterLoc, plane);

    //         assertFalse(result, "Expected false when division plane normal is not (1,0,0)");
    //     }

    @Test
    void testDaughterStem_RuleBased_VolumeTrue() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(10.0); // large enough for |10 - 5| < 10

        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        PottsModuleFlyStemProliferation module = new PottsModuleFlyStemProliferation(stemCell);

        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class));

        assertTrue(result, "Expected true since |10-5| < range");
    }

    @Test
    void testDaughterStem_RuleBased_VolumeFalse() {
        when(parameters.getString("proliferation/HAS_DETERMINISTIC_DIFFERENTIATION"))
                .thenReturn("FALSE");
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(1.0); // |10 - 5| = 5 > 1

        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        PottsModuleFlyStemProliferation module = new PottsModuleFlyStemProliferation(stemCell);

        boolean result = module.daughterStem(stemLoc, daughterLoc, mock(Plane.class));

        assertFalse(result, "Expected false since |10-5| > range");
    }
}
