package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.*;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import static arcade.core.util.Enums.Region;

/**
 * Implementation of {@link LocationFactory} for {@link PottsLocation} objects.
 * <p>
 * For a given {@link Series}, the factory uses its associated random number
 * generator for shuffling voxel lists.
 * The voxel lists a {@link PottsLocationContainer} are combined with a
 * {@link arcade.potts.agent.cell.PottsCellContainer} to instantiate a
 * {@link PottsLocation} object.
 */

public abstract class PottsLocationFactory implements LocationFactory {
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
     * on population settings.
     * For series with a loader, the specified file is loaded into the factory.
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
     * The potts layer is subdivided into a grid of voxel sets.
     * Each of these sets is used to create a location container.
     * Note that these sets contain more voxels than required for instantiating
     * a location; voxels are selected from the center outward until the required
     * number of voxels (defined by a cell container) is selected.
     * Containers are assigned regions if they exist.
     */
    @Override
    public void createLocations(Series series) {
        int m = 0;
        
        // Find largest distance between centers.
        for (MiniBox population : series.populations.values()) {
            double criticalVolume = population.getDouble("CRITICAL_VOLUME");
            int voxelsPerSide = convert(criticalVolume) + 2;
            if (voxelsPerSide > m) { m = voxelsPerSide; }
        }
        
        if (m == 0) { return; }
        
        // Get center voxels.
        ArrayList<Voxel> centers = getCenters(series.length, series.width, series.height, m);
        Utilities.shuffleList(centers, random);
        
        // Get regions (if they exist).
        HashSet<String> regionKeys = new HashSet<>();
        for (MiniBox population : series.populations.values()) {
            MiniBox regionBox = population.filter("(REGION)");
            if (regionBox.getKeys().size() > 0) { regionKeys.addAll(regionBox.getKeys()); }
        }
        
        // Create containers for each center.
        int id = 1;
        for (Voxel center : centers) {
            ArrayList<Voxel> voxels = getPossible(center, series.height, m);
            
            // Add regions (if they exist).
            EnumMap<Region, ArrayList<Voxel>> regions = null;
            if (regionKeys.size() > 0) {
                regions = new EnumMap<>(Region.class);
                for (String regionKey : regionKeys) {
                    Region region = Region.valueOf(regionKey);
                    regions.put(region, getPossible(center, series.height, m - 2));
                }
            }
            
            PottsLocationContainer cont = new PottsLocationContainer(id, center, voxels, regions);
            locations.put(id, cont);
            id++;
        }
    }
    
    /**
     * Converts volume to voxels per square side.
     *
     * @param volume  the target volume
     * @return  the voxels per side
     */
    abstract int convert(double volume);
    
    /**
     * Gets list of neighbors of a given voxel.
     *
     * @param voxel  the voxel
     * @return  the list of neighbor voxels
     */
    abstract ArrayList<Voxel> getNeighbors(Voxel voxel);
    
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
     * @param height  the array height
     * @param m  the location range
     * @return  the list of possible voxels
     */
    abstract ArrayList<Voxel> getPossible(Voxel focus, int height, int m);
    
    /**
     * Gets all centers for the given range.
     *
     * @param length  the array length
     * @param width  the array width
     * @param height  the array height
     * @param m  the location range
     * @return  the list of center voxels
     */
    abstract ArrayList<Voxel> getCenters(int length, int width, int height, int m);
    
    /**
     * Increases the number of voxels by adding from a given list of voxels.
     *
     * @param allVoxels  the list of all possible voxels
     * @param voxels  the list of selected voxels
     * @param target  the target number of voxels
     */
    void increase(ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target) {
        int size = voxels.size();
        HashSet<Voxel> neighbors = new HashSet<>();
        
        // Get neighbors.
        for (Voxel voxel : voxels) {
            ArrayList<Voxel> allNeighbors = getNeighbors(voxel);
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
     * Decreases the number of voxels by removing from the given list of voxels.
     *
     * @param voxels  the list of selected voxels
     * @param target  the target number of voxels
     */
    void decrease(ArrayList<Voxel> voxels, int target) {
        int size = voxels.size();
        ArrayList<Voxel> neighbors = new ArrayList<>();
        
        // Get neighbors.
        for (Voxel voxel : voxels) {
            ArrayList<Voxel> allNeighbors = getNeighbors(voxel);
            for (Voxel neighbor : allNeighbors) {
                if (voxels.contains(neighbor)) { continue; }
                neighbors.add(voxel);
                break;
            }
        }
        
        // Remove random neighbors until target size is reached.
        ArrayList<Voxel> neighborsShuffled = new ArrayList<>(neighbors);
        Utilities.shuffleList(neighborsShuffled, random);
        int index = 0;
        
        for (int i = 0; i < size - target; i++) {
            // Check candidate. Do not remove a candidate if it has a neighbor
            // that has only one neighbor (i.e. the candidate is the only connection)
            Voxel candidate = neighborsShuffled.get(index);
            
            // Get neighbors of candidate.
            ArrayList<Voxel> candidateNeighbors = new ArrayList<>();
            for (Voxel neighbor : getNeighbors(candidate)) {
                if (voxels.contains(neighbor)) {
                    candidateNeighbors.add(neighbor);
                }
            }
            
            // Check neighbors of neighbor list.
            boolean valid = true;
            for (Voxel neighbor : candidateNeighbors) {
                int count = getNeighbors(neighbor).stream()
                        .mapToInt(v -> voxels.contains(v) ? 1 : 0)
                        .sum();
                
                if (count == 1) {
                    valid = false;
                    break;
                }
            }
            
            index++;
            if (valid || target == 1) {
                voxels.remove(candidate);
            } else {
                i--;
            }
        }
    }
}
