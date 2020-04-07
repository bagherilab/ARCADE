package abm.sim;

import java.util.*;
import sim.engine.*;
import abm.agent.cell.PottsCell;
import abm.agent.helper.*;
import abm.env.grid.*;
import abm.env.lat.*;
import abm.env.loc.*;
import abm.util.Parameter;
import abm.util.MiniBox;

public class PottsSimulation extends SimState implements Simulation {
	private static final long serialVersionUID = 0;
	
	final Series series;
	
	private final int seed;
	
	public PottsGrid agents;
	
	public PottsSimulation(long seed, Series series) {
		super(seed);
		this.series = series;
		this.seed = (int)seed - Series.SEED_OFFSET;
	}
	
	public int[][][] potts;

	public void start() { 
		super.start();
		
		agents = new PottsGrid();
		
		// Schedule all helpers.
		for (Helper h : series._helpers) {
			h.scheduleHelper(this);
			
			if (h instanceof PottsHelper) {
				this.potts = ((PottsHelper)h).potts;
			}
		}
		
		// Clear potts array.
		for (int k = 0; k < series._height; k++) {
			for (int i = 0; i < series._length; i++) {
				for (int j = 0; j < series._width; j++) {
					potts[k][i][j] = 0;
				}
			}
		}
		
		int n = 6;
		int id = 1;
		
		for (int i = 1; i < (series._length - 1)/n; i++) {
			for (int j = 1; j < (series._width - 1)/n; j++) {
				ArrayList<PottsLocation.PottsCoordinate> coordinates = new ArrayList<>();
				
				double rand = random.nextDouble();
				if (rand < 0.5) { continue; }
				
				for (int ii = 0; ii < n ; ii++) {
					for (int jj = 0; jj < n  ; jj++) {
						coordinates.add(new PottsLocation.PottsCoordinate(i*n + ii, j*n + jj));
					}
				}
				
				int pop = rand < 0.7 ? 0 :  rand < 0.8 ? 1 : 2;
				PottsLocation loc = new PottsLocation(coordinates);
				PottsCell c = new PottsCell(id, pop, loc);
				agents.addObject(id, c);
				
				id++;
			}
		}
		
		for (Object obj : agents.getAllObjects()) {
			PottsCell c = (PottsCell)obj;
			for (PottsLocation.PottsCoordinate coord : c.getLocation().coordinates) {
				potts[0][coord.x][coord.y] = c.id;
			}
		}
	}
	
	public void finish() { super.finish(); }
	
	@Override
	public Grid getAgents() {
		return null;
	}
	
	@Override
	public Lattice getEnvironment(String key) {
		return null;
	}
	
	@Override
	public HashMap<String, MiniBox> getMolecules() {
		return null;
	}
	
	@Override
	public void setupEnvironment() {
		
	}
	
	@Override
	public void setupAgents() {
		
	}
	
	@Override
	public double getRandom() {
		return 0;
	}
	
	@Override
	public double getTime() {
		return 0;
	}
	
	@Override
	public double getDeathProb(int pop, int age) {
		return 0;
	}
	
	@Override
	public double getNextVolume(int pop) {
		return 0;
	}
	
	@Override
	public int getNextAge(int pop) {
		return 0;
	}
	
	@Override
	public Map<String, Parameter> getParams(int pop) {
		return null;
	}
	
	@Override
	public Series getSeries() { return series; }
	
	@Override
	public int getSeed() {
		return 0;
	}
	
	@Override
	public ArrayList<Location> getLocations(int radius, int height) {
		return null;
	}
	
	@Override
	public ArrayList<Location> getInitLocations(int radius) {
		return null;
	}
	
	@Override
	public Location[][][] getSpanLocations() {
		return new Location[0][][];
	}
	
	@Override
	public Location getCenterLocation() {
		return null;
	}
}