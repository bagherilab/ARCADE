package arcade.potts.agent.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sim.util.Bag;
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

        // Link selection
        GrabBag links = mock(GrabBag.class);
        when(stemCell.getLinks()).thenReturn(links);
        when(links.next(random)).thenReturn(2);

        // Other defaults
        when(stemCell.getPop()).thenReturn(3);
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

        // Plane/voxel path (chooseDivisionPlane -> WT -> getWTDivisionPlaneWithRotationalVariance)
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

    // Differentiation rule tests

    @Test
    public void daughterStem_stemTypeWT_returnsFalse() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);

        assertFalse(result);
    }

    @Test
    public void daughterStem_volumeRule_differenceWithinRange_returnsTrue() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(1.0);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(10.5); // difference = 0.5 < 1.0

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);

        assertTrue(result);
    }

    @Test
    public void daughterStem_volumeRule_differenceOutsideRange_returnsFalse() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(11.0); // difference = 1.0 > 0.5

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);

        assertFalse(result);
    }

    @Test
    public void daughterStem_locationRule_differenceWithinRange_returnsTrue() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.3, 0}); // difference = 0.3
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);

        assertTrue(result);
    }

    @Test
    public void daughterStem_locationRule_differenceOutsideRange_returnsFalse() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.7, 0}); // difference = 0.7
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));

        module = new PottsModuleFlyStemProliferation(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);

        assertFalse(result);
    }

    @Test
    public void daughterStem_invalidRule_throwsException() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("banana");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.5);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.2, 0});

        module = new PottsModuleFlyStemProliferation(stemCell);
        assertThrows(
                IllegalArgumentException.class, () -> module.daughterStem(stemLoc, daughterLoc));
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
        when(factory.getParameters(3)).thenReturn(popParametersMiniBox);

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
        // Arrange: WT stem cell, using volume-based differentiation
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(parameters.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.0); // default for
        // volume
        when(parameters.getInt("proliferation/VOLUME_BASED_CRITICAL_VOLUME")).thenReturn(0); // use
        // classic
        // mode

        // Set up the condition that parent volume < daughter volume → stem/daughter swap required
        when(stemLoc.getVolume()).thenReturn(5.0);
        when(daughterLoc.getVolume()).thenReturn(10.0);

        // Stub division plane
        Plane dummyPlane = mock(Plane.class);
        when(dummyPlane.getUnitNormalVector()).thenReturn(new Vector(1, 0, 0));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        // Stub cell creation
        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newStemCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(2), eq(25.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newStemCell);

        // Spy on the module so we can override plane selection
        PottsModuleFlyStemProliferation module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());

        // Act: call addCell
        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);

            // Assert: verify voxels were swapped and new cell scheduled
            mocked.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }

        // Assert: new stem cell was scheduled
        verify(newStemCell).schedule(any());
    }

    @Test
    public void addCell_WTVolumeNoSwap_doesNotSwapVoxelsAndCreatesNewCell() {
        // Arrange: WT stem cell, using volume-based differentiation
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
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(2), eq(25.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newStemCell);

        // Spy and override division plane logic
        PottsModuleFlyStemProliferation module = spy(new PottsModuleFlyStemProliferation(stemCell));
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());

        // Act
        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);

            // Assert: swapVoxels should NOT be called
            mocked.verify(() -> PottsLocation.swapVoxels(any(), any()), never());
        }

        // Assert: new stem cell was created and scheduled
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
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(3), eq(100.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getPop()).thenReturn(3);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getMUDDivisionPlane(eq(stemCell));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(true).when(module).daughterStem(any(), any());

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
        when(stemCell.getPop()).thenReturn(3);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(false).when(module).daughterStem(any(), any());

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }

        verify(newCell).schedule(any());
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
        when(stemCell.getPop()).thenReturn(3);
        when(nb10.getPop()).thenReturn(3);
        when(nb11.getPop()).thenReturn(3);
        when(nb12OtherPop.getPop()).thenReturn(99); // filtered

        when(stemCell.getID()).thenReturn(42);

        when(grid.getObjectAt(10)).thenReturn(nb10);
        when(grid.getObjectAt(11)).thenReturn(nb11);
        when(grid.getObjectAt(12)).thenReturn(nb12OtherPop);

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
    public void getNBsInSimulation_emptyBag_returnsEmptySet() {
        Bag bag = new Bag(); // real MASON Bag
        when(grid.getAllObjects()).thenReturn(bag);

        module = new PottsModuleFlyStemProliferation(stemCell);
        HashSet<PottsCellFlyStem> result = module.getNBsInSimulation(sim);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Empty grid should yield empty set");
    }

    @Test
    public void getNBsInSimulation_mixedObjects_returnsOnlyMatchingFlyStems() {
        // Arrange: matching NB, non-matching NB, matching non-FlyStem, random object, matching NB
        PottsCellFlyStem nbMatch1 = mock(PottsCellFlyStem.class);
        when(nbMatch1.getPop()).thenReturn(3);

        PottsCellFlyStem nbOtherPop = mock(PottsCellFlyStem.class);
        when(nbOtherPop.getPop()).thenReturn(99);

        PottsCell nonNBButSamePop = mock(PottsCell.class);
        when(nonNBButSamePop.getPop()).thenReturn(3);

        Object random = new Object();

        PottsCellFlyStem nbMatch2 = mock(PottsCellFlyStem.class);
        when(nbMatch2.getPop()).thenReturn(3);

        Bag bag = new Bag();
        bag.add(nbMatch1);
        bag.add(nbOtherPop);
        bag.add(nonNBButSamePop);
        bag.add(random);
        bag.add(nbMatch2);
        when(grid.getAllObjects()).thenReturn(bag);

        when(stemCell.getPop()).thenReturn(3);

        module = new PottsModuleFlyStemProliferation(stemCell);
        HashSet<PottsCellFlyStem> result = module.getNBsInSimulation(sim);

        assertEquals(2, result.size(), "Should return exactly the two matching FlyStem NBs");
        assertTrue(result.contains(nbMatch1));
        assertTrue(result.contains(nbMatch2));
    }

    @Test
    public void getNBsInSimulation_includesSelfCell() {
        // The module's 'cell' has pop = 3 (already stubbed in @BeforeEach)
        when(stemCell.getPop()).thenReturn(3);

        // Bag contains: self (FlyStem, pop 3), another FlyStem pop 3, a non-FlyStem pop 3, and a
        // random object
        PottsCellFlyStem another = mock(PottsCellFlyStem.class);
        when(another.getPop()).thenReturn(3);
        PottsCell nonFlyStemSamePop = mock(PottsCell.class);
        when(nonFlyStemSamePop.getPop()).thenReturn(3);
        Object random = new Object();

        Bag bag = new Bag();
        bag.add(stemCell); // self
        bag.add(another); // matching FlyStem
        bag.add(nonFlyStemSamePop); // same pop but NOT FlyStem → should be ignored
        bag.add(random); // ignored

        when(grid.getAllObjects()).thenReturn(bag);

        module = new PottsModuleFlyStemProliferation(stemCell);
        HashSet<PottsCellFlyStem> result = module.getNBsInSimulation(sim);

        assertTrue(result.contains(stemCell), "Result should include the module's own stem cell.");
        assertTrue(result.contains(another), "Result should include other matching FlyStem cells.");
        assertEquals(
                2,
                result.size(),
                "Only the two FlyStem cells with matching pop should be returned.");
    }

    @Test
    public void updateVolumeBasedGrowthRate_pdeLikeFalse_usesCellVolume() {
        // pdeLike = 0 → should call updateCellVolumeBasedGrowthRate with THIS cell's volume
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(1);

        // Make the current cell's volume distinctive so we can verify it
        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemLoc.getVolume()).thenReturn(42.5);

        module = spy(new PottsModuleFlyStemProliferation(stemCell));

        // We only want to verify the value it was called with
        doNothing().when(module).updateCellVolumeBasedGrowthRate(anyDouble(), anyDouble());
        when(stemCell.getCriticalVolume()).thenReturn(100.0);

        module.updateVolumeBasedGrowthRate(sim);

        verify(module, times(1)).updateCellVolumeBasedGrowthRate(eq(42.5), eq(100.0));
        verify(module, never()).getNBsInSimulation(any());
    }

    @Test
    public void
            updateVolumeBasedGrowthRate_pdeLikeTrue_usesAverageVolumeAndAverageCritVolAcrossNBs() {
        // pdeLike = 1 (PDE-like) and dynamicGrowthRateNBContact must be 0 to avoid ctor exception
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(1);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(0);

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

        double expectedAvgVol = (10.0 + 20.0 + 40.0) / 3.0; // 23.333333333333332
        double expectedAvgCrit = (90.0 + 110.0 + 100.0) / 3.0; // 100.0

        verify(module, times(1)).getNBsInSimulation(sim);
        verify(module, times(1))
                .updateCellVolumeBasedGrowthRate(eq(expectedAvgVol), eq(expectedAvgCrit));
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
        for (int i = 0; i < 6; i++) {
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
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(1);

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
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(1);

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
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(1);

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
        when(parameters.getInt("proliferation/PDELIKE")).thenReturn(0);
        when(parameters.getInt("proliferation/DYNAMIC_GROWTH_RATE_NB_CONTACT")).thenReturn(1);

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
}
