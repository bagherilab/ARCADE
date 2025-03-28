package arcade.patch.agent.module;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class PatchModuleStimulationTest {

    private PatchCellCART mockCell;

    private PatchCellTissue mockTarget;

    private PatchProcessInflammation mockInflammation;

    private PatchModuleStimulation action;

    private PatchSimulation sim;

    private MersenneTwisterFast randomMock;

    @BeforeEach
    public final void setUp() {
        mockCell = mock(PatchCellCART.class);
        mockTarget = mock(PatchCellTissue.class);
        mockInflammation = mock(PatchProcessInflammation.class);
        sim = mock(PatchSimulation.class);
        randomMock = mock(MersenneTwisterFast.class);
        Parameters parameters = mock(Parameters.class);
        doReturn(0.0).when(parameters).getDouble(any(String.class));
        doReturn(0).when(parameters).getInt(any(String.class));

        when(mockCell.getParameters()).thenReturn(parameters);
        when(mockCell.getProcess(any())).thenReturn(mockInflammation);
        when(mockInflammation.getInternal("granzyme")).thenReturn(1.0);
        when(mockCell.getBoundTarget()).thenReturn(mockTarget);

        action = new PatchModuleStimulation(mockCell);
    }

    @Test
    public void step_CARCellStopped_doesNotChangeCell() {
        when(mockCell.isStopped()).thenReturn(true);

        action.step(randomMock, sim);

        verify(mockTarget, never()).setState(any());
    }

    @Test
    public void step_targetCellStopped_doesNotChangeCell() {
        when(mockTarget.isStopped()).thenReturn(true);

        action.step(randomMock, sim);

        verify(mockTarget, never()).setState(any());
        verify(mockCell).unbind();
    }

    @Test
    public void step_timeDelayNotReached_doesNotChangeCell()
            throws IllegalAccessException, NoSuchFieldException {
        Field delay = PatchModuleStimulation.class.getDeclaredField("timeDelay");
        delay.setAccessible(true);
        delay.set(action, 1);

        action.step(randomMock, sim);

        verify(mockCell, never()).setState(any());
        verify(mockCell, never()).unbind();
    }

    @Test
    public void step_resetTargetCell_resetsCell() {
        when(mockCell.isStopped()).thenReturn(false);
        when(mockTarget.isStopped()).thenReturn(false);
        PatchModuleQuiescence mockProcess = mock(PatchModuleQuiescence.class);
        when(mockTarget.getModule()).thenReturn(mockProcess);

        action.step(randomMock, sim);

        verify(mockTarget).setState(PatchEnums.State.QUIESCENT);
        verify(mockCell).setState(PatchEnums.State.UNDEFINED);
    }
}
