package arcade.patch.agent.process;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCellCART;
import arcade.patch.env.location.PatchLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;

public class PatchProcessMetabolismCARTTest {

    private PatchCellCART mockCell;

    private Parameters mockParameters;

    private PatchProcessMetabolismCART metabolism;

    private PatchLocation mockLocation;

    private double cellVolume;

    static class inflammationMock extends PatchProcessInflammation {
        public inflammationMock(PatchCellCART c) {
            super(c);
        }

        @Override
        void stepProcess(MersenneTwisterFast random, Simulation sim) {}

        @Override
        public void update(Process process) {}
    }

    @BeforeEach
    public void setUp() {
        mockCell = mock(PatchCellCART.class);
        mockParameters = mock(Parameters.class);
        mockLocation = mock(PatchLocation.class);

        when(mockCell.getParameters()).thenReturn(mockParameters);
        when(mockParameters.getDouble(anyString())).thenReturn(1.0);
        when(mockCell.getLocation()).thenReturn(mockLocation);
        cellVolume = randomDoubleBetween(165, 180);
        when(mockCell.getVolume()).thenReturn(cellVolume);
        when(mockLocation.getPerimeter(anyDouble()))
                .thenReturn(randomDoubleBetween(0, 1.0) * 6 * 30 / Math.sqrt(3));
        when(mockLocation.getArea()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30);
        when(mockLocation.getVolume()).thenReturn(3.0 / 2.0 / Math.sqrt(3.0) * 30 * 30 * 8.7);
    }

    @Test
    public void testConstructorInitializesFields()
            throws NoSuchFieldException, IllegalAccessException {
        metabolism = new PatchProcessMetabolismCART(mockCell);

        assertNotNull(metabolism);

        Field metaPrefField = PatchProcessMetabolismCART.class.getDeclaredField("metaPref");
        metaPrefField.setAccessible(true);
        assertEquals(1.0, metaPrefField.get(metabolism));

        Field conversionFractionField =
                PatchProcessMetabolismCART.class.getDeclaredField("conversionFraction");
        conversionFractionField.setAccessible(true);
        assertEquals(1.0, conversionFractionField.get(metabolism));

        Field fracMassField = PatchProcessMetabolismCART.class.getDeclaredField("fracMass");
        fracMassField.setAccessible(true);
        assertEquals(1.0, fracMassField.get(metabolism));

        Field ratioGlucosePyruvateField =
                PatchProcessMetabolismCART.class.getDeclaredField("ratioGlucosePyruvate");
        ratioGlucosePyruvateField.setAccessible(true);
        assertEquals(1.0, ratioGlucosePyruvateField.get(metabolism));

        Field lactateRateField = PatchProcessMetabolismCART.class.getDeclaredField("lactateRate");
        lactateRateField.setAccessible(true);
        assertEquals(1.0, lactateRateField.get(metabolism));

        Field autophagyRateField =
                PatchProcessMetabolismCART.class.getDeclaredField("autophagyRate");
        autophagyRateField.setAccessible(true);
        assertEquals(1.0, autophagyRateField.get(metabolism));

        Field glucUptakeRateField =
                PatchProcessMetabolismCART.class.getDeclaredField("glucUptakeRate");
        glucUptakeRateField.setAccessible(true);
        assertEquals(1.0, glucUptakeRateField.get(metabolism));

        Field metabolicPreferenceIL2Field =
                PatchProcessMetabolismCART.class.getDeclaredField("metabolicPreferenceIL2");
        metabolicPreferenceIL2Field.setAccessible(true);
        assertEquals(1.0, metabolicPreferenceIL2Field.get(metabolism));

        Field metabolicPreferenceActiveField =
                PatchProcessMetabolismCART.class.getDeclaredField("metabolicPreferenceActive");
        metabolicPreferenceActiveField.setAccessible(true);
        assertEquals(1.0, metabolicPreferenceActiveField.get(metabolism));

        Field glucoseUptakeRateIL2Field =
                PatchProcessMetabolismCART.class.getDeclaredField("glucoseUptakeRateIL2");
        glucoseUptakeRateIL2Field.setAccessible(true);
        assertEquals(1.0, glucoseUptakeRateIL2Field.get(metabolism));

        Field glucoseUptakeRateActiveField =
                PatchProcessMetabolismCART.class.getDeclaredField("glucoseUptakeRateActive");
        glucoseUptakeRateActiveField.setAccessible(true);
        assertEquals(1.0, glucoseUptakeRateActiveField.get(metabolism));

        Field minimumMassFractionActiveField =
                PatchProcessMetabolismCART.class.getDeclaredField("minimumMassFractionActive");
        minimumMassFractionActiveField.setAccessible(true);
        assertEquals(1.0, minimumMassFractionActiveField.get(metabolism));

        Field timeDelayField = PatchProcessMetabolismCART.class.getDeclaredField("timeDelay");
        timeDelayField.setAccessible(true);
        assertEquals(1, timeDelayField.get(metabolism));
    }

    @Test
    public void testStepProcess() throws NoSuchFieldException, IllegalAccessException {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.05);
        metabolism = new PatchProcessMetabolismCART(mockCell);
        Field fraction = PatchProcessMetabolism.class.getDeclaredField("f");
        fraction.setAccessible(true);
        fraction.set(metabolism, 1.0);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        assertTrue(metabolism.intAmts[PatchProcessMetabolismCART.GLUCOSE] >= 0);
        assertTrue(metabolism.intAmts[PatchProcessMetabolismCART.PYRUVATE] >= 0);
    }

    @Test
    public void testStepProcessWithZeroInitialGlucose() {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.0);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        assertEquals(0.0, metabolism.intAmts[PatchProcessMetabolismCART.GLUCOSE]);
    }

    @Test
    public void testStepProcessWithMaxGlucoseConcentration() {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION"))
                .thenReturn(Double.MAX_VALUE);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        assertTrue(metabolism.intAmts[PatchProcessMetabolismCART.GLUCOSE] <= Double.MAX_VALUE);
    }

    @Test
    public void testStepProcessWithNegativeGlucoseConcentration() {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(-1.0);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        assertTrue(metabolism.intAmts[PatchProcessMetabolismCART.GLUCOSE] >= 0);
    }

    @Test
    public void testStepProcessWithZeroOxygenConcentration() {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.05);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.extAmts[PatchProcessMetabolismCART.OXYGEN] = 0.0;

        metabolism.stepProcess(random, sim);

        assertEquals(0.0, metabolism.extAmts[PatchProcessMetabolismCART.OXYGEN]);
    }

    @Test
    public void testStepProcessWithMaxOxygenConcentration() {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.05);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.extAmts[PatchProcessMetabolismCART.OXYGEN] = Double.MAX_VALUE;

        metabolism.stepProcess(random, sim);

        assertTrue(metabolism.extAmts[PatchProcessMetabolismCART.OXYGEN] <= Double.MAX_VALUE);
    }

    @Test
    public void testActivatedMetabolicPreference()
            throws IllegalAccessException, NoSuchFieldException {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.05);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        Field activeTicker = PatchProcessInflammation.class.getDeclaredField("activeTicker");
        activeTicker.setAccessible(true);
        activeTicker.set(inflammation, 1);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        double expectedMetabolicPreference = 1.0 + 1.0; // base + active
        assertEquals(expectedMetabolicPreference, metabolism.getFinalMetabolicPreference());
    }

    @Test
    public void testActivatedGlucoseUptakeRate()
            throws IllegalAccessException, NoSuchFieldException {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.05);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        Field activeTicker = PatchProcessInflammation.class.getDeclaredField("activeTicker");
        activeTicker.setAccessible(true);
        activeTicker.set(inflammation, 1);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        double expectedGlucoseUptakeRate = 1.12 + 1.0; // base + active
        assertEquals(expectedGlucoseUptakeRate, metabolism.getFinalGlucoseUptakeRate());
    }

    @Test
    public void testActivatedMinimumMassFraction()
            throws NoSuchFieldException, IllegalAccessException {
        // set up metabolism class
        when(mockParameters.getDouble("metabolism/GLUCOSE_UPTAKE_RATE")).thenReturn(1.12);
        when(mockParameters.getDouble("metabolism/INITIAL_GLUCOSE_CONCENTRATION")).thenReturn(0.05);
        metabolism = new PatchProcessMetabolismCART(mockCell);

        // set up simulation
        MersenneTwisterFast random = new MersenneTwisterFast();
        Simulation sim = mock(Simulation.class);

        // mock inflammation process
        PatchProcessInflammation inflammation = new inflammationMock(mockCell);
        Field activeTicker = PatchProcessInflammation.class.getDeclaredField("activeTicker");
        activeTicker.setAccessible(true);
        activeTicker.set(inflammation, 1);
        when(mockCell.getProcess(any())).thenReturn(inflammation);
        when(mockCell.getActivationStatus()).thenReturn(true);

        metabolism.stepProcess(random, sim);

        double expectedMinimumMassFraction = 1.0 + 1.0; // base + active
        assertEquals(expectedMinimumMassFraction, metabolism.getFinalMinimumMassFraction());
    }

    @Test
    public void testUpdate() {
        metabolism = new PatchProcessMetabolismCART(mockCell);
        PatchProcessMetabolismCART parentProcess = new PatchProcessMetabolismCART(mockCell);
        parentProcess.intAmts[PatchProcessMetabolismCART.GLUCOSE] = 100;
        when(mockCell.getVolume()).thenReturn(cellVolume / 2);

        metabolism.update(parentProcess);

        assertEquals(50, metabolism.intAmts[PatchProcessMetabolismCART.GLUCOSE]);
        assertEquals(50, parentProcess.intAmts[PatchProcessMetabolismCART.GLUCOSE]);
    }

    @Test
    public void testUpdateZeroVolumeParent() {
        metabolism = new PatchProcessMetabolismCART(mockCell);
        PatchProcessMetabolismCART parentProcess = new PatchProcessMetabolismCART(mockCell);
        parentProcess.intAmts[PatchProcessMetabolismCART.GLUCOSE] = 100;
        when(mockCell.getVolume()).thenReturn(0.0);

        metabolism.update(parentProcess);

        assertEquals(0, metabolism.intAmts[PatchProcessMetabolismCART.GLUCOSE]);
        assertEquals(100, parentProcess.intAmts[PatchProcessMetabolismCART.GLUCOSE]);
    }
}