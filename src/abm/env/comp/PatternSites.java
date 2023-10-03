package abm.env.comp;

import sim.engine.SimState;
import abm.sim.Simulation;
import abm.env.loc.Location;
import abm.util.MiniBox;

/**
 * Component using pattern based approach. Previously PatternGenerator.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.20
 * @since   2.3
 */

public abstract class PatternSites extends Sites {
	private static final long serialVersionUID = 0;
	double[][][] damageSingle, damageTotal, damageValues;
	private final double _damage;
	private final double fraction, wGrad, wFlow, wLocal;
	private boolean calcLocal, calcFlow, calcDamage;
	private final MiniBox specs;
	
	// CONSTRUCTOR.
	PatternSites(MiniBox component) {
		// Get damage scaling.
		_damage = component.getDouble("PATTERN_DAMAGE");
		
		// Get fractions.
		fraction = component.getDouble("RELATIVE_FRACTION");
		wGrad = component.getDouble("WEIGHT_GRADIENT");
		wLocal = component.getDouble("WEIGHT_LOCAL");
		wFlow = component.getDouble("WEIGHT_FLOW");
		
		// Get list of specifications.
		specs = new MiniBox();
		String[] specList = new String[] { "PATTERN_DAMAGE", "RELATIVE_FRACTION",
				"WEIGHT_GRADIENT", "WEIGHT_LOCAL", "WEIGHT_FLOW" };
		for (String spec : specList) { specs.put(spec, component.get(spec)); }
	}
	
	// ABSTRACT METHODS.
	abstract void makeSites();
	abstract double calcAvg(int i, int j, int k, double[][] delta);
	abstract void calcFlow(int i, int j, int k, int[] borders, double[][] delta, double[][] flow);
	abstract void calcDamage(int i, int j, int k, int[] borders);
	
	// METHOD: makeSites. Defines sites for different source setups.
	public void makeSites(Simulation sim) {
		// Set up damage fields.
		damageSingle = new double[DEPTH][LENGTH][WIDTH];
		damageTotal = new double[DEPTH][LENGTH][WIDTH];
		damageValues = new double[DEPTH][LENGTH][WIDTH];
		
		// Set booleans.
		if (_damage != 0) { calcDamage = true; }
		if (wLocal > 0) { calcLocal = true; }
		if (wFlow > 0) { calcFlow = true; }
		
		// Set up damage value array.
		for (int k = 0; k < DEPTH; k++) {
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) { damageValues[k][i][j] = 1.0; }
			}
		}
		
		makeSites();
	}
	
	public double[][][] getDamage() { return damageValues; }
	
	// METHOD: step.
	public void step(SimState state) {
		int[] borders = new int[BORDERS];
		double W, w, wg, wl, wf, total;
		
		// Iterate through array to calculate damage, if needed.
		if (calcDamage) {
			for (int k = 0; k < DEPTH; k++) {
				for (int i = 0; i < LENGTH; i++) {
					for (int j = 0; j < WIDTH; j++) {
						if (sites[k][i][j] == 1) { calcDamage(i, j, k, borders); }
						damageValues[k][i][j] = 1.0/Math.exp(_damage*damageTotal[k][i][j]);
					}
				}
			}
		}
		
		// Iterate through each molecule. Skip the calculation of accumulation
		// and/or damage if not required.
		for (Site site : siteList) {
			PatternSite s = (PatternSite)site;
			total = 0;
			
			// Iterate to calculate accumulation.
			if (calcLocal || calcFlow) {
				for (int k = 0; k < DEPTH; k += 2) {
					for (int i = 0; i < LENGTH; i++) {
						for (int j = 0; j < WIDTH; j++) {
							if (sites[k][i][j] != 0) {
								s.accu[k][i][j] = (s.prev[k][i][j] - s.curr[k][i][j]) / s.conc;
								total += s.accu[k][i][j];
							}
						}
					}
				}
			}
			
			// Iterate through every other layer.
			for (int k = 0; k < DEPTH; k += 2) {
				// Check if on up or down border of environment.
				borders[UP] = (k == 0 ? 0 : 1);
				borders[DOWN] = (k == DEPTH - 1 ? 0 : 1);
				
				for (int i = 0; i < LENGTH; i++) {
					// Check if on left or right border of environment.
					borders[LEFT] = (i == 0 ? 0 : 1);
					borders[RIGHT] = (i == LENGTH - 1 ? 0 : 1);
					
					for (int j = 0; j < WIDTH; j++) {
						// Check if on top or bottom border of environment.
						borders[TOP] = (j == 0 ? 0 : 1);
						borders[BOTTOM] = (j == WIDTH - 1 ? 0 : 1);
						
						if (sites[k][i][j] != 0) {
							// Calculate flow.
							if (sites[k][i][j] == 1 && calcFlow) {
								calcFlow(i, j, k, borders, s.accu[k], s.flow[k]);
							}
							
							// Calculate fraction adjustments.
							wg = 1 - s.curr[k][i][j]/s.conc;
							wl = s.accu[k][i][j];
							wf = (total == 0 ? 0 : -s.flow[k][i][j]/total);
							w = wGrad*wg + wLocal*wl + wFlow*wf;
							W = 1.0/(1.0 + Math.exp(-w));
							
							// Update fraction.
							s.delta[k][i][j] = (s.conc - s.prev[k][i][j])*(W*fraction + 1 - fraction)*damageValues[k][i][j];
						}
					}
				}
			}
		}
	}
	
	// METHOD: updateComponent. Updates damage due to movement into location.
	public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) {
		if (sim.getAgents().getNumObjectsAtLocation(newLoc) > 1) {
			int zNew = newLoc.getLatZ();
			for (int[] i : newLoc.getLatLocations()) { damageSingle[zNew][i[0]][i[1]]++; }
			int zOld = oldLoc.getLatZ();
			for (int[] i : oldLoc.getLatLocations()) { damageSingle[zOld][i[0]][i[1]]++; }
		}
	}
	
	// METHOD: equip. Adds given molecule as a site.
	public void equip(MiniBox molecule, double[][][] delta, double[][][] current, double[][][] previous) {
		int code = molecule.getInt("code");
		double conc = molecule.getDouble("CONCENTRATION");
		siteList.add(new PatternSite(code, delta, current, previous, conc));
	}
	
	// CLASS: PatternSite. Extends Site to include concentration and scaling.
	private class PatternSite extends Site {
		private final double conc;
		private final double[][][] accu, flow;
		
		private PatternSite(int code, double[][][] delta, double[][][] current, double[][][] previous, double conc) {
			super(code, delta, current, previous);
			this.conc = conc;
			accu = new double[DEPTH][LENGTH][WIDTH];
			flow = new double[DEPTH][LENGTH][WIDTH];
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The JSON is formatted as:
	 * <pre>
	 *     {
	 *         "type": "SITE",
	 *         "class": "pattern",
	 *         "specs" : {
	 *             "SPEC_NAME": spec value,
	 *             "SPEC_NAME": spec value,
	 *             ...
	 *         }
	 *     }
	 * </pre>
	 */
	public String toJSON() {
		String format = "{ " + "\"type\": \"SITE\", " + "\"class\": \"pattern\", " + "\"specs\": %s " + "}";
		return String.format(format, specs.toJSON());
	}
	
	// METHOD: toString.
	public String toString() {
		return "PATTERN SITES";
	}
}