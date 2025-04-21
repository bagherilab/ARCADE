package arcade.patch.agent.process;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.env.location.PatchLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;

public class PatchProcessInflammationCD4Test {

    private PatchProcessInflammationCD4 inflammation;

    private PatchCellCART mockCell;

    private PatchLattice mockLattice;

    private Parameters mockParameters;

    private Simulation mockSim;

    private MersenneTwisterFast mockRandom;

    private double cellVolume;

    private PatchLocation mockLocation;

    @BeforeEach
    public final void setUp() {
        mockCell = Mockito.mock(PatchCellCART.class);
        mockParameters = Mockito.mock(Parameters.class);
        mockSim = Mockito.mock(Simulation.class);
        mockRandom = Mockito.mock(MersenneTwisterFast.class);
        mockLocation = mock(PatchLocation.class);
        mockLattice = mock(PatchLattice.class);

        Mockito.when(mockCell.getParameters()).thenReturn(mockParameters);
        cellVolume = randomDoubleBetween(165, 180);
        when(mockCell.getVolume()).thenReturn(cellVolume);
        when(mockCell.getLocation()).thenReturn(mockLocation);
        when(mockLocation.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
        when(mockParameters.getDouble(anyString())).thenReturn(1.0);
        when(mockParameters.getInt(anyString())).thenReturn(1);

        when(mockSim.getLattice(anyString())).thenReturn(mockLattice);
        doNothing().when(mockLattice).setValue(any(PatchLocation.class), anyDouble());
    }

    @Test
    public void stepProcess_called_updatesEnvironment()
            throws NoSuchFieldException, IllegalAccessException {
        inflammation = new PatchProcessInflammationCD4(mockCell);
        inflammation.active = true;
        inflammation.activeTicker = 10;
        inflammation.iL2Ticker = 10;
        inflammation.boundArray = new double[180];
        Arrays.fill(inflammation.boundArray, 0);
        Field receptors = PatchProcessInflammation.class.getDeclaredField("iL2Receptors");
        receptors.setAccessible(true);
        receptors.set(inflammation, 5000);

        inflammation.stepProcess(mockRandom, mockSim);

        verify(mockLattice, times(1))
                .setValue(any(PatchLocation.class), doubleThat(value -> value >= 4E10));
    }

    @Test
    public void update_evenSplit_splitsEvenly() {
        inflammation = new PatchProcessInflammationCD4(mockCell);
        PatchProcessInflammationCD4 parentProcess = new PatchProcessInflammationCD4(mockCell);
        parentProcess.amts[PatchProcessInflammationCD4.IL2RBGA] = 100;
        when(mockCell.getVolume()).thenReturn(cellVolume / 2);

        inflammation.update(parentProcess);

        assertEquals(50, inflammation.amts[PatchProcessInflammationCD4.IL2RBGA]);
        assertEquals(50, parentProcess.amts[PatchProcessInflammationCD4.IL2RBGA]);
    }

    @Test
    public void update_withZeroVolume_splitsUnevenly() {
        inflammation = new PatchProcessInflammationCD4(mockCell);
        PatchProcessInflammationCD4 parentProcess = new PatchProcessInflammationCD4(mockCell);
        parentProcess.amts[PatchProcessInflammationCD4.IL2RBGA] = 100;
        when(mockCell.getVolume()).thenReturn(0.0);

        inflammation.update(parentProcess);

        assertEquals(0.0, inflammation.amts[PatchProcessInflammationCD8.IL2RBGA]);
        assertEquals(0.0, inflammation.amts[PatchProcessInflammationCD8.IL2_IL2RBG]);
        assertEquals(0.0, inflammation.amts[PatchProcessInflammationCD8.IL2_IL2RBGA]);
        assertEquals(100, parentProcess.amts[PatchProcessInflammationCD4.IL2RBGA]);
    }
}
