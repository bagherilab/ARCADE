package arcade.potts.env.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.sim.hamiltonian.VesselHamiltonian;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.block;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.component;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.setNetwork;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.start;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.voxelsOf;
import static arcade.potts.env.component.PottsComponentVesselGrowth.Sprout;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.util.PottsEnums.Term;

/** Tip cells: budding, steering by the aim, appending at the front, and removal. */
public class PottsVesselTipsIntegrationTest {
    // Lattice size along x [voxels] (ds = 2 um).
    private static final int LENGTH = 60;

    // Lattice size along y [voxels].
    private static final int WIDTH = 40;

    // Voxel size [um].
    private static final double DS = 2;

    // Population codes (order of populations in the setup).
    private static final int FIB = 1;

    private static final int VESSEL = 2;

    private static final int VTIP = 3;

    // Number of voxels a tip buds with.
    private static final int N_TIP0 = 6;

    // A setup with FIB, VESSEL and VTIP, the vessel, (optionally) growth and vessel cell
    // components, a VEGF layer and an ECM layer set by the tests, and matrix resistance for tips.
    static String setup(boolean growth, String growthParameters) {
        String population =
                "<population id=\"%s\" class=\"%s\" init=\"0\">"
                        + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"400\" />"
                        + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                        + "%s</population>";
        String fib =
                String.format(
                        population,
                        "FIB",
                        "fibroblast",
                        "<population.parameter id=\"DIFFERENTIATION_RATE\" value=\"0\" />");
        String vessel = String.format(population, "VESSEL", "vessel", "");
        String tip = String.format(population, "VTIP", "vessel", "");
        String layers =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"1\" /></layer>"
                        + "<layer id=\"VEGF\" /><layer id=\"ECM\" />";
        String growthComponent =
                "<component id=\"GROWTH\" class=\"vessel_growth\">"
                        + parameter("P_MAX", 0)
                        + parameter("BETA", 1000)
                        + parameter("KAPPA", 0)
                        + parameter("D_SUPPORT", 1000)
                        + parameter("K_MAT", 0)
                        + parameter("K_PRUNE", 0)
                        + growthParameters
                        + "<component.register id=\"VEGF\" /></component>";
        String components =
                "<component id=\"VESSELS\" class=\"vessels\">"
                        + "<component.register id=\"O2\" /></component>"
                        + (growth ? growthComponent : "")
                        + "<component id=\"VESSEL_CELLS\" class=\"vessel_cells\" />";
        return "<set><series name=\"tips\" ticks=\"200\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\""
                + LENGTH
                + "\" width=\""
                + WIDTH
                + "\" height=\"1\" ds=\"2\" dt=\"0.25\">"
                + "<potts>"
                + "<potts.term id=\"ADHESION\" />"
                + "<potts.term id=\"VOLUME\" />"
                + "<potts.term id=\"SURFACE\" />"
                + "<potts.term id=\"VESSEL\" />"
                + "<potts.term id=\"MATRIX\" />"
                + "<potts.parameter id=\"MCS\" value=\"10\" />"
                + "<potts.parameter term=\"matrix\" id=\"LAMBDA\" target=\"VTIP\" value=\"1\" />"
                + "</potts>"
                + "<populations>"
                + fib
                + vessel
                + tip
                + "</populations><layers>"
                + layers
                + "</layers><components>"
                + components
                + "</components></series></set>";
    }

    // Creates a component parameter tag.
    static String parameter(String id, Object value) {
        return "<component.parameter id=\"" + id + "\" value=\"" + value + "\" />";
    }

    // Adds a straight in-plane vessel along x at height y [um], from x = 21 to 99 um, with nodes
    // every 26 um.
    static void addLine(PottsVesselNetwork network, double y) {
        PottsVesselNode previous = network.addNode(21, y, 0, Root.ARTERIAL);
        for (double x = 47; x <= 99; x += 26) {
            Root root = x == 99 ? Root.VENOUS : Root.NONE;
            PottsVesselNode node = network.addNode(x, y, 0, root);
            network.addSegment(previous, node, 1);
            previous = node;
        }
    }

    // Starts a simulation with a network, a fibroblast for support, VEGF rising along +y, and an
    // ECM field; steps it once so the vessel cells are built.
    static PottsSimulation run(
            Path dir,
            boolean growth,
            String growthParameters,
            PottsVesselNetwork network,
            double[][] ecm)
            throws Exception {
        PottsSimulation sim = start(dir, setup(growth, growthParameters), 0);
        setNetwork(sim, network);
        assertNotNull(sim.insertCell(FIB, block(1, 8, 1, 8)));
        double[][] vegf = ((PottsLattice) sim.getLattice("VEGF")).getField()[0];
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                vegf[i][j] = 1 + j;
            }
        }
        if (ecm != null) {
            double[][] field = ((PottsLattice) sim.getLattice("ECM")).getField()[0];
            for (int i = 0; i < LENGTH; i++) {
                field[i] = ecm[i].clone();
            }
        }
        sim.schedule.step(sim);
        return sim;
    }

    static PottsComponentVesselGrowth growth(PottsSimulation sim) {
        return (PottsComponentVesselGrowth) sim.getComponent("GROWTH");
    }

    static PottsVesselNetwork network(PottsSimulation sim) {
        return ((PottsComponentVessels) sim.getComponent("VESSELS")).getNetwork();
    }

    // Starts a sprout at a node, aimed along a direction over the aim length.
    static Sprout sprout(PottsSimulation sim, double[] at, double[] u) {
        PottsComponentVesselGrowth growth = growth(sim);
        PottsVesselNetwork network = network(sim);
        PottsVesselNode node = network.findNode(at[0], at[1], at[2]);
        assertNotNull(node);
        Sprout sprout = new Sprout(node, null, sim.schedule.getTime() * 0.25, false);
        sprout.excluded.addAll(PottsComponentVesselGrowth.nearby(network, node, 2));
        growth.getSprouts().add(sprout);
        growth.propose(sprout, node.getPosition(), u, growth.getAimLength());
        return sprout;
    }

    // Starts a sprout at a node and steps until the growth component aims it, as if R1 had
    // started it in that update (its creation time is the time of the aim).
    static Sprout sproutOnTheHour(PottsSimulation sim, double[] at) {
        PottsComponentVesselGrowth growth = growth(sim);
        PottsVesselNetwork network = network(sim);
        PottsVesselNode node = network.findNode(at[0], at[1], at[2]);
        assertNotNull(node);
        Sprout sprout = new Sprout(node, null, sim.schedule.getTime() * 0.25, false);
        sprout.excluded.addAll(PottsComponentVesselGrowth.nearby(network, node, 2));
        growth.getSprouts().add(sprout);
        for (int k = 0; k < 4 && sprout.aimEnd == null; k++) {
            sim.schedule.step(sim);
        }
        assertNotNull(sprout.aimEnd);
        sprout.lastGrowth = sim.schedule.getTime() * 0.25;
        return sprout;
    }

    // Gets all cells of a population.
    static List<PottsCell> cellsOf(PottsSimulation sim, int pop) {
        List<PottsCell> list = new ArrayList<>();
        for (Object object : sim.getGrid().getAllObjects()) {
            if (((PottsCell) object).getPop() == pop) {
                list.add((PottsCell) object);
            }
        }
        return list;
    }

    @Test
    public void bud_takesVoxelsFromParent(@TempDir Path dir) throws Exception {
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        addLine(net, 41);
        PottsSimulation sim = run(dir, true, "", net, null);
        PottsComponentVesselCells cells = component(sim);
        Map<PottsCell, Set<Voxel>> before = new HashMap<>();
        for (PottsCell cell : cells.getPieces().keySet()) {
            before.put(cell, voxelsOf(cell));
        }
        assertEquals(3, before.size());

        Sprout sprout = sprout(sim, new double[] {47, 41, 0}, new double[] {0, 1, 0});
        cells.step(sim);

        PottsCell tip = cells.getTipCell(sprout);
        assertNotNull(tip);
        assertEquals(VTIP, tip.getPop());
        assertEquals(N_TIP0, tip.getLocation().getVolume(), 1E-9);
        Set<Voxel> tipVoxels = voxelsOf(tip);
        int losers = 0;
        for (PottsCell cell : before.keySet()) {
            Set<Voxel> now = voxelsOf(cell);
            if (now.equals(before.get(cell))) {
                continue;
            }
            losers++;
            Set<Voxel> lost = new HashSet<>(before.get(cell));
            lost.removeAll(now);
            assertEquals(tipVoxels, lost);
            assertEquals(before.get(cell).size() - N_TIP0, now.size());
        }
        assertEquals(1, losers);
        // On the side facing the aim (+y): above the node row (41 um is voxel row 20).
        for (Voxel v : tipVoxels) {
            assertTrue(v.y >= 20, "voxel " + v);
        }
    }

    @Test
    public void bud_noParent_seedsInFootprint(@TempDir Path dir) throws Exception {
        // An out-of-plane branch whose tip aims down through the plane; no vessel cell nearby.
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        PottsVesselNode a = net.addNode(40, 40, 60, Root.ARTERIAL);
        PottsVesselNode t = net.addNode(60, 40, 16, Root.NONE);
        net.addSegment(a, t, 0.2);
        PottsSimulation sim = run(dir, true, parameter("V_TIP", 30), net, null);
        PottsComponentVesselCells cells = component(sim);
        assertTrue(cells.getPieces().isEmpty());
        PottsCell fib = sim.insertCell(FIB, block(32, 38, 16, 25));
        assertNotNull(fib);
        Set<Voxel> fibBefore = voxelsOf(fib);

        Sprout sprout = sprout(sim, new double[] {60, 40, 16}, new double[] {0.6, 0, -0.8});
        cells.step(sim);

        PottsCell tip = cells.getTipCell(sprout);
        assertNotNull(tip);
        Set<Voxel> footprint = new HashSet<>();
        for (int[] v :
                PottsVesselFootprint.of(sprout.aimStart, sprout.aimEnd, 7.4, DS, 1000, 60, 40)) {
            footprint.add(new Voxel(v[0], v[1], 0));
        }
        Set<Voxel> tipVoxels = voxelsOf(tip);
        assertTrue(tipVoxels.size() >= N_TIP0);
        assertTrue(footprint.containsAll(tipVoxels));
        // Voxels were taken from the fibroblast.
        Set<Voxel> taken = new HashSet<>(fibBefore);
        taken.retainAll(tipVoxels);
        assertFalse(taken.isEmpty());
        for (Voxel v : taken) {
            assertFalse(voxelsOf(fib).contains(v));
        }
    }

    @Test
    public void seed_skipsVesselCells(@TempDir Path dir) throws Exception {
        // As above, with an in-plane vessel under the aim, too far from the tip (z = 16 um) to
        // bud from: the seed takes none of its voxels.
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        PottsVesselNode a = net.addNode(40, 40, 60, Root.ARTERIAL);
        PottsVesselNode t = net.addNode(60, 40, 16, Root.NONE);
        net.addSegment(a, t, 0.2);
        addLine(net, 41);
        PottsSimulation sim = run(dir, true, parameter("V_TIP", 30), net, null);
        PottsComponentVesselCells cells = component(sim);
        Map<PottsCell, Set<Voxel>> before = new HashMap<>();
        for (PottsCell cell : cells.getPieces().keySet()) {
            before.put(cell, voxelsOf(cell));
        }
        assertEquals(3, before.size());

        Sprout sprout = sprout(sim, new double[] {60, 40, 16}, new double[] {0.6, 0, -0.8});
        cells.step(sim);

        PottsCell tip = cells.getTipCell(sprout);
        assertNotNull(tip);
        for (Map.Entry<PottsCell, Set<Voxel>> entry : before.entrySet()) {
            assertEquals(entry.getValue(), voxelsOf(entry.getKey()));
            Set<Voxel> overlap = voxelsOf(tip);
            overlap.retainAll(entry.getValue());
            assertTrue(overlap.isEmpty());
        }
    }

    // Steps until the network has more segments, up to a number of ticks; returns the ticks.
    static int stepUntilAppend(PottsSimulation sim, int segments, int max) {
        for (int tick = 1; tick <= max; tick++) {
            sim.schedule.step(sim);
            if (network(sim).getSegments().size() > segments) {
                return tick;
            }
        }
        return -1;
    }

    @Test
    public void tip_unobstructed_appendsAtFront(@TempDir Path dir) throws Exception {
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        addLine(net, 41);
        PottsSimulation sim = run(dir, true, "", net, null);
        PottsComponentVesselCells cells = component(sim);
        Sprout sprout = sproutOnTheHour(sim, new double[] {47, 41, 0});
        PottsVesselNode base = sprout.tip;
        double created = sprout.lastGrowth;

        int ticks = stepUntilAppend(sim, 3, 8);
        assertTrue(ticks > 0, "no segment appended within 2 hours");

        // The node is at the tip cell's front (within one voxel), at least the aim length away.
        PottsVesselNode node = sprout.tip;
        assertNotSame(base, node);
        double[] p = node.getPosition();
        assertEquals(sprout.front[0], p[0], DS);
        assertEquals(sprout.front[1], p[1], DS);
        // At least the aim's reach in the plane (the aim length, 10 um, less for an aim with a
        // z part), less half a voxel.
        double[] t = base.getPosition();
        double reach = Math.min(10, Math.hypot(sprout.aimEnd[0] - t[0], sprout.aimEnd[1] - t[1]));
        double moved = Math.hypot(p[0] - t[0], p[1] - t[1]);
        // Node coordinates are stored to 0.1 um.
        assertTrue(moved >= reach - DS / 2 - 0.1, "moved " + moved + " of " + reach);
        assertTrue(moved <= 10 * (sprout.lastGrowth - created) + 0.1, "moved " + moved);

        // The tip cell carries on, and the new segment has a vessel cell (the stalk).
        PottsCell tip = cells.getTipCell(sprout);
        assertNotNull(tip);
        assertTrue(tip.getLocation().getVolume() >= N_TIP0);
        PottsVesselEdge segment = network(sim).getSegments(node).get(0);
        PottsCell stalk = cells.getCell(segment);
        assertNotNull(stalk);
        assertEquals(VESSEL, stalk.getPop());
        assertTrue(PottsComponentVesselCells.isConnected(voxelsOf(stalk)));
        assertTrue(PottsComponentVesselCells.isConnected(voxelsOf(tip)));

        // The append is counted, among no more updates than ticks.
        int[] counts = cells.getTipCounts();
        assertEquals(1, counts[2]);
        assertTrue(counts[0] >= 1 && counts[0] <= ticks + 4, "updates " + counts[0]);
    }

    @Test
    public void tip_blockedByWall_regressesAfterTRegress(@TempDir Path dir) throws Exception {
        // At the default anchor strength (LAMBDA_ANCHOR_MAX 50). ECM so dense above the vessel
        // (y >= 46 um) that the tip cannot gain voxels there; it must not tunnel through its
        // parent vessel cell instead, and no segment is appended behind or beside the node.
        double[][] ecm = new double[LENGTH][WIDTH];
        for (int i = 0; i < LENGTH; i++) {
            for (int j = 23; j < WIDTH; j++) {
                ecm[i][j] = 1E6;
            }
        }
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        addLine(net, 41);
        PottsSimulation sim = run(dir, true, "", net, ecm);
        PottsComponentVesselCells cells = component(sim);
        Sprout sprout = sproutOnTheHour(sim, new double[] {47, 41, 0});
        PottsComponentVesselGrowth growth = growth(sim);
        double created = sprout.lastGrowth;

        boolean budded = false;
        int tick = 0;
        while (growth.getSprouts().contains(sprout)) {
            sim.schedule.step(sim);
            tick++;
            assertEquals(3, network(sim).getSegments().size(), "tick " + tick);
            // The tip cell does not pass through its parent vessel: it stays on the aim's side of
            // the vessel axis (y = 41 um), so its front is never behind the node.
            PottsCell tip = cells.getTipCell(sprout);
            budded |= tip != null;
            if (tip != null) {
                for (Voxel v : voxelsOf(tip)) {
                    assertTrue((v.y + 0.5) * DS >= 41, "tick " + tick + ": tip voxel " + v);
                }
            }
            assertTrue(tick <= 4 * 25, "sprout not removed after 25 hours");
        }
        assertTrue(budded);
        // No appends; every update more than an hour after the bud is a stall.
        int[] counts = cells.getTipCounts();
        assertEquals(0, counts[2]);
        assertTrue(counts[1] > 0 && counts[1] < counts[0], counts[1] + " of " + counts[0]);
        assertTrue(counts[0] - counts[1] <= 5, "updates within the first hour " + counts[0]);
        double removed = sim.schedule.getTime() * 0.25;
        assertTrue(removed - created >= 24 - 1E-9, "removed after " + (removed - created) + " h");
        sim.schedule.step(sim);
        assertNull(cells.getTipCell(sprout));

        // The tip cell shrinks away.
        for (int k = 0; k < 20 && !cellsOf(sim, VTIP).isEmpty(); k++) {
            sim.schedule.step(sim);
        }
        assertTrue(cellsOf(sim, VTIP).isEmpty());
        assertEquals(3, cells.getPieces().size());
    }

    @Test
    public void anastomosis_tipBecomesVessel(@TempDir Path dir) throws Exception {
        // Two vessels 20 um apart; a tip from the lower one joins the upper one.
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        addLine(net, 41);
        addLine(net, 61);
        PottsSimulation sim = run(dir, true, parameter("D_ANAST", 12), net, null);
        PottsComponentVesselCells cells = component(sim);
        Sprout sprout = sproutOnTheHour(sim, new double[] {47, 41, 0});
        PottsComponentVesselGrowth growth = growth(sim);

        // Run with no credit for the front (the clock follows the tip) until the tip cell has
        // reached its aim; then give one hour of credit and step only the vessel cells, so the
        // tip cell's voxels just before the append are known.
        PottsCell tip = null;
        for (int tick = 0; tick < 12; tick++) {
            sprout.lastGrowth = sim.schedule.getTime() * 0.25;
            sim.schedule.step(sim);
            sprout.lastGrowth = sim.schedule.getTime() * 0.25;
            tip = cells.getTipCell(sprout);
            if (tip != null) {
                double[] f = PottsComponentVesselCells.front(tip, sprout, DS, 7.4);
                if (growth.appendPoint(sprout, f[0], f[1], sprout.lastGrowth + 1, DS / 2, 7.4)
                        != null) {
                    break;
                }
            }
            tip = null;
        }
        assertNotNull(tip, "tip did not reach its aim within 3 hours");
        assertEquals(6, network(sim).getSegments().size());
        int tipId = tip.getID();
        Set<Voxel> tipVoxels = voxelsOf(tip);
        PottsVesselNode from = sprout.tip;
        sprout.lastGrowth -= 1;
        cells.step(sim);

        assertEquals(8, network(sim).getSegments().size());
        assertTrue(sprout.joined);
        assertFalse(growth.getSprouts().contains(sprout));
        assertNull(cells.getTipCell(sprout));
        assertNull(sim.getPotts().getCell(tipId));
        assertTrue(cellsOf(sim, VTIP).isEmpty());

        // The joining segment has a vessel cell (the former tip cell).
        PottsVesselNode node = sprout.tip;
        assertEquals(2, network(sim).getDegree(node));
        PottsVesselEdge joining = null;
        for (PottsVesselEdge segment : network(sim).getSegments(node)) {
            if (network(sim).getOther(segment, node).getPosition()[1] > 60) {
                joining = segment;
            }
        }
        assertNotNull(joining);
        PottsCell cell = cells.getCell(joining);
        assertNotNull(cell);
        assertEquals(VESSEL, cell.getPop());
        assertTrue(joining.isPerfused());

        // The former tip cell's voxels: the stalk (if any) for the new segment, and the rest,
        // exactly, as the vessel cell of the joining segment.
        Set<Voxel> expected = new HashSet<>(tipVoxels);
        PottsVesselEdge appended = null;
        for (PottsVesselEdge segment : network(sim).getSegments(node)) {
            if (network(sim).getOther(segment, node) == from) {
                appended = segment;
            }
        }
        PottsCell stalk = cells.getCell(appended);
        if (stalk != null) {
            assertNotSame(cell, stalk);
            assertTrue(tipVoxels.containsAll(voxelsOf(stalk)));
            expected.removeAll(voxelsOf(stalk));
        }
        assertEquals(expected, voxelsOf(cell));

        // No guide remains: the old tip id has no vessel energy, and the new cell only an anchor
        // (every flip from medium costs or earns the same amount).
        VesselHamiltonian vessel = (VesselHamiltonian) sim.getPotts().getHamiltonian(Term.VESSEL);
        Set<Double> magnitudes = new HashSet<>();
        for (int x = 0; x < LENGTH; x++) {
            for (int y = 0; y < WIDTH; y++) {
                assertEquals(0, vessel.getDelta(0, tipId, x, y, 0));
                magnitudes.add(Math.abs(vessel.getDelta(0, cell.getID(), x, y, 0)));
            }
        }
        assertEquals(1, magnitudes.size(), "magnitudes " + magnitudes);
    }

    @Test
    public void couple_atStart_beforeAnyGrowthUpdate(@TempDir Path dir) throws Exception {
        // Growth every tick (INTERVAL 1) runs at the same time and ordering as vessel cells. The
        // one-time coupling event (scheduled at time 0 in the potts ordering, see
        // schedule_atComponentsOrdering_everyTick) couples it before either steps.
        PottsSimulation sim = start(dir, setup(true, parameter("INTERVAL", 1)), 0);
        PottsComponentVesselGrowth growth = growth(sim);
        setNetwork(sim, new PottsVesselNetwork(1000));
        assertFalse(growth.isCoupled());
        component(sim).coupleGrowth(sim);
        assertTrue(growth.isCoupled());
        assertTrue(growth.getActiveInPlaneSprouts().isEmpty());

        // Without a growth component, the event does nothing.
        PottsSimulation off = start(Files.createDirectory(dir.resolve("off")), setup(false, ""), 0);
        component(off).coupleGrowth(off);
        assertNull(off.getComponent("GROWTH"));
    }

    @Test
    public void growthAbsent_networkStaticAndNoTips(@TempDir Path dir) throws Exception {
        // A wound-cut vessel: two stubs with dead ends, and no growth component.
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        PottsVesselNode a = net.addNode(21, 41, 0, Root.ARTERIAL);
        PottsVesselNode b = net.addNode(47, 41, 0, Root.NONE);
        PottsVesselNode c = net.addNode(60, 41, 0, Root.NONE);
        PottsVesselNode d = net.addNode(80, 41, 0, Root.NONE);
        PottsVesselNode e = net.addNode(99, 41, 0, Root.VENOUS);
        net.addSegment(a, b, 1);
        net.addSegment(b, c, 0.5);
        net.addSegment(d, e, 1);
        PottsSimulation sim = run(dir, false, "", net, null);
        assertNull(sim.getComponent("GROWTH"));
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        List<PottsVesselEdge> segments = new ArrayList<>(network(sim).getSegments());
        String json = network(sim).toJson();
        int version = vessels.getVersion();

        for (int tick = 0; tick < 48; tick++) {
            sim.schedule.step(sim);
            assertEquals(segments, network(sim).getSegments(), "tick " + tick);
            assertEquals(json, network(sim).toJson(), "tick " + tick);
            assertEquals(version, vessels.getVersion(), "tick " + tick);
            assertTrue(cellsOf(sim, VTIP).isEmpty(), "tick " + tick);
        }
        assertEquals(3, component(sim).getPieces().size());
    }
}
