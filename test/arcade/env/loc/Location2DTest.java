package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import arcade.env.loc.Location.Voxel;
import static arcade.env.loc.PottsLocation.*;
import static arcade.env.loc.LocationTest.*;

public class Location2DTest {
	ArrayList<Voxel> voxelListForDiameters;
	
	@Before
	public void setupLists() {
		voxelListForDiameters = new ArrayList<>();
		voxelListForDiameters.add(new Voxel(5, 3, 0));
		voxelListForDiameters.add(new Voxel(6, 3, 0));
		voxelListForDiameters.add(new Voxel(7, 3, 0));
		voxelListForDiameters.add(new Voxel(5, 4, 0));
		voxelListForDiameters.add(new Voxel(6, 4, 0));
		voxelListForDiameters.add(new Voxel(7, 4, 0));
		voxelListForDiameters.add(new Voxel(5, 5, 0));
		voxelListForDiameters.add(new Voxel(7, 5, 0));
		voxelListForDiameters.add(new Voxel(8, 5, 0));
		voxelListForDiameters.add(new Voxel(8, 6, 0));
	}
	
	@Test
	public void getNeighbors_givenVoxel_returnsList() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(-1, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(0, -1, 0));
		voxels.add(new Voxel(0, 1, 0));
		
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));
		
		voxels.sort(COMPARATOR);
		neighbors.sort(COMPARATOR);
		
		assertEquals(voxels, neighbors);
	}
	
	@Test
	public void getDiameters_validLocation_calculatesValues() {
		PottsLocation2D loc = new PottsLocation2D(voxelListForDiameters);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(2, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_XY));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_XY));
	}
	
	@Test
	public void calculateSurface_validID_calculatesValue() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		PottsLocation2D loc = new PottsLocation2D(voxels);
		
		// 1 voxel
		loc.add(1, 1, 0);
		assertEquals(4, loc.calculateSurface());
		
		// 2 voxels
		loc.add(2, 1, 0);
		assertEquals(6, loc.calculateSurface());
		
		// 3 voxels
		loc.add(1, 2, 0);
		assertEquals(8, loc.calculateSurface());
		
		// 4 voxels
		loc.add(2, 2, 0);
		assertEquals(8, loc.calculateSurface());
	}
	
	@Test
	public void updateSurface_validVoxels_calculatesValue() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		PottsLocation2D loc = new PottsLocation2D(voxels);
		Voxel voxel = new Voxel(1, 1, 0);
		
		// 0 voxels
		voxels.clear();
		loc.add(1, 1, 0);
		assertEquals(4, loc.updateSurface(voxel));
		
		// 1 voxel
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		loc = new PottsLocation2D(voxels);
		loc.add(1, 1, 0);
		assertEquals(2, loc.updateSurface(voxel));
		
		// 2 voxels
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		loc = new PottsLocation2D(voxels);
		loc.add(1, 1, 0);
		assertEquals(0, loc.updateSurface(voxel));
		
		// 3 voxels
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(2, 1, 0));
		loc = new PottsLocation2D(voxels);
		loc.add(1, 1, 0);
		assertEquals(-2, loc.updateSurface(voxel));
		
		// 4 voxels
		voxels.clear();
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(2, 1, 0));
		voxels.add(new Voxel(1, 2, 0));
		loc = new PottsLocation2D(voxels);
		loc.add(1, 1, 0);
		assertEquals(-4, loc.updateSurface(voxel));
	}
	
	@Test
	public void getSlice_givenDirection_returnsValue() {
		PottsLocation2D loc = new PottsLocation2D(new ArrayList<>());
		assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.ZX_PLANE, null));
		assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.YZ_PLANE, null));
		assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, null));
		assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, null));
	}
}
