package arcade.patch.sim;

import java.util.ArrayList;
import arcade.core.sim.Simulation.Representation;
import arcade.env.grid.*;
import arcade.env.lat.*;
import arcade.env.loc.*;

/** 
 * Implements {@link arcade.core.sim.Simulation.Representation} for rectangular representation.
 * <p>
 * Cell agents exist on a rectangular grid and molecules in the environment
 * diffuse on a smaller rectangular lattice, such that each grid rectangle
 * corresponds to 4 lattice rectangles.
 * The rectangular locations are defined in the (x, y, z) coordinate space such
 * that (0,0,0) is the rectangle in the center of the simulation (for the grid)
 * and the left rectangle of the center slice (for the lattices).
 */

public class RectangularRepresentation implements Representation {
    /** Series object containing this simulation */
    final Series series;
    
    /** Length of the simulation lattice (x direction) */
    private final int length;
    
    /** Width of the simulation lattice (y direction) */
    private final int width;
    
    /** Depth of the simulation lattice (z direction) */
    private final int depth;
    
    /**
     * Representation with rectangular geometry.
     * <p>
     * Passes {@link arcade.core.sim.Series} to {@link arcade.env.loc.PatchLocationRect} to
     * update static configuration variables for the entire simulation.
     * Length, width, and depth of the rectangular lattice are calculated based
     * on radius and height of the rectangular grid.
     * 
     * @param series  the simulation series
     */
    public RectangularRepresentation(Series series) {
        this.series = series;
        PatchLocationRect.updateConfigs(series);
        
        // Calculate length, width, and depth for lattice.
        length = 4*series._radiusBounds - 2;
        width = 4*series._radiusBounds - 2;
        depth = 2*series._heightBounds - 1;
    }
    
    public Grid getNewGrid() { return new PatchGridRect(); }
    
    public Lattice getNewLattice(double val) {
        return new PatchLatticeRect(length, width, depth, val);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Spanning locations use the hexagons aligned with the axes, i.e., all 
     * (u, v, w) for which one of the values is zero.
     */
    public Location[][][] getSpanLocations() {
        int radius = series._radius;
        int height = series._height;
        
        Location[][][] span = new Location[2*height - 1][radius][4];
        int k;
        
        for (int z = -height + 1; z < height; z++) {
            for (int r = 0; r < series._radius; r++) {
                k = z + height - 1;
                span[k][r] = getLocationSet(r, z);
            }
        }

        return span;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Center location in rectangular grid located at (0,0,0).
     */
    public Location getCenterLocation() { return new PatchLocationRect(0,0,0); }
    
    /**
     * Returns the axis-aligned locations for a given radius.
     *
     * @param r  the radius from the center of the environment
     * @param z  the z axis coordinate
     * @return  the list of locations
     */
    private static PatchLocationRect[] getLocationSet(int r, int z) {
        return new PatchLocationRect[] {
            new PatchLocationRect(0, r, z),
            new PatchLocationRect(0, -r, z),
            new PatchLocationRect(r, 0, z),
            new PatchLocationRect(-r, 0, z),
        };
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Includes locations in an approximate sphere of the given radius.
     * For 2D simulations (height = 1), includes locations within a circle of
     * the given radius.
     * <p>
     * If {@code FULL_INIT} is used, then the returned locations include the
     * square locations with coordinates within the given radius.
     */
    public ArrayList<Location> getInitLocations(int radius) {
        // Return single slice for 2D case.
        if (series._height == 1) {
            if (radius == Series.FULL_INIT) { return getLocations(series._radius, series._height); }
            else { return getEqualLocations(radius); }
        }
        
        ArrayList<Location> locations = new ArrayList<>();
        PatchLocationRect loc = new PatchLocationRect(0,0,0);
        if (radius == 1) { locations.add(loc); }
        else if (radius == Series.FULL_INIT) { locations.addAll(getLocations(series._radius, series._height)); }
        else {
            // Calculate length of hypotenuse.
            double side = loc.getGridSize()/2;
            double len = 2*radius - 1;
            double c = len*side;
            
            // Determine radius for each slice to approximate a sphere.
            for (int i = 0; i < series._height; i++) {
                // Calculate lengths of the other two sides of the triangle.
                double b = i*loc.getHeight();
                double a = Math.sqrt(c*c - b*b);
                
                // Check if a is not a number.
                if (a == a) {
                    // Calculate radius corresponding to triangle length.
                    int ah = (int)Math.round(a/side);
                    int r = (int)(Math.ceil(ah/2.0) + (ah - 1)%2);
                    
                    // Add locations within that radius to list.
                    locations.addAll(getSlice(r, i));
                    if (i != 0) { locations.addAll(getSlice(r, -i)); }
                }
            }
        }
        
        return locations;
    }
    
    /**
     * Gets a list of locations within a circle of the given radius.
     *
     * @param radius  the radius of the circle bounds
     * @param height  the z axis coordinate
     * @return  a list of locations   
     */
    private ArrayList<Location> getSlice(int radius, int height) {
        ArrayList<Location> locations = new ArrayList<>();
        
        for (int x = 1 - radius; x < radius; x++) {
            for (int y = 1 - radius; y < radius; y++) {
                if (Math.sqrt(x*x + y*y) <= radius) {
                    locations.add(new PatchLocationRect(x, y, height));
                }
            }
        }
        
        return locations;
    }
    
    public ArrayList<Location> getLocations(int radius, int height) {
        ArrayList<Location> locations = new ArrayList<>();
        
        for (int x = 1 - radius; x < radius; x++) {
            for (int y = 1 - radius; y < radius; y++) {
                for (int z = 1 - height; z < height; z++) {
                    locations.add(new PatchLocationRect(x,y,z));
                }
            }
        }
        
        return locations;
    }
    
    /**
     * Gets a list of locations within the given radius.
     * <p>
     * Note that the {@code getLocations} method returns all locations for any
     * coordinate within the given radius, while this method returns only
     * locations within the circle defined by the given radius.
     * 
     * @param radius  the radius of the circle bounds
     * @return  a list of locations
     */
    private ArrayList<Location> getEqualLocations(int radius) {
        ArrayList<Location> locations = new ArrayList<>();
        
        for (int x = 1 - radius; x < radius; x++) {
            for (int y = 1 - radius; y < radius; y++) {
                if (Math.sqrt(x*x + y*y) <= radius) {
                    locations.add(new PatchLocationRect(x,y,0));
                }
            }
        }
        
        return locations;
    }
}
