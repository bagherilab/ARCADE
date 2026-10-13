package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

public class ChemotaxisHamiltonianTest {
    private static final double EPSILON = 1E-10;

    // Creates a series with populations A (code 1) and B (code 2) and layers X and Y.
    static PottsSeries makeSeries(double lambdaAX, double lambdaAY, double lambdaBX) {
        PottsSeries series = mock(PottsSeries.class);
        series.populations = new HashMap<>();
        series.layers = new HashMap<>();
        series.potts = new MiniBox();
        String[] pops = {"A", "B"};
        for (int i = 0; i < pops.length; i++) {
            MiniBox population = new MiniBox();
            population.put("CODE", i + 1);
            series.populations.put(pops[i], population);
        }
        series.layers.put("X", new MiniBox());
        series.layers.put("Y", new MiniBox());
        series.potts.put("chemotaxis/LAMBDA_X" + TARGET_SEPARATOR + "A", lambdaAX);
        series.potts.put("chemotaxis/LAMBDA_Y" + TARGET_SEPARATOR + "A", lambdaAY);
        series.potts.put("chemotaxis/LAMBDA_X" + TARGET_SEPARATOR + "B", lambdaBX);
        series.potts.put("chemotaxis/LAMBDA_Y" + TARGET_SEPARATOR + "B", 0.0);
        return series;
    }

    // Creates a mock potts with the given ids array.
    static Potts makePotts(int[][][] ids) {
        Potts potts = mock(Potts.class);
        potts.ids = ids;
        return potts;
    }

    // Creates a mock cell with the given id and population.
    static PottsCell makeCell(int id, int pop) {
        PottsCell cell = mock(PottsCell.class);
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        return cell;
    }

    // Creates a lattice with values a + bx * x + by * y + bz * z.
    static PottsLattice makeLattice(int height, double a, double bx, double by, double bz) {
        PottsLattice lattice = new PottsLattice(7, 7, height, 1, 1, new MiniBox());
        for (int k = 0; k < height; k++) {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 7; j++) {
                    lattice.getField()[k][i][j] = a + bx * i + by * j + bz * k;
                }
            }
        }
        return lattice;
    }

    // Creates a term for the ids array, with cells 1 (population A) and 2 (population B).
    static ChemotaxisHamiltonian makeTerm(
            int[][][] ids, double lambdaAX, double lambdaAY, PottsLattice x, PottsLattice y) {
        ChemotaxisHamiltonian ch =
                new ChemotaxisHamiltonian(makeSeries(lambdaAX, lambdaAY, 0), makePotts(ids));
        ch.register(makeCell(1, 1));
        ch.register(makeCell(2, 2));
        HashMap<String, Lattice> lattices = new HashMap<>();
        lattices.put("X", x);
        lattices.put("Y", y);
        ch.setLattices(lattices);
        return ch;
    }

    // Creates a 2D ids array with cell 1 at (2, 3) and (3, 3).
    static int[][][] makeIds() {
        int[][][] ids = new int[1][7][7];
        ids[0][2][3] = 1;
        ids[0][3][3] = 1;
        return ids;
    }

    @Test
    public void constructor_noPopulations_createsEmptyMaps() {
        ChemotaxisHamiltonian ch =
                new ChemotaxisHamiltonian(mock(PottsSeries.class), makePotts(new int[1][1][1]));
        assertNotNull(ch.configs);
        assertTrue(ch.popToLambdas.isEmpty());
    }

    @Test
    public void constructor_givenSeries_setsLambdasAndIds() {
        int[][][] ids = makeIds();
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(makeSeries(3, -2, 5), makePotts(ids));
        assertEquals(3, ch.popToLambdas.get(1).get("X"), EPSILON);
        assertEquals(-2, ch.popToLambdas.get(1).get("Y"), EPSILON);
        assertEquals(5, ch.popToLambdas.get(2).get("X"), EPSILON);
        assertEquals(0, ch.popToLambdas.get(2).get("Y"), EPSILON);
        assertSame(ids, ch.ids);
    }

    @Test
    public void register_givenCell_addsConfig() {
        ChemotaxisHamiltonian ch =
                new ChemotaxisHamiltonian(makeSeries(3, -2, 5), makePotts(makeIds()));
        ch.register(makeCell(7, 2));
        assertSame(ch.popToLambdas.get(2), ch.configs.get(7));
    }

    @Test
    public void deregister_givenCell_removesConfig() {
        ChemotaxisHamiltonian ch =
                new ChemotaxisHamiltonian(makeSeries(3, -2, 5), makePotts(makeIds()));
        PottsCell cell = makeCell(7, 2);
        ch.register(cell);
        ch.deregister(cell);
        assertFalse(ch.configs.containsKey(7));
    }

    @Test
    public void getDelta_gainVoxelUpGradient_returnsNegative() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), 3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        // Cell 1 gains (4, 3) from (3, 3): c = 8, c' = 6.
        assertEquals(-3 * (8 - 6), ch.getDelta(0, 1, 4, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_gainVoxelDownGradient_returnsPositive() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), 3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        // Cell 1 gains (1, 3) from (2, 3): c = 2, c' = 4.
        assertEquals(-3 * (2 - 4), ch.getDelta(0, 1, 1, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_gainVoxelWithSeveralNeighbors_usesMeanOfNeighbors() {
        int[][][] ids = makeIds();
        ids[0][3][4] = 1;
        ChemotaxisHamiltonian ch =
                makeTerm(ids, 3, 0, makeLattice(1, 0, 2, 1, 0), makeLattice(1, 0, 0, 0, 0));
        // Cell 1 gains (2, 4) from (2, 3) and (3, 4): c = 8, c' = (7 + 10) / 2.
        assertEquals(-3 * (8 - 8.5), ch.getDelta(0, 1, 2, 4, 0), EPSILON);
    }

    @Test
    public void getDelta_loseVoxelUpGradient_returnsPositive() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), 3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        // Cell 1 loses (3, 3) and retracts to (2, 3): c = 6, c' = 4.
        assertEquals(-3 * (4 - 6), ch.getDelta(1, 0, 3, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_loseIsolatedVoxel_returnsZero() {
        int[][][] ids = new int[1][7][7];
        ids[0][3][3] = 1;
        ChemotaxisHamiltonian ch =
                makeTerm(ids, 3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        assertEquals(0, ch.getDelta(1, 0, 3, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_negativeLambda_returnsPositiveUpGradient() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), -3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        assertEquals(3 * (8 - 6), ch.getDelta(0, 1, 4, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_multipleLayers_sumsLayers() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), 3, 4, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 5, 0, 0, 0));
        // Layer Y is uniform, so only layer X contributes.
        assertEquals(-3 * (8 - 6), ch.getDelta(0, 1, 4, 3, 0), EPSILON);
        ch = makeTerm(makeIds(), 3, 4, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 1, 0, 0));
        assertEquals(-3 * (8 - 6) - 4 * (4 - 3), ch.getDelta(0, 1, 4, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_betweenCells_sumsSourceAndTarget() {
        int[][][] ids = makeIds();
        ids[0][4][3] = 2;
        ids[0][5][3] = 2;
        ChemotaxisHamiltonian ch =
                makeTerm(ids, 3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        // Cell 1 (lambda 3) gains (4, 3) from (3, 3): -3 * (8 - 6).
        // Cell 2 (lambda 0) loses (4, 3): 0.
        assertEquals(-3 * (8 - 6), ch.getDelta(2, 1, 4, 3, 0), EPSILON);
        // Cell 2 (lambda 0) gains (3, 3): 0. Cell 1 loses (3, 3) to (2, 3): -3 * (4 - 6).
        assertEquals(-3 * (4 - 6), ch.getDelta(1, 2, 3, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_zeroLambda_returnsZero() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), 0, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 1, 0, 0));
        assertEquals(0, ch.getDelta(0, 1, 4, 3, 0), EPSILON);
    }

    @Test
    public void getDelta_threeDimensional_usesNeighborsAboveAndBelow() {
        int[][][] ids = new int[3][7][7];
        ids[0][3][3] = 1;
        ids[2][3][3] = 1;
        ChemotaxisHamiltonian ch =
                makeTerm(ids, 3, 0, makeLattice(3, 0, 0, 0, 2), makeLattice(3, 0, 0, 0, 0));
        // Cell 1 gains (3, 3, 1) from (3, 3, 0) and (3, 3, 2): c = 2, c' = (0 + 4) / 2.
        assertEquals(0, ch.getDelta(0, 1, 3, 3, 1), EPSILON);
        ids[2][3][3] = 0;
        // Cell 1 gains (3, 3, 1) from (3, 3, 0): c = 2, c' = 0.
        assertEquals(-3 * 2, ch.getDelta(0, 1, 3, 3, 1), EPSILON);
    }

    @Test
    public void getDelta_regions_returnsZero() {
        ChemotaxisHamiltonian ch =
                makeTerm(makeIds(), 3, 0, makeLattice(1, 0, 2, 0, 0), makeLattice(1, 0, 0, 0, 0));
        assertEquals(0, ch.getDelta(1, 1, 2, 3, 3, 0), EPSILON);
    }
}
