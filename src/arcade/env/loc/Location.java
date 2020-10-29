package arcade.env.loc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import ec.util.MersenneTwisterFast;
import static arcade.agent.cell.Cell.Tag;

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
	 * Adds a voxel at the given coordinates for given tag.
	 *
	 * @param tag  the voxel tag
	 * @param x  the x coordinate
	 * @param y  the y coordinate
	 * @param z  the z coordinate
	 */
	void add(Tag tag, int x, int y, int z);
	
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
	void remove(Tag tag, int x, int y, int z);
	
	/**
	 * Assigns the voxel at the given coordinates to the given tag.
	 * 
	 * @param tag  the voxel tag
	 * @param voxel  the voxel to assign
	 */
	void assign(Tag tag, Voxel voxel);
	
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
	 * Gets all voxels.
	 * 
	 * @return  the list of voxels.
	 */
	ArrayList<Voxel> getVoxels();
	
	/**
	 * Gets a set of tags.
	 * 
	 * @return  the set of tags.
	 */
	Set<Tag> getTags();
	
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
	int getVolume(Tag tag);
	
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
	int getSurface(Tag tag);
	
	/** Comparator for voxels */
	Comparator<Voxel> VOXEL_COMPARATOR = (v1, v2) ->
			v1.z != v2.z ? Integer.compare(v1.z, v2.z) :
					v1.x != v2.x ? Integer.compare(v1.x, v2.x) :
							Integer.compare(v1.y, v2.y);
	
	class Voxel {
		/** Voxel x coordinate */
		public final int x;
		
		/** Voxel y coordinate */
		public final int y;
		
		/** Voxel z coordinate */
		public final int z;
		
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