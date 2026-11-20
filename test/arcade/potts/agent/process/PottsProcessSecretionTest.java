package arcade.potts.agent.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.agent.process.PottsProcessSecretion.Placement;

public class PottsProcessSecretionTest {
    private static final double EPSILON = 1E-12;

    private static final double DS = 2;

    private static final int ID = 1;

    // Creates a mock cell with the given secretion parameters and voxels.
    static PottsCell makeCell(MiniBox box, Voxel... voxels) {
        PottsCell cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(new ArrayList<>(List.of(voxels))).when(location).getVoxels();
        doAnswer(
                        i ->
                                new PottsLocation2D(new ArrayList<>(List.of(voxels)))
                                        .getBoundaryVoxels(i.getArgument(0)))
                .when(location)
                .getBoundaryVoxels(any());
        doReturn(location).when(cell).getLocation();
        doReturn(new Parameters(box, null, null)).when(cell).getParameters();
        doReturn(ID).when(cell).getID();
        return cell;
    }

    // Creates secretion parameters for one layer.
    static MiniBox secretion(String layer, double rate, String region) {
        MiniBox box = new MiniBox();
        box.put("secretion/RATE_" + layer, rate);
        box.put("secretion/REGION_" + layer, region);
        return box;
    }

    // Creates an ids array with the given voxels owned by the cell.
    static int[][][] makeIds(int height, int size, Voxel... voxels) {
        int[][][] ids = new int[height][size][size];
        for (Voxel v : voxels) {
            ids[v.z][v.x][v.y] = ID;
        }
        return ids;
    }

    // Creates a mock simulation with the given ids array and lattice.
    static PottsSimulation makeSim(int[][][] ids, String layer, PottsLattice lattice) {
        PottsSimulation sim = mock(PottsSimulation.class);
        Potts potts = mock(Potts.class);
        potts.ids = ids;
        doReturn(potts).when(sim).getPotts();
        doReturn(lattice).when(sim).getLattice(layer);
        return sim;
    }

    @Test
    public void constructor_givenParameters_setsRatesAndPlacements() {
        MiniBox box = secretion("PDGF", 4, "BOUNDARY");
        box.put("secretion/RATE_ECM", 2.0);
        box.put("secretion/REGION_ECM", "INSIDE");
        PottsProcessSecretion process = new PottsProcessSecretion(makeCell(box));
        assertEquals(4, process.getRate("PDGF"), EPSILON);
        assertEquals(Placement.BOUNDARY, process.getPlacement("PDGF"));
        assertEquals(2, process.getRate("ECM"), EPSILON);
        assertEquals(Placement.INSIDE, process.getPlacement("ECM"));
    }

    @Test
    public void constructor_noLayers_secretesNothing() {
        PottsProcessSecretion process = new PottsProcessSecretion(makeCell(new MiniBox()));
        assertTrue(process.rates.isEmpty());
        assertTrue(process.placements.isEmpty());
    }

    @Test
    public void constructor_distributionRate_ignoresDistributionKeys() {
        MiniBox box = secretion("PDGF", 4, "BOUNDARY");
        box.put("secretion/RATE_PDGF_MU", 4.0);
        box.put("secretion/RATE_PDGF_SIGMA", 1.0);
        PottsProcessSecretion process = new PottsProcessSecretion(makeCell(box));
        assertEquals(1, process.rates.size());
        assertEquals(4, process.getRate("PDGF"), EPSILON);
    }

    @Test
    public void constructor_negativeRate_throws() {
        MiniBox box = secretion("PDGF", -1, "BOUNDARY");
        assertThrows(
                IllegalArgumentException.class, () -> new PottsProcessSecretion(makeCell(box)));
    }

    @Test
    public void constructor_invalidRegion_throws() {
        MiniBox box = secretion("PDGF", 1, "EDGE");
        assertThrows(
                IllegalArgumentException.class, () -> new PottsProcessSecretion(makeCell(box)));
    }

    @Test
    public void getLayers_givenLayers_returnsLayers() {
        MiniBox box = secretion("PDGF", 4, "BOUNDARY");
        box.put("secretion/RATE_ECM", 2.0);
        box.put("secretion/REGION_ECM", "INSIDE");
        PottsProcessSecretion process = new PottsProcessSecretion(makeCell(box));
        assertEquals(new HashSet<>(List.of("ECM", "PDGF")), new HashSet<>(process.getLayers()));
    }

    @Test
    public void getLayers_modified_throws() {
        PottsProcessSecretion process =
                new PottsProcessSecretion(makeCell(secretion("PDGF", 4, "BOUNDARY")));
        assertThrows(UnsupportedOperationException.class, () -> process.getLayers().clear());
        assertEquals(4, process.getRate("PDGF"), EPSILON);
    }

    @Test
    public void setRate_givenLayer_updatesRate() {
        PottsProcessSecretion process =
                new PottsProcessSecretion(makeCell(secretion("PDGF", 4, "BOUNDARY")));
        process.setRate("PDGF", 7);
        assertEquals(7, process.getRate("PDGF"), EPSILON);
        assertEquals(Placement.BOUNDARY, process.getPlacement("PDGF"));
    }

    @Test
    public void setRate_unknownLayer_throws() {
        PottsProcessSecretion process =
                new PottsProcessSecretion(makeCell(secretion("PDGF", 4, "BOUNDARY")));
        assertThrows(IllegalArgumentException.class, () -> process.setRate("ECM", 1));
    }

    @Test
    public void setRate_negativeRate_throws() {
        PottsProcessSecretion process =
                new PottsProcessSecretion(makeCell(secretion("PDGF", 4, "BOUNDARY")));
        assertThrows(IllegalArgumentException.class, () -> process.setRate("PDGF", -1));
        assertEquals(4, process.getRate("PDGF"), EPSILON);
    }

    @Test
    public void step_inside_addsAmountToOwnVoxels() {
        Voxel[] voxels = {new Voxel(1, 1, 0), new Voxel(1, 2, 0), new Voxel(2, 1, 0)};
        PottsCell cell = makeCell(secretion("ECM", 6, "INSIDE"), voxels);
        PottsLattice lattice = new PottsLattice(5, 5, 1, DS, 1, new MiniBox());
        PottsSimulation sim = makeSim(makeIds(1, 5, voxels), "ECM", lattice);

        new PottsProcessSecretion(cell).step(null, sim);
        assertEquals(0, lattice.getField()[0][1][1], EPSILON);
        lattice.step(sim);

        double expected = 6 / (3 * DS * DS * DS);
        double total = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                total += lattice.getField()[0][i][j];
            }
        }
        for (Voxel v : voxels) {
            assertEquals(expected, lattice.getField()[0][v.x][v.y], EPSILON);
        }
        assertEquals(6, total * DS * DS * DS, EPSILON);
    }

    @Test
    public void step_boundary_addsAmountToBoundaryVoxels() {
        Voxel voxel = new Voxel(2, 2, 0);
        PottsCell cell = makeCell(secretion("PDGF", 8, "BOUNDARY"), voxel);
        PottsLattice lattice = new PottsLattice(5, 5, 1, DS, 1, new MiniBox());
        PottsSimulation sim = makeSim(makeIds(1, 5, voxel), "PDGF", lattice);

        new PottsProcessSecretion(cell).step(null, sim);
        assertEquals(0, lattice.getField()[0][1][2], EPSILON);
        lattice.step(sim);

        double expected = 8 / (4 * DS * DS * DS);
        double total = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                total += lattice.getField()[0][i][j];
            }
        }
        assertEquals(expected, lattice.getField()[0][1][2], EPSILON);
        assertEquals(expected, lattice.getField()[0][3][2], EPSILON);
        assertEquals(expected, lattice.getField()[0][2][1], EPSILON);
        assertEquals(expected, lattice.getField()[0][2][3], EPSILON);
        assertEquals(0, lattice.getField()[0][2][2], EPSILON);
        assertEquals(8, total * DS * DS * DS, EPSILON);
    }

    @Test
    public void step_zeroRate_doesNotGetLattice() {
        Voxel voxel = new Voxel(2, 2, 0);
        PottsCell cell = makeCell(secretion("PDGF", 0, "BOUNDARY"), voxel);
        PottsSimulation sim = makeSim(makeIds(1, 5, voxel), "PDGF", null);
        new PottsProcessSecretion(cell).step(null, sim);
        verify(sim, never()).getLattice(anyString());
    }
}
