package arcade.potts.sim;

import java.util.HashSet;
import arcade.potts.agent.cell.PottsCell;
import static arcade.core.agent.cell.Cell.Region;

public class Potts3D extends Potts {
	/** Number of neighbors */
	public static final int NUMBER_NEIGHBORS = 6;
	
	/** List of x direction movements (N, E, S, W, U, D) */
	public static final int[] MOVES_X = { 0, 1, 0, -1, 0, 0 };
	
	/** List of y direction movements (N, E, S, W, U, D) */
	public static final int[] MOVES_Y = { -1, 0, 1, 0, 0, 0 };
	
	/** List of z direction movements (N, E, S, W, U, D) */
	public static final int[] MOVES_Z = { 0, 0, 0, 0, 1, -1 };
	
	/** Number of neighbors in plane */
	private static final int NUMBER_PLANE = 4;
	
	/** List of plane movements for first coordinate */
	private static final int[] PLANE_A = { 0, 1, 0, -1 };
	
	/** List of plane movements for second coordinate */
	private static final int[] PLANE_B = { -1, 0, 1, 0 };
	
	/** List of a direction corner movements */
	private static final int[] CORNER_A = { 1, 1, -1, -1 };
	
	/** List of b direction corner movements */
	private static final int[] CORNER_B = { -1, 1, 1, -1 };
	
	/**
	 * Creates a cellular {@code Potts} model in 3D.
	 *
	 * @param series  the simulation series
	 */
	public Potts3D(PottsSeries series) { super(series); }
	
	double getAdhesion(int id, int x, int y, int z) {
		double H = 0;
		PottsCell a = getCell(id);
		
		for (int k = z - 1; k <= z + 1; k++) {
			for (int i = x - 1; i <= x + 1; i++) {
				for (int j = y - 1; j <= y + 1; j++) {
					if (!(k == z && i == x && j == y) && IDS[k][i][j] != id) {
						PottsCell b = getCell(IDS[k][i][j]);
						if (a == null) { H += b.getAdhesion(0); }
						else if (b == null) { H += a.getAdhesion(0); }
						else { H += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop()))/2.0; }
					}
				}
			}
		}
		
		return H;
	}
	
	double getAdhesion(int id, int t, int x, int y, int z) {
		double H = 0;
		PottsCell c = getCell(id);
		Region region = Region.values()[t];
		
		for (int k = z - 1; k <= z + 1; k++) {
			for (int i = x - 1; i <= x + 1; i++) {
				for (int j = y - 1; j <= y + 1; j++) {
					Region regionxyz = Region.values()[REGIONS[k][i][j]];
					if (!(k == z && i == x && j == y) && IDS[k][i][j] == id
							&& region != regionxyz && regionxyz != Region.UNDEFINED && regionxyz != Region.DEFAULT) {
						H += (c.getAdhesion(region, regionxyz) + c.getAdhesion(regionxyz, region))/2;
					}
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
			int neighbor = IDS[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
			
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
			int neighborID = IDS[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
			int neighborRegion = REGIONS[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
			
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
		boolean[][][] array = new boolean[3][3][3];
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					array[k][i][j] = IDS[k + z - 1][i + x - 1][j + y - 1] == id;
				}
			}
		}
		return array;
	}
	
	boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
		boolean[][][] array = new boolean[3][3][3];
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					array[k][i][j] = IDS[k + z - 1][i + x - 1][j + y - 1] == id && REGIONS[k + z - 1][i + x - 1][j + y - 1] == region;
				}
			}
		}
		return array;
	}
	
	boolean getConnectivity(boolean[][][] array, boolean zero) {
		int links = 0;
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			if (array[1 + MOVES_Z[i]][1 + MOVES_X[i]][1 + MOVES_Y[i]]) { links++; }
		}
		
		switch (links) {
			case 0: return false;
			case 1: return true;
			case 2:
				// Check for opposites N/S
				if (array[1][1][0] && array[1][1][2]) { return false; }
				// Check for opposites E/W
				else if (array[1][0][1] && array[1][2][1]) { return false; }
				// Check for opposites U/D
				else if (array[0][1][1] && array[2][1][1]) { return false; }
				// Check for corners
				else {
					for (int i = 0; i < NUMBER_PLANE; i++) {
						// XY plane
						if (array[1][1 + PLANE_A[i]][1 + PLANE_B[i]]
								&& array[1][1 + PLANE_A[(i + 1) % NUMBER_PLANE] ][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]
								&& array[1][1 + CORNER_A[i]][1 + CORNER_B[i]]) {
							return true;
						}
						
						// YZ plane
						if (array[1 + PLANE_B[i]][1][1 + PLANE_A[i]]
								&& array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]][1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
								&& array[1 + CORNER_B[i]][1][1 + CORNER_A[i]]) {
							return true;
						}
						
						// ZX plane
						if (array[1 + PLANE_A[i]][1 + PLANE_B[i]][1]
								&& array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]][1]
								&& array[1 + CORNER_A[i]][1 + CORNER_B[i]][1]) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < NUMBER_PLANE; i++) {
					// XY plane
					if (!array[1][1 + PLANE_A[i]][1 + PLANE_B[i]] && !array[0][1][1] && !array[2][1][1]) {
						if (array[1][1 + CORNER_A[(i + 1) % NUMBER_PLANE]][1 + CORNER_B[(i + 1) % NUMBER_PLANE]]
								&& array[1][1 + CORNER_A[(i + 2) % NUMBER_PLANE]][1 + CORNER_B[(i + 2) % NUMBER_PLANE]]) {
							return true;
						}
					}
					
					// YZ plane
					if (!array[1 + PLANE_B[i]][1][1 + PLANE_A[i]] && !array[1][0][1] && !array[1][2][1]) {
						if (array[1 + CORNER_B[(i + 1) % NUMBER_PLANE]][1][1 + CORNER_A[(i + 1) % NUMBER_PLANE]]
								&& array[1 + CORNER_B[(i + 2) % NUMBER_PLANE]][1][1 + CORNER_A[(i + 2) % NUMBER_PLANE]]) {
							return true;
						}
					}
					
					// ZX plane
					if (!array[1 + PLANE_A[i]][1 + PLANE_B[i]][1] && !array[1][1][0] && !array[1][1][2]) {
						if (array[1 + CORNER_A[(i + 1) % NUMBER_PLANE]][1 + CORNER_B[(i + 1) % NUMBER_PLANE]][1]
								&& array[1 + CORNER_A[(i + 2) % NUMBER_PLANE]][1 + CORNER_B[(i + 2) % NUMBER_PLANE]][1]) {
							return true;
						}
					}
					
					// XYZ corners
					if (array[1][1 + PLANE_A[i]][1 + PLANE_B[i]]
							&& array[1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]) {
						if (array[0][1][1]
								&& (array[1][1 + CORNER_A[i]][1 + CORNER_B[i]] ? 
									(array[0][1 + PLANE_A[i]][1 + PLANE_B[i]] || array[0][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]) :
									(array[0][1 + PLANE_A[i]][1 + PLANE_B[i]] && array[0][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]))) {
							return true;
						}
						
						if (array[2][1][1]
								&& (array[1][1 + CORNER_A[i]][1 + CORNER_B[i]] ?
								(array[2][1 + PLANE_A[i]][1 + PLANE_B[i]] || array[2][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]) :
								(array[2][1 + PLANE_A[i]][1 + PLANE_B[i]] && array[2][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]))) {
							return true;
						}
					}
				}
				return false;
			case 4:
				// Check for XY plane
				if (!array[0][1][1] && !array[2][1][1]) {
					int n = 0;
					for (int i = 0; i < NUMBER_PLANE; i++) {
						n += (array[1][1 + CORNER_A[i]][1 + CORNER_B[i]] ? 1 : 0);
					}
					return n > 2;
				}
				// Check for YZ plane
				else if (!array[1][0][1] && !array[1][2][1]) {
					int n = 0;
					for (int i = 0; i < NUMBER_PLANE; i++) {
						n += (array[1 + CORNER_B[i]][1][1 + CORNER_A[i]] ? 1 : 0);
					}
					return n > 2;
				}
				// Check for ZX plane
				else if (!array[1][1][0] && !array[1][1][2]) {
					int n = 0;
					for (int i = 0; i < NUMBER_PLANE; i++) {
						n += (array[1 + CORNER_A[i]][1 + CORNER_B[i]][1] ? 1 : 0);
					}
					return n > 2;
				}
				else {
					boolean[] planeA = new boolean[2];
					boolean[] planeB = new boolean[2];
					boolean corner = false;
					
					for (int i = 0; i < NUMBER_PLANE; i++) {
						// Check for X
						if (array[1][0][1] && array[1][2][1]
								&& array[1 + PLANE_B[i]][1][1 + PLANE_A[i]]
								&& array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]][1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]) {
							planeA = new boolean[] {
									array[1 + PLANE_B[i]][0][1 + PLANE_A[i]],
									array[1 + PLANE_B[i]][2][1 + PLANE_A[i]]
							};
							planeB = new boolean[] {
									array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]][0][1 + PLANE_A[(i + 1) % NUMBER_PLANE]],
									array[1 + PLANE_B[(i + 1) % NUMBER_PLANE]][2][1 + PLANE_A[(i + 1) % NUMBER_PLANE]]
							};
							corner = array[1 + CORNER_B[i]][1][1 + CORNER_A[i]];
							break;
						}
						// Check for Y
						else if (array[1][1][0] && array[1][1][2]
								&& array[1 + PLANE_A[i]][1 + PLANE_B[i]][1]
								&& array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]][1]) {
							planeA = new boolean[] {
									array[1 + PLANE_A[i]][1 + PLANE_B[i]][0],
									array[1 + PLANE_A[i]][1 + PLANE_B[i]][2]
							};
							planeB = new boolean[] {
									array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]][0],
									array[1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]][2]
							};
							corner = array[1 + CORNER_A[i]][1 + CORNER_B[i]][1];
						}
						// Check for Z
						else if (array[0][1][1] && array[2][1][1]
								&& array[1][1 + PLANE_A[i]][1 + PLANE_B[i]]
								&& array[1][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]) {
							planeA = new boolean[] {
									array[0][1 + PLANE_A[i]][1 + PLANE_B[i]],
									array[2][1 + PLANE_A[i]][1 + PLANE_B[i]]
							};
							planeB = new boolean[] {
									array[0][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]],
									array[2][1 + PLANE_A[(i + 1) % NUMBER_PLANE]][1 + PLANE_B[(i + 1) % NUMBER_PLANE]]
							};
							corner = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
						}
					}
					if (planeA[0] && planeA[1] && (planeB[0] || planeB[1] || corner)) { return true; }
					else if (planeB[0] && planeB[1] && (planeA[0] || planeA[1] || corner)) { return true; }
					else if (corner && ((planeA[0] && planeB[1]) || (planeA[1] && planeB[0]))) { return true; }
				}
				return false;
			case 5:
				boolean[] plane = new boolean[4];
				boolean[] corner = new boolean[4];
				int nPlane = 0;
				int nCorner = 0;
				
				// Check XY 
				if (!array[0][1][1] || !array[2][1][1]) {
					int z = (array[0][1][1] ? 0 : 2);
					for (int i = 0; i < NUMBER_PLANE; i++) {
						corner[i] = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
						plane[i] = array[z][1 + PLANE_A[i]][1 + PLANE_B[i]];
						if (corner[i]) { nCorner++; }
						if (plane[i]) { nPlane++; }
					}
				}
				// Check YZ
				else if (!array[1][0][1] || !array[1][2][1]) {
					int x = (array[1][0][1] ? 0 : 2);
					for (int i = 0; i < NUMBER_PLANE; i++) {
						corner[i] = array[1 + CORNER_A[i]][1][1 + CORNER_B[i]];
						plane[i] = array[1 + PLANE_A[i]][x][1 + PLANE_B[i]];
						if (corner[i]) { nCorner++; }
						if (plane[i]) { nPlane++; }
					}
				}
				// Check ZX
				else if (!array[1][1][0] || !array[1][1][2]) {
					int y = (array[1][1][0] ? 0 : 2);
					for (int i = 0; i < NUMBER_PLANE; i++) {
						corner[i] = array[1 + CORNER_A[i]][1 + CORNER_B[i]][1];
						plane[i] = array[1 + PLANE_A[i]][1 + PLANE_B[i]][y];
						if (corner[i]) { nCorner++; }
						if (plane[i]) { nPlane++; }
					}
				}
				
				if (nCorner + nPlane < 4) { return false; }
				if (nPlane == 4 || nCorner + nPlane > 5) { return true; }
				if (nCorner > 2) { return nPlane > 0; }
				if (nCorner == 1 && nPlane == 3) {
					for (int i = 0; i < NUMBER_PLANE; i++) {
						if (!plane[i] && (corner[i] || corner[(i + 3) % NUMBER_PLANE])) { return true; }
					}
					return false;
				}
				if (nCorner == 2 && nPlane == 2) {
					for (int i = 0; i < NUMBER_PLANE; i++) {
						if (plane[i] && plane[(i + 1) % NUMBER_PLANE]) { return !corner[i]; }
						if (plane[i] && plane[(i + 2) % NUMBER_PLANE]) {
							return (!corner[i] || !corner[(i + 1) % NUMBER_PLANE]) &&
									(!corner[(i + 2) % NUMBER_PLANE] || !corner[(i + 3) % NUMBER_PLANE]);
						}
					}
				}
				if (nCorner == 2 && nPlane == 3) {
					for (int i = 0; i < NUMBER_PLANE; i++) {
						if (!plane[i]) { return corner[i] || corner[(i + 3) % NUMBER_PLANE]; }
					}
				}
			case 6:
				return zero;
		}
		
		return false;
	}
	
	HashSet<Integer> getUniqueIDs(int x, int y, int z) {
		int id = IDS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighbor = IDS[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
			if (id != neighbor) { unique.add(neighbor); }
		}
		return unique;
	}
	
	HashSet<Integer> getUniqueRegions(int x, int y, int z) {
		int id = IDS[z][x][y];
		int region = REGIONS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			int neighborID = IDS[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
			int neighborRegion = REGIONS[z + MOVES_Z[i]][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighborID != id) { continue; }
			if (region != neighborRegion) { unique.add(neighborRegion); }
		}
		
		return unique;
	}
}