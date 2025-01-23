package arcade.patch.agent.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellCARTCD4;
import arcade.patch.agent.cell.PatchCellContainer;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.State;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;
import static arcade.core.ARCADETestUtilities.randomIntBetween;

public class PatchActionResetTest {

    private PatchCellCART mockCell;
    private PatchActionReset actionReset;

    @BeforeEach
    public void setUp() {
        MersenneTwisterFast mockRandom = mock(MersenneTwisterFast.class);
        Series mockSeries = mock(Series.class);
        Parameters mockParameters = spy(new Parameters(new MiniBox(), null, null));
        PatchLocation mockLocation = mock(PatchLocation.class);

        doReturn(0.0).when(mockParameters).getDouble(any(String.class));
        doReturn(0).when(mockParameters).getInt(any(String.class));
        when(mockParameters.getInt("BOUND_TIME")).thenReturn(10);
        when(mockParameters.getInt("BOUND_RANGE")).thenReturn(5);
        when(mockRandom.nextInt()).thenReturn(1);

        int id = 1;
        int parentId = 1;
        int pop = 4;
        int age = randomIntBetween(1, 100800);
        int divisions = 10;
        double volume = randomDoubleBetween(100, 200);
        double height = randomDoubleBetween(4, 10);
        double criticalVolume = randomDoubleBetween(100, 200);
        double criticalHeight = randomDoubleBetween(4, 10);
        State state = State.UNDEFINED;
        ;

        PatchCellContainer container =
                new PatchCellContainer(
                        id,
                        parentId,
                        pop,
                        age,
                        divisions,
                        state,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);

        mockCell = spy(new PatchCellCARTCD4(container, mockLocation, mockParameters));

        actionReset = new PatchActionReset(mockCell, mockRandom, mockSeries, mockParameters);
    }

    @Test
    public void testSchedule() {
        Schedule mockSchedule = mock(Schedule.class);
        actionReset.schedule(mockSchedule);
        verify(mockSchedule)
                .scheduleOnce(
                        anyDouble(), eq(PatchEnums.Ordering.ACTIONS.ordinal()), eq(actionReset));
    }

    @Test
    public void testStep_CytotoxicState() {
        when(mockCell.isStopped()).thenReturn(false);
        mockCell.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);
        when(mockCell.getState()).thenReturn(State.CYTOTOXIC);

        actionReset.step(mock(SimState.class));

        verify(mockCell).setState(State.QUIESCENT);
        assertEquals(AntigenFlag.UNBOUND, mockCell.getAntigenFlag());
    }

    @Test
    public void testStep_StimulatoryState() {
        when(mockCell.isStopped()).thenReturn(false);
        mockCell.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);
        when(mockCell.getState()).thenReturn(State.STIMULATORY);

        actionReset.step(mock(SimState.class));

        verify(mockCell).setState(State.QUIESCENT);
        assertEquals(AntigenFlag.UNBOUND, mockCell.getAntigenFlag());
    }

    @Test
    public void testStep_StoppedCell() {
        when(mockCell.isStopped()).thenReturn(true);
        mockCell.setAntigenFlag(AntigenFlag.BOUND_ANTIGEN);
        actionReset.step(mock(SimState.class));
        verify(mockCell, never()).setState(any(State.class));
        assertEquals(AntigenFlag.BOUND_ANTIGEN, mockCell.getAntigenFlag());
    }
}
