package arcade.potts.env.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sim.engine.Schedule;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;
import arcade.core.ARCADE;
import arcade.core.sim.Series;
import arcade.core.sim.input.InputLoader;
import arcade.core.util.Box;
import arcade.core.util.MiniBox;
import arcade.potts.PottsARCADE;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.sim.PottsSimulation2D;
import arcade.potts.sim.hamiltonian.VesselHamiltonian;
import arcade.potts.sim.input.PottsInputBuilder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.env.component.PottsVesselPieces.Piece;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Term;

public class PottsComponentVesselCellsTest {
    private static final double EPSILON = 1E-9;

    // Population code of FIB (codes follow the order of populations in the setup).
    static final int FIB = 1;

    // Population code of VESSEL.
    static final int VESSEL = 2;

    // Lattice size along x [voxels] (ds = 2 um).
    static final int LENGTH = 40;

    // Lattice size along y [voxels].
    static final int WIDTH = 30;

    // Creates a setup with FIB, VESSEL and VTIP populations (none at start), an O2 layer, the
    // vessel component (no delivery), and the vessel cell component with the given parameters.
    static String setup(int length, int width, String parameters) {
        String fib =
                "<population id=\"FIB\" class=\"fibroblast\" init=\"0\">"
                        + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"400\" />"
                        + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                        + "<population.parameter id=\"DIFFERENTIATION_RATE\" value=\"0\" />"
                        + "</population>";
        String vessel =
                "<population id=\"VESSEL\" class=\"vessel\" init=\"0\">"
                        + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"400\" />"
                        + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                        + "</population>";
        String tip = vessel.replace("\"VESSEL\"", "\"VTIP\"");
        String layer =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"1\" /></layer>";
        String components =
                "<component id=\"VESSELS\" class=\"vessels\">"
                        + "<component.register id=\"O2\" /></component>"
                        + "<component id=\"VESSEL_CELLS\" class=\"vessel_cells\">"
                        + parameters
                        + "</component>";
        return "<set><series name=\"vc\" ticks=\"200\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\""
                + length
                + "\" width=\""
                + width
                + "\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                + "<potts>"
                + "<potts.term id=\"ADHESION\" />"
                + "<potts.term id=\"VOLUME\" />"
                + "<potts.term id=\"SURFACE\" />"
                + "<potts.term id=\"VESSEL\" />"
                + "</potts>"
                + "<populations>"
                + fib
                + vessel
                + tip
                + "</populations><layers>"
                + layer
                + "</layers><components>"
                + components
                + "</components></series></set>";
    }

    // Creates a component parameter tag.
    static String parameter(String id, Object value) {
        return "<component.parameter id=\"" + id + "\" value=\"" + value + "\" />";
    }

    // Builds the series of a setup and starts a simulation (no saver), without stepping it.
    static PottsSimulation start(Path dir, String setup, long seed) throws Exception {
        Path file = dir.resolve("setup.xml");
        Files.writeString(file, setup);
        Box parameters =
                new InputLoader().load(ARCADE.class.getResource("parameter.xml").toString());
        new InputLoader()
                .load(PottsARCADE.class.getResource("parameter.potts.xml").toString(), parameters);
        PottsInputBuilder builder = new PottsInputBuilder();
        builder.path = dir.toAbsolutePath() + "/";
        builder.parameters = parameters;
        builder.isVis = false;
        ArrayList<Series> series = builder.build(file.toString());
        PottsSimulation sim = new PottsSimulation2D(seed, series.get(0));
        sim.start();
        return sim;
    }

    // Three in-plane segments of 9 um along y = 31 um (voxel row 15) from x = 21 to 48 um.
    static PottsVesselNetwork network(double integrity) {
        PottsVesselNetwork network = new PottsVesselNetwork(1000);
        PottsVesselNode previous = network.addNode(21, 31, 0, Root.ARTERIAL);
        for (double x = 30; x <= 48; x += 9) {
            Root root = x == 48 ? Root.VENOUS : Root.NONE;
            PottsVesselNode node = network.addNode(x, 31, 0, root);
            network.addSegment(previous, node, integrity);
            previous = node;
        }
        return network;
    }

    // Sets the network of the vessel component.
    static void setNetwork(PottsSimulation sim, PottsVesselNetwork network) {
        ((PottsComponentVessels) sim.getComponent("VESSELS")).setNetwork(network);
    }

    // Gets the vessel cell component.
    static PottsComponentVesselCells component(PottsSimulation sim) {
        return (PottsComponentVesselCells) sim.getComponent("VESSEL_CELLS");
    }

    // Makes a list of voxels in the block [x0, x1) x [y0, y1).
    static ArrayList<Voxel> block(int x0, int x1, int y0, int y1) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        for (int i = x0; i < x1; i++) {
            for (int j = y0; j < y1; j++) {
                voxels.add(new Voxel(i, j, 0));
            }
        }
        return voxels;
    }

    // Converts piece voxels to a set of voxels.
    static Set<Voxel> voxelsOf(Piece piece) {
        Set<Voxel> set = new HashSet<>();
        for (int[] v : piece.voxels) {
            set.add(new Voxel(v[0], v[1], 0));
        }
        return set;
    }

    // Gets the voxels of a cell as a set.
    static Set<Voxel> voxelsOf(PottsCell cell) {
        return new HashSet<>(((PottsLocation) cell.getLocation()).getVoxels());
    }

    // Starts a simulation with the test network and the given parameters.
    static PottsSimulation startWithNetwork(Path dir, String parameters) throws Exception {
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, parameters), 0);
        setNetwork(sim, network(1));
        return sim;
    }

    @Test
    public void constructor_defaults_loadedFromParameterFile(@TempDir Path dir) throws Exception {
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, ""), 0);
        PottsComponentVesselCells cells = component(sim);
        PottsVesselNetwork network = network(1);
        PottsVesselEdge segment = network.getSegments().get(0);
        assertEquals(5, cells.rOut(segment), EPSILON);
        segment.setIntegrity(0);
        assertEquals(8, cells.rOut(segment), EPSILON);
        segment.setIntegrity(0.25);
        assertEquals(5 + 3 * 0.75, cells.rOut(segment), EPSILON);
    }

    @Test
    public void constructor_invalidTipParameters_throws() {
        for (String[] bad :
                new String[][] {{"N_TIP0", "0"}, {"G_TIP", "-1"}, {"LAMBDA_GUIDE", "-1"}}) {
            MiniBox parameters = parameters();
            parameters.put(bad[0], bad[1]);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new PottsComponentVesselCells(parameters),
                    bad[0]);
        }
    }

    // A full set of component parameters at their default values.
    static MiniBox parameters() {
        MiniBox parameters = new MiniBox();
        parameters.put("R_MATURE", 5);
        parameters.put("R_SPROUT", 8);
        parameters.put("L_PIECE", 30);
        parameters.put("N_MIN", 6);
        parameters.put("LAMBDA_ANCHOR_MIN", 10);
        parameters.put("LAMBDA_ANCHOR_MAX", 50);
        parameters.put("D_DRIFT_FOLD", 2);
        parameters.put("VESSELS", "VESSELS");
        parameters.put("POPULATION", "VESSEL");
        parameters.put("TIP_POPULATION", "VTIP");
        parameters.put("GROWTH", "GROWTH");
        parameters.put("LAMBDA_GUIDE", 50);
        parameters.put("G_TIP", 160);
        parameters.put("N_TIP0", 6);
        parameters.put("D_BUD_FOLD", 2);
        return parameters;
    }

    @Test
    public void schedule_atComponentsOrdering_everyTick() {
        PottsComponentVesselCells cells = new PottsComponentVesselCells(parameters());
        Schedule schedule = mock(Schedule.class);
        cells.schedule(schedule);
        verify(schedule).scheduleRepeating(cells, Ordering.COMPONENTS.ordinal(), 1);
        verify(schedule)
                .scheduleOnce(
                        eq(Schedule.EPOCH), eq(Ordering.POTTS.ordinal()), any(Steppable.class));
    }

    @Test
    public void makeComponent_vesselCells_createsComponent(@TempDir Path dir) throws Exception {
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, ""), 0);
        assertNotNull(component(sim));
    }

    @Test
    public void build_onePiecePerCell(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);

        Map<PottsCell, Piece> pieces = cells.getPieces();
        assertEquals(1, pieces.size());
        PottsCell cell = pieces.keySet().iterator().next();
        Piece piece = pieces.get(cell);
        assertEquals(VESSEL, cell.getPop());
        assertEquals(3, piece.segments.size());
        assertEquals(voxelsOf(piece), voxelsOf(cell));
        assertTrue(piece.voxels.size() > 50);
        assertEquals(piece.voxels.size(), cell.getTargetVolume(), EPSILON);
        assertEquals(cell.getLocation().getSurface(), cell.getTargetSurface(), EPSILON);

        Potts potts = ((PottsSimulation) sim).getPotts();
        for (int[] v : piece.voxels) {
            assertEquals(cell.getID(), potts.ids[0][v[0]][v[1]]);
        }
        PottsVesselNetwork network =
                ((PottsComponentVessels) sim.getComponent("VESSELS")).getNetwork();
        for (PottsVesselEdge segment : network.getSegments()) {
            assertSame(cell, cells.getCell(segment));
        }

        // Anchor at I = 1: gaining a voxel outside the footprint costs LAMBDA_ANCHOR_MAX.
        VesselHamiltonian vessel = (VesselHamiltonian) potts.getHamiltonian(Term.VESSEL);
        assertEquals(50, vessel.getDelta(0, cell.getID(), 2, 2, 0), EPSILON);
        int[] inside = piece.voxels.get(0);
        assertEquals(50, vessel.getDelta(cell.getID(), 0, inside[0], inside[1], 0), EPSILON);
        assertEquals(0, cells.getDriftCount());
        assertEquals(0, cells.getFragmentCount());
    }

    @Test
    public void build_anchorStrength_followsMeanIntegrity(@TempDir Path dir) throws Exception {
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, ""), 0);
        setNetwork(sim, network(0.5));
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        VesselHamiltonian vessel = (VesselHamiltonian) sim.getPotts().getHamiltonian(Term.VESSEL);
        assertEquals(10 + 40 * 0.5, vessel.getDelta(0, cell.getID(), 2, 2, 0), EPSILON);
    }

    @Test
    public void build_carvesTissueVoxels(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        ArrayList<Voxel> fibVoxels = block(14, 22, 8, 15);
        PottsCell fib = sim.insertCell(FIB, fibVoxels);
        assertNotNull(fib);
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);

        Piece piece = cells.getPieces().values().iterator().next();
        Set<Voxel> footprint = voxelsOf(piece);
        Set<Voxel> expected = new HashSet<>(fibVoxels);
        expected.removeAll(footprint);
        assertTrue(expected.size() < fibVoxels.size());
        assertFalse(expected.isEmpty());
        assertEquals(expected, voxelsOf(fib));
        assertEquals(expected.size(), fib.getLocation().getVolume(), EPSILON);
        assertSame(fib, sim.getPotts().getCell(fib.getID()));
        PottsCell vessel = cells.getPieces().keySet().iterator().next();
        assertEquals(footprint, voxelsOf(vessel));
    }

    @Test
    public void build_tissueCellFullyCovered_removed(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsCell fib = sim.insertCell(FIB, block(14, 17, 14, 17));
        assertNotNull(fib);
        int id = fib.getID();
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);

        assertNull(sim.getPotts().getCell(id));
        for (Object object : sim.getGrid().getAllObjects()) {
            assertNotSame(fib, object);
        }
        assertEquals(0, fib.getLocation().getVolume(), EPSILON);

        // The removed cell is no longer stepped.
        int age = fib.getAge();
        sim.schedule.step(sim);
        assertEquals(age, fib.getAge());
    }

    @Test
    public void build_existingVesselCells_throws(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        assertNotNull(sim.insertCell(VESSEL, block(2, 5, 2, 5)));
        PottsComponentVesselCells cells = component(sim);
        assertThrows(IllegalStateException.class, () -> cells.step(sim));
    }

    @Test
    public void build_existingTipCells_throws(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        assertNotNull(sim.insertCell(VESSEL + 1, block(2, 5, 2, 5)));
        PottsComponentVesselCells cells = component(sim);
        assertThrows(IllegalStateException.class, () -> cells.step(sim));
    }

    @Test
    public void build_noVesselTerm_throws(@TempDir Path dir) throws Exception {
        String setup = setup(LENGTH, WIDTH, "").replace("<potts.term id=\"VESSEL\" />", "");
        PottsSimulation sim = start(dir, setup, 0);
        setNetwork(sim, network(1));
        PottsComponentVesselCells cells = component(sim);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> cells.step(sim));
        assertTrue(e.getMessage().contains("vessel"));
    }

    @Test
    public void build_piecesPerLength_oneCellEach(@TempDir Path dir) throws Exception {
        // With L_PIECE 10 um, each 9 um segment is its own piece.
        PottsSimulation sim = startWithNetwork(dir, parameter("L_PIECE", 10));
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        assertEquals(3, cells.getPieces().size());
        Set<Integer> ids = new HashSet<>();
        for (PottsCell cell : cells.getPieces().keySet()) {
            assertTrue(ids.add(cell.getID()));
            assertEquals(1, cells.getPieces().get(cell).segments.size());
            assertEquals(voxelsOf(cells.getPieces().get(cell)), voxelsOf(cell));
        }
    }

    @Test
    public void step_unchangedVersion_doesNotRebuild(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        cells.step(sim);
        assertEquals(1, cells.getPieces().size());
        assertSame(cell, cells.getPieces().keySet().iterator().next());
        ((PottsComponentVessels) sim.getComponent("VESSELS")).refresh();
        cells.step(sim);
        assertSame(cell, cells.getPieces().keySet().iterator().next());
    }

    @Test
    public void step_integrityChanged_anchorUpdated(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        for (PottsVesselEdge segment : vessels.getNetwork().getSegments()) {
            segment.setIntegrity(0.99);
        }
        vessels.refresh();
        cells.step(sim);
        assertSame(cell, cells.getPieces().keySet().iterator().next());
        VesselHamiltonian vessel = (VesselHamiltonian) sim.getPotts().getHamiltonian(Term.VESSEL);
        assertEquals(10 + 40 * 0.99, vessel.getDelta(0, cell.getID(), 2, 2, 0), EPSILON);
    }

    @Test
    public void step_segmentRemoved_cellRetiredWithZeroTarget(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, parameter("L_PIECE", 10));
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsVesselNetwork network = vessels.getNetwork();
        PottsVesselEdge last = network.getSegments().get(2);
        PottsCell cell = cells.getCell(last);
        PottsCell first = cells.getCell(network.getSegments().get(0));

        network.removeSegment(last);
        vessels.refresh();
        cells.step(sim);

        assertEquals(2, cells.getPieces().size());
        assertFalse(cells.getPieces().containsKey(cell));
        assertTrue(cells.getPieces().containsKey(first));
        assertNull(cells.getCell(last));
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        VesselHamiltonian vessel = (VesselHamiltonian) sim.getPotts().getHamiltonian(Term.VESSEL);
        assertEquals(0, vessel.getDelta(0, cell.getID(), 2, 2, 0), EPSILON);
        // The cell still holds its voxels until it shrinks.
        assertSame(cell, sim.getPotts().getCell(cell.getID()));
    }

    @Test
    public void drift_cellMovedAway_counted(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, parameter("L_PIECE", 10));
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        assertEquals(0, cells.getDriftCount());
        assertEquals(0, cells.getFragmentCount());

        // Move all voxels of one cell to a block far from its segment.
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        PottsLocation location = (PottsLocation) cell.getLocation();
        Potts potts = sim.getPotts();
        for (Voxel v : new ArrayList<>(location.getVoxels())) {
            location.remove(v.x, v.y, v.z);
            potts.ids[0][v.x][v.y] = 0;
        }
        for (Voxel v : block(30, 34, 2, 5)) {
            location.add(v.x, v.y, v.z);
            potts.ids[0][v.x][v.y] = cell.getID();
        }
        cells.step(sim);
        assertEquals(1, cells.getDriftCount());
        assertEquals(0, cells.getFragmentCount());
    }

    @Test
    public void fragment_disconnectedCell_counted(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);

        // Cut the cell in two by removing a full column of its voxels.
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        PottsLocation location = (PottsLocation) cell.getLocation();
        for (Voxel v : new ArrayList<>(location.getVoxels())) {
            if (v.x == 17) {
                location.remove(v.x, v.y, v.z);
                sim.getPotts().ids[0][v.x][v.y] = 0;
            }
        }
        cells.step(sim);
        assertEquals(1, cells.getFragmentCount());
        assertEquals(0, cells.getDriftCount());
    }

    // Checks that a set of voxels is 4-connected.
    static boolean connected(Set<Voxel> voxels) {
        if (voxels.isEmpty()) {
            return true;
        }
        Set<Voxel> seen = new HashSet<>();
        List<Voxel> queue = new ArrayList<>();
        queue.add(voxels.iterator().next());
        seen.add(queue.get(0));
        while (!queue.isEmpty()) {
            Voxel v = queue.remove(queue.size() - 1);
            int[][] moves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] m : moves) {
                Voxel n = new Voxel(v.x + m[0], v.y + m[1], 0);
                if (voxels.contains(n) && seen.add(n)) {
                    queue.add(n);
                }
            }
        }
        return seen.size() == voxels.size();
    }

    @Test
    public void carve_returnsBoundaryVoxels_or_nullBelowNMin(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        Set<Voxel> before = voxelsOf(cell);
        MersenneTwisterFast random = new MersenneTwisterFast(1);

        // Below N_MIN: null, and the cell is unchanged.
        int size = before.size();
        assertNull(cells.carve(cell, size - 5, new double[] {38, 15}, random));
        assertEquals(before, voxelsOf(cell));

        // Towards +x: the voxels taken are boundary voxels at the +x end of the cell.
        double[] towards = {38, 15, 0};
        List<Voxel> taken = cells.carve(cell, 4, towards, random);
        assertNotNull(taken);
        assertEquals(4, taken.size());
        Set<Voxel> takenSet = new HashSet<>(taken);
        assertEquals(4, takenSet.size());
        assertTrue(connected(takenSet));
        int maxX = before.stream().mapToInt(v -> v.x).max().getAsInt();
        int minX = before.stream().mapToInt(v -> v.x).min().getAsInt();
        Potts potts = sim.getPotts();
        for (Voxel v : taken) {
            assertTrue(before.contains(v));
            assertEquals(0, potts.ids[0][v.x][v.y]);
            assertTrue(v.x > (minX + maxX) / 2, "voxel " + v);
            boolean boundary = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (!before.contains(new Voxel(v.x + dx, v.y + dy, 0))) {
                        boundary = true;
                    }
                }
            }
            assertTrue(boundary, "voxel " + v);
        }
        Set<Voxel> after = voxelsOf(cell);
        Set<Voxel> expected = new HashSet<>(before);
        expected.removeAll(takenSet);
        assertEquals(expected, after);
        assertEquals(size - 4, cell.getLocation().getVolume(), EPSILON);
        assertTrue(connected(after));

        // The voxels are empty, so a new cell can be inserted on them.
        assertNotNull(sim.insertCell(FIB, new ArrayList<>(taken)));
    }

    @Test
    public void carve_exactlyToNMin_allowed(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, parameter("N_MIN", 60));
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsCell cell = cells.getPieces().keySet().iterator().next();
        int size = (int) cell.getLocation().getVolume();
        MersenneTwisterFast random = new MersenneTwisterFast(1);
        assertNull(cells.carve(cell, size - 59, new double[] {0, 15}, random));
        List<Voxel> taken = cells.carve(cell, size - 60, new double[] {0, 15}, random);
        assertNotNull(taken);
        assertEquals(60, cell.getLocation().getVolume(), EPSILON);
        assertTrue(connected(voxelsOf(cell)));
    }

    // Gets the anchor of a cell by probing the vessel term: gaining an anchor voxel from medium
    // earns energy.
    static Set<Voxel> anchorOf(PottsSimulation sim, PottsCell cell) {
        VesselHamiltonian vessel = (VesselHamiltonian) sim.getPotts().getHamiltonian(Term.VESSEL);
        Set<Voxel> anchor = new HashSet<>();
        int[][] ids = sim.getPotts().ids[0];
        for (int x = 0; x < ids.length; x++) {
            for (int y = 0; y < ids[0].length; y++) {
                if (vessel.getDelta(0, cell.getID(), x, y, 0) < 0) {
                    anchor.add(new Voxel(x, y, 0));
                }
            }
        }
        return anchor;
    }

    @Test
    public void rebuild_segmentsAppendedAtTip_otherCellsKeepPieceAndAnchor(@TempDir Path dir)
            throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsVesselNetwork network = vessels.getNetwork();
        PottsCell line = cells.getPieces().keySet().iterator().next();
        Piece linePiece = cells.getPieces().get(line);
        Set<Voxel> lineAnchor = anchorOf(sim, line);
        Set<Voxel> lineVoxels = voxelsOf(line);

        // A sprout's first segment from a node in the middle of the piece.
        PottsVesselNode base = network.findNode(30, 31, 0);
        PottsVesselNode tip = network.addNode(30, 45, 0, Root.NONE);
        network.addSegment(base, tip, 0.2);
        vessels.refresh();
        cells.step(sim);
        assertEquals(2, cells.getPieces().size());
        assertSame(linePiece, cells.getPieces().get(line));
        assertEquals(lineAnchor, anchorOf(sim, line));
        assertEquals(lineVoxels, voxelsOf(line));
        PottsCell first = cells.getCell(network.getSegments(tip).get(0));
        assertNotNull(first);
        assertNotSame(line, first);
        Piece firstPiece = cells.getPieces().get(first);
        Set<Voxel> firstAnchor = anchorOf(sim, first);

        // The next segment at the tip: both earlier cells keep their pieces and anchors.
        PottsVesselNode next = network.addNode(30, 55, 0, Root.NONE);
        network.addSegment(tip, next, 0.2);
        vessels.refresh();
        cells.step(sim);
        assertSame(linePiece, cells.getPieces().get(line));
        assertEquals(lineAnchor, anchorOf(sim, line));
        assertSame(firstPiece, cells.getPieces().get(first));
        assertEquals(firstAnchor, anchorOf(sim, first));
        assertEquals(1, cells.getPieces().get(first).segments.size());
    }

    @Test
    public void rebuild_newPiece_doesNotTakeVoxelsOfVesselCells(@TempDir Path dir)
            throws Exception {
        PottsSimulation sim = startWithNetwork(dir, "");
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsVesselNetwork network = vessels.getNetwork();
        PottsCell line = cells.getPieces().keySet().iterator().next();
        Set<Voxel> lineVoxels = voxelsOf(line);

        // A segment crossing the line between its nodes; part of its footprint is the line's.
        PottsVesselNode a = network.addNode(35, 18, 0, Root.NONE);
        PottsVesselNode b = network.addNode(35, 44, 0, Root.NONE);
        PottsVesselEdge crossing = network.addSegment(a, b, 1);
        vessels.refresh();
        cells.step(sim);

        assertEquals(lineVoxels, voxelsOf(line));
        PottsCell cell = cells.getCell(crossing);
        assertNotNull(cell);
        Set<Voxel> overlap = voxelsOf(cell);
        overlap.retainAll(lineVoxels);
        assertTrue(overlap.isEmpty());
        assertTrue(voxelsOf(cell).size() >= 6);
        assertTrue(connected(voxelsOf(cell)));
    }

    @Test
    public void retire_fragmentedCell_removed(@TempDir Path dir) throws Exception {
        PottsSimulation sim = startWithNetwork(dir, parameter("L_PIECE", 10));
        PottsComponentVesselCells cells = component(sim);
        cells.step(sim);
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsVesselNetwork network = vessels.getNetwork();
        PottsVesselEdge last = network.getSegments().get(2);
        PottsCell cell = cells.getCell(last);
        int id = cell.getID();

        // Split the cell into three isolated voxels.
        PottsLocation location = (PottsLocation) cell.getLocation();
        List<Voxel> voxels = location.getVoxels();
        voxels.sort((u, w) -> u.x != w.x ? Integer.compare(u.x, w.x) : Integer.compare(u.y, w.y));
        Set<Voxel> keep = Set.of(voxels.get(0), voxels.get(voxels.size() - 1));
        Voxel middle = null;
        for (Voxel v : voxels) {
            if (!keep.contains(v)
                    && Math.abs(v.x - voxels.get(0).x) > 1
                    && Math.abs(v.x - voxels.get(voxels.size() - 1).x) > 1) {
                middle = v;
                break;
            }
        }
        for (Voxel v : voxels) {
            if (!keep.contains(v) && !v.equals(middle)) {
                location.remove(v.x, v.y, v.z);
                sim.getPotts().ids[0][v.x][v.y] = 0;
            }
        }
        assertEquals(3, location.getVolume(), EPSILON);
        assertFalse(connected(voxelsOf(cell)));

        // The segment goes; the Potts step is not run, so the fragments never shrink.
        network.removeSegment(last);
        vessels.refresh();
        cells.step(sim);
        assertTrue(cells.getRetiring().contains(cell));
        for (int k = 0; k < PottsComponentVesselCells.RETIRE_TICKS; k++) {
            cells.step(sim);
        }
        assertNull(sim.getPotts().getCell(id));
        assertFalse(cells.getRetiring().contains(cell));
        for (int[] row : sim.getPotts().ids[0]) {
            for (int value : row) {
                assertNotEquals(id, value);
            }
        }
    }
}
