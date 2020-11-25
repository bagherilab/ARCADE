package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import static arcade.core.util.Enums.State;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;

public class PottsCellFactory3D extends PottsCellFactory {
	public PottsCellFactory3D() { super(); }
	
	PottsCell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
		return new PottsCell3D(id, pop, state, age, location, false, parameters,
				criticals, lambdas, adhesion, null, null, null);
	}
	
	PottsCell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
				  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
				  EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
				  EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		return new PottsCell3D(id, pop, state, age, location, true, parameters,
				criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
	}
}