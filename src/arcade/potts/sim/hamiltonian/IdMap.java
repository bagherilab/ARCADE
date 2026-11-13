package arcade.potts.sim.hamiltonian;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Map from cell id to a value, with a fast lookup by primitive id.
 *
 * <p>Hamiltonians look up a per-cell configuration for every voxel considered in every Monte Carlo
 * step, often for each neighbor. A {@link HashMap} keyed by {@link Integer} boxes the id and hashes
 * it on each lookup. This map keeps the {@link HashMap} behavior (so existing code and tests that
 * call {@code put}, {@code remove}, {@code containsKey}, or {@code size} are unchanged) and mirrors
 * non-negative keys in an array, so that {@link #get(int)} is an array read.
 *
 * <p>All writes must go through {@link #put}, {@link #remove(Object)}, {@link #putAll}, or {@link
 * #clear}, which keep the array in step. Other {@link Map} default writes ({@code compute}, {@code
 * merge}, entry or view modification) are not mirrored and must not be used.
 *
 * @param <V> the type of values
 */
public class IdMap<V> extends HashMap<Integer, V> {
    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** Values indexed by id, for ids that are non-negative. */
    private transient Object[] values = new Object[16];

    /** Creates an empty {@code IdMap}. */
    public IdMap() {
        super();
    }

    /**
     * Gets the value for an id.
     *
     * @param id the cell id
     * @return the value, or {@code null} if there is none
     */
    @SuppressWarnings("unchecked")
    public V get(int id) {
        if (id >= 0) {
            return id < values.length ? (V) values[id] : null;
        }
        return super.get(id);
    }

    @Override
    public V put(Integer key, V value) {
        V previous = super.put(key, value);
        if (key != null && key >= 0) {
            if (key >= values.length) {
                values = Arrays.copyOf(values, Math.max(key + 1, values.length * 2));
            }
            values[key] = value;
        }
        return previous;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> map) {
        for (Map.Entry<? extends Integer, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        V previous = super.remove(key);
        if (key instanceof Integer) {
            int id = (Integer) key;
            if (id >= 0 && id < values.length) {
                values[id] = null;
            }
        }
        return previous;
    }

    @Override
    public void clear() {
        super.clear();
        Arrays.fill(values, null);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
