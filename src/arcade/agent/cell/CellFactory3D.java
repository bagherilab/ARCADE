package arcade.agent.cell;

import java.util.EnumMap;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.cell.Cell.Tag;
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
				  EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
				  EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
		return new PottsCell3D(id, pop, state, age, location, true, parameters,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
	}
}