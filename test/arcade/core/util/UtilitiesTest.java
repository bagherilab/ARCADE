package arcade.core.util;

import org.junit.*;
import java.util.ArrayList;
import java.util.Comparator;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static arcade.core.util.Utilities.*;
import static arcade.core.TestUtilities.*;

public class UtilitiesTest {
    @Test
    public void copyArray_givenArray_createsDeepCopy() {
        int x = randomIntBetween(2, 10);
        int y = randomIntBetween(2, 10);
        int z = randomIntBetween(2, 10);
        
        double[][][] fromArray = new double[x][y][z];
        double[][][] toArray = new double[x][y][z];
        
        // Population the from array.
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    fromArray[i][j][k] = randomDoubleBetween(0, 10);
                }
            }
        }
        
        copyArray(fromArray, toArray);
        
        // Check that contents match.
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    assertEquals(fromArray[i][j][k], toArray[i][j][k], EPSILON);
                }
            }
        }
        
        // Check that objects do not match.
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                assertNotSame(fromArray[i][j], toArray[i][j]);
            }
        }
    }
    
    @Test
    public void shuffleList_givenRandomNumberGenerator_shufflesList() {
        int seed = randomIntBetween(1, 1000);
        MersenneTwisterFast rng = new MersenneTwisterFast(seed);
        
        ArrayList<Integer> unshuffledList = new ArrayList<>();
        ArrayList<Integer> shuffledList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            unshuffledList.add(i);
            shuffledList.add(i);
        }
        
        shuffleList(shuffledList, rng);
        
        // Check that shuffled list and unshuffled lists are not the same order.
        assertNotEquals(unshuffledList, shuffledList);
        
        // Check that contents of lists are the same.
        shuffledList.sort(Comparator.comparingInt(integer -> integer));
        assertEquals(unshuffledList, shuffledList);
    }
    
    @Test
    public void shuffleList_givenSameSeed_shufflesSame() {
        int seed = randomIntBetween(1, 1000);
        MersenneTwisterFast rng1 = new MersenneTwisterFast(seed);
        MersenneTwisterFast rng2 = new MersenneTwisterFast(seed);
        
        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list1.add(i);
            list2.add(i);
        }
        
        shuffleList(list1, rng1);
        shuffleList(list2, rng2);
        
        // Check that both shuffled lists are the same.
        assertEquals(list1, list2);
    }
}
