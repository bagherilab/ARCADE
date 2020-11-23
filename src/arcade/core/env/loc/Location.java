package arcade.core.env.loc;

import java.util.EnumSet;
import static arcade.core.agent.cell.Cell.Region;

public interface Location {
	/**
	 * Gets a set of regions.
	 * 
	 * @return  the set of regions
	 */
	EnumSet<Region> getRegions();
	
	/**
	 * Gets the volume of the location.
	 *
	 * @return  the location volume (in voxels)
	 */
	int getVolume();
	
	/**
	 * Gets the volume of the location for a given region.
	 * 
	 * @param region  the voxel region
	 * @return  the location volume (in voxels)
	 */
	int getVolume(Region region);
	
	/**
	 * Gets the surface area of the location.
	 *
	 * @return  the location surface area (in voxels)
	 */
	int getSurface();
	
	/**
	 * Gets the surface area of the location for a given region.
	 * 
	 * @param region  the voxel region
	 * @return  the location surface area (in voxels)
	 */
	int getSurface(Region region);
}