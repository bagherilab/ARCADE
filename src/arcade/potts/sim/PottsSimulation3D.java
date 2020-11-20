package arcade.sim;

import arcade.agent.cell.*;
import arcade.env.loc.*;

public class PottsSimulation3D extends PottsSimulation {
	public PottsSimulation3D(long seed, Series series) { super(seed, series); }
	
	Potts makePotts() { return new Potts3D(series); }
	
	LocationFactory makeLocationFactory() {
		return new LocationFactory3D();
	}
	
	CellFactory makeCellFactory() {
		return new CellFactory3D();
	}
}