package arcade.potts.env.lattice;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.potts.sim.PottsSeries;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsLatticeFactoryTest {
    static PottsSeries series(HashMap<String, MiniBox> layers) throws Exception {
        PottsSeries series = mock(PottsSeries.class);
        for (String[] f : new String[][] {{"length", "7"}, {"width", "5"}, {"height", "1"}}) {
            java.lang.reflect.Field field = Series.class.getDeclaredField(f[0]);
            field.setAccessible(true);
            field.setInt(series, Integer.parseInt(f[1]));
        }
        for (String[] f : new String[][] {{"ds", "3"}, {"dt", "0.5"}}) {
            java.lang.reflect.Field field = Series.class.getDeclaredField(f[0]);
            field.setAccessible(true);
            field.setDouble(series, Double.parseDouble(f[1]));
        }
        series.layers = layers;
        return series;
    }

    static MiniBox layer(double initial) {
        MiniBox box = new MiniBox();
        box.put("INITIAL_CONCENTRATION", initial);
        return box;
    }

    @Test
    public void createLattices_nullLayers_createsNone() throws Exception {
        PottsLatticeFactory factory = new PottsLatticeFactory();
        factory.createLattices(series(null));
        assertEquals(0, factory.lattices.size());
    }

    @Test
    public void createLattices_givenLayers_sizesAndInitializes() throws Exception {
        HashMap<String, MiniBox> layers = new HashMap<>();
        layers.put("PDGF", layer(1.5));
        PottsLatticeFactory factory = new PottsLatticeFactory();
        factory.createLattices(series(layers));
        PottsLattice lattice = factory.lattices.get("PDGF");
        assertEquals(7, lattice.getLength());
        assertEquals(5, lattice.getWidth());
        assertEquals(3, lattice.getDs(), 1E-12);
        assertEquals(0.5, lattice.getDt(), 1E-12);
        assertEquals(1.5, lattice.getField()[0][6][4], 1E-12);
    }

    @Test
    public void createLattices_multipleLayers_createsEachIndependently() throws Exception {
        HashMap<String, MiniBox> layers = new HashMap<>();
        layers.put("A", layer(1));
        layers.put("B", layer(2));
        PottsLatticeFactory factory = new PottsLatticeFactory();
        factory.createLattices(series(layers));
        assertEquals(2, factory.lattices.size());
        assertNotSame(factory.lattices.get("A"), factory.lattices.get("B"));
        assertEquals(1, factory.lattices.get("A").getField()[0][0][0], 1E-12);
        assertEquals(2, factory.lattices.get("B").getField()[0][0][0], 1E-12);
    }

    @Test
    public void createLattices_loadedField_overridesInitial() throws Exception {
        HashMap<String, MiniBox> layers = new HashMap<>();
        layers.put("ECM", layer(0));
        layers.put("PDGF", layer(2));
        double[][][] loaded = new double[1][7][5];
        loaded[0][3][2] = 9.0;
        HashMap<String, double[][][]> fields = new HashMap<>();
        fields.put("ECM", loaded);
        PottsLatticeFactory factory = new PottsLatticeFactory();
        factory.createLattices(series(layers), fields);
        assertEquals(9.0, factory.lattices.get("ECM").getField()[0][3][2], 0);
        assertEquals(2.0, factory.lattices.get("PDGF").getField()[0][3][2], 0);
    }

    @Test
    public void createLattices_loadedWrongDimensions_throws() throws Exception {
        HashMap<String, MiniBox> layers = new HashMap<>();
        layers.put("ECM", layer(0));
        HashMap<String, double[][][]> fields = new HashMap<>();
        fields.put("ECM", new double[1][6][5]);
        PottsLatticeFactory factory = new PottsLatticeFactory();
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.createLattices(series(layers), fields));
    }

    @Test
    public void createLattices_loadedUnknownLayer_throws() throws Exception {
        HashMap<String, MiniBox> layers = new HashMap<>();
        layers.put("ECM", layer(0));
        HashMap<String, double[][][]> fields = new HashMap<>();
        fields.put("NOPE", new double[1][7][5]);
        PottsLatticeFactory factory = new PottsLatticeFactory();
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.createLattices(series(layers), fields));
    }

    @Test
    public void createLattices_loadedRaggedRow_throws() throws Exception {
        HashMap<String, MiniBox> layers = new HashMap<>();
        layers.put("ECM", layer(0));
        double[][][] ragged = new double[1][7][5];
        ragged[0][4] = new double[3];
        HashMap<String, double[][][]> fields = new HashMap<>();
        fields.put("ECM", ragged);
        PottsLatticeFactory factory = new PottsLatticeFactory();
        assertThrows(
                IllegalArgumentException.class,
                () -> factory.createLattices(series(layers), fields));
    }
}
