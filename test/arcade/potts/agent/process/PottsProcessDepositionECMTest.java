package arcade.potts.agent.process;

import java.util.List;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static arcade.potts.agent.process.PottsProcessSecretionTest.makeCell;
import static arcade.potts.util.PottsEnums.State;

public class PottsProcessDepositionECMTest {
    private static final double EPSILON = 1E-12;

    // Voxel size [um].
    private static final double DS = 2;

    // Deposition rate [amount/tick].
    private static final double RATE = 8;

    // Threshold on mean GF.
    private static final double THRESHOLD = 0.5;

    // O2 at half-maximal deposition [mmHg].
    private static final double K = 25;

    // Voxels of the cell.
    private static final Voxel[] VOXELS = {new Voxel(0, 0, 0), new Voxel(1, 0, 0)};

    // Volume of the cell voxels.
    private static final double VOLUME = 2 * DS * DS * DS;

    // Creates deposition parameters.
    static MiniBox deposition(double rate, double threshold, double k) {
        MiniBox box = new MiniBox();
        box.put("deposition/RATE", rate);
        box.put("deposition/GF_THRESHOLD", threshold);
        box.put("deposition/K_O2", k);
        return box;
    }

    // Creates a mock lattice with the given mean.
    static Lattice makeLayer(double mean) {
        Lattice lattice = mock(Lattice.class);
        doReturn(mean).when(lattice).getAverageValue(any());
        return lattice;
    }

    // Creates a mock ECM lattice.
    static PottsLattice makeEcm() {
        PottsLattice ecm = mock(PottsLattice.class);
        doReturn(DS).when(ecm).getDs();
        return ecm;
    }

    // Creates a mock simulation with the given layers (null for a missing layer).
    static PottsSimulation makeSim(Lattice gf, Lattice o2, PottsLattice ecm) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(gf).when(sim).getLattice("GF");
        doReturn(o2).when(sim).getLattice("O2");
        doReturn(ecm).when(sim).getLattice("ECM");
        return sim;
    }

    // Creates a process for a cell in the given state.
    static PottsProcessDepositionECM makeProcess(MiniBox box, State state) {
        PottsCell cell = makeCell(box, VOXELS);
        doReturn(state).when(cell).getState();
        return new PottsProcessDepositionECM(cell);
    }

    // Matches a double within EPSILON of the expected value.
    static double near(double expected) {
        return doubleThat(value -> Math.abs(value - expected) < EPSILON);
    }

    // Steps the process once.
    static void step(PottsProcessDepositionECM process, PottsSimulation sim) {
        process.step(mock(MersenneTwisterFast.class), sim);
    }

    @Test
    public void constructor_givenParameters_setsFields() {
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT);
        assertEquals(RATE, process.rate, EPSILON);
        assertEquals(THRESHOLD, process.gfThreshold, EPSILON);
        assertEquals(K, process.kO2, EPSILON);
    }

    @Test
    public void constructor_negativeRate_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> makeProcess(deposition(-1, THRESHOLD, K), State.QUIESCENT));
    }

    @Test
    public void constructor_negativeThreshold_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> makeProcess(deposition(RATE, -1, K), State.QUIESCENT));
    }

    @Test
    public void constructor_negativeHalfSaturation_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> makeProcess(deposition(RATE, THRESHOLD, -1), State.QUIESCENT));
    }

    @Test
    public void constructor_missingParameters_throws() {
        assertThrows(
                IllegalArgumentException.class, () -> makeProcess(new MiniBox(), State.QUIESCENT));
    }

    @Test
    public void oxygenFraction_atHalfSaturation_returnsHalf() {
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT);
        assertEquals(0.5, process.oxygenFraction(K), EPSILON);
    }

    @Test
    public void oxygenFraction_noOxygen_returnsZero() {
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT);
        assertEquals(0, process.oxygenFraction(0), EPSILON);
        assertEquals(0, process.oxygenFraction(-1), EPSILON);
    }

    @Test
    public void oxygenFraction_noLimit_returnsOne() {
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, 0), State.QUIESCENT);
        assertEquals(1, process.oxygenFraction(0), EPSILON);
        assertEquals(1, process.oxygenFraction(100), EPSILON);
    }

    @Test
    public void step_gfAboveThreshold_depositsRateOverCellVoxels() {
        PottsLattice ecm = makeEcm();
        PottsSimulation sim = makeSim(makeLayer(THRESHOLD + 0.1), null, ecm);
        step(makeProcess(deposition(RATE, THRESHOLD, 0), State.QUIESCENT), sim);
        verify(ecm).addDelta(eq(List.of(VOXELS)), near(RATE / VOLUME));
    }

    @Test
    public void step_proliferativeCell_deposits() {
        PottsLattice ecm = makeEcm();
        PottsSimulation sim = makeSim(makeLayer(THRESHOLD + 0.1), null, ecm);
        step(makeProcess(deposition(RATE, THRESHOLD, 0), State.PROLIFERATIVE), sim);
        verify(ecm).addDelta(eq(List.of(VOXELS)), near(RATE / VOLUME));
    }

    @Test
    public void step_gfAtThreshold_depositsNothing() {
        PottsLattice ecm = makeEcm();
        PottsSimulation sim = makeSim(makeLayer(THRESHOLD), null, ecm);
        step(makeProcess(deposition(RATE, THRESHOLD, 0), State.QUIESCENT), sim);
        verify(ecm, never()).addDelta(any(), anyDouble());
    }

    @Test
    public void step_noGf_depositsNothing() {
        PottsLattice ecm = makeEcm();
        PottsSimulation sim = makeSim(makeLayer(0), null, ecm);
        step(makeProcess(deposition(RATE, 0, 0), State.QUIESCENT), sim);
        verify(ecm, never()).addDelta(any(), anyDouble());
    }

    @Test
    public void step_oxygenAtHalfSaturation_depositsHalfRate() {
        PottsLattice ecm = makeEcm();
        PottsSimulation sim = makeSim(makeLayer(1), makeLayer(K), ecm);
        step(makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT), sim);
        verify(ecm).addDelta(eq(List.of(VOXELS)), near(RATE / 2 / VOLUME));
    }

    @Test
    public void step_noOxygenLimit_needsNoOxygenLayer() {
        PottsLattice ecm = makeEcm();
        PottsSimulation sim = makeSim(makeLayer(1), null, ecm);
        step(makeProcess(deposition(RATE, THRESHOLD, 0), State.QUIESCENT), sim);
        verify(sim, never()).getLattice("O2");
        verify(ecm).addDelta(eq(List.of(VOXELS)), near(RATE / VOLUME));
    }

    @Test
    public void step_notActivatedStates_depositNothing() {
        for (State state :
                new State[] {State.SENESCENT, State.APOPTOTIC, State.NECROTIC, State.AUTOTIC}) {
            PottsLattice ecm = makeEcm();
            PottsSimulation sim = makeSim(makeLayer(1), makeLayer(K), ecm);
            step(makeProcess(deposition(RATE, THRESHOLD, K), state), sim);
            verify(ecm, never()).addDelta(any(), anyDouble());
        }
    }

    @Test
    public void step_zeroRate_readsNoLayers() {
        PottsSimulation sim = mock(PottsSimulation.class);
        step(makeProcess(deposition(0, THRESHOLD, K), State.QUIESCENT), sim);
        verify(sim, never()).getLattice(any());
    }

    @Test
    public void step_noGfLayer_throws() {
        PottsSimulation sim = makeSim(null, makeLayer(K), makeEcm());
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT);
        assertThrows(IllegalStateException.class, () -> step(process, sim));
    }

    @Test
    public void step_noOxygenLayerWithLimit_throws() {
        PottsSimulation sim = makeSim(makeLayer(1), null, makeEcm());
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT);
        assertThrows(IllegalStateException.class, () -> step(process, sim));
    }

    @Test
    public void step_noEcmLayer_throws() {
        PottsSimulation sim = makeSim(makeLayer(1), makeLayer(K), null);
        PottsProcessDepositionECM process =
                makeProcess(deposition(RATE, THRESHOLD, K), State.QUIESCENT);
        assertThrows(IllegalStateException.class, () -> step(process, sim));
    }
}
