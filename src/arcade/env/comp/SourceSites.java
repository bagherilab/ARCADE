package arcade.env.comp;

import sim.engine.SimState;
import arcade.sim.Simulation;
import arcade.env.loc.Location;

/** 
 * Extension of {@link arcade.env.comp.Sites} for constant source sites.
 * <p>
 * Each index in the lattice is a site, representing a constant source.
 * The amount of concentration added to each index is the difference between
 * the concentration at the index and the source concentration of the molecule.
 *
 * @version 2.2.X
 * @since   2.3
 */

public class SourceSites extends Sites {
	/** Serialization version identifier */
	private static final long serialVersionUID = 0;
	
	/**
	 * Creates {@link arcade.env.comp.Sites} object with constant source sites.
	 */
	public SourceSites() { }
	
	public void makeSites(Simulation sim) {
		for (int k = 0; k < DEPTH; k++) {
			for (int i = 0; i < LENGTH; i++) {
				for (int j = 0; j < WIDTH; j++) {
					sites[k][i][j] = 1;
				}
			}
		}
	}
	
	/**
	 * Steps through array and assigns source sites.
	 * 
	 * @param state  the MASON simulation state
	 */
	public void step(SimState state) {
		// Iterate through each molecule.
		for (Site site : siteList) {
			SourceSite s = (SourceSite)site;
			
			// Iterate through array.
			for (int k = 0; k < DEPTH; k++) {
				for (int i = 0; i < LENGTH; i++) {
					for (int j = 0; j < WIDTH; j++) {
						if (sites[k][i][j] != 0) {
							s.delta[k][i][j] = (s.conc - s.prev[k][i][j]);
						}
					}
				}
			}
		}
	}
	
	public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) { }
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For each molecule, the following parameters are required:
	 * <ul>
	 *     <li>Source concentration</li>
	 * </ul>
	 */
	public void equip(Simulation sim, int code, double[][][] delta, double[][][] current, double[][][] previous) {
		double conc = sim.getSeries().getParam("CONC_" + Simulation.MOL_NAMES[code]);
		siteList.add(new SourceSite(code, delta, current, previous, conc));
	}
	
	/**
	 * Extension of {@link arcade.env.comp.Site} for {@link arcade.env.comp.SourceSites}.
	 * <p>
	 * Adds a field for source concentration.
	 */
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
	 *         "class": "source"
	 *     }
	 * </pre>
	 */
	public String toJSON() {
		String format = "{ "
				+ "\"type\": \"SITE\", "
				+ "\"class\": \"source\" "
				+ "}";
		
		return String.format(format);
	}
	
	public String toString() {
		return String.format("SOURCE SITES");
	}
}