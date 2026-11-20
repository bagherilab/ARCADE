package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import arcade.core.agent.cell.CellFactory;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellFibroblastTest {
    private PottsCellWoundHealingFixture f;

    @BeforeEach
    public final void setUp() {
        f = new PottsCellWoundHealingFixture();
    }

    // Creates a fibroblast in the given state.
    private PottsCellFibroblast make(State state) {
        return new PottsCellFibroblast(f.container(state), f.location, f.parameters(), null);
    }

    @Test
    public void canProliferate_quiescentPdgfAboveThreshold_returnsTrue() {
        f.means.put("PDGF", 0.25);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_quiescentPdgfAtThreshold_returnsFalse() {
        f.means.put("PDGF", 0.2);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativePdgfAtHalfThreshold_returnsTrue() {
        f.means.put("PDGF", 0.1);
        assertTrue(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativePdgfBelowHalfThreshold_returnsFalse() {
        f.means.put("PDGF", 0.09);
        assertFalse(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_contactInhibited_returnsFalse() {
        f.box.put("CONTACT_FREE_FRACTION", 0.5);
        f.means.put("PDGF", 1.0);
        f.surround(8);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void stepRules_quiescentCanProliferate_becomesProliferative() {
        f.means.put("PDGF", 0.25);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void stepRules_quiescentCannotProliferate_staysQuiescent() {
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void stepRules_proliferativeLowPdgf_staysProliferative() {
        PottsCellFibroblast cell = make(State.PROLIFERATIVE);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void stepRules_spontaneousSenescence_becomesSenescent() {
        f.box.put("SPONTANEOUS_SENESCENCE", 0.5);
        f.means.put("PDGF", 0.25);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_noTarget_doesNotDifferentiate() {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.means.put("PDGF", 1.0);
        f.means.put("ECM", 1.0);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        verify(f.grid, never()).removeObject(any(), any());
    }

    @Test
    public void stepRules_belowDifferentiationThresholds_doesNotDifferentiate() {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.box.put("DIFFERENTIATION_TARGET", "MYO");
        f.means.put("PDGF", 1.0);
        f.means.put("ECM", 0.3);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        verify(f.grid, never()).removeObject(any(), any());
    }

    @Test
    public void stepRules_aboveDifferentiationThresholds_replacesCell() {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.box.put("DIFFERENTIATION_TARGET", "MYO");
        f.means.put("PDGF", 1.0);
        f.means.put("ECM", 1.0);
        PottsCellFibroblast cell = make(State.PROLIFERATIVE);
        setUpDifferentiation(cell);

        assertFalse(cell.stepRules(f.random, f.sim));

        verify(f.grid).removeObject(cell, f.location);
        verify(f.grid).addObject(any(PottsCellMyofibroblast.class), isNull());
    }

    @Test
    public void differentiate_givenTarget_replacesWithMyofibroblast() {
        f.box.put("DIFFERENTIATION_TARGET", "MYO");
        PottsCellFibroblast cell = make(State.PROLIFERATIVE);
        setUpDifferentiation(cell);

        cell.differentiate(f.random, f.sim);

        verify(f.grid).removeObject(cell, f.location);
        verify(f.potts).deregister(cell);
        verify(cell.stopper).stop();

        ArgumentCaptor<Object> added = ArgumentCaptor.forClass(Object.class);
        verify(f.grid).addObject(added.capture(), isNull());
        PottsCellMyofibroblast newCell = (PottsCellMyofibroblast) added.getValue();
        assertEquals(PottsCellWoundHealingFixture.ID, newCell.getID());
        assertEquals(1, newCell.getParent());
        assertEquals(2, newCell.getPop());
        assertEquals(10, newCell.getAge());
        assertEquals(2, newCell.getDivisions());
        assertEquals(State.PROLIFERATIVE, newCell.getState());
        assertSame(f.location, newCell.getLocation());
        verify(f.potts).register(newCell);
        verify(f.sim.getSchedule()).scheduleRepeating(newCell, Ordering.CELLS.ordinal(), 1);
        assertEquals(Phase.PROLIFERATIVE_G1, ((PottsModule) newCell.getModule()).getPhase());
        verify(f.location).update(PottsCellWoundHealingFixture.ID, f.potts.ids, f.potts.regions);
    }

    // Sets up the target population, factory, and schedule needed for differentiation.
    private void setUpDifferentiation(PottsCellFibroblast cell) {
        MiniBox target = PottsCellWoundHealingFixture.defaults();
        target.put("CODE", 2);
        target.put("CLASS", "myofibroblast");
        f.series.populations.put("MYO", target);

        CellFactory factory = mock(CellFactory.class);
        doReturn(target).when(factory).getParameters(2);
        doReturn(factory).when(f.sim).getCellFactory();

        Schedule schedule = f.sim.getSchedule();
        doReturn(mock(Stoppable.class))
                .when(schedule)
                .scheduleRepeating(any(), anyInt(), anyDouble());
        cell.stopper = mock(Stoppable.class);
    }
}
