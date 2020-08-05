package arcade.sim;

import sim.engine.*;
import arcade.env.grid.Grid;

public abstract class Potts implements Steppable {
	/** Default tag value */
	public static final int TAG_DEFAULT = -1;
	
	/** Code for volume lambda */
	public static final int LAMBDA_VOLUME = 0;
	
	/** Code for surface lambda */
	public static final int LAMBDA_SURFACE = 1;
	
	/** Potts array for ids */
	public int[][][] IDS;
	
	/** Potts array for tags */
	public int[][][] TAGS;
	
	/** Grid holding cells */
	Grid grid;
	
	/**
	 * Creates a cellular {@code Potts} model.
	 *
	 * @param series  the simulation series
	 * @param grid  the cell grid
	 */
	public Potts(Series series, Grid grid) {
		this.grid = grid;
		
		// Creates potts arrays.
		IDS = new int[series._height][series._length][series._width];
		TAGS = new int[series._height][series._length][series._width];
	}
}