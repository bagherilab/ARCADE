package abm.env.loc;

import java.util.ArrayList;

public class PottsLocation {
	public ArrayList<PottsCoordinate> coordinates;
	
	public PottsLocation(ArrayList<PottsCoordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	public void removeCoord(int x, int y, int z) {
		coordinates.remove(new PottsCoordinate(x, y));
	}
	
	public void addCoord(int x, int  y, int z) {
		coordinates.add(new PottsCoordinate(x, y));
	}
	
	public static class PottsCoordinate {
		public int x, y;
		public PottsCoordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public final boolean equals(Object obj) {
			PottsCoordinate coord = (PottsCoordinate)obj;
			return coord.x == x && coord.y == y;
		}
	}
}