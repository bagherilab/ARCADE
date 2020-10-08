package arcade.agent.cell;

import sim.engine.*;
import arcade.sim.Simulation;
import arcade.agent.module.Module;
import arcade.agent.module.*;
import arcade.env.loc.Location;
import static arcade.sim.Potts.*;

public abstract class PottsCell implements Cell {
	/** Stopper used to stop this agent from being stepped in the schedule */
	public Stoppable stopper;
	
	/** Cell {@link arcade.env.loc.Location} object */
	private final Location location;
	
	/** Unique cell ID */
	final int id;
	
	/** Cell population index */
	final int pop;
	
	/** Cell state */
	private int state;
	
	/** Cell age (in minutes) */
	private int age;
	
	/** Number of tagged regions */
	public int tags;
	
	/** Target cell volume (in voxels) */
	private double targetVolume;
	
	/** Target tagged cell volumes (in voxels) */
	private final double[] targetTagVolumes;
	
	/** Target cell surface (in voxels) */
	private double targetSurface;
	
	/** Target tagged cell surfaces (in voxels) */
	private final double[] targetTagSurfaces;
	
	/** Critical values for cell (in voxels) */
	final double[] criticals;
	
	/** Critical values for cell (in voxels) by tag */
	final double[][] criticalsTag;
	
	/** Lambda parameters for cell */
	final double[] lambdas;
	
	/** Lambda parameters for cell by tag */
	final double[][] lambdasTag;
	
	/** Adhesion values for cell */
	final double[] adhesion;
	
	/** Adhesion values for cell by tag*/
	final double[][] adhesionTag;
	
	/** Cell state module */
	protected Module module;
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default population is 1, state is proliferative, and age is 0.
	 * The cell is created with no tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index   
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	public PottsCell(int id, int pop, Location location,
					 double[] criticals, double[] lambdas, double[] adhesion) {
		this(id, pop, STATE_PROLIFERATIVE, 0, location,
				criticals, lambdas, adhesion, 0, null, null, null);
	}
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default state is proliferative and age is 0. 
	 * 
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param criticalsTag  the list of tagged critical values
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 */
	public PottsCell(int id, int pop, Location location,
					 double[] criticals, double[] lambdas, double[] adhesion, int tags,
					 double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		this(id, pop, STATE_PROLIFERATIVE, 0, location,
				criticals, lambdas, adhesion, tags, criticalsTag, lambdasTag, adhesionsTag);
	}
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * 
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param criticalsTag  the list of tagged critical values
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 */
	public PottsCell(int id, int pop, int state, int age, Location location,
					 double[] criticals, double[] lambdas, double[] adhesion, int tags,
					 double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
		this.id = id;
		this.pop = pop;
		this.age = age;
		this.tags = tags;
		this.location = location;
		this.criticals = criticals.clone();
		this.lambdas = lambdas.clone();
		this.adhesion = adhesion.clone();
		this.criticalsTag = (tags == 0 ? null : new double[NUMBER_TERMS][tags]);
		this.lambdasTag = (tags == 0 ? null : new double[NUMBER_TERMS][tags]);
		this.adhesionTag = (tags == 0 ? null : new double[tags][tags]);
		this.targetTagVolumes = (tags == 0 ? null : new double[tags]);
		this.targetTagSurfaces = (tags == 0 ? null : new double[tags]);
		
		setState(state);
		
		if (tags != 0) {
			for (int i = 0; i < NUMBER_TERMS; i++) {
				this.criticalsTag[i] = criticalsTag[i].clone();
				this.lambdasTag[i] = lambdasTag[i].clone();
			}
			
			for (int i = 0; i < tags; i++) {
				this.adhesionTag[i] = adhesionsTag[i].clone();
			}
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
	
	public double getCriticalVolume() { return criticals[TERM_VOLUME]; }
	
	public double getCriticalVolume(int tag) { return (isValid(tag) ? criticalsTag[TERM_VOLUME][-tag - 1] : 0); }
	
	public double getCriticalSurface() { return criticals[TERM_SURFACE]; }
	
	public double getCriticalSurface(int tag) { return (isValid(tag) ? criticalsTag[TERM_SURFACE][-tag - 1] : 0); }
	
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
				module = new ApoptosisModule.Simple(this);
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
	
	public void schedule(Schedule schedule) {
		stopper = schedule.scheduleRepeating(this, Simulation.ORDERING_CELLS, 1);
	}
	
	public void initialize(int[][][] ids, int[][][] tags) {
		location.update(id, ids, tags);
		
		targetVolume = location.getVolume();
		targetSurface = location.getSurface();
		
		for (int i = 0; i < this.tags; i++) {
			targetTagVolumes[i] = location.getVolume(-i - 1);
			targetTagSurfaces[i] = location.getSurface(-i - 1);
		}
	}
	
	public void reset(int[][][] ids, int[][][] tags) {
		location.update(id, ids, tags);
		
		targetVolume = criticals[TERM_VOLUME];
		targetSurface = criticals[TERM_SURFACE];
		
		for (int i = 0; i < this.tags; i++) {
			targetTagVolumes[i] = criticalsTag[TERM_VOLUME][i];
			targetTagSurfaces[i] = criticalsTag[TERM_SURFACE][i];
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
		
		double oldTargetVolume = targetVolume;
		targetVolume = volume + rate*(scale*criticals[TERM_VOLUME] - volume)*Simulation.DT;
		
		// Ensure that target volume increases or decreases monotonically.
		if ((scale > 1 && targetVolume < oldTargetVolume) ||
				(scale < 1 && targetVolume > oldTargetVolume)) {
			targetVolume = oldTargetVolume ;
		}
		
		targetSurface = convert(targetVolume);
		
		if (tags > 0) {
			targetTagVolumes[0] += targetVolume;
			targetTagSurfaces[0] = convert(targetTagVolumes[0]);
		}
	}
	
	public void updateTarget(int tag, double rate, double scale) {
		double tagVolume = getVolume(tag);
		targetVolume -= targetTagVolumes[-tag - 1];
		
		double oldTargetTagVolume = targetTagVolumes[-tag - 1];
		targetTagVolumes[-tag - 1] = tagVolume + rate*(scale*criticalsTag[TERM_VOLUME][-tag - 1] - tagVolume)*Simulation.DT;
		
		// Ensure that target volume increases or decreases monotonically.
		if ((scale > 1 && targetTagVolumes[-tag - 1] < oldTargetTagVolume) ||
				(scale < 1 && targetTagVolumes[-tag - 1] > oldTargetTagVolume)) {
			targetTagVolumes[-tag - 1] = oldTargetTagVolume;
		}
		
		targetTagSurfaces[-tag - 1] = convert(targetTagVolumes[-tag - 1]);
		
		targetVolume += targetTagVolumes[-tag - 1];
		targetSurface = convert(targetVolume);
	}
	
	/**
	 * Calculates volume to surface area.
	 * 
	 * @param volume  the volume (in voxels)
	 * @return  the surface area (in voxels)
	 */
	abstract double convert(double volume);
	
	public String toJSON() {
		String formatNumber = "\"%s\": %s";
		String formatSet = "[%d, %.2f]";
		StringBuilder s = new StringBuilder();
		
		s.append("{\n")
				.append("\t").append(String.format(formatNumber, "id", id))
				.append(",\n\t").append(String.format(formatNumber, "pop", pop))
				.append(",\n\t").append(String.format(formatNumber, "age", age));
		
		// List overall volumes and surfaces.
		s.append(",\n\t\"volumes\": ").append(String.format(formatSet,
				location.getVolume(), targetVolume));
		s.append(",\n\t\"surfaces\": ").append(String.format(formatSet,
				location.getSurface(), targetSurface));
		
		// List module information.
		s.append(",\n\t\"module\": ")
				.append(module.toJSON().replaceAll("\n", "\n\t"));
		
		// List volumes and surfaces.
		if (tags > 0) {
			s.append(",\n\t\"tags\": [\n");
			for (int i = 0; i < tags; i++) {
				s.append("\t\t{\n\t\t\t\"tag\": ")
						.append(-i - 1)
						.append(",\n\t\t\t\"volumes\": ")
						.append(String.format(formatSet,
								location.getVolume(-i - 1), targetTagVolumes[i]))
						.append(",\n\t\t\t\"surfaces\": ")
						.append(String.format(formatSet,
								location.getSurface(-i - 1), targetTagSurfaces[i]))
						.append("\n\t\t}");
				
				if (i < tags - 1) { s.append(",\n"); }
				else { s.append("\n"); }
			}
			s.append("\t]");
		}
		
		s.append("\n}");
		
		return s.toString();
	}
}