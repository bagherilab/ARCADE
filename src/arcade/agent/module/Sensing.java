package arcade.agent.module;

import java.util.List;

import arcade.agent.cell.Cell;
import arcade.env.loc.Location;
import arcade.sim.Simulation;

public abstract class Sensing implements Module {
    
	/** Location of cell */
	final Location loc;

	/** Cell the module is associated with */
	final Cell c;

	/** Cell population index */
	final int pop;

	/** List of internal names */
    List<String> names;

	/** Step size for module (in seconds) */
	static final double STEP_SIZE = 1.0;


	/** List of internal concentrations */
	double[] concs;

    Sensing(Cell c, Simulation sim) {
		this.loc = c.getLocation();
		this.c = c;
		this.pop = c.getPop();
	}

	public double getInternal(String key) { return concs[names.indexOf(key)]; }
}
