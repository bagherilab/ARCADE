package arcade.potts.agent.cell;

import java.util.EnumMap;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Stoppable;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.module.Module;
import arcade.core.env.loc.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferation;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Implementation of {@link Cell} for potts models.
 * <p>
 * {@code PottsCell} agents exist in one of five states: quiescent, proliferative,
 * apoptotic, necrotic, or autotic.
 * Each state may be further divided into relevant phases.
 * <p>
 * General order of rules for the {@code PottsCell} step:
 * <ul>
 *     <li>update age</li>
 *     <li>step state module</li>
 * </ul>
 * <p>
 * Cell parameters are tracked using a map between the parameter name and value.
 * <p>
 * To integrate with the potts layer, {@code PottsCell} agents also contain
 * adhesion to other cell populations, critical and target volumes and surfaces,
 * and lambda values used for potts energy calculations.
 */

public final class PottsCell implements Cell {
    /** Stopper used to stop this agent from being stepped in the schedule. */
    Stoppable stopper;
    
    /** Cell {@link arcade.core.env.loc.Location} object. */
    private final PottsLocation location;
    
    /** Unique cell ID. */
    final int id;
    
    /** Cell parent ID. */
    final int parent;
    
    /** Cell population index. */
    final int pop;
    
    /** Cell state. */
    private State state;
    
    /** Cell age (in ticks). */
    private int age;
    
    /** {@code true} if the cell has regions, {@code false} otherwise. */
    private final boolean hasRegions;
    
    /** Target cell volume (in voxels). */
    private double targetVolume;
    
    /** Target region cell volumes (in voxels). */
    private final EnumMap<Region, Double> targetRegionVolumes;
    
    /** Target cell surface (in voxels). */
    private double targetSurface;
    
    /** Target region cell surfaces (in voxels). */
    private final EnumMap<Region, Double> targetRegionSurfaces;
    
    /** Critical values for cell (in voxels). */
    final EnumMap<Term, Double> criticals;
    
    /** Critical values for cell (in voxels) by region. */
    final EnumMap<Region, EnumMap<Term, Double>> criticalsRegion;
    
    /** Lambda parameters for cell. */
    final EnumMap<Term, Double> lambdas;
    
    /** Lambda parameters for cell by region. */
    final EnumMap<Region, EnumMap<Term, Double>> lambdasRegion;
    
    /** Adhesion values for cell. */
    final double[] adhesion;
    
    /** Adhesion values for cell by region. */
    final EnumMap<Region, EnumMap<Region, Double>> adhesionRegion;
    
    /** Cell state module. */
    protected Module module;
    
    /** Cell parameters. */
    final MiniBox parameters;
    
    /**
     * Creates a {@code PottsCell} agent.
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param state  the cell state
     * @param age  the cell age (in ticks)
     * @param location  the {@link arcade.core.env.loc.Location} of the cell
     * @param hasRegions  {@code true} if the cell has regions, {@code false} otherwise
     * @param parameters  the dictionary of parameters
     * @param adhesion  the list of adhesion values
     * @param criticals  the map of critical values
     * @param lambdas  the map of lambda multipliers
     * @param criticalsRegion  the map of critical values for regions
     * @param lambdasRegion  the map of lambda multipliers for regions
     * @param adhesionRegion  the map of adhesion values for regions
     */
    public PottsCell(int id, int parent, int pop, State state, int age, Location location,
                     boolean hasRegions, MiniBox parameters, double[] adhesion,
                     EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas,
                     EnumMap<Region, EnumMap<Term, Double>> criticalsRegion,
                     EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
                     EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.hasRegions = hasRegions;
        this.location = (PottsLocation) location;
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
    
    @Override
    public int getID() { return id; }
    
    @Override
    public int getParent() { return parent; }
    
    @Override
    public int getPop() { return pop; }
    
    @Override
    public State getState() { return state; }
    
    @Override
    public int getAge() { return age; }
    
    @Override
    public boolean hasRegions() { return hasRegions; }
    
    @Override
    public Location getLocation() { return location; }
    
    @Override
    public Module getModule() { return module; }
    
    @Override
    public MiniBox getParameters() { return parameters; }
    
    @Override
    public int getVolume() { return location.getVolume(); }
    
    @Override
    public int getVolume(Region region) { return (hasRegions ? location.getVolume(region) : 0); }
    
    @Override
    public int getHeight() { return location.getHeight(); }
    
    @Override
    public int getHeight(Region region) { return (hasRegions ? location.getHeight(region) : 0); }
    
    /**
     * Gets the cell surface (in voxels).
     *
     * @return  the cell surface
     */
    public int getSurface() { return location.getSurface(); }
    
    /**
     * Gets the cell surface (in voxels) for a region.
     *
     * @param region  the region
     * @return  the cell region surface
     */
    public int getSurface(Region region) { return (hasRegions ? location.getSurface(region) : 0); }
    
    /**
     * Gets the target volume (in voxels).
     *
     * @return  the target volume
     */
    public double getTargetVolume() { return targetVolume; }
    
    /**
     * Gets the target volume (in voxels) for a region.
     *
     * @param region  the region
     * @return  the target region volume
     */
    public double getTargetVolume(Region region) {
        return (hasRegions && targetRegionVolumes.containsKey(region)
                ? targetRegionVolumes.get(region)
                : 0);
    }
    
    /**
     * Gets the target surface (in voxels).
     *
     * @return  the target surface
     */
    public double getTargetSurface() { return targetSurface; }
    
    /**
     * Gets the target surface (in voxels) for a region.
     *
     * @param region  the region
     * @return  the target region surface
     */
    public double getTargetSurface(Region region) {
        return (hasRegions && targetRegionSurfaces.containsKey(region)
                ? targetRegionSurfaces.get(region)
                : 0);
    }
    
    @Override
    public double getCriticalVolume() { return criticals.get(Term.VOLUME); }
    
    @Override
    public double getCriticalVolume(Region region) {
        return (hasRegions && criticalsRegion.containsKey(region)
                ? criticalsRegion.get(region).get(Term.VOLUME)
                : 0);
    }
    
    @Override
    public double getCriticalHeight() { return criticals.get(Term.HEIGHT); }
    
    @Override
    public double getCriticalHeight(Region region) {
        return (hasRegions && criticalsRegion.containsKey(region)
                ? criticalsRegion.get(region).get(Term.HEIGHT)
                : 0);
    }
    
    /**
     * Gets the lambda for the given term.
     *
     * @param term  the term of the Hamiltonian
     * @return  the lambda value
     */
    public double getLambda(Term term) { return lambdas.get(term); }
    
    /**
     * Gets the lambda for the given term and region.
     *
     * @param term  the term of the Hamiltonian
     * @param region  the region
     * @return  the lambda value
     */
    public double getLambda(Term term, Region region) {
        return (hasRegions && lambdasRegion.containsKey(region)
                ? lambdasRegion.get(region).get(term)
                : Double.NaN);
    }
    
    /**
     * Gets the adhesion to a cell of the given population.
     *
     * @param target  the target cell population
     * @return  the adhesion value
     */
    public double getAdhesion(int target) { return adhesion[target]; }
    
    /**
     * Gets the adhesion between two regions.
     *
     * @param region1  the first region
     * @param region2  the second region
     * @return  the adhesion value
     */
    public double getAdhesion(Region region1, Region region2) {
        return (hasRegions && adhesionRegion.containsKey(region1)
                    && adhesionRegion.containsKey(region2)
                ? adhesionRegion.get(region1).get(region2)
                : Double.NaN);
    }
    
    @Override
    public void stop() { stopper.stop(); }
    
    @Override
    public PottsCell make(int newID, State newState, Location newLocation) {
        return new PottsCell(newID, id, pop, newState, age, newLocation,
                hasRegions, parameters, adhesion, criticals, lambdas,
                criticalsRegion, lambdasRegion, adhesionRegion);
    }
    
    @Override
    public void setState(State state) {
        this.state = state;
        
        switch (state) {
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            case PROLIFERATIVE:
                module = new PottsModuleProliferation.Simple(this);
                break;
            case APOPTOTIC:
                module = new PottsModuleApoptosis.Simple(this);
                break;
            case NECROTIC:
                module = new PottsModuleNecrosis(this);
                break;
            case AUTOTIC:
                module = new PottsModuleAutosis(this);
                break;
            default:
                // State must be one of the above cases.
                module = null;
                break;
        }
    }
    
    @Override
    public void schedule(Schedule schedule) {
        stopper = schedule.scheduleRepeating(this, Ordering.CELLS.ordinal(), 1);
    }
    
    /**
     * Initializes the potts arrays with the cell.
     *
     * @param ids  the {@link arcade.potts.sim.Potts} array for ids
     * @param regions  the {@link arcade.potts.sim.Potts} array for regions
     */
    public void initialize(int[][][] ids, int[][][] regions) {
        location.update(id, ids, regions);
        
        if (targetVolume != 0 && targetSurface != 0) { return; }
        
        targetVolume = location.getVolume();
        targetSurface = location.getSurface();
        
        if (!hasRegions) { return; }
        
        for (Region region : location.getRegions()) {
            targetRegionVolumes.put(region, (double) location.getVolume(region));
            targetRegionSurfaces.put(region, (double) location.getSurface(region));
        }
    }
    
    /**
     * Resets the potts arrays with the cell.
     *
     * @param ids  the {@link arcade.potts.sim.Potts} array for ids
     * @param regions  the {@link arcade.potts.sim.Potts} array for regions
     */
    public void reset(int[][][] ids, int[][][] regions) {
        location.update(id, ids, regions);
        
        double height = criticals.get(Term.HEIGHT);
        targetVolume = criticals.get(Term.VOLUME);
        targetSurface = location.convertSurface(targetVolume, height);
        
        if (!hasRegions) { return; }
        
        for (Region region : location.getRegions()) {
            EnumMap<Term, Double> regionTerms = criticalsRegion.get(region);
            double regionHeight = regionTerms.get(Term.HEIGHT);
            double regionVolume = regionTerms.get(Term.VOLUME);
            targetRegionVolumes.put(region, regionVolume);
            targetRegionSurfaces.put(region, location.convertSurface(regionVolume, regionHeight));
        }
    }
    
    /**
     * Steps through cell rules.
     *
     * @param simstate  the MASON simulation state
     */
    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        
        // Increase age of cell (in ticks).
        age++;
        
        // Step the module for the cell state.
        module.step(simstate.random, sim);
    }
    
    /**
     * Sets the target volume and surface for the cell.
     *
     * @param volume  the target volume
     * @param surface  the target surface
     */
    public void setTargets(double volume, double surface) {
        targetVolume = volume;
        targetSurface = surface;
    }
    
    /**
     * Sets the target volume and surface for a region.
     *
     * @param region  the region
     * @param volume  the target volume
     * @param surface  the target surface
     */
    public void setTargets(Region region, double volume, double surface) {
        targetRegionVolumes.put(region, volume);
        targetRegionSurfaces.put(region, surface);
    }
    
    /**
     * Updates target volume and surface area.
     *
     * @param rate  the rate of change
     * @param scale  the relative final size scaling
     */
    public void updateTarget(double rate, double scale) {
        double volume = getVolume();
        
        if (hasRegions) {
            double updateVolume = targetRegionVolumes.get(Region.DEFAULT) - targetVolume;
            targetRegionVolumes.put(Region.DEFAULT, updateVolume);
        }
        
        double oldTargetVolume = targetVolume;
        double criticalVolume = criticals.get(Term.VOLUME);
        targetVolume = volume + rate * (scale * criticalVolume - volume);
        
        // Ensure that target volume increases or decreases monotonically.
        if ((scale > 1 && targetVolume < oldTargetVolume)
                || (scale < 1 && targetVolume > oldTargetVolume)) {
            targetVolume = oldTargetVolume;
        }
        
        double criticalHeight = criticals.get(Term.HEIGHT);
        targetSurface = location.convertSurface(targetVolume, criticalHeight);
        
        if (hasRegions) {
            double updateVolume = targetRegionVolumes.get(Region.DEFAULT) + targetVolume;
            targetRegionVolumes.put(Region.DEFAULT, updateVolume);
            
            double criticalRegionHeight = criticalsRegion.get(Region.DEFAULT).get(Term.HEIGHT);
            double updateSurface = location.convertSurface(updateVolume, criticalRegionHeight);
            targetRegionSurfaces.put(Region.DEFAULT, updateSurface);
        }
    }
    
    /**
     * Updates target volume and surface area for a region.
     *
     * @param region  the region
     * @param rate  the rate of change
     * @param scale  the relative final size scaling
     */
    public void updateTarget(Region region, double rate, double scale) {
        double regionVolume = getVolume(region);
        targetVolume -= targetRegionVolumes.get(region);
        
        double criticalRegionVolume = criticalsRegion.get(region).get(Term.VOLUME);
        double criticalRegionHeight = criticalsRegion.get(region).get(Term.HEIGHT);
        
        double oldTargetRegionVolume = targetRegionVolumes.get(region);
        double updateVolume = regionVolume + rate * (scale * criticalRegionVolume - regionVolume);
        
        // Ensure that target volume increases or decreases monotonically.
        if ((scale > 1 && updateVolume < oldTargetRegionVolume)
                || (scale < 1 && updateVolume > oldTargetRegionVolume)) {
            updateVolume = oldTargetRegionVolume;
        }
        
        targetRegionVolumes.put(region, updateVolume);
        double updateSurface = location.convertSurface(updateVolume, criticalRegionHeight);
        targetRegionSurfaces.put(region, updateSurface);
        
        targetVolume += targetRegionVolumes.get(region);
        targetSurface = location.convertSurface(targetVolume, criticals.get(Term.HEIGHT));
    }
    
    @Override
    public CellContainer convert() {
        if (hasRegions) {
            EnumMap<Region, Integer> regionVolumes = new EnumMap<>(Region.class);
            for (Region region : location.getRegions()) {
                regionVolumes.put(region, location.getVolume(region));
            }
            
            return new PottsCellContainer(id, parent, pop, age, state,
                    ((PottsModule) module).getPhase(), getVolume(), regionVolumes,
                    targetVolume, targetSurface, targetRegionVolumes, targetRegionSurfaces);
        } else {
            return new PottsCellContainer(id, parent, pop, age, state,
                    ((PottsModule) module).getPhase(), getVolume(),
                    targetVolume, targetSurface);
        }
    }
}
