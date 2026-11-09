package arcade.potts.sim.hamiltonian;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import arcade.core.env.lattice.Lattice;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.sim.PottsSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;

public class MatrixHamiltonianTest {
    private static final double EPSILON = 1E-10;

    // Creates a series with populations A (code 1) and B (code 2).
    static PottsSeries makeSeries(double lambdaA, double lambdaB) {
        PottsSeries series = mock(PottsSeries.class);
        series.populations = new HashMap<>();
        series.potts = new MiniBox();
        String[] pops = {"A", "B"};
        double[] lambdas = {lambdaA, lambdaB};
        for (int i = 0; i < pops.length; i++) {
            MiniBox population = new MiniBox();
            population.put("CODE", i + 1);
            series.populations.put(pops[i], population);
            series.potts.put("matrix/LAMBDA" + TARGET_SEPARATOR + pops[i], lambdas[i]);
        }
        return series;
    }

    // Creates a mock cell with the given id and population.
    static PottsCell makeCell(int id, int pop) {
        PottsCell cell = mock(PottsCell.class);
        doReturn(id).when(cell).getID();
        doReturn(pop).when(cell).getPop();
        return cell;
    }

    // Creates a 2D layer with value x at (x, y).
    static PottsLattice makeLattice() {
        PottsLattice lattice = new PottsLattice(7, 7, 1, 1, 1, new MiniBox());
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                lattice.getField()[0][i][j] = i;
            }
        }
        return lattice;
    }

    // Creates a term with cell 1 (population A) and cell 2 (population B) and the given layers.
    static MatrixHamiltonian makeTerm(double lambdaA, double lambdaB, String layer) {
        MatrixHamiltonian mh = new MatrixHamiltonian(makeSeries(lambdaA, lambdaB));
        mh.register(makeCell(1, 1));
        mh.register(makeCell(2, 2));
        HashMap<String, Lattice> lattices = new HashMap<>();
        lattices.put(layer, makeLattice());
        mh.setLattices(lattices);
        return mh;
    }

    @Test
    public void constructor_noPopulations_createsEmptyMaps() {
        MatrixHamiltonian mh = new MatrixHamiltonian(mock(PottsSeries.class));
        assertTrue(mh.configs.isEmpty());
        assertTrue(mh.popToLambda.isEmpty());
    }

    @Test
    public void constructor_givenSeries_setsLambdas() {
        MatrixHamiltonian mh = new MatrixHamiltonian(makeSeries(3, 5));
        assertEquals(3, mh.popToLambda.get(1), EPSILON);
        assertEquals(5, mh.popToLambda.get(2), EPSILON);
    }

    @Test
    public void constructor_negativeLambda_throwsException() {
        assertThrows(
                IllegalArgumentException.class, () -> new MatrixHamiltonian(makeSeries(-1, 0)));
    }

    @Test
    public void register_givenCell_addsConfig() {
        MatrixHamiltonian mh = new MatrixHamiltonian(makeSeries(3, 5));
        mh.register(makeCell(7, 2));
        assertEquals(5, mh.configs.get(7), EPSILON);
    }

    @Test
    public void deregister_givenCell_removesConfig() {
        MatrixHamiltonian mh = new MatrixHamiltonian(makeSeries(3, 5));
        PottsCell cell = makeCell(7, 2);
        mh.register(cell);
        mh.deregister(cell);
        assertFalse(mh.configs.containsKey(7));
    }

    @Test
    public void getDelta_cellGainsFromMedium_returnsLambdaTimesECM() {
        MatrixHamiltonian mh = makeTerm(3, 5, "ECM");
        assertEquals(3 * 4, mh.getDelta(0, 1, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_cellLosesToMedium_returnsSameCost() {
        MatrixHamiltonian mh = makeTerm(3, 5, "ECM");
        assertEquals(mh.getDelta(0, 1, 4, 2, 0), mh.getDelta(1, 0, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_betweenCells_returnsSumOfBothCosts() {
        MatrixHamiltonian mh = makeTerm(3, 5, "ECM");
        assertEquals((3 + 5) * 4, mh.getDelta(1, 2, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_noECMAtVoxel_returnsZero() {
        MatrixHamiltonian mh = makeTerm(3, 5, "ECM");
        assertEquals(0, mh.getDelta(0, 1, 0, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_zeroLambda_returnsZero() {
        MatrixHamiltonian mh = makeTerm(0, 0, "ECM");
        assertEquals(0, mh.getDelta(1, 2, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_noECMLayer_returnsZero() {
        MatrixHamiltonian mh = makeTerm(3, 5, "X");
        assertEquals(0, mh.getDelta(0, 1, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_latticesNotSet_returnsZero() {
        MatrixHamiltonian mh = new MatrixHamiltonian(makeSeries(3, 5));
        mh.register(makeCell(1, 1));
        assertEquals(0, mh.getDelta(0, 1, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_unregisteredCell_returnsZero() {
        MatrixHamiltonian mh = makeTerm(3, 5, "ECM");
        assertEquals(0, mh.getDelta(0, 9, 4, 2, 0), EPSILON);
    }

    @Test
    public void getDelta_betweenRegions_returnsZero() {
        MatrixHamiltonian mh = makeTerm(3, 5, "ECM");
        assertEquals(0, mh.getDelta(1, 1, 2, 4, 2, 0), EPSILON);
    }
}
