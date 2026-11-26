package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellMyofibroblastTest {
    private PottsCellWoundHealingFixture f;

    @BeforeEach
    public final void setUp() {
        f = new PottsCellWoundHealingFixture();
        f.means.put("ECM", 1.0);
    }

    // Creates a myofibroblast in the given state.
    private PottsCellMyofibroblast make(State state) {
        return new PottsCellMyofibroblast(f.container(state), f.location, f.parameters(), null);
    }

    @Test
    public void canProliferate_quiescentPdgfAboveThreshold_returnsTrue() {
        f.means.put("PDGF", 0.25);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativePdgfBelowHalfThreshold_returnsFalse() {
        f.means.put("PDGF", 0.09);
        assertFalse(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void stepRules_infAboveThreshold_becomesApoptotic() {
        f.means.put("INF", 4.0);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_drawBelowMaturationRate_becomesMature() {
        doReturn(0.2).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertTrue(cell.mature);
    }

    @Test
    public void stepRules_drawAboveMaturationRate_staysImmature() {
        doReturn(0.3).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertFalse(cell.mature);
    }

    @Test
    public void stepRules_matureLowEcm_becomesApoptotic() {
        f.means.put("ECM", 0.05);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_matureDrawBelowSenescenceRate_becomesSenescent() {
        f.box.put("SENESCENCE_RATE", 0.5);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_immature_doesNotBecomeSenescent() {
        f.box.put("SENESCENCE_RATE", 0.5);
        doReturn(0.3).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_quiescentCanProliferate_becomesProliferative() {
        f.means.put("PDGF", 0.25);
        doReturn(0.3).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.PROLIFERATIVE, cell.getState());
    }
}
