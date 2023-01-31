package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import static arcade.core.util.Enums.Region;

/**
 * Implementation of {@link LocationFactory} for {@link PottsLocation} objects.
 * <p>
 * For a given {@link Series}, the factory uses its associated random number
 * generator for shuffling voxel lists. The voxel lists a
 * {@link PottsLocationContainer} are combined with a
 * {@link arcade.potts.agent.cell.PottsCellContainer} to instantiate a
 * {@link PottsLocation} object.
 */

public abstract class PottsLocationFactory implements LocationFactory {
    /** List of valid (x, y, z) moves. */
    static final int[][] VALID_MOVES = {
            { -1,  0,  0 },
            {  1,  0,  0 },
            {  0, -1,  0 },
            {  0,  1,  0 },
            {  0,  0, -1 },
            {  0,  0,  1 }
    };
    
    /** Random number generator instance. */
    MersenneTwisterFast random;
    
    /** Map of id to location. */
    public final HashMap<Integer, PottsLocationContainer> locations;
    
    /**
     * Creates a factory for making {@link PottsLocation} instances.
     */
    public PottsLocationFactory() {
        locations = new HashMap<>();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * For series with no loader, a list of available centers is created based
     * on population settings. For series with a loader, the specified file is
     * loaded into the factory.
     */
    @Override
    public void initialize(Series series, MersenneTwisterFast random) {
        this.random = random;
        if (series.loader != null && series.loader.loadLocations) {
            loadLocations(series);
        } else {
            createLocations(series);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Loaded locations are mapped by their id.
     */
    @Override
    public void loadLocations(Series series) {
        // Load locations.
        ArrayList<LocationContainer> containers = series.loader.loadLocations();
        
        // Map loaded container to factory.
        for (LocationContainer container : containers) {
            PottsLocationContainer locationContainer = (PottsLocationContainer) container;
            locations.put(locationContainer.id, locationContainer);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The potts layer is subdivided into a grid of voxel sets. Each of these
     * sets is used to create a location container. Note that these sets contain
     * more voxels than required for instantiating a location; voxels are
     * selected from the center outward until the required number of voxels
     * (defined by a cell container) is selected. Containers are assigned
     * regions if they exist.
     */
    @Override
    public void createLocations(Series series) {
        int heightRange = getVoxelsPerHeight(series);
        int sideRange = getVoxelsPerSide(series, heightRange);
        
        if (sideRange == 0) {
            return;
        }
        
        // Get center voxels.
        ArrayList<Voxel> centers = getCenters(series.length, series.width, series.height,
                series.margin, sideRange, heightRange);
        Utilities.shuffleList(centers, random);
        centers.sort(Comparator.comparingInt(v -> v.z));
        
        // Get regions (if they exist).
        HashSet<String> regionKeys = new HashSet<>();
        for (MiniBox population : series.populations.values()) {
            MiniBox regionBox = population.filter("(REGION)");
            if (regionBox.getKeys().size() > 0) {
                regionKeys.addAll(regionBox.getKeys());
            }
        }
        
        // Create containers for each center.
        int id = 1;
        for (Voxel center : centers) {
            ArrayList<Voxel> voxels = getPossible(center, sideRange, heightRange);
            
            // Add regions (if they exist).
            EnumMap<Region, ArrayList<Voxel>> regions = null;
            if (regionKeys.size() > 0) {
                regions = new EnumMap<>(Region.class);
                for (String regionKey : regionKeys) {
                    Region region = Region.valueOf(regionKey);
                    regions.put(region, getPossible(center, sideRange - 2, heightRange));
                }
            }
            
            PottsLocationContainer container =
                    new PottsLocationContainer(id, center, voxels, regions);
            locations.put(id, container);
            id++;
        }
    }
    
    /**
     * Converts volume to voxels per square side.
     *
     * @param volume  the target volume
     * @param height  the target height
     * @return  the voxels per side
     */
    int convert(double volume, double height) {
        int sqrt = (int) Math.ceil(Math.sqrt(volume / height));
        return sqrt + (sqrt % 2 == 0 ? 1 : 0);
    }
    
    /**
     * Finds the maximum height range between centers based on critical height.
     * The height range is at least one and at most equal to the height of the
     * simulation series with two voxel padding.
     *
     * @param series  the simulation series
     * @return  the voxel height range
     */
    int getVoxelsPerHeight(Series series) {
        int heightRange = 1;
        
        for (MiniBox population : series.populations.values()) {
            double criticalHeight = population.getDouble("CRITICAL_HEIGHT_MEAN");
            int voxelsPerHeight = (int) (Math.min(series.height - 2, Math.ceil(criticalHeight)));
            if (voxelsPerHeight > heightRange) {
                heightRange = voxelsPerHeight;
            }
        }
        
        return heightRange;
    }
    
    /**
     * Finds the maximum side range between centers based on critical volume.
     * The side range may be zero if no cells can fit in the given simulation
     * series and includes a two voxel padding.
     *
     * @param series  the simulation series
     * @param heightRange  the voxel height range
     * @return  the voxel sides range
     */
    int getVoxelsPerSide(Series series, int heightRange) {
        int sideRange = 0;
        
        for (MiniBox population : series.populations.values()) {
            double criticalVolume = population.getDouble("CRITICAL_VOLUME_MEAN");
            int padding = population.getInt("PADDING");
            int voxelsPerSide = convert(2 * criticalVolume, heightRange) + padding;
            if (voxelsPerSide > sideRange) {
                sideRange = voxelsPerSide;
            }
        }
        
        return sideRange;
    }
    
    /**
     * Selects specified number of voxels from a focus voxel.
     *
     * @param voxels  the list of voxels to select from
     * @param focus  the focus voxel
     * @param n  the number of voxels to select
     * @return  the list of selected voxels
     */
    abstract ArrayList<Voxel> getSelected(ArrayList<Voxel> voxels, Voxel focus, double n);
    
    /**
     * Gets all possible voxels within given range.
     *
     * @param focus  the focus voxel
     * @param sideRange  the location range per side
     * @param heightRange  the location range per height
     * @return  the list of possible voxels
     */
    abstract ArrayList<Voxel> getPossible(Voxel focus, int sideRange, int heightRange);
    
    /**
     * Gets all centers for the given range.
     *
     * @param length  the array length
     * @param width  the array width
     * @param height  the array height
     * @param margin  the location margin
     * @param sideRange  the location range per side
     * @param heightRange  the location range per height
     * @return  the list of center voxels
     */
    abstract ArrayList<Voxel> getCenters(int length, int width, int height, int margin,
                                         int sideRange, int heightRange);
    
    /**
     * Gets list of valid voxels around a given voxel.
     *
     * @param voxel  the voxel
     * @return  the list of valid voxels
     */
    static ArrayList<Voxel> getValid(Voxel voxel) {
        ArrayList<Voxel> valid = new ArrayList<>();
        for (int[] moves : VALID_MOVES) {
            Voxel v = new Voxel(voxel.x + moves[0], voxel.y + moves[1], voxel.z + moves[2]);
            valid.add(v);
        }
        return valid;
    }
    
    /**
     * Increases the number of voxels by adding from given list of voxels.
     *
     * @param allVoxels  the list of all possible voxels
     * @param voxels  the list of selected voxels
     * @param target  the target number of voxels
     * @param random  the seeded random number generator
     */
    static void increase(ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target,
                         MersenneTwisterFast random) {
        int size = voxels.size();
        HashSet<Voxel> neighbors = new HashSet<>();
        
        // Get neighbors.
        for (Voxel voxel : voxels) {
            ArrayList<Voxel> allNeighbors = getValid(voxel);
            for (Voxel neighbor : allNeighbors) {
                if (allVoxels.contains(neighbor) && !voxels.contains(neighbor)) {
                    neighbors.add(neighbor);
                }
            }
        }
        
        // Add in random neighbors until target size is reached.
        ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
        Utilities.shuffleList(neighborsShuffled, random);
        int n = Math.min(target - size, neighborsShuffled.size());
        for (int i = 0; i < n; i++) {
            voxels.add(neighborsShuffled.get(i));
        }
    }
    
    /**
     * Decreases the number of voxels by removing from given list of voxels.
     *
     * @param voxels  the list of selected voxels
     * @param target  the target number of voxels
     * @param random  the seeded random number generator
     */
    static void decrease(ArrayList<Voxel> voxels, int target, MersenneTwisterFast random) {
        int size = voxels.size();
        
        // Remove random voxels until target size is reached.
        ArrayList<Voxel> voxelsShuffled = new ArrayList<>(voxels);
        Utilities.shuffleList(voxelsShuffled, random);
        int index = 0;
        
        for (int i = 0; i < size - target; i++) {
            // Return if there are no remaining neighbors to remove.
            if (index == voxelsShuffled.size()) {
                return;
            }
            
            Voxel candidate = voxelsShuffled.get(index++);
            
            // Always remove if the target number of voxels is one.
            if (target == 1) {
                voxels.remove(candidate);
                continue;
            }
            
            // Check candidate. Do not remove a candidate if it has a neighbor
            // that has only one neighbor (i.e. the candidate is the only connection)
            ArrayList<Voxel> candidateNeighbors = new ArrayList<>();
            for (Voxel neighbor : getValid(candidate)) {
                if (voxels.contains(neighbor)) {
                    candidateNeighbors.add(neighbor);
                }
            }
            
            // Check neighbors of neighbor list.
            boolean valid = true;
            for (Voxel neighbor : candidateNeighbors) {
                int count = getValid(neighbor).stream()
                        .mapToInt(v -> voxels.contains(v) ? 1 : 0)
                        .sum();
                
                if (count == 1) {
                    valid = false;
                    break;
                }
            }
            
            if (valid) {
                voxels.remove(candidate);
            } else {
                i--;
            }
        }
    }
}
