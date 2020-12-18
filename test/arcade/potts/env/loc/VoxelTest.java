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
        assertEquals(voxel1, voxel2);
        assertEquals(voxel2, voxel1);
    }
    
    @Test
    public void equals_validUnequalObject_returnsFalse() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel1 = new Voxel(x, y, z);
        Voxel voxel2 = new Voxel(x, y, z + 1);
        assertNotEquals(voxel1, voxel2);
        assertNotEquals(voxel2, voxel1);
    }
    
    @Test
    public void equals_invalidObject_returnsFalse() {
        int x = (int) (Math.random() * 100);
        int y = (int) (Math.random() * 100);
        int z = (int) (Math.random() * 100);
        Voxel voxel = new Voxel(x, y, z);
        Object object = x;
        assertNotEquals(voxel, object);
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
