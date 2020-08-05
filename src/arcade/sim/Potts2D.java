package arcade.sim;

import java.util.HashSet;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

public class Potts2D extends Potts {
	/** Number of neighbors */
	public static final int NUMBER_NEIGHBORS = 4;
	
	/** List of x direction movements (N, E, S, W) */
	public static final int[] MOVES_X = { 0, 1, 0, -1 };
	
	/** List of y direction movements (N, E, S, W) */
	public static final int[] MOVES_Y = { -1, 0, 1, 0 };
	
	/** List of x direction corner movements (NE, SE, SW, NW) */
	public static final int[] MOVES_CORNER_X = { 1, 1, -1, -1 };
	
	/** List of y direction corner movements (NE, SE, SW, NW) */
	public static final int[] MOVES_CORNER_Y = { -1, 1, 1, -1 };
	
	/**
	 * Creates a cellular {@code Potts} model in 2D.
	 * 
	 * @param series  the simulation series
	 * @param grid  the cell grid
	 */
	public Potts2D(Series series, Grid grid) { super(series, grid); }
	
	double getAdhesion(int id, int x, int y, int z) {
		double H = 0;
		Cell a = getCell(id);
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (!(i == x && j == y) && IDS[z][i][j] != id) {
					Cell b = getCell(IDS[z][i][j]);
					if (a == null && b == null) { System.exit(-1); }
					else if (a == null) { H += b.getAdhesion(0); }
					else if (b == null) { H += a.getAdhesion(0); }
					else { H += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop()))/2.0; }
				}
			}
		}
		
		return H;
	}
	
	double getAdhesion(int id, int tag, int x, int y, int z) {
		double H = 0;
		Cell c = getCell(id);
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (!(i == x && j == y) && IDS[z][i][j] == id
						&& TAGS[z][i][j] != tag && TAGS[z][i][j] != 0) {
					H += (c.getAdhesion(tag, TAGS[z][i][j]) + c.getAdhesion(TAGS[z][i][j], tag))/2;
				}
			}
		}
		
		return H;
	}
	
	int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighbor = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighbor != sourceID) {
				beforeSource++;
				if (neighbor == targetID) { beforeTarget++; }
			}
			
			if (neighbor != targetID) {
				afterTarget++;
				if (neighbor == sourceID) { afterSource++; }
			}
		}
		
		// Save changes to surface.
		int sourceSurfaceChange = afterSource - beforeSource;
		int targetSurfaceChange = afterTarget - beforeTarget;
		
		return new int[] { sourceSurfaceChange, targetSurfaceChange };
	}
	
	int[] calculateChange(int id, int sourceTag, int targetTag, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighborID = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			int neighborTag = TAGS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighborTag != sourceTag || neighborID != id) {
				beforeSource++;
				if (neighborTag == targetTag && neighborID == id) { beforeTarget++; }
			}
			
			if (neighborTag != targetTag || neighborID != id) {
				afterTarget++;
				if (neighborTag == sourceTag && neighborID == id) { afterSource++; }
			}
		}
		
		// Save changes to surface.
		int sourceSurfaceChange = afterSource - beforeSource;
		int targetSurfaceChange = afterTarget - beforeTarget;
		
		return new int[] { sourceSurfaceChange, targetSurfaceChange };
	}
	
	boolean[][][] getNeighborhood(int id, int x, int y, int z) {
		boolean[][][] array = new boolean[1][3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				array[0][i][j] = IDS[z][i + x - 1][j + y - 1] == id;
			}
		}
		return array;
	}
	
	boolean[][][] getNeighborhood(int id, int tag, int x, int y, int z) {
		boolean[][][] array = new boolean[1][3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				array[0][i][j] = IDS[z][i + x - 1][j + y - 1] == id && TAGS[z][i + x - 1][j + y - 1] == tag;
			}
		}
		return array;
	}
	
	boolean getConnectivity(boolean[][][] array, boolean zero) {
		int sites = 0;
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			if (array[0][1 + MOVES_X[i]][1 + MOVES_Y[i]]) { sites++; }
		}
		
		switch (sites) {
			case 0: return false;
			case 1: return true;
			case 2:
				// Check for opposites N/S
				if (array[0][2][1] && array[0][0][1]) { return false; }
				// Check for opposites E/W
				else if (array[0][1][2] && array[0][1][0]) { return false; }
				// Check for corners
				else {
					for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
						if (array[0][1 + MOVES_X[i]][1 + MOVES_Y[i]]
								&& array[0][1 + MOVES_X[(i + 1)%NUMBER_NEIGHBORS] ][1 + MOVES_Y[(i + 1)%NUMBER_NEIGHBORS]]
								&& array[0][1 + MOVES_CORNER_X[i]][1 + MOVES_CORNER_Y[i]]) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
					if (!array[0][1 + MOVES_X[i]][1 + MOVES_Y[i]]) {
						if (array[0][1 + MOVES_CORNER_X[(i + 1)%NUMBER_NEIGHBORS]][1 + MOVES_CORNER_Y[(i + 1)%NUMBER_NEIGHBORS]]
								&& array[0][1 + MOVES_CORNER_X[(i + 2)%NUMBER_NEIGHBORS]][1 + MOVES_CORNER_Y[(i + 2)%NUMBER_NEIGHBORS]]) {
							return true;
						}
					}
				}
				return false;
			case 4:
				return zero;
		}
		
		return false;
	}
	
	HashSet<Integer> getUniqueIDs(int x, int y, int z) {
		int id = IDS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighbor = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			if (id != neighbor) { unique.add(neighbor); }
		}
		return unique;
	}
	
	HashSet<Integer> getUniqueTags(int x, int y, int z) {
		int id = IDS[z][x][y];
		int tag = TAGS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighborID = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]]; 
			int neighborTag = TAGS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighborID != id) { continue; }
			if (tag != neighborTag) { unique.add(neighborTag); }
		}
		
		return unique;
	}
}