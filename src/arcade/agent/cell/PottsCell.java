package arcade.agent.cell;

import sim.engine.*;
import arcade.sim.Simulation;
import arcade.agent.module.Module;
import arcade.agent.module.*;
import arcade.env.loc.Location;

public class PottsCell implements Cell {
	/** Multiplier for calculating surface area from volume */
	public static final double SURFACE_VOLUME_MULTIPLIER = 2*Math.sqrt(Math.PI)*1.5;
	
	/** Stopper used to stop this agent from being stepped in the schedule */
	public Stoppable stopper;
	
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
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default population is 1, state is proliferative, and age is 0.
	 * The cell is created with no tags.
	 *
	 * @param id  the cell ID
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	public PottsCell(int id, Location location,
					 double[] lambdas, double[] adhesion) {
		this(id, 1, STATE_PROLIFERATIVE, 0, location,
				lambdas, adhesion, 0, null, null);
	}
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default state is proliferative and age is 0. 
	 * 
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 */
	public PottsCell(int id, int pop, Location location,
					 double[] lambdas, double[] adhesion, int tags,
					 double[][] lambdasTag, double[][] adhesionsTag) {
		this(id, pop, STATE_PROLIFERATIVE, 0, location,
				lambdas, adhesion, tags, lambdasTag, adhesionsTag);
	}
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * 
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 */
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
	
	/**
	 * Creates a {@code PottsCell} agent based on another agent.
	 * <p>
	 * The cell will have the same population, number of tagged regions,
	 * lambdas (overall and tagged), adhesion (overall and tagged), and critical
	 * volume/surface as the parent cell.
	 * The cell age is set to 0.
	 * 
	 * @param c  the parent {@code PottsCell} agent
	 * @param id  the daughter cell ID
	 * @param state  the daughter cell state
	 * @param location  the daughter cell location
	 */
	public PottsCell(PottsCell c, int id, int state, Location location) {
		this(id, c.pop, state, 0, location,
				c.lambdas, c.adhesion, c.tags, c.lambdasTag, c.adhesionTag);
		
		criticalVolume = c.criticalVolume;
		criticalSurface = c.criticalSurface;
		
		for (int i = 0; i < this.tags; i++) {
			criticalTagVolumes[i] = c.criticalTagVolumes[i];
			criticalTagSurfaces[i] = c.criticalTagSurfaces[i];
		}
	}
	
	public int getID() { return id; }
	
	public int getPop() { return pop; }
	
	public int getState() { return state; }
	
	public int getAge() { return age; }
	
	public Location getLocation() { return location; }
	
	public Module getModule() { return module; }
	
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
				module = new ProliferationModule.Simple(this);
				break;
			case STATE_APOPTOTIC:
				module = new ApoptosisModule(this);
				break;
			case STATE_NECROTIC:
				module = new NecrosisModule(this);
				break;
			case STATE_AUTOTIC:
				module = new AutosisModule(this);
				break;
			default:
				// State must be one of the above cases.
				throw new IllegalArgumentException();
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
	
	public void reset(int[][][] ids, int[][][] tags) {
		location.update(id, ids, tags);
		
		targetVolume = criticalVolume;
		targetSurface = criticalSurface;
		
		for (int i = 0; i < this.tags; i++) {
			targetTagVolumes[i] = criticalTagVolumes[i];
			targetTagSurfaces[i] = criticalTagSurfaces[i];
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
	
	public void updateTarget(double rate, double scale) {
		double volume = getVolume();
		if (tags > 0) { targetTagVolumes[0] -= targetVolume; }
		targetVolume = volume + rate*(scale*criticalVolume - volume)*Simulation.DT;
		targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		
		if (tags > 0) {
			targetTagVolumes[0] += targetVolume;
			targetTagSurfaces[0] = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetTagVolumes[0]);
		}
	}
	
	public void updateTarget(int tag, double rate, double scale) {
		double tagVolume = getVolume(tag);
		targetVolume -= targetTagVolumes[-tag - 1];
		targetTagVolumes[-tag - 1] = tagVolume + rate*(scale*criticalTagVolumes[-tag - 1] - tagVolume)*Simulation.DT;
		targetTagSurfaces[-tag - 1] = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetTagVolumes[-tag - 1]);
		
		targetVolume += targetTagVolumes[-tag - 1];
		targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
	}
}