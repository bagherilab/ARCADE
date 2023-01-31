package arcade.core.env.location;

import java.util.EnumSet;
import static arcade.core.util.Enums.Region;

/**
 * A {@code Location} object defines the cell location within the environment.
 * <p>
 * Each agent has a {@code Location} that identifies where they are within the
 * {@link arcade.core.env.grid.Grid} (relative to other agents) and the
 * {@link arcade.core.env.lattice.Lattice} (local molecule concentrations).
 */

public interface Location {
    /**
     * Converts the location into a {@link LocationContainer}.
     *
     * @param id  the location id
     * @return  a {@link LocationContainer} instance
     */
    LocationContainer convert(int id);
    
    /**
     * Gets a set of regions.
     *
     * @return  the set of regions
     */
    EnumSet<Region> getRegions();
    
    /**
     * Gets the volume of the location.
     *
     * @return  the location volume
     */
    double getVolume();
    
    /**
     * Gets the volume of the location for a given region.
     *
     * @param region  the region
     * @return  the location region volume
     */
    double getVolume(Region region);
    
    /**
     * Gets the surface area of the location.
     *
     * @return  the location surface area
     */
    double getSurface();
    
    /**
     * Gets the surface area of the location for a given region.
     *
     * @param region  the region
     * @return  the location region surface area
     */
    double getSurface(Region region);
    
    /**
     * Gets the height of the location.
     *
     * @return  the location height
     */
    double getHeight();
    
    /**
     * Gets the height of the location for a given region.
     *
     * @param region  the region
     * @return  the location height
     */
    double getHeight(Region region);
}
