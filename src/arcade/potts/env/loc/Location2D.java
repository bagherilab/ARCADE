package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashMap;
import static arcade.potts.sim.Potts2D.*;
import static arcade.potts.env.loc.PottsLocation.Direction;

public interface Location2D {
	Direction[] DIRECTIONS = new Direction[] {
			Direction.YZ_PLANE,
			Direction.ZX_PLANE,
			Direction.POSITIVE_XY,
			Direction.NEGATIVE_XY
	};
		
	static ArrayList<Voxel> getNeighbors(Voxel voxel) {
		ArrayList<Voxel> neighbors = new ArrayList<>();
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			neighbors.add(new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z));
		}
		return neighbors;
	}
	
	static int calculateSurface(ArrayList<Voxel> voxels) {
		int surface = 0;
		
		for (Voxel voxel : voxels) {
			for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
				if (!voxels.contains(new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z))) {
					surface++;
				}
			}
		}
		
		return surface;
	}
	
	static int updateSurface(ArrayList<Voxel> voxels, Voxel voxel) {
		int change = 0;
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			if (!voxels.contains(new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z))) {
				change++;
			} else { change--; }
		}
		
		return change;
	}
	
	static HashMap<Direction, Integer> getDiameters(ArrayList<Voxel> voxels, Voxel center) {
		HashMap<Direction, Integer> minValueMap = new HashMap<>();
		HashMap<Direction, Integer> maxValueMap = new HashMap<>();
		HashMap<Direction, Boolean> existsMap = new HashMap<>();
		
		// Initialized entries into direction maps.
		for (Direction direction : DIRECTIONS) {
			minValueMap.put(direction, Integer.MAX_VALUE);
			maxValueMap.put(direction, Integer.MIN_VALUE);
			existsMap.put(direction, false);
		}
		
		Direction dir;
		int v;
		
		// Iterate through all the voxels for the location to update minimum and
		// maximum values in each direction.
		for (Voxel voxel : voxels) {
			int i = voxel.x - center.x;
			int j = voxel.y - center.y;
			
			// Need to update all directions if at the center.
			if (i == 0 && j == 0) {
				v = 0;
				
				for (Direction direction : DIRECTIONS) {
					existsMap.put(direction, true);
					if (v > maxValueMap.get(direction)) { maxValueMap.put(direction, v); }
					if (v < minValueMap.get(direction)) { minValueMap.put(direction, v); }
				}
				
				continue;
			}
			else if (j == 0) { dir = Direction.YZ_PLANE; v = i; }
			else if (i == 0) { dir = Direction.ZX_PLANE; v = j; }
			else if (i == j) { dir = Direction.POSITIVE_XY; v = i; }
			else if (i == -j) { dir = Direction.NEGATIVE_XY; v = i; }
			else { continue; }
			
			existsMap.put(dir, true);
			if (v > maxValueMap.get(dir)) { maxValueMap.put(dir, v); }
			if (v < minValueMap.get(dir)) { minValueMap.put(dir, v); }
		}
		
		HashMap<Direction, Integer> diameterMap = new HashMap<>();
		
		// Calculate diameter in each direction.
		for (Direction direction : DIRECTIONS) {
			int diameter = maxValueMap.get(direction) - minValueMap.get(direction) + 1;
			diameterMap.put(direction, existsMap.get(direction) ? diameter : 0);
		}
		
		return diameterMap;
	}
	
	static Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
		switch (direction) {
			case YZ_PLANE: return Direction.ZX_PLANE;
			case ZX_PLANE: return Direction.YZ_PLANE;
			case POSITIVE_XY: return Direction.NEGATIVE_XY;
			case NEGATIVE_XY: return Direction.POSITIVE_XY;
			default: return null;
		}
	}
	
	static ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n) {
		ArrayList<Voxel> selected = new ArrayList<>();
		double r = Math.sqrt(n/Math.PI);
		
		// Select voxels within given radius.
		for (Voxel voxel : voxels) {
			double d = Math.sqrt(Math.pow(focus.x - voxel.x, 2) + Math.pow(focus.y - voxel.y, 2));
			if (d < r) { selected.add(voxel); }
		}
		
		return selected;
	}
}