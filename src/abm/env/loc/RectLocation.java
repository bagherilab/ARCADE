package abm.env.loc;

import abm.sim.Series;
import sim.util.Bag;

/** 
 * Implementation of Location interface for a rectangular Grid to a rectangular
 * Lattice. Grid coordinates are in terms of the rectangular grid (x, y) and
 * vertical position (z) and the lattice coordinates are in (i, j) with a
 * position (0 - 3) for the four subsquares matched to a unit square.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @author  Alexis N. Prybutok <aprybutok@u.northwestern.edu>
 * @version 2.3.2
 * @since   2.3
 */

public class RectLocation implements Location {
	private static final double RECT_SIZE = 30.0; // [um], from side to side
	private static final double RECT_HEIGHT = 8.7; // [um]
	private static final double RECT_AREA = RECT_SIZE*RECT_SIZE; // [um^2]
	private static final double RECT_VOL = RECT_AREA*RECT_HEIGHT; // [um^3]
	private static final double RECT_RATIO = RECT_HEIGHT/RECT_SIZE;
	private static final double SUBRECT_SIZE = RECT_SIZE/2.0; // [um]
	private static int RADIUS, HEIGHT, RADIUS_BOUNDS, HEIGHT_BOUNDS, Z_OFFSET;
	private int x, y, z;              	 // rectangular location
	private int[][] ab = new int[4][2];  // subrectangular locations
	private int r = -1;                  // distance from center
	private byte p = -1;                 // rectangular position
	private byte zo;                     // rectangular offset
	private byte check;                  // allowable movements
	private static final byte[] A_OFF = new byte[] {0, 1, 0, 1};
	private static final byte[] B_OFF = new byte[] {0, 0, 1, 1};
	private static final byte[] MOVES = new byte[] {
		(byte)Integer.parseInt("00001000", 2), // up
		(byte)Integer.parseInt("00000100", 2), // down
		(byte)Integer.parseInt("00100000", 2), // right
		(byte)Integer.parseInt("00010000", 2), // left
		(byte)Integer.parseInt("00000010", 2), // vert up
		(byte)Integer.parseInt("00000001", 2), // vert down
		(byte)Integer.parseInt("00010010", 2), // vert up clockwise 1
		(byte)Integer.parseInt("00010110", 2), // vert up clockwise 2
		(byte)Integer.parseInt("00000110", 2), // vert up clockwise 3
		(byte)Integer.parseInt("00010001", 2), // vert down clockwise 1
		(byte)Integer.parseInt("00010101", 2), // vert down clockwise 2
		(byte)Integer.parseInt("00000101", 2), // vert down clockwise 3
	};

	// CONSTRUCTOR.
	public RectLocation(Location loc) { updateLocation(loc); }
	public RectLocation(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.zo = (byte)(Math.abs(Z_OFFSET + z)%2);
		this.r = Math.max(Math.abs(x), Math.abs(y));
		
		calcSubrectangular();
		calcChecks();
	}
	
	// PROPERTIES.
	public void setPosition(byte p) { this.p = p; }
	public byte getPosition() { return p; }
	public byte getOffset() { return zo; }
	public Location getCopy() { return new RectLocation(this); }
	public int[] getGridLocation() { return new int[] {x, y}; }
	public int getGridZ() { return z; }
	public int[] getLatLocation() { return ab[0]; }
	public int[][] getLatLocations() { return ab; }
	public int getLatZ() { return HEIGHT_BOUNDS + z - 1; }
	public double getGridSize() { return RECT_SIZE; }
	public double getLatSize() { return SUBRECT_SIZE; }
	public double getVolume() { return RECT_VOL; }
	public double getArea() { return RECT_AREA; }
	public double getHeight() { return RECT_HEIGHT; }
	public double getRatio() { return RECT_RATIO; }
	public int getMax() { return 4; }
	public int getRadius() { return r; }
	
	// METHOD: updateConfigs. Updates static configuration variables.
	public static void updateConfigs(Series series) {
		RADIUS = series._radius;
		HEIGHT = series._height;
		RADIUS_BOUNDS = series._radiusBounds;
		HEIGHT_BOUNDS = series._heightBounds;
		
		// Calculate z offset for different layers in the simulation
		int depth = 2*series._heightBounds - 1;
		Z_OFFSET = depth%2 - depth;
	}
	
	
	// METHOD: calcPerimeter. Estimates the perimeter of cell occupying the
	// rectangular location. Volume fraction used to take fraction of the
	// perimeter of the rectangle. If fraction is not 1 (i.e. at least two cells
	// in the location, then an additional inner segment is added.
	public double calcPerimeter(double f) {
		return f*4*RECT_SIZE + (f == 1 ? 0 : RECT_SIZE);
	}
	
	// METHOD: updateLocation. Updates rectangular and subrectangular locations.
	public void updateLocation(Location loc) {
		RectLocation rectLoc = (RectLocation)loc;
		x = rectLoc.x;
		y = rectLoc.y;
		z = rectLoc.z;
		zo = rectLoc.zo;
		r = rectLoc.r;
		
		calcSubrectangular();
		calcChecks();
	}
	
	// METHOD: calcSubrectangular. Calculates location on subrectangular grid based
	// on location on rectangular grid and offset.
	private void calcSubrectangular() {
		// Calculate coordinates of top right subrectangle.
		int a = 2*(x + RADIUS_BOUNDS - 1) + zo;
		int b = 2*(y + RADIUS_BOUNDS - 1) + zo;

		// Set coordinates of subrectangles starting with top left.
		for (int i = 0; i < 4; i++) {
			ab[i] = new int[] {a + A_OFF[i], b + B_OFF[i]};
		}
	}
	
	// METHOD: calcChecks. Updates the possible moves that can be made.
	private void calcChecks() {
		check = (byte)(
			(x == RADIUS - 1 ? 0 : 1 << 5) +
			(x == 1 - RADIUS ? 0 : 1 << 4) +
			(y == RADIUS - 1 ? 0 : 1 << 3) +
			(y == 1 - RADIUS ? 0 : 1 << 2) +
			(z == HEIGHT - 1 ? 0 : 1 << 1) +
			(z == 1 - HEIGHT ? 0 : 1 << 0));
	}
	
	// METHOD: getNeighborLocations. Gets neighboring locations.
	public Bag getNeighborLocations() {
		Bag neighbors = new Bag();
		byte b;
		
		// Add current location.
		neighbors.add(new RectLocation(x, y, z));
		
		// Add neighbor locations.
		for (int i = 0; i < MOVES.length; i++) {
			// Adjust byte for vertical offset.
			if (i > 5) { b = offsetByte(MOVES[i], zo); }
			else { b = MOVES[i]; }
			
			// Add location if possible to move there.
			if ((b & check ^ b) == 0) {
				neighbors.add(new RectLocation(
					x + (b >> 5 & 1) - (b >> 4 & 1),
					y + (b >> 3 & 1) - (b >> 2 & 1),
					z + (b >> 1 & 1) - (b >> 0 & 1)));
			}
		}

		return neighbors;
	}

	// METHOD: offsetByte. Left circular byte specification offset.
	private byte offsetByte(byte b, int k) {
		int left = b >> 2 & 0x3F; // left most 6 bits
		int right = b & 0x3; // right most 2 bits
		int shifted = (left << k & 0x3F) | (left >>> (6 - k) & 0x3F);
		return (byte)(shifted << 2 | right);
	}
	
	// METHOD: toLocation. Converts lattice coordinates (x, y, z) to location.
	public Location toLocation(int[] coords) {
		int z = coords[2] - HEIGHT_BOUNDS + 1;
		int zo = (byte)(Math.abs(Z_OFFSET + z)%2);
		
		// Calculate a and b coordinates
		double aa = (coords[0] - zo)/2.0 + 1 - RADIUS_BOUNDS;
		int a = (int)Math.floor(aa);
		double bb = (coords[1] - zo)/2.0 + 1 - RADIUS_BOUNDS;
		int b = (int)Math.floor(bb);
		
		// Check if out of bounds.
		if (Math.abs(a) >= RADIUS || Math.abs(b) >= RADIUS) { return null; }
		return new RectLocation(a, b, z);
	}
	
	// METHOD: hashCode. Override object hashing. 
	public final int hashCode() { return x + (y << 8); }
	
	// METHOD: equals. Override object to check if two locations are equal.
	public final boolean equals(Object obj) {
		RectLocation rectLoc = (RectLocation)obj;
		return rectLoc.z == z && rectLoc.y == y && rectLoc.x == x;
	}
	
	// METHOD: toString. Override to display object as string.
	public String toString() {
		return "[" + x + "," + y + "," + z + "]"
			+ "[" + ab[0][0] + "," + ab[0][1] + "]";
	}
	
	// METHOD: toJSON. Represents object as a JSON array.
	public String toJSON() {
		return "[" + x + "," + y + "," + z + "]";
	}
}