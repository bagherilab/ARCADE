package arcade.agent.cell;

import sim.engine.*;
import arcade.env.loc.Location;

public class PottsCell implements Cell {
	/** Stopper used to stop this agent from being stepped in the schedule */
	private Stoppable stopper;
	
	/** Cell {@link arcade.env.loc.Location} object */
	private final Location location;
	
	/** Unique cell ID */
	private final int id;
	
	/** Cell population index */
	private final int pop;
	
	/** Target cell volume (in voxels) */
	private double targetVolume;
	
	/** Target cell surface (in voxels) */
	private double targetSurface;
	
	/** Lambda parameters for cell */
	private final double[] lambdas;
	
	/** Adhesion values for cell */
	private final double[] adhesion;
	
	public PottsCell(int id, Location location,
					 double[] lambdas, double[] adhesion) {
		this(id, 1, location, lambdas, adhesion);
	}
	
	public PottsCell(int id, int pop, Location location,
					 double[] lambdas, double[] adhesion) {
		this.id = id;
		this.pop = pop;
		this.location = location;
		this.lambdas = lambdas.clone();
		this.adhesion = adhesion.clone();
	}
	
	public int getID() { return id; }
	public int getPop() { return pop; }
	public Location getLocation() { return location; }
	public int getVolume() { return location.getVolume(); }
	public int getSurface() { return location.getSurface(); }
	public double getTargetVolume() { return targetVolume; }
	public double getTargetSurface() { return targetSurface; }
	public double getLambda(int term) { return lambdas[term]; }
	public double getAdhesion(int pop) { return adhesion[pop]; }
	
	public void initialize(int[][][] potts) {
		location.update(potts, id);
		targetVolume = location.getVolume();
		targetSurface = location.getSurface();
	}
	
	public void step(SimState simstate) { }
}