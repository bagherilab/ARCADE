package abm.env.comp;

import sim.engine.SimState;
import abm.sim.Simulation;
import abm.env.loc.Location;
import abm.util.MiniBox;

/** 
 * Component using fixed source based approach. Previously SourceGenerator.
 *
 * @author  Jessica S. Yu <jessicayu@u.northwestern.edu>
 * @version 2.3.14
 * @since   2.3
 */

public class SourceSites extends Sites {
	private static final long serialVersionUID = 0;
	private double[][][] damageSingle, damageValues;
	private final double _damage;
	private String[] xSites, ySites, zSites;
	private final MiniBox specs;
	private boolean calcDamage;
	
	// CONSTRUCTOR.
	public SourceSites(MiniBox component) {
		xSites = component.get("X_SPACING").split(":");
		ySites = component.get("Y_SPACING").split(":");
		zSites = component.get("Z_SPACING").split(":");
		_damage = component.getDouble("SOURCE_DAMAGE");
		
		// Get list of specifications.
		specs = new MiniBox();
		String[] specList = new String[] { "X_SPACING", "Y_SPACING", "Z_SPACING", "SOURCE_DAMAGE" };
		for (String spec : specList) { specs.put(spec, component.get(spec)); }
	}
	
	// METHOD: makeSites. Defines sites for different source setups.
	public void makeSites(Simulation sim) {
		// Set up damage.
		damageSingle = new double[DEPTH][LENGTH][WIDTH];
		damageValues = new double[DEPTH][LENGTH][WIDTH];
		
		// Set booleans.
		if (_damage != 0) { calcDamage = true; }
		
		// Set up damage value array.
		for (int k = 0; k < DEPTH; k++) {
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) { damageValues[k][i][j] = 1.0; }
			}
		}
		
		// Set up sites.
		for (int k = 0; k < DEPTH; k++) {
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) {
					if (checkSource(xSites, i)) { continue; }
					if (checkSource(ySites, j)) { continue; }
					if (checkSource(zSites, k)) { continue; }
					sites[k][i][j] = 1;
				}
			}
		}
	}
	
	public double[][][] getDamage() { return damageValues; }
	
	// METHOD: step.
	public void step(SimState state) {
		// Iterate through array to calculate damage, if needed.
		if (calcDamage) {
			for (int k = 0; k < DEPTH; k++) {
				for (int i = 0; i < LENGTH; i++) {
					for (int j = 0; j < WIDTH; j++) {
						if (sites[k][i][j] != 0) {
							damageValues[k][i][j] = 1.0/Math.exp(_damage*damageSingle[k][i][j]);
						}
					}
				}
			}
		}
		
		// Iterate through each molecule.
		for (Site site : siteList) {
			SourceSite s = (SourceSite)site;
			
			// Iterate through array.
			for (int k = 0; k < DEPTH; k++) {
				for (int i = 0; i < LENGTH; i++) {
					for (int j = 0; j < WIDTH; j++) {
						if (sites[k][i][j] != 0) {
							s.delta[k][i][j] = (s.conc - s.prev[k][i][j])*damageValues[k][i][j];
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
		siteList.add(new SourceSite(code, delta, current, previous, conc));
	}
	
	// METHOD: checkSource. Checks if location is within site given coordinate.
	private boolean checkSource(String[] sites, int v) {
		int min, max;
		int inc = 1;
		
		// Site definition is given as a single value, min-max, or min-inc-max.
		if (sites[0].equals("*")) {
			if (sites.length == 1) { return false; }
			inc = Integer.valueOf(sites[1]);
			return v%inc != 0;
		}
		else if (sites.length == 1) {
			min = Integer.valueOf(sites[0]);
			max = Integer.valueOf(sites[0]);
		} else if (sites.length == 3) {
			min = Integer.valueOf(sites[0]);
			inc = Integer.valueOf(sites[1]);
			max = Integer.valueOf(sites[2]);
		} else {
			min = Integer.valueOf(sites[0]);
			max = Integer.valueOf(sites[1]);
		}
		
		return v < min || v > max || v%inc != 0;
	}
	
	// CLASS: SourceSite. Extends Site to include concentration.
	private class SourceSite extends Site {
		private double conc;
		private SourceSite(int code, double[][][] delta, double[][][] current, double[][][] previous, double conc) {
			super(code, delta, current, previous);
			this.conc = conc;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The JSON is formatted as:
	 * <pre>
	 *     {
	 *         "type": "SITE",
	 *         "class": "source",
	 *         "specs" : {
	 *             "SPEC_NAME": spec value,
	 *             "SPEC_NAME": spec value,
	 *             ...
	 *         }
	 *     }
	 * </pre>
	 */
	public String toJSON() {
		String format = "{ " + "\"type\": \"SITE\", " + "\"class\": \"source\", " + "\"specs\": %s " + "}";
		return String.format(format, specs.toJSON());
	}
	
	// METHOD: toString.
	public String toString() {
		String x = ""; String y = ""; String z = "";
		if (xSites[0].equals("*")) { x = "*"; }
		else { for (String s : xSites) { x += s + ":"; } }
		if (ySites[0].equals("*")) { y = "*"; }
		else { for (String s : ySites) { y += s + ":"; } }
		if (zSites[0].equals("*")) { z = "*"; }
		else { for (String s : zSites) { z += s + ":"; } }
		return String.format("SOURCE SITES [X = %s] [Y = %s] [Z = %s]", x.replaceFirst(":$",""), y.replaceFirst(":$",""), z.replaceFirst(":$",""));
	}
}