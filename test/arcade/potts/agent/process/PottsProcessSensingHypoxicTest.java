package arcade.potts.agent.process;

import java.util.List;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static arcade.potts.agent.process.PottsProcessSecretionTest.makeCell;

public class PottsProcessSensingHypoxicTest {
    private static final double EPSILON = 1E-12;

    // Voxel size [um].
    private static final double DS = 2;

    // Maximum secretion rate [amount/tick].
    private static final double RATE = 8;

    // O2 at half-maximal secretion [mmHg].
    private static final double K = 12;

    // Hill exponent.
    private static final double N = 4;

    // Voxels of the cell.
    private static final Voxel[] VOXELS = {new Voxel(0, 0, 0), new Voxel(1, 0, 0)};

    // Creates sensing parameters.
    static MiniBox sensing(double rate, double k, double n) {
        MiniBox box = new MiniBox();
        box.put("sensing/VEGF_SECRETION_RATE", rate);
        box.put("sensing/O2_HYPOXIA", k);
        box.put("sensing/HYPOXIA_HILL", n);
        return box;
    }

    // Creates a mock simulation with an O2 layer at the given mean and a VEGF layer.
    static PottsSimulation makeSim(Lattice o2, PottsLattice vegf) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(o2).when(sim).getLattice("O2");
        doReturn(vegf).when(sim).getLattice("VEGF");
        return sim;
    }

    static Lattice makeO2(double mean) {
        Lattice o2 = mock(Lattice.class);
        doReturn(mean).when(o2).getAverageValue(any());
        return o2;
    }

    static PottsLattice makeVegf() {
        PottsLattice vegf = mock(PottsLattice.class);
        doReturn(DS).when(vegf).getDs();
        return vegf;
    }

    // Matches a double within EPSILON of the expected value.
    static double near(double expected) {
        return doubleThat(value -> Math.abs(value - expected) < EPSILON);
    }

    static PottsProcessSensingHypoxic makeProcess(double rate) {
        return new PottsProcessSensingHypoxic(makeCell(sensing(rate, K, N), VOXELS));
    }

    @Test
    public void constructor_givenParameters_setsFields() {
        PottsProcessSensingHypoxic process = makeProcess(RATE);
        assertEquals(RATE, process.secretionRate, EPSILON);
        assertEquals(K, process.o2Hypoxia, EPSILON);
        assertEquals(N, process.hill, EPSILON);
    }

    @Test
    public void constructor_negativeRate_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsProcessSensingHypoxic(makeCell(sensing(-1, K, N))));
    }

    @Test
    public void constructor_nonPositiveHalfSaturation_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsProcessSensingHypoxic(makeCell(sensing(RATE, 0, N))));
    }

    @Test
    public void constructor_nonPositiveHill_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsProcessSensingHypoxic(makeCell(sensing(RATE, K, 0))));
    }

    @Test
    public void constructor_missingParameters_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsProcessSensingHypoxic(makeCell(new MiniBox())));
    }

    @Test
    public void hypoxicFraction_atHalfSaturation_returnsHalf() {
        assertEquals(0.5, makeProcess(RATE).hypoxicFraction(K), EPSILON);
    }

    @Test
    public void hypoxicFraction_noOxygen_returnsOne() {
        assertEquals(1, makeProcess(RATE).hypoxicFraction(0), EPSILON);
    }

    @Test
    public void hypoxicFraction_highOxygen_returnsNearZero() {
        // (10 K)^4 gives 1 / (1 + 10^4).
        assertEquals(1 / (1 + 1E4), makeProcess(RATE).hypoxicFraction(10 * K), EPSILON);
    }

    @Test
    public void hypoxicFraction_givenOxygen_decreasesWithOxygen() {
        PottsProcessSensingHypoxic process = makeProcess(RATE);
        double previous = 1;
        for (double o2 = 1; o2 < 60; o2 += 1) {
            double fraction = process.hypoxicFraction(o2);
            assertTrue(fraction < previous);
            previous = fraction;
        }
    }

    @Test
    public void step_atHalfSaturation_addsHalfRateOverCellVoxels() {
        PottsLattice vegf = makeVegf();
        makeProcess(RATE).step(mock(MersenneTwisterFast.class), makeSim(makeO2(K), vegf));
        // Half of RATE spread over 2 voxels of volume DS^3.
        verify(vegf).addDelta(eq(List.of(VOXELS)), near(RATE / 2 / (2 * DS * DS * DS)));
    }

    @Test
    public void step_noOxygen_addsFullRateOverCellVoxels() {
        PottsLattice vegf = makeVegf();
        makeProcess(RATE).step(mock(MersenneTwisterFast.class), makeSim(makeO2(0), vegf));
        verify(vegf).addDelta(eq(List.of(VOXELS)), near(RATE / (2 * DS * DS * DS)));
    }

    @Test
    public void step_zeroRate_addsNothingAndNeedsNoLayers() {
        PottsSimulation sim = mock(PottsSimulation.class);
        makeProcess(0).step(mock(MersenneTwisterFast.class), sim);
        verify(sim, never()).getLattice(any());
    }

    @Test
    public void step_noOxygenLayer_throws() {
        PottsSimulation sim = makeSim(null, makeVegf());
        assertThrows(
                IllegalStateException.class,
                () -> makeProcess(RATE).step(mock(MersenneTwisterFast.class), sim));
    }

    @Test
    public void step_noVegfLayer_throws() {
        PottsSimulation sim = makeSim(makeO2(0), null);
        assertThrows(
                IllegalStateException.class,
                () -> makeProcess(RATE).step(mock(MersenneTwisterFast.class), sim));
    }
}
