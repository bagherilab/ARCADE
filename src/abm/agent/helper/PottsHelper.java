package abm.agent.helper;

import java.util.HashSet;

import abm.agent.cell.PottsCell;
import abm.env.loc.PottsLocation;
import sim.engine.SimState;
import abm.sim.Simulation;
import abm.sim.PottsSimulation;
import abm.agent.cell.Cell;
import abm.env.grid.PottsGrid;
import abm.util.MiniBox;

public class PottsHelper implements Helper {
	final int LENGTH, WIDTH, HEIGHT;
	final int STEPS, TEMPERATURE;

	public static final String TARGET_VOLUME = "TARGET_VOLUME";
	
	public int[][][] potts;
	PottsGrid grid;
	
	final private double[][] ADHESIONS;
	final private double[] VOLUMES;
	final private double[] PERIMETERS;
	
	final private int[] MOVES_X = { 0, 1, 0, -1 }; // N, E, S, W
	final private int[] MOVES_Y = { -1, 0, 1, 0 }; // N, E, S, W
	final private int[] MOVES_CORNER_X = { 1, 1, -1, -1 }; // NE, SE, SW, NW
	final private int[] MOVES_CORNER_Y = { -1, 1, 1, -1 }; // NE, SE, SW, NW
	
	private double begin, end;
	
	private int sourcePerimeterChange, targetPerimeterChange;
	
	public PottsHelper(MiniBox helper, int pops, int length, int width, int height) {
		potts = new int[height][length][width];
		
		// Ensure a 1 voxel border around to avoid boundary checks.
		LENGTH = length - 2;
		WIDTH = width - 2;
		HEIGHT = height - 2;
		
		// Number of Monte Carlo steps
		STEPS = LENGTH*WIDTH;
		
		// Set adhesion matrix to default value.
		ADHESIONS = new double[pops + 1][pops + 1];
		double defaultAdhesion = helper.getDouble("ADHESION_POTTS");
		for (int i = 0; i < pops + 1; i++) {
			for (int j = 0; j < pops + 1; j++) {
				ADHESIONS[i][j] = defaultAdhesion;
			}
		}
		
		// Update adhesion matrix by parsing input.
		MiniBox adhesion = helper.filter("ADHESION");
		for (String key : adhesion.getKeys()) {
			String[] p = key.split("_")[0].split(":");
			int p1 = p[0].equals("*") ? 0 : Integer.parseInt(p[0]) + 1;
			int p2 = p[1].equals("*") ? 0 : Integer.parseInt(p[1]) + 1;
			ADHESIONS[p1][p2] = adhesion.getDouble(key);
			ADHESIONS[p2][p1] = adhesion.getDouble(key);
		}
		
		// Set adhesion of medium to itself to NaN.
		ADHESIONS[0][0] = Double.NaN;
		
		// Set volume vector to default value.
		VOLUMES = new double[pops + 1];
		for (int i = 0; i < pops + 1; i++) {
			VOLUMES[i] = helper.getDouble("VOLUME_POTTS");
		}
		
		// Update volume vector by parsing input.
		MiniBox volume = helper.filter("VOLUME");
		for (String key : volume.getKeys()) {
			String[] p = key.split("_");
			int p1 = p[0].equals("*") ? 0 : Integer.parseInt(p[0]) + 1;
			VOLUMES[p1] = volume.getDouble(key);
		}
		
		// Set volume target of medium to NaN.
		VOLUMES[0] = Double.NaN;
		
		// Set perimeter vector to default value.
		PERIMETERS = new double[pops + 1];
		for (int i = 0; i < pops + 1; i++) {
			PERIMETERS[i] = helper.getDouble("PERIMETER_POTTS");
		}
		
		// Update perimeter vector by parsing input.
		MiniBox perimeter = helper.filter("PERIMETER");
		for (String key : perimeter.getKeys()) {
			String[] p = key.split("_");
			int p1 = p[0].equals("*") ? 0 : Integer.parseInt(p[0]) + 1;
			PERIMETERS[p1] = perimeter.getDouble(key);
		}
		
		// Set perimeter target of medium to NaN.
		PERIMETERS[0] = Double.NaN;
		
		// Get temperature.
		TEMPERATURE = helper.getInt("TEMPERATURE");
	}
	
	public double getBegin() { return begin; }
	public double getEnd() { return end; }
	
	double getAdhesion(int id, int x, int y, int z) {
		double H = 0;
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (!(i == x && j == y)) {
					if (potts[z][i][j] != id) {
						int a = (id == 0 ? 0 : ((Cell)grid.getObjectAt(id)).getPop() + 1);
						int b = (potts[z][i][j] == 0 ? 0 : ((Cell)grid.getObjectAt(potts[z][i][j])).getPop() + 1);
						H += ADHESIONS[a][b];
					}
				}
			}
		} 
		
		return H;
	}
	
	double getDeltaAdhesion(int sourceID, int targetID, int x, int y, int z) {
		double source = getAdhesion(sourceID, x, y, z);
		double target = getAdhesion(targetID, x, y, z);
		return target - source;
	}
	
	double getVolume(int id, int change) {
		if (id == 0) { return 0; }
		else {
			Cell c = (Cell)grid.getObjectAt(id);
			double volume = c.getVolume();
			double targetVolume = c.getTargetVolume();
			double lambda = VOLUMES[c.getPop() + 1];
			return lambda * Math.pow((volume - targetVolume + change), 2);
		}
	}
	
	double getDeltaVolume(int sourceID, int targetID) {
		double source = getVolume(sourceID, -1) - getVolume(sourceID, 0);
		double target = getVolume(targetID, 1) - getVolume(targetID, 0);
		return target + source;
	}
	
	double getPerimeter(int id, int change) {
		if (id == 0) { return 0; }
		else {
			Cell c = (Cell)grid.getObjectAt(id);
			double perimeter = c.getPerimeter();
			double targetPerimeter = c.getTargetPerimeter();
			double lambda = PERIMETERS[c.getPop() + 1];
			return lambda * Math.pow(perimeter - targetPerimeter + change, 2) - lambda * Math.pow(perimeter - targetPerimeter, 2);
		}
	}
	
	double getDeltaPerimeter(int sourceID, int targetID, int x, int y, int z) {
		int beforeSource = 0;
		int afterSource = 0;
		int beforeTarget = 0;
		int afterTarget = 0;
		
		// Iterate through each neighbor.
		for (int i = 0; i < 4; i++) {
			int neighbor = potts[z][x + MOVES_X[i]][y + MOVES_Y[i]];
			if (neighbor != sourceID) {
				beforeSource++;
				if (neighbor == targetID) { beforeTarget++; }
			}
			
			if (neighbor != targetID) {
				afterTarget++;
				if (neighbor == sourceID) { afterSource++; }
			}
		}
		
		// Save changes to perimeter.
		sourcePerimeterChange = afterSource - beforeSource;
		targetPerimeterChange = afterTarget - beforeTarget;
		
		double source = getPerimeter(sourceID, sourcePerimeterChange);
		double target = getPerimeter(targetID, targetPerimeterChange);
		
		return target + source;
	}
	
	
	
	public void step(SimState state) {
		int x, y, sourceID, targetID;
		int z = 0;
		
		for (int step = 0; step < STEPS; step++) {
			// Get random coordinate for candidate.
			x = state.random.nextInt(LENGTH) + 1;
			y = state.random.nextInt(WIDTH) + 1;
			
			// Get id of candidate.
			sourceID = potts[z][x][y];
			
			// Get unique targets.
			HashSet<Integer> uniqueTargets = new HashSet<>();
			for (int i = 0; i < 4; i++) {
				int id = potts[z][x + MOVES_X[i]][y + MOVES_Y[i]];
				if (id != sourceID) { uniqueTargets.add(id); }
			}
			
			// Skip if there are no unique targets.
			if (uniqueTargets.size() == 0) { continue; }
			
			// Select target from unique targets.
			targetID = (int)uniqueTargets.toArray()[state.random.nextInt(uniqueTargets.size())];
			
			// Check connectivity of source.
			if (sourceID > 0) {
				boolean candidateConnected = getConnectivity(potts, z, x, y, sourceID);
				if (!candidateConnected) { continue; }
			}
			
			// Check connectivity of target.
			if (targetID > 0) {
				boolean targetConnected = getConnectivity(potts, z, x, y, targetID);
				if (!targetConnected) { continue; }
			}
			
			// Calculate energy change.
			double dH = 0;
			dH += getDeltaAdhesion(sourceID, targetID, x, y, z);
			dH += getDeltaVolume(sourceID, targetID);
			dH += getDeltaPerimeter(sourceID, targetID, x, y, z);
			
			double p;
			if (dH < 0) { p = 1; }
			else { p = Math.exp(-dH/TEMPERATURE); }
			
			if (state.random.nextDouble() < p) {
				potts[z][x][y] = targetID;
				if (sourceID > 0) {
					((Cell)grid.getObjectAt(sourceID)).removeVoxel(x, y, z, sourcePerimeterChange);
				}
				if (targetID > 0) {
					((Cell)grid.getObjectAt(targetID)).addVoxel(x, y, z, targetPerimeterChange);
				}
			}
		}
	}
	
	boolean getConnectivity(int[][][] voxels, int z, int x, int y, int id) {
		// Count number of sites in adjacency neighborhood.
		int sites = 0;
		for (int i = 0; i < 4; i++) {
			if (voxels[z][x + MOVES_X[i]][y + MOVES_Y[i]] == id) { sites++; }
		}
		
		switch (sites) {
			case 0: return false;
			case 1: return true;
			case 2:
				// Check for opposites N/S
				if (voxels[z][x + 1][y] == id && voxels[z][x - 1][y] == id) { return false; }
				// Check for opposites E/W
				else if (voxels[z][x][y + 1] == id && voxels[z][x][y - 1] == id) { return false; }
				// Check for corners
				else {
					for (int i = 0; i < 4; i++) {
						if (voxels[z][x + MOVES_X[i]][y + MOVES_Y[i]] == id
								&& voxels[z][x + MOVES_X[(i + 1)%4] ][y + MOVES_Y[(i + 1)%4]] == id
								&& voxels[z][x + MOVES_CORNER_X[i]][y + MOVES_CORNER_Y[i]] == id) {
							return true;
						}
					}
					return false;
				}
			case 3:
				for (int i = 0; i < 4; i++) {
					if (voxels[z][x + MOVES_X[i]][y + MOVES_Y[i]] != id) {
						if (voxels[z][x + MOVES_CORNER_X[(i + 1)%4]][y + MOVES_CORNER_Y[(i + 1)%4]] == id
								&& voxels[z][x + MOVES_CORNER_X[(i + 2)%4]][y + MOVES_CORNER_Y[(i + 2)%4]] == id) {
							return true;
						}
					}
				}
				return false;
			case 4:
				if (voxels[z][x][y] == 0) { return true; }
				else { System.err.println("NO"); }
		}
		return false;
	}
	
	public void scheduleHelper(Simulation sim) { scheduleHelper(sim, sim.getTime()); }
	public void scheduleHelper(Simulation sim, double begin) {
		grid = ((PottsSimulation)sim).agents;
		this.begin = begin;
		this.end = begin;
		((SimState)sim).schedule.scheduleRepeating(0, Simulation.ORDERING_HELPER + 1, this);
	}
	
	public String toJSON() { return null; }
}