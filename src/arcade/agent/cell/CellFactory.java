package arcade.agent.cell;

import java.util.*;
import arcade.sim.Series;
import arcade.env.loc.Location;
import arcade.sim.Simulation;
import arcade.util.MiniBox;
import static arcade.sim.Potts.Term;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.cell.Cell.Tag;
import static arcade.agent.module.Module.Phase;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public abstract class CellFactory {
	/** Map of population to critical values */
	HashMap<Integer, EnumMap<Term, Double>> popToCriticals;
	
	/** Map of population to lambda values */
	HashMap<Integer, EnumMap<Term, Double>> popToLambdas;
	
	/** Map of population to adhesion values */
	HashMap<Integer, double[]> popToAdhesion;
	
	/** Map of population to parameters */
	HashMap<Integer, MiniBox> popToParameters;
	
	/** Map of population to number of tags */
	HashMap<Integer, Boolean> popToTags;
	
	/** Map of population to tag critical values */
	HashMap<Integer, EnumMap<Tag, EnumMap<Term, Double>>> popToTagCriticals;
	
	/** Map of population to tag lambda values */
	HashMap<Integer, EnumMap<Tag, EnumMap<Term, Double>>> popToTagLambdas;
	
	/** Map of population to tag adhesion values */
	HashMap<Integer, EnumMap<Tag, EnumMap<Tag, Double>>> popToTagAdhesion;
	
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
		popToParameters = new HashMap<>();
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
		public final State state;
		public final Phase phase;
		public final int voxels;
		public final EnumMap<Tag, Integer> tagVoxels;
		public final double targetVolume;
		public final double targetSurface;
		public final EnumMap<Tag, Double> tagTargetVolume;
		public final EnumMap<Tag, Double> tagTargetSurface;
		
		public CellContainer(int id, int pop, int voxels) {
			this(id, pop, 0, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels, null, 0, 0, null, null);
		}
		
		public CellContainer(int id, int pop, int voxels, EnumMap<Tag, Integer> tagVoxels) {
			this(id, pop, 0, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels, tagVoxels, 0, 0, null, null);
		}
		
		public CellContainer(int id, int pop, int age, State state, Phase phase, int voxels,
							 double targetVolume, double targetSurface) {
			this(id, pop, age, state, phase, voxels, null, targetVolume, targetSurface, null, null);
		}
		
		public CellContainer(int id, int pop, int age, State state, Phase phase, int voxels,
							 EnumMap<Tag, Integer> tagVoxels,
							 double targetVolume, double targetSurface,
							 EnumMap<Tag, Double> tagTargetVolume, EnumMap<Tag, Double> tagTargetSurface) {
			this.id = id;
			this.pop = pop;
			this.age = age;
			this.state = state;
			this.phase = phase;
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
		if (series.loader != null && series.loader.loadCells) { loadCells(series); }
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
			popToParameters.put(pop, series._populations.get(key));
			
			// Iterate through terms to get critical and lambda values.
			EnumMap<Term, Double> criticals = new EnumMap<>(Term.class);
			EnumMap<Term, Double> lambdas = new EnumMap<>(Term.class);
			
			for (Term term : Term.values()) {
				criticals.put(term, population.getDouble("CRITICAL_" + term.name()));
				lambdas.put(term, population.getDouble("LAMBDA_" + term.name()));
			}
			
			popToCriticals.put(pop, criticals);
			popToLambdas.put(pop, lambdas);
			
			// Get adhesion values.
			double[] adhesion = new double[keySet.size() + 1];
			adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
			for (String p : keySet) {
				adhesion[series._populations.get(p).getInt("CODE")] =
						population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
			}
			
			popToAdhesion.put(pop, adhesion);
			popToTags.put(pop, false);
			
			// Get tags (if they exist).
			MiniBox tagBox = population.filter("TAG");
			ArrayList<String> tagKeys = tagBox.getKeys();
			if (tagKeys.size() > 0) {
				popToTags.put(pop, true);
				EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = new EnumMap<>(Tag.class);
				EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = new EnumMap<>(Tag.class);
				EnumMap<Tag, EnumMap<Tag, Double>> adhesionsTag = new EnumMap<>(Tag.class);
				
				for (String tagKey : tagKeys) {
					MiniBox populationTag = population.filter(tagKey);
					Tag tag = Tag.valueOf(tagKey);
					
					// Iterate through terms to get critical and lambda values for tag.
					EnumMap<Term, Double> criticalTagTerms = new EnumMap<>(Term.class);
					EnumMap<Term, Double> lambdaTagTerms = new EnumMap<>(Term.class);
					
					for (Term term : Term.values()) {
						criticalTagTerms.put(term, populationTag.getDouble("CRITICAL_" + term.name()));
						lambdaTagTerms.put(term, populationTag.getDouble("LAMBDA_" + term.name()));
					}
					
					criticalsTag.put(tag, criticalTagTerms);
					lambdasTag.put(tag, lambdaTagTerms);
					
					// Iterate through tags to get adhesion values.
					EnumMap<Tag, Double> adhesionTagValues = new EnumMap<>(Tag.class);
					
					for (String targetKey : tagKeys) {
						adhesionTagValues.put(Tag.valueOf(targetKey), populationTag.getDouble("ADHESION" + TARGET_SEPARATOR + targetKey));
					}
					
					adhesionsTag.put(tag, adhesionTagValues);
				}
				
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
		
		// Population sizes.
		HashMap<Integer, Integer> popToSize = new HashMap<>();
		for (MiniBox population : series._populations.values()) {
			int n = population.getInt("INIT");
			int pop = population.getInt("CODE");
			popToSize.put(pop, n);
		}
		
		// Map loaded container to factory.
		for (CellContainer cellContainer : container.cells) {
			int pop = cellContainer.pop;
			if (popToIDs.containsKey(pop) && popToIDs.get(pop).size() < popToSize.get(pop)) {
				cells.put(cellContainer.id, cellContainer);
				popToIDs.get(pop).add(cellContainer.id);
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
			boolean tags = popToTags.get(pop);
			
			// Calculate voxels and (if they exist) tag voxels.
			int voxels = population.getInt("CRITICAL_VOLUME");
			EnumMap<Tag, Integer> tagVoxels;
			
			if (!tags) { tagVoxels = null; }
			else {
				tagVoxels = new EnumMap<>(Tag.class);
				int total = 0;
				
				for (Tag tag : Tag.values()) {
					double fraction = population.getDouble("TAG" + TAG_SEPARATOR + tag);
					int voxelFraction = (int)Math.round(fraction*voxels);
					total += voxelFraction;
					if (total > voxels) { voxelFraction -= (total - voxels); }
					tagVoxels.put(tag, voxelFraction);
				}
			}
			
			for (int i = 0; i < n; i++) {
				CellContainer cellContainer = new CellContainer(id, pop, voxels, tagVoxels);
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
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 */
	abstract Cell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
						   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion);
	
	/**
	 * Creates a {@link arcade.agent.cell.Cell} object with tags.
	 *
	 * @param id  the cell ID
	 * @param pop  the cell population index
	 * @param state  the cell state
	 * @param age  the cell age (in ticks)
	 * @param location  the {@link arcade.env.loc.Location} of the cell
	 * @param parameters  the dictionary of parameters
	 * @param criticals  the map of critical values
	 * @param lambdas  the map of lambda multipliers
	 * @param adhesion  the list of adhesion values
	 * @param criticalsTag  the map of tagged critical values
	 * @param lambdasTag  the map of tagged lambda multipliers
	 * @param adhesionTag  the map of tagged adhesion values
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	abstract Cell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
						   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
						   EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
						   EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag);
	
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
		State state = cellContainer.state;
		Phase phase = cellContainer.phase;
		
		// Get copies of critical, lambda, and adhesion values.
		MiniBox parameters = popToParameters.get(pop);
		EnumMap<Term, Double> criticals = popToCriticals.get(pop).clone();
		EnumMap<Term, Double> lambdas = popToLambdas.get(pop).clone();
		double[] adhesion = popToAdhesion.get(pop).clone();
		
		// Make cell.
		Cell cell;
		
		if (popToTags.get(pop)) {
			// Initialize tag arrays.
			EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = new EnumMap<>(Tag.class);
			EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = new EnumMap<>(Tag.class);
			EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = new EnumMap<>(Tag.class);
			
			// Get copies of critical, lambda, and adhesion values.
			for (Tag tag : location.getTags()) {
				criticalsTag.put(tag, popToTagCriticals.get(pop).get(tag).clone());
				lambdasTag.put(tag, popToTagLambdas.get(pop).get(tag).clone());
				adhesionTag.put(tag, popToTagAdhesion.get(pop).get(tag).clone());
			}
			
			cell = makeCell(id, pop, age, state, location, parameters, criticals, lambdas, adhesion,
					criticalsTag, lambdasTag, adhesionTag);
		} else {
			cell = makeCell(id, pop, age, state, location, parameters, criticals, lambdas, adhesion);
		}
		
		// Update cell targets.
		cell.setTargets(cellContainer.targetVolume, cellContainer.targetSurface);
		if (cellContainer.tagTargetVolume != null && cellContainer.tagTargetSurface != null) {
			for (Tag tag : location.getTags()) {
				cell.setTargets(tag, cellContainer.tagTargetVolume.get(tag), cellContainer.tagTargetSurface.get(tag));
			}
		}
		
		// Update cell module.
		cell.getModule().setPhase(phase);
		
		return cell;
	}
}