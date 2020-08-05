package arcade.sim;

import java.util.HashSet;
import sim.engine.*;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

public class Potts2D extends Potts {
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
	
	/**
	 * Creates a cellular {@code Potts2D} model.
	 * 
	 * @param series  the simulation series
	 * @param grid  the cell grid
	 */
	public Potts2D(Series series, Grid grid) {
		super(series, grid);
		
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
	
	/**
	 * Gets adhesion energy for a given voxel tag.
	 * 
	 * @param id  the voxel id
	 * @param tag  the voxel tag
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the energy
	 */
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
	 * Gets change in adhesion energy for tag.
	 *
	 * @param id  the voxel id
	 * @param sourceTag  the tag of the source voxel
	 * @param targetTag  the tag of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaAdhesion(int id, int sourceTag, int targetTag, int x, int y, int z) {
		double source = getAdhesion(id, sourceTag, x, y, z);
		double target = getAdhesion(id, targetTag, x, y, z);
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
			Cell c = getCell(id);
			double volume = c.getVolume();
			double targetVolume = c.getTargetVolume();
			double lambda = c.getLambda(LAMBDA_VOLUME);
			return lambda * Math.pow((volume - targetVolume + change), 2);
		}
	}
	
	/**
	 * Gets volume energy for a given change in volume for tag.
	 * 
	 * @param id  the voxel id
	 * @param tag  the voxel tag
	 * @param change  the change in volume
	 * @return  the energy
	 */
	double getVolume(int id, int tag, int change) {
		Cell c = getCell(id);
		double volume = c.getVolume(tag);
		double targetVolume = c.getTargetVolume(tag);
		double lambda = c.getLambda(LAMBDA_VOLUME, tag);
		return lambda * Math.pow((volume - targetVolume + change), 2);
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
	 * Gets change in volume energy for tag.
	 * 
	 * @param id  the voxel id
	 * @param sourceTag  the tag of the source voxel
	 * @param targetTag  the tag of the source voxel
	 * @return  the change in energy
	 */
	double getDeltaVolume(int id, int sourceTag, int targetTag) {
		double source = getVolume(id, sourceTag, -1) - getVolume(id, sourceTag, 0);
		double target = getVolume(id, targetTag, 1) - getVolume(id, targetTag, 0);
		return target + source;
	}
	
	/**
	 * Gets the surface energy for a given change in surface.
	 * 
	 * @param id  the voxel id
	 * @param change  the change in surface
	 * @return  the energy
	 */
	double getSurface(int id, int change) {
		if (id == 0) { return 0; }
		else {
			Cell c = getCell(id);
			double surface = c.getSurface();
			double targetSurface = c.getTargetSurface();
			double lambda = c.getLambda(LAMBDA_SURFACE);
			return lambda * Math.pow((surface - targetSurface + change), 2);
		}
	}
	
	/**
	 * Gets the surface energy for a given change in surface for tag.
	 * 
	 * @param id  the voxel id
	 * @param tag  the voxel tag   
	 * @param change  the change in surface
	 * @return  the energy
	 */
	double getSurface(int id, int tag, int change) {
		Cell c = getCell(id);
		double surface = c.getSurface(tag);
		double targetSurface = c.getTargetSurface(tag);
		double lambda = c.getLambda(LAMBDA_SURFACE, tag);
		return lambda * Math.pow((surface - targetSurface + change), 2);
	}
	
	/**
	 * Gets change in surface energy.
	 *
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaSurface(int sourceID, int targetID, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < 4; i++) {
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
		
		double source = getSurface(sourceID, sourceSurfaceChange) - getSurface(sourceID, 0);
		double target = getSurface(targetID, targetSurfaceChange) - getSurface(targetID, 0);
		
		return target + source;
	}
	
	/**
	 * Gets change in surface energy for tag.
	 * 
	 * @param id  the voxel id
	 * @param sourceTag  the id of the source voxel
	 * @param targetTag  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaSurface(int id, int sourceTag, int targetTag, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < 4; i++) {
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
		
		double source = getSurface(id, sourceTag, sourceSurfaceChange) - getSurface(id, sourceTag, 0);
		double target = getSurface(id, targetTag, targetSurfaceChange) - getSurface(id, targetTag, 0);
		
		return target + source;
	}
	
	/**
	 * Steps through array updates for Monte Carlo step.
	 * 
	 * @param simstate  the MASON simulation state
	 */
	public void step(SimState simstate) {
		MersenneTwisterFast random = simstate.random;
		int x, y;
		int z = 0;
		
		for (int step = 0; step < STEPS; step++) {
			// Get random coordinate for candidate.
			x = random.nextInt(LENGTH) + 1;
			y = random.nextInt(WIDTH) + 1;
			
			// Get unique targets.
			HashSet<Integer> uniqueIDTargets = getUniqueIDs(x, y, z);
			HashSet<Integer> uniqueTagTargets = getUniqueTags(x, y, z);
			
			// Select unique ID (if there is one), otherwise select unique
			// tag (if there is one). If there are neither, then skip.
			if (uniqueIDTargets.size() > 0) {
				int targetID = (int)uniqueIDTargets.toArray()[simstate.random.nextInt(uniqueIDTargets.size())];
				flip(IDS[z][x][y], targetID, x, y, z, random);
			}
			else if (uniqueTagTargets.size() > 0) {
				int targetTag = (int)uniqueTagTargets.toArray()[simstate.random.nextInt(uniqueTagTargets.size())];
				flip(IDS[z][x][y], TAGS[z][x][y], targetTag, x, y, z, random);
			}
		}
	}
	
	/**
	 * Flips the voxel from source to target id based on Boltzmann probability. 
	 * 
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param random  the random number generator
	 */
	void flip(int sourceID, int targetID, int x, int y, int z, MersenneTwisterFast random) {
		// Check connectivity of source.
		if (sourceID > 0) {
			boolean candidateConnected = getConnectivity(getNeighborhood(sourceID, x, y, z), IDS[z][x][y] == 0);
			if (!candidateConnected) { return; }
			
			// Check connectivity of tags.
			if (TAGS[z][x][y] < TAG_DEFAULT) {
				boolean candidateTagConnected = getConnectivity(getNeighborhood(sourceID, TAGS[z][x][y], x, y, z), TAGS[z][x][y] == -1);
				if (!candidateTagConnected) { return; }
			}
		}
		
		// Check connectivity of target.
		if (targetID > 0) {
			boolean targetConnected = getConnectivity(getNeighborhood(targetID, x, y, z), IDS[z][x][y] == 0);
			if (!targetConnected) { return; }
			
			// Check connectivity of tags.
			if (TAGS[z][x][y] < TAG_DEFAULT) {
				boolean candidateTagConnected = getConnectivity(getNeighborhood(targetID, TAGS[z][x][y], x, y, z), TAGS[z][x][y] == -1);
				if (!candidateTagConnected) { return; }
			}
		}
		
		// Calculate energy change.
		double dH = 0;
		dH += getDeltaAdhesion(sourceID, targetID, x, y, z);
		dH += getDeltaVolume(sourceID, targetID);
		dH += getDeltaSurface(sourceID, targetID, x, y, z);
		
		double p;
		if (dH < 0) { p = 1; }
		else { p = Math.exp(-dH/TEMPERATURE); }
		
		if (random.nextDouble() < p) {
			IDS[z][x][y] = targetID;
			TAGS[z][x][y] = (targetID == 0 ? 0 : TAG_DEFAULT);
			
			if (sourceID > 0) {
				getCell(sourceID).getLocation().remove(x, y, z);
			}
			if (targetID > 0) {
				getCell(targetID).getLocation().add(x, y, z);
			}
		}
	}
	
	/**
	 * Flips the voxel from source to target tag based on Boltzmann probability.
	 * 
	 * @param id  the voxel id
	 * @param sourceTag  the tag of the source voxel
	 * @param targetTag  the tag of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param random  the random number generator
	 */
	void flip(int id, int sourceTag, int targetTag, int x, int y, int z, MersenneTwisterFast random) {
		// Check connectivity of source.
		if (sourceTag < TAG_DEFAULT) {
			boolean candidateConnected = getConnectivity(getNeighborhood(id, sourceTag, x, y, z), TAGS[z][x][y] == -1);
			if (!candidateConnected) { return; }
		}
		
		// Check connectivity of target.
		if (targetTag < TAG_DEFAULT) {
			boolean targetConnected = getConnectivity(getNeighborhood(id, targetTag, x, y, z), TAGS[z][x][y] == -1);
			if (!targetConnected) { return; }
		}
		
		// Calculate energy change.
		double dH = 0;
		dH += getDeltaAdhesion(id, sourceTag, targetTag, x, y, z);
		dH += getDeltaVolume(id, sourceTag, targetTag);
		dH += getDeltaSurface(id, sourceTag, targetTag, x, y, z);
		
		double p;
		if (dH < 0) { p = 1; }
		else { p = Math.exp(-dH/TEMPERATURE); }
		
		if (random.nextDouble() < p) {
			TAGS[z][x][y] = targetTag;
			Cell c = getCell(id);
			c.getLocation().remove(sourceTag, x, y, z);
			c.getLocation().add(targetTag, x, y, z);
		}
	}
	
	/**
	 * Gets neighborhood for the given voxel.
	 *
	 * @param id  the voxel id
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	boolean[][][] getNeighborhood(int id, int x, int y, int z) {
		boolean[][][] array = new boolean[1][3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				array[0][i][j] = IDS[z][i + x - 1][j + y - 1] == id;
			}
		}
		return array;
	}
	
	/**
	 * Gets neighborhood for the given voxel tag.
	 *
	 * @param id  the voxel id
	 * @param tag  the voxel tag
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	boolean[][][] getNeighborhood(int id, int tag, int x, int y, int z) {
		boolean[][][] array = new boolean[1][3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				array[0][i][j] = IDS[z][i + x - 1][j + y - 1] == id && TAGS[z][i + x - 1][j + y - 1] == tag;
			}
		}
		return array;
	}
	
	/**
	 * Determines connectivity of given neighborhood.
	 * 
	 * @param array  the array of neighbors
	 * @param zero  {@code true} if the location has a zero id, {@code false} otherwise
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	boolean getConnectivity(boolean[][][] array, boolean zero) {
		int sites = 0;
		for (int i = 0; i < 4; i++) {
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
					for (int i = 0; i < 4; i++) {
						if (array[0][1 + MOVES_X[i]][1 + MOVES_Y[i]]
								&& array[0][1 + MOVES_X[(i + 1)%4] ][1 + MOVES_Y[(i + 1)%4]]
								&& array[0][1 + MOVES_CORNER_X[i]][1 + MOVES_CORNER_Y[i]]) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < 4; i++) {
					if (!array[0][1 + MOVES_X[i]][1 + MOVES_Y[i]]) {
						if (array[0][1 + MOVES_CORNER_X[(i + 1)%4]][1 + MOVES_CORNER_Y[(i + 1)%4]]
								&& array[0][1 + MOVES_CORNER_X[(i + 2)%4]][1 + MOVES_CORNER_Y[(i + 2)%4]]) {
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
	
	/**
	 * Gets the {@link arcade.agent.cell.Cell} object for the given id.
	 *
	 * @param id  the cell id
	 * @return  the {@link arcade.agent.cell.Cell} object, {@code null} if id is zero
	 */
	Cell getCell(int id) {
		if (id > 0) { return (Cell)grid.getObjectAt(id); }
		else { return null; }
	}
	
	/**
	 * Gets unique IDs adjacent to given voxel.
	 * 
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the list of unique IDs
	 */
	HashSet<Integer> getUniqueIDs(int x, int y, int z) {
		int id = IDS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < 4; i++) {
			int neighbor = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			if (id != neighbor) { unique.add(neighbor); }
		}
		return unique;
	}
	
	/**
	 * Gets unique tags adjacent to given voxel.
	 * 
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the list of unique tags
	 */
	HashSet<Integer> getUniqueTags(int x, int y, int z) {
		int id = IDS[z][x][y];
		int tag = TAGS[z][x][y];
		HashSet<Integer> unique = new HashSet<>();
		
		for (int i = 0; i < 4; i++) {
			int neighborID = IDS[z][x + MOVES_X[i]][y + MOVES_Y[i]]; 
			int neighborTag = TAGS[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			
			if (neighborID != id) { continue; }
			if (tag != neighborTag) { unique.add(neighborTag); }
		}
		
		return unique;
	}
}