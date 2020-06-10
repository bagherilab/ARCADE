package arcade.env.loc;

import ec.util.MersenneTwisterFast;

public interface Location {
	/**
	 * Adds a voxel at the given coordinates.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void add(int x, int y, int z);
	
	/**
	 * Removes the voxel at the given coordinates.
	 *
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void remove(int x, int y, int z);
	
	/**
	 * Updates the array for the location.
	 *
	 * @param array  the potts array
	 * @param id  the location id
	 */
	void update(int[][][] array, int id);
	
	/**
	 * Splits the location voxels into two lists.
	 *
	 * @param random  the seeded random number generator
	 * @return  a list of voxels
	 */
	PottsLocation split(MersenneTwisterFast random);
	
	/**
	 * Gets the voxel at the center of the location.
	 *
	 * @return  the center voxel, returns {@code null} if there are no voxels
	 */
	Voxel getCenter();
	
	class Voxel {
		/** Voxel x coordinate */
		int x;
		
		/** Voxel y coordinate */
		int y;
		
		/** Voxel z coordinate */
		int z;
		
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
			Voxel voxel = (Voxel)obj;
			return voxel.x == x && voxel.y == y && voxel.z == z;
		}
		
		public String toString() {
			return String.format("[%d, %d, %d]", x, y, z);
		}
	}
}