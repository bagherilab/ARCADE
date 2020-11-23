package arcade.potts.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import arcade.core.sim.Series;
import arcade.core.sim.output.OutputLoader;
import arcade.core.agent.cell.*;
import arcade.core.agent.module.Module;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.core.sim.Series.TARGET_SEPARATOR;
import static arcade.core.agent.cell.Cell.Region;
import static arcade.core.agent.cell.Cell.State;
import static arcade.core.agent.cell.CellFactory.CellFactoryContainer;
import static arcade.potts.agent.module.PottsModule.Phase;
import static arcade.potts.agent.cell.PottsCellFactory.PottsCellContainer;
import static arcade.potts.sim.Potts.Term;
import static arcade.core.TestUtilities.*;
import static arcade.potts.PottsTestUtilities.*;

public class PottsCellFactoryTest {
	static Series createSeries(int[] init, int[] volumes) {
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
		for (Term term : Term.values()) { map.put(term, randomDoubleBetween(0, 100)); }
		return map;
	}
	
	static EnumMap<Region, EnumMap<Term, Double>> makeEnumMapRegion(EnumSet<Region> regionList) {
		EnumMap<Region, EnumMap<Term, Double>> map = new EnumMap<>(Region.class);
		for (Region region : regionList) {
			EnumMap<Term, Double> mapValues = makeEnumMap();
			map.put(region, mapValues);
		}
		return map;
	}
	
	static EnumMap<Region, EnumMap<Region, Double>> makeEnumMapTarget(EnumSet<Region> regionList) {
		EnumMap<Region, EnumMap<Region, Double>> map = new EnumMap<>(Region.class);
		for (Region region : regionList) {
			EnumMap<Region, Double> mapValues = new EnumMap<>(Region.class);
			for (Region target : regionList) { mapValues.put(target, randomDoubleBetween(0, 100)); }
			map.put(region, mapValues);
		}
		return map;
	}
	
	static class PottsCellFactoryMock extends PottsCellFactory {
		public PottsCellFactoryMock() { super(); }
		
		PottsCell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
					  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getAge()).thenReturn(age);
			when(cell.getState()).thenReturn(state);
			when(cell.getLocation()).thenReturn(location);
			when(cell.getParameters()).thenReturn(parameters);
			
			Module module = mock(PottsModule.class);
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
		
		PottsCell makeCell(int id, int pop, int age, State state, Location location, MiniBox parameters,
					  EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
					  EnumMap<Region, EnumMap<Term, Double>> criticalsRegion, EnumMap<Region, EnumMap<Term, Double>> lambdasRegion,
					  EnumMap<Region, EnumMap<Region, Double>> adhesionRegion) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getAge()).thenReturn(age);
			when(cell.getState()).thenReturn(state);
			when(cell.getLocation()).thenReturn(location);
			when(cell.getParameters()).thenReturn(parameters);
			
			Module module = mock(PottsModule.class);
			when(cell.getModule()).thenReturn(module);
			
			when(cell.getCriticalVolume()).thenReturn(criticals.get(Term.VOLUME));
			when(cell.getCriticalSurface()).thenReturn(criticals.get(Term.SURFACE));
			
			when(cell.getLambda(Term.VOLUME)).thenReturn(lambdas.get(Term.VOLUME));
			when(cell.getLambda(Term.SURFACE)).thenReturn(lambdas.get(Term.SURFACE));
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			for (Region region : location.getRegions()) {
				when(cell.getCriticalVolume(region)).thenReturn(criticalsRegion.get(region).get(Term.VOLUME));
				when(cell.getCriticalSurface(region)).thenReturn(criticalsRegion.get(region).get(Term.SURFACE));
				
				when(cell.getLambda(Term.VOLUME, region)).thenReturn(lambdasRegion.get(region).get(Term.VOLUME));
				when(cell.getLambda(Term.SURFACE, region)).thenReturn(lambdasRegion.get(region).get(Term.SURFACE));
				
				for (Region target : location.getRegions()) {
					when(cell.getAdhesion(region, target)).thenReturn(adhesionRegion.get(region).get(target));
				}
			}
			
			return cell;
		}
	}
	
	@Test
	public void initialize_noLoading_callsMethod() {
		PottsCellFactory factory = spy(new PottsCellFactoryMock());
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
		PottsCellFactory factory = spy(new PottsCellFactoryMock());
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
		PottsCellFactory factory = spy(new PottsCellFactoryMock());
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
	public void parseValues_noRegions_updatesLists() {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] {
				randomDoubleBetween(0, 100),
				randomDoubleBetween(0, 100),
				randomDoubleBetween(0, 100),
				randomDoubleBetween(0, 100)
		};
		
		String[] popKeys = new String[] { "A", "B", "C" };
		MiniBox[] popParameters = new MiniBox[3];
		
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
			popParameters[i] = population;
		}
		
		PottsCellFactory factory = new PottsCellFactoryMock();
		factory.parseValues(series);
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			assertEquals(criticals.get(Term.VOLUME) + pop, factory.popToCriticals.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(criticals.get(Term.SURFACE) + pop, factory.popToCriticals.get(pop).get(Term.SURFACE), EPSILON);
			assertEquals(lambdas.get(Term.VOLUME) + pop, factory.popToLambdas.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(lambdas.get(Term.SURFACE) + pop, factory.popToLambdas.get(pop).get(Term.SURFACE), EPSILON);
			assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
			assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
			assertEquals(popParameters[i], factory.popToParameters.get(pop));
			assertFalse(factory.popToRegions.get(pop));
		}
	}
	
	@Test
	public void parseValues_withRegions_updatesLists() {
		Series series = mock(Series.class);
		series._populations = new HashMap<>();
		
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] {
				randomDoubleBetween(0, 100),
				randomDoubleBetween(0, 100),
				randomDoubleBetween(0, 100),
				randomDoubleBetween(0, 100)
		};
		
		EnumSet<Region> regionList = EnumSet.of(Region.DEFAULT, Region.NUCLEUS, Region.UNDEFINED);
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		String[] popKeys = new String[] { "A", "B", "C" };
		MiniBox[] popParameters = new MiniBox[3];
		
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
			
			for (Region region : regionList) {
				population.put("(REGION)" + TAG_SEPARATOR + region, 0);
				population.put(region + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasRegion.get(region).get(Term.VOLUME) + pop);
				population.put(region + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasRegion.get(region).get(Term.SURFACE) + pop);
				population.put(region + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsRegion.get(region).get(Term.VOLUME) + pop);
				population.put(region + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsRegion.get(region).get(Term.SURFACE) + pop);
				
				for (Region target : regionList) {
					population.put(region + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + target, adhesionRegion.get(region).get(target) + pop);
				}
			}
			
			series._populations.put(popKeys[i], population);
			popParameters[i] = population;
		}
		
		PottsCellFactory factory = new PottsCellFactoryMock();
		factory.parseValues(series);
		
		for (int i = 0; i < popKeys.length; i++) {
			int pop = i + 1;
			assertEquals(criticals.get(Term.VOLUME) + pop, factory.popToCriticals.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(criticals.get(Term.SURFACE) + pop, factory.popToCriticals.get(pop).get(Term.SURFACE), EPSILON);
			assertEquals(lambdas.get(Term.VOLUME) + pop, factory.popToLambdas.get(pop).get(Term.VOLUME), EPSILON);
			assertEquals(lambdas.get(Term.SURFACE) + pop, factory.popToLambdas.get(pop).get(Term.SURFACE), EPSILON);
			assertArrayEquals(adhesion, factory.popToAdhesion.get(pop), EPSILON);
			assertEquals(new HashSet<>(), factory.popToIDs.get(pop));
			assertEquals(popParameters[i], factory.popToParameters.get(pop));
			assertTrue(factory.popToRegions.get(pop));
			
			for (Region region : regionList) {
				assertEquals(criticalsRegion.get(region).get(Term.VOLUME) + pop, factory.popToRegionCriticals.get(pop).get(region).get(Term.VOLUME), EPSILON);
				assertEquals(criticalsRegion.get(region).get(Term.SURFACE) + pop, factory.popToRegionCriticals.get(pop).get(region).get(Term.SURFACE), EPSILON);
				assertEquals(lambdasRegion.get(region).get(Term.VOLUME) + pop, factory.popToRegionLambdas.get(pop).get(region).get(Term.VOLUME), EPSILON);
				assertEquals(lambdasRegion.get(region).get(Term.SURFACE) + pop, factory.popToRegionLambdas.get(pop).get(region).get(Term.SURFACE), EPSILON);
				
				for (Region target : regionList) {
					assertEquals(adhesionRegion.get(region).get(target) + pop, factory.popToRegionAdhesion.get(pop).get(region).get(target), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void loadCells_givenLoadedValidPops_updatesLists() {
		int N = randomIntBetween(1, 10);
		int M = randomIntBetween(1, 10);
		ArrayList<PottsCellContainer> containers = new ArrayList<>();
		
		for (int i = 0; i < N; i++) {
			PottsCellContainer container = new PottsCellContainer(i, 1, randomIntBetween(1,10));
			containers.add(container);
		}
		
		for (int i = N; i < N + M; i++) {
			PottsCellContainer container = new PottsCellContainer(i, 2, randomIntBetween(1,10));
			containers.add(container);
		}
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
		
		CellFactoryContainer container = new CellFactoryContainer();
		for (int i = 0; i < N + M; i++) { container.cells.add(containers.get(i)); }
		doReturn(container).when(series.loader).loadCells();
		
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
		int N = randomIntBetween(1,10);
		ArrayList<PottsCellContainer> containers = new ArrayList<>();
		
		for (int i = 0; i < N; i++) {
			PottsCellContainer container = new PottsCellContainer(i, 1, randomIntBetween(1,10));
			containers.add(container);
		}
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		Series series = mock(Series.class);
		series.loader = mock(OutputLoader.class);
		
		series._populations = new HashMap<>();
		
		MiniBox pop1 = new MiniBox();
		pop1.put("CODE", 1);
		pop1.put("INIT", N);
		series._populations.put("A", pop1);
		
		CellFactoryContainer container = new CellFactoryContainer();
		for (int i = 0; i < N; i++) { container.cells.add(containers.get(i)); }
		doReturn(container).when(series.loader).loadCells();
		
		factory.loadCells(series);
		assertEquals(0, factory.cells.size());
		assertFalse(factory.popToIDs.containsKey(1));
	}
	
	@Test
	public void loadCells_givenLoadedLimitedInit_updatesLists() {
		int n = randomIntBetween(1,10);
		int N = n + randomIntBetween(1,10);
		int M = randomIntBetween(1,10);
		ArrayList<PottsCellContainer> containers = new ArrayList<>();
		
		for (int i = 0; i < N; i++) {
			PottsCellContainer container = new PottsCellContainer(i, 1, randomIntBetween(1,10));
			containers.add(container);
		}
		
		for (int i = N; i < N + M; i++) {
			PottsCellContainer container = new PottsCellContainer(i, 2, randomIntBetween(1,10));
			containers.add(container);
		}
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
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
		
		CellFactoryContainer container = new CellFactoryContainer();
		for (int i = 0; i < N + M; i++) { container.cells.add(containers.get(i)); }
		doReturn(container).when(series.loader).loadCells();
		
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
		Series series = createSeries(new int[] { }, new int[] { });
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		factory.createCells(series);
		
		assertEquals(0, factory.cells.size());
		assertEquals(0, factory.popToIDs.size());
	}
	
	@Test
	public void createCells_onePopulationNoRegions_createsList() {
		int voxels = randomIntBetween(1,10);
		int init = randomIntBetween(1,10);
		Series series = createSeries(new int[] { init }, new int[] { voxels });
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToRegions.put(1, false);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels, pottsCellContainer.voxels);
		}
	}
	
	@Test
	public void createCells_onePopulationWithRegions_createsList() {
		int voxelsA = 10*randomIntBetween(1,10);
		int voxelsB = 10*randomIntBetween(1,10);
		
		int voxels = voxelsA + voxelsB;
		int init = randomIntBetween(1,10);
		
		Series series = createSeries(new int[] { init }, new int[] { voxels });
		
		series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "UNDEFINED", (double)voxelsA/voxels);
		series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", (double)voxelsB/voxels);
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToRegions.put(1, true);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels, pottsCellContainer.voxels);
			assertEquals(voxelsA, (int)pottsCellContainer.regionVoxels.get(Region.UNDEFINED));
			assertEquals(voxelsB, (int)pottsCellContainer.regionVoxels.get(Region.NUCLEUS));
			assertEquals(0, (int)pottsCellContainer.regionVoxels.get(Region.DEFAULT));
		}
	}
	
	@Test
	public void createCells_multiplePopulationsNoRegions_createsList() {
		int voxels1 = randomIntBetween(1,10);
		int voxels2 = randomIntBetween(1,10);
		int voxels3 = randomIntBetween(1,10);
		
		int init1 = randomIntBetween(1,10);
		int init2 = randomIntBetween(1,10);
		int init3 = randomIntBetween(1,10);
		
		Series series = createSeries(new int[] { init1, init2, init3 },
				new int[] { voxels1, voxels2, voxels3 });
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		factory.popToIDs.put(3, new HashSet<>());
		factory.popToRegions.put(1, false);
		factory.popToRegions.put(2, false);
		factory.popToRegions.put(3, false);
		factory.createCells(series);
		
		assertEquals(init1 + init2 + init3, factory.cells.size());
		assertEquals(init1, factory.popToIDs.get(1).size());
		assertEquals(init2, factory.popToIDs.get(2).size());
		assertEquals(init3, factory.popToIDs.get(3).size());
		
		for (int i : factory.popToIDs.get(1)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels1, pottsCellContainer.voxels);
		}
		for (int i : factory.popToIDs.get(2)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels2, pottsCellContainer.voxels);
		}
		for (int i : factory.popToIDs.get(3)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels3, pottsCellContainer.voxels);
		}
	}
	
	@Test
	public void createCells_multiplePopulationsWithRegions_createsList() {
		int voxelsA = 10*randomIntBetween(1,10);
		int voxelsB = 10*randomIntBetween(1,10);
		
		int voxels1 = randomIntBetween(1,10);
		int voxels2 = voxelsA + voxelsB;
		int voxels3 = randomIntBetween(1,10);
		
		int init1 = randomIntBetween(1,10);
		int init2 = randomIntBetween(1,10);
		int init3 = randomIntBetween(1,10);
		
		Series series = createSeries(new int[] { init1, init2, init3 },
				new int[] { voxels1, voxels2, voxels3 });
		
		series._populations.get("pop2").put("(REGION)" + TAG_SEPARATOR + "UNDEFINED", (double)voxelsA/voxels2);
		series._populations.get("pop2").put("(REGION)" + TAG_SEPARATOR + "NUCLEUS", (double)voxelsB/voxels2);
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToIDs.put(2, new HashSet<>());
		factory.popToIDs.put(3, new HashSet<>());
		factory.popToRegions.put(1, false);
		factory.popToRegions.put(2, true);
		factory.popToRegions.put(3, false);
		factory.createCells(series);
		
		assertEquals(init1 + init2 + init3, factory.cells.size());
		assertEquals(init1, factory.popToIDs.get(1).size());
		assertEquals(init2, factory.popToIDs.get(2).size());
		assertEquals(init3, factory.popToIDs.get(3).size());
		
		for (int i : factory.popToIDs.get(1)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels1, pottsCellContainer.voxels);
		}
		for (int i : factory.popToIDs.get(2)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels2, pottsCellContainer.voxels);
			assertEquals(voxelsA, (int)pottsCellContainer.regionVoxels.get(Region.UNDEFINED));
			assertEquals(voxelsB, (int)pottsCellContainer.regionVoxels.get(Region.NUCLEUS));
			assertEquals(0, (int)pottsCellContainer.regionVoxels.get(Region.DEFAULT));
		}
		for (int i : factory.popToIDs.get(3)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels3, pottsCellContainer.voxels);
		}
	}
	
	@Test
	public void createCells_extraRegions_skipsExtra() {
		int voxel = randomIntBetween(1,10);
		int voxels = 4*voxel;
		int init = randomIntBetween(1,10);
		
		Series series = createSeries(new int[] { init }, new int[] { voxels });
		
		series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "UNDEFINED", 0.75);
		series._populations.get("pop1").put("(REGION)" + TAG_SEPARATOR + "DEFAULT", 0.75);
		
		PottsCellFactoryMock factory = new PottsCellFactoryMock();
		factory.popToIDs.put(1, new HashSet<>());
		factory.popToRegions.put(1, true);
		factory.createCells(series);
		
		assertEquals(init, factory.cells.size());
		assertEquals(init, factory.popToIDs.get(1).size());
		
		for (int i : factory.popToIDs.get(1)) {
			PottsCellContainer pottsCellContainer = (PottsCellContainer)factory.cells.get(i);
			assertEquals(voxels, pottsCellContainer.voxels);
			assertEquals(3*voxel, (int)pottsCellContainer.regionVoxels.get(Region.UNDEFINED));
			assertEquals(voxel, (int)pottsCellContainer.regionVoxels.get(Region.DEFAULT));
		}
	}
	
	@Test
	public void make_onePopulationNoRegionsNoTarget_createsObject() {
		Location location = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		int cellID = randomIntBetween(1, 10);
		int cellPop = randomIntBetween(1, 10);
		int cellAge = randomIntBetween(1, 100);
		State cellState = randomState();
		Phase cellPhase = randomPhase();
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		MiniBox parameters = mock(MiniBox.class);
		
		factory.popToCriticals.put(cellPop, criticals);
		factory.popToLambdas.put(cellPop, lambdas);
		factory.popToAdhesion.put(cellPop, adhesion);
		factory.popToParameters.put(cellPop, parameters);
		factory.popToRegions.put(cellPop, false);
		
		PottsCellContainer container = new PottsCellContainer(cellID, cellPop, cellAge, cellState, cellPhase,
				0, null, 0, 0, null, null);
		Cell cell = factory.make(container, location);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellAge, cell.getAge());
		assertEquals(cellState, cell.getState());
		assertEquals(parameters, cell.getParameters());
		verify((PottsModule)cell.getModule()).setPhase(cellPhase);
		assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas.get(Term.VOLUME), ((PottsCell)cell).getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas.get(Term.SURFACE), ((PottsCell)cell).getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion[0], ((PottsCell)cell).getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], ((PottsCell)cell).getAdhesion(1), EPSILON);
		assertEquals(0, cell.getTargetVolume(), EPSILON);
		assertEquals(0, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void make_multiplePopulationsNoRegionsNoTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		int cellID1 = randomIntBetween(1, 10);
		int cellPop1 = randomIntBetween(1, 10);
		int cellAge1 = randomIntBetween(1, 100);
		State cellState1 = randomState();
		Phase cellPhase1 = randomPhase();
		EnumMap<Term, Double> criticals1 = makeEnumMap();
		EnumMap<Term, Double> lambdas1 = makeEnumMap();
		double[] adhesion1 = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		MiniBox parameters1 = mock(MiniBox.class);
		
		factory.popToCriticals.put(cellPop1, criticals1);
		factory.popToLambdas.put(cellPop1, lambdas1);
		factory.popToAdhesion.put(cellPop1, adhesion1);
		factory.popToParameters.put(cellPop1, parameters1);
		factory.popToRegions.put(cellPop1, false);
		
		int cellID2 = cellID1 + 1;
		int cellPop2 = cellPop1 + 1;
		int cellAge2 = randomIntBetween(1, 100);
		State cellState2 = randomState();
		Phase cellPhase2 = randomPhase();
		EnumMap<Term, Double> criticals2 = makeEnumMap();
		EnumMap<Term, Double> lambdas2 = makeEnumMap();
		double[] adhesion2 = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		MiniBox parameters2 = mock(MiniBox.class);
		
		factory.popToCriticals.put(cellPop2, criticals2);
		factory.popToLambdas.put(cellPop2, lambdas2);
		factory.popToAdhesion.put(cellPop2, adhesion2);
		factory.popToParameters.put(cellPop2, parameters2);
		factory.popToRegions.put(cellPop2, false);
		
		PottsCellContainer container1 = new PottsCellContainer(cellID1, cellPop1, cellAge1, cellState1, cellPhase1,
				0, null, 0, 0, null, null);
		Cell cell1 = factory.make(container1, location1);
		
		assertTrue(cell1 instanceof PottsCell);
		assertEquals(location1, cell1.getLocation());
		assertEquals(cellID1, cell1.getID());
		assertEquals(cellPop1, cell1.getPop());
		assertEquals(cellAge1, cell1.getAge());
		assertEquals(cellState1, cell1.getState());
		assertEquals(parameters1, cell1.getParameters());
		verify((PottsModule)cell1.getModule()).setPhase(cellPhase1);
		assertEquals(criticals1.get(Term.VOLUME), cell1.getCriticalVolume(), EPSILON);
		assertEquals(criticals1.get(Term.SURFACE), cell1.getCriticalSurface(), EPSILON);
		assertEquals(lambdas1.get(Term.VOLUME), ((PottsCell)cell1).getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas1.get(Term.SURFACE), ((PottsCell)cell1).getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion1[0], ((PottsCell)cell1).getAdhesion(0), EPSILON);
		assertEquals(adhesion1[1], ((PottsCell)cell1).getAdhesion(1), EPSILON);
		assertEquals(adhesion1[2], ((PottsCell)cell1).getAdhesion(2), EPSILON);
		assertEquals(adhesion1[3], ((PottsCell)cell1).getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
		
		PottsCellContainer container2 = new PottsCellContainer(cellID2, cellPop2, cellAge2, cellState2, cellPhase2,
				0, null, 0, 0, null, null);
		Cell cell2 = factory.make(container2, location2);
		
		assertTrue(cell2 instanceof PottsCell);
		assertEquals(location2, cell2.getLocation());
		assertEquals(cellID2, cell2.getID());
		assertEquals(cellPop2, cell2.getPop());
		assertEquals(cellAge2, cell2.getAge());
		assertEquals(cellState2, cell2.getState());
		assertEquals(parameters2, cell2.getParameters());
		verify((PottsModule)cell2.getModule()).setPhase(cellPhase2);
		assertEquals(criticals2.get(Term.VOLUME), cell2.getCriticalVolume(), EPSILON);
		assertEquals(criticals2.get(Term.SURFACE), cell2.getCriticalSurface(), EPSILON);
		assertEquals(lambdas2.get(Term.VOLUME), ((PottsCell)cell2).getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas2.get(Term.SURFACE), ((PottsCell)cell2).getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion2[0], ((PottsCell)cell2).getAdhesion(0), EPSILON);
		assertEquals(adhesion2[1], ((PottsCell)cell2).getAdhesion(1), EPSILON);
		assertEquals(adhesion2[2], ((PottsCell)cell2).getAdhesion(2), EPSILON);
		assertEquals(adhesion2[3], ((PottsCell)cell2).getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void make_onePopulationWithRegionsNoTarget_createsObject() {
		Location location = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		int cellID = randomIntBetween(1, 10);
		int cellPop = randomIntBetween(1, 10);
		int cellAge = randomIntBetween(1, 100);
		State cellState = randomState();
		Phase cellPhase = randomPhase();
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		MiniBox parameters = mock(MiniBox.class);
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		factory.popToCriticals.put(cellPop, criticals);
		factory.popToLambdas.put(cellPop, lambdas);
		factory.popToAdhesion.put(cellPop, adhesion);
		factory.popToParameters.put(cellPop, parameters);
		
		factory.popToRegionCriticals.put(cellPop, criticalsRegion);
		factory.popToRegionLambdas.put(cellPop, lambdasRegion);
		factory.popToRegionAdhesion.put(cellPop, adhesionRegion);
		
		factory.popToRegions.put(cellPop, true);
		
		PottsCellContainer container = new PottsCellContainer(cellID, cellPop, cellAge, cellState, cellPhase,
				0, null, 0, 0, null, null);
		Cell cell = factory.make(container, location);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellAge, cell.getAge());
		assertEquals(cellState, cell.getState());
		assertEquals(parameters, cell.getParameters());
		verify((PottsModule)cell.getModule()).setPhase(cellPhase);
		assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas.get(Term.VOLUME), ((PottsCell)cell).getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas.get(Term.SURFACE), ((PottsCell)cell).getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion[0], ((PottsCell)cell).getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], ((PottsCell)cell).getAdhesion(1), EPSILON);
		assertEquals(0, cell.getTargetVolume(), EPSILON);
		assertEquals(0, cell.getTargetSurface(), EPSILON);
		
		for (Region region : regionList) {
			assertEquals(criticalsRegion.get(region).get(Term.VOLUME), cell.getCriticalVolume(region), EPSILON);
			assertEquals(criticalsRegion.get(region).get(Term.SURFACE), cell.getCriticalSurface(region), EPSILON);
			assertEquals(lambdasRegion.get(region).get(Term.VOLUME), ((PottsCell)cell).getLambda(Term.VOLUME, region), EPSILON);
			assertEquals(lambdasRegion.get(region).get(Term.SURFACE), ((PottsCell)cell).getLambda(Term.SURFACE, region), EPSILON);
			assertEquals(0, cell.getTargetVolume(region), EPSILON);
			assertEquals(0, cell.getTargetSurface(region), EPSILON);
			
			for (Region target : regionList) {
				assertEquals(adhesionRegion.get(region).get(target), ((PottsCell)cell).getAdhesion(region, target), EPSILON);
			}
		}
	}
	
	@Test
	public void make_multiplePopulationsWithRegionsNoTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		int cellID1 = randomIntBetween(1, 10);
		int cellPop1 = randomIntBetween(1, 10);
		int cellAge1 = randomIntBetween(1, 100);
		State cellState1 = randomState();
		Phase cellPhase1 = randomPhase();
		EnumMap<Term, Double> criticals1 = makeEnumMap();
		EnumMap<Term, Double> lambdas1 = makeEnumMap();
		double[] adhesion1 = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		MiniBox parameters1 = mock(MiniBox.class);
		
		factory.popToCriticals.put(cellPop1, criticals1);
		factory.popToLambdas.put(cellPop1, lambdas1);
		factory.popToAdhesion.put(cellPop1, adhesion1);
		factory.popToParameters.put(cellPop1, parameters1);
		factory.popToRegions.put(cellPop1, false);
		
		int cellID2 = cellID1 + 1;
		int cellPop2 = cellPop1 + 1;
		int cellAge2 = randomIntBetween(1, 100);
		State cellState2 = randomState();
		Phase cellPhase2 = randomPhase();
		EnumMap<Term, Double> criticals2 = makeEnumMap();
		EnumMap<Term, Double> lambdas2 = makeEnumMap();
		double[] adhesion2 = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		MiniBox parameters2 = mock(MiniBox.class);
		
		factory.popToCriticals.put(cellPop2, criticals2);
		factory.popToLambdas.put(cellPop2, lambdas2);
		factory.popToAdhesion.put(cellPop2, adhesion2);
		factory.popToParameters.put(cellPop2, parameters2);
		factory.popToRegions.put(cellPop2, true);
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location2).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		factory.popToRegionCriticals.put(cellPop2, criticalsRegion);
		factory.popToRegionLambdas.put(cellPop2, lambdasRegion);
		factory.popToRegionAdhesion.put(cellPop2, adhesionRegion);
		
		PottsCellContainer container1 = new PottsCellContainer(cellID1, cellPop1, cellAge1, cellState1, cellPhase1,
				0, null, 0, 0, null, null);
		Cell cell1 = factory.make(container1, location1);
		
		assertTrue(cell1 instanceof PottsCell);
		assertEquals(location1, cell1.getLocation());
		assertEquals(cellID1, cell1.getID());
		assertEquals(cellPop1, cell1.getPop());
		assertEquals(cellAge1, cell1.getAge());
		assertEquals(cellState1, cell1.getState());
		assertEquals(parameters1, cell1.getParameters());
		verify((PottsModule)cell1.getModule()).setPhase(cellPhase1);
		assertEquals(criticals1.get(Term.VOLUME), cell1.getCriticalVolume(), EPSILON);
		assertEquals(criticals1.get(Term.SURFACE), cell1.getCriticalSurface(), EPSILON);
		assertEquals(lambdas1.get(Term.VOLUME), ((PottsCell)cell1).getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas1.get(Term.SURFACE), ((PottsCell)cell1).getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion1[0], ((PottsCell)cell1).getAdhesion(0), EPSILON);
		assertEquals(adhesion1[1], ((PottsCell)cell1).getAdhesion(1), EPSILON);
		assertEquals(adhesion1[2], ((PottsCell)cell1).getAdhesion(2), EPSILON);
		assertEquals(adhesion1[3], ((PottsCell)cell1).getAdhesion(3), EPSILON);
		assertEquals(0, cell1.getTargetVolume(), EPSILON);
		assertEquals(0, cell1.getTargetSurface(), EPSILON);
		
		for (Region region : regionList) {
			assertEquals(0, cell1.getCriticalVolume(region), EPSILON);
			assertEquals(0, cell1.getCriticalSurface(region), EPSILON);
			assertEquals(0, ((PottsCell)cell1).getLambda(Term.VOLUME, region), EPSILON);
			assertEquals(0, ((PottsCell)cell1).getLambda(Term.SURFACE, region), EPSILON);
			assertEquals(0, cell1.getTargetVolume(region), EPSILON);
			assertEquals(0, cell1.getTargetSurface(region), EPSILON);
			
			for (Region target : regionList) {
				assertEquals(0, ((PottsCell)cell1).getAdhesion(region, target), EPSILON);
			}
		}
		
		PottsCellContainer container2 = new PottsCellContainer(cellID2, cellPop2, cellAge2, cellState2, cellPhase2,
				0, null, 0, 0, null, null);
		Cell cell2 = factory.make(container2, location2);
		
		assertTrue(cell2 instanceof PottsCell);
		assertEquals(location2, cell2.getLocation());
		assertEquals(cellID2, cell2.getID());
		assertEquals(cellPop2, cell2.getPop());
		assertEquals(cellAge2, cell2.getAge());
		assertEquals(cellState2, cell2.getState());
		assertEquals(parameters2, cell2.getParameters());
		verify((PottsModule)cell2.getModule()).setPhase(cellPhase2);
		assertEquals(criticals2.get(Term.VOLUME), cell2.getCriticalVolume(), EPSILON);
		assertEquals(criticals2.get(Term.SURFACE), cell2.getCriticalSurface(), EPSILON);
		assertEquals(lambdas2.get(Term.VOLUME), ((PottsCell)cell2).getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas2.get(Term.SURFACE), ((PottsCell)cell2).getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion2[0], ((PottsCell)cell2).getAdhesion(0), EPSILON);
		assertEquals(adhesion2[1], ((PottsCell)cell2).getAdhesion(1), EPSILON);
		assertEquals(adhesion2[2], ((PottsCell)cell2).getAdhesion(2), EPSILON);
		assertEquals(adhesion2[3], ((PottsCell)cell2).getAdhesion(3), EPSILON);
		assertEquals(0, cell2.getTargetVolume(), EPSILON);
		assertEquals(0, cell2.getTargetSurface(), EPSILON);
		
		for (Region region : regionList) {
			assertEquals(criticalsRegion.get(region).get(Term.VOLUME), cell2.getCriticalVolume(region), EPSILON);
			assertEquals(criticalsRegion.get(region).get(Term.SURFACE), cell2.getCriticalSurface(region), EPSILON);
			assertEquals(lambdasRegion.get(region).get(Term.VOLUME), ((PottsCell)cell2).getLambda(Term.VOLUME, region), EPSILON);
			assertEquals(lambdasRegion.get(region).get(Term.SURFACE), ((PottsCell)cell2).getLambda(Term.SURFACE, region), EPSILON);
			assertEquals(0, cell2.getTargetVolume(region), EPSILON);
			assertEquals(0, cell2.getTargetSurface(region), EPSILON);
			
			for (Region target : regionList) {
				assertEquals(adhesionRegion.get(region).get(target), ((PottsCell)cell2).getAdhesion(region, target), EPSILON);
			}
		}
	}
	
	@Test
	public void make_onePopulationNoRegionsWithTarget_createsObject() {
		Location location = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		double targetVolume = randomDoubleBetween(1, 100);
		double targetSurface = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[2]);
		factory.popToRegions.put(1, false);
		
		PottsCellContainer container = new PottsCellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume, targetSurface);
		PottsCell cell = (PottsCell)factory.make(container, location);
		
		verify(cell).setTargets(targetVolume, targetSurface);
	}
	
	@Test
	public void make_multiplePopulationsNoRegionsWithTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		double targetVolume1 = randomDoubleBetween(1, 100);
		double targetSurface1 = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[3]);
		factory.popToRegions.put(1, false);
		
		double targetVolume2 = randomDoubleBetween(1, 100);
		double targetSurface2 = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(2, makeEnumMap());
		factory.popToLambdas.put(2, makeEnumMap());
		factory.popToAdhesion.put(2, new double[3]);
		factory.popToRegions.put(2, false);
		
		PottsCellContainer container1 = new PottsCellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume1, targetSurface1);
		PottsCell cell1 = (PottsCell)factory.make(container1, location1);
		verify(cell1).setTargets(targetVolume1, targetSurface1);
		
		PottsCellContainer container2 = new PottsCellContainer(2, 2, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume2, targetSurface2);
		PottsCell cell2 = (PottsCell)factory.make(container2, location2);
		verify(cell2).setTargets(targetVolume2, targetSurface2);
	}
	
	@Test
	public void make_onePopulationWithRegionsWithTarget_createsObject() {
		Location location = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		double targetVolume = randomDoubleBetween(1, 100);
		double targetSurface = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[2]);
		factory.popToRegions.put(1, true);
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		factory.popToRegionCriticals.put(1, criticalsRegion);
		factory.popToRegionLambdas.put(1, lambdasRegion);
		factory.popToRegionAdhesion.put(1, adhesionRegion);
		
		EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
		EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
		
		for (Region region : regionList) {
			targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
			targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
		}
		
		PottsCellContainer container = new PottsCellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume, targetSurface, targetRegionVolumes, targetRegionSurfaces);
		PottsCell cell = (PottsCell)factory.make(container, location);
		
		verify(cell).setTargets(targetVolume, targetSurface);
		for (Region region : regionList) {
			verify(cell).setTargets(region, targetRegionVolumes.get(region), targetRegionSurfaces.get(region));
		}
	}
	
	@Test
	public void make_multiplePopulationsWithRegionsWithTarget_createsObject() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		double targetVolume1 = randomDoubleBetween(1, 100);
		double targetSurface1 = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[3]);
		factory.popToRegions.put(1, false);
		
		double targetVolume2 = randomDoubleBetween(1, 100);
		double targetSurface2 = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(2, makeEnumMap());
		factory.popToLambdas.put(2, makeEnumMap());
		factory.popToAdhesion.put(2, new double[3]);
		factory.popToRegions.put(2, true);
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location2).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		factory.popToRegionCriticals.put(2, criticalsRegion);
		factory.popToRegionLambdas.put(2, lambdasRegion);
		factory.popToRegionAdhesion.put(2, adhesionRegion);
		
		EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
		EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
		
		for (Region region : regionList) {
			targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
			targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
		}
		
		PottsCellContainer container1 = new PottsCellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume1, targetSurface1);
		PottsCell cell1 = (PottsCell)factory.make(container1, location1);
		
		verify(cell1).setTargets(targetVolume1, targetSurface1);
		for (Region region : regionList) {
			verify(cell1, never()).setTargets(region, targetRegionVolumes.get(region), targetRegionSurfaces.get(region));
		}
		
		PottsCellContainer container2 = new PottsCellContainer(2, 2, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume2, targetSurface2, targetRegionVolumes, targetRegionSurfaces);
		PottsCell cell2 = (PottsCell)factory.make(container2, location2);
		
		verify(cell2).setTargets(targetVolume2, targetSurface2);
		for (Region region : regionList) {
			verify(cell2).setTargets(region, targetRegionVolumes.get(region), targetRegionSurfaces.get(region));
		}
	}
	
	@Test
	public void make_onePopulationWithRegionsMixedTarget_createsObject() {
		Location location = mock(Location.class);
		PottsCellFactory factory = new PottsCellFactoryMock();
		
		double targetVolume = randomDoubleBetween(1, 100);
		double targetSurface = randomDoubleBetween(1, 100);
		
		factory.popToCriticals.put(1, makeEnumMap());
		factory.popToLambdas.put(1, makeEnumMap());
		factory.popToAdhesion.put(1, new double[2]);
		factory.popToRegions.put(1, true);
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		factory.popToRegionCriticals.put(1, criticalsRegion);
		factory.popToRegionLambdas.put(1, lambdasRegion);
		factory.popToRegionAdhesion.put(1, adhesionRegion);
		
		EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
		EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
		
		for (Region region : regionList) {
			targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
			targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
		}
		
		PottsCellContainer container1 = new PottsCellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume, targetSurface, targetRegionVolumes, null);
		PottsCell cell1 = (PottsCell)factory.make(container1, location);
		
		verify(cell1).setTargets(targetVolume, targetSurface);
		for (Region region : regionList) {
			verify(cell1, never()).setTargets(region, targetRegionVolumes.get(region), targetRegionSurfaces.get(region));
		}
		
		PottsCellContainer container2 = new PottsCellContainer(1, 1, 0, State.UNDEFINED, Phase.UNDEFINED, 0, null,
				targetVolume, targetSurface, null, targetRegionSurfaces);
		PottsCell cell2 = (PottsCell)factory.make(container2, location);
		
		verify(cell2).setTargets(targetVolume, targetSurface);
		for (Region region : regionList) {
			verify(cell2, never()).setTargets(region, targetRegionVolumes.get(region), targetRegionSurfaces.get(region));
		}
	}
}
