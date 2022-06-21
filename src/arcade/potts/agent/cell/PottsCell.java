package arcade.potts.agent.cell;

import java.util.EnumMap;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Stoppable;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellContainer;
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.env.loc.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.agent.module.PottsModuleApoptosisSimple;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationSimple;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Ordering;

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
 * To integrate with the Potts layer, {@code PottsCell} agents also contain
 * critical and target volumes and surfaces.
 * Additional parameters specific to each term in the Hamiltonian are tracked
 * by the specific Hamiltonian class instance.
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
    
    /** Number of divisions. */
    private int divisions;
    
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
    
    /** Critical volume for cell (in voxels). */
    private final double criticalVolume;
    
    /** Critical volumes for cell (in voxels) by region. */
    private final EnumMap<Region, Double> criticalRegionVolumes;
    
    /** Critical height for cell (in voxels). */
    private final double criticalHeight;
    
    /** Critical heights for cell (in voxels) by region. */
    private final EnumMap<Region, Double> criticalRegionHeights;
    
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
     * @param divisions  the number of cell divisions
     * @param location  the {@link arcade.core.env.loc.Location} of the cell
     * @param hasRegions  {@code true} if the cell has regions, {@code false} otherwise
     * @param parameters  the dictionary of parameters
     * @param criticalVolume  the critical cell volume (in voxels)
     * @param criticalHeight  the critical cell height (in voxels)
     * @param criticalRegionVolumes  the map of critical volumes for regions
     * @param criticalRegionHeights  the map of critical heights for regions
     */
    public PottsCell(int id, int parent, int pop, State state, int age, int divisions,
                     Location location, boolean hasRegions, MiniBox parameters,
                     double criticalVolume, double criticalHeight,
                     EnumMap<Region, Double> criticalRegionVolumes,
                     EnumMap<Region, Double> criticalRegionHeights) {
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.divisions = divisions;
        this.hasRegions = hasRegions;
        this.location = (PottsLocation) location;
        this.parameters = parameters;
        this.criticalVolume = criticalVolume;
        this.criticalHeight = criticalHeight;
        
        setState(state);
        
        if (hasRegions) {
            this.criticalRegionVolumes = criticalRegionVolumes.clone();
            this.criticalRegionHeights = criticalRegionHeights.clone();
            this.targetRegionVolumes = new EnumMap<>(Region.class);
            this.targetRegionSurfaces = new EnumMap<>(Region.class);
        } else {
            this.criticalRegionVolumes = null;
            this.criticalRegionHeights = null;
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
    public int getDivisions() { return divisions; }
    
    @Override
    public boolean hasRegions() { return hasRegions; }
    
    @Override
    public Location getLocation() { return location; }
    
    @Override
    public Module getModule() { return module; }
    
    @Override
    public Process getProcess(String key) { return null; }
    
    @Override
    public MiniBox getParameters() { return parameters; }
    
    @Override
    public double getVolume() { return location.getVolume(); }
    
    @Override
    public double getVolume(Region region) {
        return (hasRegions ? location.getVolume(region) : 0);
    }
    
    @Override
    public double getHeight() { return location.getHeight(); }
    
    @Override
    public double getHeight(Region region) {
        return (hasRegions ? location.getHeight(region) : 0);
    }
    
    /**
     * Gets the cell surface (in voxels).
     *
     * @return  the cell surface
     */
    public double getSurface() { return location.getSurface(); }
    
    /**
     * Gets the cell surface (in voxels) for a region.
     *
     * @param region  the region
     * @return  the cell region surface
     */
    public double getSurface(Region region) {
        return (hasRegions ? location.getSurface(region) : 0);
    }
    
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
    public double getCriticalVolume() { return criticalVolume; }
    
    @Override
    public double getCriticalVolume(Region region) {
        return (hasRegions && criticalRegionVolumes.containsKey(region)
                ? criticalRegionVolumes.get(region)
                : 0);
    }
    
    @Override
    public double getCriticalHeight() { return criticalHeight; }
    
    @Override
    public double getCriticalHeight(Region region) {
        return (hasRegions && criticalRegionHeights.containsKey(region)
                ? criticalRegionHeights.get(region)
                : 0);
    }
    
    @Override
    public void stop() { stopper.stop(); }
    
    @Override
    public PottsCell make(int newID, State newState, Location newLocation) {
        divisions++;
        return new PottsCell(newID, id, pop, newState, age, divisions, newLocation,
                hasRegions, parameters, criticalVolume, criticalHeight,
                criticalRegionVolumes, criticalRegionHeights);
    }
    
    @Override
    public void setState(State state) {
        this.state = state;
        
        switch (state) {
            case QUIESCENT:
                module = new PottsModuleQuiescence(this);
                break;
            case PROLIFERATIVE:
                module = new PottsModuleProliferationSimple(this);
                break;
            case APOPTOTIC:
                module = new PottsModuleApoptosisSimple(this);
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
        
        targetVolume = location.getVolume();
        targetSurface = location.getSurface();
        
        if (!hasRegions) { return; }
        
        for (Region region : location.getRegions()) {
            targetRegionVolumes.put(region, location.getVolume(region));
            targetRegionSurfaces.put(region, location.getSurface(region));
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
        
        targetVolume = criticalVolume;
        targetSurface = location.convertSurface(targetVolume, criticalHeight);
        
        if (!hasRegions) { return; }
        
        for (Region region : location.getRegions()) {
            double regionHeight = criticalRegionHeights.get(region);
            double regionVolume = criticalRegionVolumes.get(region);
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
     * When scale is greater than one, volume increases by given rate.
     * When scale is less than one, volume decreases by given rate.
     * If scale is one, sizes are not changed.
     *
     * @param rate  the rate of change
     * @param scale  the relative final size scaling
     */
    public void updateTarget(double rate, double scale) {
        if (scale == 1) { return; }
        
        if (hasRegions) {
            if (scale < 1) { rate = Math.min(rate, targetRegionVolumes.get(Region.DEFAULT)); }
            double updateVolume = targetRegionVolumes.get(Region.DEFAULT) - targetVolume;
            targetRegionVolumes.put(Region.DEFAULT, updateVolume);
        }
        
        if (scale > 1) {
            targetVolume += rate;
            targetVolume = Math.min(targetVolume, scale * criticalVolume);
        } else {
            targetVolume -= rate;
            targetVolume = Math.max(targetVolume, scale * criticalVolume);
        }
        
        targetSurface = location.convertSurface(targetVolume, criticalHeight);
        
        if (hasRegions) {
            double updateVolume = targetRegionVolumes.get(Region.DEFAULT) + targetVolume;
            targetRegionVolumes.put(Region.DEFAULT, updateVolume);
            
            double criticalRegionHeight = criticalRegionHeights.get(Region.DEFAULT);
            double updateSurface = location.convertSurface(updateVolume, criticalRegionHeight);
            targetRegionSurfaces.put(Region.DEFAULT, updateSurface);
        }
    }
    
    /**
     * Updates target volume and surface area for a region.
     * If the region is the DEFAULT region, then updates are the same as for a
     * cell without regions.
     *
     * @param region  the region
     * @param rate  the rate of change
     * @param scale  the relative final size scaling
     */
    public void updateTarget(Region region, double rate, double scale) {
        if (!hasRegions || scale == 1) { return; }
        
        if (region == Region.DEFAULT) {
            updateTarget(rate, scale);
            return;
        }
        
        if (scale > 1) { rate = Math.min(rate, targetRegionVolumes.get(Region.DEFAULT)); }
        
        double criticalRegionVolume = criticalRegionVolumes.get(region);
        double criticalRegionHeight = criticalRegionHeights.get(region);
        
        double updateVolume = targetRegionVolumes.get(region);
        
        double preUpdateVolume = targetRegionVolumes.get(Region.DEFAULT) + updateVolume;
        targetRegionVolumes.put(Region.DEFAULT, preUpdateVolume);
        
        if (scale > 1) {
            updateVolume += rate;
            updateVolume = Math.min(updateVolume, scale * criticalRegionVolume);
        } else {
            updateVolume -= rate;
            updateVolume = Math.max(updateVolume, scale * criticalRegionVolume);
        }
        
        targetRegionVolumes.put(region, updateVolume);
        double updateSurface = location.convertSurface(updateVolume, criticalRegionHeight);
        targetRegionSurfaces.put(region, updateSurface);
        
        double postUpdateVolume = targetRegionVolumes.get(Region.DEFAULT) - updateVolume;
        targetRegionVolumes.put(Region.DEFAULT, postUpdateVolume);
        
        double defaultRegionHeight = criticalRegionHeights.get(Region.DEFAULT);
        double postUpdateSurface = location.convertSurface(postUpdateVolume, defaultRegionHeight);
        targetRegionSurfaces.put(Region.DEFAULT, postUpdateSurface);
    }
    
    @Override
    public CellContainer convert() {
        if (hasRegions) {
            EnumMap<Region, Integer> regionVolumes = new EnumMap<>(Region.class);
            for (Region region : location.getRegions()) {
                regionVolumes.put(region, (int) location.getVolume(region));
            }
            
            return new PottsCellContainer(id, parent, pop, age, divisions, state,
                    ((PottsModule) module).getPhase(), (int) getVolume(), regionVolumes,
                    criticalVolume, criticalHeight, criticalRegionVolumes, criticalRegionHeights);
        } else {
            return new PottsCellContainer(id, parent, pop, age, divisions, state,
                    ((PottsModule) module).getPhase(), (int) getVolume(),
                    criticalVolume, criticalHeight);
        }
    }
}
