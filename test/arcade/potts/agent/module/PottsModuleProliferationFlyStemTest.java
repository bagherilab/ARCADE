package arcade.potts.agent.module;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import arcade.core.env.location.Location;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.env.location.Voxel;
import static org.junit.jupiter.api.Assertions.*;

public class PottsModuleProliferationFlyStemTest {

    static ArrayList<Voxel> voxelListForAddRemove;

    static ArrayList<Voxel> voxelListA;

    static ArrayList<Voxel> voxelListB;

    static ArrayList<Voxel> voxelListAB;

    @BeforeAll
    public static void setupLists() {
        voxelListForAddRemove = new ArrayList<>();
        voxelListForAddRemove.add(new Voxel(0, 0, 0));
        voxelListForAddRemove.add(new Voxel(1, 0, 0));

        /*
         * Lattice site shape:
         *
         *     x x x x
         *     x     x
         *     x
         *
         * Each list is a subset of the shape:
         *
         *  (A)         (B)
         *  x x . .     . . x x
         *  x     .     .     x
         *  x           .
         */

        voxelListA = new ArrayList<>();
        voxelListA.add(new Voxel(0, 0, 1));
        voxelListA.add(new Voxel(0, 1, 1));
        voxelListA.add(new Voxel(1, 0, 1));
        voxelListA.add(new Voxel(0, 2, 1));

        voxelListB = new ArrayList<>();
        voxelListB.add(new Voxel(2, 0, 1));
        voxelListB.add(new Voxel(3, 0, 1));
        voxelListB.add(new Voxel(3, 1, 1));

        voxelListAB = new ArrayList<>(voxelListA);
        voxelListAB.addAll(voxelListB);
    }

    @Test
    void getSmallerLocation_location2Smaller_doesNotReturnLocation1() {
        Location location1 = new PottsLocation2D(voxelListA);
        Location location2 = new PottsLocation2D(voxelListB);

        Location result = PottsModuleProliferationFlyStem.getSmallerLocation(location1, location2);
        assertNotEquals(location1, result);
    }

    @Test
    void getSmallerLocation_location2Smaller_returnsLocation2() {
        Location location1 = new PottsLocation2D(voxelListA);
        Location location2 = new PottsLocation2D(voxelListB);

        Location result = PottsModuleProliferationFlyStem.getSmallerLocation(location1, location2);
        assertEquals(location2, result);
    }

    @Test
    void getSmallerLocation_equalVolumes_returnsLocation2() {
        voxelListB.add(new Voxel(4, 0, 1));
        Location location1 = new PottsLocation2D(voxelListA);
        Location location2 = new PottsLocation2D(voxelListB);

        Location result = PottsModuleProliferationFlyStem.getSmallerLocation(location1, location2);
        assertEquals(location2, result);
    }
}
