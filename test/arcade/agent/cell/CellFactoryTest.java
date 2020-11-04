package arcade.agent.cell;

import org.junit.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
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
import static arcade.agent.cell.Cell.Tag;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.module.Module.Phase;

public class CellFactoryTest {
	static final double EPSILON = 1E-10;
	
	static double random() { return Math.random()*100; }
	
	public static Phase randomPhase() { return Phase.values()[(int)(Math.random()*Phase.values().length - 1) + 1]; }
	
	public static State randomState() { return State.values()[(int)(Math.random()*State.values().length - 1) + 1]; }
	
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
	
	static EnumMap<Term, Double> makeEnumMap() {
		EnumMap<Term, Double> map = new EnumMap<>(Term.class);
		for (Term term : Term.values()) { map.put(term, random()); }
		return map;
	}
	
	static EnumMap<Tag, EnumMap<Term, Double>> makeEnumMapTag(EnumSet<Tag> tagList) {
		EnumMap<Tag, EnumMap<Term, Double>> map = new EnumMap<>(Tag.class);
		for (Tag tag : tagList) {
			EnumMap<Term, Double> mapValues = makeEnumMap();
			map.put(tag, mapValues);
		}
		return map;
	}
	
	static EnumMap<Tag, EnumMap<Tag, Double>> makeEnumMapTarget(EnumSet<Tag> tagList) {
		EnumMap<Tag, EnumMap<Tag, Double>> map = new EnumMap<>(Tag.class);
		for (Tag tag : tagList) {
			EnumMap<Tag, Double> mapValues = new EnumMap<>(Tag.class);
			for (Tag target : tagList) { mapValues.put(target, random()); }
			map.put(tag, mapValues);
		}
		return map;
	}
	
	static class CellFactoryMock extends CellFactory {
		public CellFactoryMock() { super(); }
		
		Cell makeCell(int id, int pop, int age, State state, Location location,
					  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getAge()).thenReturn(age);
			when(cell.getState()).thenReturn(state);
			when(cell.getLocation()).thenReturn(location);
			
			Module module = mock(Module.class);
			when(cell.getModule()).thenReturn(module);
			
			when(cell.getCriticalVolume()).thenReturn(criticals.get(Term.VOLUME));
			when(cell.getCriticalSurface()).thenReturn(criticals.get(Term.SURFACE));
			
			when(cell.getLambda(Term.VOLUME)).thenReturn(lambdas.get(Term.VOLUME));
			when(cell.getLambda(Term.SURFACE)).thenReturn(lambdas.get(Term.SURFACE));
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			return cell;
		}
		
		Cell makeCell(int id, int pop, int age, State state, Location location,
					  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					  EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
					  EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getAge()).thenReturn(age);
			when(cell.getState()).thenReturn(state);
			when(cell.getLocation()).thenReturn(location);
			
			Module module = mock(Module.class);
			when(cell.getModule()).thenReturn(module);
			
			when(cell.getCriticalVolume()).thenReturn(criticals.get(Term.VOLUME));
			when(cell.getCriticalSurface()).thenReturn(criticals.get(Term.SURFACE));
			
			when(cell.getLambda(Term.VOLUME)).thenReturn(lambdas.get(Term.VOLUME));
			when(cell.getLambda(Term.SURFACE)).thenReturn(lambdas.get(Term.SURFACE));
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			for (Tag tag : location.getTags()) {
				when(cell.getCriticalVolume(tag)).thenReturn(criticalsTag.get(tag).get(Term.VOLUME));
				when(cell.getCriticalSurface(tag)).thenReturn(criticalsTag.get(tag).get(Term.SURFACE));
				
				when(cell.getLambda(Term.VOLUME, tag)).thenReturn(lambdasTag.get(tag).get(Term.VOLUME));
				when(cell.getLambda(Term.SURFACE, tag)).thenReturn(lambdasTag.get(tag).get(Term.SURFACE));
				
				for (Tag target : location.getTags()) {
					when(cell.getAdhesion(tag, target)).thenReturn(adhesionTag.get(tag).get(target));
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
		
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
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
			population.put("LAMBDA_VOLUME", lambdas.get(Term.VOLUME) + pop);
			population.put("LAMBDA_SURFACE", lambdas.get(Term.SURFACE) + pop);
			population.put("CRITICAL_VOLUME", criticals.get(Term.VOLUME) + pop);
			population.put("CRITICAL_SURFACE", criticals.get(Term.SURFACE) + pop);
			
			series._populations.put(popKeys[i], population);
		}
		
		CellFactory factory = new CellFactoryMock();
		factory.parseValues(series);
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			assertEquals(criticals.get(Term.VOLUME) + pop, factory.popToCriticals.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(criticals.get(Term.SURFACE) + pop, factory.popToCriticals.get(pop).get(Term.SURFACE), EPSILON);
			assertEquals(lambdas.get(Term.VOLUME) + pop, factory.popToLambdas.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(lambdas.get(Term.SURFACE) + pop, factory.popToLambdas.get(pop).get(Term.SURFACE), EPSILON);
			assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
			assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
			assertFalse(factory.popToTags.get(pop));
		}
	}
	
	@Test
	public void parseValues_withTags_updatesLists() {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] { random(), random(), random(), random() };
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.DEFAULT, Tag.NUCLEUS, Tag.UNDEFINED);
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		String[] popKeys = new String[] { "A", "B", "C" };
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			MiniBox population = new MiniBox();
			
			population.put("CODE", pop);
			population.put("ADHESION:*", adhesion[0]);
			population.put("ADHESION:A", adhesion[1]);
			population.put("ADHESION:B", adhesion[2]);
			population.put("ADHESION:C", adhesion[3]);
			population.put("LAMBDA_VOLUME", lambdas.get(Term.VOLUME) + pop);
			population.put("LAMBDA_SURFACE", lambdas.get(Term.SURFACE) + pop);
			population.put("CRITICAL_VOLUME", criticals.get(Term.VOLUME) + pop);
			population.put("CRITICAL_SURFACE", criticals.get(Term.SURFACE) + pop);
			
			for (Tag tag : tagList) {
				population.put("TAG" + TAG_SEPARATOR + tag, 0);
				population.put(tag + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasTag.get(tag).get(Term.VOLUME) + pop);
				population.put(tag + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasTag.get(tag).get(Term.SURFACE) + pop);
				population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsTag.get(tag).get(Term.VOLUME) + pop);
				population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsTag.get(tag).get(Term.SURFACE) + pop);
				
				for (Tag target : tagList) {
					population.put(tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target, adhesionTag.get(tag).get(target) + pop);
				}
			}
			
			series._populations.put(popKeys[i], population);
		}
		
		CellFactory factory = new CellFactoryMock();
		factory.parseValues(series);
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			assertEquals(criticals.get(Term.VOLUME) + pop, factory.popToCriticals.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(criticals.get(Term.SURFACE) + pop, factory.popToCriticals.get(pop).get(Term.SURFACE), EPSILON);
			assertEquals(lambdas.get(Term.VOLUME) + pop, factory.popToLambdas.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(lambdas.get(Term.SURFACE) + pop, factory.popToLambdas.get(pop).get(Term.SURFACE), EPSILON);
			assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
			assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
			assertTrue(factory.popToTags.get(pop));
			
			for (Tag tag : tagList) {
				assertEquals(criticalsTag.get(tag).get(Term.VOLUME) + pop, factory.popToTagCriticals.get(pop).get(tag).get(Term.VOLUME), EPSILON);
				assertEquals(criticalsTag.get(tag).get(Term.SURFACE) + pop, factory.popToTagCriticals.get(pop).get(tag).get(Term.SURFACE), EPSILON);
				assertEquals(lambdasTag.get(tag).get(Term.VOLUME) + pop, factory.popToTagLambdas.get(pop).get(tag).get(Term.VOLUME), EPSILON);
				assertEquals(lambdasTag.get(tag).get(Term.SURFACE) + pop, factory.popToTagLambdas.get(pop).get(tag).get(Term.SURFACE), EPSILON);
				
				for (Tag target : tagList) {
					assertEquals(adhesionTag.get(tag).get(target) + pop, factory.popToTagAdhesion.get(pop).get(tag).get(target), EPSILON);
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
		factory.popToTags.put(1, false);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) { assertEquals(voxels, factory.cells.get(i).voxels); }
	}
	
	@Test
	public void createCells_onePopulationWithTags_createsList() {
		int voxelsA = 10*randomInt();
		int voxelsB = 10*randomInt();
		
		int voxels = voxelsA + voxelsB;
		int init = randomInt();
		
		Series series = createSeries(new int[] { init }, new double[] { voxels*DS });
		
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "UNDEFINED", (double)voxelsA/voxels);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "NUCLEUS", (double)voxelsB/voxels);
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToTags.put(1, true);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			assertEquals(voxels, factory.cells.get(i).voxels);
			assertEquals(voxelsA, (int)factory.cells.get(i).tagVoxels.get(Tag.UNDEFINED));
			assertEquals(voxelsB, (int)factory.cells.get(i).tagVoxels.get(Tag.NUCLEUS));
			assertEquals(0, (int)factory.cells.get(i).tagVoxels.get(Tag.DEFAULT));
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
		factory.popToTags.put(1, false);
		factory.popToTags.put(2, false);
		factory.popToTags.put(3, false);
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
		
		int voxels1 = randomInt();
		int voxels2 = voxelsA + voxelsB;
		int voxels3 = randomInt();
		
		int init1 = randomInt();
		int init2 = randomInt();
		int init3 = randomInt();
		
		Series series = createSeries(new int[] { init1, init2, init3 },
				new double[] { voxels1*DS, voxels2*DS, voxels3*DS });
		
		series._populations.get("pop2").put("TAG" + TAG_SEPARATOR + "UNDEFINED", (double)voxelsA/voxels2);
		series._populations.get("pop2").put("TAG" + TAG_SEPARATOR + "NUCLEUS", (double)voxelsB/voxels2);
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		factory.popToIDs.put(3, new HashSet<>());
		factory.popToTags.put(1, false);
		factory.popToTags.put(2, true);
		factory.popToTags.put(3, false);
		factory.createCells(series);
		
		assertEquals(init1 + init2 + init3, factory.cells.size());
		assertEquals(init1, factory.popToIDs.get(1).size());
		assertEquals(init2, factory.popToIDs.get(2).size());
		assertEquals(init3, factory.popToIDs.get(3).size());
		
		for (int i : factory.popToIDs.get(1)) { assertEquals(voxels1, factory.cells.get(i).voxels); }
		for (int i : factory.popToIDs.get(2)) {
			assertEquals(voxels2, factory.cells.get(i).voxels);
			assertEquals(voxelsA, (int)factory.cells.get(i).tagVoxels.get(Tag.UNDEFINED));
			assertEquals(voxelsB, (int)factory.cells.get(i).tagVoxels.get(Tag.NUCLEUS));
			assertEquals(0, (int)factory.cells.get(i).tagVoxels.get(Tag.DEFAULT));
		}
		for (int i : factory.popToIDs.get(3)) { assertEquals(voxels3, factory.cells.get(i).voxels); }
	}
	
	@Test
	public void createCells_extraTags_skipsExtra() {
		int voxel = randomInt();
		int voxels = 4*voxel;
		int init = randomInt();
		
		Series series = createSeries(new int[] { init }, new double[] { voxels*DS });
		
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "UNDEFINED", 0.75);
		series._populations.get("pop1").put("TAG" + TAG_SEPARATOR + "DEFAULT", 0.75);
		
		CellFactoryMock factory = new CellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToTags.put(1, true);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			assertEquals(voxels, factory.cells.get(i).voxels);
			assertEquals(3*voxel, (int)factory.cells.get(i).tagVoxels.get(Tag.UNDEFINED));
			assertEquals(voxel, (int)factory.cells.get(i).tagVoxels.get(Tag.DEFAULT));
		}
	}
	
	@Test
	public void make_onePopulationNoTagsNoTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		State cellState = randomState();
		Phase cellPhase = randomPhase();
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] { random(), random() };
		
		factory.popToCriticals.put(cellPop, criticals);
		factory.popToLambdas.put(cellPop, lambdas);
		factory.popToAdhesion.put(cellPop, adhesion);
		factory.popToTags.put(cellPop, false);
		
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
		assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas.get(Term.VOLUME), cell.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas.get(Term.SURFACE), cell.getLambda(Term.SURFACE), EPSILON);
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
		State cellState1 = randomState();
		Phase cellPhase1 = randomPhase();
		EnumMap<Term, Double> criticals1 = makeEnumMap();
		EnumMap<Term, Double> lambdas1 = makeEnumMap();
		double[] adhesion1 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop1, criticals1);
		factory.popToLambdas.put(cellPop1, lambdas1);
		factory.popToAdhesion.put(cellPop1, adhesion1);
		factory.popToTags.put(cellPop1, false);
		
		int cellID2 = cellID1 + 1;
		int cellPop2 = cellPop1 + 1;
		int cellAge2 = (int)random();
		State cellState2 = randomState();
		Phase cellPhase2 = randomPhase();
		EnumMap<Term, Double> criticals2 = makeEnumMap();
		EnumMap<Term, Double> lambdas2 = makeEnumMap();
		double[] adhesion2 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop2, criticals2);
		factory.popToLambdas.put(cellPop2, lambdas2);
		factory.popToAdhesion.put(cellPop2, adhesion2);
		factory.popToTags.put(cellPop2, false);
		
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
		assertEquals(criticals1.get(Term.VOLUME), cell1.getCriticalVolume(), EPSILON);
		assertEquals(criticals1.get(Term.SURFACE), cell1.getCriticalSurface(), EPSILON);
		assertEquals(lambdas1.get(Term.VOLUME), cell1.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas1.get(Term.SURFACE), cell1.getLambda(Term.SURFACE), EPSILON);
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
		assertEquals(criticals2.get(Term.VOLUME), cell2.getCriticalVolume(), EPSILON);
		assertEquals(criticals2.get(Term.SURFACE), cell2.getCriticalSurface(), EPSILON);
		assertEquals(lambdas2.get(Term.VOLUME), cell2.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas2.get(Term.SURFACE), cell2.getLambda(Term.SURFACE), EPSILON);
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
		State cellState = randomState();
		Phase cellPhase = randomPhase();
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] { random(), random() };
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.NUCLEUS, Tag.UNDEFINED);
		doReturn(tagList).when(location).getTags();
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		factory.popToCriticals.put(cellPop, criticals);
		factory.popToLambdas.put(cellPop, lambdas);
		factory.popToAdhesion.put(cellPop, adhesion);
		
		factory.popToTagCriticals.put(cellPop, criticalsTag);
		factory.popToTagLambdas.put(cellPop, lambdasTag);
		factory.popToTagAdhesion.put(cellPop, adhesionTag);
		
		factory.popToTags.put(cellPop, true);
		
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
		assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas.get(Term.VOLUME), cell.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas.get(Term.SURFACE), cell.getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(0, cell.getTargetVolume(), EPSILON);
		assertEquals(0, cell.getTargetSurface(), EPSILON);
		
		for (Tag tag : tagList) {
			assertEquals(criticalsTag.get(tag).get(Term.VOLUME), cell.getCriticalVolume(tag), EPSILON);
			assertEquals(criticalsTag.get(tag).get(Term.SURFACE), cell.getCriticalSurface(tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.VOLUME), cell.getLambda(Term.VOLUME, tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.SURFACE), cell.getLambda(Term.SURFACE, tag), EPSILON);
			assertEquals(0, cell.getTargetVolume(tag), EPSILON);
			assertEquals(0, cell.getTargetSurface(tag), EPSILON);
			
			for (Tag target : tagList) {
				assertEquals(adhesionTag.get(tag).get(target), cell.getAdhesion(tag, target), EPSILON);
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
		State cellState1 = randomState();
		Phase cellPhase1 = randomPhase();
		EnumMap<Term, Double> criticals1 = makeEnumMap();
		EnumMap<Term, Double> lambdas1 = makeEnumMap();
		double[] adhesion1 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop1, criticals1);
		factory.popToLambdas.put(cellPop1, lambdas1);
		factory.popToAdhesion.put(cellPop1, adhesion1);
		factory.popToTags.put(cellPop1, false);
		
		int cellID2 = cellID1 + 1;
		int cellPop2 = cellPop1 + 1;
		int cellAge2 = (int)random();
		State cellState2 = randomState();
		Phase cellPhase2 = randomPhase();
		EnumMap<Term, Double> criticals2 = makeEnumMap();
		EnumMap<Term, Double> lambdas2 = makeEnumMap();
		double[] adhesion2 = new double[] { random(), random(), random(), random() };
		
		factory.popToCriticals.put(cellPop2, criticals2);
		factory.popToLambdas.put(cellPop2, lambdas2);
		factory.popToAdhesion.put(cellPop2, adhesion2);
		factory.popToTags.put(cellPop2, true);
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.NUCLEUS, Tag.UNDEFINED);
		doReturn(tagList).when(location2).getTags();
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		factory.popToTagCriticals.put(cellPop2, criticalsTag);
		factory.popToTagLambdas.put(cellPop2, lambdasTag);
		factory.popToTagAdhesion.put(cellPop2, adhesionTag);
		
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
		assertEquals(criticals1.get(Term.VOLUME), cell1.getCriticalVolume(), EPSILON);
		assertEquals(criticals1.get(Term.SURFACE), cell1.getCriticalSurface(), EPSILON);
		assertEquals(lambdas1.get(Term.VOLUME), cell1.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas1.get(Term.SURFACE), cell1.getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion1[0], cell1.getAdhesion(0), EPSILON);
		assertEquals(adhesion1[1], cell1.getAdhesion(1), EPSILON);
		assertEquals(adhesion1[2], cell1.getAdhesion(2), EPSILON);
		assertEquals(adhesion1[3], cell1.getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
		
		for (Tag tag : tagList) {
			assertEquals(0, cell1.getCriticalVolume(tag), EPSILON);
			assertEquals(0, cell1.getCriticalSurface(tag), EPSILON);
			assertEquals(0, cell1.getLambda(Term.VOLUME, tag), EPSILON);
			assertEquals(0, cell1.getLambda(Term.SURFACE, tag), EPSILON);
			assertEquals(0, cell1.getTargetVolume(tag), EPSILON);
			assertEquals(0, cell1.getTargetSurface(tag), EPSILON);
			
			for (Tag target : tagList) {
				assertEquals(0, cell1.getAdhesion(tag, target), EPSILON);
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
		assertEquals(criticals2.get(Term.VOLUME), cell2.getCriticalVolume(), EPSILON);
		assertEquals(criticals2.get(Term.SURFACE), cell2.getCriticalSurface(), EPSILON);
		assertEquals(lambdas2.get(Term.VOLUME), cell2.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas2.get(Term.SURFACE), cell2.getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion2[0], cell2.getAdhesion(0), EPSILON);
		assertEquals(adhesion2[1], cell2.getAdhesion(1), EPSILON);
		assertEquals(adhesion2[2], cell2.getAdhesion(2), EPSILON);
		assertEquals(adhesion2[3], cell2.getAdhesion(3), EPSILON);
		assertEquals(0, cell2.getTargetVolume(), EPSILON);
		assertEquals(0, cell2.getTargetSurface(), EPSILON);
		
		for (Tag tag : tagList) {
			assertEquals(criticalsTag.get(tag).get(Term.VOLUME), cell2.getCriticalVolume(tag), EPSILON);
			assertEquals(criticalsTag.get(tag).get(Term.SURFACE), cell2.getCriticalSurface(tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.VOLUME), cell2.getLambda(Term.VOLUME, tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.SURFACE), cell2.getLambda(Term.SURFACE, tag), EPSILON);
			assertEquals(0, cell2.getTargetVolume(tag), EPSILON);
			assertEquals(0, cell2.getTargetSurface(tag), EPSILON);
			
			for (Tag target : tagList) {
				assertEquals(adhesionTag.get(tag).get(target), cell2.getAdhesion(tag, target), EPSILON);
			}
		}
	}
	
	@Test
	public void make_onePopulationNoTagsWithTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume = random();
		double targetSurface = random();
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[2]);
		factory.popToTags.put(1, false);
		
		CellContainer container = new CellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume, targetSurface);
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
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[3]);
		factory.popToTags.put(1, false);
		
		double targetVolume2 = random();
		double targetSurface2 = random();
		
		factory.popToCriticals.put(2, makeEnumMap());
		factory.popToLambdas.put(2, makeEnumMap());
		factory.popToAdhesion.put(2, new double[3]);
		factory.popToTags.put(2, false);
		
		CellContainer container1 = new CellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume1, targetSurface1);
		Cell cell1 = factory.make(container1, location1);
		verify(cell1).setTargets(targetVolume1, targetSurface1);
		
		CellContainer container2 = new CellContainer(2, 2, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume2, targetSurface2);
		Cell cell2 = factory.make(container2, location2);
		verify(cell2).setTargets(targetVolume2, targetSurface2);
	}
	
	@Test
	public void make_onePopulationWithTagsWithTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume = random();
		double targetSurface = random();
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[2]);
		factory.popToTags.put(1, true);
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.NUCLEUS, Tag.UNDEFINED);
		doReturn(tagList).when(location).getTags();
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		factory.popToTagCriticals.put(1, criticalsTag);
		factory.popToTagLambdas.put(1, lambdasTag);
		factory.popToTagAdhesion.put(1, adhesionTag);
		
		EnumMap<Tag, Double> targetTagVolumes = new EnumMap<>(Tag.class);
		EnumMap<Tag, Double> targetTagSurfaces = new EnumMap<>(Tag.class);
		
		for (Tag tag : tagList) {
			targetTagVolumes.put(tag, random());
			targetTagSurfaces.put(tag, random());
		}
		
		CellContainer container = new CellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume, targetSurface, targetTagVolumes, targetTagSurfaces);
		Cell cell = factory.make(container, location);
		
		verify(cell).setTargets(targetVolume, targetSurface);
		for (Tag tag : tagList) {
			verify(cell).setTargets(tag, targetTagVolumes.get(tag), targetTagSurfaces.get(tag));
		}
	}
	
	@Test
	public void make_multiplePopulationsWithTagsWithTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume1 = random();
		double targetSurface1 = random();
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[3]);
		factory.popToTags.put(1, false);
		
		double targetVolume2 = random();
		double targetSurface2 = random();
		
		factory.popToCriticals.put(2, makeEnumMap());
		factory.popToLambdas.put(2, makeEnumMap());
		factory.popToAdhesion.put(2, new double[3]);
		factory.popToTags.put(2, true);
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.NUCLEUS, Tag.UNDEFINED);
		doReturn(tagList).when(location2).getTags();
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		factory.popToTagCriticals.put(2, criticalsTag);
		factory.popToTagLambdas.put(2, lambdasTag);
		factory.popToTagAdhesion.put(2, adhesionTag);
		
		EnumMap<Tag, Double> targetTagVolumes = new EnumMap<>(Tag.class);
		EnumMap<Tag, Double> targetTagSurfaces = new EnumMap<>(Tag.class);
		
		for (Tag tag : tagList) {
			targetTagVolumes.put(tag, random());
			targetTagSurfaces.put(tag, random());
		}
		
		CellContainer container1 = new CellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume1, targetSurface1);
		Cell cell1 = factory.make(container1, location1);
		
		verify(cell1).setTargets(targetVolume1, targetSurface1);
		for (Tag tag : tagList) {
			verify(cell1, never()).setTargets(tag, targetTagVolumes.get(tag), targetTagSurfaces.get(tag));
		}
		
		CellContainer container2 = new CellContainer(2, 2, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume2, targetSurface2, targetTagVolumes, targetTagSurfaces);
		Cell cell2 = factory.make(container2, location2);
		
		verify(cell2).setTargets(targetVolume2, targetSurface2);
		for (Tag tag : tagList) {
			verify(cell2).setTargets(tag, targetTagVolumes.get(tag), targetTagSurfaces.get(tag));
		}
	}
	
	@Test
	public void make_onePopulationWithTagsMixedTarget_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		double targetVolume = random();
		double targetSurface = random();
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[2]);
		factory.popToTags.put(1, true);
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.NUCLEUS, Tag.UNDEFINED);
		doReturn(tagList).when(location).getTags();
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		factory.popToTagCriticals.put(1, criticalsTag);
		factory.popToTagLambdas.put(1, lambdasTag);
		factory.popToTagAdhesion.put(1, adhesionTag);
		
		EnumMap<Tag, Double> targetTagVolumes = new EnumMap<>(Tag.class);
		EnumMap<Tag, Double> targetTagSurfaces = new EnumMap<>(Tag.class);
		
		for (Tag tag : tagList) {
			targetTagVolumes.put(tag, random());
			targetTagSurfaces.put(tag, random());
		}
		
		CellContainer container1 = new CellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume, targetSurface, targetTagVolumes, null);
		Cell cell1 = factory.make(container1, location);
		
		verify(cell1).setTargets(targetVolume, targetSurface);
		for (Tag tag : tagList) {
			verify(cell1, never()).setTargets(tag, targetTagVolumes.get(tag), targetTagSurfaces.get(tag));
		}
		
		CellContainer container2 = new CellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume, targetSurface, null, targetTagSurfaces);
		Cell cell2 = factory.make(container2, location);
		
		verify(cell2).setTargets(targetVolume, targetSurface);
		for (Tag tag : tagList) {
			verify(cell2, never()).setTargets(tag, targetTagVolumes.get(tag), targetTagSurfaces.get(tag));
		}
	}
}
