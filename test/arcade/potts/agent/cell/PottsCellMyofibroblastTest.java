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
    public void canProliferate_woundMatrixBelowGate_returnsFalse() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.1);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_woundMatrixAboveGate_returnsTrue() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.5);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void constructor_negativeWoundMatrixProliferation_throws() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", -1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
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

    @Test
    public void constructor_negativeGfSurvival_throws() {
        f.box.put("GF_SURVIVAL", -0.1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_negativeGfApoptosis_throws() {
        f.box.put("GF_APOPTOSIS", -0.1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_invalidBystanderMature_throws() {
        f.box.put("BYSTANDER_MATURE", 2);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    // Creates a mature quiescent myofibroblast with GF below a survival threshold of 0.5, the
    // given GF apoptosis rate and the given draw.
    private PottsCellMyofibroblast makeLowGf(double rate, double draw) {
        f.means.put("GF", 0.1);
        f.box.put("GF_SURVIVAL", 0.5);
        f.box.put("GF_APOPTOSIS", rate);
        doReturn(draw).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        return cell;
    }

    @Test
    public void stepRules_lowGfZeroRate_becomesApoptoticWithoutDraw() {
        PottsCellMyofibroblast cell = makeLowGf(0, 0.99);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.APOPTOTIC, cell.getState());
        verify(f.random, never()).nextDouble();
    }

    @Test
    public void stepRules_lowGfDrawBelowRate_becomesApoptotic() {
        PottsCellMyofibroblast cell = makeLowGf(0.3, 0.2);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_lowGfSurvivor_doesNotBecomeSenescent() {
        f.box.put("SENESCENCE_RATE", 0.5);
        PottsCellMyofibroblast cell = makeLowGf(0.3, 0.4);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_lowGfSurvivorLowEcm_skipsTensionCheck() {
        f.means.put("ECM", 0.05);
        PottsCellMyofibroblast cell = makeLowGf(0.3, 0.4);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_gfAboveSurvival_doesNotBecomeApoptotic() {
        f.means.put("GF", 0.6);
        f.box.put("GF_SURVIVAL", 0.5);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        cell.stepRules(f.random, f.sim);
        assertNotEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_zeroGfSurvival_doesNotCheckGf() {
        f.means.put("GF", 0.0);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        cell.stepRules(f.random, f.sim);
        assertNotEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_immatureLowGf_doesNotBecomeApoptotic() {
        f.means.put("GF", 0.1);
        f.box.put("GF_SURVIVAL", 0.5);
        doReturn(0.3).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    // Sets up paracrine bystander conditions (INF above the bystander threshold, below apoptosis).
    private void setParacrine(int matureOnly) {
        f.box.put("BYSTANDER_PARACRINE", 0.5);
        f.box.put("INF_BYSTANDER", 1);
        f.means.put("INF", 2.0);
        f.box.put("BYSTANDER_MATURE", matureOnly);
    }

    @Test
    public void stepRules_bystanderAllCellsImmature_becomesSenescent() {
        setParacrine(0);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
        assertFalse(cell.mature);
    }

    @Test
    public void stepRules_bystanderMatureOnlyImmature_doesNotBecomeSenescent() {
        setParacrine(1);
        doReturn(0.3).when(f.random).nextDouble();
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.stepRules(f.random, f.sim);
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_bystanderMatureOnlyMature_becomesSenescent() {
        setParacrine(1);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_bystanderMatureOnlyLowEcm_becomesApoptotic() {
        setParacrine(1);
        f.means.put("ECM", 0.05);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        cell.stepRules(f.random, f.sim);
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void stepRules_bystanderMatureOnlyLowGf_becomesApoptotic() {
        setParacrine(1);
        f.means.put("GF", 0.1);
        f.box.put("GF_SURVIVAL", 0.5);
        PottsCellMyofibroblast cell = make(State.QUIESCENT);
        cell.mature = true;
        cell.stepRules(f.random, f.sim);
        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void canProliferate_repopulationParameterSet_clotGoneReturnsFalse() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.box.put("REPOPULATION_DENSITY", 0.2);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
        verify(f.grid, never()).getAllObjects();
    }
}
