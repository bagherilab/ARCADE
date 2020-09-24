package arcade.agent.cell;

import arcade.env.loc.Location;

public class PottsCell3D extends PottsCell {
	/** Multiplier for calculating surface area from volume */
	public static final double SURFACE_VOLUME_MULTIPLIER = Math.cbrt(36*Math.PI)*2;
	
	/**
	 * Creates a {@code PottsCell} agent for 3D simulations.
	 * <p>
	 * The default population is 1, state is proliferative, and age is 0.
	 * The cell is created with no tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index   
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	public PottsCell3D(int id, int pop, Location location,
					   double[] criticals, double[] lambdas, double[] adhesion) {
		super(id, pop, location, criticals, lambdas, adhesion);
	}
	
	/**
	 * Creates a {@code PottsCell} agent for 3D simulations.
	 * <p>
	 * The default state is proliferative and age is 0. 
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param criticalsTag  the list of tagged critical values
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 */
	public PottsCell3D(int id, int pop, Location location,
					   double[] criticals, double[] lambdas, double[] adhesion, int tags,
					   double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		super(id, pop, location, criticals, lambdas, adhesion, tags, criticalsTag, lambdasTag, adhesionsTag);
	}
	
	/**
	 * Creates a {@code PottsCell} agent for 3D simulations.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param criticalsTag  the list of tagged critical values
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
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