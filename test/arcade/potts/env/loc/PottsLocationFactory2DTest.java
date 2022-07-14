package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;

public class PottsLocationFactory2DTest {
    static final MersenneTwisterFast RANDOM = mock(MersenneTwisterFast.class);
    
    @Test
    public void getPossible_givenZero_createsEmpty() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> list = factory.getPossible(new Voxel(0, 0, 0), 0, 0);
        assertEquals(0, list.size());
    }
    
    @Test
    public void getPossible_givenSize_createsList() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        
        int x = randomIntBetween(0, 10);
        int y = randomIntBetween(0, 10);
        int z = randomIntBetween(0, 10);
        int s = randomIntBetween(0, 10) * 2 + 1;
        
        ArrayList<Voxel> list = factory.getPossible(new Voxel(x, y, z), s, 0);
        assertEquals(s * s, list.size());
        
        for (Voxel voxel : list) {
            assertTrue(voxel.x < x + (s - 1) / 2 + 1);
            assertTrue(voxel.x > x - (s - 1) / 2 - 1);
            assertTrue(voxel.y < y + (s - 1) / 2 + 1);
            assertTrue(voxel.y > y - (s - 1) / 2 - 1);
            assertEquals(0, voxel.z);
        }
    }
    
    @Test
    public void getCenters_threeSideExactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 1, 0, 3, 0);
        
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
    public void getCenters_threeSideExactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(11, 8, 1, 0, 3, 0);
        
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
    public void getCenters_threeSideInexactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(7, 7, 1, 0, 3, 0);
        
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
    public void getCenters_threeSideInexactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(10, 7, 1, 0, 3, 0);
        
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
    public void getCenters_fiveSideExactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(12, 12, 1, 0, 5, 0);
        
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
    public void getCenters_fiveSideExactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(17, 12, 1, 0, 5, 0);
        
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
    public void getCenters_fiveSideInexactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(11, 11, 1, 0, 5, 0);
        
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
    public void getCenters_fiveSideInexactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory2D factory = new PottsLocationFactory2D();
        ArrayList<Voxel> centers = factory.getCenters(16, 11, 1, 0, 5, 0);
        
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
        PottsLocationFactory2D.increase(allVoxels, voxels, 5, RANDOM);
        
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
        PottsLocationFactory2D.increase(allVoxels, voxels, 4, RANDOM);
        
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
        PottsLocationFactory2D.increase(allVoxels, voxels, 5, RANDOM);
        
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
        PottsLocationFactory2D.decrease(voxels, 1, RANDOM);
        
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
        PottsLocationFactory2D.decrease(voxels, 3, RANDOM);
        
        HashSet<Voxel> expected = new HashSet<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        
        assertEquals(3, voxels.size());
        assertTrue(expected.contains(new Voxel(0, 0, 0)));
        assertTrue(expected.containsAll(voxels));
    }
    
    @Test
    public void decrease_unconnectedTarget_updatesList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(1, 1, 0));
        voxels.add(new Voxel(-1, -1, 0));
        voxels.add(new Voxel(1, -1, 0));
        voxels.add(new Voxel(-1, 1, 0));
        
        PottsLocationFactory2D.decrease(voxels, 7, RANDOM);
        
        int[][] corners = new int[][] {
                { -1, -1 },
                { 1, -1 },
                { 1, 1 },
                { -1, 1 },
        };
        
        int[][] sides = new int[][] {
                { 0, -1 },
                { 1, 0 },
                { 0, 1 },
                { -1, 0 },
        };
        
        for (int i = 0; i < corners.length; i++) {
            if (voxels.contains(new Voxel(corners[i][0], corners[i][1], 0))) {
                boolean checkA = voxels.contains(new Voxel(sides[i][0], sides[i][1], 0));
                boolean checkB = voxels.contains(new Voxel(sides[(i + 1) % 4][0], sides[(i + 1) % 4][1], 0));
                assertTrue(checkA || checkB);
            }
        }
    }
    
    @Test
    public void decrease_invalidTarget_updatesList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(2, 0, 0));
        voxels.add(new Voxel(3, 0, 0));
        voxels.add(new Voxel(4, 0, 0));
        PottsLocationFactory2D.decrease(voxels, 2, RANDOM);
    
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(3, 0, 0));
        expected.add(new Voxel(4, 0, 0));
        
        voxels.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected, voxels);
    }
}
