package arcade.sim;

import java.util.HashSet;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

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
	public static final int NUMBER_PLANE = 4;
	
	/** List of plane movements for first coordinate */
	private static final int[] PLANE_A = { 0, 1, 0, -1 };
	
	/** List of plane movements for second coordinate */
	private static final int[] PLANE_B = { -1, 0, 1, 0 };
	
	/** List of x direction corner movements */
	private static final int[] CORNER_A = { 1, 1, -1, -1 };
	
	/** List of y direction corner movements */
	private static final int[] CORNER_B = { -1, 1, 1, -1 };
	
	/**
	 * Creates a cellular {@code Potts} model in 3D.
	 *
	 * @param series  the simulation series
	 * @param grid  the cell grid
	 */
	public Potts3D(Series series, Grid grid) { super(series, grid); }
	
	double getAdhesion(int id, int x, int y, int z) {
		// TODO
		return 0;
	}
	
	double getAdhesion(int id, int tag, int x, int y, int z) {
		// TODO
		return 0;
	}
	
	int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
		// TODO
		return null;
	}
	
	int[] calculateChange(int id, int sourceTag, int targetTag, int x, int y, int z) {
		// TODO
		return null;
	}
	
	boolean[][][] getNeighborhood(int id, int x, int y, int z) {
		// TODO
		return null;
	}
	
	boolean[][][] getNeighborhood(int id, int tag, int x, int y, int z) {
		// TODO
		return null;
	}
	
	boolean getConnectivity(boolean[][][] array, boolean zero) {
		int sites = 0;
		for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
			if (array[1 + MOVES_Z[i]][1 + MOVES_X[i]][1 + MOVES_Y[i]]) { sites++; }
		}
		
		switch (sites) {
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
								&& array[1][1 + PLANE_A[(i + 1)%NUMBER_PLANE] ][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]
								&& array[1][1 + CORNER_A[i]][1 + CORNER_B[i]]) {
							return true;
						}
						
						// YZ plane
						if (array[1 + PLANE_B[i]][1][1 + PLANE_A[i]]
								&& array[1 + PLANE_B[(i + 1)%NUMBER_PLANE]][1][1 + PLANE_A[(i + 1)%NUMBER_PLANE]]
								&& array[1 + CORNER_B[i]][1][1 + CORNER_A[i]]) {
							return true;
						}
						
						// ZX plane
						if (array[1 + PLANE_A[i]][1 + PLANE_B[i]][1]
								&& array[1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]][1]
								&& array[1  + CORNER_A[i]][1 + CORNER_B[i]][1]) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < NUMBER_PLANE; i++) {
					// XY plane
					if (!array[1][1 + PLANE_A[i]][1 + PLANE_B[i]]) {
						if (array[1][1 + CORNER_A[(i + 1)%NUMBER_PLANE]][1 + CORNER_B[(i + 1)%NUMBER_PLANE]]
								&& array[1][1 + CORNER_A[(i + 2)%NUMBER_PLANE]][1 + CORNER_B[(i + 2)%NUMBER_PLANE]]) {
							return true;
						}
					}
					
					// YZ plane
					if (!array[1 + PLANE_B[i]][1][1 + PLANE_A[i]]) {
						if (array[1 + CORNER_B[(i + 1)%NUMBER_PLANE]][1][1 + CORNER_A[(i + 1)%NUMBER_PLANE]]
								&& array[1 + CORNER_B[(i + 2)%NUMBER_PLANE]][1][1 + CORNER_A[(i + 2)%NUMBER_PLANE]]) {
							return true;
						}
					}
					
					// ZX plane
					if (!array[1 + PLANE_A[i]][1 + PLANE_B[i]][1]) {
						if (array[1 + CORNER_A[(i + 1)%NUMBER_PLANE]][1 + CORNER_B[(i + 1)%NUMBER_PLANE]][1]
								&& array[1 + CORNER_A[(i + 2)%NUMBER_PLANE]][1 + CORNER_B[(i + 2)%NUMBER_PLANE]][1]) {
							return true;
						}
					}
					
					// XYZ corners
					if (array[1][1 + PLANE_A[i]][1 + PLANE_B[i]]
							&& array[1][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]) {
						if (array[0][1][1]
								&& (array[1][1 + CORNER_A[i]][1 + CORNER_B[i]] ? 
									(array[0][1 + PLANE_A[i]][1 + PLANE_B[i]] || array[0][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]) :
									(array[0][1 + PLANE_A[i]][1 + PLANE_B[i]] && array[0][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]))) {
							return true;
						}
						
						if (array[2][1][1]
								&& (array[1][1 + CORNER_A[i]][1 + CORNER_B[i]] ?
								(array[2][1 + PLANE_A[i]][1 + PLANE_B[i]] || array[2][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]) :
								(array[2][1 + PLANE_A[i]][1 + PLANE_B[i]] && array[2][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]))) {
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
								&& array[1 + PLANE_B[(i + 1)%NUMBER_PLANE]][1][1 + PLANE_A[(i + 1)%NUMBER_PLANE]]) {
							planeA = new boolean[] {
									array[1 + PLANE_B[i]][0][1 + PLANE_A[i]],
									array[1 + PLANE_B[i]][2][1 + PLANE_A[i]]
							};
							planeB = new boolean[] {
									array[1 + PLANE_B[(i + 1)%NUMBER_PLANE]][0][1 + PLANE_A[(i + 1)%NUMBER_PLANE]],
									array[1 + PLANE_B[(i + 1)%NUMBER_PLANE]][2][1 + PLANE_A[(i + 1)%NUMBER_PLANE]]
							};
							corner = array[1 + CORNER_B[i]][1][1 + CORNER_A[i]];
							break;
						}
						// Check for Y
						else if (array[1][1][0] && array[1][1][2]
								&& array[1 + PLANE_A[i]][1 + PLANE_B[i]][1]
								&& array[1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]][1]) {
							planeA = new boolean[] {
									array[1 + PLANE_A[i]][1 + PLANE_B[i]][0],
									array[1 + PLANE_A[i]][1 + PLANE_B[i]][2]
							};
							planeB = new boolean[] {
									array[1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]][0],
									array[1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]][2]
							};
							corner = array[1 + CORNER_A[i]][1 + CORNER_B[i]][1];
						}
						// Check for Z
						else if (array[0][1][1] && array[2][1][1]
								&& array[1][1 + PLANE_A[i]][1 + PLANE_B[i]]
								&& array[1][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]) {
							planeA = new boolean[] {
									array[0][1 + PLANE_A[i]][1 + PLANE_B[i]],
									array[2][1 + PLANE_A[i]][1 + PLANE_B[i]]
							};
							planeB = new boolean[] {
									array[0][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]],
									array[2][1 + PLANE_A[(i + 1)%NUMBER_PLANE]][1 + PLANE_B[(i + 1)%NUMBER_PLANE]]
							};
							corner = array[1][1 + CORNER_A[i]][1 + CORNER_B[i]];
						}
					}
					if (planeA[0] && planeA[1] && (planeB[0] || planeB[1] || corner)) { return true; }
					else if (planeB[0] && planeB[1] && (planeA[0] || planeA[1] || corner)) { return true; }
					else if (corner && ((planeA[0] && planeB[1]) || (planeA[1] && planeB[0]))) { return true; }
				}
		}
		
		return false;
	}
	
	HashSet<Integer> getUniqueIDs(int x, int y, int z) {
		// TODO
		return null;
	}
	
	HashSet<Integer> getUniqueTags(int x, int y, int z) {
		// TODO
		return null;
	}
}