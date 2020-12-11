package arcade.potts.env.loc;

import org.junit.Test;
import static org.junit.Assert.*;

public class VoxelTest {
    @Test
    public void hashCode_validObject_returnCode() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel = new Voxel(x, y, z);
        assertEquals(x + y * (int) Math.pow(2, 8) + z * (int) Math.pow(2, 16), voxel.hashCode());
    }
    
    @Test
    public void equals_validEqualObject_returnsTrue() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel1 = new Voxel(x, y, z);
        Voxel voxel2 = new Voxel(x, y, z);
        assertTrue(voxel1.equals(voxel2));
        assertTrue(voxel2.equals(voxel1));
    }
    
    @Test
    public void equals_validUnequalObject_returnsFalse() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel1 = new Voxel(x, y, z);
        Voxel voxel2 = new Voxel(x, y, z + 1);
        assertFalse(voxel1.equals(voxel2));
        assertFalse(voxel2.equals(voxel1));
    }
    
    @Test
    public void equals_invalidObject_returnsFalse() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel = new Voxel(x, y, z);
        Object object = x;
        assertFalse(voxel.equals(object));
    }
    
    @Test
    public void toString_validObject_returnsValue() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel = new Voxel(x, y, z);
        assertEquals("[" + x + ", " + y + ", " + z + "]", voxel.toString());
    }
}
