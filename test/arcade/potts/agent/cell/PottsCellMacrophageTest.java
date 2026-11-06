package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import arcade.potts.agent.process.PottsProcessSecretion;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Domain;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellMacrophageTest {
    private static final double EPSILON = 1E-12;

    // Threshold on mean INF for the M1 phenotype.
    private static final double THRESHOLD = 2;

    private PottsCellWoundHealingFixture f;

    @BeforeEach
    public final void setUp() {
        f = new PottsCellWoundHealingFixture();
    }

    // Creates a macrophage in the given state.
    private PottsCellMacrophage make(State state) {
        return new PottsCellMacrophage(f.container(state), f.location, f.parameters(), null);
    }

    // Adds a secretion process for MMP and PDGF, with constant rates of 1 for both.
    private void addSecretion() {
        f.box.put("(PROCESS)" + TAG_SEPARATOR + "SECRETION", "");
        for (String layer : new String[] {"MMP", "PDGF"}) {
            f.box.put("secretion/RATE_" + layer, 1.0);
            f.box.put("secretion/REGION_" + layer, "INSIDE");
        }
    }

    // Turns on the switch: M1 secretes MMP at 5, M2 secretes PDGF at 3.
    private void addSwitch() {
        addSecretion();
        f.box.put("INF_POLARIZATION", THRESHOLD);
        f.box.put("M1_RATE_MMP", 5.0);
        f.box.put("M1_RATE_PDGF", 0.0);
        f.box.put("M2_RATE_MMP", 0.0);
        f.box.put("M2_RATE_PDGF", 3.0);
    }

    // Gets the secretion process of the cell.
    private static PottsProcessSecretion secretion(PottsCellMacrophage cell) {
        return (PottsProcessSecretion) cell.getProcess(Domain.SECRETION);
    }

    @Test
    public void canProliferate_highPdgf_returnsFalse() {
        f.means.put("PDGF", 10.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void stepRules_quiescent_staysQuiescent() {
        PottsCellMacrophage cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_infAboveThreshold_setsM1Rates() {
        addSwitch();
        f.means.put("INF", THRESHOLD + 0.1);
        PottsCellMacrophage cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertTrue(cell.isM1(f.sim));
        assertEquals(5, secretion(cell).getRate("MMP"), EPSILON);
        assertEquals(0, secretion(cell).getRate("PDGF"), EPSILON);
    }

    @Test
    public void stepRules_infAtThreshold_setsM2Rates() {
        addSwitch();
        f.means.put("INF", THRESHOLD);
        PottsCellMacrophage cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertFalse(cell.isM1(f.sim));
        assertEquals(0, secretion(cell).getRate("MMP"), EPSILON);
        assertEquals(3, secretion(cell).getRate("PDGF"), EPSILON);
    }

    @Test
    public void stepRules_noInfLayer_setsM2Rates() {
        addSwitch();
        doReturn(null).when(f.sim).getLattice("INF");
        PottsCellMacrophage cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertEquals(3, secretion(cell).getRate("PDGF"), EPSILON);
    }

    @Test
    public void stepRules_infFalls_switchesBackToM2() {
        addSwitch();
        PottsCellMacrophage cell = make(State.QUIESCENT);
        f.means.put("INF", THRESHOLD + 1);
        cell.stepRules(f.random, f.sim);
        f.means.put("INF", 0.0);
        cell.stepRules(f.random, f.sim);
        assertEquals(0, secretion(cell).getRate("MMP"), EPSILON);
        assertEquals(3, secretion(cell).getRate("PDGF"), EPSILON);
    }

    @Test
    public void stepRules_switchOff_leavesRatesUnchanged() {
        addSecretion();
        f.means.put("INF", 100.0);
        PottsCellMacrophage cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertFalse(cell.isM1(f.sim));
        assertEquals(1, secretion(cell).getRate("MMP"), EPSILON);
        assertEquals(1, secretion(cell).getRate("PDGF"), EPSILON);
    }

    @Test
    public void constructor_switchWithoutSecretion_throws() {
        f.box.put("INF_POLARIZATION", THRESHOLD);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_negativeThreshold_throws() {
        f.box.put("INF_POLARIZATION", -1.0);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_switchMissingPhenotypeRate_throws() {
        // Only M1_RATE_MMP is given; in a setup, defaults copy every rate to every layer.
        addSecretion();
        f.box.put("INF_POLARIZATION", THRESHOLD);
        f.box.put("M1_RATE_MMP", 5.0);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void step_quiescentBasalDeath_becomesApoptotic() {
        f.box.put("BASAL_DEATH", 0.5);
        PottsCellMacrophage cell = make(State.QUIESCENT);
        sim.engine.SimState simstate = f.sim;
        simstate.random = f.random;
        cell.step(simstate);
        assertEquals(State.APOPTOTIC, cell.getState());
    }
}
