package arcade.potts.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static org.junit.jupiter.api.Assertions.*;
import static arcade.potts.integration.PottsWoundIntegrationTest.cells;
import static arcade.potts.integration.PottsWoundIntegrationTest.population;
import static arcade.potts.integration.PottsWoundIntegrationTest.run;
import static arcade.potts.integration.PottsWoundIntegrationTest.setup;

public class PottsInsertIntegrationTest {
    // Population code of TIS (codes follow the order of populations in the setup).
    private static final int TIS = 1;

    // Population code of MAC.
    private static final int MAC = 2;

    // Population code of MYO.
    private static final int MYO = 3;

    // Number of ticks run.
    private static final int TICKS = 4;

    // Creates an insert action for a population at a tick.
    static String insert(String id, String pop, int tick, int number) {
        return "<action id=\""
                + id
                + "\" class=\"insert\">"
                + "<action.parameter id=\"TIME_DELAY\" value=\""
                + tick
                + "\" />"
                + "<action.parameter id=\"INSERT_NUMBER\" value=\""
                + number
                + "\" />"
                + "<action.register id=\""
                + pop
                + "\" /></action>";
    }

    // Runs 2 initial tissue cells, inserting 3 MAC at tick 1 and 1 MYO at tick 2.
    // The MYO has INF_APOPTOSIS below zero, so it becomes apoptotic on its first step.
    static String runInsert(Path dir) throws Exception {
        String populations =
                population("TIS", "tissue", 2, "")
                        + population("MAC", "macrophage", 0, "")
                        + population(
                                "MYO",
                                "myofibroblast",
                                0,
                                "<population.parameter id=\"INF_APOPTOSIS\" value=\"-1\" />");
        String actions = insert("INSERT_MAC", "MAC", 1, 3) + insert("INSERT_MYO", "MYO", 2, 1);
        return run(dir, "insert", setup("insert", TICKS, populations, actions));
    }

    // Gets the cells of a population at a tick.
    static List<JsonObject> ofPop(String prefix, int tick, int pop) throws Exception {
        List<JsonObject> list = new ArrayList<>();
        for (JsonElement element : cells(prefix, tick)) {
            JsonObject cell = element.getAsJsonObject();
            if (cell.get("pop").getAsInt() == pop) {
                list.add(cell);
            }
        }
        return list;
    }

    // Reads the locations at a tick.
    static JsonArray locations(String prefix, int tick) throws Exception {
        String json =
                Files.readString(Path.of(prefix + String.format("%06d.LOCATIONS.json", tick)));
        return JsonParser.parseString(json).getAsJsonArray();
    }

    // Saved file N holds the state at the start of tick N, so a cell inserted at tick t is
    // first saved in file t + 1.
    @Test
    public void insert_atTimeDelay_appearsInNextSavedTickWithNewIds(@TempDir Path dir)
            throws Exception {
        String prefix = runInsert(dir);

        assertEquals(0, ofPop(prefix, 1, MAC).size());
        assertEquals(3, ofPop(prefix, 2, MAC).size());
        assertEquals(0, ofPop(prefix, 2, MYO).size());
        assertEquals(1, ofPop(prefix, 3, MYO).size());

        int maxTissueId = 0;
        for (JsonObject cell : ofPop(prefix, 0, TIS)) {
            maxTissueId = Math.max(maxTissueId, cell.get("id").getAsInt());
        }
        assertTrue(maxTissueId > 0);

        HashSet<Integer> ids = new HashSet<>();
        for (JsonElement element : cells(prefix, 3)) {
            JsonObject cell = element.getAsJsonObject();
            int id = cell.get("id").getAsInt();
            assertTrue(ids.add(id));
            if (cell.get("pop").getAsInt() != TIS) {
                assertTrue(id > maxTissueId);
            }
        }
    }

    @Test
    public void insert_insertedCells_stepFromNextTick(@TempDir Path dir) throws Exception {
        String prefix = runInsert(dir);

        for (JsonObject cell : ofPop(prefix, 2, MAC)) {
            assertEquals(0, cell.get("age").getAsInt());
        }
        for (JsonObject cell : ofPop(prefix, 3, MAC)) {
            assertEquals(1, cell.get("age").getAsInt());
        }
    }

    @Test
    public void insert_run_keepsVoxelAccountingConsistent(@TempDir Path dir) throws Exception {
        String prefix = runInsert(dir);

        for (int tick = 0; tick <= TICKS; tick++) {
            HashMap<Integer, Integer> sizes = new HashMap<>();
            HashSet<String> claimed = new HashSet<>();
            for (JsonElement element : locations(prefix, tick)) {
                JsonObject location = element.getAsJsonObject();
                int size = 0;
                for (JsonElement region : location.getAsJsonArray("location")) {
                    for (JsonElement voxel : region.getAsJsonObject().getAsJsonArray("voxels")) {
                        assertTrue(claimed.add(voxel.toString()), "voxel in two cells: " + voxel);
                        size++;
                    }
                }
                sizes.put(location.get("id").getAsInt(), size);
            }

            JsonArray cells = cells(prefix, tick);
            assertEquals(cells.size(), sizes.size());
            for (JsonElement element : cells) {
                JsonObject cell = element.getAsJsonObject();
                int id = cell.get("id").getAsInt();
                assertEquals(sizes.get(id).intValue(), cell.get("voxels").getAsInt());
            }
        }
    }

    // TODO (M4.7): switch to an inserted macrophage once MACROPHAGE_DEATH exists.
    @Test
    public void insert_myofibroblastBelowInfThreshold_becomesApoptotic(@TempDir Path dir)
            throws Exception {
        String prefix = runInsert(dir);

        List<JsonObject> inserted = ofPop(prefix, 3, MYO);
        assertEquals(1, inserted.size());
        assertNotEquals("APOPTOTIC", inserted.get(0).get("state").getAsString());

        List<JsonObject> stepped = ofPop(prefix, 4, MYO);
        assertEquals(1, stepped.size());
        assertEquals("APOPTOTIC", stepped.get(0).get("state").getAsString());
    }
}
