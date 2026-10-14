package arcade.potts.agent.module;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCellWound;
import arcade.potts.agent.process.PottsProcessSecretion;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.agent.module.PottsModule.PoissonFactory;
import static arcade.potts.util.PottsEnums.Domain;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

public class PottsModuleSenescenceTest {
    private MiniBox box;

    private PottsCellWound cell;

    private PottsProcessSecretion secretion;

    private PottsSimulation sim;

    private MersenneTwisterFast random;

    private double time;

    @BeforeEach
    public final void setUp() {
        box = new MiniBox();
        box.put("senescence/RATE_EARLY", 2);
        box.put("senescence/STEPS_EARLY", 5);
        box.put("senescence/CLEARANCE_EARLY", 0);
        box.put("senescence/CLEARANCE_LATE", 0);
        box.put("senescence/BASAL_CLEARANCE", 0);
        box.put("senescence/DRUG_START", 10);
        box.put("senescence/DRUG_DURATION", 5);
        box.put("senescence/DRUG_KILL", 0);
        box.put("senescence/EARLY_RATE_PDGF", 4);
        box.put("senescence/EARLY_RATE_INF", 0);
        box.put("senescence/LATE_RATE_PDGF", 0);
        box.put("senescence/LATE_RATE_INF", 6);

        cell = mock(PottsCellWound.class);
        doAnswer(i -> new Parameters(box, null, null)).when(cell).getParameters();
        secretion = mock(PottsProcessSecretion.class);
        doReturn(Set.of("PDGF", "INF")).when(secretion).getLayers();
        doReturn(secretion).when(cell).getProcess(Domain.SECRETION);

        sim = mock(PottsSimulation.class);
        Schedule schedule = mock(Schedule.class);
        doAnswer(i -> time).when(schedule).getTime();
        doReturn(schedule).when(sim).getSchedule();
        doReturn(mock(PottsSeries.class)).when(sim).getSeries();

        random = mock(MersenneTwisterFast.class);
        doReturn(0.0).when(random).nextDouble();
    }

    // Sets the number of Poisson steps returned for the module.
    private static void setSteps(PottsModuleSenescence module, int steps) {
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory factory = mock(PoissonFactory.class);
        doReturn(poisson).when(factory).createPoisson(anyDouble(), any());
        module.poissonFactory = factory;
    }

    @Test
    public void constructor_givenParameters_setsEarlyPhaseAndEarlyRates() {
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        assertEquals(Phase.SENESCENT_EARLY, module.getPhase());
        verify(secretion).setRate("PDGF", 4);
        verify(secretion).setRate("INF", 0);
    }

    @Test
    public void constructor_noSecretionProcess_setsEarlyPhase() {
        doReturn(null).when(cell).getProcess(Domain.SECRETION);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        assertEquals(Phase.SENESCENT_EARLY, module.getPhase());
    }

    @Test
    public void setPhase_late_setsLateRates() {
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.setPhase(Phase.SENESCENT_LATE);
        assertEquals(Phase.SENESCENT_LATE, module.getPhase());
        verify(secretion).setRate("PDGF", 0);
        verify(secretion).setRate("INF", 6);
    }

    @Test
    public void step_earlyNotEnoughSteps_staysEarly() {
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        setSteps(module, 4);
        module.step(random, sim);
        assertEquals(Phase.SENESCENT_EARLY, module.getPhase());
    }

    @Test
    public void step_earlyEnoughSteps_movesToLate() {
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        setSteps(module, 2);
        module.step(random, sim);
        module.step(random, sim);
        assertEquals(Phase.SENESCENT_EARLY, module.getPhase());
        module.step(random, sim);
        assertEquals(Phase.SENESCENT_LATE, module.getPhase());
        verify(secretion).setRate("INF", 6);
    }

    @Test
    public void step_late_staysLate() {
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.setPhase(Phase.SENESCENT_LATE);
        setSteps(module, 100);
        module.step(random, sim);
        assertEquals(Phase.SENESCENT_LATE, module.getPhase());
    }

    @Test
    public void step_zeroClearance_doesNotDraw() {
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        setSteps(module, 0);
        module.step(random, sim);
        verify(random, never()).nextDouble();
        verify(cell, never()).setState(any());
    }

    @Test
    public void step_basalClearance_setsApoptotic() {
        box.put("senescence/BASAL_CLEARANCE", 0.5);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.step(random, sim);
        verify(cell).setState(State.APOPTOTIC);
    }

    @Test
    public void step_earlyTouchingMacrophage_usesEarlyClearance() {
        box.put("senescence/CLEARANCE_EARLY", 0.5);
        doReturn(true).when(cell).touchesMacrophage(sim);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.step(random, sim);
        verify(cell).setState(State.APOPTOTIC);
    }

    @Test
    public void step_lateTouchingMacrophageZeroLateClearance_notCleared() {
        box.put("senescence/CLEARANCE_EARLY", 0.5);
        doReturn(true).when(cell).touchesMacrophage(sim);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.setPhase(Phase.SENESCENT_LATE);
        module.step(random, sim);
        verify(cell, never()).setState(any());
    }

    @Test
    public void step_lateTouchingMacrophage_usesLateClearance() {
        box.put("senescence/CLEARANCE_LATE", 0.5);
        doReturn(true).when(cell).touchesMacrophage(sim);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.setPhase(Phase.SENESCENT_LATE);
        module.step(random, sim);
        verify(cell).setState(State.APOPTOTIC);
    }

    @Test
    public void step_notTouchingMacrophage_notCleared() {
        box.put("senescence/CLEARANCE_EARLY", 0.5);
        doReturn(false).when(cell).touchesMacrophage(sim);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.step(random, sim);
        verify(cell, never()).setState(any());
    }

    @Test
    public void step_drugBeforeWindow_notCleared() {
        box.put("senescence/DRUG_KILL", 0.5);
        time = 9;
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.step(random, sim);
        verify(cell, never()).setState(any());
    }

    @Test
    public void step_drugAtWindowStart_setsApoptotic() {
        box.put("senescence/DRUG_KILL", 0.5);
        time = 10;
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.step(random, sim);
        verify(cell).setState(State.APOPTOTIC);
    }

    @Test
    public void step_drugAtWindowEnd_notCleared() {
        box.put("senescence/DRUG_KILL", 0.5);
        time = 15;
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        module.step(random, sim);
        verify(cell, never()).setState(any());
    }

    @Test
    public void step_cleared_doesNotAdvancePhase() {
        box.put("senescence/BASAL_CLEARANCE", 0.5);
        PottsModuleSenescence module = new PottsModuleSenescence(cell);
        setSteps(module, 100);
        module.step(random, sim);
        assertEquals(Phase.SENESCENT_EARLY, module.getPhase());
    }
}
