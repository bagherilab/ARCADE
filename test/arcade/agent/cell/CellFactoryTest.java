package arcade.agent.cell;

import org.junit.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import arcade.sim.Series;
import arcade.sim.output.OutputLoader;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import arcade.env.loc.Location;
import arcade.agent.module.Module;
import static arcade.sim.Potts.*;
import static arcade.MainTest.*;
import static arcade.agent.cell.CellFactory.CellContainer;
import static arcade.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.util.MiniBox.TAG_SEPARATOR;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.sim.Simulation.DS;
import static arcade.agent.cell.Cell.*;

public class CellFactoryTest {
	static final double EPSILON = 1E-10;
	
	static double random() { return Math.random()*100; }
	
	static Series createSeries(int[] init, double[] volumes) {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
		for (int i = 0; i < volumes.length; i++) {
			int pop = i + 1;
			MiniBox box = new MiniBox();
			box.put("CODE", pop);
			box.put("INIT", init[i]);
			box.put("CRITICAL_VOLUME", volumes[i]);
			series._populations.put("pop" + pop, box);
		}
		
		return series;
	}
	
	static class CellFactoryMock extends CellFactory {
		public CellFactoryMock() { super(); }
		
		Cell makeCell(int id, int pop, int age, int state, Location location,
					  double[] criticals, double[] lambdas, double[] adhesion) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getAge()).thenReturn(age);
			when(cell.getState()).thenReturn(state);
			when(cell.getLocation()).thenReturn(location);
			
			Module module = mock(Module.class);
			when(cell.getModule()).thenReturn(module);
			
			when(cell.getCriticalVolume()).thenReturn(criticals[0]);
			when(cell.getCriticalSurface()).thenReturn(criticals[1]);
			
			when(cell.getLambda(TERM_VOLUME)).thenReturn(lambdas[0]);
			when(cell.getLambda(TERM_SURFACE)).thenReturn(lambdas[1]);
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			return cell;
		}
		
		Cell makeCell(int id, int pop, int age, int state, Location location,
					  double[] criticals, double[] lambdas, double[] adhesion, int tags,
					  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getAge()).thenReturn(age);
			when(cell.getState()).thenReturn(state);
			when(cell.getLocation()).thenReturn(location);
			
			Module module = mock(Module.class);
			when(cell.getModule()).thenReturn(module);
			
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
	public void initialize_noLoading_callsMethod() {
		CellFactory factory = spy(new CellFactoryMock());
		Series series = mock(Series.class);
		series.loader = null;
		
		doNothing().when(factory).parseValues(series);
		doNothing().when(factory).loadCells(series);
		doNothing().when(factory).createCells(series);
		
		factory.initialize(series);
		
		verify(factory).parseValues(series);
		verify(factory, never()).loadCells(series);
		verify(factory).createCells(series);
	}
	
	@Test
	public void initialize_noLoadingWithLoader_callsMethod() {
		CellFactory factory = spy(new CellFactoryMock());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		try {
			Field field = OutputLoader.class.getDeclaredField("loadCells");
			field.setAccessible(true);
			field.set(series.loader, false);
		} catch (Exception ignored) { }
		
		doNothing().when(factory).parseValues(series);
		doNothing().when(factory).loadCells(series);
		doNothing().when(factory).createCells(series);
		
		factory.initialize(series);
		
		verify(factory).parseValues(series);
		verify(factory, never()).loadCells(series);
		verify(factory).createCells(series);
	}
	
	@Test
	public void initialize_withLoadingWithLoader_callsMethod() {
		CellFactory factory = spy(new CellFactoryMock());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		try {
			Field field = OutputLoader.class.getDeclaredField("loadCells");
			field.setAccessible(true);
			field.set(series.loader, true);
		} catch (Exception ignored) { }
		
		doNothing().when(factory).parseValues(series);
		doNothing().when(factory).loadCells(series);
		doNothing().when(factory).createCells(series);
		
		factory.initialize(series);
		
		verify(factory).parseValues(series);
		verify(factory).loadCells(series);
		verify(factory, never()).createCells(series);
	}
	
	@Test
	public void parseValues_noTags_updatesLists() {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random(), random(), random() };
		
		String[] popKeys = new String[] { "A", "B", "C" };
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			MiniBox population = new MiniBox();
			
			population.put("CODE", pop);
			population.put("ADHESION:*", adhesion[0]);
			population.put("ADHESION:A", adhesion[1]);
			population.put("ADHESION:B", adhesion[2]);
			population.put("ADHESION:C", adhesion[3]);
			population.put("LAMBDA_VOLUME", lambdas[0] + pop);
			population.put("LAMBDA_SURFACE", lambdas[1] + pop);
			population.put("CRITICAL_VOLUME", criticals[0] + pop);
			population.put("CRITICAL_SURFACE", criticals[1] + pop);
			
			series._populations.put(popKeys[i], population);
		}
		
		CellFactory factory = new CellFactoryMock();
		factory.parseValues(series);
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			assertEquals(criticals[0] + pop, factory.popToCriticals.get(pop)[0], EPSILON);
			assertEquals(criticals[1] + pop, factory.popToCriticals.get(pop)[1], EPSILON);
			assertEquals(lambdas[0] + pop, factory.popToLambdas.get(pop)[0], EPSILON);
			assertEquals(lambdas[1] + pop, factory.popToLambdas.get(pop)[1], EPSILON);
			assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
			assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
		}
	}
	
	@Test
	public void parseValues_withTags_updatesLists() {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
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
		
		String[] popKeys = new String[] { "A", "B", "C" };
		String[] tagKeys = new String[] { "a", "b", "c" };
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			MiniBox population = new MiniBox();
			
			population.put("CODE", pop);
			population.put("ADHESION:*", adhesion[0]);
			population.put("ADHESION:A", adhesion[1]);
			population.put("ADHESION:B", adhesion[2]);
			population.put("ADHESION:C", adhesion[3]);
			population.put("LAMBDA_VOLUME", lambdas[0] + pop);
			population.put("LAMBDA_SURFACE", lambdas[1] + pop);
			population.put("CRITICAL_VOLUME", criticals[0] + pop);
			population.put("CRITICAL_SURFACE", criticals[1] + pop);
			
			for (int j = 0; j < tagKeys.length; j++) {
				String tag = tagKeys[j];
				
				population.put("TAG" + TAG_SEPARATOR + tag, 0);
				population.put(tag + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasTag[0][j] + pop);
				population.put(tag + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasTag[1][j] + pop);
				population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsTag[0][j] + pop);
				population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsTag[1][j] + pop);
				
				for (int k = 0; k < tagKeys.length; k++) {
					population.put(tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + tagKeys[k], adhesionTag[j][k] + pop);
				}
			}
			
			series._populations.put(popKeys[i], population);
		}
		
		CellFactory factory = new CellFactoryMock();
		factory.parseValues(series);
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			assertEquals(criticals[0] + pop, factory.popToCriticals.get(pop)[0], EPSILON);
			assertEquals(criticals[1] + pop, factory.popToCriticals.get(pop)[1], EPSILON);
			assertEquals(lambdas[0] + pop, factory.popToLambdas.get(pop)[0], EPSILON);
			assertEquals(lambdas[1] + pop, factory.popToLambdas.get(pop)[1], EPSILON);
			assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
			assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
			
			assertArrayEquals(tagKeys, factory.popToTags.get(pop).toArray());
			
			for (int j = 0; j < tagKeys.length; j++) {
				assertEquals(criticalsTag[TERM_VOLUME][j] + pop, factory.popToTagCriticals.get(pop)[TERM_VOLUME][j], EPSILON);
				assertEquals(criticalsTag[TERM_SURFACE][j] + pop, factory.popToTagCriticals.get(pop)[TERM_SURFACE][j], EPSILON);
				assertEquals(lambdasTag[TERM_VOLUME][j] + pop, factory.popToTagLambdas.get(pop)[TERM_VOLUME][j], EPSILON);
				assertEquals(lambdasTag[TERM_SURFACE][j] + pop, factory.popToTagLambdas.get(pop)[TERM_SURFACE][j], EPSILON);
				
				for (int k = 0; k < tagKeys.length; k++) {
					assertEquals(adhesionTag[j][k] + pop, factory.popToTagAdhesion.get(pop)[j][k], EPSILON);
				}
			}
		}
	}
	
	@Test
	public void loadCells_givenLoadedValidPops_updatesLists() {
		int N = randomInt();
		int M = randomInt();
		ArrayList<CellContainer> containers = new ArrayList<>();
		
		for (int i = 0; i < N; i++) {
			CellContainer container = new CellContainer(i, 1, randomInt());
			containers.add(container);
		}
		
		for (int i = N; i < N + M; i++) {
			CellContainer container = new CellContainer(i, 2, randomInt());
			containers.add(container);
		}
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		series._populations = new HashMap<>();
		
		MiniBox pop1 = new MiniBox();
		pop1.put("CODE", 1);
		pop1.put("INIT", N);
		series._populations.put("A", pop1);
		
		MiniBox pop2 = new MiniBox();
		pop2.put("CODE", 2);
		pop2.put("INIT", M);
		series._populations.put("B", pop2);
		
		doAnswer(invocation -> {
			factory.container = new CellFactoryContainer();
			for (int i = 0; i < N + M; i++) { factory.container.cells.add(containers.get(i)); }
			return null;
		}).when(series.loader).load(factory);
				
		factory.loadCells(series);
		assertEquals(N + M, factory.cells.size());
		assertEquals(N, factory.popToIDs.get(1).size());
		assertEquals(M, factory.popToIDs.get(2).size());
		for (int i = 0; i < N; i++) {
			assertEquals(i, factory.container.cells.get(i).id);
			assertEquals(1, factory.container.cells.get(i).pop);
		}
		for (int i = N; i < N + M; i++) {
			assertEquals(i, factory.container.cells.get(i).id);
			assertEquals(2, factory.container.cells.get(i).pop);
		}
	}
	
	@Test
	public void loadCells_givenLoadedInvalidPops_updatesLists() {
		int N = randomInt();
		ArrayList<CellContainer> containers = new ArrayList<>();
		
		for (int i = 0; i < N; i++) {
			CellContainer container = new CellContainer(i, 1, randomInt());
			containers.add(container);
		}
		
		CellFactoryMock factory = new CellFactoryMock();
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		series._populations = new HashMap<>();
		
		MiniBox pop1 = new MiniBox();
		pop1.put("CODE", 1);
		pop1.put("INIT", N);
		series._populations.put("A", pop1);
		
		doAnswer(invocation -> {
			factory.container = new CellFactoryContainer();
			for (int i = 0; i < N; i++) { factory.container.cells.add(containers.get(i)); }
			return null;
		}).when(series.loader).load(factory);
		
		factory.loadCells(series);
		assertEquals(0, factory.cells.size());
		assertFalse(factory.popToIDs.containsKey(1));
	}
	
	@Test
	public void loadCells_givenLoadedLimitedInit_updatesLists() {
		int n = randomInt();
		int N = n + randomInt();
		int M = randomInt();
		ArrayList<CellContainer> containers = new ArrayList<>();
		
		for (int i = 0; i < N; i++) {
			CellContainer container = new CellContainer(i, 1, randomInt());
			containers.add(container);
		}
		
		for (int i = N; i < N + M; i++) {
			CellContainer container = new CellContainer(i, 2, randomInt());
			containers.add(container);
		}
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		series._populations = new HashMap<>();
		
		MiniBox pop1 = new MiniBox();
		pop1.put("CODE", 1);
		pop1.put("INIT", n);
		series._populations.put("A", pop1);
		
		MiniBox pop2 = new MiniBox();
		pop2.put("CODE", 2);
		pop2.put("INIT", M);
		series._populations.put("B", pop2);
		
		doAnswer(invocation -> {
			factory.container = new CellFactoryContainer();
			for (int i = 0; i < N + M; i++) { factory.container.cells.add(containers.get(i)); }
			return null;
		}).when(series.loader).load(factory);
		
		factory.loadCells(series);
		assertEquals(n + M, factory.cells.size());
		assertEquals(n, factory.popToIDs.get(1).size());
		assertEquals(M, factory.popToIDs.get(2).size());
		for (int i = 0; i < n; i++) {
			assertEquals(i, factory.container.cells.get(i).id);
			assertEquals(1, factory.container.cells.get(i).pop);
		}
		for (int i = N; i < N + M; i++) {
			assertEquals(i, factory.container.cells.get(i).id);
			assertEquals(2, factory.container.cells.get(i).pop);
		}
	}
	
	@Test
	public void createCells_noPopulation_createsEmpty() {
		Series series = createSeries(new int[] { }, new double[] { });
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.createCells(series);
		
		assertEquals(0, factory.cells.size());
		assertEquals(0, factory.popToIDs.size());
	}
	
	@Test
	public void createCells_onePopulationNoTags_createsList() {
		int voxels = randomInt();
		int init = randomInt();
		Series series = createSeries(new int[] { init }, new double[] { voxels*DS });
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) { assertEquals(voxels, factory.cells.get(i).voxels); }
	}
	
	@Test
	public void createCells_onePopulationWithTags_createsList() {
		int voxelsA = randomInt();
		int voxelsB = randomInt();
		int voxelsC = randomInt();
		
		int voxels = voxelsA + voxelsB + voxelsC;
		int init = randomInt();
		
		Series series = createSeries(new int[] { init }, new double[] { voxels*DS });
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("A");
		tags.add("B");
		tags.add("C");
		
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "A", (double)voxelsA/voxels);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "B", (double)voxelsB/voxels);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "C", (double)voxelsC/voxels);
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToTags.put(1, tags);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			assertEquals(voxels, factory.cells.get(i).voxels);
			assertEquals(voxelsA, (int)factory.cells.get(i).tagVoxels.get("A"));
			assertEquals(voxelsB, (int)factory.cells.get(i).tagVoxels.get("B"));
			assertEquals(voxelsC, (int)factory.cells.get(i).tagVoxels.get("C"));
		}
	}
	
	@Test
	public void createCells_multiplePopulationsNoTags_createsList() {
		int voxels1 = randomInt();
		int voxels2 = randomInt();
		int voxels3 = randomInt();
		
		int init1 = randomInt();
		int init2 = randomInt();
		int init3 = randomInt();
		
		Series series = createSeries(new int[] { init1, init2, init3 },
				new double[] { voxels1*DS, voxels2*DS, voxels3*DS });
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		factory.popToIDs.put(3, new HashSet<>());
		factory.createCells(series);
		
		assertEquals(init1 + init2 + init3, factory.cells.size());
		assertEquals(init1, factory.popToIDs.get(1).size());
		assertEquals(init2, factory.popToIDs.get(2).size());
		assertEquals(init3, factory.popToIDs.get(3).size());
		
		for (int i : factory.popToIDs.get(1)) { assertEquals(voxels1, factory.cells.get(i).voxels); }
		for (int i : factory.popToIDs.get(2)) { assertEquals(voxels2, factory.cells.get(i).voxels); }
		for (int i : factory.popToIDs.get(3)) { assertEquals(voxels3, factory.cells.get(i).voxels); }
	}
	
	@Test
	public void createCells_multiplePopulationsWithTags_createsList() {
		int voxelsA = 10*randomInt();
		int voxelsB = 10*randomInt();
		int voxelsC = 10*randomInt();
		
		int voxels1 = randomInt();
		int voxels2 = voxelsA + voxelsB + voxelsC;
		int voxels3 = randomInt();
		
		int init1 = randomInt();
		int init2 = randomInt();
		int init3 = randomInt();
		
		Series series = createSeries(new int[] { init1, init2, init3 },
				new double[] { voxels1*DS, voxels2*DS, voxels3*DS });
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("A");
		tags.add("B");
		tags.add("C");
		
		series._populations.get("pop2").put("TAG" + TAG_SEPARATOR + "A", (double)voxelsA/voxels2);
		series._populations.get("pop2").put("TAG" + TAG_SEPARATOR + "B", (double)voxelsB/voxels2);
		series._populations.get("pop2").put("TAG" + TAG_SEPARATOR + "C", (double)voxelsC/voxels2);
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		factory.popToIDs.put(3, new HashSet<>());
		factory.popToTags.put(2, tags);
		factory.createCells(series);
		
		assertEquals(init1 + init2 + init3, factory.cells.size());
		assertEquals(init1, factory.popToIDs.get(1).size());
		assertEquals(init2, factory.popToIDs.get(2).size());
		assertEquals(init3, factory.popToIDs.get(3).size());
		
		for (int i : factory.popToIDs.get(1)) { assertEquals(voxels1, factory.cells.get(i).voxels); }
		for (int i : factory.popToIDs.get(2)) {
			assertEquals(voxels2, factory.cells.get(i).voxels);
			assertEquals(voxelsA, (int)factory.cells.get(i).tagVoxels.get("A"));
			assertEquals(voxelsB, (int)factory.cells.get(i).tagVoxels.get("B"));
			assertEquals(voxelsC, (int)factory.cells.get(i).tagVoxels.get("C"));
		}
		for (int i : factory.popToIDs.get(3)) { assertEquals(voxels3, factory.cells.get(i).voxels); }
	}
	
	@Test
	public void createCells_extraTags_skipsExtra() {
		int voxel = randomInt();
		int voxels = 4*voxel;
		int init = randomInt();
		
		Series series = createSeries(new int[] { init }, new double[] { voxels*DS });
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("A");
		tags.add("B");
		
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "A", 0.75);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "B", 0.75);
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToTags.put(1, tags);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			assertEquals(voxels, factory.cells.get(i).voxels);
			assertEquals(3*voxel, (int)factory.cells.get(i).tagVoxels.get("A"));
			assertEquals(voxel, (int)factory.cells.get(i).tagVoxels.get("B"));
		}
	}
	
	@Test
	public void make_onePopulationNoTagsNoTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		int cellState = (int)(Math.random()*5);
		int cellPhase = (int)random();
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		factory.popToCriticals.put(cellPop, criticals);
		factory.popToLambdas.put(cellPop, lambdas);
		factory.popToAdhesion.put(cellPop, adhesion);
		
		CellContainer container = new CellContainer(cellID, cellPop, cellAge, cellState, cellPhase,
				0, null, 0, 0, null, null);
		Cell cell = factory.make(container, location);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellAge, cell.getAge());
		assertEquals(cellState, cell.getState());
		verify(cell.getModule()).setPhase(cellPhase);
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(0, cell.getTargetVolume(), EPSILON);
		assertEquals(0, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void make_multiplePopulationsNoTagsNoTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID1 = (int)random() + 1;
		int cellPop1 = (int)random() + 1;
		int cellAge1 = (int)random();
		int cellState1 = (int)(Math.random()*5);
		int cellPhase1 = (int)random();
		double[] criticals1 = new double[] { random(), random() };
		double[] lambdas1 = new double[] { random(), random() };
		double[] adhesion1 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop1, criticals1);
		factory.popToLambdas.put(cellPop1, lambdas1);
		factory.popToAdhesion.put(cellPop1, adhesion1);
		
		int cellID2 = cellID1 + 1;
		int cellPop2 = cellPop1 + 1;
		int cellAge2 = (int)random();
		int cellState2 = (int)(Math.random()*5);
		int cellPhase2 = (int)random();
		double[] criticals2 = new double[] { random(), random() };
		double[] lambdas2 = new double[] { random(), random() };
		double[] adhesion2 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop2, criticals2);
		factory.popToLambdas.put(cellPop2, lambdas2);
		factory.popToAdhesion.put(cellPop2, adhesion2);
		
		CellContainer container1 = new CellContainer(cellID1, cellPop1, cellAge1, cellState1, cellPhase1,
				0, null, 0, 0, null, null);
		Cell cell1 = factory.make(container1, location1);
		
		assertTrue(cell1 instanceof PottsCell);
		assertEquals(location1, cell1.getLocation());
		assertEquals(cellID1, cell1.getID());
		assertEquals(cellPop1, cell1.getPop());
		assertEquals(cellAge1, cell1.getAge());
		assertEquals(cellState1, cell1.getState());
		verify(cell1.getModule()).setPhase(cellPhase1);
		assertEquals(criticals1[0], cell1.getCriticalVolume(), EPSILON);
		assertEquals(criticals1[1], cell1.getCriticalSurface(), EPSILON);
		assertEquals(lambdas1[0], cell1.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas1[1], cell1.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion1[0], cell1.getAdhesion(0), EPSILON);
		assertEquals(adhesion1[1], cell1.getAdhesion(1), EPSILON);
		assertEquals(adhesion1[2], cell1.getAdhesion(2), EPSILON);
		assertEquals(adhesion1[3], cell1.getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
		
		CellContainer container2 = new CellContainer(cellID2, cellPop2, cellAge2, cellState2, cellPhase2,
				0, null, 0, 0, null, null);
		Cell cell2 = factory.make(container2, location2);
		
		assertTrue(cell2 instanceof PottsCell);
		assertEquals(location2, cell2.getLocation());
		assertEquals(cellID2, cell2.getID());
		assertEquals(cellPop2, cell2.getPop());
		assertEquals(cellAge2, cell2.getAge());
		assertEquals(cellState2, cell2.getState());
		verify(cell2.getModule()).setPhase(cellPhase2);
		assertEquals(criticals2[0], cell2.getCriticalVolume(), EPSILON);
		assertEquals(criticals2[1], cell2.getCriticalSurface(), EPSILON);
		assertEquals(lambdas2[0], cell2.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas2[1], cell2.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion2[0], cell2.getAdhesion(0), EPSILON);
		assertEquals(adhesion2[1], cell2.getAdhesion(1), EPSILON);
		assertEquals(adhesion2[2], cell2.getAdhesion(2), EPSILON);
		assertEquals(adhesion2[3], cell2.getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void make_onePopulationWithTagsNoTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		int cellState = (int)(Math.random()*5);
		int cellPhase = (int)random();
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
		
		factory.popToCriticals.put(cellPop, criticals);
		factory.popToLambdas.put(cellPop, lambdas);
		factory.popToAdhesion.put(cellPop, adhesion);
		
		factory.popToTagCriticals.put(cellPop, criticalsTag);
		factory.popToTagLambdas.put(cellPop, lambdasTag);
		factory.popToTagAdhesion.put(cellPop, adhesionTag);
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("A");
		tags.add("B");
		tags.add("C");
		factory.popToTags.put(cellPop, tags);
		
		CellContainer container = new CellContainer(cellID, cellPop, cellAge, cellState, cellPhase,
				0, null, 0, 0, null, null);
		Cell cell = factory.make(container, location);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellAge, cell.getAge());
		assertEquals(cellState, cell.getState());
		verify(cell.getModule()).setPhase(cellPhase);
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(0, cell.getTargetVolume(), EPSILON);
		assertEquals(0, cell.getTargetSurface(), EPSILON);
		
		for (int i = 0; i < tags.size(); i++) {
			assertEquals(criticalsTag[0][i], cell.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			assertEquals(0, cell.getTargetVolume(-i - 1), EPSILON);
			assertEquals(0, cell.getTargetSurface(-i - 1), EPSILON);
			
			for (int j = 0; j < tags.size(); j++) {
				assertEquals(adhesionTag[i][j], cell.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
	
	@Test
	public void make_multiplePopulationsWithTagsNoTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID1 = (int)random() + 1;
		int cellPop1 = (int)random() + 1;
		int cellAge1 = (int)random();
		int cellState1 = (int)(Math.random()*5);
		int cellPhase1 = (int)random();
		double[] criticals1 = new double[] { random(), random() };
		double[] lambdas1 = new double[] { random(), random() };
		double[] adhesion1 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop1, criticals1);
		factory.popToLambdas.put(cellPop1, lambdas1);
		factory.popToAdhesion.put(cellPop1, adhesion1);
		
		int cellID2 = cellID1 + 1;
		int cellPop2 = cellPop1 + 1;
		int cellAge2 = (int)random();
		int cellState2 = (int)(Math.random()*5);
		int cellPhase2 = (int)random();
		double[] criticals2 = new double[] { random(), random() };
		double[] lambdas2 = new double[] { random(), random() };
		double[] adhesion2 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop2, criticals2);
		factory.popToLambdas.put(cellPop2, lambdas2);
		factory.popToAdhesion.put(cellPop2, adhesion2);
		
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
		
		factory.popToTagCriticals.put(cellPop2, criticalsTag);
		factory.popToTagLambdas.put(cellPop2, lambdasTag);
		factory.popToTagAdhesion.put(cellPop2, adhesionTag);
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("A");
		tags.add("B");
		tags.add("C");
		factory.popToTags.put(cellPop2, tags);
		
		CellContainer container1 = new CellContainer(cellID1, cellPop1, cellAge1, cellState1, cellPhase1,
				0, null, 0, 0, null, null);
		Cell cell1 = factory.make(container1, location1);
		
		assertTrue(cell1 instanceof PottsCell);
		assertEquals(location1, cell1.getLocation());
		assertEquals(cellID1, cell1.getID());
		assertEquals(cellPop1, cell1.getPop());
		assertEquals(cellAge1, cell1.getAge());
		assertEquals(cellState1, cell1.getState());
		verify(cell1.getModule()).setPhase(cellPhase1);
		assertEquals(criticals1[0], cell1.getCriticalVolume(), EPSILON);
		assertEquals(criticals1[1], cell1.getCriticalSurface(), EPSILON);
		assertEquals(lambdas1[0], cell1.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas1[1], cell1.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion1[0], cell1.getAdhesion(0), EPSILON);
		assertEquals(adhesion1[1], cell1.getAdhesion(1), EPSILON);
		assertEquals(adhesion1[2], cell1.getAdhesion(2), EPSILON);
		assertEquals(adhesion1[3], cell1.getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
		
		for (int i = 0; i < tags.size(); i++) {
			assertEquals(0, cell1.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(0, cell1.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(0, cell1.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(0, cell1.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			assertEquals(0, cell1.getTargetVolume(-i - 1), EPSILON);
			assertEquals(0, cell1.getTargetSurface(-i - 1), EPSILON);
			
			for (int j = 0; j < tags.size(); j++) {
				assertEquals(0, cell1.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
		
		CellContainer container2 = new CellContainer(cellID2, cellPop2, cellAge2, cellState2, cellPhase2,
				0, null, 0, 0, null, null);
		Cell cell2 = factory.make(container2, location2);
		
		assertTrue(cell2 instanceof PottsCell);
		assertEquals(location2, cell2.getLocation());
		assertEquals(cellID2, cell2.getID());
		assertEquals(cellPop2, cell2.getPop());
		assertEquals(cellAge2, cell2.getAge());
		assertEquals(cellState2, cell2.getState());
		verify(cell2.getModule()).setPhase(cellPhase2);
		assertEquals(criticals2[0], cell2.getCriticalVolume(), EPSILON);
		assertEquals(criticals2[1], cell2.getCriticalSurface(), EPSILON);
		assertEquals(lambdas2[0], cell2.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas2[1], cell2.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion2[0], cell2.getAdhesion(0), EPSILON);
		assertEquals(adhesion2[1], cell2.getAdhesion(1), EPSILON);
		assertEquals(adhesion2[2], cell2.getAdhesion(2), EPSILON);
		assertEquals(adhesion2[3], cell2.getAdhesion(3), EPSILON);
		assertEquals(0, cell2.getTargetVolume(), EPSILON);
		assertEquals(0, cell2.getTargetSurface(), EPSILON);
		
		for (int i = 0; i < tags.size(); i++) {
			assertEquals(criticalsTag[0][i], cell2.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell2.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell2.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell2.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			assertEquals(0, cell2.getTargetVolume(-i - 1), EPSILON);
			assertEquals(0, cell2.getTargetSurface(-i - 1), EPSILON);
			
			for (int j = 0; j < tags.size(); j++) {
				assertEquals(adhesionTag[i][j], cell2.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
	
	@Test
	public void make_onePopulationNoTagsWithTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume = random();
		double targetSurface = random();
		
		factory.popToCriticals.put(1, new double[2]);
		factory.popToLambdas.put(1, new double[2]);
		factory.popToAdhesion.put(1, new double[2]);
		
		CellContainer container = new CellContainer(1, 1, 0, 0, 0, 0, targetVolume, targetSurface);
		Cell cell = factory.make(container, location);
		
		verify(cell).setTargets(targetVolume, targetSurface);
	}
	
	@Test
	public void make_multiplePopulationsNoTagsWithTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume1 = random();
		double targetSurface1 = random();
		
		factory.popToCriticals.put(1, new double[2]);
		factory.popToLambdas.put(1, new double[2]);
		factory.popToAdhesion.put(1, new double[3]);
		
		double targetVolume2 = random();
		double targetSurface2 = random();
		
		factory.popToCriticals.put(2, new double[2]);
		factory.popToLambdas.put(2, new double[2]);
		factory.popToAdhesion.put(2, new double[3]);
		
		CellContainer container1 = new CellContainer(1, 1, 0, 0, 0, 0, targetVolume1, targetSurface1);
		Cell cell1 = factory.make(container1, location1);
		verify(cell1).setTargets(targetVolume1, targetSurface1);
		
		CellContainer container2 = new CellContainer(2, 2, 0, 0, 0, 0, targetVolume2, targetSurface2);
		Cell cell2 = factory.make(container2, location2);
		verify(cell2).setTargets(targetVolume2, targetSurface2);
	}
	
	@Test
	public void make_onePopulationWithTagsWithTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume = random();
		double targetSurface = random();
		
		double[] tagVolumes = new double[] { random(), random(), random() };
		double[] tagSurfaces = new double[] { random(), random(), random() };
		
		HashMap<String, Double> targetTagVolumes = new HashMap<>();
		HashMap<String, Double> targetTagSurfaces = new HashMap<>();
		
		factory.popToCriticals.put(1, new double[2]);
		factory.popToLambdas.put(1, new double[2]);
		factory.popToAdhesion.put(1, new double[3]);
		
		factory.popToTagCriticals.put(1, new double[2][3]);
		factory.popToTagLambdas.put(1, new double[2][3]);
		factory.popToTagAdhesion.put(1, new double[3][3]);
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("CYTOPLASM");
		tags.add("NUCLEUS");
		factory.popToTags.put(1, tags);
		
		int[] tagCodes = new int[] { TAG_CYTOPLASM, TAG_NUCLEUS };
		
		for (int i = 0; i < tags.size(); i++) {
			targetTagVolumes.put(tags.get(i), tagVolumes[i]);
			targetTagSurfaces.put(tags.get(i), tagSurfaces[i]);
		}
		
		CellContainer container = new CellContainer(1, 1, 0, 0, 0, 0, null,
				targetVolume, targetSurface, targetTagVolumes, targetTagSurfaces);
		Cell cell = factory.make(container, location);
		
		verify(cell).setTargets(targetVolume, targetSurface);
		for (int i = 0; i < tags.size(); i++) {
			verify(cell).setTargets(tagCodes[i], tagVolumes[i], tagSurfaces[i]);
		}
	}
	
	@Test
	public void make_multiplePopulationsWithTagsWithTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume1 = random();
		double targetSurface1 = random();
		
		factory.popToCriticals.put(1, new double[2]);
		factory.popToLambdas.put(1, new double[2]);
		factory.popToAdhesion.put(1, new double[3]);
		
		double targetVolume2 = random();
		double targetSurface2 = random();
		
		factory.popToCriticals.put(2, new double[2]);
		factory.popToLambdas.put(2, new double[2]);
		factory.popToAdhesion.put(2, new double[3]);
		
		double[] tagVolumes = new double[] { random(), random(), random() };
		double[] tagSurfaces = new double[] { random(), random(), random() };
		
		HashMap<String, Double> targetTagVolumes = new HashMap<>();
		HashMap<String, Double> targetTagSurfaces = new HashMap<>();
		
		factory.popToTagCriticals.put(2, new double[2][3]);
		factory.popToTagLambdas.put(2, new double[2][3]);
		factory.popToTagAdhesion.put(2, new double[3][3]);
		
		ArrayList<String> tags = new ArrayList<>();
		tags.add("CYTOPLASM");
		tags.add("NUCLEUS");
		factory.popToTags.put(2, tags);
		
		int[] tagCodes = new int[] { TAG_CYTOPLASM, TAG_NUCLEUS };
		
		for (int i = 0; i < tags.size(); i++) {
			targetTagVolumes.put(tags.get(i), tagVolumes[i]);
			targetTagSurfaces.put(tags.get(i), tagSurfaces[i]);
		}
		
		CellContainer container1 = new CellContainer(1, 1, 0, 0, 0, 0, targetVolume1, targetSurface1);
		Cell cell1 = factory.make(container1, location1);
		
		verify(cell1).setTargets(targetVolume1, targetSurface1);
		for (int i = 0; i < tags.size(); i++) {
			verify(cell1, never()).setTargets(tagCodes[i], tagVolumes[i], tagSurfaces[i]);
		}
		
		CellContainer container2 = new CellContainer(2, 2, 0, 0, 0, 0, null,
				targetVolume2, targetSurface2, targetTagVolumes, targetTagSurfaces);
		Cell cell2 = factory.make(container2, location2);
		
		verify(cell2).setTargets(targetVolume2, targetSurface2);
		for (int i = 0; i < tags.size(); i++) {
			verify(cell2).setTargets(tagCodes[i], tagVolumes[i], tagSurfaces[i]);
		}
	}
}
