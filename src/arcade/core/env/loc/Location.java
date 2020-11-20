package arcade.core.env.loc;

import java.util.ArrayList;
import java.util.EnumSet;
import ec.util.MersenneTwisterFast;
import arcade.potts.env.loc.Voxel;
import static arcade.core.agent.cell.Cell.Region;

public interface Location {
	/** Location split directions */
	enum Direction {
		/** Direction along the yz plane (y = 0, z = 0) */
		YZ_PLANE,
		
		/** Direction along the zx plane (z = 0, x = 0) */
		ZX_PLANE,
		
		/** Direction along the xy plane (x = 0, y = 0) */
		XY_PLANE,
		
		/** Direction along the positive xy axis (x = y, z = 0) */
		POSITIVE_XY,
		
		/** Direction along the negative xy axis (x = -y, z = 0) */
		NEGATIVE_XY,
		
		/** Direction along the positive yz axis (y = z, x = 0) */
		POSITIVE_YZ,
		
		/** Direction along the negative yz axis (y = -z, x = 0) */
		NEGATIVE_YZ,
		
		/** Direction along the positive zx axis (z = x, y = 0) */
		POSITIVE_ZX,
		
		/** Direction along the negative zx axis (z = -x, y = 0) */
		NEGATIVE_ZX
	}
	
	/**
	 * Adds a voxel at the given coordinates.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void add(int x, int y, int z);
	
	/**
	 * Adds a voxel at the given coordinates for given region.
	 *
	 * @param region  the voxel region
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void add(Region region, int x, int y, int z);
	
	/**
	 * Removes the voxel at the given coordinates.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void remove(int x, int y, int z);
	
	/**
	 * Removes the voxel at the given coordinates for given region.
	 * 
	 * @param region  the voxel region
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void remove(Region region, int x, int y, int z);
	
	/**
	 * Assigns the voxel at the given coordinates to the given region.
	 *
	 * @param region  the voxel region
	 * @param voxel  the voxel to assign
	 */
	void assign(Region region, Voxel voxel);
	
	/**
	 * Clears all voxel lists and arrays.
	 *
	 * @param ids  the potts array for ids
	 * @param regions  the potts array for regions
	 */
	void clear(int[][][] ids, int[][][] regions);
	
	/**
	 * Updates the array for the location.
	 *
	 * @param id  the location id
	 * @param ids  the potts array for ids
	 * @param regions  the potts array for regions
	 */
	void update(int id, int[][][] ids, int[][][] regions);
	
	/**
	 * Splits the location voxels into two lists.
	 *
	 * @param random  the seeded random number generator
	 * @return  a location with the split voxels
	 */
	Location split(MersenneTwisterFast random);
	
	/**
	 * Gets all voxels.
	 *
	 * @return  the list of voxels.
	 */
	ArrayList<Voxel> getVoxels();
	
	/**
	 * Gets a set of regions.
	 * 
	 * @return  the set of regions
	 */
	EnumSet<Region> getRegions();
	
	/**
	 * Gets the voxel at the center of the location.
	 *
	 * @return  the center voxel, returns {@code null} if there are no voxels
	 */
	Voxel getCenter();
	
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