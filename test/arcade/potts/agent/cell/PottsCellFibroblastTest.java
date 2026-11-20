package arcade.potts.agent.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import sim.util.Bag;
import arcade.core.agent.cell.CellFactory;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.env.location.PottsLocation;
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
    public void canProliferate_quiescentGfAboveThreshold_returnsTrue() {
        f.means.put("GF", 0.25);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_quiescentGfAtThreshold_returnsFalse() {
        f.means.put("GF", 0.2);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeGfAtHalfThreshold_returnsTrue() {
        f.means.put("GF", 0.1);
        assertTrue(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeGfBelowHalfThreshold_returnsFalse() {
        f.means.put("GF", 0.09);
        assertFalse(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_quiescentWoundMatrixAboveGate_returnsTrue() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.5);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_quiescentWoundMatrixAtGate_returnsFalse() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.4);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_quiescentNoWoundMatrix_returnsFalse() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeWoundMatrixAtHalfGate_returnsTrue() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.2);
        assertTrue(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeWoundMatrixBelowHalfGate_returnsFalse() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.19);
        assertFalse(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_woundMatrixGateWithLowGf_returnsFalse() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 0.1);
        f.means.put("WOUND_MATRIX", 1.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_noWoundMatrixGate_doesNotReadWoundMatrix() {
        f.means.put("GF", 1.0);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
        verify(f.sim, never()).getLattice("WOUND_MATRIX");
    }

    @Test
    public void constructor_negativeWoundMatrixProliferation_throws() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", -1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void canProliferate_contactInhibited_returnsFalse() {
        f.box.put("CONTACT_FREE_FRACTION", 0.5);
        f.means.put("GF", 1.0);
        f.surround(8);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void stepRules_quiescentCanProliferate_becomesProliferative() {
        f.means.put("GF", 0.25);
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
    public void stepRules_proliferativeLowGf_staysProliferative() {
        PottsCellFibroblast cell = make(State.PROLIFERATIVE);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void stepRules_spontaneousSenescence_becomesSenescent() {
        f.box.put("SPONTANEOUS_SENESCENCE", 0.5);
        f.means.put("GF", 0.25);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void stepRules_noTarget_doesNotDifferentiate() {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.means.put("GF", 1.0);
        f.means.put("ECM", 1.0);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        verify(f.grid, never()).removeObject(any(), any());
    }

    @Test
    public void stepRules_belowDifferentiationThresholds_doesNotDifferentiate() {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.box.put("DIFFERENTIATION_TARGET", "MYO");
        f.means.put("GF", 1.0);
        f.means.put("ECM", 0.3);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.stepRules(f.random, f.sim));
        verify(f.grid, never()).removeObject(any(), any());
    }

    @Test
    public void stepRules_aboveDifferentiationThresholds_replacesCell() {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.box.put("DIFFERENTIATION_TARGET", "MYO");
        f.means.put("GF", 1.0);
        f.means.put("ECM", 1.0);
        PottsCellFibroblast cell = make(State.PROLIFERATIVE);
        setUpDifferentiation(cell);

        assertFalse(cell.stepRules(f.random, f.sim));

        verify(f.grid).removeObject(cell, f.location);
        verify(f.grid).addObject(any(PottsCellMyofibroblast.class), isNull());
    }

    // Sets up a quiescent fibroblast above the GF and ECM thresholds, with a wound matrix gate of
    // 0.5.
    private PottsCellFibroblast makeMatrixGated(double matrix) {
        f.box.put("DIFFERENTIATION_RATE", 0.5);
        f.box.put("DIFFERENTIATION_TARGET", "MYO");
        f.box.put("WOUND_MATRIX_DIFFERENTIATION", 0.5);
        f.means.put("GF", 1.0);
        f.means.put("ECM", 1.0);
        f.means.put("WOUND_MATRIX", matrix);
        PottsCellFibroblast cell = make(State.QUIESCENT);
        setUpDifferentiation(cell);
        return cell;
    }

    @Test
    public void stepRules_woundMatrixAboveGate_replacesCell() {
        PottsCellFibroblast cell = makeMatrixGated(0.6);
        assertFalse(cell.stepRules(f.random, f.sim));
        verify(f.grid).removeObject(cell, f.location);
    }

    @Test
    public void stepRules_woundMatrixAtGate_doesNotDifferentiate() {
        PottsCellFibroblast cell = makeMatrixGated(0.5);
        assertTrue(cell.stepRules(f.random, f.sim));
        verify(f.grid, never()).removeObject(any(), any());
    }

    @Test
    public void stepRules_noWoundMatrix_doesNotDifferentiate() {
        PottsCellFibroblast cell = makeMatrixGated(0);
        assertTrue(cell.stepRules(f.random, f.sim));
        verify(f.grid, never()).removeObject(any(), any());
    }

    @Test
    public void hasWoundMatrix_noGate_doesNotReadWoundMatrix() {
        PottsCellFibroblast cell = make(State.QUIESCENT);
        assertTrue(cell.hasWoundMatrix(f.sim));
        verify(f.sim, never()).getLattice("WOUND_MATRIX");
    }

    @Test
    public void constructor_negativeWoundMatrixGate_throws() {
        f.box.put("WOUND_MATRIX_DIFFERENTIATION", -1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
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

    @Test
    public void canProliferate_quiescentHypoxic_returnsFalse() {
        f.box.put("O2_PROLIF_MIN", 2.0);
        f.means.put("GF", 1.0);
        f.means.put("O2", 1.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeHypoxic_returnsFalse() {
        f.box.put("O2_PROLIF_MIN", 2.0);
        f.means.put("GF", 1.0);
        f.means.put("O2", 1.0);
        assertFalse(make(State.PROLIFERATIVE).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_enoughOxygen_returnsTrue() {
        f.box.put("O2_PROLIF_MIN", 2.0);
        f.means.put("GF", 1.0);
        f.means.put("O2", 3.0);
        assertTrue(make(State.QUIESCENT).canProliferate(f.sim));
    }

    // Sets up a fibroblast after the clot is gone, in new tissue, with the repopulation rule on and
    // the cell centroid at (x, y); others are centroids of other cells in the grid.
    private PottsCellFibroblast makeRepopulating(
            State state, double density, double x, double y, double[]... others) {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.box.put("REPOPULATION_DENSITY", density);
        f.means.put("GF", 1.0);
        f.means.put("WOUND_MATRIX", 0.0);
        f.means.put("MATURE_ECM", 0.0);
        PottsCellFibroblast cell = make(state);
        doReturn(new double[] {x, y, 0}).when(f.location).getCentroid();
        Bag bag = new Bag();
        bag.add(cell);
        for (double[] o : others) {
            PottsLocation loc = mock(PottsLocation.class);
            doReturn(new double[] {o[0], o[1], 0}).when(loc).getCentroid();
            PottsCell other = mock(PottsCell.class);
            doReturn(loc).when(other).getLocation();
            bag.add(other);
        }
        doReturn(bag).when(f.grid).getAllObjects();
        return cell;
    }

    @Test
    public void canProliferate_repopulationOff_doesNotReadGrid() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.means.put("GF", 1.0);
        assertFalse(make(State.QUIESCENT).canProliferate(f.sim));
        verify(f.grid, never()).getAllObjects();
    }

    @Test
    public void canProliferate_clotGoneNewTissueLowDensity_returnsTrue() {
        assertTrue(makeRepopulating(State.QUIESCENT, 0.2, 2, 2).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_proliferativeClotGoneLowDensity_returnsTrue() {
        assertTrue(makeRepopulating(State.PROLIFERATIVE, 0.2, 2, 2).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_clotGoneHighDensity_returnsFalse() {
        double[] a = {1, 2};
        double[] b = {3, 3};
        assertFalse(makeRepopulating(State.QUIESCENT, 0.2, 2, 2, a, b).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_clotGoneFarCells_notCounted() {
        double[] far = {10, 10};
        assertTrue(makeRepopulating(State.QUIESCENT, 0.2, 2, 2, far, far).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_clotGoneMatureTissue_returnsFalse() {
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 2, 2);
        f.means.put("MATURE_ECM", 0.5);
        assertFalse(cell.canProliferate(f.sim));
        verify(f.grid, never()).getAllObjects();
    }

    @Test
    public void canProliferate_clotGoneLowGf_returnsFalse() {
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 2, 2);
        f.means.put("GF", 0.2);
        assertFalse(cell.canProliferate(f.sim));
    }

    @Test
    public void canProliferate_clotGoneContactInhibited_returnsFalse() {
        f.box.put("CONTACT_FREE_FRACTION", 0.5);
        f.surround(8);
        assertFalse(makeRepopulating(State.QUIESCENT, 0.2, 2, 2).canProliferate(f.sim));
    }

    @Test
    public void canProliferate_clotPresent_doesNotReadGrid() {
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 2, 2);
        f.means.put("WOUND_MATRIX", 1.0);
        assertTrue(cell.canProliferate(f.sim));
        verify(f.grid, never()).getAllObjects();
    }

    @Test
    public void canProliferate_noMatureEcmLayer_throws() {
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 2, 2);
        doReturn(null).when(f.sim).getLattice("MATURE_ECM");
        assertThrows(IllegalStateException.class, () -> cell.canProliferate(f.sim));
    }

    @Test
    public void getLocalDensity_center_dividesByDiscArea() {
        // Radius 1.5 around (2, 2) covers all 9 voxels of the 3 x 3 domain.
        double[] a = {1, 1};
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 2, 2, a);
        assertEquals(2.0 / 9, cell.getLocalDensity(f.sim), 1e-12);
    }

    @Test
    public void getLocalDensity_corner_dividesByAreaInsideDomain() {
        // Radius 1.5 around (1, 1) covers 4 voxels inside the domain.
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 1, 1);
        assertEquals(0.25, cell.getLocalDensity(f.sim), 1e-12);
    }

    @Test
    public void getLocalDensity_vesselCell_notCountedAndVoxelsLeftOut() {
        // A vessel cell with centroid (1, 1) owns voxels (1, 1) and (1, 2): it is not counted and
        // its 2 voxels are left out of the 9-voxel disc around (2, 2).
        double[] a = {3, 3};
        PottsCellFibroblast cell = makeRepopulating(State.QUIESCENT, 0.2, 2, 2, a);
        PottsLocation loc = mock(PottsLocation.class);
        doReturn(new double[] {1, 1, 0}).when(loc).getCentroid();
        PottsCellVessel vessel = mock(PottsCellVessel.class);
        doReturn(loc).when(vessel).getLocation();
        f.grid.getAllObjects().add(vessel);
        f.cells.put(9, vessel);
        f.ids[0][1][1] = 9;
        f.ids[0][1][2] = 9;
        assertEquals(2.0 / 7, cell.getLocalDensity(f.sim), 1e-12);
    }

    @Test
    public void constructor_negativeRepopulationDensity_throws() {
        f.box.put("REPOPULATION_DENSITY", -1);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_zeroRepopulationRadius_throws() {
        f.box.put("WOUND_MATRIX_PROLIFERATION", 0.4);
        f.box.put("REPOPULATION_DENSITY", 0.2);
        f.box.put("REPOPULATION_RADIUS", 0);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }

    @Test
    public void constructor_repopulationWithoutClotGate_throws() {
        f.box.put("REPOPULATION_DENSITY", 0.2);
        assertThrows(IllegalArgumentException.class, () -> make(State.QUIESCENT));
    }
}
