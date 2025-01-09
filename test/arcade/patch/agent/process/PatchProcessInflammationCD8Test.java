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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;

public class PatchProcessInflammationCD8Test {

    private PatchProcessInflammationCD8 inflammation;
    private PatchCellCART mockCell;
    private Parameters mockParameters;
    private Simulation mockSimulation;
    private MersenneTwisterFast mockRandom;
    private double cellVolume;

    @BeforeEach
    public void setUp() {
        mockCell = Mockito.mock(PatchCellCART.class);
        mockParameters = Mockito.mock(Parameters.class);
        mockSimulation = Mockito.mock(Simulation.class);
        mockRandom = Mockito.mock(MersenneTwisterFast.class);
        PatchLocation mockLocation = mock(PatchLocation.class);
        PatchLattice mockLattice = mock(PatchLattice.class);

        Mockito.when(mockCell.getParameters()).thenReturn(mockParameters);
        cellVolume = randomDoubleBetween(165, 180);
        when(mockCell.getVolume()).thenReturn(cellVolume);
        when(mockCell.getLocation()).thenReturn(mockLocation);
        when(mockLocation.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
        when(mockParameters.getDouble(anyString())).thenReturn(1.0);
        when(mockParameters.getInt(anyString())).thenReturn(1);

        when(mockSimulation.getLattice(anyString())).thenReturn(mockLattice);
        doNothing().when(mockLattice).setValue(any(PatchLocation.class), anyDouble());
    }

    @Test
    public void testConstructor() throws NoSuchFieldException, IllegalAccessException {
        inflammation = new PatchProcessInflammationCD8(mockCell);
        assertNotNull(inflammation);

        assertEquals(1, inflammation.amts[PatchProcessInflammationCD8.GRANZYME]);

        Field prior = PatchProcessInflammationCD8.class.getDeclaredField("priorIL2granz");
        prior.setAccessible(true);
        assertEquals(0.0, prior.get(inflammation));

        Field delay = PatchProcessInflammationCD8.class.getDeclaredField("GRANZ_SYNTHESIS_DELAY");
        delay.setAccessible(true);
        assertEquals(1, delay.get(inflammation));
    }

    @Test
    public void testStepProcess() throws NoSuchFieldException, IllegalAccessException {
        inflammation = new PatchProcessInflammationCD8(mockCell);
        inflammation.active = true;
        inflammation.activeTicker = 10;
        inflammation.IL2Ticker = 10;
        inflammation.boundArray = new double[180];
        Arrays.fill(inflammation.boundArray, 10000);

        Field receptors = PatchProcessInflammation.class.getDeclaredField("IL2_RECEPTORS");
        receptors.setAccessible(true);
        receptors.set(inflammation, 5000);

        inflammation.stepProcess(mockRandom, mockSimulation);

        assertTrue(inflammation.amts[PatchProcessInflammationCD8.GRANZYME] > 1);
    }

    @Test
    public void testStepProcessInactive() throws NoSuchFieldException, IllegalAccessException {
        inflammation = new PatchProcessInflammationCD8(mockCell);
        inflammation.active = false;
        inflammation.activeTicker = 10;
        inflammation.IL2Ticker = 10;
        inflammation.boundArray = new double[180];
        Arrays.fill(inflammation.boundArray, 10000);

        Field receptors = PatchProcessInflammation.class.getDeclaredField("IL2_RECEPTORS");
        receptors.setAccessible(true);
        receptors.set(inflammation, 5000);

        inflammation.stepProcess(mockRandom, mockSimulation);

        assertEquals(1, inflammation.amts[PatchProcessInflammationCD8.GRANZYME]);
    }

    @Test
    public void testStepProcessActiveTickerLessThanDelay()
            throws NoSuchFieldException, IllegalAccessException {
        Mockito.when(mockParameters.getInt("inflammation/GRANZ_SYNTHESIS_DELAY")).thenReturn(5);
        inflammation = new PatchProcessInflammationCD8(mockCell);
        inflammation.active = true;
        inflammation.activeTicker = 3;
        inflammation.IL2Ticker = 10;
        inflammation.boundArray = new double[180];
        Arrays.fill(inflammation.boundArray, 10000);

        Field receptors = PatchProcessInflammation.class.getDeclaredField("IL2_RECEPTORS");
        receptors.setAccessible(true);
        receptors.set(inflammation, 5000);

        inflammation.stepProcess(mockRandom, mockSimulation);

        assertEquals(1, inflammation.amts[PatchProcessInflammationCD8.GRANZYME]);
    }

    @Test
    public void testUpdate() {
        inflammation = new PatchProcessInflammationCD8(mockCell);
        PatchProcessInflammationCD8 parentProcess = new PatchProcessInflammationCD8(mockCell);
        parentProcess.amts[PatchProcessInflammationCD8.GRANZYME] = 100;
        when(mockCell.getVolume()).thenReturn(cellVolume / 2);

        inflammation.update(parentProcess);

        assertEquals(50, inflammation.amts[PatchProcessInflammationCD8.GRANZYME]);
        assertEquals(50, parentProcess.amts[PatchProcessInflammationCD8.GRANZYME]);
    }

    @Test
    public void testUpdateZeroVolumeParent() {
        inflammation = new PatchProcessInflammationCD8(mockCell);
        PatchProcessInflammationCD8 parentProcess = new PatchProcessInflammationCD8(mockCell);
        parentProcess.amts[PatchProcessInflammationCD8.GRANZYME] = 100;
        when(mockCell.getVolume()).thenReturn(0.0);

        inflammation.update(parentProcess);

        assertEquals(0, inflammation.amts[PatchProcessInflammationCD8.GRANZYME]);
        assertEquals(100, parentProcess.amts[PatchProcessInflammationCD8.GRANZYME]);
    }
}
