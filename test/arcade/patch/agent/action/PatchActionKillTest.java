package arcade.patch.agent.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.module.PatchModuleApoptosis;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums;
import arcade.patch.util.PatchEnums.AntigenFlag;
import arcade.patch.util.PatchEnums.State;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatchActionKillTest {

    private PatchCellCART mockCell;
    private PatchCellTissue mockTarget;
    private PatchProcessInflammation mockInflammation;
    private PatchActionKill action;
    private Schedule schedule;
    private PatchSimulation sim;

    @BeforeEach
    public void setUp() {
        mockCell = mock(PatchCellCART.class);
        mockTarget = mock(PatchCellTissue.class);
        mockInflammation = mock(PatchProcessInflammation.class);
        MersenneTwisterFast random = new MersenneTwisterFast();
        Series series = mock(Series.class);
        Parameters parameters = mock(Parameters.class);
        schedule = mock(Schedule.class);
        sim = mock(PatchSimulation.class);

        when(mockCell.getProcess(any())).thenReturn(mockInflammation);
        when(mockInflammation.getInternal("granzyme")).thenReturn(1.0);

        action = new PatchActionKill(mockCell, mockTarget, random, series, parameters);
    }

    @Test
    public void testSchedule() {
        action.schedule(schedule);
        verify(schedule)
                .scheduleOnce(anyDouble(), eq(PatchEnums.Ordering.ACTIONS.ordinal()), eq(action));
    }

    @Test
    public void testStep_CARCellStopped() {
        when(mockCell.isStopped()).thenReturn(true);
        action.step(sim);
        verify(mockTarget, never()).setState(any());
    }

    @Test
    public void testStep_TargetCellStopped() {
        when(mockTarget.isStopped()).thenReturn(true);
        action.step(sim);
        verify(mockTarget, never()).setState(any());
        verify(mockCell).setAntigenFlag(AntigenFlag.BOUND_CELL_RECEPTOR);
    }

    @Test
    public void testStep_KillTargetCell() {
        when(mockCell.isStopped()).thenReturn(false);
        when(mockTarget.isStopped()).thenReturn(false);
        PatchModuleApoptosis mockProcess = mock(PatchModuleApoptosis.class);
        when(mockTarget.getModule()).thenReturn(mockProcess);
        action.step(sim);
        verify(mockTarget).setState(State.APOPTOTIC);
        verify(mockInflammation).setInternal("granzyme", 0.0);
    }
}
