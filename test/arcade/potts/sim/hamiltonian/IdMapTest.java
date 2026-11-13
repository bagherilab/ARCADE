package arcade.potts.sim.hamiltonian;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IdMapTest {
    @Test
    public void get_afterPut_returnsValue() {
        IdMap<String> map = new IdMap<>();
        map.put(3, "a");
        map.put(100, "b");
        assertEquals("a", map.get(3));
        assertEquals("b", map.get(100));
        assertEquals("a", map.get((Object) 3));
        assertNull(map.get(4));
        assertNull(map.get(10000));
        assertEquals(2, map.size());
    }

    @Test
    public void get_nullValue_returnsNull() {
        IdMap<String> map = new IdMap<>();
        map.put(0, null);
        assertNull(map.get(0));
        assertTrue(map.containsKey(0));
        assertEquals(1, map.size());
    }

    @Test
    public void get_afterRemove_returnsNull() {
        IdMap<String> map = new IdMap<>();
        map.put(5, "a");
        assertEquals("a", map.remove(5));
        assertNull(map.get(5));
        assertFalse(map.containsKey(5));
    }

    @Test
    public void get_afterReplace_returnsNewValue() {
        IdMap<String> map = new IdMap<>();
        map.put(5, "a");
        assertEquals("a", map.put(5, "b"));
        assertEquals("b", map.get(5));
    }

    @Test
    public void get_negativeId_usesMap() {
        IdMap<String> map = new IdMap<>();
        map.put(-1, "a");
        assertEquals("a", map.get(-1));
        map.remove(-1);
        assertNull(map.get(-1));
    }

    @Test
    public void putAll_called_mirrorsValues() {
        IdMap<String> map = new IdMap<>();
        map.putAll(Map.of(1, "a", 40, "b"));
        assertEquals("a", map.get(1));
        assertEquals("b", map.get(40));
    }

    @Test
    public void clear_called_removesValues() {
        IdMap<String> map = new IdMap<>();
        map.put(1, "a");
        map.clear();
        assertNull(map.get(1));
        assertTrue(map.isEmpty());
    }
}
