package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.env.loc.LocationContainer;
import arcade.core.util.Utilities;
import static arcade.core.util.Enums.Region;

public abstract class PottsLocations extends PottsLocation {
    private static final int MAX_ITERATIONS = 100;
    
    /** Map of region to location */
    public EnumMap<Region, PottsLocation> locations;
    
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
    
    public EnumSet<Region> getRegions() { return EnumSet.copyOf(locations.keySet()); }
    
    public int getVolume(Region region) { return (locations.containsKey(region) ? locations.get(region).volume : 0); }
    
    public int getSurface(Region region) { return (locations.containsKey(region) ? locations.get(region).surface : 0); }
    
    public void add(int x, int y, int z) {
        super.add(x, y, z);
        locations.get(Region.DEFAULT).add(x, y, z);
    }
    
    public void add(Region region, int x, int y, int z) {
        super.add(x, y, z);
        
        Voxel voxel = new Voxel(x, y, z);
        for (PottsLocation loc : locations.values()) {
            if (loc.voxels.contains(voxel)) { return; }
        }
        
        if (!locations.containsKey(region)) { locations.put(region, makeLocation(new ArrayList<>())); }
        locations.get(region).add(x, y, z);
    }
    
    public void remove(int x, int y, int z) {
        super.remove(x, y, z);
        for (PottsLocation location : locations.values()) { location.remove(x, y, z); }
    }
    
    public void remove(Region region, int x, int y, int z) {
        Voxel voxel = new Voxel(x, y, z);
        if (locations.containsKey(region) && !locations.get(region).voxels.contains(voxel)) { return; }
        super.remove(x, y, z);
        if (locations.containsKey(region)) { locations.get(region).remove(x, y, z); }
    }
    
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
        
        locations.get(oldRegion).voxels.remove(voxel);
        locations.get(oldRegion).volume--;
        locations.get(oldRegion).surface -= locations.get(oldRegion).updateSurface(voxel);
    }
    
    public void clear(int[][][] ids, int[][][] regions) {
        for (Voxel voxel : voxels) {
            ids[voxel.z][voxel.x][voxel.y] = 0;
            regions[voxel.z][voxel.x][voxel.y] = 0;
        }
        
        voxels.clear();
        locations.clear();
    }
    
    public void update(int id, int[][][] ids, int[][][] regions) {
        super.update(id, ids, regions);
        
        for (Region region : locations.keySet()) {
            locations.get(region).update(region.ordinal(), regions, null);
        }
    }
    
    public LocationContainer convert(int id) {
        EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
        for (Region region : locations.keySet()) { regions.put(region, locations.get(region).voxels); }
        return new PottsLocationContainer(id, getCenter(), voxels, regions);
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
     * Regionged regions are re-assigned between the two splits.
     * 
     * @param voxelsA  the list of voxels for this location
     * @param voxelsB  the list of voxels for the split location
     * @param random  the seeded random number generator 
     * @return  a {@link arcade.core.env.loc.Location} object with the split voxels
     */
    Location separateVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
                            MersenneTwisterFast random) {
        PottsLocations splitLocation = makeLocations(voxelsB);
        EnumMap<Region, Double> fractions = new EnumMap<>(Region.class);
        
        // Update voxels in current location.
        for (Region region : locations.keySet()) {
            // Track fraction of voxels for each region.
            fractions.put(region, (double)locations.get(region).voxels.size()/voxels.size());
            
            // Assign to default region if in current split (A), otherwise remove
            // because it is in the new split (B).
            ArrayList<Voxel> regionVoxels = new ArrayList<>(locations.get(region).voxels);
            for (Voxel voxel : regionVoxels) {
                if (voxelsA.contains(voxel)) { assign(Region.DEFAULT, voxel); }
                else { remove(voxel.x, voxel.y, voxel.z); }
            }
            
            // Create empty regions in split location.
            if (!splitLocation.locations.containsKey(region)) {
                splitLocation.locations.put(region, makeLocation(new ArrayList<>()));
            }
        }
        
        // Assign voxel regions.
        assignVoxels(this, fractions, random);
        assignVoxels(splitLocation, fractions, random);
        
        return splitLocation;
    }
    
    /**
     * Assigns regions to voxels based on given fractions.
     * 
     * @param location  the location containing voxels to assign
     * @param fractions  the region fractions
     * @param random  the seeded random number generator 
     */
    static void assignVoxels(PottsLocations location, EnumMap<Region, Double> fractions,
                             MersenneTwisterFast random) {
        ArrayList<Voxel> defaultVoxels = location.locations.get(Region.DEFAULT).voxels;
        
        for (Region region : location.locations.keySet()) {
            // No assignment for default regions.
            if (region == Region.DEFAULT) { continue; }
            
            // Get approximate number of voxels to assign.
            double n = fractions.get(region)*location.volume;
            
            // Select assignment center. If the center voxel doesn't exist,
            // then select random voxel.
            Voxel center = location.locations.get(Region.DEFAULT).getCenter();
            if (!defaultVoxels.contains(center)) {
                center = defaultVoxels.get(random.nextInt(defaultVoxels.size()));
            }
            
            // Select voxels to region.
            selectVoxels(location, center, region, defaultVoxels, n, random);
        }
    }
    
    /**
     * Selects voxels to assign based on distance from given center.
     * 
     * @param location  the location containing voxels to assign
     * @param center  the center voxel
     * @param region  the region to assign
     * @param voxels  the list of available voxels to assign
     * @param n  the target number of voxels to assign
     * @param random  the seeded random number generator 
     */
    static void selectVoxels(PottsLocations location, Voxel center, Region region, ArrayList<Voxel> voxels,
                             double n, MersenneTwisterFast random) {
        ArrayList<Voxel> selected = location.getSelected(center, n);
        
        // Check that selected voxels are connected (remove any that are not).
        checkVoxels(selected, location, random, true);
        
        int currentSize = selected.size();
        int iter = 0;
        
        // Add additional voxels if selected voxels is less than target number.
        while (currentSize < n && iter < MAX_ITERATIONS) {
            HashSet<Voxel> neighbors = new HashSet<>();
            
            // Get all valid connected neighbor voxels.
            for (Voxel voxel : selected) {
                ArrayList<Voxel> allNeighbors = location.getNeighbors(voxel);
                for (Voxel neighbor : allNeighbors) {
                    if (voxels.contains(neighbor) && !selected.contains(neighbor)) { neighbors.add(neighbor); }
                }
            }
            
            // Add in random neighbors until target size is reached. If the number
            // of neighbors is less than the different between the current and
            // target sizes, then add all neighbors.
            if (neighbors.size() < n - currentSize) {
                selected.addAll(neighbors);
            } else {
                ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
                Utilities.shuffleList(neighborsShuffled, random);
                for (int i = 0; i < n - currentSize; i++) {
                    selected.add(neighborsShuffled.get(i));
                }
            }
            
            // Update iteration and current selected size.
            iter++;
            currentSize = selected.size();
        }
        
        // Reassign selected voxels.
        for (Voxel voxel : selected) { location.assign(region, voxel); }
    }
}
