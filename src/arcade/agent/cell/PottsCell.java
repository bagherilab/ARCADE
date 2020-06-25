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
	
	/** Number of tagged regions */
	private final int tags;
	
	/** Target cell volume (in voxels) */
	private double targetVolume;
	
	/** Target tagged cell volumes (in voxels) */
	private final double[] targetTagVolumes;
	
	/** Target cell surface (in voxels) */
	private double targetSurface;
	
	/** Target tagged cell surfaces (in voxels) */
	private final double[] targetTagSurfaces;
	
	/** Lambda parameters for cell */
	private final double[] lambdas;
	
	/** Lambda parameters for cell by tag */
	private final double[][] lambdasTag;
	
	/** Adhesion values for cell */
	private final double[] adhesion;
	
	/** Adhesion values for cell by tag*/
	private final double[][] adhesionTag;
	
	public PottsCell(int id, Location location,
					 double[] lambdas, double[] adhesion) {
		this(id, 1, location, lambdas, adhesion, 0, null, null);
	}
	
	public PottsCell(int id, int pop, Location location,
					 double[] lambdas, double[] adhesion, int tags,
					 double[][] lambdasTag, double[][] adhesionsTag) {
		this.id = id;
		this.pop = pop;
		this.tags = tags;
		this.location = location;
		this.lambdas = lambdas.clone();
		this.adhesion = adhesion.clone();
		this.lambdasTag = lambdasTag;
		this.adhesionTag = adhesionsTag;
		this.targetTagVolumes = (tags == 0 ? null : new double[tags]);
		this.targetTagSurfaces = (tags == 0 ? null : new double[tags]);
	}
	
	public int getID() { return id; }
	
	public int getPop() { return pop; }
	
	public Location getLocation() { return location; }
	
	public int getVolume() { return location.getVolume(); }
	
	public int getVolume(int tag) { return (isValid(tag) ? location.getVolume(tag) : 0); }
	
	public int getSurface() { return location.getSurface(); }
	
	public int getSurface(int tag) { return (isValid(tag) ? location.getSurface(tag) : 0); }
	
	public double getTargetVolume() { return targetVolume; }
	
	public double getTargetVolume(int tag) { return (isValid(tag) ? targetTagVolumes[-tag - 1] : 0); }
	
	public double getTargetSurface() { return targetSurface; }
	
	public double getTargetSurface(int tag) { return (isValid(tag) ? targetTagSurfaces[-tag - 1] : 0); }
	
	public double getLambda(int term) { return lambdas[term]; }
	
	public double getLambda(int term, int tag) { return (isValid(tag) ? lambdasTag[term][-tag - 1] : Double.NaN); }
	
	public double getAdhesion(int pop) { return adhesion[pop]; }
	
	public double getAdhesion(int tag1, int tag2) { return (isValid(tag1) && isValid(tag2) ? adhesionTag[-tag1 - 1][-tag2 - 1] : Double.NaN); }
	
	public void initialize(int[][][] ids, int[][][] tags) {
		location.update(id, ids, tags);
		targetVolume = location.getVolume();
		targetSurface = location.getSurface();
		
		for (int i = 0; i < this.tags; i++) {
			targetTagVolumes[i] = location.getVolume(-i - 1);
			targetTagSurfaces[i] = location.getSurface(-i - 1);
		}
	}
	
	/**
	 * Checks if the tag is valid.
	 * 
	 * @param tag  the tag to check
	 * @return  {@code true} if the tag is valid, {@code false} otherwise
	 */
	boolean isValid(int tag) { return -tag > 0 && -tag <= tags; }
	
	public void step(SimState simstate) { }
}