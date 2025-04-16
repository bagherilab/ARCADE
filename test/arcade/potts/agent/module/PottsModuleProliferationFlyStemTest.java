package arcade.potts.agent.module;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import sim.util.Double3D;
import arcade.core.env.grid.Grid;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.Vector;
import arcade.core.util.distributions.NormalDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.env.location.Voxel;
import arcade.potts.util.PottsEnums.State;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PottsModuleProliferationFlyStemTest {
    PottsCellFlyStem stemCell;

    PottsModuleProliferationFlyStem module;

    PottsLocation2D stemLoc;

    PottsLocation daughterLoc;

    Parameters parameters;

    Simulation sim;

    Potts potts;

    Grid grid;

    PottsCellFactory factory;

    MersenneTwisterFast random;

    NormalDistribution dist;

    @BeforeEach
    public final void setup() {
        stemCell = mock(PottsCellFlyStem.class);
        stemLoc = mock(PottsLocation2D.class);
        daughterLoc = mock(PottsLocation.class);
        parameters = mock(Parameters.class);
        sim = mock(PottsSimulation.class);
        potts = mock(Potts.class);
        grid = mock(Grid.class);
        factory = mock(PottsCellFactory.class);
        random = mock(MersenneTwisterFast.class);
        dist = mock(NormalDistribution.class);

        when(((PottsSimulation) sim).getPotts()).thenReturn(potts);
        potts.ids = new int[1][1][1];
        potts.regions = new int[1][1][1];
        when(sim.getGrid()).thenReturn(grid);
        when(sim.getCellFactory()).thenReturn(factory);
        when(sim.getSchedule()).thenReturn(mock(sim.engine.Schedule.class));
        when(sim.getID()).thenReturn(42);

        when(stemCell.getLocation()).thenReturn(stemLoc);
        when(stemLoc.split(eq(random), any(Plane.class))).thenReturn(daughterLoc);

        when(stemCell.getParameters()).thenReturn(parameters);
        when(parameters.getDistribution("proliferation/DIV_ROTATION_DISTRIBUTION"))
                .thenReturn(dist);
        when(dist.nextDouble()).thenReturn(0.1);
        when(stemCell.make(anyInt(), any(), eq(random))).thenReturn(mock(PottsCellContainer.class));
    }

    @AfterEach
    final void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void constructor_volumeRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = new PottsModuleProliferationFlyStem(stemCell);
        assertNotNull(module.splitDirectionDistribution);
        assertEquals("volume", module.differentiationRuleset);
    }

    @Test
    public void constructor_locationRuleset_setsExpectedFields() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        module = new PottsModuleProliferationFlyStem(stemCell);
        assertNotNull(module.splitDirectionDistribution);
        assertEquals("location", module.differentiationRuleset);
    }

    @Test
    public void getSmallerLocation_locationsDifferentSizes_returnsCorrectLocation() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getVolume()).thenReturn(10.0);
        when(loc2.getVolume()).thenReturn(20.0);
        assertEquals(loc1, PottsModuleProliferationFlyStem.getSmallerLocation(loc1, loc2));
    }

    @Test
    public void getSmallerLocation_locationsSameSize_returnsLocation2() {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getVolume()).thenReturn(10.0);
        when(loc2.getVolume()).thenReturn(10.0);
        assertEquals(loc2, PottsModuleProliferationFlyStem.getSmallerLocation(loc1, loc2));
    }

    @Test
    public void getBasalLocation_locationsDifferent_returnsLowerCentroidY() throws Exception {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 1, 0});
        assertEquals(loc1, PottsModuleProliferationFlyStem.getBasalLocation(loc1, loc2));
    }

    @Test
    public void getBasalLocation_locationsSame_returnsLocation2() throws Exception {
        PottsLocation loc1 = mock(PottsLocation.class);
        PottsLocation loc2 = mock(PottsLocation.class);
        when(loc1.getCentroid()).thenReturn(new double[] {0, 2, 0});
        when(loc2.getCentroid()).thenReturn(new double[] {0, 2, 0});
        assertEquals(loc2, PottsModuleProliferationFlyStem.getBasalLocation(loc1, loc2));
    }

    @Test
    public void getCellSplitLocation_WT_callsFunctionsWithCorrectParameters() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT splitOffsetPercentX
        expectedOffset.add(80); // WT splitOffsetPercentY
        PottsModuleProliferationFlyStem.getCellSplitLocation(PottsCellFlyStem.StemType.WT, stemCell);
        verify(stemLoc).getOffset(expectedOffset);
    }

    @Test
    public void getCellSplitLocation_MUDMUT_callsFunctionsWithCorrectParameters() {
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT splitOffsetPercentX
        expectedOffset.add(50); // MUDMUT splitOffsetPercentY
        PottsModuleProliferationFlyStem.getCellSplitLocation(PottsCellFlyStem.StemType.MUDMUT, stemCell);
        verify(stemLoc).getOffset(expectedOffset);
    }

    @Test
    public void
    getWTDivisionPlaneWithRotationalVariance_called_callsRotateVectorWithCorrectParameters() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        MockedStatic<Vector> mockedVector = mockStatic(Vector.class);
        Vector dummyRotatedVector = new Vector(1, 1, 1);
        mockedVector
                .when(
                        () ->
                                Vector.rotateVectorAroundAxis(
                                        any(Vector.class), any(Vector.class), anyDouble()))
                .thenReturn(dummyRotatedVector);

        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50);
        expectedOffset.add(80);
        Voxel dummyVoxel = mock(Voxel.class);
        when(stemLoc.getOffset(expectedOffset)).thenReturn(dummyVoxel);

        module = new PottsModuleProliferationFlyStem(stemCell);
        module.getWTDivisionPlaneWithRotationalVariance(stemCell, .1);

        // Capture the arguments passed to Vector.rotateVectorAroundAxis.
        ArgumentCaptor<Vector> plainCaptor = ArgumentCaptor.forClass(Vector.class);
        ArgumentCaptor<Vector> axisCaptor = ArgumentCaptor.forClass(Vector.class);
        ArgumentCaptor<Double> offsetCaptor = ArgumentCaptor.forClass(Double.class);
        mockedVector.verify(
                () ->
                        Vector.rotateVectorAroundAxis(
                                plainCaptor.capture(),
                                axisCaptor.capture(),
                                offsetCaptor.capture()));

        assertEquals(PottsCellFlyStem.StemType.WT.splitDirection.vector, plainCaptor.getValue());
        assertEquals(Direction.XY_PLANE.vector, axisCaptor.getValue());
        assertEquals(0.1, offsetCaptor.getValue());
    }

    @Test
    public void getMUDDivisionPlane_called_makesCorrectPlane() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);

        Voxel divVoxel = new Voxel(1, 2, 3);

        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT splitOffsetPercentX
        expectedOffset.add(50); // MUDMUT splitOffsetPercentY
        when(stemLoc.getOffset(expectedOffset)).thenReturn(divVoxel);

        module = new PottsModuleProliferationFlyStem(stemCell);
        Plane mudDivisionPlane = module.getMUDDivisionPlane(stemCell);

        Vector expectedNormal = PottsCellFlyStem.StemType.MUDMUT.splitDirection.vector;
        Double3D expectedPoint = new Double3D(1, 2, 3);

        assertEquals(expectedPoint, mudDivisionPlane.getReferencePoint());
        assertEquals(expectedNormal, mudDivisionPlane.getUnitNormalVector());
    }

    @Test
    public void daughterStem_stemTypeWT_returnsFalse() {
        when(((PottsCellFlyStem) stemCell).getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        module = new PottsModuleProliferationFlyStem(stemCell);
        boolean isDaughterStem = module.daughterStem(stemLoc, daughterLoc);
        assertFalse(isDaughterStem);
    }

    @Test
    public void daughterStem_volumeRuleset_differenceWithinRange_returnsTrue() {
        when(((PottsCellFlyStem) stemCell).getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE")).thenReturn(.5);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(10.4); // range is assumed >= 0.5 for this to return true
        module = new PottsModuleProliferationFlyStem(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);
        assertTrue(result);
    }

    @Test
    public void daughterStem_volumeRuleset_differenceOutsideRange_returnsFalse() {
        when(((PottsCellFlyStem) stemCell).getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE")).thenReturn(.5);
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(11.0);
        module = new PottsModuleProliferationFlyStem(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);
        assertFalse(result);
    }

    @Test
    public void daughterStem_locationRuleset_differenceWithinRange_returnsTrue() {
        when(((PottsCellFlyStem) stemCell).getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE")).thenReturn(.5);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.3, 0});
        module = new PottsModuleProliferationFlyStem(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);
        assertTrue(result);
    }

    @Test
    public void daughterStem_locationRuleset_differenceOutsideRange_returnsFalse() {
        when(((PottsCellFlyStem) stemCell).getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        when(parameters.getDouble("proliferation/DIFFERENTIATION_RULESET_EQUALITY_RANGE")).thenReturn(.5);
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.6, 0});
        module = new PottsModuleProliferationFlyStem(stemCell);
        boolean result = module.daughterStem(stemLoc, daughterLoc);
        assertFalse(result);
    }

    @Test
    public void daughterStem_invalidRuleset_throwsException() {
        when(((PottsCellFlyStem) stemCell).getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("invalid_ruleset");
        when(stemLoc.getCentroid()).thenReturn(new double[] {0, 1.0, 0});
        when(daughterLoc.getCentroid()).thenReturn(new double[] {0, 1.3, 0});
        module = new PottsModuleProliferationFlyStem(stemCell);
        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> module.daughterStem(stemLoc, daughterLoc));
        assertTrue(exception.getMessage().contains("Invalid differentiation ruleset"));
    }

    @Test
    public void addCell_WTVolumeSwapLoc_usesWTDivisionPlaneandCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        doReturn(0.0).when(module).getDivisionPlaneRotationOffset();
        doReturn(false).when(module).daughterStem(any(), any());

        // must swap voxels
        when(stemLoc.getVolume()).thenReturn(5.0);
        when(daughterLoc.getVolume()).thenReturn(10.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random))).thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        try (MockedStatic<PottsLocation> mockedStatic = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mockedStatic.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_WTVolumeNoSwapLoc_usesWTDivisionPlaneandCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        doReturn(0.0).when(module).getDivisionPlaneRotationOffset();
        doReturn(false).when(module).daughterStem(any(), any());

        // must not swap voxels
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random))).thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        try (MockedStatic<PottsLocation> mockedStatic = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mockedStatic.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc), never());
        }
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_MUDVolumeDifferentiatesSwapLoc_usesWTDivisionPlaneandCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        doReturn(0.0).when(module).getDivisionPlaneRotationOffset();
        doReturn(false).when(module).daughterStem(any(), any());

        // must swap voxels
        when(stemLoc.getVolume()).thenReturn(5.0);
        when(daughterLoc.getVolume()).thenReturn(10.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random))).thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        try (MockedStatic<PottsLocation> mockedStatic = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mockedStatic.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc));
        }
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_MUDVolumeDifferentiatesNoSwapLoc_usesWTDivisionPlaneandCreatesNewCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        doReturn(0.0).when(module).getDivisionPlaneRotationOffset();
        doReturn(false).when(module).daughterStem(any(), any());

        // must not swap voxels
        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(5.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random))).thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        try (MockedStatic<PottsLocation> mockedStatic = mockStatic(PottsLocation.class)) {
            module.addCell(random, sim);
            mockedStatic.verify(() -> PottsLocation.swapVoxels(stemLoc, daughterLoc), never());
        }
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_MUDVolumeDaughterIsStem_usesMUDDivisionPlaneAndCreatesStemCell() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        // MUD-style division plane because offset >= 45
        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getMUDDivisionPlane(eq(stemCell));
        doReturn(90.0).when(module).getDivisionPlaneRotationOffset();
        doReturn(true).when(module).daughterStem(any(), any());

        PottsCellContainer container = mock(PottsCellContainer.class);
        PottsCell newCell = mock(PottsCell.class);
        when(stemCell.getPop()).thenReturn(99);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random), eq(99))).thenReturn(container);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        module.addCell(random, sim);

        verify(stemCell).reset(potts.ids, potts.regions);
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_invalidRuleset_throwsException() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("banana");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));

        Plane dummyPlane = mock(Plane.class);
        doReturn(dummyPlane).when(module).getWTDivisionPlaneWithRotationalVariance(eq(stemCell), anyDouble());
        doReturn(0.0).when(module).getDivisionPlaneRotationOffset();
        doReturn(false).when(module).daughterStem(any(), any());

        assertThrows(IllegalArgumentException.class, () -> module.addCell(random, sim));
    }
}
