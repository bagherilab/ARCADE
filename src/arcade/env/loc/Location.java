package arcade.env.loc;

import ec.util.MersenneTwisterFast;

public interface Location {
	/** Location split directions */
	enum Direction {
		/** Direction along the x axis (y = 0, z = 0) */
		YZ_PLANE,
		
		/** Direction along the y axis (x = 0, z = 0) */
		ZX_PLANE,
		
		/** Direction along the z axis (x = 0, y = 0) */
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
	 * Adds a voxel at the given coordinates for given tag.
	 *
	 * @param tag  the voxel tag
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void add(int tag, int x, int y, int z);
	
	/**
	 * Removes the voxel at the given coordinates.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void remove(int x, int y, int z);
	
	/**
	 * Removes the voxel at the given coordinates for given tag.
	 * 
	 * @param tag  the voxel tag
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void remove(int tag, int x, int y, int z);
	
	/**
	 * Assigns the voxel at the given coordinates to the given tag.
	 * 
	 * @param tag  the voxel tag
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void assign(int tag, int x, int y, int z);
	
	/**
	 * Clears all voxel lists and arrays.
	 * 
	 * @param ids  the potts array for ids
	 * @param tags  the potts array for tags
	 */
	void clear(int[][][] ids, int[][][] tags);
	
	/**
	 * Updates the array for the location.
	 * 
	 * @param id  the location id
	 * @param ids  the potts array for ids
	 * @param tags  the potts array for tags
	 */
	void update(int id, int[][][] ids, int[][][] tags);
	
	/**
	 * Splits the location voxels into two lists.
	 *
	 * @param random  the seeded random number generator
	 * @return  a location with the split voxels
	 */
	Location split(MersenneTwisterFast random);
	
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
	 * Gets the volume of the location for a given tag.
	 * 
	 * @param tag  the voxel tag
	 * @return  the location volume (in voxels)
	 */
	int getVolume(int tag);
	
	/**
	 * Gets the surface area of the location.
	 *
	 * @return  the location surface area (in voxels)
	 */
	int getSurface();
	
	/**
	 * Gets the surface area of the location for a given tag.
	 * 
	 * @param tag  the voxel tag
	 * @return  the location surface area (in voxels)
	 */
	int getSurface(int tag);
	
	class Voxel {
		/** Voxel x coordinate */
		final int x;
		
		/** Voxel y coordinate */
		final int y;
		
		/** Voxel z coordinate */
		final int z;
		
		/**
		 * Creates a {@code Voxel} at the given coordinates.
		 *
		 * @param x  the x coordinate
		 * @param y  the y coordinate
		 * @param z  the z coordinate
		 */
		public Voxel(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		/**
		 * Gets hash based on (x, y, z) coordinates.
		 *
		 * @return  the hash
		 */
		public final int hashCode() { return x + (y << 8) + (z << 16); }
		
		/**
		 * Checks if two locations have the same (x, y, z) coordinates.
		 *
		 * @param obj  the voxel to compare
		 * @return  {@code true} if voxels have the same coordinates, {@code false} otherwise
		 */
		public final boolean equals(Object obj) {
			if (!(obj instanceof Voxel)) { return false; }
			Voxel voxel = (Voxel)obj;
			return voxel.x == x && voxel.y == y && voxel.z == z;
		}
		
		public String toString() {
			return String.format("[%d, %d, %d]", x, y, z);
		}
	}
}