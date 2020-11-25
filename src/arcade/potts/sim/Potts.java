package arcade.potts.sim;

import java.util.HashSet;
import sim.engine.*;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;

public abstract class Potts implements Steppable {
	/** Length (x direction) of potts array */
	final int LENGTH;
	
	/** Width (y direction) of potts array */
	final int WIDTH;
	
	/** Depth (z direction) of potts array */
	final int HEIGHT;
	
	/** Number of steps in Monte Carlo Step */
	final int STEPS;
	
	/** Effective cell temperature */
	final double TEMPERATURE;
	
	/** {@code true} if cells have regions, {@code false} otherwise */
	final boolean HAS_REGIONS;
	
	/** Potts array for ids */
	public int[][][] IDS;
	
	/** Potts array for regions */
	public int[][][] REGIONS;
	
	/** Grid holding cells */
	public Grid grid;
	
	/**
	 * Creates a cellular {@code Potts} model.
	 *
	 * @param series  the simulation series
	 */
	public Potts(PottsSeries series) {
		// Creates potts arrays.
		IDS = new int[series._height][series._length][series._width];
		REGIONS = new int[series._height][series._length][series._width];
		
		// Ensure a 1 voxel border around to avoid boundary checks.
		LENGTH = series._length - 2;
		WIDTH = series._width - 2;
		HEIGHT = (series._height == 1 ? 1 : series._height - 2);
		
		// Number of Monte Carlo steps
		int MCS = series._potts.getInt("MCS");
		STEPS = MCS*LENGTH*WIDTH*HEIGHT;
		
		// Get temperature.
		TEMPERATURE = series._potts.getDouble("TEMPERATURE");
		
		// Check if there are regions.
		HAS_REGIONS = series._populations.values().stream()
				.map(e -> e.filter("(REGION)").getKeys().size())
				.anyMatch(e -> e > 0);
	}
	
	/**
	 * Steps through array updates for Monte Carlo step.
	 *
	 * @param simstate  the MASON simulation state
	 */
	public void step(SimState simstate) {
		MersenneTwisterFast random = simstate.random;
		double r;
		int x, y, z;
		
		for (int step = 0; step < STEPS; step++) {
			// Get random coordinate for candidate.
			x = random.nextInt(LENGTH) + 1;
			y = random.nextInt(WIDTH) + 1;
			z = (HEIGHT == 1 ? 0 : random.nextInt(HEIGHT) + 1);
			r = random.nextDouble();
			
			// Check if cell has regions.
			boolean hasRegions = (IDS[z][x][y] != 0 && getCell(IDS[z][x][y]).hasRegions());
			
			// Get unique targets.
			HashSet<Integer> uniqueIDTargets = getUniqueIDs(x, y, z);
			HashSet<Integer> uniqueRegionTargets = getUniqueRegions(x, y, z);
			
			// Select unique ID (if there is one), otherwise select unique
			// region (if there is one). If there are neither, then skip.
			if (hasRegions && uniqueRegionTargets.size() > 0) {
				int targetRegion = (int)uniqueRegionTargets.toArray()[simstate.random.nextInt(uniqueRegionTargets.size())];
				flip(IDS[z][x][y], REGIONS[z][x][y], targetRegion, x, y, z, r);
			}
			else if (uniqueIDTargets.size() > 0) {
				int targetID = (int)uniqueIDTargets.toArray()[simstate.random.nextInt(uniqueIDTargets.size())];
				flip(IDS[z][x][y], targetID, x, y, z, r);
			}
		}
	}
	
	/**
	 * Flips connected voxel from source to target id based on Boltzmann probability. 
	 *
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param r  a random number
	 */
	void flip(int sourceID, int targetID, int x, int y, int z, double r) {
		// Check connectivity of source.
		if (sourceID > 0) {
			boolean candidateConnected = getConnectivity(getNeighborhood(sourceID, x, y, z), IDS[z][x][y] == 0);
			if (!candidateConnected) { return; }
			
			// Check connectivity of regions.
			if (REGIONS[z][x][y] > Region.DEFAULT.ordinal()) {
				boolean candidateRegionConnected = getConnectivity(getNeighborhood(sourceID, REGIONS[z][x][y], x, y, z), false);
				if (!candidateRegionConnected) { return; }
			}
		}
		
		// Check connectivity of target.
		if (targetID > 0) {
			boolean targetConnected = getConnectivity(getNeighborhood(targetID, x, y, z), IDS[z][x][y] == 0);
			if (!targetConnected) { return; }
			
			// Check connectivity of regions.
			if (REGIONS[z][x][y] > Region.DEFAULT.ordinal()) {
				boolean candidateRegionConnected = getConnectivity(getNeighborhood(targetID, REGIONS[z][x][y], x, y, z), false);
				if (!candidateRegionConnected) { return; }
			}
		}
		
		// Change the voxel.
		change(sourceID, targetID, x, y, z, r);
	}
	
	/**
	 * Calculates energy change to decide if a voxel is flipped.
	 * 
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param r  a random number
	 */
	void change(int sourceID, int targetID, int x, int y, int z, double r) {
		// Calculate energy change.
		double dH = 0;
		dH += getDeltaAdhesion(sourceID, targetID, x, y, z);
		dH += getDeltaVolume(sourceID, targetID);
		dH += getDeltaSurface(sourceID, targetID, x, y, z);
		
		// Calculate probability.
		double p;
		if (dH < 0) { p = 1; }
		else { p = Math.exp(-dH/TEMPERATURE); }
		
		if (r < p) {
			IDS[z][x][y] = targetID;
			if (HAS_REGIONS) { REGIONS[z][x][y] = (targetID == 0 ? Region.UNDEFINED.ordinal() : Region.DEFAULT.ordinal()); }
			
			if (sourceID > 0) { ((PottsLocation)getCell(sourceID).getLocation()).remove(x, y, z); }
			if (targetID > 0) { ((PottsLocation)getCell(targetID).getLocation()).add(x, y, z); }
		}
	}
	
	/**
	 * Flips connected voxel from source to target region based on Boltzmann probability.
	 *
	 * @param id  the voxel id
	 * @param sourceRegion  the region of the source voxel
	 * @param targetRegion  the region of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param r  a random number
	 */
	void flip(int id, int sourceRegion, int targetRegion, int x, int y, int z, double r) {
		// Check connectivity of source.
		if (sourceRegion > Region.DEFAULT.ordinal()) {
			boolean candidateConnected = getConnectivity(getNeighborhood(id, sourceRegion, x, y, z), false);
			if (!candidateConnected) { return; }
		}
		
		// Check connectivity of target.
		if (targetRegion > Region.DEFAULT.ordinal()) {
			boolean targetConnected = getConnectivity(getNeighborhood(id, targetRegion, x, y, z), false);
			if (!targetConnected) { return; }
		}
		
		// Change the voxel region.
		change(id, sourceRegion, targetRegion, x, y, z, r);
	}
	
	/**
	 * Calculates energy change to decide if a voxel region is flipped.
	 * 
	 * @param id  the voxel id
	 * @param sourceRegion  the region of the source voxel
	 * @param targetRegion  the region of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @param r  a random number
	 */
	void change(int id, int sourceRegion, int targetRegion, int x, int y, int z, double r) {
		// Calculate energy change.
		double dH = 0;
		dH += getDeltaAdhesion(id, sourceRegion, targetRegion, x, y, z);
		dH += getDeltaVolume(id, sourceRegion, targetRegion);
		dH += getDeltaSurface(id, sourceRegion, targetRegion, x, y, z);
		
		// Calculate probability.
		double p;
		if (dH < 0) { p = 1; }
		else { p = Math.exp(-dH/TEMPERATURE); }
		
		if (r < p) {
			REGIONS[z][x][y] = targetRegion;
			PottsCell c = getCell(id);
			((PottsLocation)c.getLocation()).remove(Region.values()[sourceRegion], x, y, z);
			((PottsLocation)c.getLocation()).add(Region.values()[targetRegion], x, y, z);
		}
	}
	
	/**
	 * Gets the {@link arcade.core.agent.cell.Cell} object for the given id.
	 *
	 * @param id  the cell id
	 * @return  the {@link arcade.core.agent.cell.Cell} object, {@code null} if id is zero
	 */
	PottsCell getCell(int id) {
		if (id > 0) { return (PottsCell)grid.getObjectAt(id); }
		else { return null; }
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
	abstract double getAdhesion(int id, int x, int y, int z);
	
	/**
	 * Gets adhesion energy for a given voxel region.
	 *
	 * @param id  the voxel id
	 * @param region  the voxel region
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the energy
	 */
	abstract double getAdhesion(int id, int region, int x, int y, int z);
	
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
	 * Gets change in adhesion energy for region.
	 *
	 * @param id  the voxel id
	 * @param sourceRegion  the region of the source voxel
	 * @param targetRegion  the region of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaAdhesion(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
		double source = getAdhesion(id, sourceRegion, x, y, z);
		double target = getAdhesion(id, targetRegion, x, y, z);
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
		PottsCell c = getCell(id);
		double volume = c.getVolume();
		double targetVolume = c.getTargetVolume();
		double lambda = c.getLambda(Term.VOLUME);
		return lambda * Math.pow((volume - targetVolume + change), 2);
	}
	
	/**
	 * Gets volume energy for a given change in volume for region.
	 *
	 * @param id  the voxel id
	 * @param t  the voxel region 
	 * @param change  the change in volume
	 * @return  the energy
	 */
	double getVolume(int id, int t, int change) {
		Region region = Region.values()[t];
		if (id == 0 || region == Region.DEFAULT) { return 0; }
		PottsCell c = getCell(id);
		double volume = c.getVolume(region);
		double targetVolume = c.getTargetVolume(region);
		double lambda = c.getLambda(Term.VOLUME, region);
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
	 * Gets change in volume energy for region.
	 *
	 * @param id  the voxel id
	 * @param sourceRegion  the region of the source voxel
	 * @param targetRegion  the region of the source voxel
	 * @return  the change in energy
	 */
	double getDeltaVolume(int id, int sourceRegion, int targetRegion) {
		double source = getVolume(id, sourceRegion, -1) - getVolume(id, sourceRegion, 0);
		double target = getVolume(id, targetRegion, 1) - getVolume(id, targetRegion, 0);
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
		PottsCell c = getCell(id);
		double surface = c.getSurface();
		double targetSurface = c.getTargetSurface();
		double lambda = c.getLambda(Term.SURFACE);
		return lambda * Math.pow((surface - targetSurface + change), 2);
	}
	
	/**
	 * Gets the surface energy for a given change in surface for region.
	 *
	 * @param id  the voxel id
	 * @param t  the voxel region   
	 * @param change  the change in surface
	 * @return  the energy
	 */
	double getSurface(int id, int t, int change) {
		Region region = Region.values()[t];
		if (id == 0 || region == Region.DEFAULT) { return 0; }
		PottsCell c = getCell(id);
		double surface = c.getSurface(region);
		double targetSurface = c.getTargetSurface(region);
		double lambda = c.getLambda(Term.SURFACE, region);
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
		int[] changes = calculateChange(sourceID, targetID, x, y, z);
		double source = getSurface(sourceID, changes[0]) - getSurface(sourceID, 0);
		double target = getSurface(targetID, changes[1]) - getSurface(targetID, 0);
		return target + source;
	}
	
	/**
	 * Gets change in surface energy for region.
	 *
	 * @param id  the voxel id
	 * @param sourceRegion  the id of the source voxel
	 * @param targetRegion  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the change in energy
	 */
	double getDeltaSurface(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
		int[] changes = calculateChange(id, sourceRegion, targetRegion, x, y, z);
		double source = getSurface(id, sourceRegion, changes[0]) - getSurface(id, sourceRegion, 0);
		double target = getSurface(id, targetRegion, changes[1]) - getSurface(id, targetRegion, 0);
		return target + source;
	}
	
	/**
	 * Calculates change in surface.
	 *
	 * @param sourceID  the id of the source voxel
	 * @param targetID  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the list of changes in source and target
	 */
	abstract int[] calculateChange(int sourceID, int targetID, int x, int y, int z);
	
	/**
	 * Calculates change in surface for region
	 *
	 * @param id  the voxel id
	 * @param sourceRegion  the id of the source voxel
	 * @param targetRegion  the id of the target voxel
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the list of changes in source and target
	 */
	abstract int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z);
	
	/**
	 * Gets neighborhood for the given voxel.
	 *
	 * @param id  the voxel id
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	abstract boolean[][][] getNeighborhood(int id, int x, int y, int z);
	
	/**
	 * Gets neighborhood for the given voxel region.
	 *
	 * @param id  the voxel id
	 * @param region  the voxel region
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	abstract boolean[][][] getNeighborhood(int id, int region, int x, int y, int z);
	
	/**
	 * Determines connectivity of given neighborhood.
	 *
	 * @param array  the array of neighbors
	 * @param zero  {@code true} if the location has a zero id, {@code false} otherwise
	 * @return  {@code true} if simply connected, {@code false} otherwise
	 */
	abstract boolean getConnectivity(boolean[][][] array, boolean zero);
	
	/**
	 * Gets unique IDs adjacent to given voxel.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the list of unique IDs
	 */
	abstract HashSet<Integer> getUniqueIDs(int x, int y, int z);
	
	/**
	 * Gets unique regions adjacent to given voxel.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 * @return  the list of unique regions
	 */
	abstract HashSet<Integer> getUniqueRegions(int x, int y, int z);
}