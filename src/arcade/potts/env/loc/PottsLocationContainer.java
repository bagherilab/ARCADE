package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.EnumMap;
import arcade.core.agent.cell.CellContainer;
import arcade.core.env.loc.*;
import arcade.potts.agent.cell.PottsCellContainer;
import static arcade.core.util.Enums.Region;

/**
 * Implementation of {@link LocationContainer} for {@link PottsLocation} objects.
 * <p>
 * The container can be instantiated for locations with or without regions.
 * The voxels available in a container are not necessarily all used when
 * instantiating a {@link Location} instance.
 * Instead, the number of voxels is selected based on the associated
 * {@link PottsCellContainer} and {@link PottsLocationFactory} instance.
 */

public final class PottsLocationContainer implements LocationContainer {
    /** Unique location container ID. */
    public final int id;
    
    /** List of all available voxels. */
    public final ArrayList<Voxel> allVoxels;
    
    /** Voxel at center of available voxels. */
    public final Voxel center;
    
    /** Map of region to list of voxels. */
    public final EnumMap<Region, ArrayList<Voxel>> regions;
    
    /**
     * Creates a {@code PottsLocationContainer} instance.
     * <p>
     * The container does not have any regions.
     *
     * @param id  the location ID
     * @param center  the location center
     * @param voxels  the list of voxels
     */
    public PottsLocationContainer(int id, Voxel center, ArrayList<Voxel> voxels) {
        this(id, center, voxels, null);
    }
    
    /**
     * Creates a {@code PottsLocationContainer} instance.
     *
     * @param id  the location ID
     * @param center  the location center
     * @param voxels  the list of voxels
     * @param regions  the list of region voxels
     */
    public PottsLocationContainer(int id, Voxel center, ArrayList<Voxel> voxels,
                                  EnumMap<Region, ArrayList<Voxel>> regions) {
        this.id = id;
        this.center = center;
        this.allVoxels = voxels;
        this.regions = regions;
    }
    
    @Override
    public int getID() { return id; }
    
    @Override
    public Location convert(LocationFactory factory, CellContainer cell) {
        return convert((PottsLocationFactory) factory, (PottsCellContainer) cell);
    }
    
    /**
     * Converts the location container into a {@link PottsLocation}.
     *
     * @param factory  the cell factory instance
     * @param cell  the cell container
     * @return  a {@link PottsLocation} instance
     */
    private Location convert(PottsLocationFactory factory, PottsCellContainer cell) {
        // Check for 3D.
        boolean is3D = (factory instanceof PottsLocationFactory3D);
        
        // Select voxels.
        int target = cell.voxels;
        ArrayList<Voxel> voxels = select(target, factory, allVoxels);
        
        // Make location.
        PottsLocation location;
        
        // Add regions.
        if (cell.regionVoxels != null) {
            EnumMap<Region, Integer> regionTargetMap = cell.regionVoxels;
            location = (is3D ? new PottsLocations3D(voxels) : new PottsLocations2D(voxels));
            
            for (Region region : Region.values()) {
                // TODO add handling of other regions
                if (region != Region.NUCLEUS) { continue; }
                
                // Select region voxels.
                int regTarget = regionTargetMap.get(region);
                ArrayList<Voxel> allRegVoxels = regions.get(region);
                ArrayList<Voxel> regVoxels = select(regTarget, factory, allRegVoxels);
                
                // Assign regions.
                regVoxels.forEach(voxel -> location.assign(region, voxel));
            }
        } else {
            location = (is3D ? new PottsLocation3D(voxels) : new PottsLocation2D(voxels));
        }
        
        return location;
    }
    
    /**
     * Selects target number of voxels from list of valid voxels.
     *
     * @param target  the target number of voxels
     * @param factory  the location factory instance
     * @param voxels  the list of valid voxels
     * @return  a list of selected voxels
     */
    private ArrayList<Voxel> select(int target, PottsLocationFactory factory,
                                    ArrayList<Voxel> voxels) {
        ArrayList<Voxel> selected;
        
        // Select voxels from list of all valid voxels.
        if (target == voxels.size()) {
            selected = new ArrayList<>(voxels);
        } else {
            selected = factory.getSelected(voxels, center, target);
        }
    
        // Add or remove voxels to reach target number.
        int size = selected.size();
        if (size < target) {
            PottsLocationFactory.increase(voxels, selected, target, factory.random);
        } else if (size > target) {
            PottsLocationFactory.decrease(selected, target, factory.random);
        }
        
        return selected;
    }
}
