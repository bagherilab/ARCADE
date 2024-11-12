package abm.agent.module;

import abm.sim.Series;
import abm.sim.Simulation;
import abm.agent.cell.Cell;
import abm.agent.cell.TissueCell;
import abm.sim.TreatmentSimulation;
import abm.env.loc.Location;

/**
 * @version 2.4.2
 * @since   2.4
 */

public class Radiation implements Module {
	private final double RADIATION_THRESHOLD;
	double dosage;
	boolean wasRadiated;
	
	/** Cell the module is associated with */
	final Cell c;
	
	/** Cell population index */
	final int pop;
	
	public Radiation(Cell c, Simulation sim) {
		Series series = sim.getSeries();
		
		// Set parameters.
		this.pop = c.getPop();
		this.c = c;
		this.RADIATION_THRESHOLD = series.getParam(pop, "RADIATION_THRESHOLD");
	}
	
	public void stepModule(Simulation sim) {
		double time = sim.getTime();
		
		if (c.getType() != Cell.TYPE_APOPT && dosage > RADIATION_THRESHOLD) {
			Location loc = c.getLocation();
			double[][] field = sim.getEnvironment("oxygen").getComponent("diffuser").getField()[loc.getLatZ()];
			double sum = 0;
			for (int[] i : loc.getLatLocations()) { sum += field[i[0]][i[1]]; }
			double oxygen = sum/loc.getMax();
			double p = Math.pow(oxygen, 2)/(Math.pow(oxygen, 2) + Math.pow(3, 2));
			if (sim.getRandom() < p) {
				((TissueCell)c).apoptose(sim);
				wasRadiated = true;
			}
		}
		
		if (!(sim instanceof TreatmentSimulation)) { return; }
		if (c.getType() == Cell.TYPE_APOPT
				&& time == c.getHelper().getEnd() 
				&& wasRadiated) {
			StringBuilder loc = new StringBuilder();
			for (int i : c.getLocation().getGridLocation()) { loc.append(i).append(","); }
			loc.append(c.getLocation().getGridZ());
			((TreatmentSimulation)sim).addEvent("*radiation", pop  + ",[" + loc.toString() + "]");
		}
	}
	
	public void updateModule(Module mod, double f) {
		Radiation radiation = (Radiation)mod;
		this.dosage = radiation.dosage;
	}
	
	public double getInternal(String key) { return dosage; }
	
	public void apply(double dosage) { this.dosage += dosage; }
	
	public String toJSON() {
		return "[" + dosage + "]";
	}
}