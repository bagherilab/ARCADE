package arcade.potts.env.loc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;
import arcade.core.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

public class PottsLocationFactoryTest {
    final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
    
    static Series createSeries(int length, int width, int height,
                               double[] volumes, double[] heights) {
        Series series = mock(Series.class);
        series.populations = new HashMap<>();
        
        try {
            Field lengthField = Series.class.getDeclaredField("length");
            lengthField.setAccessible(true);
            lengthField.setInt(series, length);
            
            Field widthField = Series.class.getDeclaredField("width");
            widthField.setAccessible(true);
            widthField.setInt(series, width);
            
            Field heightField = Series.class.getDeclaredField("height");
            heightField.setAccessible(true);
            heightField.setInt(series, height);
        } catch (Exception ignored) { }
        
        for (int i = 0; i < volumes.length; i++) {
            int pop = i + 1;
            MiniBox box = new MiniBox();
            box.put("CODE", pop);
            box.put("CRITICAL_VOLUME_MEAN", volumes[i]);
            box.put("CRITICAL_HEIGHT_MEAN", heights[i]);
            series.populations.put("pop" + pop, box);
        }
        
        return series;
    }
    
    static class PottsLocationFactoryMock extends PottsLocationFactory {
        PottsLocationFactoryMock() { super(); }
        
        @Override
        ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
            ArrayList<Voxel> selected = new ArrayList<>();
            for (int i = 0; i < n + focus.x; i++) {
                selected.add(new Voxel(i, 0, 0));
            }
            return selected;
        }
        
        @Override
        ArrayList<Voxel> getPossible(Voxel focus, int s, int h) {
            ArrayList<Voxel> possible = new ArrayList<>();
            for (int i = 0; i < s; i++) {
                for (int j = 0; j < s; j++) {
                    for (int k = 0; k < h; k++) {
                        possible.add(new Voxel(i, j, k));
                    }
                }
            }
            return possible;
        }
        
        @Override
        ArrayList<Voxel> getCenters(int length, int width, int height, int s, int h) {
            ArrayList<Voxel> centers = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    for (int k = 0; k < height; k++) {
                        centers.add(new Voxel(i + s, j + s, k + h));
                    }
                }
            }
            return centers;
        }
    }
    
    @Test
    public void initialize_noLoading_callsMethod() {
        PottsLocationFactory factory = spy(new PottsLocationFactoryMock());
        Series series = mock(Series.class);
        series.loader = null;
        
        doNothing().when(factory).loadLocations(series);
        doNothing().when(factory).createLocations(series);
        
        factory.initialize(series, random);
        
        verify(factory).createLocations(series);
        verify(factory, never()).loadLocations(series);
    }
    
    @Test
    public void initialize_noLoadingWithLoader_callsMethod() {
        PottsLocationFactory factory = spy(new PottsLocationFactoryMock());
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        try {
            Field field = OutputLoader.class.getDeclaredField("loadLocations");
            field.setAccessible(true);
            field.set(series.loader, false);
        } catch (Exception ignored) { }
        
        doNothing().when(factory).loadLocations(series);
        doNothing().when(factory).createLocations(series);
        
        factory.initialize(series, random);
        
        verify(factory).createLocations(series);
        verify(factory, never()).loadLocations(series);
    }
    
    @Test
    public void initialize_withLoadingWithLoader_callsMethod() {
        PottsLocationFactory factory = spy(new PottsLocationFactoryMock());
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        try {
            Field field = OutputLoader.class.getDeclaredField("loadLocations");
            field.setAccessible(true);
            field.set(series.loader, true);
        } catch (Exception ignored) { }
        
        doNothing().when(factory).loadLocations(series);
        doNothing().when(factory).createLocations(series);
        
        factory.initialize(series, random);
        
        verify(factory, never()).createLocations(series);
        verify(factory).loadLocations(series);
    }
    
    @Test
    public void loadLocations_givenLoaded_updatesList() {
        int n = randomIntBetween(1, 100);
        ArrayList<PottsLocationContainer> containers = new ArrayList<>();
        ArrayList<ArrayList<Voxel>> allVoxels = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ArrayList<Voxel> voxels = new ArrayList<>();
            voxels.add(new Voxel(i, i, i));
            voxels.add(new Voxel(i, 0, 0));
            voxels.add(new Voxel(0, i, 0));
            voxels.add(new Voxel(0, 0, i));
            PottsLocationContainer container = new PottsLocationContainer(i, new Voxel(i, i, i), voxels, null);
            containers.add(container);
            allVoxels.add(voxels);
        }
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        Series series = mock(Series.class);
        series.loader = mock(OutputLoader.class);
        
        ArrayList<LocationContainer> container = new ArrayList<>();
        IntStream.range(0, n).forEach(i -> container.add(containers.get(i)));
        doReturn(container).when(series.loader).loadLocations();
        
        factory.loadLocations(series);
        assertEquals(n, factory.locations.size());
        for (int i = 0; i < n; i++) {
            PottsLocationContainer pottsLocationContainer = factory.locations.get(i);
            assertEquals(new Voxel(i, i, i), pottsLocationContainer.center);
            assertEquals(allVoxels.get(i), pottsLocationContainer.allVoxels);
        }
    }
    
    @Test
    public void createLocations_noPopulation_createsEmpty() {
        Series series = createSeries(0, 0, 0, new double[] { }, new double[] { });
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        factory.createLocations(series);
        
        assertEquals(0, factory.locations.size());
    }
    
    @Test
    public void createLocations_noRegions_createsList() {
        int length = randomIntBetween(1, 10);
        int width = randomIntBetween(1, 10);
        int height = randomIntBetween(1, 10);
        int s = randomIntBetween(1, 10);
        int h = randomIntBetween(1, 10);
        Series series = createSeries(length, width, height, new double[] { 0 }, new double[] { 0 });
        
        PottsLocationFactoryMock factory = spy(new PottsLocationFactoryMock());
        factory.random = random;
        doReturn(h).when(factory).getVoxelsPerHeight(series);
        doReturn(s).when(factory).getVoxelsPerSide(series, h);
        factory.createLocations(series);
        
        assertEquals(length * width * height, factory.locations.values().size());
        for (PottsLocationContainer container : factory.locations.values()) {
            assertTrue(container.center.x <= length + s);
            assertTrue(container.center.x >= s);
            assertTrue(container.center.y <= width + s);
            assertTrue(container.center.y >= s);
            assertTrue(container.center.z <= height + h);
            assertTrue(container.center.z >= h);
            assertEquals(s * s * h, container.allVoxels.size());
        }
    }
    
    @Test
    public void createLocations_withRegions_createsList() {
        int length = randomIntBetween(1, 10);
        int width = randomIntBetween(1, 10);
        int height = randomIntBetween(1, 10);
        int s = randomIntBetween(3, 10);
        int h = randomIntBetween(1, 10);
        int padding = 2;
        Series series = createSeries(length, width, height, new double[] { 0 }, new double[] { 0 });
        
        series.populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "DEFAULT", 0.0);
        series.populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", 0.0);
        
        PottsLocationFactoryMock factory = spy(new PottsLocationFactoryMock());
        factory.random = random;
        doReturn(h).when(factory).getVoxelsPerHeight(series);
        doReturn(s).when(factory).getVoxelsPerSide(series, h);
        factory.createLocations(series);
        
        assertEquals(length * width * height, factory.locations.values().size());
        for (PottsLocationContainer container : factory.locations.values()) {
            assertTrue(container.center.x <= length + s);
            assertTrue(container.center.x >= s);
            assertTrue(container.center.y <= width + s);
            assertTrue(container.center.y >= s);
            assertTrue(container.center.z <= height + h);
            assertTrue(container.center.z >= h);
            assertEquals(s * s * h, container.allVoxels.size());
            assertEquals(2, container.regions.size());
            assertEquals((s - padding) * (s - padding) * h, container.regions.get(Region.DEFAULT).size());
            assertEquals((s - padding) * (s - padding) * h, container.regions.get(Region.NUCLEUS).size());
            
            for (Voxel voxel : container.regions.get(Region.DEFAULT)) {
                assertTrue(voxel.x <= s - padding);
                assertTrue(voxel.x >= 0);
                assertTrue(voxel.y <= s - padding);
                assertTrue(voxel.y >= 0);
                assertTrue(voxel.z <= h);
                assertTrue(voxel.z >= 0);
            }
    
            for (Voxel voxel : container.regions.get(Region.NUCLEUS)) {
                assertTrue(voxel.x <= s - padding);
                assertTrue(voxel.x >= 0);
                assertTrue(voxel.y <= s - padding);
                assertTrue(voxel.y >= 0);
                assertTrue(voxel.z <= h);
                assertTrue(voxel.z >= 0);
            }
        }
    }
    
    @Test
    public void convert_exactOddSides_calculateValue() {
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = randomIntBetween(2, 10);
        assertEquals(1, factory.convert(1 * 1 * h, h));
        assertEquals(3, factory.convert(3 * 3 * h, h));
        assertEquals(5, factory.convert(5 * 5 * h, h));
        assertEquals(7, factory.convert(7 * 7 * h, h));
    }
    
    @Test
    public void convert_exactEvenSides_calculateValue() {
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = randomIntBetween(2, 10);
        assertEquals(3, factory.convert(2 * 2 * h, h));
        assertEquals(5, factory.convert(4 * 4 * h, h));
        assertEquals(7, factory.convert(6 * 6 * h, h));
        assertEquals(9, factory.convert(8 * 8 * h, h));
    }
    
    @Test
    public void convert_inexactOddSides_calculateValue() {
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = randomIntBetween(2, 10);
        assertEquals(3, factory.convert(1 * 1 * h + 1, h));
        assertEquals(5, factory.convert(3 * 3 * h + 1, h));
        assertEquals(7, factory.convert(5 * 5 * h + 1, h));
        assertEquals(9, factory.convert(7 * 7 * h + 1, h));
    }
    
    @Test
    public void convert_inexactEvenSides_calculateValue() {
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = randomIntBetween(2, 10);
        assertEquals(3, factory.convert(2 * 2 * h - 1, h));
        assertEquals(5, factory.convert(4 * 4 * h - 1, h));
        assertEquals(7, factory.convert(6 * 6 * h - 1, h));
        assertEquals(9, factory.convert(8 * 8 * h - 1, h));
    }
    
    @Test
    public void getVoxelsPerHeight_minimumHeight_returnsOne() {
        int n = randomIntBetween(3, 10);
        double[] heights = new double[n];
        int seriesHeight = 0;
        
        Series series = createSeries(0, 0, seriesHeight, new double[n], heights);
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = factory.getVoxelsPerHeight(series);
        
        assertEquals(1, h);
    }
    
    @Test
    public void getVoxelsPerHeight_cellHeight_returnsValue() {
        int n = randomIntBetween(3, 10);
        double[] heights = new double[n];
        int seriesHeight = 100 * n + 2;
        int maxHeight = 0;
        
        for (int i = 0; i < n; i++) {
            heights[i] = randomIntBetween(1, 100);
            maxHeight = (int) Math.max(maxHeight, heights[i]);
        }
        
        Series series = createSeries(0, 0, seriesHeight, new double[n], heights);
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = factory.getVoxelsPerHeight(series);
        
        assertEquals(maxHeight, h);
    }
    
    @Test
    public void getVoxelsPerHeight_fractionalHeight_returnsValue() {
        int n = randomIntBetween(3, 10);
        double[] heights = new double[n];
        int seriesHeight = 100 * n + 2;
        int maxHeight = 0;
        
        for (int i = 0; i < n; i++) {
            heights[i] = randomDoubleBetween(1, 100);
            maxHeight = (int) Math.max(maxHeight, Math.ceil(heights[i]));
        }
        
        Series series = createSeries(0, 0, seriesHeight, new double[n], heights);
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = factory.getVoxelsPerHeight(series);
        
        assertEquals(maxHeight, h);
    }
    
    @Test
    public void getVoxelsPerHeight_seriesHeight_returnsValue() {
        int n = randomIntBetween(3, 10);
        double[] heights = new double[n];
        int padding = 2;
        int seriesHeight = randomIntBetween(3, 10) + padding;
        
        for (int i = 0; i < n; i++) {
            heights[i] = randomIntBetween(seriesHeight + 1, 100);
        }
        
        Series series = createSeries(0, 0, seriesHeight, new double[n], heights);
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int h = factory.getVoxelsPerHeight(series);
        
        assertEquals(seriesHeight - padding, h);
    }
    
    @Test
    public void getVoxelsPerSide_noPopulations_returnsZero() {
        Series series = createSeries(0, 0, 0, new double[0], new double[0]);
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int s = factory.getVoxelsPerSide(series, 0);
        
        assertEquals(0, s);
    }
    
    @Test
    public void getVoxelsPerSide_noPadding_returnsValue() {
        int h = randomIntBetween(3, 10);
        int n = randomIntBetween(3, 10);
        double[] volumes = new double[n];
        int padding = 0;
        int maxVolume = 0;
        
        for (int i = 0; i < n; i++) {
            int v = (randomIntBetween(2, 10) * 2 + 1);
            volumes[i] = v * v * h / 2.0;
            maxVolume = Math.max(maxVolume, v);
        }
        
        Series series = createSeries(0, 0, 0, volumes, new double[n]);
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int s = factory.getVoxelsPerSide(series, h);
        
        assertEquals(maxVolume + padding, s);
    }
    
    @Test
    public void getVoxelsPerSide_withPadding_returnsValue() {
        int h = randomIntBetween(3, 10);
        int n = randomIntBetween(3, 10);
        double[] volumes = new double[n];
        int[] paddings = new int[n];
        int maxVolume = 0;
        
        for (int i = 0; i < n; i++) {
            int v = (randomIntBetween(2, 10) * 2 + 1);
            volumes[i] = v * v * h / 2.0;
            
            int p = randomIntBetween(2, 10);
            paddings[i] = p;
            maxVolume = Math.max(maxVolume, v + p);
        }
        
        Series series = createSeries(0, 0, 0, volumes, new double[n]);
        
        for (int i = 0; i < n; i++) {
            int pop = i + 1;
            MiniBox box = series.populations.get("pop" + pop);
            box.put("PADDING", paddings[i]);
        }
        
        PottsLocationFactory factory = new PottsLocationFactoryMock();
        int s = factory.getVoxelsPerSide(series, h);
        
        assertEquals(maxVolume, s);
    }
}
