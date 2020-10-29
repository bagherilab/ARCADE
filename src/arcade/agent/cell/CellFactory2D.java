package arcade.agent.cell;

import java.util.EnumMap;
import arcade.env.loc.Location;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.cell.Cell.Tag;
import static arcade.sim.Potts.Term;

public class CellFactory2D extends CellFactory {
	public CellFactory2D() { super(); }
	
	Cell makeCell(int id, int pop, int age, State state, Location location,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
		return new PottsCell2D(id, pop, state, age, location, false,
				criticals, lambdas, adhesion, null, null, null);
	}
	
	Cell makeCell(int id, int pop, int age, State state, Location location,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
				  EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
				  EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
		return new PottsCell2D(id, pop, state, age, location, true,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
	}
}