package arcade.agent.cell;

import java.util.EnumMap;
import arcade.env.loc.Location;
import static arcade.sim.Potts.Term;

public class PottsCell3D extends PottsCell {
	/** Multiplier for calculating surface area from volume */
	public static final double SURFACE_VOLUME_MULTIPLIER = Math.cbrt(36*Math.PI)*2;
	
	/**
	 * Creates a {@code PottsCell} agent for 3D simulations.
	 * <p>
	 * The default state is proliferative and age is 0.
	 * The cell is created with no tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	public PottsCell3D(int id, int pop, Location location,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
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
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param criticalsTag  the map of tagged critical values
	 * @param lambdasTag  the map of tagged lambda multipliers
	 * @param adhesionTag  the map of tagged adhesion values
	 */
	public PottsCell3D(int id, int pop, Location location,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					 EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
					 EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
		super(id, pop, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
	}
	
	/**
	 * Creates a {@code PottsCell} agent for 3D simulations.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param hasTags  {@code true} if the cell has tags, {@code false} otherwise
	 * @param criticalsTag  the map of tagged critical values
	 * @param lambdasTag  the map of tagged lambda multipliers
	 * @param adhesionTag  the map of tagged adhesion values
	 */
	public PottsCell3D(int id, int pop, State state, int age, Location location, boolean hasTags,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					 EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
					 EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
		super(id, pop, state, age, location, hasTags, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
	}
	
	public PottsCell make(int id, State state, Location location) {
		return new PottsCell3D(id, pop, state, 0, location, hasTags,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
	}
	
	public double convert(double volume) {
		return SURFACE_VOLUME_MULTIPLIER*Math.pow(volume, 2./3);
	}
}