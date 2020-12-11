package arcade.potts.env.grid;

import org.junit.Test;
import sim.util.Bag;
import static org.junit.Assert.*;

public class PottsGridTest {
    final String objectA = "A";
    final String objectB = "B";
    
    @Test
    public void getAllObjects_withoutContents_returnsEmpty() {
        PottsGrid grid = new PottsGrid();
        assertEquals(0, grid.getAllObjects().size());
    }
    
    @Test
    public void getAllObjects_withContents_returnsContents() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, objectA);
        grid.addObject(2, objectB);
        assertEquals(2, grid.getAllObjects().size());
        assertTrue(grid.getAllObjects().contains(objectA));
        assertTrue(grid.getAllObjects().contains(objectB));
    }
    
    @Test
    public void addObject_validID_updatesObject() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, objectA);
        
        Bag allObjects = new Bag();
        allObjects.add(objectA);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void addObject_existingID_doesNothing() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, objectA);
        grid.addObject(1, objectB);
        
        Bag allObjects = new Bag();
        allObjects.add(objectA);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void addObject_zeroID_doesNothing() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(0, objectA);
        assertEquals(0, grid.allObjects.size());
    }
    
    @Test
    public void addObject_nullObject_doesNothing() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, null);
        assertEquals(1, grid.allObjects.size());
    }
    
    @Test
    public void removeObject_existingID_updatesObject() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, objectA);
        grid.addObject(2, objectB);
        grid.removeObject(1);
        
        Bag allObjects = new Bag();
        allObjects.add(objectB);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void removeObject_invalidID_doesNothing() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, objectA);
        grid.removeObject(2);
        
        Bag allObjects = new Bag();
        allObjects.add(objectA);
        
        assertEquals(1, grid.allObjects.size());
        assertSame(allObjects.get(0), grid.allObjects.get(0));
    }
    
    @Test
    public void removeObject_zeroID_doesNothing() {
        PottsGrid grid = new PottsGrid();
        grid.removeObject(0);
        assertEquals(0, grid.allObjects.size());
    }
    
    @Test
    public void getObjectAt_validID_returnObject() {
        PottsGrid grid = new PottsGrid();
        grid.addObject(1, objectA);
        grid.addObject(2, objectB);
        
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
