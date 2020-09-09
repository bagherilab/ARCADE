package arcade.agent.cell;

import arcade.env.loc.Location;

public class PottsCell3D extends PottsCell {
	/** Multiplier for calculating surface area from volume */
	public static final double SURFACE_VOLUME_MULTIPLIER = Math.cbrt(36*Math.PI)*2;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Cell agent used for 3D simulations.
	 */
	public PottsCell3D(int id, int pop, Location location,
					   double[] criticals, double[] lambdas, double[] adhesion) {
		super(id, pop, location, criticals, lambdas, adhesion);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Cell agent used for 3D simulations.
	 */
	public PottsCell3D(int id, int pop, Location location,
					   double[] criticals, double[] lambdas, double[] adhesion, int tags,
					   double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		super(id, pop, location, criticals, lambdas, adhesion, tags, criticalsTag, lambdasTag, adhesionsTag);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Cell agent used for 3D simulations.
	 */
	public PottsCell3D(int id, int pop, int state, int age, Location location,
					   double[] criticals, double[] lambdas, double[] adhesion, int tags,
					   double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		super(id, pop, state, age, location, criticals, lambdas, adhesion, tags, criticalsTag, lambdasTag, adhesionsTag);
	}
	
	public PottsCell make(int id, int state, Location location) {
		return new PottsCell3D(id, pop, state, 0, location,
				criticals, lambdas, adhesion, tags, criticalsTag, lambdasTag, adhesionTag);
	}
	
	public double convert(double volume) {
		return SURFACE_VOLUME_MULTIPLIER*Math.pow(volume, 2./3);
	}
}