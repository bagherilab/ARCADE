package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.agent.module.*;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.PottsSimulation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.agent.cell.PottsCellFactoryTest.*;
import static arcade.potts.util.PottsEnums.Domain;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellTest {
    private static final double EPSILON = 1E-8;
    
    private static final double OFFSET = 0.01;
    
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());
    
    static EnumMap<Region, Double> criticalVolumesRegionMock;
    
    static EnumMap<Region, Double> criticalHeightsRegionMock;
    
    static PottsLocation locationMock;
    
    static int locationVolume;
    
    static int locationHeight;
    
    static int locationSurface;
    
    static EnumMap<Region, Integer> locationRegionVolumes;
    
    static EnumMap<Region, Integer> locationRegionHeights;
    
    static EnumMap<Region, Integer> locationRegionSurfaces;
    
    static int cellID = randomIntBetween(1, 10);
    
    static int cellParent = randomIntBetween(1, 10);
    
    static int cellPop = randomIntBetween(1, 10);
    
    static int cellAge = randomIntBetween(1, 1000);
    
    static int cellDivisions = randomIntBetween(1, 100);
    
    static double cellCriticalVolume = randomDoubleBetween(10, 100);
    
    static double cellCriticalHeight = randomDoubleBetween(10, 100);
    
    static State cellState = State.QUIESCENT;
    
    static PottsCell cellDefault;
    
    static PottsCell cellWithRegions;
    
    static PottsCell cellWithoutRegions;
    
    static EnumSet<Region> regionList;
    
    static MiniBox parametersMock;
    
    static class PottsCellMock extends PottsCell {
        PottsCellMock(int id, int parent, int pop, CellState state, int age, int divisions,
                      Location location, boolean hasRegions, MiniBox parameters,
                      double criticalVolume, double criticalHeight,
                      EnumMap<Region, Double> criticalRegionVolumes,
                      EnumMap<Region, Double> criticalRegionHeights) {
            super(id, parent, pop, state, age, divisions, location, hasRegions, parameters,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        }
        
        @Override
        public PottsCellMock make(int newID, CellState newState, Location newLocation,
                                  MersenneTwisterFast random) {
            return new PottsCellMock(newID, id, pop, newState, age, divisions, newLocation,
                    hasRegions, parameters, criticalVolume, criticalHeight,
                    criticalRegionVolumes, criticalRegionHeights);
        }
        
        @Override
        void setStateModule(CellState newState) {
            module = mock(PottsModule.class);
        }
    }
    
    @BeforeClass
    public static void setupMocks() {
        parametersMock = mock(MiniBox.class);
        locationMock = mock(PottsLocation.class);
        
        regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS);
        when(locationMock.getRegions()).thenReturn(regionList);
        
        Answer<Double> answer = invocation -> {
            Double value1 = invocation.getArgument(0);
            Double value2 = invocation.getArgument(1);
            return value1 * value2;
        };
        when((locationMock).convertSurface(anyDouble(), anyDouble())).thenAnswer(answer);
        
        locationRegionVolumes = new EnumMap<>(Region.class);
        locationRegionHeights = new EnumMap<>(Region.class);
        locationRegionSurfaces = new EnumMap<>(Region.class);
        
        // Random volumes and surfaces for regions.
        for (Region region : regionList) {
            locationRegionVolumes.put(region, randomIntBetween(10, 20));
            locationRegionHeights.put(region, randomIntBetween(10, 20));
            locationRegionSurfaces.put(region, randomIntBetween(10, 20));
            
            when(locationMock.getVolume(region)).thenReturn((double) locationRegionVolumes.get(region));
            when(locationMock.getHeight(region)).thenReturn((double) locationRegionHeights.get(region));
            when(locationMock.getSurface(region)).thenReturn((double) locationRegionSurfaces.get(region));
            
            locationVolume += locationRegionVolumes.get(region);
            locationHeight += locationRegionHeights.get(region);
            locationSurface += locationRegionSurfaces.get(region);
        }
        
        when(locationMock.getVolume()).thenReturn((double) locationVolume);
        when(locationMock.getHeight()).thenReturn((double) locationHeight);
        when(locationMock.getSurface()).thenReturn((double) locationSurface);
        
        // Region criticals.
        criticalVolumesRegionMock = new EnumMap<>(Region.class);
        criticalHeightsRegionMock = new EnumMap<>(Region.class);
        for (Region region : regionList) {
            criticalVolumesRegionMock.put(region, (double) locationRegionVolumes.get(region));
            criticalHeightsRegionMock.put(region, (double) locationRegionHeights.get(region));
        }
        
        cellDefault = new PottsCellMock(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        cellWithRegions = new PottsCellMock(cellID, cellParent, 1, cellState, cellAge, cellDivisions,
                locationMock, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalVolumesRegionMock, criticalHeightsRegionMock);
        cellWithoutRegions = new PottsCellMock(cellID, cellParent, 1, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalVolumesRegionMock, criticalHeightsRegionMock);
    }
    
    static PottsCell make(boolean regions) {
        return make(locationMock, regions);
    }
    
    static PottsCell make(Location location, boolean regions) {
        if (!regions) {
            return new PottsCellMock(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                    location, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                    null, null);
        } else {
            return new PottsCellMock(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                    location, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
                    criticalVolumesRegionMock, criticalHeightsRegionMock);
        }
    }
    
    @Test
    public void getID_defaultConstructor_returnsValue() {
        assertEquals(cellID, cellDefault.getID());
    }
    
    @Test
    public void getParent_defaultConstructor_returnsValue() {
        assertEquals(cellParent, cellDefault.getParent());
    }
    
    @Test
    public void getParent_valueAssigned_returnsValue() {
        int parent = randomIntBetween(0, 100);
        PottsCell cell = new PottsCellMock(cellID, parent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(parent, cell.getParent());
    }
    
    @Test
    public void getPop_defaultConstructor_returnsValue() {
        assertEquals(cellPop, cellDefault.getPop());
    }
    
    @Test
    public void getPop_valueAssigned_returnsValue() {
        int pop = randomIntBetween(0, 100);
        PottsCell cell = new PottsCellMock(cellID, cellParent, pop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(pop, cell.getPop());
    }
    
    @Test
    public void getState_defaultConstructor_returnsValue() {
        assertEquals(cellState, cellDefault.getState());
    }
    
    @Test
    public void getState_valueAssigned_returnsValue() {
        State state = State.random(RANDOM);
        PottsCell cell = new PottsCellMock(cellID, cellParent, cellPop, state, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(state, cell.getState());
    }
    
    @Test
    public void getAge_defaultConstructor_returnsValue() {
        assertEquals(cellAge, cellDefault.getAge());
    }
    
    @Test
    public void getAge_valueAssigned_returnsValue() {
        int age = randomIntBetween(0, 100);
        PottsCell cell = new PottsCellMock(cellID, cellParent, cellPop, cellState, age, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(age, cell.getAge());
    }
    
    @Test
    public void getDivisions_defaultConstructor_returnsValue() {
        assertEquals(cellDivisions, cellDefault.getDivisions());
    }
    
    @Test
    public void getDivisions_valueAssigned_returnsValue() {
        int divisions = randomIntBetween(0, 100);
        PottsCell cell = new PottsCellMock(cellID, cellParent, cellPop, cellState, cellAge, divisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        assertEquals(divisions, cell.getDivisions());
    }
    
    @Test
    public void hasRegions_withoutRegions_returnsFalse() {
        assertFalse(cellWithoutRegions.hasRegions());
    }
    
    @Test
    public void hasRegions_withRegions_returnsTrue() {
        assertTrue(cellWithRegions.hasRegions());
    }
    
    @Test
    public void getLocation_defaultConstructor_returnsObject() {
        assertSame(locationMock, cellDefault.getLocation());
    }
    
    @Test
    public void getModule_defaultConstructor_returnsObject() {
        assertTrue(cellDefault.getModule() instanceof PottsModule);
    }
    
    @Test
    public void getProcess_defaultConstructor_returnsNull() {
        assertNull(cellDefault.getProcess(Domain.UNDEFINED));
    }
    
    @Test
    public void getParameters_defaultConstructor_returnsObject() {
        assertSame(parametersMock, cellDefault.getParameters());
    }
    
    @Test
    public void getVolume_defaultConstructor_returnsValue() {
        assertEquals(locationVolume, (int) cellDefault.getVolume());
    }
    
    @Test
    public void getVolume_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionVolumes.get(region), (int) cellWithRegions.getVolume(region));
        }
    }
    
    @Test
    public void getVolume_nullRegion_returnsZero() {
        assertEquals(0, (int) cellWithRegions.getVolume(null));
    }
    
    @Test
    public void getVolume_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, (int) cellWithoutRegions.getVolume(region));
        }
    }
    
    @Test
    public void getHeight_defaultConstructor_returnsValue() {
        assertEquals(locationHeight, (int) cellDefault.getHeight());
    }
    
    @Test
    public void getHeight_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionHeights.get(region), (int) cellWithRegions.getHeight(region));
        }
    }
    
    @Test
    public void getHeight_nullRegion_returnsZero() {
        assertEquals(0, (int) cellWithRegions.getHeight(null));
    }
    
    @Test
    public void getHeight_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, (int) cellWithoutRegions.getHeight(region));
        }
    }
    
    @Test
    public void getSurface_defaultConstructor_returnsValue() {
        assertEquals(locationSurface, (int) cellDefault.getSurface());
    }
    
    @Test
    public void getSurface_validRegions_returnsValue() {
        for (Region region : regionList) {
            assertEquals((int) locationRegionSurfaces.get(region), (int) cellWithRegions.getSurface(region));
        }
    }
    
    @Test
    public void getSurface_nullRegion_returnsZero() {
        assertEquals(0, (int) cellWithRegions.getSurface(null));
    }
    
    @Test
    public void getSurface_noRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, (int) cellWithoutRegions.getSurface(region));
        }
    }
    
    @Test
    public void getTargetVolume_beforeInitialize_returnsZero() {
        assertEquals(0, cellDefault.getTargetVolume(), EPSILON);
    }
    
    @Test
    public void getTargetVolume_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithRegions.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getTargetVolume(null), EPSILON);
    }
    
    @Test
    public void getTargetVolume_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
    }
    
    @Test
    public void getTargetVolume_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cell.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetVolume_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getTargetVolume(null), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getTargetVolume_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getTargetVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_beforeInitialize_returnsZero() {
        assertEquals(0, cellDefault.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void getTargetSurface_beforeInitializeValidRegion_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithRegions.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getTargetSurface(null), EPSILON);
    }
    
    @Test
    public void getTargetSurface_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void getTargetSurface_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionSurfaces.get(region), cell.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getTargetSurface_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getTargetSurface(null), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getTargetSurface_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getTargetSurface(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_beforeInitialize_returnsValue() {
        assertEquals(cellCriticalVolume, cellDefault.getCriticalVolume(), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeValidRegion_returnsValue() {
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cellWithRegions.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getCriticalVolume(null), EPSILON);
        assertEquals(0, cellWithRegions.getCriticalVolume(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(cellCriticalVolume, cell.getCriticalVolume(), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionVolumes.get(region), cell.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalVolume_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getCriticalVolume(null), EPSILON);
    }
    
    @Test
    public void getCriticalVolume_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getCriticalVolume(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_beforeInitialize_returnsValue() {
        assertEquals(cellCriticalHeight, cellDefault.getCriticalHeight(), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_beforeInitializeValidRegion_returnsValue() {
        for (Region region : regionList) {
            assertEquals(locationRegionHeights.get(region), cellWithRegions.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_beforeInitializeInvalidRegion_returnsZero() {
        assertEquals(0, cellWithRegions.getCriticalHeight(null), EPSILON);
        assertEquals(0, cellWithRegions.getCriticalHeight(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_beforeInitializeNoRegions_returnsZero() {
        for (Region region : regionList) {
            assertEquals(0, cellWithoutRegions.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_afterInitialize_returnsValue() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        assertEquals(cellCriticalHeight, cell.getCriticalHeight(), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_afterInitializeValidRegion_returnsValue() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(locationRegionHeights.get(region), cell.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void getCriticalHeight_afterInitializeInvalidRegion_returnsZero() {
        PottsCell cell = make(true);
        cell.initialize(null, null);
        assertEquals(0, cell.getCriticalHeight(null), EPSILON);
    }
    
    @Test
    public void getCriticalHeight_afterInitializeNoRegion_returnsZero() {
        PottsCell cell = make(false);
        cell.initialize(null, null);
        for (Region region : regionList) {
            assertEquals(0, cell.getCriticalHeight(region), EPSILON);
        }
    }
    
    @Test
    public void stop_called_callsMethod() {
        PottsCell cell = make(false);
        cell.stopper = mock(Stoppable.class);
        cell.stop();
        verify(cell.stopper).stop();
    }
    
    @Test
    public void schedule_validInput_callsMethod() {
        Schedule schedule = spy(mock(Schedule.class));
        PottsCell cell = make(false);
        doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
        cell.schedule(schedule);
        verify(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
    }
    
    @Test
    public void schedule_validInput_assignStopper() {
        Schedule schedule = spy(mock(Schedule.class));
        PottsCell cell = make(false);
        doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, Ordering.CELLS.ordinal(), 1);
        cell.schedule(schedule);
        assertNotNull(cell.stopper);
    }
    
    @Test
    public void initialize_withoutRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCell cell = make(location, false);
        int[][][] array = new int[1][3][3];
        cell.initialize(array, null);
        
        verify(location).update(cellID, array, null);
    }
    
    @Test
    public void initialize_withRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        when(location.getRegions()).thenReturn(regionList);
        PottsCell cell = make(location, true);
        int[][][] array1 = new int[1][3][3];
        int[][][] array2 = new int[1][3][3];
        cell.initialize(array1, array2);
        
        verify(location).update(cellID, array1, array2);
    }
    
    @Test
    public void initialize_withoutRegions_updatesTargets() {
        int volume = randomIntBetween(0, 100);
        int surface = randomIntBetween(0, 100);
        PottsLocation location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn((double) volume);
        when(location.getSurface()).thenReturn((double) surface);
        
        PottsCell cell = make(location, false);
        cell.initialize(new int[1][3][3], null);
        
        assertEquals(volume, cell.getTargetVolume(), EPSILON);
        assertEquals(surface, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void initialize_withRegions_updatesTargets() {
        int volume1 = randomIntBetween(0, 100);
        int volume2 = randomIntBetween(0, 100);
        int surface1 = randomIntBetween(0, 100);
        int surface2 = randomIntBetween(0, 100);
        PottsLocation location = mock(PottsLocation.class);
        when(location.getVolume()).thenReturn((double) (volume1 + volume2));
        when(location.getSurface()).thenReturn((double) (surface1 + surface2));
        when(location.getVolume(Region.DEFAULT)).thenReturn((double) volume1);
        when(location.getSurface(Region.DEFAULT)).thenReturn((double) surface1);
        when(location.getVolume(Region.NUCLEUS)).thenReturn((double) volume2);
        when(location.getSurface(Region.NUCLEUS)).thenReturn((double) surface2);
        when(location.getRegions()).thenReturn(regionList);
        
        PottsCell cell = make(location, true);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(volume1 + volume2, cell.getTargetVolume(), EPSILON);
        assertEquals(surface1 + surface2, cell.getTargetSurface(), EPSILON);
        assertEquals(volume1, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(surface1, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        assertEquals(volume2, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(surface2, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
    }
    
    @Test
    public void reset_withoutRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCell cell = make(location, false);
        int[][][] array = new int[1][3][3];
        cell.initialize(array, null);
        cell.reset(array, null);
        
        verify(location, times(2)).update(cellID, array, null);
    }
    
    @Test
    public void reset_withRegions_callsMethod() {
        PottsLocation location = mock(PottsLocation.class);
        when(location.getRegions()).thenReturn(regionList);
        PottsCell cell = make(location, true);
        int[][][] array1 = new int[1][3][3];
        int[][][] array2 = new int[1][3][3];
        cell.initialize(array1, array2);
        cell.reset(array1, array2);
        
        verify(location, times(2)).update(cellID, array1, array2);
    }
    
    @Test
    public void reset_withoutRegions_updatesTargets() {
        PottsCell cell = make(false);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        cell.updateTarget(randomDoubleBetween(0, 10), randomDoubleBetween(0, 10));
        cell.reset(new int[1][3][3], null);
        
        assertEquals(cellCriticalVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(cellCriticalVolume * cellCriticalHeight, cell.getTargetSurface(), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void reset_withRegions_updatesTargets() {
        PottsCell cell = make(true);
        cell.initialize(new int[1][3][3], new int[1][3][3]);
        cell.updateTarget(Region.DEFAULT, randomDoubleBetween(0, 10), randomDoubleBetween(0, 10));
        cell.updateTarget(Region.NUCLEUS, randomDoubleBetween(0, 10), randomDoubleBetween(0, 10));
        cell.reset(new int[1][3][3], new int[1][3][3]);
        
        assertEquals(cellCriticalVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(cellCriticalVolume * cellCriticalHeight, cell.getTargetSurface(), EPSILON);
        
        double volumeDefault = criticalVolumesRegionMock.get(Region.DEFAULT);
        double heightDefault = criticalHeightsRegionMock.get(Region.DEFAULT);
        assertEquals(volumeDefault, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(volumeDefault * heightDefault, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        
        double volumeNucleus = criticalVolumesRegionMock.get(Region.NUCLEUS);
        double heightNucleus = criticalHeightsRegionMock.get(Region.NUCLEUS);
        assertEquals(volumeNucleus, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(volumeNucleus * heightNucleus, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
    }
    
    @Test
    public void step_singleStep_updatesAge() {
        PottsCell cell = make(false);
        PottsSimulation sim = mock(PottsSimulation.class);
        cell.module = mock(Module.class);
        
        cell.step(sim);
        assertEquals(cellAge + 1, cell.getAge(), EPSILON);
    }
    
    @Test
    public void setTargets_noRegions_updateValues() {
        double targetVolume = randomDoubleBetween(0, 10);
        double targetSurface = randomDoubleBetween(0, 10);
        PottsCell cell = make(false);
        cell.setTargets(targetVolume, targetSurface);
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void setTargets_withRegions_updateValues() {
        double targetVolume = randomDoubleBetween(0, 10);
        double targetSurface = randomDoubleBetween(0, 10);
        PottsCell cell = make(true);
        cell.setTargets(Region.NUCLEUS, targetVolume, targetSurface);
        
        assertEquals(targetVolume, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.UNDEFINED), EPSILON);
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_noScale_doesNothing() {
        double scale = 1.0;
        double rate = randomDoubleBetween(0, 100);
        
        PottsCell cell = make(false);
        cell.initialize(null, null);
        cell.updateTarget(rate, scale);
        
        assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void updateTarget_noRegions_updatesValues() {
        double[] scales = new double[] {
                randomDoubleBetween(1 + OFFSET, 2),
                randomDoubleBetween(1 + OFFSET, 2),
                randomDoubleBetween(0, 1 - OFFSET),
                randomDoubleBetween(0, 1 - OFFSET),
        };
        
        double[] rates = new double[] {
                randomDoubleBetween(0, scales[0] - 1) * cellCriticalVolume,
                randomDoubleBetween(scales[1] - 1, scales[1]) * cellCriticalVolume,
                randomDoubleBetween(0, 1 - scales[2]) * cellCriticalVolume,
                randomDoubleBetween(1 - scales[3], 2 - scales[3]) * cellCriticalVolume,
        };
        
        double[] expectedVolumes = new double[] {
                cellCriticalVolume + rates[0],
                scales[1] * cellCriticalVolume,
                cellCriticalVolume - rates[2],
                scales[3] * cellCriticalVolume,
        };
        
        for (int i = 0; i < scales.length; i++) {
            PottsCell cell = make(false);
            cell.reset(null, null);
            cell.updateTarget(rates[i], scales[i]);
            
            double expectedVolume = expectedVolumes[i];
            assertEquals(expectedVolume, cell.getTargetVolume(), EPSILON);
            assertEquals(cellCriticalHeight * expectedVolume, cell.getTargetSurface(), EPSILON);
        }
    }
    
    @Test
    public void updateTarget_withRegions_updatesValues() {
        double defaultVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double defaultHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double threshold = defaultVolume / cellCriticalVolume;
        
        double[] scales = new double[] {
                randomDoubleBetween(0, 1 - threshold),
                randomDoubleBetween(0, 1 - threshold),
                randomDoubleBetween(0, 1 - threshold),
                randomDoubleBetween(1 - threshold, 1),
                randomDoubleBetween(1 - threshold, 1),
                randomDoubleBetween(1 - threshold, 1),
                randomDoubleBetween(1 + OFFSET, 2),
                randomDoubleBetween(1 + OFFSET, 2),
        };
        
        double[] rates = new double[] {
                randomDoubleBetween(0, threshold) * cellCriticalVolume,
                randomDoubleBetween(threshold, 1 - scales[1]) * cellCriticalVolume,
                randomDoubleBetween(1 - scales[2], 2 - scales[2]) * cellCriticalVolume,
                randomDoubleBetween(0, 1 - scales[3]) * cellCriticalVolume,
                randomDoubleBetween(threshold, 2 - scales[4]) * cellCriticalVolume,
                randomDoubleBetween(1 - scales[5], threshold) * cellCriticalVolume,
                randomDoubleBetween(0, scales[6] - 1) * cellCriticalVolume,
                randomDoubleBetween(scales[7] - 1, scales[7]) * cellCriticalVolume,
        };
        
        double[] expectedVolumes = new double[] {
                cellCriticalVolume - rates[0],
                cellCriticalVolume - defaultVolume,
                cellCriticalVolume - defaultVolume,
                cellCriticalVolume - rates[3],
                scales[4] * cellCriticalVolume,
                scales[5] * cellCriticalVolume,
                cellCriticalVolume + rates[6],
                scales[7] * cellCriticalVolume,
        };
        
        for (int i = 0; i < scales.length; i++) {
            PottsCell cell = make(true);
            cell.reset(null, null);
            cell.updateTarget(rates[i], scales[i]);
            
            double expectedVolume = expectedVolumes[i];
            assertEquals(expectedVolume, cell.getTargetVolume(), EPSILON);
            assertEquals(cellCriticalHeight * expectedVolume, cell.getTargetSurface(), EPSILON);
            
            double expectedDefaultVolume = defaultVolume - cellCriticalVolume + expectedVolume;
            assertEquals(expectedDefaultVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
            assertEquals(defaultHeight * expectedDefaultVolume, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        }
    }
    
    @Test
    public void updateTarget_regionNoScale_doesNothing() {
        double scale = 1.0;
        double rate = randomDoubleBetween(0, 100);
        
        PottsCell cell = make(true);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        assertEquals(locationRegionVolumes.get(Region.DEFAULT), cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(locationRegionSurfaces.get(Region.DEFAULT), cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_regionNoRegion_doesNothing() {
        double scale = randomDoubleBetween(1, 10);
        double rate = randomDoubleBetween(0, 100);
        
        PottsCell cell = make(false);
        cell.initialize(null, null);
        cell.updateTarget(Region.DEFAULT, rate, scale);
        
        assertEquals(0, cell.getTargetVolume(Region.DEFAULT), EPSILON);
        assertEquals(0, cell.getTargetSurface(Region.DEFAULT), EPSILON);
    }
    
    @Test
    public void updateTarget_defaultRegion_updatesValues() {
        double defaultVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double defaultHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double threshold = defaultVolume / cellCriticalVolume;
        
        double[] scales = new double[] {
                randomDoubleBetween(0, 1 - threshold),
                randomDoubleBetween(0, 1 - threshold),
                randomDoubleBetween(0, 1 - threshold),
                randomDoubleBetween(1 - threshold, 1),
                randomDoubleBetween(1 - threshold, 1),
                randomDoubleBetween(1 - threshold, 1),
                randomDoubleBetween(1 + OFFSET, 2),
                randomDoubleBetween(1 + OFFSET, 2),
        };
        
        double[] rates = new double[] {
                randomDoubleBetween(0, threshold) * cellCriticalVolume,
                randomDoubleBetween(threshold, 1 - scales[1]) * cellCriticalVolume,
                randomDoubleBetween(1 - scales[2], 2 - scales[2]) * cellCriticalVolume,
                randomDoubleBetween(0, 1 - scales[3]) * cellCriticalVolume,
                randomDoubleBetween(threshold, 2 - scales[4]) * cellCriticalVolume,
                randomDoubleBetween(1 - scales[5], threshold) * cellCriticalVolume,
                randomDoubleBetween(0, scales[6] - 1) * cellCriticalVolume,
                randomDoubleBetween(scales[7] - 1, scales[7]) * cellCriticalVolume,
        };
        
        double[] expectedVolumes = new double[] {
                cellCriticalVolume - rates[0],
                cellCriticalVolume - defaultVolume,
                cellCriticalVolume - defaultVolume,
                cellCriticalVolume - rates[3],
                scales[4] * cellCriticalVolume,
                scales[5] * cellCriticalVolume,
                cellCriticalVolume + rates[6],
                scales[7] * cellCriticalVolume,
        };
        
        for (int i = 0; i < scales.length; i++) {
            PottsCell cell = make(true);
            cell.reset(null, null);
            cell.updateTarget(Region.DEFAULT, rates[i], scales[i]);
            
            double expectedVolume = expectedVolumes[i];
            assertEquals(expectedVolume, cell.getTargetVolume(), EPSILON);
            assertEquals(cellCriticalHeight * expectedVolume, cell.getTargetSurface(), EPSILON);
            
            double expectedDefaultVolume = defaultVolume - cellCriticalVolume + expectedVolume;
            assertEquals(expectedDefaultVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
            assertEquals(defaultHeight * expectedDefaultVolume, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        }
    }
    
    @Test
    public void updateTarget_nonDefaultRegion_updatesValues() {
        double defaultVolume = criticalVolumesRegionMock.get(Region.DEFAULT);
        double defaultHeight = criticalHeightsRegionMock.get(Region.DEFAULT);
        double regionVolume = criticalVolumesRegionMock.get(Region.NUCLEUS);
        double regionHeight = criticalHeightsRegionMock.get(Region.NUCLEUS);
        double threshold = defaultVolume / regionVolume;
        
        double[] scales = new double[] {
                randomDoubleBetween(threshold + 1, threshold + 2),
                randomDoubleBetween(threshold + 1, threshold + 2),
                randomDoubleBetween(threshold + 1, threshold + 2),
                randomDoubleBetween(1, threshold + 1),
                randomDoubleBetween(1, threshold + 1),
                randomDoubleBetween(1, threshold + 1),
                randomDoubleBetween(0, 1 - OFFSET),
                randomDoubleBetween(0, 1 - OFFSET),
        };
        
        double[] rates = new double[] {
                randomDoubleBetween(0, threshold) * regionVolume,
                randomDoubleBetween(threshold, scales[1] - 1) * regionVolume,
                randomDoubleBetween(scales[2] - 1, scales[2]) * regionVolume,
                randomDoubleBetween(scales[3] - 1, threshold) * regionVolume,
                randomDoubleBetween(0, scales[4] - 1) * regionVolume,
                randomDoubleBetween(threshold, scales[5]) * regionVolume,
                randomDoubleBetween(0, 1 - scales[6]) * regionVolume,
                randomDoubleBetween(1 - scales[7], 2 - scales[7]) * regionVolume,
        };
        
        double[] expectedRegionVolumes = new double[] {
                regionVolume + rates[0],
                regionVolume + defaultVolume,
                regionVolume + defaultVolume,
                scales[3] * regionVolume,
                regionVolume + rates[4],
                scales[5] * regionVolume,
                regionVolume - rates[6],
                scales[7] * regionVolume,
        };
        
        for (int i = 0; i < scales.length; i++) {
            PottsCell cell = make(true);
            cell.reset(null, null);
            cell.updateTarget(Region.NUCLEUS, rates[i], scales[i]);
            
            double expectedRegionVolume = expectedRegionVolumes[i];
            assertEquals(expectedRegionVolume, cell.getTargetVolume(Region.NUCLEUS), EPSILON);
            assertEquals(regionHeight * expectedRegionVolume, cell.getTargetSurface(Region.NUCLEUS), EPSILON);
            
            double expectedDefaultVolume = defaultVolume + regionVolume - expectedRegionVolume;
            assertEquals(expectedDefaultVolume, cell.getTargetVolume(Region.DEFAULT), EPSILON);
            assertEquals(defaultHeight * expectedDefaultVolume, cell.getTargetSurface(Region.DEFAULT), EPSILON);
        }
    }
    
    @Test
    public void convert_noRegions_createsContainer() {
        Location location = mock(PottsLocation.class);
        MiniBox parameters = mock(MiniBox.class);
        
        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        
        PottsCell cell = new PottsCellMock(id, parent, pop, state, age, divisions, location,
                false, parameters, criticalVolume, criticalHeight,
                null, null);
        doReturn(phase).when((PottsModule) cell.getModule()).getPhase();
        
        int voxels = randomIntBetween(1, 100);
        doReturn((double) voxels).when(location).getVolume();
        
        PottsCellContainer container = (PottsCellContainer) cell.convert();
        
        assertEquals(id, container.id);
        assertEquals(parent, container.parent);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(divisions, container.divisions);
        assertEquals(state, container.state);
        assertEquals(phase, container.phase);
        assertEquals(voxels, container.voxels);
        assertNull(container.regionVoxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        assertNull(container.criticalRegionVolumes);
        assertNull(container.criticalRegionHeights);
    }
    
    @Test
    public void convert_withRegions_createsContainer() {
        PottsLocation location = mock(PottsLocation.class);
        MiniBox parameters = mock(MiniBox.class);
        
        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        
        EnumSet<Region> regions = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regions).when(location).getRegions();
        
        EnumMap<Region, Double> criticalRegionVolumes = makeRegionEnumMap();
        EnumMap<Region, Double> criticalRegionHeights = makeRegionEnumMap();
        
        PottsCell cell = new PottsCellMock(id, parent, pop, state, age, divisions, location, true,
                parameters, criticalVolume, criticalHeight,
                criticalRegionVolumes, criticalRegionHeights);
        doReturn(phase).when((PottsModule) cell.getModule()).getPhase();
        
        int voxels = randomIntBetween(1, 100);
        doReturn((double) voxels).when(location).getVolume();
        
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        for (Region region : regions) {
            int value = randomIntBetween(1, 100);
            regionVoxels.put(region, value);
            doReturn((double) value).when(location).getVolume(region);
        }
        
        PottsCellContainer container = (PottsCellContainer) cell.convert();
        
        assertEquals(id, container.id);
        assertEquals(parent, container.parent);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(divisions, container.divisions);
        assertEquals(state, container.state);
        assertEquals(phase, container.phase);
        assertEquals(voxels, container.voxels);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
        
        for (Region region : regions) {
            assertEquals(regionVoxels.get(region), container.regionVoxels.get(region));
            assertEquals(criticalRegionVolumes.get(region), container.criticalRegionVolumes.get(region), EPSILON);
            assertEquals(criticalRegionHeights.get(region), container.criticalRegionHeights.get(region), EPSILON);
        }
    }
}
