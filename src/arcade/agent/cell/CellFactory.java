package arcade.agent.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import arcade.sim.Series;
import arcade.env.loc.Location;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public abstract class CellFactory {
	/** Map of population to critical values */
	HashMap<Integer, double[]> popToCriticals;
	
	/** Map of population to lambda values */
	HashMap<Integer, double[]> popToLambdas;
	
	/** Map of population to adhesion values */
	HashMap<Integer, double[]> popToAdhesion;
	
	/** Map of population to number of tags */
	HashMap<Integer, ArrayList<String>> popToTags;
	
	/** Map of population to tag critical values */
	HashMap<Integer, double[][]> popToTagCriticals;
	
	/** Map of population to tag lambda values */
	HashMap<Integer, double[][]> popToTagLambdas;
	
	/** Map of population to tag adhesion values */
	HashMap<Integer, double[][]> popToTagAdhesion;
	
	/** Map of population to list of ids */
	public final HashMap<Integer, HashSet<Integer>> popToIDs;
	
	/** Map of id to cell */
	public final HashMap<Integer, CellContainer> cells;
	
	/** Container for loaded cells */
	public CellFactoryContainer container;
	
	/**
	 * Creates a factory for making {@link arcade.agent.cell.Cell} instances.
	 */
	public CellFactory() {
		cells = new HashMap<>();
		popToCriticals = new HashMap<>();
		popToLambdas = new HashMap<>();
		popToAdhesion = new HashMap<>();
		popToTags = new HashMap<>();
		popToTagCriticals = new HashMap<>();
		popToTagLambdas = new HashMap<>();
		popToTagAdhesion = new HashMap<>();
		popToIDs = new HashMap<>();
	}
	
	/**
	 * Container class for loading into {@link arcade.agent.cell.CellFactory}.
	 */
	public static class CellFactoryContainer {
		final public ArrayList<CellContainer> cells;
		public CellFactoryContainer() { cells = new ArrayList<>(); }
	}
	
	/**
	 * Container class for loading a {@link arcade.agent.cell.Cell}.
	 */
	public static class CellContainer {
		public final int id;
		public final int pop;
		public final int age;
		public final int voxels;
		public final HashMap<String, Integer> tagVoxels;
		public final double targetVolume;
		public final double targetSurface;
		public final HashMap<String, Double> tagTargetVolume;
		public final HashMap<String, Double> tagTargetSurface;
		
		public CellContainer(int id, int pop, int age, int voxels) {
			this(id, pop, age, voxels, null, 0, 0, null, null);
		}
		
		public CellContainer(int id, int pop, int age, int voxels,
							 double targetVolume, double targetSurface) {
			this(id, pop, age, voxels, null, targetVolume, targetSurface, null, null);
		}
		
		public CellContainer(int id, int pop, int age, int voxels,
							 HashMap<String, Integer> tagVoxels) {
			this(id, pop, age, voxels, tagVoxels, 0, 0, null, null);
		}
		
		public CellContainer(int id, int pop, int age, int voxels,
							 HashMap<String, Integer> tagVoxels,
							 double targetVolume, double targetSurface,
							 HashMap<String, Double> tagTargetVolume, HashMap<String, Double> tagTargetSurface) {
			this.id = id;
			this.pop = pop;
			this.age = age;
			this.voxels = voxels;
			this.tagVoxels = tagVoxels;
			this.targetVolume = targetVolume;
			this.targetSurface = targetSurface;
			this.tagTargetVolume = tagTargetVolume;
			this.tagTargetSurface = tagTargetSurface;
		}
	}
	
	/**
	 * Initializes the factory for the given series.
	 * <p>
	 * Regardless of loader, the population settings are parsed to get critical,
	 * lambda, and adhesion values used for instantiating cells.
	 * 
	 * @param series  the simulation series
	 */
	public void initialize(Series series) {
		parseValues(series);
		if (series.loader != null) { loadCells(series); }
		else { createCells(series); }
	}
	
	/**
	 * Parses the population settings into maps from population to parameter value.
	 * 
	 * @param series  the simulation series
	 */
	void parseValues(Series series) {
		Set<String> keySet = series._populations.keySet();
		
		for (String key : keySet) {
			MiniBox population = series._populations.get(key);
			int pop = population.getInt("CODE");
			popToIDs.put(pop, new HashSet<>());
			
			// Get lambda values.
			double[] criticals = new double[] {
					population.getDouble("CRITICAL_VOLUME"),
					population.getDouble("CRITICAL_SURFACE")
			};
			
			popToCriticals.put(pop, criticals);
			
			// Get lambda values.
			double[] lambdas = new double[] {
					population.getDouble("LAMBDA_VOLUME"),
					population.getDouble("LAMBDA_SURFACE")
			};
			
			popToLambdas.put(pop, lambdas);
			
			// Get adhesion values.
			double[] adhesion = new double[keySet.size() + 1];
			adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
			for (String p : keySet) {
				adhesion[series._populations.get(p).getInt("CODE")] =
						population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
			}
			
			popToAdhesion.put(pop, adhesion);
			
			// Get tags (if they exist).
			MiniBox tagBox = population.filter("TAG");
			if (tagBox.getKeys().size() > 0) {
				ArrayList<String> tagKeys = tagBox.getKeys();
				int tags = tagKeys.size();
				
				double[][] criticalsTag = new double[NUMBER_TERMS][tags];
				double[][] lambdasTag = new double[NUMBER_TERMS][tags];
				double[][] adhesionsTag = new double[tags][tags];
				
				for (int i = 0; i < tags; i++) {
					MiniBox populationTag = population.filter(tagKeys.get(i));
					
					// Load tag critical values.
					criticalsTag[TERM_VOLUME][i] = populationTag.getDouble("CRITICAL_VOLUME");
					criticalsTag[TERM_SURFACE][i] = populationTag.getDouble("CRITICAL_SURFACE");
					
					// Load tag lambda values.
					lambdasTag[TERM_VOLUME][i] = populationTag.getDouble("LAMBDA_VOLUME");
					lambdasTag[TERM_SURFACE][i] = populationTag.getDouble("LAMBDA_SURFACE");
					
					// Load tag adhesion values.
					for (int j = 0; j < tags; j++) {
						adhesionsTag[i][j] = populationTag.getDouble("ADHESION" + TARGET_SEPARATOR + tagKeys.get(j));
					}
				}
				
				popToTags.put(pop, tagKeys);
				popToTagCriticals.put(pop, criticalsTag);
				popToTagLambdas.put(pop, lambdasTag);
				popToTagAdhesion.put(pop, adhesionsTag);
			}
		}
	}
	
	/**
	 * Loads cell containers into the factory container.
	 *
	 * @param series  the simulation series
	 */
	void loadCells(Series series) {
		// Load cells.
		series.loader.load(this);
		
		// Map loaded container to factory.
		for (CellContainer cellContainer : container.cells) {
			if (popToIDs.containsKey(cellContainer.pop)) {
				cells.put(cellContainer.id, cellContainer);
				popToIDs.get(cellContainer.pop).add(cellContainer.id);
			}
		}
	}
	
	/**
	 * Creates cell containers from population settings.
	 * 
	 * @param series  the simulation series
	 */
	void createCells(Series series) {
		int id = 1;
		
		// Create containers for each population.
		for (MiniBox population : series._populations.values()) {
			int n = population.getInt("INIT");
			int pop = population.getInt("CODE");
			ArrayList<String> tags = popToTags.get(pop);
			
			// Calculate voxels and (if they exist) tag voxels.
			int voxels = (int)(population.getDouble("CRITICAL_VOLUME")/Simulation.DS);
			HashMap<String, Integer> tagVoxels;
			
			if (tags == null) { tagVoxels = null; }
			else {
				tagVoxels = new HashMap<>();
				int total = 0;
				
				for (String tag : tags) {
					double fraction = population.getDouble("TAG" + TAG_SEPARATOR + tag);
					int voxelFraction = (int)Math.round(fraction*voxels);
					total += voxelFraction;
					if (total > voxels) { voxelFraction -= (total - voxels); }
					tagVoxels.put(tag, voxelFraction);
				}
			}
			
			for (int i = 0; i < n; i++) {
				CellContainer cellContainer = new CellContainer(id, pop, 0, voxels, tagVoxels);
				cells.put(id, cellContainer);
				popToIDs.get(cellContainer.pop).add(cellContainer.id);
				id++;
			}
		}
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
	abstract Cell makeCell(int id, int pop, int age, Location location,
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
	abstract Cell makeCell(int id, int pop, int age, Location location,
						   double[] criticals, double[] lambdas, double[] adhesion, int tags,
						   double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag);
	
	/**
	 * Create a {@link arcade.agent.cell.Cell} object in the given population.
	 *
	 * @param cellContainer  the cell container
	 * @param location  the cell location
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	public Cell make(CellContainer cellContainer, Location location) {
		int id = cellContainer.id;
		int pop = cellContainer.pop;
		int age = cellContainer.age;
		
		// Get copies of critical, lambda, and adhesion values.
		double[] criticals = popToCriticals.get(pop).clone();
		double[] lambdas = popToLambdas.get(pop).clone();
		double[] adhesion = popToAdhesion.get(pop).clone();
		
		// Make cell.
		Cell cell;
		
		if (popToTags.containsKey(pop)) {
			int tags = popToTags.get(pop).size();
			
			// Initialize tag arrays.
			double[][] criticalsTag = new double[NUMBER_TERMS][tags];
			double[][] lambdasTag = new double[NUMBER_TERMS][tags];
			double[][] adhesionsTag = new double[tags][tags];
			
			// Get copies of critical and lambda values.
			for (int i = 0; i < NUMBER_TERMS; i++) {
				criticalsTag[i] = popToTagCriticals.get(pop)[i].clone();
				lambdasTag[i] = popToTagLambdas.get(pop)[i].clone();
			}
			
			// Get copies of tag values.
			for (int i = 0; i < tags; i++) {
				adhesionsTag[i] = popToTagAdhesion.get(pop)[i].clone();
			}
			
			cell = makeCell(id, pop, age, location, criticals, lambdas, adhesion, tags,
					criticalsTag, lambdasTag, adhesionsTag);
		} else {
			cell = makeCell(id, pop, age, location, criticals, lambdas, adhesion);
		}
		
		return cell;
	}
}