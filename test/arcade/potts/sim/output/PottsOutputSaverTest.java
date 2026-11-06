package arcade.potts.sim.output;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputSerializerTest;
import arcade.core.util.MiniBox;
import arcade.potts.env.component.PottsComponentVessels;
import arcade.potts.env.component.PottsVesselNetwork;
import arcade.potts.env.component.PottsVesselNode;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsOutputSaverTest {
    @Test
    public void makeGSON_called_returnsObjects() {
        Series series = mock(Series.class);
        PottsOutputSaver saver = new PottsOutputSaver(series);
        Gson gson = saver.makeGSON();
        OutputSerializerTest.checkAdaptors(gson);
        PottsOutputSerializerTest.checkAdaptors(gson);
    }

    static PottsSimulation mockSimulation(TreeMap<String, double[][][]> layers) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(0).when(sim).getSeed();
        doReturn(layers).when(sim).getLayers();
        doReturn(new ArrayList<>()).when(sim).getCells();
        doReturn(new ArrayList<>()).when(sim).getLocations();
        return sim;
    }

    @Test
    public void save_withLatticeAndOffset_writesOffsetLayerFile(@TempDir Path path)
            throws Exception {
        PottsSeries series = mock(PottsSeries.class);
        doReturn(path.toString() + "/run").when(series).getPrefix();
        doReturn(1).when(series).getInterval();
        PottsOutputSaver saver = new PottsOutputSaver(series);
        saver.saveLattice = true;
        saver.tickOffset = 100;
        TreeMap<String, double[][][]> layers = new TreeMap<>();
        layers.put("PDGF", new double[][][] {{{0.1, 1.0 / 3}}});
        saver.equip(mockSimulation(layers));
        saver.save(5);
        Path layerFile = path.resolve("run_0000_000105.LAYERS.json");
        assertTrue(Files.exists(layerFile));
        HashMap<String, double[][][]> read =
                new Gson()
                        .fromJson(
                                Files.readString(layerFile),
                                new TypeToken<HashMap<String, double[][][]>>() {}.getType());
        assertEquals(1.0 / 3, read.get("PDGF")[0][0][1], 0);
        assertTrue(Files.exists(path.resolve("run_0000_000105.CELLS.json")));
    }

    @Test
    public void save_noLattice_writesNoLayerFile(@TempDir Path path) throws Exception {
        PottsSeries series = mock(PottsSeries.class);
        doReturn(path.toString() + "/run").when(series).getPrefix();
        doReturn(1).when(series).getInterval();
        PottsOutputSaver saver = new PottsOutputSaver(series);
        saver.equip(mockSimulation(new TreeMap<>()));
        saver.save(5);
        assertTrue(Files.exists(path.resolve("run_0000_000005.CELLS.json")));
        assertFalse(Files.exists(path.resolve("run_0000_000005.LAYERS.json")));
    }

    @Test
    public void save_vesselComponent_writesNetworkOnlyWhenChanged(@TempDir Path path)
            throws Exception {
        PottsSeries series = mock(PottsSeries.class);
        doReturn(path.toString() + "/run").when(series).getPrefix();
        doReturn(1).when(series).getInterval();
        series.components = new HashMap<>();
        series.components.put("VESSELS", new MiniBox());

        PottsComponentVessels vessels = mock(PottsComponentVessels.class);
        PottsVesselNetwork network = new PottsVesselNetwork(100);
        network.addNode(1, 2, 3, PottsVesselNode.Root.ARTERIAL);
        doReturn(network).when(vessels).getNetwork();
        doReturn(1).when(vessels).getVersion();

        PottsSimulation sim = mockSimulation(new TreeMap<>());
        doReturn(series).when(sim).getSeries();
        doReturn(vessels).when(sim).getComponent("VESSELS");
        PottsOutputSaver saver = new PottsOutputSaver(series);
        saver.equip(sim);

        saver.save(0);
        saver.save(1);
        Path first = path.resolve("run_0000_000000.VESSELS.json");
        assertTrue(Files.exists(first));
        assertEquals(network.toJson(), Files.readString(first));
        assertFalse(Files.exists(path.resolve("run_0000_000001.VESSELS.json")));

        doReturn(2).when(vessels).getVersion();
        saver.save(2);
        assertTrue(Files.exists(path.resolve("run_0000_000002.VESSELS.json")));
    }

    @Test
    public void save_noComponents_writesNoVesselFile(@TempDir Path path) throws Exception {
        PottsSeries series = mock(PottsSeries.class);
        doReturn(path.toString() + "/run").when(series).getPrefix();
        doReturn(1).when(series).getInterval();
        PottsSimulation sim = mockSimulation(new TreeMap<>());
        doReturn(series).when(sim).getSeries();
        PottsOutputSaver saver = new PottsOutputSaver(series);
        saver.equip(sim);
        saver.save(0);
        assertFalse(Files.exists(path.resolve("run_0000_000000.VESSELS.json")));
    }
}
