package arcade.potts.agent.module;

import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.NormalDistribution;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.State;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.State;

public class PottsModuleProliferationFlyStemTest {
    PottsCellFlyStem stemCell;

    PottsModuleProliferationFlyStem module;

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

        // Default centroid and volume values (can be overridden in specific tests)
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.6, 0});

        // Parameter stubs
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

    @Test
    public void constructor_volumeRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.42);
        module = new PottsModuleProliferationFlyStem(stemCell);

        assertNotNull(module.splitDirectionDistribution);
        assertEquals("volume", module.differentiationRuleset);
        assertEquals(0.42, module.range, EPSILON);
    }

    @Test
    public void constructor_locationRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE"))
                .thenReturn(0.99);
        module = new PottsModuleProliferationFlyStem(stemCell);

        assertNotNull(module.splitDirectionDistribution);
        assertEquals("location", module.differentiationRuleset);
        assertEquals(0.99, module.range, EPSILON);
    }

    @Test
    public void getSmallerLocation_locationsDifferentSizes_returnsSmallerLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getVolume()).thenReturn(5.0);
        when(loc2.getVolume()).thenReturn(10.0);

        PottsLocation result = PottsModuleProliferationFlyStem.getSmallerLocation(loc1, loc2);
        assertEquals(loc1, result);
    }

    @Test
    public void getSmallerLocation_locationsSameSize_returnsSecondLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getVolume()).thenReturn(10.0);
        when(loc2.getVolume()).thenReturn(10.0);

        PottsLocation result = PottsModuleProliferationFlyStem.getSmallerLocation(loc1, loc2);
        assertEquals(loc2, result);
    }

    @Test
    public void getBasalLocation_centroidsDifferent_returnsLowerCentroid() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 1, 0});

        PottsLocation result = PottsModuleProliferationFlyStem.getBasalLocation(loc1, loc2);
        assertEquals(loc1, result);
    }

    @Test
    public void getBasalLocation_centroidsSame_returnsSecondLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 2, 0});

        PottsLocation result = PottsModuleProliferationFlyStem.getBasalLocation(loc1, loc2);
        assertEquals(loc2, result);
    }

    @Test
    public void getCellSplitVoxel_WT_callsLocationOffsetWithCorrectParams() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT.splitOffsetPercentX
        expectedOffset.add(80); // WT.splitOffsetPercentY

        PottsModuleProliferationFlyStem.getCellSplitVoxel(PottsCellFlyStem.StemType.WT, stemCell);
        verify(stemLoc).getOffset(expectedOffset);
    }

    @Test
    public void getCellSplitVoxel_MUDMUT_callsLocationOffsetWithCorrectParams() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT.splitOffsetPercentX
        expectedOffset.add(50); // MUDMUT.splitOffsetPercentY

        PottsModuleProliferationFlyStem.getCellSplitVoxel(
                PottsCellFlyStem.StemType.MUDMUT, stemCell);
        verify(stemLoc).getOffset(expectedOffset);
    }

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
        when(stemLoc.getOffset(expectedOffset)).thenReturn(splitVoxel);

        module = new PottsModuleProliferationFlyStem(stemCell);

        // Apply both rotations manually to get expected result
        Vector afterBaseRotation =
                Vector.rotateVectorAroundAxis(apicalAxis, new Vector(0, 0, 1), baseRotation);
        Vector expectedNormal =
                Vector.rotateVectorAroundAxis(
                        afterBaseRotation, new Vector(0, 0, 1), offsetRotation);

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

        Vector expectedNormal = new Vector(1, 0, 0);

        Voxel splitVoxel = new Voxel(7, 8, 9);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT x offset percent
        expectedOffset.add(50); // MUDMUT y offset percent
        when(stemLoc.getOffset(expectedOffset)).thenReturn(splitVoxel);

        module = new PottsModuleProliferationFlyStem(stemCell);
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

        module = new PottsModuleProliferationFlyStem(stemCell);
        double offset = module.sampleDivisionPlaneOffset();

        assertEquals(12.34, offset, EPSILON);
    }

    @Test
    public void daughterStem_stemTypeWT_returnsFalse() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);

        module = new PottsModuleProliferationFlyStem(stemCell);
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

        module = new PottsModuleProliferationFlyStem(stemCell);
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

        module = new PottsModuleProliferationFlyStem(stemCell);
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

        module = new PottsModuleProliferationFlyStem(stemCell);
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

        module = new PottsModuleProliferationFlyStem(stemCell);
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

        module = new PottsModuleProliferationFlyStem(stemCell);
        assertThrows(
                IllegalArgumentException.class, () -> module.daughterStem(stemLoc, daughterLoc));
    }

    // @Test
    // public void getDaughterCellApicalAxis_random_returnsRandomVector() {
    //     when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("random");
    //     when(random.nextDouble(true, true)).thenReturn(0.1, 0.2); // predictable random output

    //     module = new PottsModuleProliferationFlyStem(stemCell);
    //     Vector result = module.getDaughterCellApicalAxis(random);

    //     assertEquals(0.1, result.getX(), EPSILON);
    //     assertEquals(0.2, result.getY(), EPSILON);
    //     assertEquals(0, result.getZ(), EPSILON);
    // }

    @Test
    public void getDaughterCellApicalAxis_global_returnsApicalAxis() {
        Vector expectedAxis = new Vector(1.0, 2.0, 3.0);
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(expectedAxis);

        module = new PottsModuleProliferationFlyStem(stemCell);
        Vector result = module.getDaughterCellApicalAxis(random);

        assertEquals(expectedAxis.getX(), result.getX(), EPSILON);
        assertEquals(expectedAxis.getY(), result.getY(), EPSILON);
        assertEquals(expectedAxis.getZ(), result.getZ(), EPSILON);
    }

    @Test
    public void calculateGMCDaughterCellCriticalVolume_called_returnsExpectedValue() {
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        when(stemCell.getStemType())
                .thenReturn(PottsCellFlyStem.StemType.WT); // WT has 0.2 proportion

        module = new PottsModuleProliferationFlyStem(stemCell);
        double result = module.calculateGMCDaughterCellCriticalVolume();

        assertEquals(20.0, result, EPSILON); // 100 * 0.2
    }

    @Test
    public void chooseDivisionPlane_WT_callsWTVariant() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(dist.nextDouble()).thenReturn(12.0); // this can be any value

        module = spy(new PottsModuleProliferationFlyStem(stemCell));

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

        module = spy(new PottsModuleProliferationFlyStem(stemCell));

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

        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        Plane expectedPlane = mock(Plane.class);
        doReturn(expectedPlane).when(module).getMUDDivisionPlane(stemCell);

        Plane result = module.chooseDivisionPlane(stemCell);

        assertEquals(expectedPlane, result);
        verify(module).getMUDDivisionPlane(stemCell);
        verify(module, never()).getWTDivisionPlaneWithRotationalVariance(any(), anyDouble());
    }

    @Test
    public void addCell_WTVolumeSwap_swapsVoxelsAndCreatesNewCell() {
        // Set WT type
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.nextDouble()).thenReturn(10.0); // offset

        // Make the volumes so that the parent becomes the GMC (swap needed)
        when(stemLoc.getVolume()).thenReturn(5.0);
        when(daughterLoc.getVolume()).thenReturn(10.0);

        // Setup simulation and components
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

        // Prepare new cell creation
        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCellFlyStem newCell = mock(PottsCellFlyStem.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(2), eq(20.0)))
                .thenReturn(container); // 100 * 0.2 = 20.0
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        // Set critical volume and links
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        GrabBag links = mock(GrabBag.class);
        when(stemCell.getLinks()).thenReturn(links);
        when(links.next(random)).thenReturn(2);

        // Use a spy for module so we can stub getWTDivisionPlaneWithRotationalVariance
        module = spy(new PottsModuleProliferationFlyStem(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());

        // Stub the split call
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }

        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_WTVolumeNoSwap_doesNotSwapVoxelsAndCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getString("proliferation/APICAL_AXIS_RULESET")).thenReturn("global");
        when(stemCell.getApicalAxis()).thenReturn(new Vector(0, 1, 0));
        when(dist.nextDouble()).thenReturn(10.0);

        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0); // no swap needed

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
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(2), eq(20.0)))
                .thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);
        when(stemCell.getCriticalVolume()).thenReturn(100.0);
        GrabBag links = mock(GrabBag.class);
        when(stemCell.getLinks()).thenReturn(links);
        when(links.next(random)).thenReturn(2);

        module = spy(new PottsModuleProliferationFlyStem(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane)
                .when(module)
                .getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);

        try (MockedStatic<PottsLocation> mocked = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mocked.verify(() -> PottsLocation.swapVoxels(any(), any()), never());
        }

        verify(newCell).schedule(any());
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

        module = spy(new PottsModuleProliferationFlyStem(stemCell));
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getMUDDivisionPlane(eq(stemCell));
        when(stemLoc.split(eq(random), eq(dummyPlane))).thenReturn(daughterLoc);
        doReturn(true).when(module).daughterStem(any(), any());

        module.addCell(random, sim);

        verify(newCell).schedule(any());
    }

    // addCell_MUDMUTOffsetBelowThreshold_createsGMCWithVolumeSwap
    // addCell_MUDMUTOffsetBelowThreshold_createsGMCWithVolumeNoSwap
    // addCell_MUDMUTLocationRuleDifferenceWithinRange_createsStemCell
    // addCell_invalidDifferentiationRule_throwsException
}
