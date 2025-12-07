package arcade.potts.agent.module;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCellFlyGMC;
import static org.mockito.Mockito.*;

public class PottsModuleProliferationVolumeBasedDivisionTest {

    static class PottsModuleProliferationVolumeBasedDivisionMock
            extends PottsModuleProliferationVolumeBasedDivision {
        boolean addCellCalled = false;

        PottsModuleProliferationVolumeBasedDivisionMock(PottsCellFlyGMC cell) {
            super(cell);
        }

        @Override
        void addCell(MersenneTwisterFast random, Simulation sim) {
            addCellCalled = true;
        }
    }

    @Test
    public void step_belowCheckpoint_updatesTargetOnly() {
        PottsCellFlyGMC cell = mock(PottsCellFlyGMC.class);
        Parameters params = mock(Parameters.class);
        when(cell.getParameters()).thenReturn(params);
        when(params.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(params.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(cell.getCriticalVolume()).thenReturn(100.0);
        when(cell.getVolume()).thenReturn(50.0); // below checkpoint

        PottsModuleProliferationVolumeBasedDivisionMock module =
                new PottsModuleProliferationVolumeBasedDivisionMock(cell);

        module.step(mock(MersenneTwisterFast.class), mock(Simulation.class));

        verify(cell).updateTarget(4.0, 1.2);
        assert !module.addCellCalled : "addCell should not be called below checkpoint";
    }

    @Test
    public void step_atOrAboveCheckpoint_triggersAddCell() {

        PottsCellFlyGMC cell = mock(PottsCellFlyGMC.class);
        Parameters params = mock(Parameters.class);
        when(cell.getParameters()).thenReturn(params);
        when(params.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(params.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(cell.getCriticalVolume()).thenReturn(100.0);
        when(cell.getVolume()).thenReturn(120.0); // at or above checkpoint

        PottsModuleProliferationVolumeBasedDivisionMock module =
                new PottsModuleProliferationVolumeBasedDivisionMock(cell);

        module.step(mock(MersenneTwisterFast.class), mock(Simulation.class));

        verify(cell).updateTarget(4.0, 1.2);
        assert module.addCellCalled : "addCell should be called at or above checkpoint";
    }
}
