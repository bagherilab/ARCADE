package abm.env.loc;

import abm.sim.PottsSimulation;
import abm.sim.Simulation;
import ec.util.MersenneTwisterFast;

import javax.sql.PooledConnection;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PottsLocation {
	final static private int[] MOVES_X = { 0, 1, 0, -1 }; // N, E, S, W
	final static private int[] MOVES_Y = { -1, 0, 1, 0 }; // N, E, S, W
	
	public ArrayList<PottsCoordinate> coordinates;
	
	public PottsLocation(ArrayList<PottsCoordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	public void removeCoord(int x, int y, int z) {
		coordinates.remove(new PottsCoordinate(x, y, z));
	}
	
	public void addCoord(int x, int  y, int z) {
		coordinates.add(new PottsCoordinate(x, y, z));
	}
	
	public void addCoords(ArrayList<PottsCoordinate> coords) { coordinates.addAll(coords); }
	
	
	public void update(int[][][] array, int id) {
		for (PottsCoordinate coord : coordinates) {
			array[coord.z][coord.x][coord.y] = id;
		}
	}
	
	private int getXCenter() {
		double x = 0;
		for (PottsCoordinate coord : coordinates) { x += coord.x; }
		return (int)Math.round(x/coordinates.size());
	}
	
	private int getYCenter() {
		double y = 0;
		for (PottsCoordinate coord : coordinates) { y += coord.y; }
		return (int)Math.round(y/coordinates.size());
	}
	
	private int getZCenter() {
		double z = 0;
		for (PottsCoordinate coord : coordinates) { z += coord.z; }
		return (int)Math.round(z/coordinates.size());
	}
	
	private PottsCoordinate getCenter() {
		return new PottsCoordinate(getXCenter(), getYCenter(), getZCenter());
	}
	
	private int getDiameter(Direction dir) {
		PottsCoordinate center = getCenter();
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		
		for (PottsCoordinate coord : coordinates) {
			int x = coord.x - center.x;
			int y = coord.y - center.y;
			
			switch (dir) {
				case X_DIRECTION:
					if (x < min) { min = x; }
					if (x > max) { max = x; }
					break;
				case Y_DIRECTION:
					if (y < min) { min = y; }
					if (y > max) { max = y; }
					break;
				case POSITIVE_XY:
					if (x == y) {
						if (x < min) { min = x; }
						if (x > max) { max = x; }
					}
					break;
				case NEGATIVE_XY:
					if (x == -y) {
						if (x < min) { min = x; }
						if (x > max) { max = x; }
					}
					break;
			}
			
		}
		
		return max - min;
	}
	
	enum Direction { X_DIRECTION, Y_DIRECTION, POSITIVE_XY, NEGATIVE_XY }
	
	public PottsLocation splitCoord(Simulation sim) {
		// Get center voxel.
		PottsCoordinate center = getCenter();
		
		// Get diameters.
		Direction shortestDirection = Direction.X_DIRECTION;
//		int shortestDiameter = Integer.MAX_VALUE;
//		
//		for (Direction direction : Direction.values()) {
//			int diameter = getDiameter(direction);
//			if (diameter < shortestDiameter) {
//				shortestDiameter = diameter;
//				shortestDirection = direction;
//			}
//		}
		
		ArrayList<PottsCoordinate> coordinatesA = new ArrayList<>();
		ArrayList<PottsCoordinate> coordinatesB = new ArrayList<>();
				
		for (PottsCoordinate coordinate : coordinates) {
			if (coordinate.x < center.x) {
				coordinatesA.add(coordinate);
			}
			else if (coordinate.x > center.x) {
				coordinatesB.add(coordinate);
			}
			else {
				coordinatesA.add(coordinate);
				
			}
		}
		
		// Check that both coordinate sets are simply connected.
		
		
		
		
		if (sim.getRandom() < 0.5) {
			coordinates = coordinatesA;

			return new PottsLocation(coordinatesB);
		} else {
			coordinates = coordinatesB;
			return new PottsLocation(coordinatesA);
		}
		// 
		// 

		
		
		
		
//		return null;
	}
	
	public int calculatePerimeter(int id, int[][][] array) {
		int perimeter = 0;
		
		for (PottsCoordinate coordinate : coordinates) {
			for (int i = 0; i < 4; i++) {
				if (array[coordinate.z][coordinate.x+ MOVES_X[i]][coordinate.y + MOVES_Y[i]] != id) { perimeter++; }
			}
		}
		
		return perimeter;
	}
	
//	// Count number of sites in adjacency neighborhood.
//	int sites = 0;
//				for (int i = 0; i < 4; i++) {
//		if (voxels[z][x + MOVES_X[i]][y + MOVES_Y[i]] == id) { sites++; }
//	}
//	
				
	public static ArrayList<PottsCoordinate> removeUnconnected(PottsLocation location, int[][][] array, int id) {
		ArrayList<PottsCoordinate> connected = new ArrayList<>();
		ArrayList<PottsCoordinate> unconnected = new ArrayList<>();
		
		for (PottsCoordinate coordinate : location.coordinates) {
			int sites = 0;
			for (int i = 0; i < 4; i++) {
				if (array[coordinate.z][coordinate.x+ MOVES_X[i]][coordinate.y + MOVES_Y[i]] == id) { sites++; }
			}
			
			if (sites == 0) { unconnected.add(coordinate); }
			else { connected.add(coordinate); }
		}
		
		location.coordinates.removeAll(unconnected);
		
		return unconnected;
	}
	
	
	public static class PottsCoordinate {
		public int x, y, z;
		public PottsCoordinate(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public final boolean equals(Object obj) {
			PottsCoordinate coord = (PottsCoordinate)obj;
			return coord.x == x && coord.y == y && coord.z == z;
		}
	}
}