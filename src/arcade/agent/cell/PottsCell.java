package arcade.agent.cell;

import sim.engine.*;
import arcade.sim.Simulation;
import arcade.agent.module.Module;
import arcade.agent.module.*;
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
	
	/** Cell state */
	private int state;
	
	/** Cell age (in minutes) */
	private int age;
	
	/** Number of tagged regions */
	private final int tags;
	
	/** Target cell volume (in voxels) */
	private double targetVolume;
	
	/** Critical cell volume (in voxels) */
	private double criticalVolume;
	
	/** Target tagged cell volumes (in voxels) */
	private final double[] targetTagVolumes;
	
	/** Critical tagged cell volumes (in voxels) */
	private final double[] criticalTagVolumes;
	
	/** Target cell surface (in voxels) */
	private double targetSurface;
	
	/** Critical cell surface (in voxels) */
	private double criticalSurface;
	
	/** Target tagged cell surfaces (in voxels) */
	private final double[] targetTagSurfaces;
	
	/** Critical tagged cell surfaces (in voxels) */
	private final double[] criticalTagSurfaces;
	
	/** Lambda parameters for cell */
	private final double[] lambdas;
	
	/** Lambda parameters for cell by tag */
	private final double[][] lambdasTag;
	
	/** Adhesion values for cell */
	private final double[] adhesion;
	
	/** Adhesion values for cell by tag*/
	private final double[][] adhesionTag;
	
	/** Cell state module */
	protected Module module;
	
	public PottsCell(int id, Location location,
					 double[] lambdas, double[] adhesion) {
		this(id, 1, STATE_PROLIFERATIVE, 0, location,
				lambdas, adhesion, 0, null, null);
	}
	
	public PottsCell(int id, int pop, Location location,
					 double[] lambdas, double[] adhesion, int tags,
					 double[][] lambdasTag, double[][] adhesionsTag) {
		this(id, pop, STATE_PROLIFERATIVE, 0, location,
				lambdas, adhesion, tags, lambdasTag, adhesionsTag);
	}
	
	public PottsCell(int id, int pop, int state, int age, Location location,
					 double[] lambdas, double[] adhesion, int tags,
					 double[][] lambdasTag, double[][] adhesionsTag) {
		this.id = id;
		this.pop = pop;
		this.age = age;
		this.tags = tags;
		this.location = location;
		this.lambdas = lambdas.clone();
		this.adhesion = adhesion.clone();
		this.lambdasTag = lambdasTag;
		this.adhesionTag = adhesionsTag;
		this.targetTagVolumes = (tags == 0 ? null : new double[tags]);
		this.targetTagSurfaces = (tags == 0 ? null : new double[tags]);
		this.criticalTagVolumes = (tags == 0 ? null : new double[tags]);
		this.criticalTagSurfaces = (tags == 0 ? null : new double[tags]);
		setState(state);
	}
	
	public int getID() { return id; }
	
	public int getPop() { return pop; }
	
	public int getState() { return state; }
	
	public int getAge() { return age; }
	
	public Location getLocation() { return location; }
	
	public int getVolume() { return location.getVolume(); }
	
	public int getVolume(int tag) { return (isValid(tag) ? location.getVolume(tag) : 0); }
	
	public int getSurface() { return location.getSurface(); }
	
	public int getSurface(int tag) { return (isValid(tag) ? location.getSurface(tag) : 0); }
	
	public double getTargetVolume() { return targetVolume; }
	
	public double getTargetVolume(int tag) { return (isValid(tag) ? targetTagVolumes[-tag - 1] : 0); }
	
	public double getTargetSurface() { return targetSurface; }
	
	public double getTargetSurface(int tag) { return (isValid(tag) ? targetTagSurfaces[-tag - 1] : 0); }
	
	public double getCriticalVolume() { return criticalVolume; }
	
	public double getCriticalVolume(int tag) { return (isValid(tag) ? criticalTagVolumes[-tag - 1] : 0); }
	
	public double getCriticalSurface() { return criticalSurface; }
	
	public double getCriticalSurface(int tag) { return (isValid(tag) ? criticalTagSurfaces[-tag - 1] : 0); }
	
	public double getLambda(int term) { return lambdas[term]; }
	
	public double getLambda(int term, int tag) { return (isValid(tag) ? lambdasTag[term][-tag - 1] : Double.NaN); }
	
	public double getAdhesion(int pop) { return adhesion[pop]; }
	
	public double getAdhesion(int tag1, int tag2) { return (isValid(tag1) && isValid(tag2) ? adhesionTag[-tag1 - 1][-tag2 - 1] : Double.NaN); }
	
	public void setState(int state) {
		this.state = state;
		
		switch (state) {
			case STATE_QUIESCENT:
				module = new QuiescenceModule(this);
				break;
			case STATE_PROLIFERATIVE:
				module = new ProliferationModule(this);
				break;
			case STATE_APOPTOTIC:
				module = new ApoptosisModule(this);
				break;
			case STATE_NECROTIC:
				module = new NecrosisModule(this);;
				break;
			case STATE_AUTOTIC:
				module = new AutosisModule(this);;
				break;
		}
	}
	
	public void initialize(int[][][] ids, int[][][] tags) {
		location.update(id, ids, tags);
		
		targetVolume = location.getVolume();
		targetSurface = location.getSurface();
		
		criticalVolume = targetVolume;
		criticalSurface = targetSurface;
		
		for (int i = 0; i < this.tags; i++) {
			targetTagVolumes[i] = location.getVolume(-i - 1);
			targetTagSurfaces[i] = location.getSurface(-i - 1);
			
			criticalTagVolumes[i] = targetTagVolumes[i];
			criticalTagSurfaces[i] = targetTagSurfaces[i];
		}
	}
	
	/**
	 * Checks if the tag is valid.
	 * 
	 * @param tag  the tag to check
	 * @return  {@code true} if the tag is valid, {@code false} otherwise
	 */
	boolean isValid(int tag) { return -tag > 0 && -tag <= tags; }
	
	/**
	 * Steps through cell rules.
	 * 
	 * @param simstate  the MASON simulation state
	 */
	public void step(SimState simstate) {
		Simulation sim = (Simulation)simstate;
		
		// Increase age of cell (in ticks).
		age++;
		
		// Step the module for the cell state.
		module.step(simstate.random, sim);
	}
}