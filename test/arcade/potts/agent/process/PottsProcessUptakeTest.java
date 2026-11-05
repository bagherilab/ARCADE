package arcade.potts.agent.process;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsProcessUptakeTest {
    private static final double EPSILON = 1E-12;

    private static final double DS = 2;

    // Creates a mock cell with the given uptake parameters and voxels.
    static PottsCell makeCell(MiniBox box, Voxel... voxels) {
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(new ArrayList<>(List.of(voxels))).when(location).getVoxels();
        doReturn(location).when(cell).getLocation();
        doReturn(new Parameters(box, null, null)).when(cell).getParameters();
        return cell;
    }

    // Creates uptake parameters for one layer.
    static MiniBox uptake(String layer, double rate) {
        MiniBox box = new MiniBox();
        box.put("uptake/RATE_" + layer, rate);
        return box;
    }

    // Creates a 5 x 5 lattice, with or without a diffuser.
    static PottsLattice makeLattice(boolean diffuser) {
        MiniBox parameters = new MiniBox();
        if (diffuser) {
            parameters.put("(OPERATION)" + TAG_SEPARATOR + "DIFFUSER", "");
            parameters.put("diffuser/DIFFUSIVITY", 10.0);
            parameters.put("diffuser/MAX_SUBSTEP", 2.0);
            parameters.put("diffuser/HALF_SATURATION", 1.0);
        }
        return new PottsLattice(5, 5, 1, DS, 1.0 / 60, parameters);
    }

    // Creates a mock simulation with the given lattice.
    static PottsSimulation makeSim(String layer, PottsLattice lattice) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(lattice).when(sim).getLattice(layer);
        return sim;
    }

    @Test
    public void constructor_givenRates_readsEachLayer() {
        MiniBox box = uptake("O2", 4);
        box.put("uptake/RATE_GLUCOSE", 2.0);
        PottsProcessUptake process = new PottsProcessUptake(makeCell(box));
        assertEquals(4, process.getRate("O2"), EPSILON);
        assertEquals(2, process.getRate("GLUCOSE"), EPSILON);
        assertEquals(2, process.getLayers().size());
    }

    @Test
    public void constructor_distributionRate_ignoresDistributionKeys() {
        MiniBox box = uptake("O2", 4);
        box.put("uptake/RATE_O2_MU", 4.0);
        box.put("uptake/RATE_O2_SIGMA", 1.0);
        PottsProcessUptake process = new PottsProcessUptake(makeCell(box));
        assertEquals(1, process.rates.size());
        assertEquals(4, process.getRate("O2"), EPSILON);
    }

    @Test
    public void constructor_negativeRate_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsProcessUptake(makeCell(uptake("O2", -1))));
    }

    @Test
    public void step_givenRate_addsRatePerVoxelToSink() {
        Voxel[] voxels = {new Voxel(1, 1, 0), new Voxel(1, 2, 0), new Voxel(2, 1, 0)};
        PottsLattice lattice = makeLattice(true);
        PottsSimulation sim = makeSim("O2", lattice);

        new PottsProcessUptake(makeCell(uptake("O2", 6), voxels)).step(null, sim);

        double expected = 6 / (3 * DS * DS * DS);
        double total = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                total += lattice.getSink()[0][i][j];
                assertEquals(0, lattice.getField()[0][i][j], EPSILON);
            }
        }
        for (Voxel v : voxels) {
            assertEquals(expected, lattice.getSink()[0][v.x][v.y], EPSILON);
        }
        assertEquals(3 * expected, total, EPSILON);
    }

    @Test
    public void step_zeroRate_addsNothing() {
        PottsLattice lattice = makeLattice(true);
        PottsSimulation sim = makeSim("O2", lattice);

        new PottsProcessUptake(makeCell(uptake("O2", 0), new Voxel(1, 1, 0))).step(null, sim);

        assertEquals(0, lattice.getSink()[0][1][1], EPSILON);
    }

    @Test
    public void step_layerWithoutDiffuser_throws() {
        PottsSimulation sim = makeSim("O2", makeLattice(false));
        PottsProcessUptake process =
                new PottsProcessUptake(makeCell(uptake("O2", 6), new Voxel(1, 1, 0)));

        Exception e = assertThrows(IllegalStateException.class, () -> process.step(null, sim));
        assertTrue(e.getMessage().contains("O2"));
    }

    @Test
    public void step_missingLayer_throws() {
        PottsSimulation sim = mock(PottsSimulation.class);
        PottsProcessUptake process =
                new PottsProcessUptake(makeCell(uptake("O2", 6), new Voxel(1, 1, 0)));

        assertThrows(IllegalStateException.class, () -> process.step(null, sim));
    }
}
