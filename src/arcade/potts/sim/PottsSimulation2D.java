package arcade.potts.sim;

import arcade.core.sim.Series;
import arcade.potts.agent.cell.*;
import arcade.potts.env.loc.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	Potts makePotts() { return new Potts2D(series); }
	
	PottsLocationFactory makeLocationFactory() {
		return new PottsLocationFactory2D();
	}
	
	PottsCellFactory makeCellFactory() {
		return new PottsCellFactory2D();
	}
}