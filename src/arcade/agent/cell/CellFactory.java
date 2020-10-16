package arcade.agent.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.sim.Series.TARGET_SEPARATOR;

public abstract class CellFactory {
	int offset;
	
	public CellFactory() {
		offset = 1;
	}
	
	public ArrayList<Integer> getIDs(int n, MiniBox population) {
		ArrayList<Integer> ids = new ArrayList<>();
		int m = (int)Math.round(population.getDouble("FRACTION")*n);
		for (int i = 0; i < m; i++) { ids.add(i + offset); }
		offset = offset + m;
		return ids;
	}
	
	/**
	 * Creates a {@link arcade.agent.cell.Cell} object.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index   
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	abstract Cell makeCell(int id, int pop, Location location,
						   double[] criticals, double[] lambdas, double[] adhesion);
	
	/**
	 * Creates a {@link arcade.agent.cell.Cell} object with tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param criticals  the list of critical values
	 * @param lambdas  the list of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param tags  the number of tags
	 * @param criticalsTag  the list of tagged critical values
	 * @param lambdasTag  the list of tagged lambda multipliers
	 * @param adhesionsTag  the list of tagged adhesion values
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	abstract Cell makeCell(int id, int pop, Location location,
						   double[] criticals, double[] lambdas, double[] adhesion, int tags,
						   double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag);
	
	/**
	 * Create a {@link arcade.agent.cell.Cell} object in the given population.
	 *
	 * @param id  the cell id
	 * @param population  the population settings
	 * @param location  the cell location
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	public Cell make(int id, MiniBox population, Location location,
					 HashMap<String, MiniBox> populations) {
		int pop = population.getInt("CODE");
		
		// Get critical values.
		double[] criticals = new double[] {
				population.getDouble("CRITICAL_VOLUME"),
				population.getDouble("CRITICAL_SURFACE")
		};
		
		// Get lambda values.
		double[] lambdas = new double[] {
				population.getDouble("LAMBDA_VOLUME"),
				population.getDouble("LAMBDA_SURFACE")
		};
		
		// Get adhesion values.
		Set<String> pops = populations.keySet();
		double[] adhesion = new double[pops.size() + 1];
		adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
		for (String p : pops) {
			adhesion[populations.get(p).getInt("CODE")] = population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
		}
		
		// Get tags if there are any.
		MiniBox tag = population.filter("TAG");
		if (tag.getKeys().size() > 0) {
			int tags = tag.getKeys().size();
			
			double[][] criticalsTag = new double[NUMBER_TERMS][tags];
			double[][] lambdasTag = new double[NUMBER_TERMS][tags];
			double[][] adhesionsTag = new double[tags][tags];
			
			for (int i = 0; i < tags; i++) {
				MiniBox populationTag = population.filter(tag.getKeys().get(i));
				
				// Load tag critical values.
				criticalsTag[TERM_VOLUME][i] = populationTag.getDouble("CRITICAL_VOLUME");
				criticalsTag[TERM_SURFACE][i] = populationTag.getDouble("CRITICAL_SURFACE");
				
				// Load tag lambda values.
				lambdasTag[TERM_VOLUME][i] = populationTag.getDouble("LAMBDA_VOLUME");
				lambdasTag[TERM_SURFACE][i] = populationTag.getDouble("LAMBDA_SURFACE");
				
				// Load tag adhesion values.
				for (int j = 0; j < tags; j++) {
					adhesionsTag[i][j] = populationTag.getDouble("ADHESION" + TARGET_SEPARATOR + tag.getKeys().get(j));
				}
			}
			
			return makeCell(id, pop, location, criticals, lambdas, adhesion, tags,
					criticalsTag, lambdasTag, adhesionsTag);
		} else {
			return makeCell(id, pop, location, criticals, lambdas, adhesion);
		}
	}
}