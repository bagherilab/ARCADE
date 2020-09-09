package arcade.sim;

import java.util.ArrayList;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import static arcade.env.loc.Location.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	public void setupPotts() { potts = new Potts2D(series, agents); }
	
	ArrayList<ArrayList<Voxel>> makeAllLocations() {
		// TODO
		return null;
	}
	
	Location makeLocation(ArrayList<Voxel> voxels) {
		// TODO
		return null;
	}
	
	Cell makeCell(int id, int pop, Location location) {
		// TODO
		return null;
	}
}