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
	public void getDiameters_validVoxelsXY_calculatesValues() {
		PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersXY);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(2, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_XY));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_XY));
	}
	
	@Test
	public void getDiameters_invalidVoxelsXY_returnsZero() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(0, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(0, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(0, (int)diameters.get(Direction.POSITIVE_XY));
		assertEquals(0, (int)diameters.get(Direction.NEGATIVE_XY));
	}
	
	@Test
	public void getDiameters_validVoxelsYZ_calculatesValues() {
		PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersYZ);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(2, (int)diameters.get(Direction.XY_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_YZ));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_YZ));
	}
	
	@Test
	public void getDiameters_invalidVoxelsYZ_returnsZero() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(0, (int)diameters.get(Direction.ZX_PLANE));
		assertEquals(0, (int)diameters.get(Direction.XY_PLANE));
		assertEquals(0, (int)diameters.get(Direction.POSITIVE_YZ));
		assertEquals(0, (int)diameters.get(Direction.NEGATIVE_YZ));
	}
	
	@Test
	public void getDiameters_validVoxelsZX_calculatesValues() {
		PottsLocation3D loc = new PottsLocation3D(voxelListForDiametersZX);
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(3, (int)diameters.get(Direction.XY_PLANE));
		assertEquals(2, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(4, (int)diameters.get(Direction.POSITIVE_ZX));
		assertEquals(3, (int)diameters.get(Direction.NEGATIVE_ZX));
	}
	
	@Test
	public void getDiameters_invalidVoxelsZX_returnsZero() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		HashMap<Direction, Integer> diameters = loc.getDiameters();
		assertEquals(0, (int)diameters.get(Direction.XY_PLANE));
		assertEquals(0, (int)diameters.get(Direction.YZ_PLANE));
		assertEquals(0, (int)diameters.get(Direction.POSITIVE_ZX));
		assertEquals(0, (int)diameters.get(Direction.NEGATIVE_ZX));
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
	
	@Test
	public void getSlice_YZPlaneDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.ZX_PLANE, 1);
		diametersA.put(Direction.XY_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.XY_PLANE, 1);
		diametersB.put(Direction.ZX_PLANE, 2);
		
		assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.YZ_PLANE, diametersA));
		assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.YZ_PLANE, diametersB));
	}
	
	@Test
	public void getSlice_ZXPlaneDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.XY_PLANE, 1);
		diametersA.put(Direction.YZ_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.YZ_PLANE, 1);
		diametersB.put(Direction.XY_PLANE, 2);
		
		assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.ZX_PLANE, diametersA));
		assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.ZX_PLANE, diametersB));
	}
	
	@Test
	public void getSlice_XYPlaneDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.YZ_PLANE, 1);
		diametersA.put(Direction.ZX_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.ZX_PLANE, 1);
		diametersB.put(Direction.YZ_PLANE, 2);
		
		assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.XY_PLANE, diametersA));
		assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.XY_PLANE, diametersB));
	}
	
	@Test
	public void getSlice_positiveXYDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.NEGATIVE_XY, 1);
		diametersA.put(Direction.XY_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.XY_PLANE, 1);
		diametersB.put(Direction.NEGATIVE_XY, 2);
		
		assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.POSITIVE_XY, diametersA));
		assertEquals(Direction.NEGATIVE_XY, loc.getSlice(Direction.POSITIVE_XY, diametersB));
	}
	
	@Test
	public void getSlice_negativeXYDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.POSITIVE_XY, 1);
		diametersA.put(Direction.XY_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.XY_PLANE, 1);
		diametersB.put(Direction.POSITIVE_XY, 2);
		
		assertEquals(Direction.XY_PLANE, loc.getSlice(Direction.NEGATIVE_XY, diametersA));
		assertEquals(Direction.POSITIVE_XY, loc.getSlice(Direction.NEGATIVE_XY, diametersB));
	}
	
	@Test
	public void getSlice_positiveYZDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.NEGATIVE_YZ, 1);
		diametersA.put(Direction.YZ_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.YZ_PLANE, 1);
		diametersB.put(Direction.NEGATIVE_YZ, 2);
		
		assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.POSITIVE_YZ, diametersA));
		assertEquals(Direction.NEGATIVE_YZ, loc.getSlice(Direction.POSITIVE_YZ, diametersB));
	}
	
	@Test
	public void getSlice_negativeYZDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.POSITIVE_YZ, 1);
		diametersA.put(Direction.YZ_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.YZ_PLANE, 1);
		diametersB.put(Direction.POSITIVE_YZ, 2);
		
		assertEquals(Direction.YZ_PLANE, loc.getSlice(Direction.NEGATIVE_YZ, diametersA));
		assertEquals(Direction.POSITIVE_YZ, loc.getSlice(Direction.NEGATIVE_YZ, diametersB));
	}
	
	@Test
	public void getSlice_positiveZXDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.NEGATIVE_ZX, 1);
		diametersA.put(Direction.ZX_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.ZX_PLANE, 1);
		diametersB.put(Direction.NEGATIVE_ZX, 2);
		
		assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.POSITIVE_ZX, diametersA));
		assertEquals(Direction.NEGATIVE_ZX, loc.getSlice(Direction.POSITIVE_ZX, diametersB));
	}
	
	@Test
	public void getSlice_negativeZXDirection_returnsValue() {
		PottsLocation3D loc = new PottsLocation3D(new ArrayList<>());
		
		HashMap<Direction, Integer> diametersA = new HashMap<>();
		diametersA.put(Direction.POSITIVE_ZX, 1);
		diametersA.put(Direction.ZX_PLANE, 2);
		
		HashMap<Direction, Integer> diametersB = new HashMap<>();
		diametersB.put(Direction.ZX_PLANE, 1);
		diametersB.put(Direction.POSITIVE_ZX, 2);
		
		assertEquals(Direction.ZX_PLANE, loc.getSlice(Direction.NEGATIVE_ZX, diametersA));
		assertEquals(Direction.POSITIVE_ZX, loc.getSlice(Direction.NEGATIVE_ZX, diametersB));
	}
}
