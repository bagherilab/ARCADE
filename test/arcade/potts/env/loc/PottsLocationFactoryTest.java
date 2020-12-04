package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumMap;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;
import arcade.core.env.loc.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCellContainer;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.core.util.Enums.Region;
import static arcade.core.TestUtilities.*;

public class PottsLocationFactoryTest {
    final MersenneTwisterFast random = mock(MersenneTwisterFast.class);
    
    static Series createSeries(int length, int width, int height, double[] volumes) {
        Series series = mock(Series.class);
        series._populations = new HashMap<>();
        
        try {
            Field lengthField = Series.class.getDeclaredField("_length");
            lengthField.setAccessible(true);
            lengthField.setInt(series, length);
            
            Field widthField = Series.class.getDeclaredField("_width");
            widthField.setAccessible(true);
            widthField.setInt(series, width);
            
            Field heightField = Series.class.getDeclaredField("_height");
            heightField.setAccessible(true);
            heightField.setInt(series, height);
        } catch (Exception ignored) { }
        
        for (int i = 0; i < volumes.length; i++) {
            int pop = i + 1;
            MiniBox box = new MiniBox();
            box.put("CODE", pop);
            box.put("CRITICAL_VOLUME", volumes[i]);
            series._populations.put("pop" + pop, box);
        }
        
        return series;
    }
    
    static class PottsLocationFactoryMock extends PottsLocationFactory {
        public PottsLocationFactoryMock() { super(); }
        
        int convert(double volume) { return (int)(volume + 1); }
        
        ArrayList<Voxel> getNeighbors(Voxel voxel) {
            ArrayList<Voxel> neighbors = new ArrayList<>();
            neighbors.add(new Voxel(voxel.x - 1, 0, 0));
            neighbors.add(new Voxel(voxel.x + 1, 0, 0));
            return neighbors;
        }
        
        ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
            ArrayList<Voxel> selected = new ArrayList<>();
            for (int i = 0; i < n + focus.x; i++) { selected.add(new Voxel(i, 0, 0)); }
            return selected;
        }
        
        ArrayList<Voxel> getPossible(Voxel focus, int m) {
            ArrayList<Voxel> possible = new ArrayList<>();
            for (int i = 0; i < m; i++) { possible.add(new Voxel(i, 0, 0)); }
            return possible;
        }
        
        ArrayList<Voxel> getCenters(int length, int width, int height, int m) {
            ArrayList<Voxel> centers = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    for (int k = 0; k < height; k++) {
                        centers.add(new Voxel(i + m, j + m, k + m));
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
        int n = randomIntBetween(1,100);
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
        
        ArrayList<LocationContainer> container = new ArrayList();
        for (int i = 0; i < n; i++) { container.add(containers.get(i)); }
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
        Series series = createSeries(0, 0, 0, new double[] { });
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        factory.createLocations(series);
        
        assertEquals(0, factory.locations.size());
    }
    
    @Test
    public void createLocations_onePopulationNoRegions_createsList() {
        int length = randomIntBetween(1, 10);
        int width = randomIntBetween(1,10);
        int height = randomIntBetween(1,10);
        int volume = randomIntBetween(1,100);
        Series series = createSeries(length, width, height, new double[] { volume });
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        factory.random = random;
        factory.createLocations(series);
        
        assertEquals(length*width*height, factory.locations.values().size());
        for (PottsLocationContainer container : factory.locations.values()) {
            assertTrue(container.center.x <= length + volume + 3);
            assertTrue(container.center.x >= volume + 3);
            assertTrue(container.center.y <= width + volume + 3);
            assertTrue(container.center.y >= volume + 3);
            assertTrue(container.center.z <= height + volume + 3);
            assertTrue(container.center.z >= volume + 3);
            assertEquals(volume + 3, container.allVoxels.size());
        }
    }
    
    @Test
    public void createLocations_onePopulationWithRegions_createsList() {
        int length = randomIntBetween(1,10);
        int width = randomIntBetween(1,10);
        int height = randomIntBetween(1,10);
        int volume = randomIntBetween(1,100);
        Series series = createSeries(length, width, height, new double[] { volume });
        
        series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "DEFAULT", 0.0);
        series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", 0.0);
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        factory.random = random;
        factory.createLocations(series);
        
        assertEquals(length*width*height, factory.locations.values().size());
        for (PottsLocationContainer container : factory.locations.values()) {
            assertTrue(container.center.x <= length + volume + 3);
            assertTrue(container.center.x >= volume + 3);
            assertTrue(container.center.y <= width + volume + 3);
            assertTrue(container.center.y >= volume + 3);
            assertTrue(container.center.z <= height + volume + 3);
            assertTrue(container.center.z >= volume + 3);
            assertEquals(volume + 3, container.allVoxels.size());
            assertEquals(2, container.regions.size());
            
            ArrayList<Voxel> regionVoxels = new ArrayList<>(container.allVoxels);
            regionVoxels.remove(new Voxel(volume + 2, 0, 0));
            regionVoxels.remove(new Voxel(volume + 1, 0, 0));
            
            assertEquals(regionVoxels, container.regions.get(Region.DEFAULT));
            assertEquals(regionVoxels, container.regions.get(Region.NUCLEUS));
        }
    }
    
    @Test
    public void createLocations_multiplePopulationsNoRegions_createsList() {
        int length = randomIntBetween(1,10);
        int width = randomIntBetween(1,10);
        int height = randomIntBetween(1,10);
        int volume1 = randomIntBetween(1,100);
        int volume2 = volume1 + randomIntBetween(1,100);
        int volume3 = volume1 - randomIntBetween(1,100);
        Series series = createSeries(length, width, height, new double[] { volume1, volume2, volume3 });
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        factory.random = random;
        factory.createLocations(series);
        
        assertEquals(length*width*height, factory.locations.values().size());
        for (PottsLocationContainer container : factory.locations.values()) {
            assertTrue(container.center.x <= length + volume2 + 3);
            assertTrue(container.center.x >= volume2 + 3);
            assertTrue(container.center.y <= width + volume2 + 3);
            assertTrue(container.center.y >= volume2 + 3);
            assertTrue(container.center.z <= height + volume2 + 3);
            assertTrue(container.center.z >= volume2 + 3);
            assertEquals(volume2 + 3, container.allVoxels.size());
        }
    }
    
    @Test
    public void createLocations_multiplePopulationsWithRegions_createsList() {
        int length = randomIntBetween(1,10);
        int width = randomIntBetween(1,10);
        int height = randomIntBetween(1,10);
        int volume1 = randomIntBetween(1,100);
        int volume2 = volume1 + randomIntBetween(1,100);
        int volume3 = volume1 - randomIntBetween(1,100);
        Series series = createSeries(length, width, height, new double[] { volume1, volume2, volume3 });
        
        series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "DEFAULT", 0.0);
        series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", 0.0);
        
        PottsLocationFactoryMock factory = new PottsLocationFactoryMock();
        factory.random = random;
        factory.createLocations(series);
        
        assertEquals(length*width*height, factory.locations.values().size());
        for (PottsLocationContainer container : factory.locations.values()) {
            assertTrue(container.center.x <= length + volume2 + 3);
            assertTrue(container.center.x >= volume2 + 3);
            assertTrue(container.center.y <= width + volume2 + 3);
            assertTrue(container.center.y >= volume2 + 3);
            assertTrue(container.center.z <= height + volume2 + 3);
            assertTrue(container.center.z >= volume2 + 3);
            assertEquals(volume2 + 3, container.allVoxels.size());
            assertEquals(2, container.regions.size());
            
            ArrayList<Voxel> regionVoxels = new ArrayList<>(container.allVoxels);
            regionVoxels.remove(new Voxel(volume2 + 2, 0, 0));
            regionVoxels.remove(new Voxel(volume2 + 1, 0, 0));
            
            assertEquals(regionVoxels, container.regions.get(Region.DEFAULT));
            assertEquals(regionVoxels, container.regions.get(Region.NUCLEUS));
        }
    }
}
