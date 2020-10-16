package arcade.agent.cell;

import arcade.env.loc.Location;
import static arcade.agent.cell.Cell.*;

public class CellFactory3D extends CellFactory {
	public CellFactory3D() { super(); }
	
	Cell makeCell(int id, int pop, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion) {
		return new PottsCell3D(id, pop, location, criticals, lambdas, adhesion);
	}
	
	Cell makeCell(int id, int pop, Location location,
				  double[] criticals, double[] lambdas, double[] adhesion, int tags,
				  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		return new PottsCell3D(id, pop, STATE_PROLIFERATIVE, 0, location,
				criticals, lambdas, adhesion, tags,
				criticalsTag, lambdasTag, adhesionsTag);
	}
}