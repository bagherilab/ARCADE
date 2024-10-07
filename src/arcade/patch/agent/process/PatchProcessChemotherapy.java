package abm.agent.module;

import java.util.ArrayList;
import abm.sim.*;
import abm.agent.cell.*;
import abm.env.loc.Location;
import abm.env.lat.Lattice;
import abm.util.MiniBox;
import sim.util.Bag;

/**
 * @version 2.4.2
 * @since   2.4
 */

public class Chemotherapy implements Module {
	private final double CHEMOTHERAPY_THRESHOLD;
	Cell c;
	boolean wasChemo;
	ArrayList<MiniBox> drugs;
	double[] internal;
	int n;
	
	/** Cell population index */
	final int pop;
	
	public Chemotherapy(Cell c, Simulation sim) {
		Series series = sim.getSeries();
		
		this.c = c;
		this.pop = c.getPop();
		this.drugs = ((TreatmentSimulation)sim).drugs;
		this.CHEMOTHERAPY_THRESHOLD = series.getParam(pop, "CHEMOTHERAPY_THRESHOLD");
		
		// Initialize internal concentration array.
		n = drugs.size();
		internal = new double[n];
	}
	
	public void stepModule(Simulation sim) {
		double time = sim.getTime();
		Location loc = c.getLocation();
		
		// Calculate relative surface area of cell.
		Bag bag = sim.getAgents().getObjectsAtLocation(loc);
		double f = c.getVolume()/Cell.calcTotalVolume(bag);
		double area = loc.getArea()*f;
		double surfaceArea = area*2 + (c.getVolume()/area)*loc.calcPerimeter(f);
		
		for (int i = 0; i < n; i++) {
			MiniBox drug = drugs.get(i);
			Lattice lat = sim.getEnvironment(drug.get("id"));
			
			// Take up drug from environment, relative to gradient.
			double external = lat.getAverageVal(loc)*loc.getVolume();
			double gradient = (external/loc.getVolume()) - (internal[i]/c.getVolume());
			gradient *= gradient < 1E-10 ? 0 : 1;
			double uptake = drug.getDouble("UPTAKE")*surfaceArea*gradient;
			internal[i] += uptake;
			
			// Update environment.
			lat.updateVal(loc, 1.0 - uptake/external);
			
			if (c.getType() == Cell.TYPE_PROLI && internal[i] > CHEMOTHERAPY_THRESHOLD) {
				double[][] field = sim.getEnvironment("oxygen").getComponent("diffuser").getField()[loc.getLatZ()];
				double sum = 0;
				for (int[] j : loc.getLatLocations()) { sum += field[j[0]][j[1]]; }
				double oxygen = sum/loc.getMax();
				double p = Math.pow(oxygen, 2)/(Math.pow(oxygen, 2) + Math.pow(3, 2));
				if (sim.getRandom() < p) {
					((TissueCell)c).apoptose(sim);
					wasChemo = true;
				}
			}
			
			// Remove drug.
			internal[i] = Math.exp(-drug.getDouble("REMOVAL"))*internal[i];
		}
		
		if (!(sim instanceof TreatmentSimulation)) { return; }
		if (c.getType() == Cell.TYPE_APOPT
				&& time == c.getHelper().getEnd()
				&& wasChemo) {
			StringBuilder cloc = new StringBuilder();
			for (int i : c.getLocation().getGridLocation()) { cloc.append(i).append(","); }
			cloc.append(c.getLocation().getGridZ());
			((TreatmentSimulation)sim).addEvent("*chemotherapy", pop  + ",[" + cloc.toString() + "]");
		}
	}
	
	public void updateModule(Module mod, double f) {
		Chemotherapy chemotherapy = (Chemotherapy)mod;
		
		for (int i = 0; i < n; i++) {
			this.internal[i] = chemotherapy.internal[i]*f;
			chemotherapy.internal[i] *= (1 - f);
		}
	}
	
	public double getInternal(String key) {
		for (int i = 0; i < n; i++) {
			if (drugs.get(i).get("id").equals(key)) { return internal[i]; }
		}
		return Double.NaN;
	}
	
	public String toJSON() { 
		StringBuilder concs = new StringBuilder();
		for (int i = 0; i < n; i++) { concs.append(internal[i]).append(","); }
		return "[" + concs.toString().replaceFirst(",$","") + "]";
	}
}