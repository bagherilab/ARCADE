package arcade.patch.agent.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.State;
import static org.mockito.Mockito.*;

public class PatchModuleCytotoxicityTest {

    private PatchCellCART mockCell;

    private PatchCellTissue mockTarget;

    private PatchProcessInflammation mockInflammation;

    private PatchModuleCytotoxicity action;

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

        action = new PatchModuleCytotoxicity(mockCell);
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
    public void step_killTargetCell_killsCellAndUsesGranzyme() {
        when(mockCell.isStopped()).thenReturn(false);
        when(mockTarget.isStopped()).thenReturn(false);
        PatchModuleApoptosis mockProcess = mock(PatchModuleApoptosis.class);
        when(mockTarget.getModule()).thenReturn(mockProcess);
        action.step(randomMock, sim);

        verify(mockTarget).setState(State.APOPTOTIC);
        verify(mockInflammation).setInternal("granzyme", 0.0);
    }
}
