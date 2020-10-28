package arcade.agent.cell;

import arcade.env.loc.Location;

public class CellFactory2D extends CellFactory {
	public CellFactory2D() { super(); }
	
	Cell makeCell(int id, int pop, int age, int state, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion) {
		return new PottsCell2D(id, pop, state, age, location,
				criticals, lambdas, adhesion, 0, null, null, null);
	}
	
	Cell makeCell(int id, int pop, int age, int state, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion, int tags,
				  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		return new PottsCell2D(id, pop, state, age, location,
				criticals, lambdas, adhesion, tags,
				criticalsTag, lambdasTag, adhesionsTag);
	}
}