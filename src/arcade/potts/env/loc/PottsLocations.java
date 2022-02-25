package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.env.loc.LocationContainer;
import static arcade.core.util.Enums.Region;

/**
 * Abstract extension of {@link PottsLocation} for regions.
 * <p>
 * {@code PottsLocations} objects manage an additional map of region to
 * {@link PottsLocation} objects that manages the specific subsets of voxels
 * for each region.
 * <p>
 * Concrete implementations of {@code PottsLocations} manage the dimensionality
 * of the voxels.
 * <p>
 * {@code PottsLocations} also provides several additional general static methods
 * for manipulating voxel lists needed for regions:
 * <ul>
 *     <li><strong>Assign</strong> voxels in lists to regions</li>
 *     <li><strong>Select</strong> voxels to assign based on distance</li>
 * </ul>
 */

public abstract class PottsLocations extends PottsLocation {
    /** Map of region to location. */
    protected EnumMap<Region, PottsLocation> locations;
    
    /**
     * Creates a {@code PottsLocations} for a list of voxels.
     *
     * @param voxels  the list of voxels
     */
    public PottsLocations(ArrayList<Voxel> voxels) {
        super(voxels);
        this.locations = new EnumMap<>(Region.class);
        
        ArrayList<Voxel> voxelCopy = new ArrayList<>(voxels);
        locations.put(Region.DEFAULT, makeLocation(voxelCopy));
    }
    
    @Override
    public ArrayList<Voxel> getVoxels(Region region) {
        return (locations.containsKey(region)
                ? new ArrayList<>(locations.get(region).voxels)
                : new ArrayList<>());
    }
    
    @Override
    public EnumSet<Region> getRegions() { return EnumSet.copyOf(locations.keySet()); }
    
    @Override
    public double getVolume(Region region) {
        return (locations.containsKey(region) ? locations.get(region).volume : 0);
    }
    
    @Override
    public double getSurface(Region region) {
        return (locations.containsKey(region) ? locations.get(region).surface : 0);
    }
    
    @Override
    public double getHeight(Region region) {
        return (locations.containsKey(region) ? locations.get(region).height : 0);
    }
    
    @Override
    public void add(int x, int y, int z) {
        super.add(x, y, z);
        locations.get(Region.DEFAULT).add(x, y, z);
    }
    
    @Override
    public void add(Region region, int x, int y, int z) {
        super.add(x, y, z);
        
        Voxel voxel = new Voxel(x, y, z);
        
        for (PottsLocation loc : locations.values()) {
            if (loc.voxels.contains(voxel)) { return; }
        }
        
        if (!locations.containsKey(region)) {
            locations.put(region, makeLocation(new ArrayList<>()));
        }
        
        locations.get(region).add(x, y, z);
    }
    
    @Override
    public void remove(int x, int y, int z) {
        super.remove(x, y, z);
        locations.forEach((region, location) -> location.remove(x, y, z));
    }
    
    @Override
    public void remove(Region region, int x, int y, int z) {
        Voxel voxel = new Voxel(x, y, z);
        
        if (locations.containsKey(region) && !locations.get(region).voxels.contains(voxel)) {
            return;
        }
        
        super.remove(x, y, z);
        
        if (locations.containsKey(region)) {
            locations.get(region).remove(x, y, z);
        }
    }
    
    @Override
    public void assign(Region region, Voxel voxel) {
        Region oldRegion = Region.UNDEFINED;
        
        // Check all regions for the voxel.
        for (Region key : locations.keySet()) {
            if (key != region && locations.get(key).voxels.contains(voxel)) { oldRegion = key; }
        }
        
        // Only assign if voxel exists and is assigned to a different region.
        if (oldRegion == Region.UNDEFINED) { return; }
        
        // Create new region location if it does not exist.
        if (!locations.containsKey(region)) {
            locations.put(region, makeLocation(new ArrayList<>()));
        }
        
        locations.get(region).voxels.add(voxel);
        locations.get(region).volume++;
        locations.get(region).surface += locations.get(region).updateSurface(voxel);
        locations.get(region).height += locations.get(region).updateHeight(voxel);
        locations.get(region).updateCenter(voxel.x, voxel.y, voxel.z, 1);
        
        locations.get(oldRegion).voxels.remove(voxel);
        locations.get(oldRegion).volume--;
        locations.get(oldRegion).surface -= locations.get(oldRegion).updateSurface(voxel);
        locations.get(oldRegion).height -= locations.get(oldRegion).updateHeight(voxel);
        locations.get(oldRegion).updateCenter(voxel.x, voxel.y, voxel.z, -1);
    }
    
    @Override
    public void distribute(Region region, int target, MersenneTwisterFast random) {
        if (region == Region.DEFAULT) { return; }
        
        PottsLocation defaultLocation = locations.get(Region.DEFAULT);
        PottsLocation regionLocation = locations.get(region);
        ArrayList<Voxel> regionVoxels = new ArrayList<>(regionLocation.voxels);
        
        // Select assignment center from the region voxels, if it exists,
        // otherwise from default voxels.
        Voxel center;
        if (regionVoxels.size() == 0) {
            center = defaultLocation.adjust(defaultLocation.getCenter());
        } else {
            center = regionLocation.adjust(regionLocation.getCenter());
            for (Voxel voxel : regionVoxels) {
                assign(Region.DEFAULT, voxel);
            }
        }
        
        // Select voxels and make sure they are connected. Remove any that
        // are not connected.
        ArrayList<Voxel> selected = getSelected(center, target);
        checkVoxels(selected, this, random, true);
        
        // Add or remove voxels to hit the target number.
        int currentSize = selected.size();
        if (currentSize < target) {
            PottsLocationFactory.increase(voxels, selected, target, random);
        } else if (currentSize > target) {
            PottsLocationFactory.decrease(selected, target, random);
        }
        
        // Reassign selected voxels.
        selected.forEach(voxel -> assign(region, voxel));
    }
    
    @Override
    public void clear(int[][][] ids, int[][][] regions) {
        for (Voxel voxel : voxels) {
            ids[voxel.z][voxel.x][voxel.y] = 0;
            regions[voxel.z][voxel.x][voxel.y] = 0;
        }
        
        voxels.clear();
        locations.clear();
    }
    
    @Override
    public void update(int id, int[][][] ids, int[][][] regions) {
        super.update(id, ids, regions);
        
        for (Region region : locations.keySet()) {
            locations.get(region).update(region.ordinal(), regions, null);
        }
    }
    
    @Override
    public LocationContainer convert(int id) {
        EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
        for (Region region : locations.keySet()) {
            regions.put(region, locations.get(region).voxels);
        }
        return new PottsLocationContainer(id, getCenter(), voxels, regions);
    }
    
    @Override
    public double[] getCentroid(Region region) {
        return (locations.containsKey(region) ? locations.get(region).getCentroid() : null);
    }
    
    /**
     * Makes a new {@code PottsLocations} with the given voxels.
     *
     * @param voxels  the list of voxels
     * @return  a new {@code PottsLocations}
     */
    abstract PottsLocations makeLocations(ArrayList<Voxel> voxels);
    
    /**
     * Separates the voxels in the list between this location and a new location.
     * <p>
     * Regions are re-assigned between the two splits.
     *
     * @param voxelsA  the list of voxels for this location
     * @param voxelsB  the list of voxels for the split location
     * @param random  the seeded random number generator
     * @return  a {@link arcade.core.env.loc.Location} object with the split voxels
     */
    @Override
    Location separateVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
                            MersenneTwisterFast random) {
        PottsLocations splitLocation = makeLocations(voxelsB);
        EnumMap<Region, Double> fractions = new EnumMap<>(Region.class);
        int total = voxels.size();
        
        // Update voxels in current location.
        for (Region region : locations.keySet()) {
            // Track fraction of voxels for each region.
            fractions.put(region, (double) locations.get(region).voxels.size() / total);
            
            // Assign to default region if in current split (A), otherwise remove
            // because it is in the new split (B).
            ArrayList<Voxel> regionVoxels = new ArrayList<>(locations.get(region).voxels);
            for (Voxel voxel : regionVoxels) {
                if (voxelsA.contains(voxel)) {
                    assign(Region.DEFAULT, voxel);
                } else {
                    remove(voxel.x, voxel.y, voxel.z);
                }
            }
            
            // Create empty regions in split location.
            if (!splitLocation.locations.containsKey(region)) {
                splitLocation.locations.put(region, makeLocation(new ArrayList<>()));
            }
        }
        
        // Assign voxel regions.
        for (Region region : locations.keySet()) {
            // No assignment for default regions.
            if (region == Region.DEFAULT) { continue; }
            
            // Get target number of voxels to assign for current split.
            int target = (int) (fractions.get(region) * this.volume);
            this.distribute(region, target, random);
            
            // Get target number of voxels to assign for new split.
            int splitTarget = (int) (fractions.get(region) * splitLocation.volume);
            splitLocation.distribute(region, splitTarget, random);
        }
        
        return splitLocation;
    }
}
