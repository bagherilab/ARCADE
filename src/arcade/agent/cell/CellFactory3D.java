package arcade.agent.cell;

import arcade.env.loc.Location;

public class CellFactory3D extends CellFactory {
	public CellFactory3D() { super(); }
	
	Cell makeCell(int id, int pop, int age, int state, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion) {
		return new PottsCell3D(id, pop, state, age, location,
				criticals, lambdas, adhesion, 0, null, null, null);
	}
	
	Cell makeCell(int id, int pop, int age, int state, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion, int tags,
				  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		return new PottsCell3D(id, pop, state, age, location,
				criticals, lambdas, adhesion, tags,
				criticalsTag, lambdasTag, adhesionsTag);
	}
}