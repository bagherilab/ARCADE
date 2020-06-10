package arcade.agent.cell;

import arcade.env.loc.Location;
import arcade.sim.PottsSimulation;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import sim.engine.*;
import arcade.env.loc.PottsLocation;

import java.util.ArrayList;

import static arcade.agent.helper.PottsHelper.*;

public class PottsCell implements Cell {
	public static final double RATE_QUIESCENT_PROLIFERATIVE = 1/115.27;
	public static final double DURATION_G1 = 2.5*60;
	public static final double DURATION_S = 8*60;
	public static final double DURATION_G2 = 3*60;
	public final PottsLocation location;
	int pop;
	int voxels;
	public int id;
//	int tick;
	int age;
	int state;
	int targetVoxels;
	int phase;
	public int perimeter;
	public int divisions;
//	private MiniBox potts;
	
	public PottsCell(int[][][] potts, int id, int pop, PottsLocation loc) {
		this.location = loc;
		voxels = location.coordinates.size();
		this.pop = pop;
		this.id = id;
		this.age = 0;
		state = PROLIFERATIVE;
		divisions = 20;
//		this.tick = 0;
//		this.potts = new MiniBox();
//		
//		// Add initial potts parameters.
//		potts.put(TARGET_VOLUME) = 40;
		targetVoxels = 200;
		
		
		this.perimeter = location.calculatePerimeter(id, potts);
		
	}
	
	public PottsLocation getLocation() {
		return location;
	}
	
	public int getID() { return id; }
	public int getState() { return state; }
	public int getPop() {
		return pop;
	}
	public int getPhase() { return phase; }
	
	
	public void removeVoxel(int x, int y, int z, int delta) {
		location.removeCoord(x, y, z);
		voxels--;
		perimeter += delta;
	}
	
	public void addVoxel(int x, int y, int z, int delta) {
		location.addCoord(x, y, z);
		voxels++;
		perimeter += delta;
	}
	
	public int getVolume() {
		return voxels;
	}
	
	public double getTargetVolume() {
		return targetVoxels;
	}
	
	public int getPerimeter() { 
		return perimeter;
	}
	
	public double getTargetPerimeter() {
		return 200; //(int)Math.round(2*Math.PI*Math.sqrt(voxels/Math.PI));
	}
	
	public void step(SimState simstate) {
		PottsSimulation sim = (PottsSimulation)simstate;
		
		// Increase age and current tick.
		age++;
//		tick++;
//		System.out.println(divisions);
		// If cell is quiescent, probability of becoming proliferative.
		if (state == QUIESCENT) {
//			double p = 1 - Math.exp(-RATE_QUIESCENT_PROLIFERATIVE);
//			double r = sim.getRandom();
////			System.out.println(r);
//			if (r < p) {
////				System.out.println(r + " " +p + " " + simstate.schedule.getTime());
//				state = PROLIFERATIVE;
//				phase = PHASE_G1;
////				tick = 0;
//				targetVoxels = 40;
//			}
		} else if (state == PROLIFERATIVE && divisions > 0 && voxels > 25) {
			double p, r;
			divide(simstate, sim);
//			switch (phase) {
//				case PHASE_G1:
//					p = 1/DURATION_G1;
//					r = sim.getRandom();
//					if (r < p) {
//						phase = PHASE_S;
//						targetVoxels = voxels;
//					}
//					break;
//				case PHASE_S:
//					p = 1/DURATION_S;
//					r = sim.getRandom();
//					if (r < p) {
//						phase = PHASE_G2;
//						targetVoxels = 40;
//					}
//					break;
//				case PHASE_G2:
//					p = 1/DURATION_G2;
//					r = sim.getRandom();
//					if (r < p) {
//						phase = PHASE_M;
//					}
//					break;
//				case PHASE_M:
					
					
					
					
					
					
//			}
			
		}
//		
//		else if (type == TYPE_PROLI) {
//			volume += 2;

//			if (volume > critVolume*2) {
//				type = TYPE_QUIES;
//				volume /= 2;
////				Cell daughter = new IPSCell(sim, pop, location, volume/2, 0, null, null);
//				Location newLoc =  TissueCell.getBestLocation(sim, daughter);
//				if (newLoc != null) {
//					daughter.getLocation().updateLocation(newLoc);
//					sim.getAgents().addObject(daughter, daughter.getLocation());
//					((SimState)sim).schedule.scheduleRepeating(daughter, Simulation.ORDERING_CELLS, 1);
//				}

//			}
//		}
	}
	
	
	private void divide(SimState simstate, Simulation sim) {
		// Local copy of potts.
		int[][][] potts = ((PottsSimulation)sim).potts;
		
//		System.out.println(this);
				
		// Split current location.
		PottsLocation newLoc = location.splitCoord(sim);
		
		int nextID = ((PottsSimulation)sim).nextID;
		
		// Update id grid.
		location.update(potts, id);
		newLoc.update(potts, nextID);
		
		// Check parent cell location for unconnected voxels.
		ArrayList<PottsLocation.PottsCoordinate> unconnectedA = PottsLocation.removeUnconnected(location, potts, id);
		ArrayList<PottsLocation.PottsCoordinate> unconnectedB = PottsLocation.removeUnconnected(newLoc, potts, nextID);
		
		if (unconnectedA.size() != 0 || unconnectedB.size() != 0) {
			location.addCoords(unconnectedB);
			newLoc.addCoords(unconnectedA);
			
			location.update(potts, id);
			newLoc.update(potts, nextID);
			
			
			unconnectedA = PottsLocation.removeUnconnected(location, potts, id);
			unconnectedB = PottsLocation.removeUnconnected(newLoc, potts, nextID);
		}
		
		
		if (unconnectedA.size() != 0 || unconnectedB.size() != 0) {
			System.out.println('x');
		}

		
		
//		newLoc.addCoords(unconnected);
		
		
//		
		PottsCell newCell = new PottsCell(potts, nextID, pop, newLoc);
		sim.getAgents().addObject(nextID, newCell);
		
		((PottsSimulation)sim).nextID++;
		
		state = QUIESCENT;
//		newCell.state = QUIESCENT;
//		
//		location.update(sim.potts, id);
//		
//		
//		ArrayList<PottsLocation.PottsCoordinate> newUnconnected = PottsLocation.removeUnconnected(newLoc, sim.potts,  sim.nextID);
//		if (newUnconnected.size() > 0) {
//			System.out.println("NO");
//			System.exit(-1);
//		}
//
//
//
//
//
//
////					
////
		simstate.schedule.scheduleRepeating(simstate.schedule.getTime() + 1, 0, newCell);
////
////					
////					
//		// Check if simply connected.
//		
//		
//		
//		
//		sim.nextID++;
//		phase = PHASE_G0;
//		state = QUIESCENT;
//		
//		targetVoxels = 40;
//		newCell.targetVoxels = 40;
//		System.out.println(targetVoxels + " " + newCell.targetVoxels);
		divisions--;
		newCell.divisions = divisions;
//		
	}
}