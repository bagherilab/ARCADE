package arcade.potts.agent.process;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.agent.process.PottsProcessSecretionTest.makeCell;
import static arcade.potts.util.PottsEnums.State;

public class PottsProcessClearanceTest {
    private static final double EPSILON = 1E-12;

    // Clearance rate [1/tick].
    private static final double RATE = 0.1;

    // Lattice size [voxels].
    private static final int SIZE = 4;

    // Voxels of the cell.
    private static final Voxel[] VOXELS = {new Voxel(1, 1, 0), new Voxel(1, 2, 0)};

    // Creates clearance parameters.
    static MiniBox clearance(double rate) {
        MiniBox box = new MiniBox();
        box.put("clearance/RATE", rate);
        return box;
    }

    // Creates a process for a cell in the given state.
    static PottsProcessClearance makeProcess(double rate, State state) {
        PottsCell cell = makeCell(clearance(rate), VOXELS);
        doReturn(state).when(cell).getState();
        return new PottsProcessClearance(cell);
    }

    // Creates a one-plane field filled with a value.
    static double[][][] makeField(double value) {
        double[][][] field = new double[1][SIZE][SIZE];
        for (double[] row : field[0]) {
            Arrays.fill(row, value);
        }
        return field;
    }

    // Creates a mock simulation whose WOUND_MATRIX layer has the given field (null for none).
    static PottsSimulation makeSim(double[][][] field) {
        PottsSimulation sim = mock(PottsSimulation.class);
        if (field != null) {
            Lattice lattice = mock(Lattice.class);
            doReturn(field).when(lattice).getField();
            doReturn(lattice).when(sim).getLattice("WOUND_MATRIX");
        }
        return sim;
    }

    // Steps the process once.
    static void step(PottsProcessClearance process, PottsSimulation sim) {
        process.step(mock(MersenneTwisterFast.class), sim);
    }

    // Checks whether a voxel is one of the cell's voxels.
    static boolean isCellVoxel(int x, int y) {
        for (Voxel v : VOXELS) {
            if (v.x == x && v.y == y) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void constructor_givenRate_setsRate() {
        assertEquals(RATE, makeProcess(RATE, State.QUIESCENT).rate, EPSILON);
    }

    @Test
    public void constructor_negativeRate_throws() {
        assertThrows(IllegalArgumentException.class, () -> makeProcess(-1, State.QUIESCENT));
    }

    @Test
    public void step_clearsOnlyOwnVoxels() {
        double[][][] field = makeField(1.0);
        step(makeProcess(RATE, State.QUIESCENT), makeSim(field));
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                double expected = isCellVoxel(x, y) ? Math.exp(-RATE) : 1.0;
                assertEquals(expected, field[0][x][y], EPSILON, "voxel " + x + "," + y);
            }
        }
    }

    @Test
    public void step_twoTicks_compounds() {
        double[][][] field = makeField(1.0);
        PottsProcessClearance process = makeProcess(RATE, State.QUIESCENT);
        PottsSimulation sim = makeSim(field);
        step(process, sim);
        step(process, sim);
        assertEquals(Math.exp(-2 * RATE), field[0][1][1], EPSILON);
    }

    @Test
    public void step_proliferative_clears() {
        double[][][] field = makeField(1.0);
        step(makeProcess(RATE, State.PROLIFERATIVE), makeSim(field));
        assertEquals(Math.exp(-RATE), field[0][1][2], EPSILON);
    }

    @Test
    public void step_senescentOrApoptotic_noChange() {
        for (State state : new State[] {State.SENESCENT, State.APOPTOTIC}) {
            double[][][] field = makeField(1.0);
            step(makeProcess(RATE, state), makeSim(field));
            assertEquals(1.0, field[0][1][1], EPSILON, state.name());
        }
    }

    @Test
    public void step_noLayer_noOp() {
        assertDoesNotThrow(() -> step(makeProcess(RATE, State.QUIESCENT), makeSim(null)));
    }

    @Test
    public void step_zeroField_staysZero() {
        double[][][] field = makeField(0.0);
        step(makeProcess(RATE, State.QUIESCENT), makeSim(field));
        assertEquals(0.0, field[0][1][1]);
    }

    @Test
    public void step_zeroRate_noChange() {
        double[][][] field = makeField(0.7);
        step(makeProcess(0, State.QUIESCENT), makeSim(field));
        assertEquals(0.7, field[0][1][1]);
    }
}
