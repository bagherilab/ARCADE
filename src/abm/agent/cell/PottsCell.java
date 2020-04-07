package abm.agent.cell;

import sim.engine.*;
import abm.env.loc.PottsLocation;

public class PottsCell {
	public final PottsLocation location;
	int pop;
	int voxels;
	public int id;
	
	public PottsCell(int id, int pop, PottsLocation loc) {
		this.location = loc;
		voxels = location.coordinates.size();
		this.pop = pop;
		this.id = id;
	}
	
	public PottsLocation getLocation() {
		return location;
	}
	
	public int getPop() {
		return pop;
	}
	
	public void removeVoxel(int x, int y, int z) {
		location.removeCoord(x, y, z);
		voxels--;
	}
	
	public void addVoxel(int x, int y, int z) {
		location.addCoord(x, y, z);
		voxels++;
	}
	
	public int getNumVoxels() {
		return voxels;
	}
	
	public int getCritVoxels() {
		return 40;
	}
	
	public void step(SimState state) { }
}