package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.Test;
import arcade.core.env.location.*;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferation;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.location.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellStemTest {
    private static final double EPSILON = 1E-8;
    
    static PottsLocation locationMock = mock(PottsLocation.class);
    
    static int cellID = randomIntBetween(1, 10);
    
    static int cellParent = randomIntBetween(1, 10);
    
    static int cellPop = randomIntBetween(1, 10);
    
    static int cellAge = randomIntBetween(1, 1000);
    
    static int cellDivisions = randomIntBetween(1, 100);
    
    static double cellCriticalVolume = randomDoubleBetween(10, 100);
    
    static double cellCriticalHeight = randomDoubleBetween(10, 100);
    
    static State cellState = State.UNDEFINED;
    
    static MiniBox parametersMock = mock(MiniBox.class);
    
    @Test
    public void setState_givenState_assignsValue() {
        PottsCellStem cell = new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        
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
        PottsCellStem cell = new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
        
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
        PottsCellStem cell = new PottsCellStem(cellID, cellParent, cellPop, cellState, cellAge, cellDivisions,
                locationMock, false, parametersMock, cellCriticalVolume, cellCriticalHeight,
                null, null);
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
        PottsCellStem cell2 = (PottsCellStem) cell1.make(cellID + 1, State.QUIESCENT, location2, null);
        
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
        PottsCellStem cell2 = (PottsCellStem) cell1.make(cellID + 1, State.QUIESCENT, location2, null);
        
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
