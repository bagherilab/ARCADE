package arcade.potts.env.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PottsVesselFootprintTest {
    private static final double DS = 3;

    private static final double T = 800;

    private static Set<Long> keys(List<int[]> voxels) {
        Set<Long> keys = new HashSet<>();
        for (int[] v : voxels) {
            keys.add(v[0] * 100000L + v[1]);
        }
        return keys;
    }

    @Test
    public void footprint_inPlaneSegment_isStrip() {
        List<int[]> voxels =
                PottsVesselFootprint.of(
                        new double[] {30, 30, 0}, new double[] {90, 30, 0}, 5, DS, T, 100, 100);
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (int[] v : voxels) {
            assertTrue(Math.abs((v[1] + 0.5) * DS - 30) <= 5 + DS / 2);
            minX = Math.min(minX, v[0]);
            maxX = Math.max(maxX, v[0]);
        }
        assertTrue((minX + 0.5) * DS >= 25 - 1E-9 && (minX + 0.5) * DS <= 25 + DS);
        assertTrue((maxX + 0.5) * DS <= 95 + 1E-9 && (maxX + 0.5) * DS >= 95 - DS);
    }

    @Test
    public void footprint_crossingSegment_isDisk() {
        List<int[]> voxels =
                PottsVesselFootprint.of(
                        new double[] {60, 60, -50}, new double[] {60, 60, 50}, 5, DS, T, 100, 100);
        double expected = Math.PI * Math.pow(5 / DS, 2);
        assertEquals(expected, voxels.size(), 4);
        for (int[] v : voxels) {
            double dx = (v[0] + 0.5) * DS - 60;
            double dy = (v[1] + 0.5) * DS - 60;
            assertTrue(Math.hypot(dx, dy) <= 5 + 1E-9);
        }
    }

    @Test
    public void footprint_periodicImage_matchesDirect() {
        // Slab of thickness 20: z = 19 to -19 is a segment from 19 to 21, the image of -1 to 1.
        List<int[]> wrapped =
                PottsVesselFootprint.of(
                        new double[] {60, 60, 19}, new double[] {80, 60, -19}, 5, DS, 20, 100, 100);
        List<int[]> direct =
                PottsVesselFootprint.of(
                        new double[] {60, 60, -1}, new double[] {80, 60, 1}, 5, DS, 20, 100, 100);
        assertFalse(direct.isEmpty());
        assertEquals(keys(direct), keys(wrapped));
    }

    @Test
    public void footprint_segmentPastEdge_clipped() {
        List<int[]> voxels =
                PottsVesselFootprint.of(
                        new double[] {-20, 30, 0}, new double[] {20, 30, 0}, 5, DS, T, 100, 100);
        assertFalse(voxels.isEmpty());
        boolean hasEdge = false;
        for (int[] v : voxels) {
            assertTrue(v[0] >= 0 && v[1] >= 0 && v[0] < 100 && v[1] < 100);
            hasEdge |= v[0] == 0;
        }
        assertTrue(hasEdge);
        List<int[]> inside =
                PottsVesselFootprint.of(
                        new double[] {0, 30, 0}, new double[] {20, 30, 0}, 5, DS, T, 100, 100);
        // The strip past the edge, clipped at x = 0, equals the strip of the in-lattice half.
        assertEquals(keys(inside), keys(voxels));
        assertEquals(inside.size(), voxels.size());
    }

    @Test
    public void footprint_matchesPythonTool() throws IOException {
        String json;
        try (InputStream in =
                getClass().getResourceAsStream("/resources/vessels/small_unw.VESSELS.json")) {
            assertNotNull(in);
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        PottsVesselNetwork net = PottsVesselNetwork.fromJson(json);
        Set<Long> union = new HashSet<>();
        for (PottsVesselEdge s : net.getSegments()) {
            union.addAll(
                    keys(
                            PottsVesselFootprint.of(
                                    s.getStart().getPosition(),
                                    s.getEnd().getPosition(),
                                    5,
                                    3,
                                    net.getThickness(),
                                    667,
                                    283)));
        }
        // From: tools/.venv/bin/python tools/vessels/footprint.py runs/m5/o2v1/unw/s0 --count 5
        // (the tool rounds nodes to 0.1 um, as ARCADE does)
        assertEquals(2065, union.size());
    }
}
