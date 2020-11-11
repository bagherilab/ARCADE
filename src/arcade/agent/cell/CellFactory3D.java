package arcade.agent.cell;

import java.util.EnumMap;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.cell.Cell.Region;
import static arcade.sim.Potts.Term;

public class CellFactory3D extends CellFactory {
	public CellFactory3D() { super(); }
	
	Cell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
		return new PottsCell3D(id, pop, state, age, location, false, parameters,
				criticals, lambdas, adhesion, null, null, null);
	}
	
	Cell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
				  EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
				  EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		return new PottsCell3D(id, pop, state, age, location, true, parameters,
				criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
	}
}