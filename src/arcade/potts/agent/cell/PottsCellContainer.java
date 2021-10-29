package arcade.potts.agent.cell;

import java.util.EnumMap;
import arcade.core.agent.cell.*;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Implementation of {@link CellContainer} for {@link PottsCell} agents.
 * <p>
 * The container can be instantiated for cells with or without regions and
 * with or without target sizes.
 * Cell parameters and critical sizing are drawn from the associated
 * {@link PottsCellFactory} instance.
 */

public final class PottsCellContainer implements CellContainer {
    /** Unique cell container ID. */
    public final int id;
    
    /** Cell parent ID. */
    public final int parent;
    
    /** Cell population index. */
    public final int pop;
    
    /** Cell age (in ticks). */
    public final int age;
    
    /** Number of divisions. */
    public final int divisions;
    
    /** Cell state. */
    public final State state;
    
    /** Cell phase. */
    public final Phase phase;
    
    /** Cell size (in voxels). */
    public final int voxels;
    
    /** Cell region sizes (in voxels). */
    public final EnumMap<Region, Integer> regionVoxels;
    
    /** Target cell volume (in voxels). */
    public final double targetVolume;
    
    /** Target cell surface (in voxels). */
    public final double targetSurface;
    
    /** Target region cell volumes (in voxels). */
    public final EnumMap<Region, Double> regionTargetVolume;
    
    /** Target region cell surfaces (in voxels). */
    public final EnumMap<Region, Double> regionTargetSurface;
    
    /**
     * Creates a {@code PottsCellContainer} instance.
     * <p>
     * The default state is proliferative (phase G1).
     * The container does not have any regions or targets.
     *
     * @param id  the cell ID
     * @param pop  the cell population index
     * @param age  the cell age
     * @param divisions  the number of cell divisions
     * @param voxels  the cell size (in voxels)
     */
    public PottsCellContainer(int id, int pop, int age, int divisions, int voxels) {
        this(id, 0, pop, age, divisions, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels,
                null, 0, 0, null, null);
    }
    
    /**
     * Creates a {@code PottsCellContainer} instance.
     * <p>
     * The default state is proliferative (phase G1).
     * The container does not have any targets.
     *
     * @param id  the cell ID
     * @param pop  the cell population index
     * @param age  the cell age
     * @param divisions  the number of cell divisions
     * @param voxels  the cell size (in voxels)
     * @param regionVoxels  the cell region sizes (in voxels)
     */
    public PottsCellContainer(int id, int pop, int age, int divisions, int voxels,
                              EnumMap<Region, Integer> regionVoxels) {
        this(id, 0, pop, age, divisions, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels,
                regionVoxels, 0, 0, null, null);
    }
    
    /**
     * Creates a {@code PottsCellContainer} instance.
     * <p>
     * The container does not have any regions.
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param age  the cell age
     * @param divisions  the number of cell divisions
     * @param state  the cell state
     * @param phase  the cell phase
     * @param voxels  the cell size (in voxels)
     * @param targetVolume  the target volume
     * @param targetSurface  the target surface
     */
    public PottsCellContainer(int id, int parent, int pop, int age, int divisions,
                              State state, Phase phase, int voxels,
                              double targetVolume, double targetSurface) {
        this(id, parent, pop, age, divisions, state, phase, voxels,
                null, targetVolume, targetSurface, null, null);
    }
    
    /**
     * Creates a {@code PottsCellContainer} instance.
     *
     * @param id  the cell ID
     * @param parent  the parent ID
     * @param pop  the cell population index
     * @param age  the cell age
     * @param divisions  the number of cell divisions
     * @param state  the cell state
     * @param phase  the cell phase
     * @param voxels  the cell size (in voxels)
     * @param regionVoxels  the cell region sizes (in voxels)
     * @param targetVolume  the target volume
     * @param targetSurface  the target surface
     * @param regionTargetVolume  the target region volumes
     * @param regionTargetSurface  the target surface volumes
     */
    public PottsCellContainer(int id, int parent, int pop, int age, int divisions,
                              State state, Phase phase, int voxels,
                              EnumMap<Region, Integer> regionVoxels,
                              double targetVolume, double targetSurface,
                              EnumMap<Region, Double> regionTargetVolume,
                              EnumMap<Region, Double> regionTargetSurface) {
        this.id = id;
        this.parent = parent;
        this.pop = pop;
        this.age = age;
        this.divisions = divisions;
        this.state = state;
        this.phase = phase;
        this.voxels = voxels;
        this.regionVoxels = regionVoxels;
        this.targetVolume = targetVolume;
        this.targetSurface = targetSurface;
        this.regionTargetVolume = regionTargetVolume;
        this.regionTargetSurface = regionTargetSurface;
    }
    
    @Override
    public int getID() { return id; }
    
    @Override
    public Cell convert(CellFactory factory, Location location) {
        return convert((PottsCellFactory) factory, location);
    }
    
    /**
     * Converts the cell container into a {@link PottsCell}.
     *
     * @param factory  the cell factory instance
     * @param location  the cell location
     * @return  a {@link PottsCell} instance
     */
    private Cell convert(PottsCellFactory factory, Location location) {
        // Get copies of critical values.
        MiniBox parameters = factory.popToParameters.get(pop);
        double criticalVolume = factory.popToCriticalVolumes.get(pop);
        double criticalHeight = factory.popToCriticalHeights.get(pop);
        
        // Make cell.
        PottsCell cell;
        
        if (factory.popToRegions.get(pop)) {
            // Initialize region arrays.
            EnumMap<Region, Double> criticalRegionVolumes = new EnumMap<>(Region.class);
            EnumMap<Region, Double> criticalRegionHeights = new EnumMap<>(Region.class);
            
            // Get copies of critical values.
            for (Region region : location.getRegions()) {
                criticalRegionVolumes.put(region,
                        factory.popToRegionCriticalVolumes.get(pop).get(region));
                criticalRegionHeights.put(region,
                        factory.popToRegionCriticalHeights.get(pop).get(region));
            }
            
            cell = new PottsCell(id, parent, pop, state, age, divisions,
                    location, true, parameters, criticalVolume, criticalHeight,
                    criticalRegionVolumes, criticalRegionHeights);
        } else {
            cell = new PottsCell(id, parent, pop, state, age, divisions,
                    location, false, parameters, criticalVolume, criticalHeight,
                    null, null);
        }
        
        // Update cell targets.
        cell.setTargets(targetVolume, targetSurface);
        if (regionTargetVolume != null && regionTargetSurface != null) {
            for (Region region : location.getRegions()) {
                cell.setTargets(region,
                        regionTargetVolume.get(region),
                        regionTargetSurface.get(region));
            }
        }
        
        // Update cell module.
        PottsModule module = (PottsModule) cell.getModule();
        if (module != null) { module.setPhase(phase); }
        
        return cell;
    }
}
