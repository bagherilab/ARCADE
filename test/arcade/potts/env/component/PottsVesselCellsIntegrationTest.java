package arcade.potts.env.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import arcade.core.ARCADE;
import arcade.core.util.MiniBox;
import arcade.potts.agent.action.PottsActionRecruit;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.FIB;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.block;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.component;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.parameter;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.setNetwork;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.setup;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.start;
import static arcade.potts.env.component.PottsComponentVesselCellsTest.voxelsOf;
import static arcade.potts.env.component.PottsVesselNode.Root;

public class PottsVesselCellsIntegrationTest {
    // Lattice size along x [voxels] (ds = 2 um).
    private static final int LENGTH = 60;

    // Lattice size along y [voxels].
    private static final int WIDTH = 40;

    // Size of the square blocks fibroblasts start in [voxels], about their target volume.
    private static final int FIB_SIZE = 7;

    // Three in-plane segments of 26 um along y = 41 um (voxel row 20) from x = 21 to 99 um.
    static PottsVesselNetwork network() {
        PottsVesselNetwork network = new PottsVesselNetwork(1000);
        PottsVesselNode previous = network.addNode(21, 41, 0, Root.ARTERIAL);
        for (double x = 47; x <= 99; x += 26) {
            Root root = x == 99 ? Root.VENOUS : Root.NONE;
            PottsVesselNode node = network.addNode(x, 41, 0, root);
            network.addSegment(previous, node, 1);
            previous = node;
        }
        return network;
    }

    // Starts a simulation with the network and fibroblasts packed over the whole lattice.
    static PottsSimulation packed(Path dir, long seed) throws Exception {
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, ""), seed);
        setNetwork(sim, network());
        for (int x = 1; x + FIB_SIZE <= LENGTH - 1; x += FIB_SIZE) {
            for (int y = 1; y + FIB_SIZE <= WIDTH - 1; y += FIB_SIZE) {
                assertNotNull(sim.insertCell(FIB, block(x, x + FIB_SIZE, y, y + FIB_SIZE)));
            }
        }
        return sim;
    }

    // Checks that a voxel is within one voxel (in x and y) of a set of voxels.
    static boolean near(Voxel v, Set<Voxel> set) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (set.contains(new Voxel(v.x + dx, v.y + dy, 0))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void stable_matureVesselUnderPressure(@TempDir Path dir) throws Exception {
        PottsSimulation sim = packed(dir, 0);
        PottsComponentVesselCells cells = component(sim);

        for (int tick = 0; tick < 100; tick++) {
            sim.schedule.step(sim);
            assertEquals(3, cells.getPieces().size());
            for (PottsCell cell : cells.getPieces().keySet()) {
                Set<Voxel> footprint = voxelsOf(cells.getPieces().get(cell));
                for (Voxel v : voxelsOf(cell)) {
                    assertTrue(near(v, footprint), "tick " + tick + " voxel " + v);
                }
            }
            assertEquals(0, cells.getDriftCount(), "tick " + tick);
            assertEquals(0, cells.getFragmentCount(), "tick " + tick);
        }
    }

    @Test
    public void remove_segmentGone_cellShrinksAway(@TempDir Path dir) throws Exception {
        PottsSimulation sim = packed(dir, 0);
        PottsComponentVesselCells cells = component(sim);
        for (int tick = 0; tick < 5; tick++) {
            sim.schedule.step(sim);
        }

        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsVesselNetwork net = vessels.getNetwork();
        PottsVesselEdge last = net.getSegments().get(2);
        PottsCell cell = cells.getCell(last);
        assertNotNull(cell);
        int id = cell.getID();
        net.removeSegment(last);
        vessels.refresh();

        int ticks = 0;
        while (sim.getPotts().getCell(id) != null && ticks < 50) {
            sim.schedule.step(sim);
            ticks++;
        }
        assertNull(sim.getPotts().getCell(id), "cell not removed after 50 ticks");
        assertTrue(((PottsLocation) cell.getLocation()).getVoxels().isEmpty());
        for (int[][] plane : sim.getPotts().ids) {
            for (int[] row : plane) {
                for (int value : row) {
                    assertNotEquals(id, value);
                }
            }
        }
        assertEquals(2, cells.getPieces().size());
        assertFalse(cells.getPieces().containsKey(cell));
    }

    @Test
    public void remove_isolatedGrazingPiece_noCell(@TempDir Path dir) throws Exception {
        // A segment that only grazes the plane gives fewer than N_MIN voxels and no cell.
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, parameter("N_MIN", 6)), 0);
        PottsVesselNetwork net = new PottsVesselNetwork(1000);
        PottsVesselNode a = net.addNode(40, 41, 4.9, Root.ARTERIAL);
        PottsVesselNode b = net.addNode(44, 41, 4.9, Root.VENOUS);
        net.addSegment(a, b, 1);
        setNetwork(sim, net);
        sim.schedule.step(sim);
        assertEquals(0, component(sim).getPieces().size());
    }

    // An O2-vessels setup (as in the vessels integration test) without vessel cells.
    static String offSetup() {
        String tissue =
                "<population id=\"TIS\" class=\"tissue\" init=\"10\">"
                        + "<population.parameter id=\"CRITICAL_VOLUME\" value=\"400\" />"
                        + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                        + "<population.process id=\"UPTAKE\" />"
                        + "<population.parameter process=\"uptake\" id=\"RATE_O2\""
                        + " value=\"15270\" /></population>";
        String layer =
                "<layer id=\"O2\"><layer.operation id=\"DIFFUSER\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"DIFFUSIVITY\""
                        + " value=\"2000\" />"
                        + "<layer.parameter operation=\"diffuser\" id=\"STEADY\" value=\"1\" />"
                        + "</layer>";
        String component =
                "<component id=\"VESSELS\" class=\"vessels\">"
                        + "<component.parameter id=\"P_O2\" value=\"20\" />"
                        + "<component.register id=\"O2\" /></component>";
        return "<set><series name=\"off\" ticks=\"3\" interval=\"1\" start=\"0\" end=\"0\""
                + " length=\"60\" width=\"40\" height=\"1\" ds=\"3\" dt=\"0.25\">"
                + "<populations>"
                + tissue
                + "</populations><layers>"
                + layer
                + "</layers><components>"
                + component
                + "</components></series></set>";
    }

    // Runs the off setup into a directory and returns the sorted output file names.
    static List<String> runOff(Path dir, Path init) throws Exception {
        Path file = dir.resolve("off.xml");
        Files.writeString(file, offSetup());
        ARCADE.main(
                new String[] {
                    "potts",
                    file.toString(),
                    dir.toAbsolutePath() + "/",
                    "--savelayers",
                    "--loadpath",
                    init.toAbsolutePath() + "/init_[#]_000000",
                    "--loadlayers"
                });
        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            files.map(p -> p.getFileName().toString())
                    .filter(n -> n.startsWith("off_"))
                    .sorted()
                    .forEach(names::add);
        }
        return names;
    }

    @Test
    public void off_noComponent_byteIdentical(@TempDir Path dir) throws Exception {
        Path init = Files.createDirectory(dir.resolve("init"));
        Files.writeString(
                init.resolve("init_0000_000000.VESSELS.json"), network().toJson().toString());
        HashMap<String, double[][][]> layers = new HashMap<>();
        layers.put("O2", new double[1][60][40]);
        Files.writeString(init.resolve("init_0000_000000.LAYERS.json"), new Gson().toJson(layers));

        Path first = Files.createDirectory(dir.resolve("a"));
        Path second = Files.createDirectory(dir.resolve("b"));
        List<String> namesA = runOff(first, init);
        List<String> namesB = runOff(second, init);

        // Same files, byte for byte, in both runs.
        assertFalse(namesA.isEmpty());
        assertEquals(namesA, namesB);
        for (String name : namesA) {
            assertTrue(
                    Arrays.equals(
                            Files.readAllBytes(first.resolve(name)),
                            Files.readAllBytes(second.resolve(name))),
                    name);
        }

        // Only the tissue population appears: no vessel cells without the component.
        for (int tick = 0; tick <= 3; tick++) {
            String json =
                    Files.readString(
                            first.resolve(String.format("off_0000_%06d.CELLS.json", tick)));
            for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
                assertEquals(1, element.getAsJsonObject().get("pop").getAsInt());
            }
        }
    }

    // Population code of MAC, declared after FIB, VESSEL and VTIP.
    private static final int MAC = 4;

    // MAC critical volume [voxels], as in the default setups (4 x 4 voxels). Much smaller MACs (8
    // voxels) collapse to one voxel even in open medium, so they cannot test exit.
    private static final int MAC_VOXELS = 16;

    // Ticks a MAC entered on one voxel is given to grow.
    private static final int GROW_TICKS = 40;

    // MAC-VESSEL adhesion, twice the default medium adhesion of 50.
    private static final double ADHESION_MAC_VESSEL = 100;

    // The packed setup with a MAC population of the given critical volume [voxels of 8 um^3] and
    // MAC-VESSEL adhesion set in both directions.
    static String macSetup(int macVoxels) {
        String mac =
                "<population id=\"MAC\" class=\"macrophage\" init=\"0\">"
                        + "<population.parameter id=\"CRITICAL_VOLUME\" value=\""
                        + macVoxels * 8
                        + "\" />"
                        + "<population.parameter id=\"CRITICAL_HEIGHT\" value=\"2\" />"
                        + "</population>";
        String adhesion = "";
        for (String target : new String[] {"MAC:VESSEL", "VESSEL:MAC"}) {
            adhesion +=
                    "<potts.parameter term=\"adhesion\" id=\"ADHESION\" value=\""
                            + ADHESION_MAC_VESSEL
                            + "\" target=\""
                            + target
                            + "\" />";
        }
        String term = "<potts.term id=\"VESSEL\" />";
        return setup(LENGTH, WIDTH, "")
                .replace("</populations>", mac + "</populations>")
                .replace(term, term + adhesion);
    }

    // Starts a simulation with the network, the MAC population, and optionally packed fibroblasts,
    // and steps it once so the vessel cells are built.
    static PottsSimulation withMac(Path dir, int macVoxels, boolean pack) throws Exception {
        return withMac(dir, macVoxels, pack, 0);
    }

    // As above, with a given seed.
    static PottsSimulation withMac(Path dir, int macVoxels, boolean pack, long seed)
            throws Exception {
        PottsSimulation sim = start(dir, macSetup(macVoxels), seed);
        setNetwork(sim, network());
        if (pack) {
            for (int x = 1; x + FIB_SIZE <= LENGTH - 1; x += FIB_SIZE) {
                for (int y = 1; y + FIB_SIZE <= WIDTH - 1; y += FIB_SIZE) {
                    assertNotNull(sim.insertCell(FIB, block(x, x + FIB_SIZE, y, y + FIB_SIZE)));
                }
            }
        }
        sim.schedule.step(sim);
        return sim;
    }

    // A recruit action for MAC on vessel sites, with the vessel cell component.
    static PottsActionRecruit recruit(PottsSimulation sim) {
        return recruit(sim, 4);
    }

    // A recruit action for MAC on vessel sites, inserting within a distance [um] of a site.
    static PottsActionRecruit recruit(PottsSimulation sim, double distance) {
        MiniBox parameters = new MiniBox();
        parameters.put("SOURCE", "VESSELS");
        parameters.put("R_VESSEL", 1);
        parameters.put("VESSELS", "VESSELS");
        parameters.put("SIGNALS", "NONE");
        parameters.put("K_RECRUIT", 1);
        parameters.put("D_RECRUIT", distance);
        parameters.put("VESSEL_CELLS", "VESSEL_CELLS");
        PottsActionRecruit action = new PottsActionRecruit(parameters);
        action.register(sim, "MAC");
        return action;
    }

    // Gets the centre voxel of the footprint of a vessel cell.
    static Voxel centre(PottsComponentVesselCells cells, PottsCell cell) {
        Set<Voxel> footprint = voxelsOf(cells.getPieces().get(cell));
        double x = footprint.stream().mapToInt(v -> v.x).average().getAsDouble();
        double y = footprint.stream().mapToInt(v -> v.y).average().getAsDouble();
        Voxel site = new Voxel((int) Math.round(x), (int) Math.round(y), 0);
        assertTrue(footprint.contains(site));
        return site;
    }

    // Gets the middle vessel cell (of the second segment).
    static PottsCell middle(PottsSimulation sim) {
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsCell cell = component(sim).getCell(vessels.getNetwork().getSegments().get(1));
        assertNotNull(cell);
        return cell;
    }

    @Test
    public void adhesion_macVessel_loadedAsPair(@TempDir Path dir) throws Exception {
        PottsSimulation sim = start(dir, macSetup(8), 0);
        MiniBox potts = ((PottsSeries) sim.getSeries()).potts;
        assertEquals(ADHESION_MAC_VESSEL, potts.getDouble("adhesion/ADHESION:MAC:VESSEL"));
        assertEquals(ADHESION_MAC_VESSEL, potts.getDouble("adhesion/ADHESION:VESSEL:MAC"));
        assertEquals(50, potts.getDouble("adhesion/ADHESION:MAC:*"));
        assertEquals(50, potts.getDouble("adhesion/ADHESION:MAC:FIB"));
    }

    @Test
    public void recruit_siteInVessel_entersOnFreeVoxelNextToVessel(@TempDir Path dir)
            throws Exception {
        PottsSimulation sim = withMac(dir, MAC_VOXELS, false);
        PottsComponentVesselCells cells = component(sim);
        PottsCell vessel = middle(sim);
        Set<Voxel> before = voxelsOf(vessel);
        PottsActionRecruit action = recruit(sim, 10);

        PottsCell mac = action.recruitAt(sim, MAC, centre(cells, vessel));

        // One free voxel within D_RECRUIT (10 um, 3 voxels) of the site; the vessel cell keeps all.
        assertNotNull(mac);
        assertEquals(MAC, mac.getPop());
        assertEquals(1, mac.getLocation().getVolume(), 1E-9);
        assertEquals(before, voxelsOf(vessel));
        assertFalse(before.contains(voxelsOf(mac).iterator().next()));
        assertEquals(1, action.getRecruited());
        assertEquals(1, action.getExits());
        assertEquals(0, action.getFailures());

        // In packed tissue there is no free voxel: the attempt fails and is counted.
        PottsSimulation packedSim =
                withMac(Files.createDirectory(dir.resolve("p")), MAC_VOXELS, true);
        PottsCell packedVessel = middle(packedSim);
        PottsActionRecruit packedAction = recruit(packedSim, 10);
        assertNull(
                packedAction.recruitAt(packedSim, MAC, centre(component(packedSim), packedVessel)));
        assertEquals(0, packedAction.getRecruited());
        assertEquals(0, packedAction.getExits());
        assertEquals(1, packedAction.getFailures());
    }

    @Test
    public void recruit_enteredMac_growsOutsideFootprint(@TempDir Path dir) throws Exception {
        // A MAC that enters on one voxel grows toward its critical volume and stays off the
        // footprint; several seeds, as growth is stochastic.
        for (long seed = 0; seed < 4; seed++) {
            Path seedDir = Files.createDirectory(dir.resolve("s" + seed));
            PottsSimulation sim = withMac(seedDir, MAC_VOXELS, false, seed);
            PottsComponentVesselCells cells = component(sim);
            PottsCell vessel = middle(sim);
            Set<Voxel> footprint = voxelsOf(cells.getPieces().get(vessel));
            Set<Voxel> all = new HashSet<>();
            for (PottsCell cell : cells.getPieces().keySet()) {
                all.addAll(voxelsOf(cells.getPieces().get(cell)));
            }
            PottsCell mac = recruit(sim, 10).recruitAt(sim, MAC, centre(cells, vessel));
            assertNotNull(mac);

            for (int tick = 0; tick < GROW_TICKS; tick++) {
                sim.schedule.step(sim);
            }

            assertSame(mac, sim.getPotts().getCell(mac.getID()));
            assertTrue(
                    mac.getLocation().getVolume() >= MAC_VOXELS / 2,
                    "seed " + seed + ": MAC volume " + mac.getLocation().getVolume());
            for (Voxel v : voxelsOf(mac)) {
                assertFalse(all.contains(v), "seed " + seed + ": MAC voxel " + v + " in footprint");
            }
            Set<Voxel> now = voxelsOf(vessel);
            for (Voxel v : footprint) {
                assertTrue(near(v, now), "seed " + seed + ": footprint voxel " + v + " lost");
            }
        }
    }

    @Test
    public void remove_withMacNextToIt_cellShrinksAway(@TempDir Path dir) throws Exception {
        PottsSimulation sim = withMac(dir, MAC_VOXELS, false);
        PottsComponentVesselCells cells = component(sim);
        PottsComponentVessels vessels = (PottsComponentVessels) sim.getComponent("VESSELS");
        PottsVesselEdge last = vessels.getNetwork().getSegments().get(2);
        PottsCell cell = cells.getCell(last);
        assertNotNull(cell);
        int id = cell.getID();
        PottsCell mac = recruit(sim, 10).recruitAt(sim, MAC, centre(cells, cell));
        assertNotNull(mac);

        vessels.getNetwork().removeSegment(last);
        vessels.refresh();
        int ticks = 0;
        while (sim.getPotts().getCell(id) != null && ticks < 50) {
            sim.schedule.step(sim);
            ticks++;
        }

        assertNull(sim.getPotts().getCell(id), "cell not removed after 50 ticks");
        assertTrue(((PottsLocation) cell.getLocation()).getVoxels().isEmpty());
        for (int[] row : sim.getPotts().ids[0]) {
            for (int value : row) {
                assertNotEquals(id, value);
            }
        }
        assertFalse(cells.getPieces().containsKey(cell));
        assertSame(mac, sim.getPotts().getCell(mac.getID()));
        assertTrue(mac.getLocation().getVolume() > 0);
    }

    @Test
    public void build_footprintCutsFibroblast_ownerStaysConnected(@TempDir Path dir)
            throws Exception {
        // A tall fibroblast crosses the footprint band (row 20). Taking the band would cut it in
        // two; a cell left in pieces hangs PottsLocation.split when it divides.
        PottsSimulation sim = start(dir, setup(LENGTH, WIDTH, ""), 0);
        setNetwork(sim, network());
        PottsCell fib = sim.insertCell(FIB, block(24, 28, 4, 36));
        assertNotNull(fib);
        double before = fib.getLocation().getVolume();
        PottsComponentVesselCells cells = component(sim);

        sim.schedule.step(sim);

        assertFalse(cells.getPieces().isEmpty());
        Set<Voxel> kept = voxelsOf(fib);
        assertFalse(kept.isEmpty());
        assertTrue(kept.size() < before);
        assertTrue(PottsComponentVesselCells.isConnected(kept), "fibroblast left in pieces");
        int[][] ids = sim.getPotts().ids[0];
        for (int x = 0; x < ids.length; x++) {
            for (int y = 0; y < ids[x].length; y++) {
                assertEquals(
                        kept.contains(new Voxel(x, y, 0)),
                        ids[x][y] == fib.getID(),
                        "lattice and location differ at " + x + "," + y);
            }
        }
    }
}
