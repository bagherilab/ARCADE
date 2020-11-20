package arcade.potts.env.loc;

public final class Voxel {
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