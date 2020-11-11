package arcade.agent.cell;

import java.util.EnumMap;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Potts.Term;

public class PottsCell2D extends PottsCell {
	/** Multiplier for calculating surface area from volume */
	public static final double SURFACE_VOLUME_MULTIPLIER = 2*Math.sqrt(Math.PI)*1.5;
	
	/**
	 * Creates a {@code PottsCell} agent for 2D simulations.
	 * <p>
	 * The default state is proliferative and age is 0.
	 * The cell is created with no regions.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	public PottsCell2D(int id, int pop, Location location, MiniBox parameters,
					   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
		super(id, pop, location, parameters, criticals, lambdas, adhesion);
	}
	
	/**
	 * Creates a {@code PottsCell} agent for 2D simulations.
	 * <p>
	 * The default state is proliferative and age is 0.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param criticalsRegion  the map of critical values for regions
	 * @param lambdasRegion  the map of lambda multipliers for regions
	 * @param adhesionRegion  the map of adhesion values for regions
	 */
	public PottsCell2D(int id, int pop, Location location, MiniBox parameters,
					   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					   EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
					   EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		super(id, pop, location, parameters, criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
	}
	
	/**
	 * Creates a {@code PottsCell} agent for 2D simulations.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param hasRegions  {@code true} if the cell has regions, {@code false} otherwise
	 * @param criticalsRegion  the map of critical values for regions
	 * @param lambdasRegion  the map of lambda multipliers for regions
	 * @param adhesionRegion  the map of adhesion values for regions
	 */
	public PottsCell2D(int id, int pop, State state, int age, Location location, boolean hasRegions, MiniBox parameters,
					   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					   EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
					   EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		super(id, pop, state, age, location, hasRegions, parameters, criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
	}
	
	public PottsCell make(int id, State state, Location location) {
		return new PottsCell2D(id, pop, state, 0, location, hasRegions, parameters,
				criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
	}
	
	public double convert(double volume) {
		return SURFACE_VOLUME_MULTIPLIER*Math.sqrt(volume);
	}
}