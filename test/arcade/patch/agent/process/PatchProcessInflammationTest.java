package arcade.patch.agent.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.env.location.PatchLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PatchProcessInflammationTest {

    private PatchProcessInflammation inflammation;

    private PatchCellCART mockCell;

    private Parameters mockParameters;

    private Simulation mockSimulation;

    private MersenneTwisterFast mockRandom;

    static class InflammationMock extends PatchProcessInflammation {
        InflammationMock(PatchCellCART c) {
            super(c);
        }

        @Override
        void stepProcess(MersenneTwisterFast random, Simulation sim) {}

        @Override
        public void update(Process process) {}
    }

    @BeforeEach
    public final void setUp() {
        mockCell = Mockito.mock(PatchCellCART.class);
        mockParameters = Mockito.mock(Parameters.class);
        mockSimulation = Mockito.mock(Simulation.class);
        mockRandom = Mockito.mock(MersenneTwisterFast.class);
        PatchLocation mockLocation = mock(PatchLocation.class);
        PatchLattice mockLattice = mock(PatchLattice.class);

        Mockito.when(mockCell.getParameters()).thenReturn(mockParameters);
        Mockito.when(mockCell.getLocation()).thenReturn(mockLocation);
        Mockito.when(mockCell.getVolume()).thenReturn(150.0);
        Mockito.when(mockLocation.getVolume()).thenReturn(1000.0);
        Mockito.when(mockParameters.getDouble(anyString())).thenReturn(1.0);
        Mockito.when(mockParameters.getInt(anyString())).thenReturn(1);
        Mockito.when(mockSimulation.getLattice(anyString())).thenReturn(mockLattice);
        doNothing().when(mockLattice).setValue(any(PatchLocation.class), anyDouble());
        inflammation = new InflammationMock(mockCell);
    }

    @Test
    public void constructor_called_initiatesValues() {
        assertNotNull(inflammation);
        assertEquals(0, inflammation.getInternal("IL-2"));
        assertEquals(1.0, inflammation.getInternal("IL2R_total"));
    }

    @Test
    public void getInternal_called_returnsInternalValue() {
        inflammation.amts[PatchProcessInflammation.IL2_INT_TOTAL] = 10.0;
        assertEquals(10.0, inflammation.getInternal("IL-2"));
    }

    @Test
    public void setInternal_called_setsLayer() {
        inflammation.setInternal("IL-2", 10.0);
        assertEquals(10.0, inflammation.getInternal("IL-2"));
    }
}
