package arcade.potts.sim;

import java.util.ArrayList;
import java.util.HashSet;
import sim.engine.SimState;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;
import arcade.core.env.grid.Grid;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Cellular Potts Model (CPM) implementation.
 * <p>
 * The potts layer tracks cells in an array of ids that define the morphology of
 * each cell (alongside non-cell areas). The corresponding array of regions
 * further defines regions within a given cell.
 * <p>
 * The Hamiltonian, which decides which positions in the arrays are flipped,
 * consists of a series of terms including:
 * <ul>
 *     <li>Cell to cell and cell to non-cell adhesion</li>
 *     <li>Volume constraint between actual and target volume</li>
 *     <li>Surface constraint between actual and target surface</li>
 * </ul>
 */

public abstract class Potts implements Steppable {
    /** Length (x direction) of potts array. */
    public final int length;
    
    /** Width (y direction) of potts array. */
    public final int width;
    
    /** Depth (z direction) of potts array. */
    public final int height;
    
    /** Number of steps in Monte Carlo Step. */
    final int steps;
    
    /** Effective cell temperature. */
    final double temperature;
    
    /** {@code true} if potts is single layer, {@code false} otherwise. */
    final boolean isSingle;
    
    /** {@code true} if cells have regions, {@code false} otherwise. */
    final boolean hasRegions;
    
    /** Potts array for ids. */
    public int[][][] ids;
    
    /** Potts array for regions. */
    public int[][][] regions;
    
    /** Grid holding cells. */
    Grid grid;
    
    /** List of Hamiltonian terms. */
    ArrayList<Hamiltonian> hamiltonian;
    
    /**
     * Creates a cellular {@code Potts} model.
     *
     * @param series  the simulation series
     */
    public Potts(PottsSeries series) {
        // Creates potts arrays.
        ids = new int[series.height][series.length][series.width];
        regions = new int[series.height][series.length][series.width];
        
        // Ensure a 1 voxel border around to avoid boundary checks.
        length = series.length - 2;
        width = series.width - 2;
        height = (series.height == 1 ? 1 : series.height - 2);
        
        // Number of Monte Carlo steps
        double mcs = series.potts.getDouble("MCS");
        steps = (int) (mcs * length * width * height);
        
        // Get temperature.
        temperature = series.potts.getDouble("TEMPERATURE");
        
        // Check if potts is a single layer.
        isSingle = series.height == 1;
        
        // Check if there are regions.
        hasRegions = series.populations.values().stream()
                .map(e -> e.filter("(REGION)").getKeys().size())
                .anyMatch(e -> e > 0);
        
        // Initialize hamiltonian list.
        hamiltonian = new ArrayList<>();
        if (series.terms != null) {
            series.terms.stream()
                    .map(term -> getHamiltonian(term, series))
                    .forEach(h -> hamiltonian.add(h));
        }
    }
    
    /**
     * Registers the cell to all Hamiltonian term instances.
     *
     * @param cell  the cell instance
     */
    public void register(PottsCell cell) {
        for (Hamiltonian h : hamiltonian) {
            h.register(cell);
        }
    }
    
    /**
     * Deregisters the cell from all Hamiltonian term instances.
     *
     * @param cell  the cell instance
     */
    public void deregister(PottsCell cell) {
        for (Hamiltonian h : hamiltonian) {
            h.deregister(cell);
        }
    }
    
    /**
     * Gets instance of selected Hamiltonian term.
     *
     * @param term  the Hamiltonian term
     * @param series  the simulation series
     * @return  the Hamiltonian instance
     */
    abstract Hamiltonian getHamiltonian(Term term, PottsSeries series);
    
    /**
     * Steps through array updates for Monte Carlo step.
     *
     * @param simstate  the MASON simulation state
     */
    @Override
    public void step(SimState simstate) {
        MersenneTwisterFast random = simstate.random;
        double r;
        int x;
        int y;
        int z;
        
        for (int step = 0; step < steps; step++) {
            // Get random coordinate for candidate.
            x = random.nextInt(length) + 1;
            y = random.nextInt(width) + 1;
            z = (random.nextInt(height) + 1) * (isSingle ? 0 : 1);
            r = random.nextDouble();
            
            // Check if cell has regions.
            boolean hasRegionsCell = (ids[z][x][y] != 0 && getCell(ids[z][x][y]).hasRegions());
            
            // Get unique targets.
            HashSet<Integer> uniqueIDTargets = getUniqueIDs(x, y, z);
            HashSet<Integer> uniqueRegionTargets = getUniqueRegions(x, y, z);
            
            // Select unique ID (if there is one), otherwise select unique
            // region (if there is one). If there are neither, then skip.
            if (hasRegionsCell && uniqueRegionTargets.size() > 0) {
                int i = simstate.random.nextInt(uniqueRegionTargets.size());
                int targetRegion = (int) uniqueRegionTargets.toArray()[i];
                flip(ids[z][x][y], regions[z][x][y], targetRegion, x, y, z, r);
            } else if (uniqueIDTargets.size() > 0) {
                int i = simstate.random.nextInt(uniqueIDTargets.size());
                int targetID = (int) uniqueIDTargets.toArray()[i];
                flip(ids[z][x][y], targetID, x, y, z, r);
            }
        }
    }
    
    /**
     * Flips connected voxel from source to target id based on Boltzmann
     * probability.
     *
     * @param sourceID  the id of the source voxel
     * @param targetID  the id of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @param r  a random number
     */
    void flip(int sourceID, int targetID, int x, int y, int z, double r) {
        boolean zero = ids[z][x][y] == 0;
        
        // Check connectivity of source.
        if (sourceID > 0) {
            boolean[][][] neighborhood = getNeighborhood(sourceID, x, y, z);
            boolean candidateConnected = getConnectivity(neighborhood, zero);
            if (!candidateConnected) {
                return;
            }
            
            // Check connectivity of regions.
            if (regions[z][x][y] > Region.DEFAULT.ordinal()) {
                boolean[][][] rNeighborhood = getNeighborhood(sourceID, regions[z][x][y], x, y, z);
                boolean candidateRegionConnected = getConnectivity(rNeighborhood, false);
                if (!candidateRegionConnected) {
                    return;
                }
            }
        }
        
        // Check connectivity of target.
        if (targetID > 0) {
            boolean[][][] neighborhood = getNeighborhood(targetID, x, y, z);
            boolean targetConnected = getConnectivity(neighborhood, zero);
            if (!targetConnected) {
                return;
            }
            
            // Check connectivity of regions.
            if (regions[z][x][y] > Region.DEFAULT.ordinal()) {
                boolean[][][] rNeighborhood = getNeighborhood(targetID, regions[z][x][y], x, y, z);
                boolean candidateRegionConnected = getConnectivity(rNeighborhood, false);
                if (!candidateRegionConnected) {
                    return;
                }
            }
        }
        
        // Change the voxel.
        change(sourceID, targetID, x, y, z, r);
    }
    
    /**
     * Calculates energy change to decide if a voxel is flipped.
     *
     * @param sourceID  the id of the source voxel
     * @param targetID  the id of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @param r  a random number
     */
    void change(int sourceID, int targetID, int x, int y, int z, double r) {
        // Calculate energy change.
        double dH = 0;
        for (Hamiltonian h : hamiltonian) {
            dH += h.getDelta(sourceID, targetID, x, y, z);
        }
        
        // Calculate probability.
        double p;
        if (dH < 0) {
            p = 1;
        } else {
            p = Math.exp(-dH / temperature);
        }
        
        if (r < p) {
            ids[z][x][y] = targetID;
            if (hasRegions) {
                regions[z][x][y] = (targetID == 0
                        ? Region.UNDEFINED.ordinal()
                        : Region.DEFAULT.ordinal());
            }
            
            if (sourceID > 0) {
                ((PottsLocation) getCell(sourceID).getLocation()).remove(x, y, z);
            }
            if (targetID > 0) {
                ((PottsLocation) getCell(targetID).getLocation()).add(x, y, z);
            }
        }
    }
    
    /**
     * Flips connected voxel from source to target region based on Boltzmann
     * probability.
     *
     * @param id  the voxel id
     * @param sourceRegion  the region of the source voxel
     * @param targetRegion  the region of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @param r  a random number
     */
    void flip(int id, int sourceRegion, int targetRegion, int x, int y, int z, double r) {
        boolean zero = regions[z][x][y] == Region.DEFAULT.ordinal();
        
        // Check connectivity of source.
        if (sourceRegion > Region.DEFAULT.ordinal()) {
            boolean[][][] neighborhood = getNeighborhood(id, sourceRegion, x, y, z);
            boolean candidateConnected = getConnectivity(neighborhood, zero);
            if (!candidateConnected) {
                return;
            }
        }
        
        // Check connectivity of target.
        if (targetRegion > Region.DEFAULT.ordinal()) {
            boolean[][][] neighborhood = getNeighborhood(id, targetRegion, x, y, z);
            boolean targetConnected = getConnectivity(neighborhood, zero);
            if (!targetConnected) {
                return;
            }
        }
        
        // Change the voxel region.
        change(id, sourceRegion, targetRegion, x, y, z, r);
    }
    
    /**
     * Calculates energy change to decide if a voxel region is flipped.
     *
     * @param id  the voxel id
     * @param sourceRegion  the region of the source voxel
     * @param targetRegion  the region of the target voxel
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @param r  a random number
     */
    void change(int id, int sourceRegion, int targetRegion, int x, int y, int z, double r) {
        // Calculate energy change.
        double dH = 0;
        for (Hamiltonian h : hamiltonian) {
            dH += h.getDelta(id, sourceRegion, targetRegion, x, y, z);
        }
        
        // Calculate probability.
        double p;
        if (dH < 0) {
            p = 1;
        } else {
            p = Math.exp(-dH / temperature);
        }
        
        if (r < p) {
            regions[z][x][y] = targetRegion;
            PottsCell c = getCell(id);
            ((PottsLocation) c.getLocation()).remove(Region.values()[sourceRegion], x, y, z);
            ((PottsLocation) c.getLocation()).add(Region.values()[targetRegion], x, y, z);
        }
    }
    
    /**
     * Gets the {@link PottsCell} object for the given id.
     *
     * @param id  the cell id
     * @return  the {@link PottsCell} object, {@code null} if id is zero
     */
    public PottsCell getCell(int id) {
        if (id > 0) {
            return (PottsCell) grid.getObjectAt(id);
        } else {
            return null;
        }
    }
    
    /**
     * Gets neighborhood for the given voxel.
     *
     * @param id  the voxel id
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    abstract boolean[][][] getNeighborhood(int id, int x, int y, int z);
    
    /**
     * Gets neighborhood for the given voxel region.
     *
     * @param id  the voxel id
     * @param region  the voxel region
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    abstract boolean[][][] getNeighborhood(int id, int region, int x, int y, int z);
    
    /**
     * Determines connectivity of given neighborhood.
     *
     * @param array  the array of neighbors
     * @param zero {@code true} if location has zero id, {@code false} otherwise
     * @return  {@code true} if simply connected, {@code false} otherwise
     */
    abstract boolean getConnectivity(boolean[][][] array, boolean zero);
    
    /**
     * Gets unique IDs adjacent to given voxel.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the list of unique IDs
     */
    abstract HashSet<Integer> getUniqueIDs(int x, int y, int z);
    
    /**
     * Gets unique regions adjacent to given voxel.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @param z  the z coordinate
     * @return  the list of unique regions
     */
    abstract HashSet<Integer> getUniqueRegions(int x, int y, int z);
}
