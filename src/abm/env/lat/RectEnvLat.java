package abm.env.lat;

import abm.sim.Simulation;
import abm.env.comp.*;
import abm.util.MiniBox;

/** 
 * Environment lattice class for subrectangular grid with abstract class
 * EnvLattice, which implements the Lattice interface.
 *
 * @version 2.3.12
 * @since   2.3
 */

public class RectEnvLat extends EnvLattice {
	// CONSTRUCTOR.
	public RectEnvLat(int length, int width, int depth) { this(length, width, depth, 0); }
	public RectEnvLat(int length, int width, int depth, double val) {
		super(length, width, depth, val);
	}
	
	// METHOD: makeDiffuser. Instantiates a rectangular diffuser.
	public Component makeDiffuser(Simulation sim, MiniBox molecule) {
		return new RectDiffuser(sim, this, molecule);
	}
}