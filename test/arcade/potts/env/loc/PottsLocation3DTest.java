package arcade.potts.env.loc;

import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.potts.env.loc.Voxel.VOXEL_COMPARATOR;

public class PottsLocation3DTest {
    static MersenneTwisterFast randomDoubleZero;
    static MersenneTwisterFast randomDoubleOne;
    static ArrayList<Voxel> voxelListA;
    static ArrayList<Voxel> voxelListB;
    static ArrayList<Voxel> voxelListC;
    static ArrayList<Voxel> voxelListAC;
    static ArrayList<Voxel> voxelListCA;
    static ArrayList<Voxel> voxelListBC;
    static ArrayList<Voxel> voxelListAB;
    
    @BeforeClass
    public static void setupMocks() {
        randomDoubleZero = mock(MersenneTwisterFast.class);
        when(randomDoubleZero.nextDouble()).thenReturn(0.0);
        
        randomDoubleOne = mock(MersenneTwisterFast.class);
        when(randomDoubleOne.nextDouble()).thenReturn(1.0);
    }
    
    @BeforeClass
    public static void setupLists() {
        /*
         * Lattice site shape:
         *     z = 0            z = 1            z = 2
         *     x x . .          x x x x          . . . .
         *     .   x .          x   x x          .   . x
         *     .     . x        x     x x        x     x .
         *
         * Each list is a subset of the shape:
         *
         *  (A)         (B)         (C)        (A) + (C)   (B) + (C)   (A) + (B)
         *  x x . .     . . . .     . . . .     x x . .     . . . .     x x . .
         *  .   . .     .   . .     .   x .     .   x .     .   x .     .   . .
         *  .     . .   .     . .   .     . x   .     . x   .     . x   .     . .
         *
         *  x x . .     . . x x     . . . .     x x . .     . . x x     x x x x
         *  x   . .     .   . x     .   x .     x   x .     .   x x     x   . x
         *  x     . .   .     . .   .     x x   x     x x   .     x x   x     . .
         *
         *  . . . .     . . . .     . . . .     . . . .     . . . .     . . . .
         *  .   . .     .   . x     .   . .     .   . .     .   . x     .   . x
         *  x     . .   .     x x   .     . .   x     . .   .     x x   x     x x
         */
        
        voxelListA = new ArrayList<>();
        voxelListA.add(new Voxel(0, 0, 1));
        voxelListA.add(new Voxel(0, 1, 1));
        voxelListA.add(new Voxel(1, 0, 1));
        voxelListA.add(new Voxel(0, 2, 1));
        voxelListA.add(new Voxel(0, 0, 0));
        voxelListA.add(new Voxel(1, 0, 0));
        voxelListA.add(new Voxel(0, 2, 2));
        
        voxelListB = new ArrayList<>();
        voxelListB.add(new Voxel(2, 0, 1));
        voxelListB.add(new Voxel(3, 0, 1));
        voxelListB.add(new Voxel(3, 1, 1));
        voxelListB.add(new Voxel(3, 1, 2));
        voxelListB.add(new Voxel(3, 2, 2));
        voxelListB.add(new Voxel(4, 2, 2));
        
        voxelListC = new ArrayList<>();
        voxelListC.add(new Voxel(2, 1, 1));
        voxelListC.add(new Voxel(3, 2, 1));
        voxelListC.add(new Voxel(4, 2, 1));
        voxelListC.add(new Voxel(2, 1, 0));
        voxelListC.add(new Voxel(4, 2, 0));
        
        voxelListAC = new ArrayList<>(voxelListA);
        voxelListAC.addAll(voxelListC);
        
        voxelListCA = new ArrayList<>(voxelListC);
        voxelListCA.addAll(voxelListA);
        
        voxelListBC = new ArrayList<>(voxelListB);
        voxelListBC.addAll(voxelListC);
        
        voxelListAB = new ArrayList<>(voxelListA);
        voxelListAB.addAll(voxelListB);
    }
    
    @Test
    public void makeLocation_givenList_createsObject() {
        ArrayList<Voxel> voxels = new ArrayList<>();
        voxels.add(new Voxel(0, 0, 0));
        PottsLocation3D oldLoc = new PottsLocation3D(new ArrayList<>());
        PottsLocation newLoc = oldLoc.makeLocation(voxels);
        
        assertTrue(newLoc instanceof PottsLocation3D);
        assertEquals(1, newLoc.voxels.size());
    }
    
    @Test
    public void checkVoxels_noVoxels_returnsNull() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxels = new ArrayList<>();
        assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
        assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
    }
    
    @Test
    public void checkVoxels_connectedVoxels_returnsNull() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxels = new ArrayList<>(voxelListA);
        assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
        assertNull(PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
    }
    
    @Test
    public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerVisited_returnsList() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
        assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
    }
    
    @Test
    public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerUnvisited_returnsList() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxels = new ArrayList<>(voxelListCA);
        ArrayList<Voxel> unvisited = new ArrayList<>();
        unvisited.add(voxelListC.get(0));
        unvisited.add(voxelListC.get(3));
        assertEquals(unvisited, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, false));
    }
    
    @Test
    public void checkVoxels_unconnectedVoxelsWithUpdateLargerVisited_updatesList() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxels = new ArrayList<>(voxelListAC);
        assertEquals(voxelListC, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
        assertEquals(voxelListA, voxels);
    }
    
    @Test
    public void checkVoxels_unconnectedVoxelsWithUpdateLargerUnvisited_updatesList() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxels = new ArrayList<>(voxelListCA);
        ArrayList<Voxel> unvisited = new ArrayList<>();
        unvisited.add(voxelListC.get(0));
        unvisited.add(voxelListC.get(3));
        
        ArrayList<Voxel> visited = new ArrayList<>(voxelListCA);
        visited.remove(voxelListC.get(0));
        visited.remove(voxelListC.get(3));
        
        assertEquals(unvisited, PottsLocation.checkVoxels(voxels, loc, randomDoubleZero, true));
        assertEquals(visited, voxels);
    }
    
    @Test
    public void connectVoxels_bothListsConnected_doesNothing() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListA);
        ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
        PottsLocation.connectVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
        assertEquals(voxelListA, voxelsA);
        assertEquals(voxelListB, voxelsB);
    }
    
    @Test
    public void connectVoxels_oneListUnconnected_updatesLists() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        
        ArrayList<Voxel> voxelsA1 = new ArrayList<>(voxelListAC);
        ArrayList<Voxel> voxelsB1 = new ArrayList<>(voxelListB);
        PottsLocation.connectVoxels(voxelsA1, voxelsB1, loc, randomDoubleZero);
        assertEquals(voxelListA, voxelsA1);
        assertEquals(voxelListBC, voxelsB1);
        
        ArrayList<Voxel> voxelsA2 = new ArrayList<>(voxelListAC);
        ArrayList<Voxel> voxelsB2 = new ArrayList<>(voxelListB);
        PottsLocation.connectVoxels(voxelsB2, voxelsA2, loc, randomDoubleZero);
        assertEquals(voxelListA, voxelsA2);
        assertEquals(voxelListBC, voxelsB2);
    }
    
    @Test
    public void balanceVoxels_balancedLists_doesNothing() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListA);
        ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListB);
        PottsLocation.balanceVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
        assertEquals(voxelListA, voxelsA);
        assertEquals(voxelListB, voxelsB);
    }
    
    @Test
    public void balanceVoxels_unbalancedLists_updatesLists() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxelsA = new ArrayList<>(voxelListAB);
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        voxelsA.remove(new Voxel(3, 2, 2));
        voxelsB.add(new Voxel(3, 2, 2));
        
        PottsLocation.balanceVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
        
        voxelListA.sort(VOXEL_COMPARATOR);
        voxelListB.sort(VOXEL_COMPARATOR);
        voxelsA.sort(VOXEL_COMPARATOR);
        voxelsB.sort(VOXEL_COMPARATOR);
        
        assertEquals(voxelListA, voxelsA);
        assertEquals(voxelListB, voxelsB);
    }
    
    @Test
    public void balanceVoxels_unconnectedLists_updatesLists() {
        PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>(voxelListAC);
        
        voxelsB.add(new Voxel(1, 1, 1));
        voxelsB.add(new Voxel(3, 1, 1));
        voxelsB.remove(new Voxel(1, 0, 1));
        voxelsA.add(new Voxel(1, 0, 1));
        
        PottsLocation.balanceVoxels(voxelsA, voxelsB, loc, randomDoubleZero);
        
        ArrayList<Voxel> voxelListAX = new ArrayList<>(voxelListA);
        ArrayList<Voxel> voxelListCX = new ArrayList<>(voxelListC);
        voxelListAX.add(new Voxel(1, 1, 1));
        voxelListCX.add(new Voxel(3, 1, 1));
        
        voxelListAX.sort(VOXEL_COMPARATOR);
        voxelListCX.sort(VOXEL_COMPARATOR);
        voxelsA.sort(VOXEL_COMPARATOR);
        voxelsB.sort(VOXEL_COMPARATOR);
        
        assertEquals(voxelListAX, voxelsA);
        assertEquals(voxelListCX, voxelsB);
    }
    
    @Test
    public void split_balanceableLocationRandomZero_returnsList() {
        PottsLocation3D loc = new PottsLocation3D(voxelListAB);
        PottsLocation3D split = (PottsLocation3D) loc.split(randomDoubleZero);
        
        ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListA);
        ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListB);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
    
    @Test
    public void split_balanceableLocationRandomOne_returnsList() {
        PottsLocation3D loc = new PottsLocation3D(voxelListAB);
        PottsLocation3D split = (PottsLocation3D) loc.split(randomDoubleOne);
        
        ArrayList<Voxel> locVoxels = new ArrayList<>(voxelListB);
        ArrayList<Voxel> splitVoxels = new ArrayList<>(voxelListA);
        
        locVoxels.sort(VOXEL_COMPARATOR);
        loc.voxels.sort(VOXEL_COMPARATOR);
        splitVoxels.sort(VOXEL_COMPARATOR);
        split.voxels.sort(VOXEL_COMPARATOR);
        
        assertEquals(locVoxels, loc.voxels);
        assertEquals(splitVoxels, split.voxels);
    }
}
