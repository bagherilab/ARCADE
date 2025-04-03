package arcade.potts.agent.module;

import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ec.util.MersenneTwisterFast;
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
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
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
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // WT splitOffsetPercentX
        expectedOffset.add(80); // WT splitOffsetPercentY
        PottsModuleProliferationFlyStem.getCellSplitLocation(stemCell);
        verify(stemLoc).getOffset(expectedOffset);
    }

    @Test
    public void getCellSplitLocation_MUDMUT_callsFunctionsWithCorrectParameters() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
        ArrayList<Integer> expectedOffset = new ArrayList<>();
        expectedOffset.add(50); // MUDMUT splitOffsetPercentX
        expectedOffset.add(50); // MUDMUT splitOffsetPercentY
        PottsModuleProliferationFlyStem.getCellSplitLocation(stemCell);
        verify(stemLoc).getOffset(expectedOffset);
    }

    @Test
    public void getDivisionPlaneWithRotationalVariance_WT_callsRotateVectorWithCorrectParameters() {
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
        module.getDivisionPlaneWithRotationalVariance(stemCell);

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
        // Verify the plain vector is from the cell's stem type.
        assertEquals(PottsCellFlyStem.StemType.WT.splitDirection.vector, plainCaptor.getValue());
        // Verify the axis is Direction.XY_PLANE.vector.
        assertEquals(Direction.XY_PLANE.vector, axisCaptor.getValue());
        // Verify the rotation offset is 0.1 (as returned by dist.nextDouble()).
        assertEquals(0.1, offsetCaptor.getValue());
    }

    @Test
    public void
            getDivisionPlaneWIthRotationalVariance_MUDMUT_callsRotateVectorWithCorrectParameters() {
        when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.MUDMUT);
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
        expectedOffset.add(50);
        Voxel dummyVoxel = mock(Voxel.class);
        when(stemLoc.getOffset(expectedOffset)).thenReturn(dummyVoxel);

        module = new PottsModuleProliferationFlyStem(stemCell);
        module.getDivisionPlaneWithRotationalVariance(stemCell);

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
        // Verify the plain vector is from the cell's stem type.
        assertEquals(
                PottsCellFlyStem.StemType.MUDMUT.splitDirection.vector, plainCaptor.getValue());
        // Verify the axis is Direction.XY_PLANE.vector.
        assertEquals(Direction.XY_PLANE.vector, axisCaptor.getValue());
        // Verify the rotation offset is 0.1 (as returned by dist.nextDouble()).
        assertEquals(0.1, offsetCaptor.getValue());
    }

    @Test
    public void addCell_volumeRulesetWithSwap_callsSwapVoxelsAndSchedulesNewCell() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));
        Plane dummyDivisionPlane = mock(Plane.class);
        doReturn(dummyDivisionPlane).when(module).getDivisionPlaneWithRotationalVariance(stemCell);

        when(stemLoc.getVolume()).thenReturn(10.0);
        when(daughterLoc.getVolume()).thenReturn(20.0);

        PottsCellContainer container = mock(PottsCellContainer.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random))).thenReturn(container);
        PottsCell newCell = mock(PottsCell.class);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        module.addCell(random, sim);

        // swapVoxels should be called
        verify(stemLoc).swapVoxels(daughterLoc);

        verify(stemCell).reset(potts.ids, potts.regions);
        verify(stemCell).make(42, State.PROLIFERATIVE, random);
        verify(grid).addObject(newCell, null);
        verify(potts).register(newCell);
        verify(newCell).reset(potts.ids, potts.regions);
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_locationRulesetNoSwap_callsNewCellCreationWithoutSwap() {
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("location");
        Plane dummyDivisionPlane = mock(Plane.class);
        module = spy(new PottsModuleProliferationFlyStem(stemCell));
        doReturn(dummyDivisionPlane).when(module).getDivisionPlaneWithRotationalVariance(stemCell);

        double[] daughterCentroid = {0, 3, 0}; // basal
        double[] stemCentroid = {0, 2, 0}; // apical
        when(daughterLoc.getCentroid()).thenReturn(daughterCentroid);
        when(stemLoc.getCentroid()).thenReturn(stemCentroid);

        PottsCellContainer container = mock(PottsCellContainer.class);
        when(stemCell.make(eq(42), eq(State.PROLIFERATIVE), eq(random))).thenReturn(container);
        PottsCell newCell = mock(PottsCell.class);
        when(container.convert(eq(factory), eq(daughterLoc), eq(random))).thenReturn(newCell);

        module.addCell(random, sim);

        // swapVoxels should NOT be called.
        verify(stemLoc, never()).swapVoxels(any());

        verify(stemCell).reset(potts.ids, potts.regions);
        verify(stemCell).make(42, State.PROLIFERATIVE, random);
        verify(grid).addObject(newCell, null);
        verify(potts).register(newCell);
        verify(newCell).reset(potts.ids, potts.regions);
        verify(newCell).schedule(any());
    }

    @Test
    public void addCell_invalidDifferentiationRuleset_throwsIllegalArgumentException() {
        // Set an invalid ruleset value.
        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("invalid");
        module = spy(new PottsModuleProliferationFlyStem(stemCell));
        doReturn(mock(Plane.class)).when(module).getDivisionPlaneWithRotationalVariance(stemCell);

        // Expect an IllegalArgumentException when addCell is invoked.
        assertThrows(IllegalArgumentException.class, () -> module.addCell(random, sim));
    }
}
