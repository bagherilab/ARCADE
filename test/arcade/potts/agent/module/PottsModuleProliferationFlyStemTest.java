package arcade.potts.agent.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.core.util.Plane;
import arcade.core.util.distributions.NormalDistribution;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PottsModuleProliferationFlyStemTest {

    PottsCellFlyStem stemCell;
    PottsModuleProliferationFlyStem module;
    PottsLocation stemLoc;
    PottsLocation daughterLoc;
    Parameters parameters;
    Simulation sim;
    Potts potts;
    Grid grid;
    PottsCellFactory factory;
    MersenneTwisterFast random;
    NormalDistribution dist;

    @BeforeEach
    public void setup() {
        stemCell = mock(PottsCellFlyStem.class);
        stemLoc = mock(PottsLocation.class);
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

        when(parameters.getString("proliferation/DIFFERENTIATION_RULESET")).thenReturn("volume");
        // when(stemCell.getStemType()).thenReturn(PottsCellFlyStem.StemType.WT);
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
}
