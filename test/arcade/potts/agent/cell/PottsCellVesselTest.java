package arcade.potts.agent.cell;

import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.env.location.PottsLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellVesselTest {
    private static PottsCellVessel makeCell(PottsLocation location) {
        PottsCellContainer container =
                new PottsCellContainer(1, 0, 2, 0, 0, State.UNDEFINED, null, 0, 10, 5);
        Parameters parameters = new Parameters(new MiniBox(), null, null);
        return new PottsCellVessel(container, location, parameters, null);
    }

    @Test
    public void step_onlyAges() {
        PottsLocation location = mock(PottsLocation.class);
        doReturn(25.0).when(location).getVolume();
        PottsCellVessel cell = makeCell(location);
        SimState sim = mock(SimState.class);

        for (int i = 0; i < 10; i++) {
            cell.step(sim);
        }

        assertEquals(10, cell.getAge());
        assertEquals(State.UNDEFINED, cell.getState());
        assertEquals(25.0, cell.getVolume(), 1E-8);
        verify(location, never()).add(anyInt(), anyInt(), anyInt());
        verify(location, never()).remove(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void convert_vesselCell_returnsContainerWithNullPhase() {
        PottsLocation location = mock(PottsLocation.class);
        doReturn(25.0).when(location).getVolume();
        PottsCellVessel cell = makeCell(location);

        PottsCellContainer container = (PottsCellContainer) cell.convert();

        assertEquals(1, container.id);
        assertEquals(2, container.pop);
        assertEquals(0, container.age);
        assertNull(container.phase);
        assertEquals(25, container.voxels);
    }

    @Test
    public void make_throws() {
        PottsCellVessel cell = makeCell(mock(PottsLocation.class));
        assertThrows(
                UnsupportedOperationException.class,
                () -> cell.make(2, State.UNDEFINED, new MersenneTwisterFast()));
    }
}
