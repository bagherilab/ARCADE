package arcade.sim;

import java.util.*;
import sim.engine.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.*;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.sim.Series.TARGET_SEPARATOR;

public abstract class PottsSimulation extends SimState implements Simulation {
	/** {@link arcade.sim.Series} object containing this simulation */
	final Series series;
	
	/** Random number generator seed for this simulation */
	final int seed;
	
	/** {@link arcade.sim.Potts} object for the simulation */
	Potts potts;
	
	/** {@link arcade.env.grid.Grid} containing agents in the simulation */
	Grid agents;
	
	/** Cell ID tracker */
	int id;
	
	/**
	 * Simulation instance for a {@link arcade.sim.Series} for given random seed.
	 * 
	 * @param seed  the random seed for random number generator
	 * @param series  the simulation series
	 */
	public PottsSimulation(long seed, Series series) {
		super(seed);
		this.series = series;
		this.seed = (int)seed - Series.SEED_OFFSET;
	}
	
	public Series getSeries() { return series; }
	public Schedule getSchedule() { return schedule; }
	public int getSeed() { return seed; }
	public int getID() { return ++id; }
	public Potts getPotts() { return potts; }
	public Grid getAgents() { return agents; }
	public Lattice getEnvironment(String key) { return null; }
	
	/**
	 * Called at the start of the simulation to set up agents, environment, and
	 * schedule profilers, checkpoints, components, and helpers as needed.
	 */
	public void start() {
		super.start();
		
		// Reset id.
		id = 0;
		
		setupPotts();
		setupAgents();
		setupEnvironment();
		
		scheduleProfilers();
		scheduleCheckpoints();
		scheduleHelpers();
		scheduleComponents();
	}
	
	/**
	 * Called at the end of the simulation.
	 */
	public void finish() {
		super.finish();
		
		// TODO add methods to resetting simulation
	}
	
	/**
	 * Creates the {@link arcade.sim.Potts} object for the simulation.
	 * 
	 * @return  a {@link arcade.sim.Potts} object
	 */
	abstract Potts makePotts();
	
	public void setupPotts() {
		potts = makePotts();
		schedule.scheduleRepeating(1, ORDERING_POTTS, potts);
	}
	
	/**
	 * Creates a list of all available center coordinates for the simulation.
	 *
	 * @return  the list of centers
	 */
	abstract ArrayList<int[]> makeCenters();
	
	/**
	 * Creates a location around given center points.
	 *
	 * @param population  the population settings
	 * @param center  the center coordinates
	 * @return  a {@link arcade.env.loc.Location} object
	 */
	abstract Location makeLocation(MiniBox population, int[] center);
	
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
	 * @param center  the center coordinates
	 * @return  a {@link arcade.agent.cell.Cell} object
	 */
	Cell makeCell(int id, MiniBox population, int[] center) {
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
		Set<String> pops = series._populations.keySet();
		double[] adhesion = new double[pops.size() + 1];
		adhesion[0] = population.getDouble("ADHESION" + TARGET_SEPARATOR + "*");
		for (String p : pops) {
			adhesion[series._populations.get(p).getInt("CODE")] = population.getDouble("ADHESION" + TARGET_SEPARATOR + p);
		}
		
		// Create location.
		Location location = makeLocation(population, center);
		
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
	
	public void setupAgents() {
		// Initialize grid for agents.
		agents = new PottsGrid();
		potts.grid = agents;
		
		// Get list of available centers.
		ArrayList<int[]> availableCenters = makeCenters();
		int totalAvailable = availableCenters.size();
		
		Simulation.shuffle(availableCenters, random);
		
		// Iterate through each population to create the constituent cells.
		for (MiniBox population : series._populations.values()) {
			int n = (int)Math.round(totalAvailable*population.getDouble("FRACTION"));
			ArrayList<int[]> assignedCenters = new ArrayList<>();
			
			for (int i = 0; i < n; i++) {
				// Make the cell.
				int[] center = availableCenters.get(i);
				Cell cell = makeCell(++id, population, center);
				
				// Add, initialize, and schedule the cell.
				agents.addObject(id, cell);
				cell.initialize(potts.IDS, potts.TAGS);
				cell.schedule(schedule);
				
				// Keep track of voxel lists that are assigned.
				assignedCenters.add(center);
			}
			
			// Remove the assigned voxel lists from the available centers.
			availableCenters.removeAll(assignedCenters);
		}
	}
	
	public void setupEnvironment() {
		// TODO add environment setup (currently not needed)
	}
	
	public void scheduleProfilers() {
		// TODO add profiler scheduling
	}
	
	public void scheduleCheckpoints() {
		// TODO add checkpoint scheduling
	}
	
	public void scheduleHelpers() {
		// TODO add helper scheduling
	}
	
	public void scheduleComponents() {
		// TODO add component scheduling
	}




//		double voxels = series._populations

//		1. Calculate the number of voxels per cell as V = VC/v where VC is the critical volume and v is the number of voxels per cell
//		2. Divide the environment into equal squares of size S x S where S = ceil(sqrt(4*V/pi)) + 2 and V is the largest number of voxels per cell across populations
//		3. Select the center point of each square

//		int r = 2;
//		ArrayList<ArrayList<Voxel>> locations = new ArrayList<>();
//		int n = 2*r + 2;
//		
//		for (int i = 0; i < (series._length - 2)/n; i++) {
//			for (int j = 0; j < (series._width - 2)/n; j++) {
//				ArrayList<Voxel> voxels = new ArrayList<>();
//				double cx = i*n + 1 - 0.5 + n/2.;
//				double cy = j*n + 1 - 0.5 + n/2.;
//				
//				for (int ii = 1; ii < n - 1; ii++) {
//					for (int jj = 1; jj < n - 1; jj++) {
//						int x = i*n + ii;
//						int y = j*n + jj;
//						
//						double d = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
//						if (d <= r) { voxels.add(new Voxel(x, y, 0)); }
//					}
//				}
//				
//				locations.add(voxels);
//			}
//		}
//		


//	
////	private List<String> getRecordFromLine(String line) {
////		List<String> values = new ArrayList<>();
////		try (Scanner rowScanner = new Scanner(line)) {
////			rowScanner.useDelimiter(COMMA_DELIMITER);
////		}
////		return values;
////	}
//	
//	Pattern pattern = Pattern.compile("([0-9]+),(\\-[0-9]+),([0-9]+),([0-9]+),([0-9]+)");
//	
//	Map<Integer, Location> parseVoxels(String file) {
//		List<String> records = new ArrayList<>();
//		
//		try {
//			FileInputStream stream = new FileInputStream(file);
//			Scanner scanner = new Scanner(stream);
//			while (scanner.hasNextLine()) { records.add(scanner.nextLine()); }
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		Map<Integer, Location> map = new HashMap<>();
//		
//		
//		for (String record : records) {
//			if (pattern.matcher(record).matches()) {
//				Matcher matcher = pattern.matcher(record);
//				
//				while (matcher.find()) {
//					Location loc;
//					int id = Integer.parseInt(matcher.group(1));
//					
//					if (map.containsKey(id)) {
//						loc = map.get(id);
//					} else {
//						loc = new PottsLocations3D(new ArrayList<>());
//						map.put(id, loc);
//						
//					}
//					
////					System.out.println(record);
////					System.out.println(matcher.group(1));
//					
//					loc.add(Integer.parseInt(matcher.group(2)),
//							Integer.parseInt(matcher.group(3)),
//							Integer.parseInt(matcher.group(4)),
//							Integer.parseInt(matcher.group(5)));
//				}
//				
//			}
//		}
//		return map;
//	}





//		
//		Map<Integer, Location> locationws = parseVoxels("positions.csv");
//	
//		
//			
//			agents.addObject(id, c);
//			c.initialize(potts.IDS, potts.TAGS);
////			c.update(potts.potts);
////			c.location.update(potts.potts, c.id);
////			c.stopper = 
////			schedule.scheduleRepeating(0, ORDERING_CELLS, c);
////			scheduleCell(c);
//			c.schedule(schedule);
//
//
//			id++;
//			
//			break;
//			
////			if (id > 8) {break;}
//					
//
//		}
//
//		nextID = id;
//
//		schedule.scheduleRepeating(1, ORDERING_POTTS, potts);


//	public ArrayList<PottsLocations3D> getSquareLocations() {
//		ArrayList<PottsLocations3D> locations = new ArrayList<>();
////		int n = 11;
////		int nn = (n - 1)/4 ;
//////		int id = 1;
////		
////		int offset = 10;
////		
////		int z = 2;
////
////		for (int i = 1; i < (series._length  - 1)/n; i++) {
////			for (int j = 1; j < (series._width  - 1)/n; j++) {
////				ArrayList<PottsLocation3D.Voxel> voxels = new ArrayList<>();
////				ArrayList<PottsLocation3D.Voxel> membrane = new ArrayList<>();
////				ArrayList<PottsLocation3D.Voxel> nucleus = new ArrayList<>();
////				ArrayList<PottsLocation3D.Voxel> other = new ArrayList<>();
////
////				double rand = random.nextDouble();
//////				if (rand < 0.6) { continue; }
////
////				for (int ii = 0; ii < n  - 1; ii++) {
////					for (int jj = 0; jj < n - 1 ; jj++) {
////						voxels.add(new PottsLocation3D.Voxel(i*n + ii, j*n + jj, z));
////						voxels.add(new PottsLocation3D.Voxel(i*n + ii, j*n + jj, z - 1));
////						voxels.add(new PottsLocation3D.Voxel(i*n + ii, j*n + jj, z + 1));
////					}
////				}
////				
////				PottsLocations3D loc = new PottsLocations3D(new ArrayList<>() );
////				
////				
////				for (int ii = 0; ii < n - 1; ii++) {
////					for (int jj = 0; jj < n - 1 ; jj++) {
////						if (ii >= nn && ii < n - nn - 1 && jj >= nn && jj < n -nn - 1) {
////
////							loc.add(TAG_NUCLEUS ,i*n + ii, j*n + jj, z);
////							loc.add(TAG_CYTOPLASM,i*n + ii, j*n + jj, z + 1);
////							loc.add(TAG_CYTOPLASM,i*n + ii, j*n + jj, z - 1);
////
////							
////						}
////						
////						else {
////							loc.add(TAG_CYTOPLASM,i*n + ii, j*n + jj, z);
////							loc.add(TAG_CYTOPLASM,i*n + ii, j*n + jj, z + 1);
////							loc.add(TAG_CYTOPLASM,i*n + ii, j*n + jj, z - 1);
////						}
////					}
////				}
////				
////				locations.add(loc);
////			}
////		}
//
//		return locations;
//	}
//
}