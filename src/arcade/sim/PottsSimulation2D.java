package arcade.sim;

import java.util.ArrayList;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import static arcade.agent.cell.Cell.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	Potts makePotts() { return new Potts2D(series); }
	
	LocationFactory makeLocations() {
		LocationFactory factory = new LocationFactory2D(series._length, series._width, series._height);
		factory.makeCenters(new ArrayList<>(series._populations.values()));
		return factory;
	}
	
	Cell makeCell(int id, int pop, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion) {
		return new PottsCell2D(id, pop, location, criticals, lambdas, adhesion);
	}
	
	Cell makeCell(int id, int pop, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion, int tags,
				  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		return new PottsCell2D(id, pop, STATE_PROLIFERATIVE, 0, location,
				criticals, lambdas, adhesion, tags,
				criticalsTag, lambdasTag, adhesionsTag);
	}
}