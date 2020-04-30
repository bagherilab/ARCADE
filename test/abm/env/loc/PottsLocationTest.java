package abm.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.ArrayList;
import abm.env.loc.Location.Voxel;

public class PottsLocationTest {
	@Test
	public void testAdd() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		PottsLocation loc = new PottsLocation(new ArrayList<>());
		
		// Addition of two locations.
		loc.add(0, 0, 0);
		loc.add(1, 0, 0);
		assertEquals(voxels, loc.voxels);
		
		// Addition of existing location doesn't change list.
		loc.add(0, 0, 0);
		assertEquals(voxels, loc.voxels);
	}
	
	@Test
	public void testRemove() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		PottsLocation loc = new PottsLocation(voxels);
		
		ArrayList<Location.Voxel> voxelsRemoved = new ArrayList<>();
		voxelsRemoved.add(new Voxel(1, 0, 0));
		
		// Removal of one location.
		loc.remove(0, 0, 0);
		assertEquals(voxelsRemoved, loc.voxels);
		
		// Removal of both locations
		loc.remove(1, 0, 0);
		assertEquals(new ArrayList<>(), loc.voxels);
	}
	
	@Test
	public void testUpdate() {
		int[][][] array = new int[][][] { { { 0, 1, 2 } } };
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(0, 1, 0));
		PottsLocation loc = new PottsLocation(voxels);
		
		loc.update(array, 3);
		assertArrayEquals(new int[] { 0, 3, 2 }, array[0][0]);
	}
}
