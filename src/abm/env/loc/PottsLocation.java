package abm.env.loc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import ec.util.MersenneTwisterFast;
import abm.sim.Simulation;
import static abm.sim.Potts.*;

public class PottsLocation implements Location {
	/** Difference between split voxel numbers */
	final static private int BALANCE_DIFFERENCE = 2;
	
	/** List of voxels for the location */
	ArrayList<Voxel> voxels;
	
	/** Location split directions */
	enum Direction {
		/** Direction along the x axis (y = 0) */
		X_DIRECTION,
		
		/** Direction along the y axis (x = 0) */
		Y_DIRECTION,
		
		/** Direction along the positive xy axis (x = y) */
		POSITIVE_XY,
		
		/** Direction along the negative xy axis (x  = -y) */
		NEGATIVE_XY
	}
	
	/**
	 * Creates a {@code PottsLocation} for a list of voxels.
	 *
	 * @param voxels  the list of voxels
	 */
	public PottsLocation(ArrayList<Voxel> voxels) {
		this.voxels = voxels;
	}
	
	/**
	 * Gets the size of the location.
	 * 
	 * @return  the location size (in voxels)
	 */
	public int getSize() { return voxels.size(); }
	
	public void add(int x, int y, int z) {
		Voxel voxel = new Voxel(x, y, z);
		if (!voxels.contains(voxel)) { voxels.add(voxel); }
	}
	
	public void remove(int x, int y, int z) { voxels.remove(new Voxel(x, y, z)); }
	
	public void update(int[][][] array, int id) {
		for (Voxel voxel : voxels) { array[voxel.z][voxel.x][voxel.y] = id; }
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The location are split along the direction with the shortest diameter.
	 * The lists of locations are guaranteed to be connected, and generally will
	 * be balanced in size.
	 * One of the splits is assigned to the current location and the other is
	 * returned.
	 */
	public PottsLocation split(MersenneTwisterFast random) {
		// Get center voxel.
		Voxel center = getCenter();
		
		// Initialize lists of split voxels.
		ArrayList<Voxel> voxelsA = new ArrayList<>();
		ArrayList<Voxel> voxelsB = new ArrayList<>();
		
		// Get split direction.
		Direction direction = getDirection(random);
		splitVoxels(direction, voxelsA, voxelsB, center, random);
		
		// Ensure that voxel split are connected and balanced.
		connectVoxels(voxelsA, voxelsB, random);
		balanceVoxels(voxelsA, voxelsB, random);
		
		// Select one split to keep for this location and return the other.
		if (random.nextDouble() < 0.5) {
			voxels = voxelsA;
			return new PottsLocation(voxelsB);
		} else {
			voxels = voxelsB;
			return new PottsLocation(voxelsA);
		}
	}
	
	public Voxel getCenter() { return new Voxel(getCenterX(), getCenterY(), getCenterZ()); }
	
	/**
	 * Gets the x coordinate of the voxel at the center of the location.
	 *
	 * @return  the x coordinate
	 */
	int getCenterX() {
		double x = 0;
		for (Voxel voxel : voxels) { x += voxel.x; }
		return (int)Math.round(x/voxels.size());
	}
	
	/**
	 * Gets the y coordinate of the voxel at the center of the location.
	 *
	 * @return  the y coordinate
	 */
	int getCenterY() {
		double y = 0;
		for (Voxel voxel : voxels) { y += voxel.y; }
		return (int)Math.round(y/voxels.size());
	}
	
	/**
	 * Gets the z coordinate of the voxel at the center of the location.
	 *
	 * @return  the z coordinate
	 */
	int getCenterZ() {
		double z = 0;
		for (Voxel voxel : voxels) { z += voxel.z; }
		return (int)Math.round(z/voxels.size());
	}
	
	/**
	 * Gets the direction of the shortest diameter in the location.
	 * 
	 * @param random  the seeded random number generator
	 * @return  the direction of the shortest diameter
	 */
	Direction getDirection(MersenneTwisterFast random) {
		Voxel center = getCenter();
		
		EnumMap<Direction, Integer> minValueMap = new EnumMap<>(Direction.class);
		EnumMap<Direction, Integer> maxValueMap = new EnumMap<>(Direction.class);
		EnumMap<Direction, Integer> diameterMap = new EnumMap<>(Direction.class);
		
		// Initialized entries into direction maps.
		for (Direction direction : Direction.values()) {
			minValueMap.put(direction, Integer.MAX_VALUE);
			maxValueMap.put(direction, Integer.MIN_VALUE);
		}
		
		Direction dir;
		int v;
		
		// Iterate through all the voxels for the location to update minimum and
		// maximum values in each direction.
		for (Voxel voxel : voxels) {
			int i = voxel.x - center.x;
			int j = voxel.y - center.y;
			
			if (j == 0) { dir = Direction.X_DIRECTION; v = i; }
			else if (i == 0) { dir = Direction.Y_DIRECTION; v = j; }
			else if (i == j) { dir = Direction.POSITIVE_XY; v = i; }
			else if (i == -j) { dir = Direction.NEGATIVE_XY; v = i; }
			else { continue; }
			
			if (v > maxValueMap.get(dir)) { maxValueMap.put(dir, v); }
			if (v < minValueMap.get(dir)) { minValueMap.put(dir, v); }
		}
		
		ArrayList<Direction> directions = new ArrayList<>();
		
		// Calculate diameter in each direction and get minimum diameter.
		int diameter;
		int minimumDiameter = Integer.MAX_VALUE;
		for (Direction direction : Direction.values()) {
			diameter = maxValueMap.get(direction) - minValueMap.get(direction);
			diameterMap.put(direction, diameter);
			if (diameter < minimumDiameter) { minimumDiameter = diameter; }
		}
		
		// Find all directions with the minimum diameter.
		for (Direction direction : Direction.values()) {
			if (diameterMap.get(direction) == minimumDiameter) { directions.add(direction); }
		}
		
		// Randomly select one direction.
		return directions.get(random.nextInt(directions.size()));
	}
	
	/**
	 * Splits the voxels in the location along a given direction.
	 * 
	 * @param direction  the direction of the shortest diameter
	 * @param voxelsA  the container list for the first half of the split
	 * @param voxelsB  the container list for the second half of the split
	 * @param center  the center voxel
	 * @param random  the seeded random number generator
	 */
	void splitVoxels(Direction direction, ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
					 Voxel center, MersenneTwisterFast random) {
		for (Voxel voxel : voxels) {
			switch (direction) {
				case X_DIRECTION:
					if (voxel.x < center.x) { voxelsA.add(voxel); }
					else if (voxel.x > center.x) { voxelsB.add(voxel); }
					else {
						if (random.nextDouble() > 0.5) { voxelsA.add(voxel); }
						else { voxelsB.add(voxel); }
					}
					break;
				case Y_DIRECTION:
					if (voxel.y < center.y) { voxelsA.add(voxel); }
					else if (voxel.y > center.y) { voxelsB.add(voxel); }
					else {
						if (random.nextDouble() > 0.5) { voxelsA.add(voxel); }
						else { voxelsB.add(voxel); }
					}
					break;
				case POSITIVE_XY:
					if (voxel.x - center.x > voxel.y - center.y) { voxelsA.add(voxel); }
					else if (voxel.x - center.x < voxel.y - center.y) { voxelsB.add(voxel); }
					else {
						if (random.nextDouble() > 0.5) { voxelsA.add(voxel); }
						else { voxelsB.add(voxel); }
					}
					break;
				case NEGATIVE_XY:
					if (voxel.x - center.x > center.y - voxel.y) { voxelsA.add(voxel); }
					else if (voxel.x - center.x < center.y - voxel.y) { voxelsB.add(voxel); }
					else {
						if (random.nextDouble() > 0.5) { voxelsA.add(voxel); }
						else { voxelsB.add(voxel); }
					}
					break;
			}
		}
	}
	
	/**
	 * Connects voxels in the splits.
	 * <p>
	 * Checks that the voxels in each split are connected.
	 * If not, then move the unconnected voxels into the other split.
	 * 
	 * @param voxelsA  the list for the first half of the split
	 * @param voxelsB  the list for the second half of the split
	 * @param random  the seeded random number generator
	 */
	static void connectVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
							  MersenneTwisterFast random) {
		// Check that both coordinate lists are simply connected.
		ArrayList<Voxel> unconnectedA = checkVoxels(voxelsA, random, true);
		ArrayList<Voxel> unconnectedB = checkVoxels(voxelsB, random, true);
		
		// If either coordinate list is not connected, attempt to connect them
		// by adding in the unconnected coordinates of the other list.
		while (unconnectedA != null || unconnectedB != null) {
			ArrayList<Voxel> unconnectedAB = null;
			ArrayList<Voxel> unconnectedBA = null;
			
			if (unconnectedA != null) { voxelsB.addAll(unconnectedA); }
			unconnectedBA = checkVoxels(voxelsB, random, true);
			
			if (unconnectedB != null) { voxelsA.addAll(unconnectedB); }
			unconnectedAB = checkVoxels(voxelsA, random,true);

			unconnectedA = unconnectedAB;
			unconnectedB = unconnectedBA;
		}
	}
	
	/**
	 * Balances voxels in the splits.
	 * <p>
	 * Checks that the number of voxels in each split are within a certain
	 * difference.
	 * If not, then add voxels from the larger split into the smaller split
	 * such that both splits are still connected.
	 * For small split sizes, there may not be a valid split that is both
	 * connected and within the difference; in these cases, connectedness is
	 * prioritized and the splits are returned not balanced.
	 *
	 * @param voxelsA  the list for the first half of the split
	 * @param voxelsB  the list for the second half of the split
	 * @param random  the seeded random number generator
	 */
	static void balanceVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
							  MersenneTwisterFast random) {
		int nA = voxelsA.size();
		int nB = voxelsB.size();
		
		while (Math.abs(nA - nB) > BALANCE_DIFFERENCE) {
			ArrayList<Voxel> fromVoxels, toVoxels;
			
			if (nA  > nB) {
				fromVoxels = voxelsA;
				toVoxels = voxelsB;
			}
			else {
				fromVoxels = voxelsB;
				toVoxels = voxelsA;
			}
			
			// Get all valid neighbor voxels.
			LinkedHashSet<Voxel> neighborSet = new LinkedHashSet<>();
			for (Voxel voxel : toVoxels) {
				for (int i = 0; i < 4; i++) {
					Voxel neighbor = new Voxel( voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z);
					if (!toVoxels.contains(neighbor)) { neighborSet.add(neighbor); }
				}
			}
			
			ArrayList<Voxel> neighborList = new ArrayList<>(neighborSet);
			Simulation.shuffle(neighborList, random);
			
			// Select a neighbor to move from one list to the other.
			boolean added = false;
			ArrayList<Voxel> invalidCoords = new ArrayList<>();
			for (Voxel voxel : neighborList) {
				if (fromVoxels.contains(voxel)) {
					toVoxels.add(voxel);
					fromVoxels.remove(voxel);
					
					// Check that removal of coordinate does not cause the list
					// to become unconnected.
					ArrayList<Voxel> unconnected = checkVoxels(fromVoxels, random,false);
					if (unconnected == null) {
						added = true;
						break;
					}
					else {
						fromVoxels.add(voxel);
						toVoxels.remove(voxel);
						invalidCoords.add(voxel);
					}
				}
			}
			
			if (!added) {
				toVoxels.addAll(invalidCoords);
				fromVoxels.removeAll(invalidCoords);
				connectVoxels(voxelsA, voxelsB, random);
				break;
			}
			
			nA = voxelsA.size();
			nB = voxelsB.size();
		}
	}
	
	/**
	 * Checks voxels in the list for connectedness.
	 * <p>
	 * All the connected voxels from a random starting voxel are found and
	 * marked as visited.
	 * If there are no remaining unvisited voxels, then the list is fully
	 * connected.
	 * If there are, then the smaller of the visited or unvisited lists is
	 * returned.
	 * <p>
	 * Some voxel lists may have more than one unconnected section.
	 * 
	 * @param voxels  the list of voxels
	 * @param random  the seeded random number generator 
	 * @param update  {@code true} if the voxel list should be updated, {@code false} otherwise
	 * @return  a list of unconnected voxels, {@code null} if the list is connected
	 */
	static ArrayList<Voxel> checkVoxels(ArrayList<Voxel> voxels,
												MersenneTwisterFast random, boolean update) {
		ArrayList<Voxel> unvisited = new ArrayList<>(voxels);
		ArrayList<Voxel> visited = new ArrayList<>();
		ArrayList<Voxel> nextList;
		LinkedHashSet<Voxel> nextSet;
		LinkedHashSet<Voxel> currSet = new LinkedHashSet<>();
		
		currSet.add(unvisited.get(random.nextInt(unvisited.size())));
		int currSize = currSet.size();
		
		while (currSize > 0) {
			nextSet = new LinkedHashSet<>();
			
			// Iterate through each coordinate in current coordinate set.
			for (Voxel voxel : currSet) {
				nextList = new ArrayList<>();
				
				// Iterate through each connected direction from current voxel
				// and add to neighbor list if it exists.
				for (int i = 0; i < 4; i++) {
					Voxel neighbor = new Voxel(voxel.x + MOVES_X[i], voxel.y + MOVES_Y[i], voxel.z);
					if (unvisited.contains(neighbor)) { nextList.add(neighbor); }
				}
				
				// Updated next voxel set with list of neighbors.
				nextSet.addAll(nextList);
				visited.add(voxel);
				unvisited.remove(voxel);
			}
			
			currSet = nextSet;
			currSize = currSet.size();
		}
		
		// If not all coordinates have been visited, then the list of
		// coordinates is not connected. 
		if (unvisited.size() != 0) {
			if (unvisited.size() > visited.size()) {
				if (update) { voxels.removeAll(visited); }
				return visited;
			} else {
				if (update) { voxels.removeAll(unvisited); }
				return unvisited;
			}
		}
		else { return null; }
	}
	
	/**
	 * Calculates perimeter of location.
	 * 
	 * @param id  the location id
	 * @param array  the potts array
	 * @return  the perimeter
	 */
	public int calculatePerimeter(int id, int[][][] array) {
		int perimeter = 0;
		
		for (Voxel voxel : voxels) {
			for (int i = 0; i < 4; i++) {
				if (array[voxel.z][voxel.x + MOVES_X[i]][voxel.y + MOVES_Y[i]] != id) { perimeter++; }
			}
		}
		
		return perimeter;
	}
}