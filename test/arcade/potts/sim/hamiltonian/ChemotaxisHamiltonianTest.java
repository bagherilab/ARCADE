package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
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

    // Creates a mock cell with the given id, population, volume, and centroid.
    static PottsCell makeCell(int id, int pop, double volume, double[] centroid) {
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(volume).when(location).getVolume();
        doReturn(centroid).when(location).getCentroid();
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        doReturn(location).when(cell).getLocation();
        return cell;
    }

    // Creates a 2D lattice with values a + bx * x + by * y.
    static PottsLattice makeLattice(double a, double bx, double by) {
        PottsLattice lattice = new PottsLattice(10, 10, 1, 1, 1, new MiniBox());
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                lattice.getField()[0][i][j] = a + bx * i + by * j;
            }
        }
        return lattice;
    }

    // Creates a term with cell 1 (population A) registered and lattices X and Y set.
    static ChemotaxisHamiltonian makeTerm(
            double lambdaAX, double lambdaAY, PottsLattice x, PottsLattice y) {
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(makeSeries(lambdaAX, lambdaAY, 0));
        ch.register(makeCell(1, 1, 9, new double[] {4, 5, 0}));
        HashMap<String, Lattice> lattices = new HashMap<>();
        lattices.put("X", x);
        lattices.put("Y", y);
        ch.setLattices(lattices);
        return ch;
    }

    @Test
    public void constructor_noPopulations_createsEmptyMaps() {
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(mock(PottsSeries.class));
        assertNotNull(ch.configs);
        assertTrue(ch.popToLambdas.isEmpty());
    }

    @Test
    public void constructor_givenSeries_setsLambdas() {
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(makeSeries(3, -2, 5));
        assertEquals(3, ch.popToLambdas.get(1).get("X"), EPSILON);
        assertEquals(-2, ch.popToLambdas.get(1).get("Y"), EPSILON);
        assertEquals(5, ch.popToLambdas.get(2).get("X"), EPSILON);
        assertEquals(0, ch.popToLambdas.get(2).get("Y"), EPSILON);
    }

    @Test
    public void register_givenCell_addsConfig() {
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(makeSeries(3, -2, 5));
        PottsCell cell = makeCell(7, 2, 9, new double[] {0, 0, 0});
        ch.register(cell);
        assertSame(cell.getLocation(), ch.configs.get(7).location);
        assertSame(ch.popToLambdas.get(2), ch.configs.get(7).lambdas);
    }

    @Test
    public void deregister_givenCell_removesConfig() {
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(makeSeries(3, -2, 5));
        PottsCell cell = makeCell(7, 2, 9, new double[] {0, 0, 0});
        ch.register(cell);
        ch.deregister(cell);
        assertFalse(ch.configs.containsKey(7));
    }

    @Test
    public void getGradient_linearField_returnsSlopeEverywhere() {
        double[][] field = makeLattice(1, 2, -3).getField()[0];
        for (double[] position : new double[][] {{4, 5}, {4.3, 5.8}, {0, 0}, {9, 9}, {0.5, 8.5}}) {
            double[] gradient = ChemotaxisHamiltonian.getGradient(field, position[0], position[1]);
            assertEquals(2, gradient[0], EPSILON);
            assertEquals(-3, gradient[1], EPSILON);
        }
    }

    @Test
    public void getGradient_quadraticField_interpolatesCentralDifferences() {
        double[][] field = new double[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                field[i][j] = i * i;
            }
        }
        // Central difference at x = i is 2i, so the value at x = 4.25 is 8.5.
        double[] gradient = ChemotaxisHamiltonian.getGradient(field, 4.25, 3);
        assertEquals(8.5, gradient[0], EPSILON);
        assertEquals(0, gradient[1], EPSILON);
        // One-sided difference at the edge x = 0 is 1.
        assertEquals(1, ChemotaxisHamiltonian.getGradient(field, 0, 3)[0], EPSILON);
    }

    @Test
    public void getGradient_outsideArray_usesNearestEdge() {
        double[][] field = makeLattice(0, 2, 1).getField()[0];
        double[] gradient = ChemotaxisHamiltonian.getGradient(field, -3, 12);
        assertEquals(2, gradient[0], EPSILON);
        assertEquals(1, gradient[1], EPSILON);
    }

    @Test
    public void getDelta_gainVoxelUpGradient_returnsNegative() {
        ChemotaxisHamiltonian ch = makeTerm(3, 0, makeLattice(0, 2, 0), makeLattice(0, 0, 0));
        // Gaining voxel (6, 5) moves the centroid by (6 - 4) / (9 + 1) = 0.2 in x.
        assertEquals(-3 * 2 * 0.2, ch.getDelta(0, 1, 6, 5, 0), EPSILON);
    }

    @Test
    public void getDelta_loseVoxelDownGradient_returnsNegative() {
        ChemotaxisHamiltonian ch = makeTerm(3, 0, makeLattice(0, 2, 0), makeLattice(0, 0, 0));
        // Losing voxel (2, 5) moves the centroid by -(2 - 4) / (9 - 1) = 0.25 in x.
        assertEquals(-3 * 2 * 0.25, ch.getDelta(1, 0, 2, 5, 0), EPSILON);
    }

    @Test
    public void getDelta_negativeLambda_returnsPositiveUpGradient() {
        ChemotaxisHamiltonian ch = makeTerm(-3, 0, makeLattice(0, 2, 0), makeLattice(0, 0, 0));
        assertEquals(3 * 2 * 0.2, ch.getDelta(0, 1, 6, 5, 0), EPSILON);
    }

    @Test
    public void getDelta_multipleLayers_sumsLayers() {
        ChemotaxisHamiltonian ch = makeTerm(3, 4, makeLattice(0, 2, 0), makeLattice(0, 0, 1));
        // Gaining voxel (6, 7) moves the centroid by (0.2, 0.2).
        double expected = -3 * 2 * 0.2 - 4 * 1 * 0.2;
        assertEquals(expected, ch.getDelta(0, 1, 6, 7, 0), EPSILON);
    }

    @Test
    public void getDelta_betweenCells_sumsSourceAndTarget() {
        ChemotaxisHamiltonian ch = makeTerm(3, 0, makeLattice(0, 2, 0), makeLattice(0, 0, 0));
        ch.register(makeCell(2, 1, 4, new double[] {7, 5, 0}));
        // Cell 1 gains (6, 5): -3 * 2 * 0.2. Cell 2 loses (6, 5): -(6 - 7) / 3 in x.
        double expected = -3 * 2 * 0.2 - 3 * 2 * (1.0 / 3);
        assertEquals(expected, ch.getDelta(2, 1, 6, 5, 0), EPSILON);
    }

    @Test
    public void getDelta_zeroLambda_returnsZero() {
        ChemotaxisHamiltonian ch = makeTerm(0, 0, makeLattice(0, 2, 0), makeLattice(0, 0, 1));
        assertEquals(0, ch.getDelta(0, 1, 6, 7, 0), EPSILON);
    }

    @Test
    public void getDelta_lastVoxel_returnsZero() {
        ChemotaxisHamiltonian ch = new ChemotaxisHamiltonian(makeSeries(3, 0, 0));
        ch.register(makeCell(1, 1, 1, new double[] {4, 5, 0}));
        ch.setLattices(new HashMap<>());
        assertEquals(0, ch.getDelta(1, 0, 4, 5, 0), EPSILON);
    }

    @Test
    public void getDelta_regions_returnsZero() {
        ChemotaxisHamiltonian ch = makeTerm(3, 0, makeLattice(0, 2, 0), makeLattice(0, 0, 0));
        assertEquals(0, ch.getDelta(1, 1, 2, 6, 5, 0), EPSILON);
    }
}
