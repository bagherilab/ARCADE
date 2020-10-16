package arcade.sim;

import java.util.ArrayList;
import arcade.agent.cell.*;
import arcade.env.loc.*;

public class PottsSimulation2D extends PottsSimulation {
	public PottsSimulation2D(long seed, Series series) { super(seed, series); }
	
	Potts makePotts() { return new Potts2D(series); }
	
	LocationFactory makeLocationFactory() {
		LocationFactory factory = new LocationFactory2D(series._length, series._width, series._height);
		factory.makeCenters(new ArrayList<>(series._populations.values()));
		return factory;
	}
	
	CellFactory makeCellFactory() {
		CellFactory factory = new CellFactory2D();
		return factory;
	}
}