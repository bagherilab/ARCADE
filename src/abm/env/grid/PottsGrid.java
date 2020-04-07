package abm.env.grid;

import java.util.HashMap;
import sim.util.Bag;
import abm.agent.cell.*;

public class PottsGrid  {
	private HashMap<Integer, PottsCell> cells;
	private Bag allCells;
	int next;
	
	public PottsGrid() {
		cells = new HashMap<>();
		allCells = new Bag();
		cells.put(0, null);
		next = 0;
	}
	
	public PottsCell getCellAt(int id) {
		return (PottsCell)cells.get(id);
	}
	
	public Bag getAllObjects() { return allCells; }
	
	public void addObject(int id, Object obj) {
		allCells.add(obj);
		cells.put(id, (PottsCell)obj);
	}
}