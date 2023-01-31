package arcade.potts.sim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import sim.engine.Schedule;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.*;
import arcade.core.env.grid.Grid;
import arcade.core.env.location.*;
import arcade.core.sim.Series;
import arcade.core.sim.output.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellContainer;
import arcade.potts.agent.cell.PottsCellFactory;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.PottsLocationContainer;
import arcade.potts.env.location.PottsLocationFactory;
import arcade.potts.env.location.Voxel;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.sim.Series.SEED_OFFSET;
import static arcade.potts.sim.PottsSeries.TARGET_SEPARATOR;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsSimulationTest {
    static final long RANDOM_SEED = randomSeed();
    
    private static final int TOTAL_LOCATIONS = 6;
    
    static Series seriesZeroPop;
    
    static Series seriesOnePop;
    
    static Series seriesMultiPop;
    
    static Series seriesNullCell;
    
    static Series seriesNullLocation;
    
    static Series seriesNullBoth;
    
    static Series createSeries(int[] pops, String[] keys) {
        Series series = mock(PottsSeries.class);
        HashMap<String, MiniBox> populations = new HashMap<>();
        
        for (int i = 0; i < pops.length; i++) {
            MiniBox population = new MiniBox();
            population.put("CODE", pops[i]);
            population.put("ADHESION:*", randomDoubleBetween(0, 100));
            populations.put(keys[i], population);
            
            for (String key : keys) {
                population.put("ADHESION" + TARGET_SEPARATOR + key, randomDoubleBetween(0, 100));
            }
        }
        
        series.populations = populations;
        
        return series;
    }
    
    @BeforeClass
    public static void setupSeries() {
        // Zero populations.
        seriesZeroPop = createSeries(new int[0], new String[0]);
        
        // One population.
        seriesOnePop = createSeries(new int[] { 1 }, new String[] { "A" });
        seriesOnePop.populations.get("A").put("INIT", 5.);
        
        // Multiple populations.
        seriesMultiPop = createSeries(new int[] { 1, 2, 3 }, new String[] { "B", "C", "D" });
        seriesMultiPop.populations.get("B").put("INIT", 3);
        seriesMultiPop.populations.get("C").put("INIT", 1);
        seriesMultiPop.populations.get("D").put("INIT", 2);
        
        // Invalid populations.
        seriesNullCell = createSeries(new int[] { 1 }, new String[] { "A" });
        seriesNullCell.populations.get("A").put("INIT", 2);
        
        seriesNullLocation = createSeries(new int[] { 1 }, new String[] { "A" });
        seriesNullLocation.populations.get("A").put("INIT", 2);
        
        seriesNullBoth = createSeries(new int[] { 1 }, new String[] { "A" });
        seriesNullBoth.populations.get("A").put("INIT", 2);
    }
    
    static class PottsSimulationMock extends PottsSimulation {
        private final HashMap<MiniBox, HashMap<Integer, PottsLocation>> locationMap = new HashMap<>();
        
        private final HashMap<MiniBox, HashMap<Integer, CellContainer>> cellContainerMap = new HashMap<>();
        
        private final HashMap<MiniBox, HashMap<Integer, LocationContainer>> locationContainerMap = new HashMap<>();
        
        PottsSimulationMock(long seed, Series series) { super(seed, series); }
        
        @Override
        public Potts makePotts() { return mock(Potts.class); }
        
        private void mockLocations(PottsLocationFactory factory, MiniBox pop,
                                   int n, int m, MersenneTwisterFast random) {
            HashMap<Integer, PottsLocation> idToLocation = new HashMap<>();
            locationMap.put(pop, idToLocation);
            
            HashMap<Integer, CellContainer> idToCellContainer = new HashMap<>();
            cellContainerMap.put(pop, idToCellContainer);
            
            HashMap<Integer, LocationContainer> idToLocationContainer = new HashMap<>();
            locationContainerMap.put(pop, idToLocationContainer);
            
            for (int i = 0; i < n; i++) {
                int id = i + m + 1;
                PottsLocation loc = mock(PottsLocation.class);
                PottsLocationContainer locationContainer = mock(PottsLocationContainer.class);
                CellContainer cellContainer = mock(PottsCellContainer.class);
                
                idToLocation.put(id, loc);
                idToCellContainer.put(id, cellContainer);
                idToLocationContainer.put(id, locationContainer);
                
                factory.locations.put(id, locationContainer);
                doReturn(new Voxel(id, id, id)).when(loc).getCenter();
                doReturn(loc).when(locationContainer).convert(factory, cellContainer);
            }
        }
        
        @Override
        public PottsLocationFactory makeLocationFactory() {
            PottsLocationFactory factory = mock(PottsLocationFactory.class);
            
            try {
                Field locationField = PottsLocationFactory.class.getDeclaredField("locations");
                locationField.setAccessible(true);
                locationField.set(factory, new HashMap<Integer, LocationContainer>());
            } catch (Exception ignored) { }
            
            doAnswer(invocation -> {
                mockLocations(factory, seriesOnePop.populations.get("A"), 5, 0, random);
                return null;
            }).when(factory).initialize(seriesOnePop, random);
            
            doAnswer(invocation -> {
                mockLocations(factory, seriesMultiPop.populations.get("B"), 3, 0, random);
                mockLocations(factory, seriesMultiPop.populations.get("C"), 1, 3, random);
                mockLocations(factory, seriesMultiPop.populations.get("D"), 2, 4, random);
                return null;
            }).when(factory).initialize(seriesMultiPop, random);
            
            doAnswer(invocation -> {
                mockLocations(factory, seriesNullCell.populations.get("A"), 2, 0, random);
                return null;
            }).when(factory).initialize(seriesNullCell, random);
            
            doAnswer(invocation -> {
                mockLocations(factory, seriesNullLocation.populations.get("A"), 2, 0, random);
                factory.locations.remove(2);
                return null;
            }).when(factory).initialize(seriesNullLocation, random);
            
            doAnswer(invocation -> {
                mockLocations(factory, seriesNullBoth.populations.get("A"), 2, 0, random);
                factory.locations.remove(2);
                return null;
            }).when(factory).initialize(seriesNullBoth, random);
            
            return factory;
        }
        
        private void mockCells(PottsCellFactory factory, Series series, String code, int n, int m) {
            MiniBox pop = series.populations.get(code);
            HashSet<Integer> ids = new HashSet<>();
            
            for (int i = 0; i < n; i++) {
                int id = i + m + 1;
                PottsCell cell = mock(PottsCell.class);
                Location loc = locationMap.get(pop).get(id);
                CellContainer container = cellContainerMap.get(pop).get(id);
                ids.add(id);
                
                factory.cells.put(id, (PottsCellContainer) container);
                doReturn(id).when(cell).getID();
                doReturn(pop.getInt("CODE")).when(cell).getPop();
                doReturn(loc).when(cell).getLocation();
                doReturn(cell).when(container).convert(factory, loc);
            }
            
            factory.popToIDs.put(pop.getInt("CODE"), ids);
        }
        
        @Override
        public PottsCellFactory makeCellFactory() {
            PottsCellFactory factory = mock(PottsCellFactory.class);
            
            try {
                Field cellField = PottsCellFactory.class.getDeclaredField("cells");
                cellField.setAccessible(true);
                cellField.set(factory, new HashMap<Integer, CellContainer>());
                
                Field popField = PottsCellFactory.class.getDeclaredField("popToIDs");
                popField.setAccessible(true);
                popField.set(factory, new HashMap<Integer, ArrayList<Integer>>());
            } catch (Exception ignored) { }
            
            doAnswer(invocation -> {
                mockCells(factory, seriesOnePop, "A", 5, 0);
                return null;
            }).when(factory).initialize(seriesOnePop, random);
            
            doAnswer(invocation -> {
                mockCells(factory, seriesMultiPop, "B", 3, 0);
                mockCells(factory, seriesMultiPop, "C", 1, 3);
                mockCells(factory, seriesMultiPop, "D", 2, 4);
                return null;
            }).when(factory).initialize(seriesMultiPop, random);
            
            doAnswer(invocation -> {
                mockCells(factory, seriesNullCell, "A", 2, 0);
                factory.cells.remove(2);
                return null;
            }).when(factory).initialize(seriesNullCell, random);
            
            doAnswer(invocation -> {
                mockCells(factory, seriesNullLocation, "A", 2, 0);
                return null;
            }).when(factory).initialize(seriesNullLocation, random);
            
            doAnswer(invocation -> {
                mockCells(factory, seriesNullBoth, "A", 2, 0);
                factory.cells.remove(2);
                return null;
            }).when(factory).initialize(seriesNullBoth, random);
            
            return factory;
        }
    }
    
    @Test
    public void getSeries_initialized_returnsObject() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        assertEquals(series, sim.getSeries());
    }
    
    @Test
    public void getSchedule_initialized_returnsObject() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        assertEquals(sim.schedule, sim.getSchedule());
    }
    
    @Test
    public void getSeed_initialized_returnsValue() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED + SEED_OFFSET, series);
        assertEquals(RANDOM_SEED, sim.getSeed());
    }
    
    @Test
    public void getID_initialized_incrementsValue() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        assertEquals(1, sim.getID());
        assertEquals(2, sim.getID());
        assertEquals(3, sim.getID());
    }
    
    @Test
    public void getID_started_resetsValues() {
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
        sim.getSeries().isVis = true;
        sim.getSeries().saver = mock(OutputSaver.class);
        sim.start();
        assertEquals(1, sim.getID());
        sim.start();
        assertEquals(1, sim.getID());
    }
    
    @Test
    public void getCells_givenList_returnsContainers() {
        PottsSimulation sim = mock(PottsSimulation.class, CALLS_REAL_METHODS);
        sim.grid = mock(Grid.class);
        
        Bag objects = new Bag();
        ArrayList<CellContainer> cellContainers = new ArrayList<>();
        doReturn(objects).when(sim.grid).getAllObjects();
        
        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) {
            Cell cell = mock(Cell.class);
            CellContainer cellContainer = mock(CellContainer.class);
            doReturn(i).when(cell).getID();
            doReturn(cellContainer).when(cell).convert();
            
            objects.add(cell);
            cellContainers.add(cellContainer);
        }
        
        assertEquals(cellContainers, sim.getCells());
    }
    
    @Test
    public void getLocations_givenList_returnsContainers() {
        PottsSimulation sim = mock(PottsSimulation.class, CALLS_REAL_METHODS);
        sim.grid = mock(Grid.class);
        
        Bag objects = new Bag();
        ArrayList<LocationContainer> locationContainers = new ArrayList<>();
        
        doReturn(objects).when(sim.grid).getAllObjects();
        
        int n = randomIntBetween(5, 10);
        for (int i = 0; i < n; i++) {
            Cell cell = mock(Cell.class);
            Location location = mock(Location.class);
            LocationContainer locationContainer = mock(LocationContainer.class);
            
            doReturn(i).when(cell).getID();
            doReturn(location).when(cell).getLocation();
            doReturn(locationContainer).when(location).convert(i);
            
            objects.add(cell);
            locationContainers.add(locationContainer);
        }
        
        assertEquals(locationContainers, sim.getLocations());
    }
    
    @Test
    public void getPotts_initialized_returnsNull() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        assertNull(sim.getPotts());
    }
    
    @Test
    public void getGrid_initialized_returnsNull() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        assertNull(sim.getGrid());
    }
    
    @Test
    public void getLattice_initialized_returnsNull() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        assertNull(sim.getLattice(""));
    }
    
    @Test
    public void start_called_callsMethods() {
        Series series = createSeries(new int[0], new String[0]);
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
        doNothing().when(sim).doOutput(anyBoolean());
        sim.start();
        
        verify(sim).setupPotts();
        verify(sim).setupAgents();
        verify(sim).setupEnvironment();
        verify(sim).scheduleActions();
        verify(sim).scheduleComponents();
    }
    
    @Test
    public void start_isVis_skipsSaver() {
        Series series = createSeries(new int[0], new String[0]);
        series.saver = mock(OutputSaver.class);
        series.isVis = true;
        
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
        doNothing().when(sim).doOutput(anyBoolean());
        sim.start();
        
        verify(sim, never()).doOutput(true);
        verify(series.saver, never()).equip(sim);
    }
    
    @Test
    public void start_notVis_setsSaver() {
        Series series = createSeries(new int[0], new String[0]);
        series.saver = mock(OutputSaver.class);
        series.isVis = false;
        
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
        doNothing().when(sim).doOutput(anyBoolean());
        sim.start();
        
        verify(sim).doOutput(true);
        verify(series.saver).equip(sim);
    }
    
    @Test
    public void start_notNull_setsLoader() {
        Series series = createSeries(new int[0], new String[0]);
        series.loader = mock(OutputLoader.class);
        
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
        doNothing().when(sim).doOutput(anyBoolean());
        sim.start();
        
        verify(series.loader).equip(sim);
    }
    
    @Test
    public void finish_isVis_callsMethods() {
        Series series = createSeries(new int[0], new String[0]);
        series.isVis = true;
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
        doNothing().when(sim).doOutput(anyBoolean());
        sim.finish();
        
        verify(sim, never()).doOutput(false);
    }
    
    @Test
    public void finish_isNotVis_callsMethods() {
        Series series = createSeries(new int[0], new String[0]);
        series.isVis = false;
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, series));
        doNothing().when(sim).doOutput(anyBoolean());
        sim.finish();
        
        verify(sim).doOutput(false);
    }
    
    @Test
    public void setupPotts_mockSeries_initializesPotts() {
        Series series = mock(PottsSeries.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        sim.setupPotts();
        assertNotNull(sim.potts);
    }
    
    @Test
    public void setupPotts_mockSeries_schedulesPotts() {
        Series series = mock(PottsSeries.class);
        Schedule schedule = spy(Schedule.class);
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
        sim.schedule = schedule;
        sim.setupPotts();
        verify(sim.schedule).scheduleRepeating(1, Ordering.POTTS.ordinal(), sim.potts);
    }
    
    @Test
    public void setupAgents_anyPopulation_setsPotts() {
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(sim.grid, sim.potts.grid);
    }
    
    @Test
    public void setupAgents_zeroPopulations_initializesGrid() {
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertNotNull(sim.grid);
    }
    
    @Test
    public void setupAgents_zeroPopulations_createsNoAgents() {
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(0, sim.grid.getAllObjects().numObjs);
    }
    
    @Test
    public void setupAgents_onePopulation_createsAgents() {
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesOnePop);
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(TOTAL_LOCATIONS - 1, sim.grid.getAllObjects().numObjs);
        
        int[] pops = new int[] { 1, 1, 1, 1, 1 };
        for (int i = 0; i < TOTAL_LOCATIONS - 1; i++) {
            int id = i + 1;
            Cell cell = (Cell) sim.grid.getObjectAt(id);
            assertEquals(id, cell.getID());
            assertEquals(pops[i], cell.getPop());
            assertEquals(new Voxel(id, id, id), ((PottsLocation) cell.getLocation()).getCenter());
        }
        
        assertNull(sim.grid.getObjectAt(6));
        assertEquals(TOTAL_LOCATIONS, sim.getID());
    }
    
    @Test
    public void setupAgents_onePopulation_callMethods() {
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesOnePop));
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        
        for (Object obj : sim.grid.getAllObjects()) {
            verify((PottsCell) obj).initialize(sim.potts.ids, sim.potts.regions);
            verify((Cell) obj).schedule(sim.schedule);
            verify(sim.potts).register((PottsCell) obj);
        }
    }
    
    @Test
    public void setupAgents_multiplePopulations_createsAgents() {
        PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesMultiPop);
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(TOTAL_LOCATIONS, sim.grid.getAllObjects().numObjs);
        
        int[] pops = new int[] { 1, 1, 1, 2, 3, 3 };
        for (int i = 0; i < TOTAL_LOCATIONS; i++) {
            int id = i + 1;
            Cell cell = (Cell) sim.grid.getObjectAt(id);
            assertEquals(id, cell.getID());
            assertEquals(pops[i], cell.getPop());
            assertEquals(new Voxel(id, id, id), ((PottsLocation) cell.getLocation()).getCenter());
        }
        
        assertEquals(TOTAL_LOCATIONS + 1, sim.getID());
    }
    
    @Test
    public void setupAgents_multiplePopulations_callMethods() {
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesMultiPop));
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        
        for (Object obj : sim.grid.getAllObjects()) {
            verify((PottsCell) obj).initialize(sim.potts.ids, sim.potts.regions);
            verify((PottsCell) obj).schedule(sim.schedule);
            verify(sim.potts).register((PottsCell) obj);
        }
    }
    
    @Test
    public void setupAgents_insufficientLocations_excludesExtra() {
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesNullLocation));
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(1, sim.grid.getAllObjects().numObjs);
        assertEquals(2, sim.getID());
    }
    
    @Test
    public void setupAgents_insufficientCells_excludesExtra() {
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesNullCell));
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(1, sim.grid.getAllObjects().numObjs);
        assertEquals(2, sim.getID());
    }
    
    @Test
    public void setupAgents_insufficientBoth_excludesExtra() {
        PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesNullBoth));
        sim.potts = mock(Potts.class);
        sim.setupAgents();
        assertEquals(1, sim.grid.getAllObjects().numObjs);
        assertEquals(2, sim.getID());
    }
    
    @Test
    public void doOutput_isScheduled_schedulesOutput() {
        Series series = mock(PottsSeries.class);
        Schedule schedule = mock(Schedule.class);
        OutputSaver saver = mock(OutputSaver.class);
        
        PottsSimulation sim = new PottsSimulationMock(RANDOM_SEED, series);
        sim.series.saver = saver;
        sim.schedule = schedule;
        
        int interval = randomIntBetween(1, 100);
        doReturn(interval).when(series).getInterval();
        
        sim.doOutput(true);
        verify(saver).schedule(schedule, interval);
        verify(saver, never()).saveCells(anyInt());
        verify(saver, never()).saveLocations(anyInt());
    }
    
    @Test
    public void doOutput_isNotScheduled_savesOutput() {
        Series series = mock(PottsSeries.class);
        Schedule schedule = mock(Schedule.class);
        OutputSaver saver = mock(OutputSaver.class);
        
        PottsSimulation sim = new PottsSimulationMock(RANDOM_SEED, series);
        sim.series.saver = saver;
        sim.schedule = schedule;
        
        double time = randomDoubleBetween(1, 100);
        doReturn(time).when(sim.schedule).getTime();
        
        sim.doOutput(false);
        verify(saver, never()).schedule(eq(schedule), anyDouble());
        verify(saver).saveCells((int) time + 1);
        verify(saver).saveLocations((int) time + 1);
    }
}
