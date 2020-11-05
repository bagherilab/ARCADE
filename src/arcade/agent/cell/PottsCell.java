package arcade.agent.cell;

import sim.engine.*;
import java.util.EnumMap;
import arcade.sim.Simulation;
import arcade.agent.module.Module;
import arcade.agent.module.*;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Potts.Term;

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
	private State state;
	
	/** Cell age (in minutes) */
	private int age;
	
	/** {@code true} if the cell has tags, {@code false} otherwise */
	public boolean hasTags;
	
	/** Target cell volume (in voxels) */
	private double targetVolume;
	
	/** Target tagged cell volumes (in voxels) */
	private final EnumMap<Tag, Double> targetTagVolumes;
	
	/** Target cell surface (in voxels) */
	private double targetSurface;
	
	/** Target tagged cell surfaces (in voxels) */
	private final EnumMap<Tag, Double> targetTagSurfaces;
	
	/** Critical values for cell (in voxels) */
	final EnumMap<Term, Double> criticals;
	
	/** Critical values for cell (in voxels) by tag */
	final EnumMap<Tag, EnumMap<Term, Double>> criticalsTag;
	
	/** Lambda parameters for cell */
	final EnumMap<Term, Double> lambdas;
	
	/** Lambda parameters for cell by tag */
	final EnumMap<Tag, EnumMap<Term, Double>> lambdasTag;
	
	/** Adhesion values for cell */
	final double[] adhesion;
	
	/** Adhesion values for cell by tag*/
	final EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag;
	
	/** Cell state module */
	protected Module module;
	
	/** Cell parameters */
	final MiniBox parameters;
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default state is proliferative and age is 0.
	 * The cell is created with no tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	public PottsCell(int id, int pop, Location location, MiniBox parameters,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
		this(id, pop, State.PROLIFERATIVE, 0, location, false, parameters,
				criticals, lambdas, adhesion, null, null, null);
	}
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default state is proliferative and age is 0.
	 * 
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param criticalsTag  the map of tagged critical values
	 * @param lambdasTag  the map of tagged lambda multipliers
	 * @param adhesionTag  the map of tagged adhesion values
	 */
	public PottsCell(int id, int pop, Location location, MiniBox parameters,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					 EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
					 EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
		this(id, pop, State.PROLIFERATIVE, 0, location, true, parameters,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
	}
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * 
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param hasTags  {@code true} if the cell has tags, {@code false} otherwise
	 * @param criticalsTag  the map of tagged critical values
	 * @param lambdasTag  the map of tagged lambda multipliers
	 * @param adhesionTag  the map of tagged adhesion values
	 */
	public PottsCell(int id, int pop, State state, int age, Location location, boolean hasTags, MiniBox parameters,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					 EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
					 EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
		this.id = id;
		this.pop = pop;
		this.age = age;
		this.hasTags = hasTags;
		this.location = location;
		this.parameters = parameters;
		this.criticals = criticals.clone();
		this.lambdas = lambdas.clone();
		this.adhesion = adhesion.clone();
		
		setState(state);
		
		if (hasTags) {
			this.criticalsTag = new EnumMap<>(Tag.class);
			this.lambdasTag = new EnumMap<>(Tag.class);
			this.adhesionTag = new EnumMap<>(Tag.class);
			this.targetTagVolumes = new EnumMap<>(Tag.class);
			this.targetTagSurfaces = new EnumMap<>(Tag.class);
			
			for (Tag tag : location.getTags()) {
				this.criticalsTag.put(tag, criticalsTag.get(tag).clone());
				this.lambdasTag.put(tag, lambdasTag.get(tag).clone());
				this.adhesionTag.put(tag, adhesionTag.get(tag).clone());
			}
		} else {
			this.criticalsTag = null;
			this.lambdasTag = null;
			this.adhesionTag = null;
			this.targetTagVolumes = null;
			this.targetTagSurfaces = null;
		}
	}
	
	public int getID() { return id; }
	
	public int getPop() { return pop; }
	
	public State getState() { return state; }
	
	public int getAge() { return age; }
	
	public boolean hasTags() { return hasTags; }
	
	public Location getLocation() { return location; }
	
	public Module getModule() { return module; }
	
	public MiniBox getParameters() { return parameters; }
	
	public int getVolume() { return location.getVolume(); }
	
	public int getVolume(Tag tag) { return (hasTags ? location.getVolume(tag) : 0); }
	
	public int getSurface() { return location.getSurface(); }
	
	public int getSurface(Tag tag) { return (hasTags ? location.getSurface(tag) : 0); }
	
	public double getTargetVolume() { return targetVolume; }
	
	public double getTargetVolume(Tag tag) { return (hasTags && targetTagVolumes.containsKey(tag) ? targetTagVolumes.get(tag) : 0); }
	
	public double getTargetSurface() { return targetSurface; }
	
	public double getTargetSurface(Tag tag) { return (hasTags && targetTagSurfaces.containsKey(tag) ? targetTagSurfaces.get(tag) : 0); }
	
	public double getCriticalVolume() { return criticals.get(Term.VOLUME); }
	
	public double getCriticalVolume(Tag tag) { return (hasTags && criticalsTag.containsKey(tag) ? criticalsTag.get(tag).get(Term.VOLUME) : 0); }
	
	public double getCriticalSurface() { return criticals.get(Term.SURFACE); }
	
	public double getCriticalSurface(Tag tag) { return (hasTags && criticalsTag.containsKey(tag) ? criticalsTag.get(tag).get(Term.SURFACE) : 0); }
	
	public double getLambda(Term term) { return lambdas.get(term); }
	
	public double getLambda(Term term, Tag tag) { return (hasTags && lambdasTag.containsKey(tag) ? lambdasTag.get(tag).get(term) : Double.NaN); }
	
	public double getAdhesion(int pop) { return adhesion[pop]; }
	
	public double getAdhesion(Tag tag1, Tag tag2) { return (hasTags && adhesionTag.containsKey(tag1) && adhesionTag.containsKey(tag2) ? adhesionTag.get(tag1).get(tag2) : Double.NaN); }
	
	public void setState(State state) {
		this.state = state;
		
		switch (state) {
			case QUIESCENT:
				module = new QuiescenceModule(this);
				break;
			case PROLIFERATIVE:
				module = new ProliferationModule.Simple(this);
				break;
			case APOPTOTIC:
				module = new ApoptosisModule.Simple(this);
				break;
			case NECROTIC:
				module = new NecrosisModule(this);
				break;
			case AUTOTIC:
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
		
		if (targetVolume != 0 && targetSurface != 0) { return; }
		
		targetVolume = location.getVolume();
		targetSurface = location.getSurface();
		
		if (!hasTags) { return; }
		
		for (Tag tag : location.getTags()) {
			targetTagVolumes.put(tag, (double)location.getVolume(tag));
			targetTagSurfaces.put(tag, (double)location.getSurface(tag));
		}
	}
	
	public void reset(int[][][] ids, int[][][] tags) {
		location.update(id, ids, tags);
		
		targetVolume = criticals.get(Term.VOLUME);
		targetSurface = criticals.get(Term.SURFACE);
		
		if (!hasTags) { return; }
		
		for (Tag tag : location.getTags()) {
			targetTagVolumes.put(tag, criticalsTag.get(tag).get(Term.VOLUME));
			targetTagSurfaces.put(tag, criticalsTag.get(tag).get(Term.SURFACE));
		}
	}
	
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
	
	public void setTargets(double volume, double surface) {
		targetVolume = volume;
		targetSurface = surface;
	}
	
	public void setTargets(Tag tag, double volume, double surface) {
		targetTagVolumes.put(tag, volume);
		targetTagSurfaces.put(tag, surface);
	}
	
	public void updateTarget(double rate, double scale) {
		double volume = getVolume();
		if (hasTags) { targetTagVolumes.put(Tag.DEFAULT, targetTagVolumes.get(Tag.DEFAULT) - targetVolume); }
		
		double oldTargetVolume = targetVolume;
		targetVolume = volume + rate*(scale*criticals.get(Term.VOLUME) - volume)*Simulation.DT;
		
		// Ensure that target volume increases or decreases monotonically.
		if ((scale > 1 && targetVolume < oldTargetVolume) ||
				(scale < 1 && targetVolume > oldTargetVolume)) {
			targetVolume = oldTargetVolume ;
		}
		
		targetSurface = convert(targetVolume);
		
		if (hasTags) {
			targetTagVolumes.put(Tag.DEFAULT, targetTagVolumes.get(Tag.DEFAULT) + targetVolume);
			targetTagSurfaces.put(Tag.DEFAULT, convert(targetTagVolumes.get(Tag.DEFAULT)));
		}
	}
	
	public void updateTarget(Tag tag, double rate, double scale) {
		double tagVolume = getVolume(tag);
		targetVolume -= targetTagVolumes.get(tag);
		
		double oldTargetTagVolume = targetTagVolumes.get(tag);
		targetTagVolumes.put(tag, tagVolume + rate*(scale*criticalsTag.get(tag).get(Term.VOLUME) - tagVolume)*Simulation.DT);
		
		// Ensure that target volume increases or decreases monotonically.
		if ((scale > 1 && targetTagVolumes.get(tag) < oldTargetTagVolume) ||
				(scale < 1 && targetTagVolumes.get(tag) > oldTargetTagVolume)) {
			targetTagVolumes.put(tag, oldTargetTagVolume);
		}
		
		targetTagSurfaces.put(tag, convert(targetTagVolumes.get(tag)));
		
		targetVolume += targetTagVolumes.get(tag);
		targetSurface = convert(targetVolume);
	}
	
	/**
	 * Calculates volume to surface area.
	 * 
	 * @param volume  the volume (in voxels)
	 * @return  the surface area (in voxels)
	 */
	abstract double convert(double volume);
}