package arcade.potts.sim;

import java.util.HashSet;
import arcade.potts.agent.cell.PottsCell;
import static arcade.core.agent.cell.Cell.Region;

public class Potts2D extends Potts {
	/** Number of neighbors */
	public static final int NUMBER_NEIGHBORS = 4;
	
	/** List of x direction movements (N, E, S, W) */
	public static final int[] MOVES_X = { 0, 1, 0, -1 };
	
	/** List of y direction movements (N, E, S, W) */
	public static final int[] MOVES_Y = { -1, 0, 1, 0 };
	
	/** List of x direction corner movements (NE, SE, SW, NW) */
	private static final int[] CORNER_X = { 1, 1, -1, -1 };
	
	/** List of y direction corner movements (NE, SE, SW, NW) */
	private static final int[] CORNER_Y = { -1, 1, 1, -1 };
	
	/**
	 * Creates a cellular {@code Potts} model in 2D.
	 * 
	 * @param series  the simulation series
	 */
	public Potts2D(PottsSeries series) { super(series); }
	
	double getAdhesion(int id, int x, int y, int z) {
		double H = 0;
		PottsCell a = getCell(id);
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (!(i == x && j == y) && IDS[z][i][j] != id) {
					PottsCell b = getCell(IDS[z][i][j]);
					if (a == null) { H += b.getAdhesion(0); }
					else if (b == null) { H += a.getAdhesion(0); }
					else { H += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop()))/2.0; }
				}
			}
		}
		
		return H;
	}
	
	double getAdhesion(int id, int t, int x, int y, int z) {
		double H = 0;
		PottsCell c = getCell(id);
		Region region = Region.values()[t];
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				Region regionxy = Region.values()[REGIONS[z][i][j]];
				if (!(i == x && j == y) && IDS[z][i][j] == id
						&& regionxy != region && regionxy != Region.UNDEFINED && regionxy != Region.DEFAULT) {
					H += (c.getAdhesion(region, regionxy) + c.getAdhesion(regionxy, region))/2;
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
	
	int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighborID = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			int neighborRegion = REGIONS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighborRegion != sourceRegion || neighborID != id) {
				beforeSource++;
				if (neighborRegion == targetRegion && neighborID == id) { beforeTarget++; }
			}
			
			if (neighborRegion != targetRegion || neighborID != id) {
				afterTarget++;
				if (neighborRegion == sourceRegion && neighborID == id) { afterSource++; }
			}
		}
		
		// Save changes to surface.
		int sourceSurfaceChange = afterSource - beforeSource;
		int targetSurfaceChange = afterTarget - beforeTarget;
		
		return new int[] { sourceSurfaceChange, targetSurfaceChange };
	}
	
	boolean[][][] getNeighborhood(int id, int x, int y, int z) {
		boolean[][] array = new boolean[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				array[i][j] = IDS[0][i + x - 1][j + y - 1] == id;
			}
		}
		return new boolean[][][] { array };
	}
	
	boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
		boolean[][] array = new boolean[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				array[i][j] = IDS[0][i + x - 1][j + y - 1] == id && REGIONS[0][i + x - 1][j + y - 1] == region;
			}
		}
		return new boolean[][][] { array };
	}
	
	boolean getConnectivity(boolean[][][] array, boolean zero) {
		boolean[][] _array = array[0];
		int links = 0;
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			if (_array[1 + MOVES_X[i]][1 + MOVES_Y[i]]) { links++; }
		}
		
		switch (links) {
			case 0: return false;
			case 1: return true;
			case 2:
				// Check for opposites N/S
				if (_array[1][2] && _array[1][0]) { return false; }
				// Check for opposites E/W
				else if (_array[2][1] && _array[0][1]) { return false; }
				// Check for corners
				else {
					for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
						if (_array[1 + MOVES_X[i]][1 + MOVES_Y[i]]
								&& _array[1 + MOVES_X[(i + 1)%NUMBER_NEIGHBORS] ][1 + MOVES_Y[(i + 1)%NUMBER_NEIGHBORS]]
								&& _array[1 + CORNER_X[i]][1 + CORNER_Y[i]]) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
					if (!_array[1 + MOVES_X[i]][1 + MOVES_Y[i]]) {
						if (_array[1 + CORNER_X[(i + 1)%NUMBER_NEIGHBORS]][1 + CORNER_Y[(i + 1)%NUMBER_NEIGHBORS]]
								&& _array[1 + CORNER_X[(i + 2)%NUMBER_NEIGHBORS]][1 + CORNER_Y[(i + 2)%NUMBER_NEIGHBORS]]) {
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
	
	HashSet<Integer> getUniqueRegions(int x, int y, int z) {
		int id = IDS[z][x][y];
		int region = REGIONS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighborID = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]]; 
			int neighborRegion = REGIONS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighborID != id) { continue; }
			if (region != neighborRegion) { unique.add(neighborRegion); }
		}
		
		return unique;
	}
}