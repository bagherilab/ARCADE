package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.core.agent.cell.CellFactory;
import arcade.core.env.loc.LocationFactory;
import arcade.potts.agent.cell.*;
import arcade.potts.env.loc.*;

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