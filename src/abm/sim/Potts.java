package abm.sim;

import java.util.HashSet;
import sim.engine.*;
import abm.agent.cell.Cell;
import abm.env.grid.Grid;

public class Potts implements Steppable {
	/** Code for volume lambda */
	public static final int LAMBDA_VOLUME = 0;
	
	/** Code for perimeter lambda */
	public static final int LAMBDA_PERIMETER = 1;
	
	/** List of x direction movements (N, E, S, W) */
	public static final int[] MOVES_X = { 0, 1, 0, -1 };
	
	/** List of y direction movements (N, E, S, W) */
	public static final int[] MOVES_Y = { -1, 0, 1, 0 };
	
	/** List of x direction corner movements (NE, SE, SW, NW) */
	public static final int[] MOVES_CORNER_X = { 1, 1, -1, -1 };
	
	/** List of y direction corner movements (NE, SE, SW, NW) */
	public static final int[] MOVES_CORNER_Y = { -1, 1, 1, -1 };
	
	/** Length (x direction) of potts array */
	final int LENGTH;
	
	/** Width (y direction) of potts array */
	final int WIDTH;
	
	/** Depth (z direction) of potts array */
	final int HEIGHT;
	
	/** Number of steps in Monte Carlo Step */
	final int STEPS;
	
	/** Effective cell temperature */
	final int TEMPERATURE;
	
	/** Potts array */
	public int[][][] potts;
	
	/** Grid holding cells */
	Grid grid;
	
	/** Change in perimeter for source */
	private int sourcePerimeterChange;
	
	/** Change in perimeter for target */
	private int targetPerimeterChange;
	
	/**
	 * Creates a cellular {@code Potts} model.
	 * 
	 * @param series  the simulation series
	 * @param grid  the cell grid
	 */
	public Potts(Series series, Grid grid) {
		this.grid = grid;
		
		// Creates potts array.
		potts = new int[series._height][series._length][series._width];
		
		// Ensure a 1 voxel border around to avoid boundary checks.
		LENGTH = series._length - 2;
		WIDTH = series._width - 2;
		HEIGHT = series._height - 2;
		
		// Number of Monte Carlo steps
		STEPS = LENGTH*WIDTH;
		
		// Get temperature.
		TEMPERATURE = (int)series.getParam("TEMPERATURE");
	}
	
	/**
	 * Gets adhesion energy for a given voxel.
	 * 
	 * @param id  the voxel id
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the energy
	 */
	double getAdhesion(int id, int x, int y, int z) {
		double H = 0;
		Cell a = (id == 0 ? null : (Cell)grid.getObjectAt(id));
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (!(i == x && j == y)) {
					if (potts[z][i][j] != id) {
						Cell b = (potts[z][i][j] == 0 ? null : ((Cell)grid.getObjectAt(potts[z][i][j])));
						if (a == null && b == null) { System.exit(-1); }
						else if (a == null) { H += b.getAdhesion(0); }
						else if (b == null) { H += a.getAdhesion(0); }
						else { H += (a.getAdhesion(b.getPop()) + b.getAdhesion(a.getPop()))/2.0; }
					}
				}
			}
		} 
		
		return H;
	}
	
	/**
	 * Gets change in adhesion energy.
	 * 
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaAdhesion(int sourceID, int targetID, int x, int y, int z) {
		double source = getAdhesion(sourceID, x, y, z);
		double target = getAdhesion(targetID, x, y, z);
		return target - source;
	}
	
	/**
	 * Gets volume energy for a given change in volume.
	 * 
	 * @param id  the voxel id
	 * @param change  the change in volume
	 * @return  the energy
	 */
	double getVolume(int id, int change) {
		if (id == 0) { return 0; }
		else {
			Cell c = (Cell)grid.getObjectAt(id);
			double volume = c.getVolume();
			double targetVolume = c.getTargetVolume();
			double lambda = c.getLambda(LAMBDA_VOLUME);
			return lambda * Math.pow((volume - targetVolume + change), 2);
		}
	}
	
	/**
	 * Gets change in volume energy.
	 *
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @return  the change in energy
	 */
	double getDeltaVolume(int sourceID, int targetID) {
		double source = getVolume(sourceID, -1) - getVolume(sourceID, 0);
		double target = getVolume(targetID, 1) - getVolume(targetID, 0);
		return target + source;
	}
	
	/**
	 * Gets the perimeter energy for a given change in perimeter.
	 * 
	 * @param id  the voxel id
	 * @param change  the change in perimeter
	 * @return  the energy
	 */
	double getPerimeter(int id, int change) {
		if (id == 0) { return 0; }
		else {
			Cell c = (Cell)grid.getObjectAt(id);
			double perimeter = c.getPerimeter();
			double targetPerimeter = c.getTargetPerimeter();
			double lambda = c.getLambda(LAMBDA_PERIMETER);
			return lambda * Math.pow(perimeter - targetPerimeter + change, 2) - lambda * Math.pow(perimeter - targetPerimeter, 2);
		}
	}
	
	/**
	 * Gets change in perimeter energy.
	 *
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaPerimeter(int sourceID, int targetID, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < 4; i++) {
			int neighbor = potts[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			if (neighbor != sourceID) {
				beforeSource++;
				if (neighbor == targetID) { beforeTarget++; }
			}
			
			if (neighbor != targetID) {
				afterTarget++;
				if (neighbor == sourceID) { afterSource++; }
			}
		}
		
		// Save changes to perimeter.
		sourcePerimeterChange = afterSource - beforeSource;
		targetPerimeterChange = afterTarget - beforeTarget;
		
		double source = getPerimeter(sourceID, sourcePerimeterChange);
		double target = getPerimeter(targetID, targetPerimeterChange);
		
		return target + source;
	}
	
	/**
	 * Steps through array updates for Monte Carlo step.
	 * 
	 * @param simstate  the MASON simulation state
	 */
	public void step(SimState simstate) {
		int x, y, sourceID, targetID;
		int z = 0;
		
		for (int step = 0; step < STEPS; step++) {
			// Get random coordinate for candidate.
			x = simstate.random.nextInt(LENGTH) + 1;
			y = simstate.random.nextInt(WIDTH) + 1;
			
			// Get id of candidate.
			sourceID = potts[z][x][y];
			
			// Get unique targets.
			HashSet<Integer> uniqueTargets = new HashSet<>();
			for (int i = 0; i < 4; i++) {
				int id = potts[z][x + MOVES_X[i]][y + MOVES_Y[i]];
				if (id != sourceID) { uniqueTargets.add(id); }
			}
			
			// Skip if there are no unique targets.
			if (uniqueTargets.size() == 0) { continue; }
			
			// Select target from unique targets.
			targetID = (int)uniqueTargets.toArray()[simstate.random.nextInt(uniqueTargets.size())];
			
			// Check connectivity of source.
			if (sourceID > 0) {
				boolean candidateConnected = getConnectivity(sourceID, x, y, z);
				if (!candidateConnected) { continue; }
			}
			
			// Check connectivity of target.
			if (targetID > 0) {
				boolean targetConnected = getConnectivity(targetID, x, y, z);
				if (!targetConnected) { continue; }
			}
			
			// Calculate energy change.
			double dH = 0;
			dH += getDeltaAdhesion(sourceID, targetID, x, y, z);
			dH += getDeltaVolume(sourceID, targetID);
			dH += getDeltaPerimeter(sourceID, targetID, x, y, z);
			
			double p;
			if (dH < 0) { p = 1; }
			else { p = Math.exp(-dH/TEMPERATURE); }
			
			if (simstate.random.nextDouble() < p) {
				potts[z][x][y] = targetID;
				if (sourceID > 0) {
					((Cell)grid.getObjectAt(sourceID)).removeVoxel(x, y, z, sourcePerimeterChange);
				}
				if (targetID > 0) {
					((Cell)grid.getObjectAt(targetID)).addVoxel(x, y, z, targetPerimeterChange);
				}
			}
		}
	}
	
	/**
	 * Gets connectivity for the given voxel.
	 * 
	 * @param id  the voxel id
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	boolean getConnectivity(int id, int x, int y, int z) {
		// Count number of sites in adjacency neighborhood.
		int sites = 0;
		for (int i = 0; i < 4; i++) {
			if (potts[z][x + MOVES_X[i]][y + MOVES_Y[i]] == id) { sites++; }
		}
		
		switch (sites) {
			case 0: return false;
			case 1: return true;
			case 2:
				// Check for opposites N/S
				if (potts[z][x + 1][y] == id && potts[z][x - 1][y] == id) { return false; }
				// Check for opposites E/W
				else if (potts[z][x][y + 1] == id && potts[z][x][y - 1] == id) { return false; }
				// Check for corners
				else {
					for (int i = 0; i < 4; i++) {
						if (potts[z][x + MOVES_X[i]][y + MOVES_Y[i]] == id
								&& potts[z][x + MOVES_X[(i + 1)%4] ][y + MOVES_Y[(i + 1)%4]] == id
								&& potts[z][x + MOVES_CORNER_X[i]][y + MOVES_CORNER_Y[i]] == id) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < 4; i++) {
					if (potts[z][x + MOVES_X[i]][y + MOVES_Y[i]] != id) {
						if (potts[z][x + MOVES_CORNER_X[(i + 1)%4]][y + MOVES_CORNER_Y[(i + 1)%4]] == id
								&& potts[z][x + MOVES_CORNER_X[(i + 2)%4]][y + MOVES_CORNER_Y[(i + 2)%4]] == id) {
							return true;
						}
					}
				}
				return false;
			case 4:
				return potts[z][x][y] == 0;
		}
		return false;
	}
}