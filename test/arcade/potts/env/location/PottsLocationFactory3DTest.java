package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.env.location.Voxel.VOXEL_COMPARATOR;

public class PottsLocationFactory3DTest {
    static final MersenneTwisterFast RANDOM = mock(MersenneTwisterFast.class);
    
    @Test
    public void getPossible_givenZero_createsEmpty() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> list = factory.getPossible(new Voxel(0, 0, 0), 0, 0);
        assertEquals(0, list.size());
    }
    
    @Test
    public void getPossible_givenSize_createsList() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        
        int x = randomIntBetween(0, 10);
        int y = randomIntBetween(0, 10);
        int z = randomIntBetween(0, 10);
        int s = randomIntBetween(0, 10) * 2 + 1;
        int h = randomIntBetween(0, 10) * 2 + 1;
        
        ArrayList<Voxel> list = factory.getPossible(new Voxel(x, y, z), s, h);
        assertEquals(s * s * h, list.size());
        
        for (Voxel voxel : list) {
            assertTrue(voxel.x < x + (s - 1) / 2 + 1);
            assertTrue(voxel.x > x - (s - 1) / 2 - 1);
            assertTrue(voxel.y < y + (s - 1) / 2 + 1);
            assertTrue(voxel.y > y - (s - 1) / 2 - 1);
            assertTrue(voxel.z < z + (h - 1) / 2 + 1);
            assertTrue(voxel.z > z - (h - 1) / 2 - 1);
        }
    }
    
    @Test
    public void getCenters_threeSideExactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 8, 0, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 2));
        expected.add(new Voxel(2, 5, 2));
        expected.add(new Voxel(5, 2, 2));
        expected.add(new Voxel(5, 5, 2));
        expected.add(new Voxel(2, 2, 5));
        expected.add(new Voxel(2, 5, 5));
        expected.add(new Voxel(5, 2, 5));
        expected.add(new Voxel(5, 5, 5));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideExactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(11, 8, 5, 0, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 2));
        expected.add(new Voxel(2, 5, 2));
        expected.add(new Voxel(5, 2, 2));
        expected.add(new Voxel(5, 5, 2));
        expected.add(new Voxel(8, 2, 2));
        expected.add(new Voxel(8, 5, 2));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideInexactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(7, 7, 7, 0, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 2));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideInexactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(10, 7, 13, 0, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 2));
        expected.add(new Voxel(5, 2, 2));
        expected.add(new Voxel(2, 2, 5));
        expected.add(new Voxel(5, 2, 5));
        expected.add(new Voxel(2, 2, 8));
        expected.add(new Voxel(5, 2, 8));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideExactWithMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(14, 11, 8, 3, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(5, 5, 2));
        expected.add(new Voxel(8, 5, 2));
        expected.add(new Voxel(5, 5, 5));
        expected.add(new Voxel(8, 5, 5));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeSideInexactWithMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(13, 10, 8, 2, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(4, 4, 2));
        expected.add(new Voxel(7, 4, 2));
        expected.add(new Voxel(4, 4, 5));
        expected.add(new Voxel(7, 4, 5));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideExactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(12, 12, 12, 0, 5, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 3));
        expected.add(new Voxel(3, 8, 3));
        expected.add(new Voxel(8, 3, 3));
        expected.add(new Voxel(8, 8, 3));
        expected.add(new Voxel(3, 3, 8));
        expected.add(new Voxel(3, 8, 8));
        expected.add(new Voxel(8, 3, 8));
        expected.add(new Voxel(8, 8, 8));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideExactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(17, 12, 7, 0, 5, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 3));
        expected.add(new Voxel(3, 8, 3));
        expected.add(new Voxel(8, 3, 3));
        expected.add(new Voxel(8, 8, 3));
        expected.add(new Voxel(13, 3, 3));
        expected.add(new Voxel(13, 8, 3));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideInexactEqualSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(11, 11, 11, 0, 5, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 3));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideInexactUnequalSizeNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(16, 11, 9, 0, 5, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(3, 3, 3));
        expected.add(new Voxel(8, 3, 3));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideExactWithMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(22, 17, 12, 5, 5, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(8, 8, 3));
        expected.add(new Voxel(13, 8, 3));
        expected.add(new Voxel(8, 8, 8));
        expected.add(new Voxel(13, 8, 8));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveSideInexactWithMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(23, 19, 12, 2, 5, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(5, 5, 3));
        expected.add(new Voxel(10, 5, 3));
        expected.add(new Voxel(15, 5, 3));
        expected.add(new Voxel(5, 10, 3));
        expected.add(new Voxel(10, 10, 3));
        expected.add(new Voxel(15, 10, 3));
        expected.add(new Voxel(5, 5, 8));
        expected.add(new Voxel(10, 5, 8));
        expected.add(new Voxel(15, 5, 8));
        expected.add(new Voxel(5, 10, 8));
        expected.add(new Voxel(10, 10, 8));
        expected.add(new Voxel(15, 10, 8));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_threeHeightAllLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 3, 0, 3, 1);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 1));
        expected.add(new Voxel(2, 5, 1));
        expected.add(new Voxel(5, 2, 1));
        expected.add(new Voxel(5, 5, 1));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveHeightAllLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 5, 0, 3, 1);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 1));
        expected.add(new Voxel(2, 5, 1));
        expected.add(new Voxel(5, 2, 1));
        expected.add(new Voxel(5, 5, 1));
        expected.add(new Voxel(2, 2, 2));
        expected.add(new Voxel(2, 5, 2));
        expected.add(new Voxel(5, 2, 2));
        expected.add(new Voxel(5, 5, 2));
        expected.add(new Voxel(2, 2, 3));
        expected.add(new Voxel(2, 5, 3));
        expected.add(new Voxel(5, 2, 3));
        expected.add(new Voxel(5, 5, 3));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveHeightUnfilledLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 5, 0, 3, 2);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 1));
        expected.add(new Voxel(2, 5, 1));
        expected.add(new Voxel(5, 2, 1));
        expected.add(new Voxel(5, 5, 1));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_fiveHeightFilledLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 5, 0, 3, 3);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 2));
        expected.add(new Voxel(2, 5, 2));
        expected.add(new Voxel(5, 2, 2));
        expected.add(new Voxel(5, 5, 2));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_sevenHeightAllLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 7, 0, 3, 1);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            expected.add(new Voxel(2, 2, i));
            expected.add(new Voxel(2, 5, i));
            expected.add(new Voxel(5, 2, i));
            expected.add(new Voxel(5, 5, i));
        }
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_sevenHeightUnfilledLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 7, 0, 3, 2);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 1));
        expected.add(new Voxel(2, 5, 1));
        expected.add(new Voxel(5, 2, 1));
        expected.add(new Voxel(5, 5, 1));
        expected.add(new Voxel(2, 2, 3));
        expected.add(new Voxel(2, 5, 3));
        expected.add(new Voxel(5, 2, 3));
        expected.add(new Voxel(5, 5, 3));
        
        centers.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected.size(), centers.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), centers.get(i));
        }
    }
    
    @Test
    public void getCenters_sevenHeightFilledLayersNoMargin_createsCenters() {
        PottsLocationFactory3D factory = new PottsLocationFactory3D();
        ArrayList<Voxel> centers = factory.getCenters(8, 8, 7, 0, 3, 5);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 2, 3));
        expected.add(new Voxel(2, 5, 3));
        expected.add(new Voxel(5, 2, 3));
        expected.add(new Voxel(5, 5, 3));
        
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
                for (int k = 0; k < n; k++) {
                    allVoxels.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                }
            }
        }
        
        voxels.add(new Voxel(0, 0, 0));
        PottsLocationFactory3D.increase(allVoxels, voxels, 7, RANDOM);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        expected.add(new Voxel(0, 0, 1));
        expected.add(new Voxel(0, 0, -1));
        
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
                for (int k = 0; k < n; k++) {
                    allVoxels.add(new Voxel(i - n / 2, j - n / 2, k - n / 2));
                }
            }
        }
        
        voxels.add(new Voxel(0, 0, 0));
        PottsLocationFactory3D.increase(allVoxels, voxels, 6, RANDOM);
        
        HashSet<Voxel> expected = new HashSet<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        expected.add(new Voxel(0, 0, 1));
        expected.add(new Voxel(0, 0, -1));
        
        assertEquals(6, voxels.size());
        assertTrue(expected.containsAll(voxels));
    }
    
    @Test
    public void increase_invalidTarget_addsValid() {
        ArrayList<Voxel> allVoxels = new ArrayList<>();
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                allVoxels.add(new Voxel(i - n / 2, j - n / 2, 0));
            }
        }
        
        voxels.add(new Voxel(0, 0, 0));
        PottsLocationFactory3D.increase(allVoxels, voxels, 7, RANDOM);
        
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
    public void decrease_exactTarget_updatesList() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(1, 0, 0));
        voxels.add(new Voxel(-1, 0, 0));
        voxels.add(new Voxel(0, -1, 0));
        voxels.add(new Voxel(0, 1, 0));
        voxels.add(new Voxel(0, 0, 1));
        voxels.add(new Voxel(0, 0, -1));
        PottsLocationFactory3D.decrease(voxels, 1, RANDOM);
        
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
        voxels.add(new Voxel(0, 0, 1));
        voxels.add(new Voxel(0, 0, -1));
        PottsLocationFactory3D.decrease(voxels, 4, RANDOM);
        
        HashSet<Voxel> expected = new HashSet<>();
        expected.add(new Voxel(0, 0, 0));
        expected.add(new Voxel(1, 0, 0));
        expected.add(new Voxel(-1, 0, 0));
        expected.add(new Voxel(0, -1, 0));
        expected.add(new Voxel(0, 1, 0));
        expected.add(new Voxel(0, 0, 1));
        expected.add(new Voxel(0, 0, -1));
        
        assertEquals(4, voxels.size());
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
        
        voxels.add(new Voxel(0, 0, -1));
        voxels.add(new Voxel(1, 0, -1));
        voxels.add(new Voxel(-1, 0, -1));
        voxels.add(new Voxel(0, -1, -1));
        voxels.add(new Voxel(0, 1, -1));
        
        voxels.add(new Voxel(0, 0, 1));
        voxels.add(new Voxel(1, 0, 1));
        voxels.add(new Voxel(-1, 0, 1));
        voxels.add(new Voxel(0, -1, 1));
        voxels.add(new Voxel(0, 1, 1));
        
        PottsLocationFactory3D.decrease(voxels, 17, RANDOM);
        
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
        
        voxels.add(new Voxel(2, 0, 1));
        voxels.add(new Voxel(1, 0, 1));
        voxels.add(new Voxel(0, 0, 1));
        voxels.add(new Voxel(0, 0, 0));
        voxels.add(new Voxel(0, 0, -1));
        voxels.add(new Voxel(1, 0, -1));
        voxels.add(new Voxel(2, 0, -1));
        PottsLocationFactory3D.decrease(voxels, 3, RANDOM);
        
        ArrayList<Voxel> expected = new ArrayList<>();
        expected.add(new Voxel(2, 0, 1));
        expected.add(new Voxel(1, 0, 1));
        expected.add(new Voxel(1, 0, -1));
        expected.add(new Voxel(2, 0, -1));
        
        voxels.sort(VOXEL_COMPARATOR);
        expected.sort(VOXEL_COMPARATOR);
        
        assertEquals(expected, voxels);
    }
}
