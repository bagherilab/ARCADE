package arcade.patch.agent.process;

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

    private static final double EPSILON = 1e-3;

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
    public void stepProcess_called_updatesEnvironment() {
        when(mockParameters.getDouble("inflammation/IL2_RECEPTORS")).thenReturn(5000.0);
        inflammation = new PatchProcessInflammationCD4(mockCell);
        inflammation.active = true;
        inflammation.activeTicker = 10;
        inflammation.iL2Ticker = 10;
        inflammation.boundArray = new double[180];
        Arrays.fill(inflammation.boundArray, 0);
        inflammation.boundArray[9] = 100;

        inflammation.stepProcess(mockRandom, mockSim);

        double expectedValue = 4.33E10;

        verify(mockLattice, times(1))
                .setValue(
                        eq(mockLocation),
                        doubleThat(val -> Math.abs(val - expectedValue) < EPSILON * expectedValue));
    }

    @Test
    public void stepProcess_noPriorIL2_updatesEnvironment() {
        when(mockParameters.getDouble("inflammation/IL2_RECEPTORS")).thenReturn(5000.0);
        inflammation = new PatchProcessInflammationCD4(mockCell);
        inflammation.active = true;
        inflammation.activeTicker = 10;
        inflammation.iL2Ticker = 10;
        inflammation.boundArray = new double[180];
        Arrays.fill(inflammation.boundArray, 0);

        inflammation.stepProcess(mockRandom, mockSim);

        double expectedValue = 4.32E10;

        verify(mockLattice, times(1))
                .setValue(
                        eq(mockLocation),
                        doubleThat(val -> Math.abs(val - expectedValue) < EPSILON * expectedValue));
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
    public void update_withUnevenVolume_splitsUnevenly() {
        inflammation = new PatchProcessInflammationCD4(mockCell);
        PatchProcessInflammationCD4 parentProcess = new PatchProcessInflammationCD4(mockCell);
        parentProcess.amts[PatchProcessInflammationCD4.IL2RBGA] = 100;
        when(mockCell.getVolume()).thenReturn(cellVolume / 4);

        inflammation.update(parentProcess);

        assertEquals(25, inflammation.amts[PatchProcessInflammationCD4.IL2RBGA]);
        assertEquals(75, parentProcess.amts[PatchProcessInflammationCD4.IL2RBGA]);
    }
}
