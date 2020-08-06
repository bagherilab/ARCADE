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
		// TODO
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