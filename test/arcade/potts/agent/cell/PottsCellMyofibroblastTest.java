package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
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
    public void canProliferate_quiescentGfAboveThreshold_returnsTrue() {
        f.means.put("GF", 0.25);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeGfBelowHalfThreshold_returnsFalse() {
        f.means.put("GF", 0.09);
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
        f.means.put("GF", 0.25);
        doReturn(0.3).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    // Creates a mature quiescent myofibroblast that always becomes senescent once allowed, with
    // the senescence window opening at tick 10 and the simulation at the given tick.
    private PottsCellMyofibroblast makeGated(double time) {
        f.box.put("SENESCENCE_RATE", 0.5);
        f.box.put("SENESCENCE_START", 10);
        Schedule schedule = f.sim.getSchedule();
        doReturn(time).when(schedule).getTime();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        return cell;
    }

    @Test
    public void stepRules_matureBeforeSenescenceStart_doesNotBecomeSenescent() {
        PottsCellMyofibroblast cell = makeGated(9);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_matureAtSenescenceStart_becomesSenescent() {
        PottsCellMyofibroblast cell = makeGated(10);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_matureAfterSenescenceStart_becomesSenescent() {
        PottsCellMyofibroblast cell = makeGated(30);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_beforeSenescenceStart_drawsOnlyForMaturation() {
        // Immature cell before the start: one draw (maturation), none for senescence.
        PottsCellMyofibroblast cell = makeGated(0);
        cell.mature = false;
        cell.stepRules(f.random, f.sim);
        assertTrue(cell.mature);
        assertEquals(State.QUIESCENT, cell.getState());
        verify(f.random, times(1)).nextDouble();
    }

    @Test
    public void stepRules_lowEcmBeforeSenescenceStart_becomesApoptotic() {
        f.means.put("ECM", 0.05);
        PottsCellMyofibroblast cell = makeGated(0);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void isSenescenceOpen_noStart_doesNotReadTime() {
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.isSenescenceOpen(f.sim));
        verify(f.sim, never()).getSchedule();
    }

    // Creates a mature quiescent myofibroblast below the ECM threshold with the given tension
    // apoptosis rate and the given draw.
    private PottsCellMyofibroblast makeLowEcm(double rate, double draw) {
        f.means.put("ECM", 0.05);
        f.box.put("TENSION_APOPTOSIS", rate);
        doReturn(draw).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        return cell;
    }

    @Test
    public void stepRules_lowEcmZeroTensionRate_becomesApoptoticWithoutDraw() {
        PottsCellMyofibroblast cell = makeLowEcm(0, 0.99);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.APOPTOTIC, cell.getState());
        verify(f.random, never()).nextDouble();
    }

    @Test
    public void stepRules_lowEcmDrawBelowTensionRate_becomesApoptotic() {
        PottsCellMyofibroblast cell = makeLowEcm(0.3, 0.2);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_lowEcmDrawAboveTensionRate_survives() {
        PottsCellMyofibroblast cell = makeLowEcm(0.3, 0.4);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_lowEcmSurvivor_doesNotBecomeSenescent() {
        f.box.put("SENESCENCE_RATE", 0.5);
        PottsCellMyofibroblast cell = makeLowEcm(0.3, 0.4);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_highEcmWithTensionRate_doesNotBecomeApoptotic() {
        f.box.put("TENSION_APOPTOSIS", 0.3);
        doReturn(0.2).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_lowEcmTensionRate_independentOfSenescenceRate() {
        f.box.put("SENESCENCE_RATE", 0.9);
        PottsCellMyofibroblast cell = makeLowEcm(0.3, 0.2);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void constructor_negativeTensionApoptosis_throws() {
        f.box.put("TENSION_APOPTOSIS", -0.1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_negativeSenescenceStart_throws() {
        f.box.put("SENESCENCE_START", -1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void canProliferate_hypoxic_returnsFalse() {
        f.box.put("O2_PROLIF_MIN", 2.0);
        f.means.put("GF", 1.0);
        f.means.put("O2", 1.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }
}
