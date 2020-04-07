package abm.agent.helper;

import java.util.HashSet;
import sim.engine.SimState;
import abm.sim.Simulation;
import abm.sim.PottsSimulation;
import abm.env.grid.PottsGrid;
import abm.util.MiniBox;

public class PottsHelper implements Helper {
	final int LENGTH, WIDTH, HEIGHT;
	final int STEPS, TEMPERATURE;
	
	public int[][][] potts;
	PottsGrid grid;
	
	final private double[][] ADHESIONS;
	final private double[] VOLUMES;
	
	final private int[] MOVES_X = { 0, 1, 0, -1 }; // N, E, S, W
	final private int[] MOVES_Y = { -1, 0, 1, 0 }; // N, E, S, W
	final private int[] MOVES_CORNER_X = { 1, 1, -1, -1 }; // NE, SE, SW, NW
	final private int[] MOVES_CORNER_Y = { -1, 1, 1, -1 }; // NE, SE, SW, NW
	
	private double begin, end;
	
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
		
		// Get temperature.
		TEMPERATURE = helper.getInt("TEMPERATURE");
	}
	
	public double getBegin() { return begin; }
	public double getEnd() { return end; }
	
	double getAdhesion(PottsGrid grid, int id, int x, int y, int z) {
		double H = 0;
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (!(i == x && j == y)) {
					if (potts[z][i][j] != id) {
						int a = (id == 0 ? 0 : grid.getCellAt(id).getPop() + 1);
						int b = (potts[z][i][j] == 0 ? 0 : grid.getCellAt(potts[z][i][j]).getPop() + 1);
						H += ADHESIONS[a][b];
					}
				}
			}
		} 
		
		return H;
	}
	
	double getDeltaAdhesion(PottsGrid grid, int sourceID, int targetID, int x, int y, int z) {
		double source = getAdhesion(grid, sourceID, x, y, z);
		double target = getAdhesion(grid, targetID, x, y, z);
		return target - source;
	}
	
	double getVolume(int id, int change) {
		if (id == 0) { return 0; }
		else {
			double size = grid.getCellAt(id).getNumVoxels();
			double critSize = grid.getCellAt(id).getCritVoxels();
			return VOLUMES[grid.getCellAt(id).getPop() + 1]*Math.pow((size - critSize + change), 2);
		}
	}
	
	double getDeltaVolume(int sourceID, int targetID) {
		double source = getVolume(sourceID, -1) - getVolume(sourceID, 0);
		double target = getVolume(targetID, 1) - getVolume(targetID, 0);
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
			dH += getDeltaAdhesion(grid, sourceID, targetID, x, y, z);
			dH += getDeltaVolume(sourceID, targetID);
			
			double p;
			if (dH < 0) { p = 1; }
			else { p = Math.exp(-dH/TEMPERATURE); }
			
			if (state.random.nextDouble() < p) {
				potts[z][x][y] = targetID;
				if (sourceID > 0) { grid.getCellAt(sourceID).removeVoxel(x, y, z); }
				if (targetID > 0) { grid.getCellAt(targetID).addVoxel(x, y, z); }
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