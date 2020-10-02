package arcade.env.loc;

import java.util.ArrayList;
import ec.util.MersenneTwisterFast;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static arcade.env.loc.Location.Voxel;

public abstract class LocationFactory {
	/** Length (x direction) of array */
	final int LENGTH;
	
	/** Width (y direction) of array */
	final int WIDTH;
	
	/** Depth (z direction) of array */
	final int HEIGHT;
	
	/** List of available locations */
	final ArrayList<Voxel> availableLocations;
	
	/** List of unavailable locations */
	final ArrayList<Voxel> unavailableLocations;
	
	/**
	 * Creates a factory for making {@link arcade.env.loc.Location} instances.
	 *
	 * @param length  the length of array (x direction)
	 * @param width  the width of array (y direction)
	 * @param height  the height of array (z direction)
	 */
	public LocationFactory(int length, int width, int height) {
		this.LENGTH = length;
		this.WIDTH = width;
		this.HEIGHT = height;
		
		availableLocations = new ArrayList<>();
		unavailableLocations = new ArrayList<>();
	}
	
	/**
	 * Creates a list of available center locations.
	 * 
	 * @param populations  the list of populations
	 */
	public abstract void makeCenters(ArrayList<MiniBox> populations);
	
	/**
	 * Creates a location around given center point.
	 * 
	 * @param population  the population settings
	 * @param center  the center voxel
	 * @param random  the random number generator
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	abstract Location makeLocation(MiniBox population, Voxel center, MersenneTwisterFast random);
	
	/**
	 * Gets locations for the given population.
	 * 
	 * @param population  the population settings
	 * @param random  the random number generator
	 * @return  a list of {@link arcade.env.loc.Location} objects
	 */
	public ArrayList<Location> getLocations(MiniBox population, MersenneTwisterFast random) {
		ArrayList<Voxel> assignedLocations = new ArrayList<>();
		ArrayList<Location> locations = new ArrayList<>();
		
		int n = availableLocations.size() + unavailableLocations.size();
		int m = (int)Math.round(population.getDouble("FRACTION")*n);
		Simulation.shuffle(availableLocations, random);
		
		int nm = Math.min(availableLocations.size(), m);
		for (int i = 0; i < nm; i++) {
			Voxel center = availableLocations.get(i);
			locations.add(makeLocation(population, center, random));
			assignedLocations.add(center);
		}
		
		availableLocations.removeAll(assignedLocations);
		unavailableLocations.addAll(assignedLocations);
		
		return locations;
	}
}