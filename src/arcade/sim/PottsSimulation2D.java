package arcade.sim;

import java.util.ArrayList;
import arcade.agent.cell.Cell;
import arcade.util.MiniBox;
import static arcade.env.loc.Location.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	public Potts makePotts() { return new Potts2D(series, agents); }
	
	ArrayList<arcade.env.loc.Location.Voxel> makeCenters() {
		// TODO
		return null;
	}
	
	Cell makeCell(int id, MiniBox population, Voxel center) {
		// TODO
		return null;
	}
}