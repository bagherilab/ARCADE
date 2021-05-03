package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.env.loc.LocationContainer;
import arcade.core.util.Utilities;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Direction;

/**
 * Abstract implementation of {@link Location} for potts models.
 * <p>
 * {@code PottsLocation} objects manage the a list of associated {@link Voxel}
 * objects that comprise the location.
 * These voxels are represented as an array in the {@link arcade.potts.sim.Potts}
 * layer and the two representations are kept in sync.
 * <p>
 * Concrete implementations of {@code PottsLocation} manage the dimensionality
 * of the voxels.
 * <p>
 * {@code PottsLocation} also provides several general static methods for
 * manipulating voxel lists:
 * <ul>
 *     <li><strong>Split</strong> voxel list along a given direction</li>
 *     <li><strong>Connect</strong> voxels to ensure that each list contains
 *     connected voxels</li>
 *     <li><strong>Balance</strong> voxels between two lists to ensure they have
 *     a similar number of voxels</li>
 *     <li><strong>Check</strong> voxels for connectedness</li>
 * </ul>
 */

public abstract class PottsLocation implements Location {
    /** Relative difference between split voxel numbers. */
    private static final double BALANCE_DIFFERENCE = 0.05;
    
    /** List of voxels for the location. */
    final ArrayList<Voxel> voxels;
    
    /** Location volume. */
    int volume;
    
    /** Location surface. */
    int surface;
    
    /** Location height. */
    int height;
    
    /**
     * Creates a {@code PottsLocation} for a list of voxels.
     *
     * @param voxels  the list of voxels
     */
    public PottsLocation(ArrayList<Voxel> voxels) {
        this.voxels = new ArrayList<>(voxels);
        this.volume = voxels.size();
        this.surface = calculateSurface();
        this.height = calculateHeight();
    }
    
    /**
     * Gets all voxels.
     *
     * @return  the list of voxels.
     */
    public ArrayList<Voxel> getVoxels() { return new ArrayList<>(voxels); }
    
    @Override
    public EnumSet<Region> getRegions() { return null; }
    
    @Override
    public final int getVolume() { return volume; }
    
    @Override
    public int getVolume(Region region) { return getVolume(); }
    
    @Override
    public final int getSurface() { return surface; }
    
    @Override
    public int getSurface(Region region) { return getSurface(); }
    
    @Override
    public final int getHeight() { return height; }
    
    @Override
    public int getHeight(Region region) { return getHeight(); }
    
    /**
     * Adds a voxel at the given coordinates.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     */
    public void add(int x, int y, int z) {
        Voxel voxel = new Voxel(x, y, z);
        if (!voxels.contains(voxel)) {
            voxels.add(voxel);
            volume++;
            surface += updateSurface(voxel);
            height += updateHeight(voxel);
        }
    }
    
    /**
     * Adds a voxel at the given coordinates for given region.
     *
     * @param region  the voxel region
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     */
    public void add(Region region, int x, int y, int z) { add(x, y, z); }
    
    /**
     * Removes the voxel at the given coordinates.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     */
    public void remove(int x, int y, int z) {
        Voxel voxel = new Voxel(x, y, z);
        if (voxels.contains(voxel)) {
            voxels.remove(voxel);
            volume--;
            surface -= updateSurface(voxel);
            height -= updateHeight(voxel);
        }
    }
    
    /**
     * Removes the voxel at the given coordinates for given region.
     *
     * @param region  the voxel region
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     */
    public void remove(Region region, int x, int y, int z) { remove(x, y, z); }
    
    /**
     * Assigns the voxel at the given coordinates to the given region.
     *
     * @param region  the voxel region
     * @param voxel  the voxel to assign
     */
    public void assign(Region region, Voxel voxel) { }
    
    /**
     * Clears all voxel lists and arrays.
     *
     * @param ids  the potts array for ids
     * @param regions  the potts array for regions
     */
    public void clear(int[][][] ids, int[][][] regions) {
        voxels.forEach(voxel -> ids[voxel.z][voxel.x][voxel.y] = 0);
        voxels.clear();
    }
    
    /**
     * Updates the array for the location.
     *
     * @param id  the location id
     * @param ids  the potts array for ids
     * @param regions  the potts array for regions
     */
    public void update(int id, int[][][] ids, int[][][] regions) {
        voxels.forEach(voxel -> ids[voxel.z][voxel.x][voxel.y] = id);
    }
    
    /**
     * Splits the location voxels into two lists.
     * <p>
     * The location are split along the direction with the shortest diameter.
     * The lists of locations are guaranteed to be connected, and generally will
     * be balanced in size.
     * One of the splits is assigned to the current location and the other is
     * returned.
     *
     * @param random  the seeded random number generator
     * @return  a location with the split voxels
     */
    public Location split(MersenneTwisterFast random) {
        // Get center voxel.
        Voxel center = getCenter();
        
        // Initialize lists of split voxels.
        ArrayList<Voxel> voxelsA = new ArrayList<>();
        ArrayList<Voxel> voxelsB = new ArrayList<>();
        
        // Get split direction.
        Direction direction = getDirection(random);
        splitVoxels(direction, voxels, voxelsA, voxelsB, center, random);
        
        // Ensure that voxel split is connected and balanced.
        connectVoxels(voxelsA, voxelsB, this, random);
        balanceVoxels(voxelsA, voxelsB, this, random);
        
        // Select one split to keep for this location and return the other.
        if (random.nextDouble() < 0.5) {
            return separateVoxels(voxelsA, voxelsB, random);
        } else {
            return separateVoxels(voxelsB, voxelsA, random);
        }
    }
    
    /**
     * Gets the voxel at the center of the location.
     *
     * @return  the center voxel, returns {@code null} if there are no voxels
     */
    public Voxel getCenter() {
        if (voxels.size() == 0) { return null; }
        return new Voxel(getCenterX(), getCenterY(), getCenterZ());
    }
    
    /**
     * Gets the x coordinate of the voxel at the center of the location.
     *
     * @return  the x coordinate
     */
    int getCenterX() {
        double x = voxels.stream().mapToDouble(voxel -> voxel.x).sum();
        return (int) Math.round(x / voxels.size());
    }
    
    /**
     * Gets the y coordinate of the voxel at the center of the location.
     *
     * @return  the y coordinate
     */
    int getCenterY() {
        double y = voxels.stream().mapToDouble(voxel -> voxel.y).sum();
        return (int) Math.round(y / voxels.size());
    }
    
    /**
     * Gets the z coordinate of the voxel at the center of the location.
     *
     * @return  the z coordinate
     */
    int getCenterZ() {
        double z = voxels.stream().mapToDouble(voxel -> voxel.z).sum();
        return (int) Math.round(z / voxels.size());
    }
    
    /**
     * Makes a new {@code PottsLocation} with the given voxels.
     *
     * @param voxels  the list of voxels
     * @return  a new {@code PottsLocation}
     */
    abstract PottsLocation makeLocation(ArrayList<Voxel> voxels);
    
    /**
     * Converts volume and height to surface area.
     *
     * @param volume  the volume (in voxels)
     * @param height  the height (in voxels)
     * @return  the surface area (in voxels)
     */
    public abstract double convertSurface(double volume, double height);
    
    /**
     * Calculates surface of location.
     *
     * @return  the surface
     */
    abstract int calculateSurface();
    
    /**
     * Calculates the local change in surface of the location.
     *
     * @param voxel  the voxel the update is centered in
     * @return  the change in surface
     */
    abstract int updateSurface(Voxel voxel);
    
    /**
     * Calculates height of location.
     *
     * @return  the height
     */
    abstract int calculateHeight();
    
    /**
     * Calculates the local change in height of the location.
     *
     * @param voxel  the voxel the update is centered in
     * @return  the change in height
     */
    abstract int updateHeight(Voxel voxel);
    
    /**
     * Gets list of neighbors of a given voxel.
     *
     * @param focus  the focus voxel
     * @return  the list of neighbor voxels
     */
    abstract ArrayList<Voxel> getNeighbors(Voxel focus);
    
    /**
     * Calculates diameters in each direction.
     *
     * @return  the map of direction to diameter
     */
    abstract HashMap<Direction, Integer> getDiameters();
    
    /**
     * Selects the slice direction for a given minimum diameter direction.
     *
     * @param direction  the direction of the minimum diameter
     * @param diameters  the list of diameters
     * @return  the slice direction
     */
    abstract Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters);
    
    /**
     * Selects specified number of voxels from a focus voxel.
     *
     * @param focus  the focus voxel
     * @param n  the number of voxels to select
     * @return  the list of selected voxels
     */
    abstract ArrayList<Voxel> getSelected(Voxel focus, double n);
    
    /**
     * Gets the direction of the slice.
     *
     * @param random  the seeded random number generator
     * @return  the direction of the slice
     */
    Direction getDirection(MersenneTwisterFast random) {
        HashMap<Direction, Integer> diameters = getDiameters();
        ArrayList<Direction> directions = new ArrayList<>();
        
        // Determine minimum diameter.
        int diameter;
        int minimumDiameter = Integer.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            diameter = diameters.getOrDefault(direction, Integer.MAX_VALUE);
            if (diameter < minimumDiameter) { minimumDiameter = diameter; }
        }
        
        // Find all directions with the minimum diameter.
        for (Direction direction : Direction.values()) {
            diameter = diameters.getOrDefault(direction, Integer.MAX_VALUE);
            if (diameter == minimumDiameter) { directions.add(direction); }
        }
        
        // Randomly select one direction with the minimum diameter.
        Direction d = directions.get(random.nextInt(directions.size()));
        
        // Convert diameter direction to slice direction.
        return getSlice(d, diameters);
    }
    
    @Override
    public LocationContainer convert(int id) {
        return new PottsLocationContainer(id, getCenter(), voxels);
    }
    
    /**
     * Separates the voxels in the list between this location and a new location.
     *
     * @param voxelsA  the list of voxels for this location
     * @param voxelsB  the list of voxels for the split location
     * @param random  the seeded random number generator
     * @return  a {@link arcade.core.env.loc.Location} object with the split voxels
     */
    Location separateVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
                            MersenneTwisterFast random) {
        voxels.clear();
        voxels.addAll(voxelsA);
        volume = voxels.size();
        surface = calculateSurface();
        return makeLocation(voxelsB);
    }
    
    /**
     * Splits the voxels in the location along a given direction.
     *
     * @param direction  the direction of the slice
     * @param voxels  the list of voxels
     * @param voxelsA  the container list for the first half of the split
     * @param voxelsB  the container list for the second half of the split
     * @param center  the center voxel
     * @param random  the seeded random number generator
     */
    static void splitVoxels(Direction direction, ArrayList<Voxel> voxels,
                            ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
                            Voxel center, MersenneTwisterFast random) {
        for (Voxel voxel : voxels) {
            switch (direction) {
                case ZX_PLANE:
                    if (voxel.y < center.y) {
                        voxelsA.add(voxel);
                    } else if (voxel.y > center.y) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case YZ_PLANE:
                    if (voxel.x < center.x) {
                        voxelsA.add(voxel);
                    } else if (voxel.x > center.x) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case XY_PLANE:
                    if (voxel.z < center.z) {
                        voxelsA.add(voxel);
                    } else if (voxel.z > center.z) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case POSITIVE_XY:
                    if (voxel.x - center.x > center.y - voxel.y) {
                        voxelsA.add(voxel);
                    } else if (voxel.x - center.x < center.y - voxel.y) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case NEGATIVE_XY:
                    if (voxel.x - center.x > voxel.y - center.y) {
                        voxelsA.add(voxel);
                    } else if (voxel.x - center.x < voxel.y - center.y) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case POSITIVE_YZ:
                    if (voxel.y - center.y > center.z - voxel.z) {
                        voxelsA.add(voxel);
                    } else if (voxel.y - center.y < center.z - voxel.z) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case NEGATIVE_YZ:
                    if (voxel.y - center.y > voxel.z - center.z) {
                        voxelsA.add(voxel);
                    } else if (voxel.y - center.y < voxel.z - center.z) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case POSITIVE_ZX:
                    if (voxel.z - center.z > center.x - voxel.x) {
                        voxelsA.add(voxel);
                    } else if (voxel.z - center.z < center.x - voxel.x) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                case NEGATIVE_ZX:
                    if (voxel.z - center.z > voxel.x - center.x) {
                        voxelsA.add(voxel);
                    } else if (voxel.z - center.z < voxel.x - center.x) {
                        voxelsB.add(voxel);
                    } else {
                        if (random.nextDouble() > 0.5) {
                            voxelsA.add(voxel);
                        } else {
                            voxelsB.add(voxel);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Connects voxels in the splits.
     * <p>
     * Checks that the voxels in each split are connected.
     * If not, then move the unconnected voxels into the other split.
     *
     * @param voxelsA  the list for the first half of the split
     * @param voxelsB  the list for the second half of the split
     * @param location  the location instance
     * @param random  the seeded random number generator
     */
    static void connectVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
                              PottsLocation location, MersenneTwisterFast random) {
        // Check that both coordinate lists are simply connected.
        ArrayList<Voxel> unconnectedA = checkVoxels(voxelsA, location, random, true);
        ArrayList<Voxel> unconnectedB = checkVoxels(voxelsB, location, random, true);
        
        // If either coordinate list is not connected, attempt to connect them
        // by adding in the unconnected coordinates of the other list.
        while (unconnectedA != null || unconnectedB != null) {
            ArrayList<Voxel> unconnectedAB;
            ArrayList<Voxel> unconnectedBA;
            
            if (unconnectedA != null) { voxelsB.addAll(unconnectedA); }
            unconnectedBA = checkVoxels(voxelsB, location, random, true);
            
            if (unconnectedB != null) { voxelsA.addAll(unconnectedB); }
            unconnectedAB = checkVoxels(voxelsA, location, random, true);
            
            unconnectedA = unconnectedAB;
            unconnectedB = unconnectedBA;
        }
    }
    
    /**
     * Balances voxels in the splits.
     * <p>
     * Checks that the number of voxels in each split are within a certain
     * difference.
     * If not, then add voxels from the larger split into the smaller split
     * such that both splits are still connected.
     * For small split sizes, there may not be a valid split that is both
     * connected and within the difference; in these cases, connectedness is
     * prioritized and the splits are returned not balanced.
     *
     * @param voxelsA  the list for the first half of the split
     * @param voxelsB  the list for the second half of the split
     * @param location  the location instance
     * @param random  the seeded random number generator
     */
    static void balanceVoxels(ArrayList<Voxel> voxelsA, ArrayList<Voxel> voxelsB,
                              PottsLocation location, MersenneTwisterFast random) {
        int nA = voxelsA.size();
        int nB = voxelsB.size();
        
        while (Math.abs(nA - nB) > Math.ceil((nA + nB) * BALANCE_DIFFERENCE)) {
            ArrayList<Voxel> fromVoxels;
            ArrayList<Voxel> toVoxels;
            
            if (nA > nB) {
                fromVoxels = voxelsA;
                toVoxels = voxelsB;
            } else {
                fromVoxels = voxelsB;
                toVoxels = voxelsA;
            }
            
            // Get all valid neighbor voxels.
            LinkedHashSet<Voxel> neighborSet = new LinkedHashSet<>();
            for (Voxel voxel : toVoxels) {
                ArrayList<Voxel> neighbors = location.getNeighbors(voxel);
                for (Voxel neighbor : neighbors) {
                    if (!toVoxels.contains(neighbor)) { neighborSet.add(neighbor); }
                }
            }
            
            // If one list is empty, add all voxels in other list as neighbors.
            if (toVoxels.size() == 0) { neighborSet.addAll(fromVoxels); }
            
            ArrayList<Voxel> neighborList = new ArrayList<>(neighborSet);
            Utilities.shuffleList(neighborList, random);
            
            // Select a neighbor to move from one list to the other.
            boolean added = false;
            ArrayList<Voxel> invalidCoords = new ArrayList<>();
            for (Voxel voxel : neighborList) {
                if (fromVoxels.contains(voxel)) {
                    toVoxels.add(voxel);
                    fromVoxels.remove(voxel);
                    
                    // Check that removal of coordinate does not cause the list
                    // to become unconnected.
                    ArrayList<Voxel> unconnected = checkVoxels(fromVoxels, location, random, false);
                    if (unconnected == null) {
                        added = true;
                        break;
                    } else {
                        fromVoxels.add(voxel);
                        toVoxels.remove(voxel);
                        invalidCoords.add(voxel);
                    }
                }
            }
            
            if (!added) {
                toVoxels.addAll(invalidCoords);
                fromVoxels.removeAll(invalidCoords);
                connectVoxels(voxelsA, voxelsB, location, random);
                break;
            }
            
            nA = voxelsA.size();
            nB = voxelsB.size();
        }
    }
    
    /**
     * Checks voxels in the list for connectedness.
     * <p>
     * All the connected voxels from a random starting voxel are found and
     * marked as visited.
     * If there are no remaining unvisited voxels, then the list is fully
     * connected.
     * If there are, then the smaller of the visited or unvisited lists is
     * returned.
     * <p>
     * Some voxel lists may have more than one unconnected section.
     *
     * @param voxels  the list of voxels
     * @param location  the location instance
     * @param random  the seeded random number generator
     * @param update  {@code true} if the voxel list should be updated, {@code false} otherwise
     * @return  a list of unconnected voxels, {@code null} if the list is connected
     */
    static ArrayList<Voxel> checkVoxels(ArrayList<Voxel> voxels, PottsLocation location,
                                        MersenneTwisterFast random, boolean update) {
        if (voxels.size() == 0) { return null; }
        
        ArrayList<Voxel> unvisited = new ArrayList<>(voxels);
        ArrayList<Voxel> visited = new ArrayList<>();
        ArrayList<Voxel> nextList;
        LinkedHashSet<Voxel> nextSet;
        LinkedHashSet<Voxel> currSet = new LinkedHashSet<>();
        
        currSet.add(unvisited.get(random.nextInt(unvisited.size())));
        int currSize = currSet.size();
        
        while (currSize > 0) {
            nextSet = new LinkedHashSet<>();
            
            // Iterate through each coordinate in current coordinate set.
            for (Voxel voxel : currSet) {
                nextList = new ArrayList<>();
                
                // Iterate through each connected direction from current voxel
                // and add to neighbor list if it exists.
                ArrayList<Voxel> neighbors = location.getNeighbors(voxel);
                for (Voxel neighbor : neighbors) {
                    if (unvisited.contains(neighbor)) { nextList.add(neighbor); }
                }
                
                // Updated next voxel set with list of neighbors.
                nextSet.addAll(nextList);
                visited.add(voxel);
                unvisited.remove(voxel);
            }
            
            currSet = nextSet;
            currSize = currSet.size();
        }
        
        // If not all coordinates have been visited, then the list of
        // coordinates is not connected.
        if (unvisited.size() != 0) {
            if (unvisited.size() > visited.size()) {
                if (update) { voxels.removeAll(visited); }
                return visited;
            } else {
                if (update) { voxels.removeAll(unvisited); }
                return unvisited;
            }
        } else {
            return null;
        }
    }
}
