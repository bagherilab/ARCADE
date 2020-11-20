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
	Stoppable stopper;
	
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
	
	/** {@code true} if the cell has regions, {@code false} otherwise */
	public boolean hasRegions;
	
	/** Target cell volume (in voxels) */
	private double targetVolume;
	
	/** Target region cell volumes (in voxels) */
	private final EnumMap<Region, Double> targetRegionVolumes;
	
	/** Target cell surface (in voxels) */
	private double targetSurface;
	
	/** Target region cell surfaces (in voxels) */
	private final EnumMap<Region, Double> targetRegionSurfaces;
	
	/** Critical values for cell (in voxels) */
	final EnumMap<Term, Double> criticals;
	
	/** Critical values for cell (in voxels) by region */
	final EnumMap<Region, EnumMap<Term, Double>> criticalsRegion;
	
	/** Lambda parameters for cell */
	final EnumMap<Term, Double> lambdas;
	
	/** Lambda parameters for cell by region */
	final EnumMap<Region, EnumMap<Term, Double>> lambdasRegion;
	
	/** Adhesion values for cell */
	final double[] adhesion;
	
	/** Adhesion values for cell by region*/
	final EnumMap<Region, EnumMap<Region, Double>> adhesionRegion;
	
	/** Cell state module */
	protected Module module;
	
	/** Cell parameters */
	final MiniBox parameters;
	
	/**
	 * Creates a {@code PottsCell} agent.
	 * <p>
	 * The default state is proliferative and age is 0.
	 * The cell is created with no regions.
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
	 * @param criticalsRegion  the map of critical values for regions
	 * @param lambdasRegion  the map of lambda multipliers for regions
	 * @param adhesionRegion  the map of adhesion values for regions
	 */
	public PottsCell(int id, int pop, Location location, MiniBox parameters,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					 EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
					 EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		this(id, pop, State.PROLIFERATIVE, 0, location, true, parameters,
				criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
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
	 * @param hasRegions  {@code true} if the cell has regions, {@code false} otherwise
	 * @param criticalsRegion  the map of critical values for regions
	 * @param lambdasRegion  the map of lambda multipliers for regions
	 * @param adhesionRegion  the map of adhesion values for regions
	 */
	public PottsCell(int id, int pop, State state, int age, Location location, boolean hasRegions, MiniBox parameters,
					 EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					 EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
					 EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
		this.id = id;
		this.pop = pop;
		this.age = age;
		this.hasRegions = hasRegions;
		this.location = location;
		this.parameters = parameters;
		this.criticals = criticals.clone();
		this.lambdas = lambdas.clone();
		this.adhesion = adhesion.clone();
		
		setState(state);
		
		if (hasRegions) {
			this.criticalsRegion = new EnumMap<>(Region.class);
			this.lambdasRegion = new EnumMap<>(Region.class);
			this.adhesionRegion = new EnumMap<>(Region.class);
			this.targetRegionVolumes = new EnumMap<>(Region.class);
			this.targetRegionSurfaces = new EnumMap<>(Region.class);
			
			for (Region region : location.getRegions()) {
				this.criticalsRegion.put(region, criticalsRegion.get(region).clone());
				this.lambdasRegion.put(region, lambdasRegion.get(region).clone());
				this.adhesionRegion.put(region, adhesionRegion.get(region).clone());
			}
		} else {
			this.criticalsRegion = null;
			this.lambdasRegion = null;
			this.adhesionRegion = null;
			this.targetRegionVolumes = null;
			this.targetRegionSurfaces = null;
		}
	}
	
	public int getID() { return id; }
	
	public int getPop() { return pop; }
	
	public State getState() { return state; }
	
	public int getAge() { return age; }
	
	public boolean hasRegions() { return hasRegions; }
	
	public Location getLocation() { return location; }
	
	public Module getModule() { return module; }
	
	public MiniBox getParameters() { return parameters; }
	
	public int getVolume() { return location.getVolume(); }
	
	public int getVolume(Region region) { return (hasRegions ? location.getVolume(region) : 0); }
	
	public int getSurface() { return location.getSurface(); }
	
	public int getSurface(Region region) { return (hasRegions ? location.getSurface(region) : 0); }
	
	public double getTargetVolume() { return targetVolume; }
	
	public double getTargetVolume(Region region) { return (hasRegions && targetRegionVolumes.containsKey(region) ? targetRegionVolumes.get(region) : 0); }
	
	public double getTargetSurface() { return targetSurface; }
	
	public double getTargetSurface(Region region) { return (hasRegions && targetRegionSurfaces.containsKey(region) ? targetRegionSurfaces.get(region) : 0); }
	
	public double getCriticalVolume() { return criticals.get(Term.VOLUME); }
	
	public double getCriticalVolume(Region region) { return (hasRegions && criticalsRegion.containsKey(region) ? criticalsRegion.get(region).get(Term.VOLUME) : 0); }
	
	public double getCriticalSurface() { return criticals.get(Term.SURFACE); }
	
	public double getCriticalSurface(Region region) { return (hasRegions && criticalsRegion.containsKey(region) ? criticalsRegion.get(region).get(Term.SURFACE) : 0); }
	
	public double getLambda(Term term) { return lambdas.get(term); }
	
	public double getLambda(Term term, Region region) { return (hasRegions && lambdasRegion.containsKey(region) ? lambdasRegion.get(region).get(term) : Double.NaN); }
	
	public double getAdhesion(int pop) { return adhesion[pop]; }
	
	public double getAdhesion(Region region1, Region region2) { return (hasRegions && adhesionRegion.containsKey(region1) && adhesionRegion.containsKey(region2) ? adhesionRegion.get(region1).get(region2) : Double.NaN); }
	
	public void stop() { stopper.stop(); }
	
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
	
	public void initialize(int[][][] ids, int[][][] regions) {
		location.update(id, ids, regions);
		
		if (targetVolume != 0 && targetSurface != 0) { return; }
		
		targetVolume = location.getVolume();
		targetSurface = location.getSurface();
		
		if (!hasRegions) { return; }
		
		for (Region region : location.getRegions()) {
			targetRegionVolumes.put(region, (double)location.getVolume(region));
			targetRegionSurfaces.put(region, (double)location.getSurface(region));
		}
	}
	
	public void reset(int[][][] ids, int[][][] regions) {
		location.update(id, ids, regions);
		
		targetVolume = criticals.get(Term.VOLUME);
		targetSurface = criticals.get(Term.SURFACE);
		
		if (!hasRegions) { return; }
		
		for (Region region : location.getRegions()) {
			targetRegionVolumes.put(region, criticalsRegion.get(region).get(Term.VOLUME));
			targetRegionSurfaces.put(region, criticalsRegion.get(region).get(Term.SURFACE));
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
	
	public void setTargets(Region region, double volume, double surface) {
		targetRegionVolumes.put(region, volume);
		targetRegionSurfaces.put(region, surface);
	}
	
	public void updateTarget(double rate, double scale) {
		double volume = getVolume();
		if (hasRegions) { targetRegionVolumes.put(Region.DEFAULT, targetRegionVolumes.get(Region.DEFAULT) - targetVolume); }
		
		double oldTargetVolume = targetVolume;
		targetVolume = volume + rate*(scale*criticals.get(Term.VOLUME) - volume);
		
		// Ensure that target volume increases or decreases monotonically.
		if ((scale > 1 && targetVolume < oldTargetVolume) ||
				(scale < 1 && targetVolume > oldTargetVolume)) {
			targetVolume = oldTargetVolume ;
		}
		
		targetSurface = convert(targetVolume);
		
		if (hasRegions) {
			targetRegionVolumes.put(Region.DEFAULT, targetRegionVolumes.get(Region.DEFAULT) + targetVolume);
			targetRegionSurfaces.put(Region.DEFAULT, convert(targetRegionVolumes.get(Region.DEFAULT)));
		}
	}
	
	public void updateTarget(Region region, double rate, double scale) {
		double regionVolume = getVolume(region);
		targetVolume -= targetRegionVolumes.get(region);
		
		double oldTargetRegionVolume = targetRegionVolumes.get(region);
		targetRegionVolumes.put(region, regionVolume + rate*(scale*criticalsRegion.get(region).get(Term.VOLUME) - regionVolume));
		
		// Ensure that target volume increases or decreases monotonically.
		if ((scale > 1 && targetRegionVolumes.get(region) < oldTargetRegionVolume) ||
				(scale < 1 && targetRegionVolumes.get(region) > oldTargetRegionVolume)) {
			targetRegionVolumes.put(region, oldTargetRegionVolume);
		}
		
		targetRegionSurfaces.put(region, convert(targetRegionVolumes.get(region)));
		
		targetVolume += targetRegionVolumes.get(region);
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