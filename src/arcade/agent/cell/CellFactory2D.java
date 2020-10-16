package arcade.agent.cell;

import arcade.env.loc.Location;
import static arcade.agent.cell.Cell.*;

public class CellFactory2D extends CellFactory {
	public CellFactory2D() { super(); }
	
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