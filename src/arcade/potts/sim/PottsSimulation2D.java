package arcade.sim;

import arcade.agent.cell.*;
import arcade.env.loc.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	Potts makePotts() { return new Potts2D(series); }
	
	LocationFactory makeLocationFactory() {
		return new LocationFactory2D();
	}
	
	CellFactory makeCellFactory() {
		return new CellFactory2D();
	}
}