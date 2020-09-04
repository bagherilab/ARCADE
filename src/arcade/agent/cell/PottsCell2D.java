package arcade.agent.cell;

import arcade.env.loc.Location;

public class PottsCell2D extends PottsCell {
	/** Multiplier for calculating surface area from volume */
	public static final double SURFACE_VOLUME_MULTIPLIER = 2*Math.sqrt(Math.PI)*1.5;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Cell agent used for 2D simulations.
	 */
	public PottsCell2D(int id, int pop, Location location,
					   double[] lambdas, double[] adhesion) {
		super(id, pop, location, lambdas, adhesion);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Cell agent used for 2D simulations.
	 */
	public PottsCell2D(int id, int pop, Location location,
					   double[] lambdas, double[] adhesion, int tags,
					   double[][] lambdasTag, double[][] adhesionsTag) {
		super(id, pop, location, lambdas, adhesion, tags, lambdasTag, adhesionsTag);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Cell agent used for 2D simulations.
	 */
	public PottsCell2D(int id, int pop, int state, int age, Location location,
					   double[] lambdas, double[] adhesion, int tags,
					   double[][] lambdasTag, double[][] adhesionsTag) {
		super(id, pop, state, age, location, lambdas, adhesion, tags, lambdasTag, adhesionsTag);
	}
	
	public PottsCell makeCell(int id, int state, Location location) {
		return new PottsCell2D(id, pop, state, 0, location,
				lambdas, adhesion, tags, lambdasTag, adhesionTag);
	}
	
	public double convert(double volume) {
		return SURFACE_VOLUME_MULTIPLIER*Math.sqrt(volume);
	}
}