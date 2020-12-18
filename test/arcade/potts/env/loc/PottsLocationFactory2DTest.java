package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.BeforeClass;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;

public class PottsLocationFactory2DTest {
    static final PottsLocationFactory2D FACTORY = mock(PottsLocationFactory2D.class, CALLS_REAL_METHODS);
    
    @BeforeClass
    public static void setupMocks() {
        FACTORY.random = mock(MersenneTwisterFast.class);
    }
    
    @Test
    public void convert_exactOddSquares_calculateValue() {
        assertEquals(1, FACTORY.convert(1 * 1 * 1));
        assertEquals(3, FACTORY.convert(3 * 3 * 1));
        assertEquals(5, FACTORY.convert(5 * 5 * 1));
        assertEquals(7, FACTORY.convert(7 * 7 * 1));
    }
    
    @Test
    public void convert_exactEvenSquares_calculateValue() {
        assertEquals(3, FACTORY.convert(2 * 2 * 1));
        assertEquals(5, FACTORY.convert(4 * 4 * 1));
        assertEquals(7, FACTORY.convert(6 * 6 * 1));
        assertEquals(9, FACTORY.convert(8 * 8 * 1));
    }
    
    @Test
    public void convert_inexactOddSquares_calculateValue() {
        assertEquals(3, FACTORY.convert(1 * 1 * 1 + 1));
        assertEquals(5, FACTORY.convert(3 * 3 * 1 + 1));
        assertEquals(7, FACTORY.convert(5 * 5 * 1 + 1));
        assertEquals(9, FACTORY.convert(7 * 7 * 1 + 1));
    }
    
    @Test
    public void convert_inexactEvenSquares_calculateValue() {
        assertEquals(3, FACTORY.convert(2 * 2 * 1 - 1));
        assertEquals(5, FACTORY.convert(4 * 4 * 1 - 1));
        assertEquals(7, FACTORY.convert(6 * 6 * 1 - 1));
        assertEquals(9, FACTORY.convert(8 * 8 * 1 - 1));
    }
    
    @Test
    public void getPossible_givenZero_createsEmpty() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> list = factory.getPossible(new Voxel(0, 0, 0), 0);
        assertEquals(0, list.size());
    }
    
    @Test
    public void getPossible_givenSize_createsList() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        
        int x = randomIntBetween(0, 10);
        int y = randomIntBetween(0, 10);
        int z = randomIntBetween(0, 10);
        int n = randomIntBetween(0, 10) * 2 + 1;
        
        ArrayList<Voxel> list = factory.getPossible(new Voxel(x, y, z), n);
        assertEquals(n * n, list.size());
        
        for (Voxel voxel : list) {
            assertTrue(voxel.x < x + (n - 1) / 2 + 1);
            assertTrue(voxel.x > x - (n - 1) / 2 - 1);
            assertTrue(voxel.y < y + (n - 1) / 2 + 1);
            assertTrue(voxel.y > y - (n - 1) / 2 - 1);
            assertEquals(0, voxel.z);
        }
    }
    
    @Test
    public void getCenters_threeSideExactEqualSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 1, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 0));
        expected.add(new Voxel(2, 5, 0));
        expected.add(new Voxel(5, 2, 0));
        expected.add(new Voxel(5, 5, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideExactUnequalSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(11, 8, 1, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 0));
        expected.add(new Voxel(2, 5, 0));
        expected.add(new Voxel(5, 2, 0));
        expected.add(new Voxel(5, 5, 0));
        expected.add(new Voxel(8, 2, 0));
        expected.add(new Voxel(8, 5, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideInexactEqualSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(7, 7, 1, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideInexactUnequalSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(10, 7, 1, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 0));
        expected.add(new Voxel(5, 2, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideExactEqualSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(12, 12, 1, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 0));
        expected.add(new Voxel(3, 8, 0));
        expected.add(new Voxel(8, 3, 0));
        expected.add(new Voxel(8, 8, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideExactUnequalSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(17, 12, 1, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 0));
        expected.add(new Voxel(3, 8, 0));
        expected.add(new Voxel(8, 3, 0));
        expected.add(new Voxel(8, 8, 0));
        expected.add(new Voxel(13, 3, 0));
        expected.add(new Voxel(13, 8, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideInexactEqualSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(11, 11, 1, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideInexactUnequalSize_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(16, 11, 1, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 0));
        expected.add(new Voxel(8, 3, 0));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void increase_exactTarget_updatesList() {
        ArrayList<Voxel> allVoxels = new ArrayList<>();
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                allVoxels.add(new Voxel(i - n / 2, j - n / 2, 0));
            }
        }
        
        voxels.add(new Voxel(0, 0, 0));
        FACTORY.increase(allVoxels, voxels, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        
        voxels.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected, voxels);
    }
    
    @Test
    public void increase_inexactTarget_updatesList() {
        ArrayList<Voxel> allVoxels = new ArrayList<>();
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                allVoxels.add(new Voxel(i - n / 2, j - n / 2, 0));
            }
        }
        
        voxels.add(new Voxel(0, 0, 0));
        FACTORY.increase(allVoxels, voxels, 4);
        
        HashSet<Voxel> expected = new HashSet<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        
        assertEquals(4, voxels.size());
        assertTrue(expected.containsAll(voxels));
    }
    
    @Test
    public void increase_invalidTarget_addsValid() {
        ArrayList<Voxel> allVoxels = new ArrayList<>();
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = 10;
        for (int i = 0; i < n; i++) {
            allVoxels.add(new Voxel(i - n / 2, 0, 0));
        }
        
        voxels.add(new Voxel(0, 0, 0));
        FACTORY.increase(allVoxels, voxels, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        
        voxels.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected, voxels);
    }
    
    @Test
    public void decrease_exactTarget_updatesList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        FACTORY.decrease(voxels, 1);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(0, 0, 0));
        
        voxels.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected, voxels);
    }
    
    @Test
    public void decrease_inexactTarget_updatesList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        FACTORY.decrease(voxels, 3);
        
        HashSet<Voxel> expected = new HashSet<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        
        assertEquals(3, voxels.size());
        assertTrue(expected.containsAll(voxels));
    }
}
