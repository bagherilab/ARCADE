package arcade.potts.env.grid;

import org.junit.Test;
import sim.util.Bag;
import arcade.core.agent.cell.Cell;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PottsGridTest {
    private static Cell createObjectMock(int id) {
        Cell object = mock(Cell.class);
        doReturn(id).when(object).getID();
        return object;
    }
    
    @Test
    public void getAllObjects_withoutContents_returnsEmpty() {
        PottsGrid grid = new PottsGrid();
        assertEquals(0, grid.getAllObjects().size());
    }
    
    @Test
    public void getAllObjects_withContents_returnsContents() {
        Cell objectA = createObjectMock(1);
        Cell objectB = createObjectMock(2);
        PottsGrid grid = new PottsGrid();
        grid.addObject(objectA, null);
        grid.addObject(objectB, null);
        assertEquals(2, grid.getAllObjects().size());
        assertTrue(grid.getAllObjects().contains(objectA));
        assertTrue(grid.getAllObjects().contains(objectB));
    }
    
    @Test
    public void addObject_validID_updatesObject() {
        Cell object = createObjectMock(1);
        PottsGrid grid = new PottsGrid();
        grid.addObject(object, null);
        
        Bag allObjects = new Bag();
        allObjects.add(object);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void addObject_existingID_doesNothing() {
        Cell objectA = createObjectMock(1);
        Cell objectB = createObjectMock(1);
        PottsGrid grid = new PottsGrid();
        grid.addObject(objectA, null);
        grid.addObject(objectB, null);
        
        Bag allObjects = new Bag();
        allObjects.add(objectA);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void addObject_zeroID_doesNothing() {
        Cell object = createObjectMock(0);
        PottsGrid grid = new PottsGrid();
        grid.addObject(object, null);
        assertEquals(0, grid.allObjects.size());
    }
    
    @Test
    public void addObject_nullObject_doesNothing() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(null, null);
        assertEquals(0, grid.allObjects.size());
    }
    
    @Test
    public void removeObject_existingID_updatesObject() {
        Cell objectA = createObjectMock(1);
        Cell objectB = createObjectMock(2);
        PottsGrid grid = new PottsGrid();
        grid.addObject(objectA, null);
        grid.addObject(objectB, null);
        grid.removeObject(objectA, null);
        
        Bag allObjects = new Bag();
        allObjects.add(objectB);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void removeObject_invalidID_doesNothing() {
        Cell objectA = createObjectMock(1);
        Cell objectB = createObjectMock(2);
        PottsGrid grid = new PottsGrid();
        grid.addObject(objectA, null);
        grid.removeObject(objectB, null);
        
        Bag allObjects = new Bag();
        allObjects.add(objectA);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void removeObject_zeroID_doesNothing() {
        Cell object = createObjectMock(0);
        PottsGrid grid = new PottsGrid();
        grid.removeObject(object, null);
        assertEquals(0, grid.allObjects.size());
    }
    
    @Test
    public void getObjectAt_validID_returnObject() {
        Cell objectA = createObjectMock(1);
        Cell objectB = createObjectMock(2);
        PottsGrid grid = new PottsGrid();
        grid.addObject(objectA, null);
        grid.addObject(objectB, null);
        
        assertSame(objectA, grid.getObjectAt(1));
        assertSame(objectB, grid.getObjectAt(2));
    }
    
    @Test
    public void getObjectAt_invalidID_returnNull() {
        PottsGrid grid = new PottsGrid();
        assertNull(grid.getObjectAt(1));
    }
    
    @Test
    public void getObjectAt_zeroID_returnNull() {
        PottsGrid grid = new PottsGrid();
        assertNull(grid.getObjectAt(0));
    }
}
