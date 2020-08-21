package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import arcade.env.loc.Location.Voxel;
import static arcade.env.loc.PottsLocation.*;
import static arcade.env.loc.LocationTest.*;

public class Location3DTest {
	ArrayList<Voxel> voxelListForDiametersXY, voxelListForDiametersYZ, voxelListForDiametersZX;
	
	@Before
	public void setupLists() {
		int[][] diameter = new int[][] {
				{ 5, 6, 7, 5, 6, 7, 5, 7, 8, 8, 5, 6, 7, 5, 6, 7 },
				{ 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 2, 2, 2 },
				{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 2, 2, 2 }
		};
		
		voxelListForDiametersXY = new ArrayList<>();
		voxelListForDiametersYZ = new ArrayList<>();
		voxelListForDiametersZX = new ArrayList<>();
		
		for (int i = 0; i < diameter[0].length; i++) {
			voxelListForDiametersXY.add(new Voxel(diameter[0][i], diameter[1][i], diameter[2][i]));
			voxelListForDiametersYZ.add(new Voxel(diameter[2][i], diameter[0][i], diameter[1][i]));
			voxelListForDiametersZX.add(new Voxel(diameter[1][i], diameter[2][i], diameter[0][i]));
		}
	}
	
	@Test
	public void getNeighbors_givenVoxel_returnsList() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		voxels.add(new Voxel(-1, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(0, -1, 0));
		voxels.add(new Voxel(0, 1, 0));
		voxels.add(new Voxel(0, 0, -1));
		voxels.add(new Voxel(0, 0, 1));
		
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		ArrayList<Voxel> neighbors = loc.getNeighbors(new Voxel(0, 0, 0));
		
		voxels.sort(COMPARATOR);
		neighbors.sort(COMPARATOR);
		
		assertEquals(voxels, neighbors);
	}
	
	@Test
	public void getDiameters_validLocationXY_calculatesValues() {
		PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersXY);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(2, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_XY));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_XY));
	}
	
	@Test
	public void getDiameters_validLocationYZ_calculatesValues() {
		PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersYZ);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(2, (int)diameters.get(Direction.XY_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_YZ));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_YZ));
	}
	
	@Test
	public void getDiameters_validLocationZX_calculatesValues() {
		PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersZX);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.XY_PLANE));
		assertEquals(2, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_ZX));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_ZX));
	}
	
	@Test
	public void calculateSurface_validID_calculatesValue() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		PottsLocation3D loc = new PottsLocation3D(voxels);
		
		// 1 voxel
		loc.add(1, 1, 0);
		assertEquals(6, loc.calculateSurface());
		
		// 2 voxels
		loc.add(1, 1, 1);
		assertEquals(10, loc.calculateSurface());
		
		// 3 voxels
		loc.add(1, 2, 0);
		assertEquals(14, loc.calculateSurface());
		
		// 4 voxels
		loc.add(2, 1, 1);
		assertEquals(18, loc.calculateSurface());
	}
	
	@Test
	public void updateSurface_validVoxels_calculatesValue() {
		ArrayList<Voxel> voxels = new ArrayList<>();
		PottsLocation3D loc = new PottsLocation3D(voxels);
		Voxel voxel = new Voxel(1, 1, 0);
		
		// 0 voxels
		voxels.clear();
		loc.add(1, 1, 0);
		assertEquals(6, loc.updateSurface(voxel));
		
		// 1 voxel
		voxels.clear();
		voxels.add(new Voxel(1, 1, 1));
		loc = new PottsLocation3D(voxels);
		loc.add(1, 1, 0);
		assertEquals(4, loc.updateSurface(voxel));
		
		// 2 voxels
		voxels.clear();
		voxels.add(new Voxel(1, 1, 1));
		voxels.add(new Voxel(1, 0, 1));
		loc = new PottsLocation3D(voxels);
		loc.add(1, 1, 0);
		assertEquals(4, loc.updateSurface(voxel));
		
		// 3 voxels
		voxels.clear();
		voxels.add(new Voxel(1, 1, 1));
		voxels.add(new Voxel(1, 0, 1));
		voxels.add(new Voxel(1, 0, 0));
		loc = new PottsLocation3D(voxels);
		loc.add(1, 1, 0);
		assertEquals(2, loc.updateSurface(voxel));
		
		// 4 voxels
		voxels.clear();
		voxels.add(new Voxel(1, 1, 1));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(1, 0, 0));
		voxels.add(new Voxel(1, 2, 0));
		loc = new PottsLocation3D(voxels);
		loc.add(1, 1, 0);
		assertEquals(0, loc.updateSurface(voxel));
	}
}
