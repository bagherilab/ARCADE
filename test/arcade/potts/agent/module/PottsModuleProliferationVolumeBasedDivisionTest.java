package arcade.potts.agent.module;

import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCellFlyGMC;
import arcade.potts.env.location.PottsLocation2D;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PottsModuleProliferationVolumeBasedDivisionTest {

    static class PottsModuleProliferationVolumeBasedDivisionMock
            extends PottsModuleProliferationVolumeBasedDivision {
        boolean addCellCalled = false;
        boolean growthRateUpdated = false;

        PottsModuleProliferationVolumeBasedDivisionMock(PottsCellFlyGMC cell) {
            super(cell);
        }

        @Override
        void addCell(MersenneTwisterFast random, Simulation sim) {
            addCellCalled = true;
        }

        @Override
        public void updateGrowthRate() {
            growthRateUpdated = true;
        }
    }

    @Test
    public void step_belowCheckpoint_updatesTarget() {
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
        assert module.growthRateUpdated : "growth rate should be updated on every step";
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
        assert module.growthRateUpdated : "growth rate should be updated on every step";
        assert module.addCellCalled : "addCell should be called at or above checkpoint";
    }

    @Test
    public void updateVolumeBasedGrowthRate_ratioOne_keepsBaseRate() {
        // baseGrowth = 4.0, volume = Ka => growth = 4.0
        PottsCellFlyGMC cell = mock(PottsCellFlyGMC.class);
        Parameters params = mock(Parameters.class);
        PottsLocation2D loc = mock(PottsLocation2D.class);

        when(cell.getParameters()).thenReturn(params);
        when(params.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(params.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(params.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(1);
        when(params.getDouble("proliferation/GROWTH_RATE_VOLUME_SENSITIVITY")).thenReturn(2.0);

        when(cell.getLocation()).thenReturn(loc);
        when(loc.getVolume()).thenReturn(100.0);
        when(cell.getCriticalVolume()).thenReturn(100.0);

        PottsModuleProliferationVolumeBasedDivisionTest
                        .PottsModuleProliferationVolumeBasedDivisionMock
                module = new PottsModuleProliferationVolumeBasedDivisionMock(cell);

        module.updateVolumeBasedGrowthRate();
        assertEquals(4.0, module.cellGrowthRate, 1e-9);
    }

    @Test
    public void updateVolumeBasedGrowthRate_ratioGreaterThanOne_scalesUpByPowerLaw() {
        // baseGrowth = 2.0, ratio = 2.0, sensitivity = 3 => 2 * 2^3 = 2 * 8 = 12
        PottsCellFlyGMC cell = mock(PottsCellFlyGMC.class);
        Parameters params = mock(Parameters.class);
        PottsLocation2D loc = mock(PottsLocation2D.class);

        when(cell.getParameters()).thenReturn(params);
        when(params.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(params.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(2.0);
        when(params.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(1);
        when(params.getDouble("proliferation/GROWTH_RATE_VOLUME_SENSITIVITY")).thenReturn(3.0);

        when(cell.getLocation()).thenReturn(loc);
        when(loc.getVolume()).thenReturn(200.0);
        when(cell.getCriticalVolume()).thenReturn(100.0);

        PottsModuleProliferationVolumeBasedDivisionTest
                        .PottsModuleProliferationVolumeBasedDivisionMock
                module = new PottsModuleProliferationVolumeBasedDivisionMock(cell);

        module.updateVolumeBasedGrowthRate();
        assertEquals(16.0, module.cellGrowthRate, 1e-9);
    }

    @Test
    public void updateVolumeBasedGrowthRate_ratioLessThanOne_scalesDownByPowerLaw() {
        // baseGrowth = 4.0, ratio = 0.5, sensitivity = 2.0 => 4 * 0.5^2 = 1.0
        PottsCellFlyGMC cell = mock(PottsCellFlyGMC.class);
        Parameters params = mock(Parameters.class);
        PottsLocation2D loc = mock(PottsLocation2D.class);

        when(cell.getParameters()).thenReturn(params);
        when(params.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(params.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(4.0);
        when(params.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(1);
        when(params.getDouble("proliferation/GROWTH_RATE_VOLUME_SENSITIVITY")).thenReturn(2.0);

        when(cell.getLocation()).thenReturn(loc);
        when(loc.getVolume()).thenReturn(50.0);
        when(cell.getCriticalVolume()).thenReturn(100.0);

        PottsModuleProliferationVolumeBasedDivisionTest
                        .PottsModuleProliferationVolumeBasedDivisionMock
                module = new PottsModuleProliferationVolumeBasedDivisionMock(cell);

        module.updateVolumeBasedGrowthRate();
        assertEquals(1.0, module.cellGrowthRate, 1e-9);
    }

    @Test
    public void updateVolumeBasedGrowthRate_zeroSensitivity_returnsBaseRateRegardlessOfVolume() {
        // sensitivity = 0 => growth = baseGrowth * ratio^0 = baseGrowth
        PottsCellFlyGMC cell = mock(PottsCellFlyGMC.class);
        Parameters params = mock(Parameters.class);
        PottsLocation2D loc = mock(PottsLocation2D.class);

        when(cell.getParameters()).thenReturn(params);
        when(params.getDouble("proliferation/SIZE_TARGET")).thenReturn(1.2);
        when(params.getDouble("proliferation/CELL_GROWTH_RATE")).thenReturn(3.5);
        when(params.getInt("proliferation/DYNAMIC_GROWTH_RATE_VOLUME")).thenReturn(1);
        when(params.getDouble("proliferation/GROWTH_RATE_VOLUME_SENSITIVITY")).thenReturn(0.0);

        when(cell.getLocation()).thenReturn(loc);
        when(loc.getVolume()).thenReturn(250.0);
        when(cell.getCriticalVolume()).thenReturn(100.0);

        PottsModuleProliferationVolumeBasedDivisionTest
                        .PottsModuleProliferationVolumeBasedDivisionMock
                module = new PottsModuleProliferationVolumeBasedDivisionMock(cell);

        module.updateVolumeBasedGrowthRate();
        assertEquals(3.5, module.cellGrowthRate, 1e-9);
    }
}
