package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferation;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.location.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellStemTest {
    private static final double EPSILON = 1E-8;
    
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
        when(((PottsLocation) locationMock).convertSurface(anyDouble(), anyDouble())).thenAnswer(answer);
        
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
        
        cellDefault = new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        cellWithRegions = new PottsCellStem(cellID, cellParent, 1, cellState, cellAge, cellDivisions,
                locationMock, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalVolumesRegionMock, criticalHeightsRegionMock);
        cellWithoutRegions = new PottsCellStem(cellID, cellParent, 1, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                criticalVolumesRegionMock, criticalHeightsRegionMock);
    }
    
    static PottsCellStem make(boolean regions) {
        return make(locationMock, regions);
    }
    
    static PottsCellStem make(Location location, boolean regions) {
        if (!regions) {
            return new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                    location, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                    null, null);
        } else {
            return new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                    location, true, parametersMock, cellCriticalVolume, cellCriticalHeight,
                    criticalVolumesRegionMock, criticalHeightsRegionMock);
        }
    }

    @Test
    public void setState_givenState_assignsValue() {
        PottsCellStem cell = make(false);
        
        cell.setState(State.QUIESCENT);
        assertEquals(State.QUIESCENT, cell.getState());
        
        cell.setState(State.PROLIFERATIVE);
        assertEquals(State.PROLIFERATIVE, cell.getState());
        
        cell.setState(State.APOPTOTIC);
        assertEquals(State.APOPTOTIC, cell.getState());
        
        cell.setState(State.NECROTIC);
        assertEquals(State.NECROTIC, cell.getState());
        
        cell.setState(State.AUTOTIC);
        assertEquals(State.AUTOTIC, cell.getState());
    }
    
    @Test
    public void setState_givenState_updatesModule() {
        PottsCellStem cell = make(false);
        
        cell.setState(State.QUIESCENT);
        assertTrue(cell.module instanceof PottsModuleQuiescence);
        
        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.module instanceof PottsModuleProliferation);
        
        cell.setState(State.APOPTOTIC);
        assertTrue(cell.module instanceof PottsModuleApoptosis);
        
        cell.setState(State.NECROTIC);
        assertTrue(cell.module instanceof PottsModuleNecrosis);
        
        cell.setState(State.AUTOTIC);
        assertTrue(cell.module instanceof PottsModuleAutosis);
    }
    
    @Test
    public void setState_invalidState_setsNull() {
        PottsCellStem cell = make(false);
        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }

    @Test
    public void make_noRegions_setsFields() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = mock(MiniBox.class);
        Location location1 = mock(PottsLocation.class);
        Location location2 = mock(PottsLocation.class);
        
        PottsCellStem cell1 = new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                location1, false, parameters, criticalVolume, criticalHeight,
                null, null);
        PottsCellStem cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2, null);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellID, cell2.parent);
        assertEquals(cellPop, cell2.pop);
        assertEquals(cellAge, cell2.getAge());
        assertEquals(cellDivisions + 1, cell1.getDivisions());
        assertEquals(cellDivisions + 1, cell2.getDivisions());
        assertFalse(cell2.hasRegions());
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(criticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell2.getCriticalHeight(), EPSILON);
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
    public void make_hasRegions_setsFields() {
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = mock(MiniBox.class);
        PottsLocation location1 = mock(PottsLocation.class);
        PottsLocation location2 = mock(PottsLocation.class);
        EnumMap<Region, Double> criticalVolumesRegion = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalHeightsRegion = new EnumMap<>(Region.class);
        
        for (Region region : Region.values()) {
            criticalVolumesRegion.put(region, randomDoubleBetween(10, 100));
            criticalHeightsRegion.put(region, randomDoubleBetween(10, 100));
        }
        
        EnumSet<Region> allRegions = EnumSet.allOf(Region.class);
        doReturn(allRegions).when(location1).getRegions();
        doReturn(allRegions).when(location2).getRegions();
        
        PottsCellStem cell1 = new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                location1, true, parameters, criticalVolume, criticalHeight,
                criticalVolumesRegion, criticalHeightsRegion);
        PottsCellStem cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2, null);
        
        assertEquals(cellID + 1, cell2.id);
        assertEquals(cellID, cell2.parent);
        assertEquals(cellPop, cell2.pop);
        assertEquals(cellAge, cell2.getAge());
        assertEquals(cellDivisions + 1, cell1.getDivisions());
        assertEquals(cellDivisions + 1, cell2.getDivisions());
        assertTrue(cell2.hasRegions());
        assertEquals(location2, cell2.getLocation());
        assertEquals(cell2.parameters, parameters);
        assertEquals(criticalVolume, cell2.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell2.getCriticalHeight(), EPSILON);
        for (Region region : Region.values()) {
            assertEquals(criticalVolumesRegion.get(region), cell2.getCriticalVolume(region), EPSILON);
            assertEquals(criticalHeightsRegion.get(region), cell2.getCriticalHeight(region), EPSILON);
        }
    }
}
