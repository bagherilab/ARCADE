package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sim.engine.Schedule;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.sim.Simulation.*;
import static arcade.sim.Potts.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.sim.Series.SEED_OFFSET;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public class PottsSimulationTest {
	private static final double EPSILON = 1E-4;
	static final long RANDOM_SEED = (long)(Math.random()*1000);
	private static final int TOTAL_LOCATIONS = 6;
	static Series seriesZeroPop, seriesOnePop, seriesMultiPop;
	
	static double random() { return Math.random()*100; }
	
	static Series createSeries(int[] pops, String[] keys) {
		Series series = mock(Series.class);
		HashMap<String, MiniBox> populations = new HashMap<>();
		
		for (int i = 0; i < pops.length; i++) {
			MiniBox population = new MiniBox();
			population.put("CODE", pops[i]);
			population.put("ADHESION:*", random());
			populations.put(keys[i], population);
			
			for (String key : keys) {
				population.put("ADHESION" + TARGET_SEPARATOR + key, random());
			}
		}
		
		series._populations = populations;
		
		return series;
	}
	
	static Series createSeries(int[] pops, String[] keys, double[] volumes) {
		Series series = mock(Series.class);
		HashMap<String, MiniBox> populations = new HashMap<>();
		
		for (int i = 0; i < pops.length; i++) {
			MiniBox population = new MiniBox();
			population.put("CODE", pops[i]);
			population.put("ADHESION:*", 0);
			population.put("CRITICAL_VOLUME", volumes[i]);
			populations.put(keys[i], population);
			
			for (String key : keys) {
				population.put("ADHESION" + TARGET_SEPARATOR + key, 0);
			}
		}
		
		series._populations = populations;
		
		return series;
	}
	
	static Series createSeries(int[] pops, String[] keys, double[] volumes,
							   int length, int width, int height) {
		Series series = createSeries(pops, keys, volumes);
		
		try {
			Field lengthField = Series.class.getDeclaredField("_length");
			lengthField.setAccessible(true);
			lengthField.setInt(series, length);
			
			Field widthField = Series.class.getDeclaredField("_width");
			widthField.setAccessible(true);
			widthField.setInt(series, width);
			
			Field heightField = Series.class.getDeclaredField("_height");
			heightField.setAccessible(true);
			heightField.setInt(series, height);
		} catch (Exception ignored) { }
		
		return series;
	}
	
	@BeforeClass
	public static void setupSeries() {
		// Zero populations.
		seriesZeroPop = createSeries(new int[0], new String[0]);
		
		// One population.
		seriesOnePop = createSeries(new int[] { 1 }, new String[] { "A" });
		seriesOnePop._populations.get("A").put("FRACTION", 5./TOTAL_LOCATIONS);
		
		// Multiple populations.
		seriesMultiPop = createSeries(new int[] { 1, 2, 3 }, new String[] { "B", "C", "D" });
		seriesMultiPop._populations.get("B").put("FRACTION", 3./TOTAL_LOCATIONS);
		seriesMultiPop._populations.get("C").put("FRACTION", 1./TOTAL_LOCATIONS);
		seriesMultiPop._populations.get("D").put("FRACTION", 2./TOTAL_LOCATIONS);
	}
	
	static class PottsSimulationMock extends PottsSimulation {
		PottsSimulationMock(long seed, Series series) { super(seed, series); }
		
		public Potts makePotts() { return mock(Potts.class); }
		
		ArrayList<int[]> makeCenters() {
			ArrayList<int[]> centers = new ArrayList<>();
			centers.add(new int[] { 0, 0, 0 });
			centers.add(new int[] { 1, 0, 0 });
			centers.add(new int[] { 2, 0, 0 });
			centers.add(new int[] { 3, 0, 0 });
			centers.add(new int[] { 4, 0, 0 });
			centers.add(new int[] { 5, 0, 0 });
			return centers;
		}
		
		Location makeLocation(MiniBox population, int[] center) {
			PottsLocation loc = mock(PottsLocation.class);
			when(loc.getCenter()).thenReturn(new Voxel(center[0], center[1], center[2]));
			return loc;
		}
		
		Cell makeCell(int id, int pop, Location location,
					  double[] criticals, double[] lambdas, double[] adhesion) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getLocation()).thenReturn(location);
			
			when(cell.getCriticalVolume()).thenReturn(criticals[0]);
			when(cell.getCriticalSurface()).thenReturn(criticals[1]);
			
			when(cell.getLambda(TERM_VOLUME)).thenReturn(lambdas[0]);
			when(cell.getLambda(TERM_SURFACE)).thenReturn(lambdas[1]);
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			return cell;
		}
		
		Cell makeCell(int id, int pop, Location location,
					  double[] criticals, double[] lambdas, double[] adhesion, int tags,
					  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getLocation()).thenReturn(location);
			
			when(cell.getCriticalVolume()).thenReturn(criticals[0]);
			when(cell.getCriticalSurface()).thenReturn(criticals[1]);
			
			when(cell.getLambda(TERM_VOLUME)).thenReturn(lambdas[0]);
			when(cell.getLambda(TERM_SURFACE)).thenReturn(lambdas[1]);
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			for (int i = 0; i < tags; i++) {
				when(cell.getCriticalVolume(-i - 1)).thenReturn(criticalsTag[0][i]);
				when(cell.getCriticalSurface(-i - 1)).thenReturn(criticalsTag[1][i]);
				
				when(cell.getLambda(TERM_VOLUME, -i - 1)).thenReturn(lambdasTag[0][i]);
				when(cell.getLambda(TERM_SURFACE, -i - 1)).thenReturn(lambdasTag[1][i]);
				
				for (int j = 0; j < tags; j++) {
					when(cell.getAdhesion(-i - 1, -j - 1)).thenReturn(adhesionsTag[i][j]);
				}
			}
			
			return cell;
		}
	}
	
	@Test
	public void getSeries_initialized_returnsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertEquals(series, sim.getSeries());
	}
	
	@Test
	public void getSchedule_initialized_returnsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertEquals(sim.schedule, sim.getSchedule());
	}
	
	@Test
	public void getSeed_initialized_returnsValue() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED + SEED_OFFSET, series);
		assertEquals(RANDOM_SEED, sim.getSeed());
	}
	
	@Test
	public void getID_initialized_incrementsValue() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertEquals(1, sim.getID());
		assertEquals(2, sim.getID());
		assertEquals(3, sim.getID());
	}
	
	@Test
	public void getID_started_resetsValues() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.start();
		assertEquals(1, sim.getID());
		sim.start();
		assertEquals(1, sim.getID());
	}
	
	@Test
	public void getPotts_initialized_returnsNull() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertNull(sim.getPotts());
	}
	
	@Test
	public void getAgents_initialized_returnsNull() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertNull(sim.getAgents());
	}
	
	@Test
	public void getEnvironments_initialized_returnsNull() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		assertNull(sim.getEnvironment(""));
	}
	
	@Test
	public void start_callsMethods() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesZeroPop));
		sim.start();
		
		verify(sim).setupPotts();
		verify(sim).setupAgents();
		verify(sim).setupEnvironment();
		verify(sim).scheduleProfilers();
		verify(sim).scheduleCheckpoints();
		verify(sim).scheduleHelpers();
		verify(sim).scheduleComponents();
	}
	
	@Test
	public void setupPotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.setupPotts();
		assertNotNull(sim.potts);
	}
	
	@Test
	public void setupPotts_mockSeries_schedulesPotts() {
		Series series = mock(Series.class);
		Schedule schedule = spy(Schedule.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.schedule = schedule;
		sim.setupPotts();
		verify(sim.schedule).scheduleRepeating(1, ORDERING_POTTS, sim.potts);
	}
	
	@Test
	public void setupAgents_anyPopulation_setsPotts() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(sim.agents, sim.potts.grid);
	}
	
	@Test
	public void setupAgents_exceedsLocations_setsPotts() {
		Series series = createSeries(new int[] { 1 }, new String[] { "A" });
		series._populations.get("A").put("FRACTION", 2);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(TOTAL_LOCATIONS, sim.agents.getAllObjects().numObjs);
	}
	
	@Test
	public void setupAgents_zeroPopulations_initializesGrid() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertNotNull(sim.agents);
	}
	
	@Test
	public void setupAgents_zeroPopulations_createsNoAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesZeroPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(0, sim.agents.getAllObjects().numObjs);
	}
	
	@Test
	public void setupAgents_onePopulation_createsAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesOnePop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(TOTAL_LOCATIONS - 1, sim.agents.getAllObjects().numObjs);
		
		HashSet<Voxel> assignedLocations = new HashSet<>();
		HashSet<Voxel> possibleLocations = new HashSet<>();
		
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			possibleLocations.add(new Voxel(i, 0, 0));
		}
		
		int[] pops = new int[] { 1, 1, 1, 1, 1 };
		for (int i = 0; i < TOTAL_LOCATIONS - 1; i++) {
			Cell cell = (Cell)sim.agents.getAllObjects().get(i);
			assertEquals(i + 1, cell.getID());
			assertEquals(pops[i], cell.getPop());
			assignedLocations.add(cell.getLocation().getCenter());
		}
		
		assertTrue(possibleLocations.containsAll(assignedLocations));
		assertNull(sim.agents.getObjectAt(6));
		assertEquals(TOTAL_LOCATIONS, sim.getID());
	}
	
	@Test
	public void setupAgents_onePopulation_callMethods() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesOnePop));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		
		for (Object obj : sim.agents.getAllObjects()) {
			verify((Cell)obj).initialize(sim.potts.IDS, sim.potts.TAGS);
			verify((Cell)obj).schedule(sim.schedule);
		}
	}
	
	@Test
	public void setupAgents_multiplePopulations_createsAgents() {
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, seriesMultiPop);
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		assertEquals(TOTAL_LOCATIONS, sim.agents.getAllObjects().numObjs);
		
		HashSet<Voxel> assignedLocations = new HashSet<>();
		HashSet<Voxel> possibleLocations = new HashSet<>();
		
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			possibleLocations.add(new Voxel(i, 0, 0));
		}
		
		int[] pops = new int[] { 1, 1, 1, 2, 3, 3 };
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			Cell cell = (Cell)sim.agents.getAllObjects().get(i);
			assertEquals(i + 1, cell.getID());
			assertEquals(pops[i], cell.getPop());
			assignedLocations.add(cell.getLocation().getCenter());
		}
		
		assertTrue(possibleLocations.containsAll(assignedLocations));
		assertEquals(TOTAL_LOCATIONS + 1, sim.getID());
	}
	
	@Test
	public void setupAgents_multiplePopulations_callMethods() {
		PottsSimulationMock sim = spy(new PottsSimulationMock(RANDOM_SEED, seriesMultiPop));
		sim.potts = mock(Potts.class);
		sim.setupAgents();
		
		for (Object obj : sim.agents.getAllObjects()) {
			verify((Cell)obj).initialize(sim.potts.IDS, sim.potts.TAGS);
			verify((Cell)obj).schedule(sim.schedule);
		}
	}
	
	@Test
	public void makeCell_onePopulationNoTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:A", adhesion[1]);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		HashSet<String> keys = new HashSet<>();
		keys.add("A");
		series._populations = mock(HashMap.class);
		when(series._populations.keySet()).thenReturn(keys);
		when(series._populations.get("A")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("A").getInt("CODE")).thenReturn(1);
		Cell cell = sim.makeCell(cellID, population, new int[] { 0, 0, 0 });
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
	}
	
	@Test
	public void makeCell_multiplePopulationsNoTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random(), random(), random() };
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:B", adhesion[1]);
		population.put("ADHESION:C", adhesion[2]);
		population.put("ADHESION:D", adhesion[3]);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		HashSet<String> keys = new HashSet<>();
		keys.add("B"); keys.add("C"); keys.add("D");
		series._populations = mock(HashMap.class);
		when(series._populations.keySet()).thenReturn(keys);
		when(series._populations.get("B")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("B").getInt("CODE")).thenReturn(1);
		when(series._populations.get("C")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("C").getInt("CODE")).thenReturn(2);
		when(series._populations.get("D")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("D").getInt("CODE")).thenReturn(3);
		Cell cell = sim.makeCell(cellID, population, new int[] { 0, 0, 0 });
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(adhesion[2], cell.getAdhesion(2), EPSILON);
		assertEquals(adhesion[3], cell.getAdhesion(3), EPSILON);
	}
	
	@Test
	public void makeCell_onePopulationWithTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		double[][] criticalsTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] lambdasTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] adhesionTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:A", adhesion[1]);
		population.put("TAG/a", 0);
		population.put("TAG/b", 0);
		population.put("TAG/c", 0);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		int tags = 3;
		String[] _tags = new String[] { "a", "b", "c" };
		for (int i = 0; i < tags; i++) {
			String tag = _tags[i];
			population.put(tag + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasTag[1][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsTag[1][i]);
			
			for (int j = 0; j < tags; j++) {
				population.put(tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + _tags[j], adhesionTag[i][j]);
			}
		}
		
		HashSet<String> keys = new HashSet<>();
		keys.add("A");
		series._populations = mock(HashMap.class);
		when(series._populations.keySet()).thenReturn(keys);
		when(series._populations.get("A")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("A").getInt("CODE")).thenReturn(1);
		Cell cell = sim.makeCell(cellID, population, new int[] { 0, 0, 0 });
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		
		for (int i = 0; i < tags; i++) {
			assertEquals(criticalsTag[0][i], cell.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			
			for (int j = 0; j < tags; j++) {
				assertEquals(adhesionTag[i][j], cell.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
	
	@Test
	public void makeCell_multiplePopulationsWithTags_createsObject() {
		Series series = mock(Series.class);
		PottsSimulationMock sim = new PottsSimulationMock(RANDOM_SEED, series);
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random(), random(), random() };
		
		double[][] criticalsTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] lambdasTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] adhesionTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:B", adhesion[1]);
		population.put("ADHESION:C", adhesion[2]);
		population.put("ADHESION:D", adhesion[3]);
		population.put("TAG/a", 0);
		population.put("TAG/b", 0);
		population.put("TAG/c", 0);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		int tags = 3;
		String[] _tags = new String[] { "a", "b", "c" };
		for (int i = 0; i < tags; i++) {
			String tag = _tags[i];
			population.put(tag + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasTag[1][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsTag[1][i]);
			
			for (int j = 0; j < tags; j++) {
				population.put(tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + _tags[j], adhesionTag[i][j]);
			}
		}
		
		HashSet<String> keys = new HashSet<>();
		keys.add("B"); keys.add("C"); keys.add("D");
		series._populations = mock(HashMap.class);
		when(series._populations.keySet()).thenReturn(keys);
		when(series._populations.get("B")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("B").getInt("CODE")).thenReturn(1);
		when(series._populations.get("C")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("C").getInt("CODE")).thenReturn(2);
		when(series._populations.get("D")).thenReturn(mock(MiniBox.class));
		when(series._populations.get("D").getInt("CODE")).thenReturn(3);
		Cell cell = sim.makeCell(cellID, population, new int[] { 0, 0, 0 });
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(adhesion[2], cell.getAdhesion(2), EPSILON);
		assertEquals(adhesion[3], cell.getAdhesion(3), EPSILON);
		
		for (int i = 0; i < tags; i++) {
			assertEquals(criticalsTag[0][i], cell.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			
			for (int j = 0; j < tags; j++) {
				assertEquals(adhesionTag[i][j], cell.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
}
