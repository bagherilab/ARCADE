package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.Cell;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import static arcade.core.agent.cell.Cell.State;
import static arcade.core.agent.cell.Cell.Region;
import static arcade.potts.sim.Potts.Term;

public class CellFactory2D extends CellFactory {
	public CellFactory2D() { super(); }
	
	Cell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
		return new PottsCell2D(id, pop, state, age, location, false, parameters,
				criticals, lambdas, adhesion, null, null, null);
	}
	
	Cell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
				  EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
				  EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		return new PottsCell2D(id, pop, state, age, location, true, parameters,
				criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
	}
}