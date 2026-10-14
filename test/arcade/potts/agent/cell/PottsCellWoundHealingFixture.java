package arcade.potts.agent.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

/** Shared setup for wound healing cell tests: a 3 x 3 domain with the cell at the center. */
public final class PottsCellWoundHealingFixture {
    /** Id of the cell under test. */
    public static final int ID = 7;

    /** Population parameters. */
    public final MiniBox box = defaults();

    /** Mean layer values returned by the mocked lattices. */
    public final HashMap<String, Double> means = new HashMap<>();

    /** Cells returned by the mocked potts, by id. */
    public final HashMap<Integer, PottsCell> cells = new HashMap<>();

    /** Potts ids array, including the one-voxel border. */
    public final int[][][] ids = new int[1][5][5];

    /** ECM field. */
    public final double[][][] ecm = new double[1][5][5];

    /** Mocked location of the cell. */
    public final PottsLocation location = mock(PottsLocation.class);

    /** Mocked potts. */
    public final Potts potts = mock(Potts.class);

    /** Mocked grid. */
    public final Grid grid = mock(Grid.class);

    /** Mocked simulation. */
    public final PottsSimulation sim = mock(PottsSimulation.class);

    /** Mocked series. */
    public final PottsSeries series = mock(PottsSeries.class);

    /** Mocked random number generator. */
    public final MersenneTwisterFast random = mock(MersenneTwisterFast.class);

    /** Creates the fixture with the cell at voxel (2, 2, 0). */
    public PottsCellWoundHealingFixture() {
        setVoxels(new Voxel(2, 2, 0));

        potts.ids = ids;
        doAnswer(i -> cells.get((int) i.getArgument(0))).when(potts).getCell(anyInt());
        doReturn(potts).when(sim).getPotts();
        doReturn(grid).when(sim).getGrid();

        for (String layer : new String[] {"PDGF", "ECM", "INF"}) {
            PottsLattice lattice = mock(PottsLattice.class);
            doAnswer(i -> means.getOrDefault(layer, 0.0)).when(lattice).getAverageValue(any());
            doReturn(ecm).when(lattice).getField();
            doReturn(lattice).when(sim).getLattice(layer);
        }

        doReturn(mock(Schedule.class)).when(sim).getSchedule();

        series.populations = new HashMap<>();
        doReturn(series).when(sim).getSeries();

        doReturn(0.0).when(random).nextDouble();
    }

    /**
     * Gets default population parameters for wound healing cells.
     *
     * @return the parameters
     */
    public static MiniBox defaults() {
        MiniBox box = new MiniBox();
        box.put("CONTACT_FREE_FRACTION", 0);
        box.put("SPONTANEOUS_SENESCENCE", 0);
        box.put("BYSTANDER_CONTACT", 0);
        box.put("BYSTANDER_PARACRINE", 0);
        box.put("INF_BYSTANDER", 1000);
        box.put("PDGF_PROLIFERATION", 0.2);
        box.put("PDGF_DIFFERENTIATION", 0.3);
        box.put("ECM_DIFFERENTIATION", 0.3);
        box.put("DIFFERENTIATION_RATE", 0);
        box.put("DIFFERENTIATION_TARGET", "NONE");
        box.put("INF_APOPTOSIS", 3);
        box.put("MATURATION", 4);
        box.put("ECM_TENSION", 0.1);
        box.put("SENESCENCE_RATE", 0);
        box.put("ECM_PROLIFERATION", 0);
        box.put("senescence/RATE_EARLY", 1);
        box.put("senescence/STEPS_EARLY", 2);
        box.put("senescence/CLEARANCE_EARLY", 0);
        box.put("senescence/CLEARANCE_LATE", 0);
        box.put("senescence/BASAL_CLEARANCE", 0);
        box.put("senescence/DRUG_START", 0);
        box.put("senescence/DRUG_DURATION", 0);
        box.put("senescence/DRUG_KILL", 0);
        box.put("proliferation/SIZE_TARGET", 2);
        for (String id : new String[] {"G1", "S", "G2", "M"}) {
            box.put("proliferation/RATE_" + id, 1);
            box.put("proliferation/STEPS_" + id, 1);
        }
        box.put("proliferation/CELL_GROWTH_RATE", 0);
        box.put("proliferation/NUCLEUS_GROWTH_RATE", 0);
        box.put("proliferation/BASAL_APOPTOSIS_RATE", 0);
        box.put("proliferation/NUCLEUS_CONDENSATION_FRACTION", 0);
        String[] apoptosis = {
            "RATE_EARLY",
            "RATE_LATE",
            "STEPS_EARLY",
            "STEPS_LATE",
            "WATER_LOSS_RATE",
            "CYTOPLASMIC_BLEBBING_RATE",
            "NUCLEUS_PYKNOSIS_RATE",
            "NUCLEUS_FRAGMENTATION_RATE",
        };
        for (String id : apoptosis) {
            box.put("apoptosis/" + id, 1);
        }
        return box;
    }

    /**
     * Sets the voxels of the cell and marks them in the ids array.
     *
     * @param voxels the voxels
     */
    public void setVoxels(Voxel... voxels) {
        for (int[][] plane : ids) {
            for (int[] row : plane) {
                for (int j = 0; j < row.length; j++) {
                    if (row[j] == ID) {
                        row[j] = 0;
                    }
                }
            }
        }
        for (Voxel v : voxels) {
            ids[v.z][v.x][v.y] = ID;
        }
        List<Voxel> list = new ArrayList<>(List.of(voxels));
        doAnswer(i -> new ArrayList<>(list)).when(location).getVoxels();
    }

    /**
     * Adds a neighboring cell at the given voxel.
     *
     * @param id the neighbor id
     * @param x the x coordinate
     * @param y the y coordinate
     * @param neighbor the neighbor cell
     */
    public void addNeighbor(int id, int x, int y, PottsCell neighbor) {
        ids[0][x][y] = id;
        cells.put(id, neighbor);
    }

    /**
     * Fills all four in-domain neighbors of the center voxel with another cell.
     *
     * @param id the neighbor id
     */
    public void surround(int id) {
        PottsCell neighbor = mock(PottsCell.class);
        addNeighbor(id, 1, 2, neighbor);
        addNeighbor(id, 3, 2, neighbor);
        addNeighbor(id, 2, 1, neighbor);
        addNeighbor(id, 2, 3, neighbor);
    }

    /**
     * Creates a container for the cell.
     *
     * @param state the cell state
     * @return the container
     */
    public PottsCellContainer container(State state) {
        return new PottsCellContainer(ID, 1, 1, 10, 2, state, null, 0, 100, 5);
    }

    /**
     * Creates a container for the cell with a phase.
     *
     * @param state the cell state
     * @param phase the cell phase
     * @return the container
     */
    public PottsCellContainer container(State state, Phase phase) {
        return new PottsCellContainer(ID, 1, 1, 10, 2, state, phase, 0, 100, 5);
    }

    /**
     * Gets the parameters for the cell.
     *
     * @return the parameters
     */
    public Parameters parameters() {
        return new Parameters(box, null, null);
    }
}
