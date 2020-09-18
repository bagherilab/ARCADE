package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import arcade.util.Box;
import arcade.util.MiniBox;

public class SeriesTest {
	private static final double EPSILON = 1E-4;
	private static Box PARAMETERS;
	private static final String DEFAULT_NAME = "DEFAULT_NAME";
	private static final String DEFAULT_PATH = "/default/path/";
	private static final int DEFAULT_START_SEED = randomInt();
	private static final int DEFAULT_END_SEED = randomInt();
	private static final int DEFAULT_TICKS = randomInt();
	private static final int DEFAULT_LENGTH = randomInt();
	private static final int DEFAULT_WIDTH = randomInt();
	private static final int DEFAULT_HEIGHT = randomOdd();
	private static final double ADHESION = randomDouble();
	
	private static final String[] POPULATION_PARAMETER_NAMES = new String[] {
			"LAMBDA_VOLUME",
			"LAMBDA_SURFACE",
			"CRITICAL_VOLUME",
			"CRITICAL_SURFACE",
			"ADHESION"
	};
	
	private static final double[] POPULATION_PARAMETER_VALUES = new double[] {
			randomDouble(), randomDouble(), randomDouble(), randomDouble(), randomDouble()
	};
	
	private static final int POPULATION_PARAMETER_COUNT = POPULATION_PARAMETER_NAMES.length;
	
	private static final String POPULATION_ID_1 = randomString();
	private static final String POPULATION_ID_2 = randomString();
	private static final String POPULATION_ID_3 = randomString();
	
	private static final String TAG_ID_1 = randomString();
	private static final String TAG_ID_2 = randomString();
	
	private static MiniBox DEFAULTS, POPULATION;
	
	private static final HashMap<String, ArrayList<Box>> setupListsMock = mock(HashMap.class);
	
	static int randomInt() { return (int)(Math.random()*100); }
	
	static double randomDouble() { return Math.random()*100; }
	
	static int randomEven() { return (int)(Math.random()*100)*2; }
	
	static int randomOdd() { return (int)(Math.random()*100)*2 + 1; }
	
	static String randomString() {
		return new Random().ints(65, 91)
				.limit(5)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
	
	@BeforeClass
	public static void setupParameters() {
		PARAMETERS = new Box();
		
		// DEFAULTS 
		
		PARAMETERS.addTag("START_SEED", "DEFAULT");
		PARAMETERS.addTag("END_SEED", "DEFAULT");
		PARAMETERS.addTag("TICKS", "DEFAULT");
		PARAMETERS.addTag("LENGTH", "DEFAULT");
		PARAMETERS.addTag("WIDTH", "DEFAULT");
		PARAMETERS.addTag("HEIGHT", "DEFAULT");
		
		PARAMETERS.addAtt("START_SEED", "value", "" + DEFAULT_START_SEED);
		PARAMETERS.addAtt("END_SEED", "value", "" + DEFAULT_END_SEED);
		PARAMETERS.addAtt("TICKS", "value", "" + DEFAULT_TICKS);
		PARAMETERS.addAtt("LENGTH", "value", "" + DEFAULT_LENGTH);
		PARAMETERS.addAtt("WIDTH", "value", "" + DEFAULT_WIDTH);
		PARAMETERS.addAtt("HEIGHT", "value", "" + DEFAULT_HEIGHT);
		
		DEFAULTS = PARAMETERS.getIdValForTag("DEFAULT");
		
		// POPULATION
		for (int i = 0; i < POPULATION_PARAMETER_COUNT; i++) {
			PARAMETERS.addTag(POPULATION_PARAMETER_NAMES[i], "POPULATION");
			PARAMETERS.addAtt(POPULATION_PARAMETER_NAMES[i], "value", "" + POPULATION_PARAMETER_VALUES[i]);
		}
		
		PARAMETERS.addTag("ADHESION", "POPULATION");
		PARAMETERS.addAtt("ADHESION", "value", "" + ADHESION);
		
		POPULATION = PARAMETERS.getIdValForTag("POPULATION");
	}
	
	private HashMap<String, MiniBox> makeDicts() {
		HashMap<String, MiniBox> setupDicts = new HashMap<>();
		
		MiniBox set = new MiniBox();
		set.put("path", DEFAULT_PATH);
		setupDicts.put("set", set);
		
		MiniBox series = new MiniBox();
		series.put("name", DEFAULT_NAME);
		setupDicts.put("series", series);
		
		setupDicts.put("simulation", new MiniBox());
		setupDicts.put("agents", new MiniBox());
		setupDicts.put("environment", new MiniBox());
		
		return setupDicts;
	}
	
	private HashMap<String, ArrayList<Box>> makeLists() {
		HashMap<String, ArrayList<Box>> setupLists = new HashMap<>();
		
		ArrayList<Box> populations = new ArrayList<>();
		setupLists.put("populations", populations);
		
		return setupLists;
	}
	
	@Test
	public void constructor_noPrefix_updatesNames() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_NAME, series.getName());
		assertEquals(DEFAULT_PATH + DEFAULT_NAME + "_", series.getPrefix());
	}
	
	@Test
	public void constructor_givenPrefix_updatesNames() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("set").put("prefix", "PREFIX_");
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_NAME, series.getName());
		assertEquals(DEFAULT_PATH + "PREFIX_" + DEFAULT_NAME + "_", series.getPrefix());
	}
	
	@Test
	public void constructor_seedsNotGiven_usesDefaults() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_START_SEED, series.getStartSeed());
		assertEquals(DEFAULT_END_SEED, series.getEndSeed());
	}
	
	@Test
	public void constructor_seedGiven_usesGiven() {
		int startSeed = randomInt();
		int endSeed = startSeed + randomInt();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("start", startSeed);
		setupDicts.get("series").put("end", endSeed);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(startSeed, series.getStartSeed());
		assertEquals(endSeed, series.getEndSeed());
	}
	
	@Test
	public void constructor_ticksNotGiven_usesDefault() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(DEFAULT_TICKS, series.getTicks());
	}
	
	@Test
	public void constructor_ticksGiven_usesGiven() {
		int ticks = randomInt();
		HashMap<String, MiniBox> setupDicts = makeDicts();
		setupDicts.get("series").put("ticks", ticks);
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		
		assertEquals(ticks, series.getTicks());
	}
	
	@Test
	public void constructor_withoutVis_updatesField() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, false);
		assertFalse(series.isVis);
	}
	
	@Test
	public void constructor_withVis_updatesField() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = new Series(setupDicts, setupListsMock, PARAMETERS, true);
		assertTrue(series.isVis);
	}
	
	@Test
	public void initialize_default_callsMethods() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		HashMap<String, ArrayList<Box>> setupLists = makeLists();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		series.initialize(setupDicts, setupLists, PARAMETERS);
		
		MiniBox simulation = setupDicts.get("simulation");
		MiniBox agents = setupDicts.get("agents");
		MiniBox environment = setupDicts.get("environment");
		
		verify(series).updateSizing(eq(simulation), any(MiniBox.class));
		verify(series).updateAgents(agents);
		verify(series).updateEnvironment(environment);
		
		ArrayList<Box> populations = setupLists.get("populations");
		verify(series).updatePopulations(eq(populations), any(MiniBox.class));
		
		ArrayList<Box> helpers = setupLists.get("helpers");
		verify(series).updateHelpers(eq(helpers), any(MiniBox.class));
		
		ArrayList<Box> components = setupLists.get("components");
		verify(series).updateComponents(eq(components), any(MiniBox.class));
		
		ArrayList<Box> profilers = setupLists.get("profilers");
		verify(series).updateProfilers(eq(profilers), any(MiniBox.class));
		
		ArrayList<Box> checkpoints = setupLists.get("checkpoints");
		verify(series).updateCheckpoints(eq(checkpoints), any(MiniBox.class));
	}
	
	@Test
	public void updateSizing_sizesNotGiven_usesDefaults() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		MiniBox simulation = setupDicts.get("simulation");
		series.updateSizing(simulation, DEFAULTS);
		
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
	}
	
	@Test
	public void updateSizing_oneSizeGivenOdd_useGiven() {
		int length = randomOdd();
		int width = randomOdd();
		int height = randomOdd();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		MiniBox simulation;
		
		simulation = new MiniBox();
		simulation.put("length", length);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		simulation = new MiniBox();
		simulation.put("width", width);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		simulation = new MiniBox();
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height, series._height);
	}
	
	@Test
	public void updateSizing_twoSizesGivenOdd_useGiven() {
		int length = randomOdd();
		int width = randomOdd();
		int height = randomOdd();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		MiniBox simulation;
		
		simulation = new MiniBox();
		simulation.put("length", length);
		simulation.put("width", width);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		simulation = new MiniBox();
		simulation.put("width", width);
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(height, series._height);
		
		simulation = new MiniBox();
		simulation.put("length", length);
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height, series._height);
	}
	
	@Test
	public void updateSizing_allSizesGivenOdd_useGiven() {
		int length = randomOdd();
		int width = randomOdd();
		int height = randomOdd();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		MiniBox simulation = new MiniBox();
		simulation.put("length", length);
		simulation.put("width", width);
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(height, series._height);
	}
	
	@Test
	public void updateSizing_oneSizeGivenEven_useGiven() {
		int length = randomEven();
		int width = randomEven();
		int height = randomEven();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		MiniBox simulation;
		
		simulation = new MiniBox();
		simulation.put("length", length);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		simulation = new MiniBox();
		simulation.put("width", width);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		simulation = new MiniBox();
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height + 1, series._height);
	}
	
	@Test
	public void updateSizing_twoSizesGivenEven_useGiven() {
		int length = randomEven();
		int width = randomEven();
		int height = randomEven();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		MiniBox simulation;
		
		simulation = new MiniBox();
		simulation.put("length", length);
		simulation.put("width", width);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(DEFAULT_HEIGHT, series._height);
		
		simulation = new MiniBox();
		simulation.put("width", width);
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(DEFAULT_LENGTH, series._length);
		assertEquals(width, series._width);
		assertEquals(height + 1, series._height);
		
		simulation = new MiniBox();
		simulation.put("length", length);
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(DEFAULT_WIDTH, series._width);
		assertEquals(height + 1, series._height);
	}
	
	@Test
	public void updateSizing_allSizesGivenEven_useGiven() {
		int length = randomEven();
		int width = randomEven();
		int height = randomEven();
		
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		
		MiniBox simulation = new MiniBox();
		simulation.put("length", length);
		simulation.put("width", width);
		simulation.put("height", height);
		series.updateSizing(simulation, DEFAULTS);
		assertEquals(length, series._length);
		assertEquals(width, series._width);
		assertEquals(height + 1, series._height);
	}
	
	private Series makeSeriesForPopulation(Box[] boxes) {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		HashMap<String, ArrayList<Box>> setupLists = makeLists();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		ArrayList<Box> populations = setupLists.get("populations");
		populations.addAll(Arrays.asList(boxes));
		series.updatePopulations(populations, POPULATION);
		return series;
	}
	
	@Test
	public void updatePopulation_noPopulations_createsMap() {
		HashMap<String, MiniBox> setupDicts = makeDicts();
		Series series = spy(new Series(setupDicts, setupListsMock, PARAMETERS, false));
		series.updatePopulations(null, POPULATION);
		assertEquals(0, series._populations.size());
	}
	
	@Test
	public void updatePopulation_onePopulation_createsMap() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		
		assertEquals(1, series._populations.size());
		assertNotNull(series._populations.get(POPULATION_ID_1));
		assertEquals(1, series._populations.get(POPULATION_ID_1).getInt("CODE"));
	}
	
	@Test
	public void updatePopulation_multiplePopulations_createsMap() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		boxes[1].add("ID", POPULATION_ID_2);
		boxes[2].add("ID", POPULATION_ID_3);
		Series series = makeSeriesForPopulation(boxes);
		
		assertEquals(3, series._populations.size());
		assertNotNull(series._populations.get(POPULATION_ID_1));
		assertNotNull(series._populations.get(POPULATION_ID_2));
		assertNotNull(series._populations.get(POPULATION_ID_3));
		assertEquals(1, series._populations.get(POPULATION_ID_1).getInt("CODE"));
		assertEquals(2, series._populations.get(POPULATION_ID_2).getInt("CODE"));
		assertEquals(3, series._populations.get(POPULATION_ID_3).getInt("CODE"));
	}
	
	@Test
	public void updatePopulation_noFraction_setsZero() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		
		MiniBox box = series._populations.get(POPULATION_ID_1);
		assertEquals(0, box.getDouble("FRACTION"), EPSILON);
	}
	
	@Test
	public void updatePopulation_givenValidFraction_setsValue() {
		String[] fractions = new String[] { "0", "0.", "0.0", ".0", "0.5", "0.67", "1", "1.", "1.0" };
		double[] values = new double[] { 0, 0, 0, 0, 0.5, 0.67, 1, 1, 1 };
		
		for (int i = 0; i < fractions.length; i++) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[0].add("FRACTION", fractions[i]);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(values[i], box.getDouble("FRACTION"), EPSILON);
		}
	}
	
	@Test
	public void updatePopulation_givenInvalidFraction_setsZero() {
		String[] fractions = new String[] { "1.1", "2", "a", "-0.5" };
		
		for (String fraction : fractions) {
			Box[] boxes = new Box[]{new Box()};
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[0].add("FRACTION", fraction);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(0, box.getDouble("FRACTION"), EPSILON);
		}
	}
	
	@Test
	public void updatePopulation_withTagsValidFraction_setsTags() {
		String[] fractions = new String[] { "0", "0.", "0.0", ".0", "0.5", "0.67", "1", "1.", "1.0" };
		double[] values = new double[] { 0, 0, 0, 0, 0.5, 0.67, 1, 1, 1 };
		
		for (int i = 0; i < fractions.length; i++) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[0].addTag(TAG_ID_1, "TAG");
			boxes[0].addAtt(TAG_ID_1,"fraction", fractions[i]);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(values[i], box.getDouble("TAG/" + TAG_ID_1), EPSILON);
		}
	}
	
	@Test
	public void updatePopulation_withTagsInvalidFraction_setsTags() {
		String[] fractions = new String[] { "1.1", "2", "a", "-0.5" };
		
		for (String fraction : fractions) {
			Box[] boxes = new Box[]{new Box()};
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[0].addTag(TAG_ID_1, "TAG");
			boxes[0].addAtt(TAG_ID_1, "fraction", fraction);
			Series series = makeSeriesForPopulation(boxes);
			
			MiniBox box = series._populations.get(POPULATION_ID_1);
			assertEquals(0, box.getDouble("TAG/" + TAG_ID_1), EPSILON);
		}
	}
	
	@Test
	public void updatePopulation_noParametersOnePopNoTags_usesDefaults() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			assertEquals(POPULATION.get(parameter), box.get(parameter));
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesOnePopNoTags_usesGiven() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			
			String value = "" + randomDouble();
			boxes[0].addAtt(populationParameter, "value", value);
			boxes[0].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box = series._populations.get(POPULATION_ID_1);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				String expected = (parameter.equals(populationParameter) ? value : POPULATION.get(parameter));
				assertEquals(expected, box.get(parameter));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterScaledOnePopNoTags_updatesDefaults() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			
			double scale = randomDouble();
			boxes[0].addAtt(populationParameter, "scale", "" + scale);
			boxes[0].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box = series._populations.get(POPULATION_ID_1);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				double expected = (parameter.equals(populationParameter) ? scale : 1)*POPULATION.getDouble(parameter);
				assertEquals(expected, box.getDouble(parameter), EPSILON);
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesScaledOnePopNoTags_updatesDefaults() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt(populationParameter1, "value", "" + value);
				boxes[0].addTag(populationParameter1, "PARAMETER");
				boxes[0].addAtt(populationParameter2, "scale", "" + scale);
				boxes[0].addTag(populationParameter2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(expected, box.getDouble(parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noParametersMultiplePopsNoTags_usesDefaults() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		boxes[1].add("ID", POPULATION_ID_2);
		boxes[2].add("ID", POPULATION_ID_3);
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box1 = series._populations.get(POPULATION_ID_1);
		MiniBox box2 = series._populations.get(POPULATION_ID_2);
		MiniBox box3 = series._populations.get(POPULATION_ID_3);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			assertEquals(POPULATION.get(parameter), box1.get(parameter));
			assertEquals(POPULATION.get(parameter), box2.get(parameter));
			assertEquals(POPULATION.get(parameter), box3.get(parameter));
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesMultiplePopsNoTags_usesGiven() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[1].add("ID", POPULATION_ID_2);
			boxes[2].add("ID", POPULATION_ID_3);
			
			String value = "" + randomDouble();
			boxes[1].addAtt(populationParameter, "value", value);
			boxes[1].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box1 = series._populations.get(POPULATION_ID_1);
			MiniBox box2 = series._populations.get(POPULATION_ID_2);
			MiniBox box3 = series._populations.get(POPULATION_ID_3);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				assertEquals(POPULATION.get(parameter), box1.get(parameter));
				assertEquals(POPULATION.get(parameter), box3.get(parameter));
				String expected = (parameter.equals(populationParameter) ? value : POPULATION.get(parameter));
				assertEquals(expected, box2.get(parameter));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterScaledMultiplePopsNoTags_updatesDefaults() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[1].add("ID", POPULATION_ID_2);
			boxes[2].add("ID", POPULATION_ID_3);
			
			double scale = randomDouble();
			boxes[1].addAtt(populationParameter, "scale", "" + scale);
			boxes[1].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box1 = series._populations.get(POPULATION_ID_1);
			MiniBox box2 = series._populations.get(POPULATION_ID_2);
			MiniBox box3 = series._populations.get(POPULATION_ID_3);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				assertEquals(POPULATION.get(parameter), box1.get(parameter));
				assertEquals(POPULATION.get(parameter), box3.get(parameter));
				double expected = (parameter.equals(populationParameter) ? scale : 1)*POPULATION.getDouble(parameter);
				assertEquals(expected, box2.getDouble(parameter), EPSILON);
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesScaledMultiplePopsNoTags_updatesDefaults() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				boxes[1].add("ID", POPULATION_ID_2);
				boxes[2].add("ID", POPULATION_ID_3);
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[1].addAtt(populationParameter1, "value", "" + value);
				boxes[1].addTag(populationParameter1, "PARAMETER");
				boxes[1].addAtt(populationParameter2, "scale", "" + scale);
				boxes[1].addTag(populationParameter2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box1 = series._populations.get(POPULATION_ID_1);
				MiniBox box2 = series._populations.get(POPULATION_ID_2);
				MiniBox box3 = series._populations.get(POPULATION_ID_3);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					assertEquals(POPULATION.get(parameter), box1.get(parameter));
					assertEquals(POPULATION.get(parameter), box3.get(parameter));
					
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(expected, box2.getDouble(parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noAdhesionOnePopNoTags_usesDefaults() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		assertEquals(POPULATION.get("ADHESION"), box.get("ADHESION:*"));
		assertEquals(POPULATION.get("ADHESION"), box.get("ADHESION:" + POPULATION_ID_1));
	}
	
	@Test
	public void updatePopulation_givenAdhesionValuesOnePopNoTags_usesGiven() {
		String[] pops = new String[] { "*", POPULATION_ID_1 };
		for (String modifiedPop : pops) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			
			String value = "" + randomDouble();
			boxes[0].addAtt("ADHESION:" + modifiedPop, "value", value);
			boxes[0].addTag("ADHESION:" + modifiedPop, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box = series._populations.get(POPULATION_ID_1);
			
			for (String pop : pops) {
				String expected = (pop.equals(modifiedPop) ? value : POPULATION.get("ADHESION"));
				assertEquals(expected, box.get("ADHESION:" + pop));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenAdhesionScaledOnePopNoTags_usesGiven() {
		String[] pops = new String[] { "*", POPULATION_ID_1 };
		for (String modifiedPop : pops) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			
			double scale = randomDouble();
			boxes[0].addAtt("ADHESION:" + modifiedPop, "scale", "" + scale);
			boxes[0].addTag("ADHESION:" + modifiedPop, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box = series._populations.get(POPULATION_ID_1);
			
			for (String pop : pops) {
				double expected = (pop.equals(modifiedPop) ? scale : 1)*POPULATION.getDouble("ADHESION");
				assertEquals(expected, box.getDouble("ADHESION:" + pop), EPSILON);
			}
		}
	}
	
	@Test
	public void updatePopulation_givenAdhesionValuesScaledOnePopNoTags_usesGiven() {
		String[] pops = new String[] { "*", POPULATION_ID_1 };
		for (String modifiedPop1 : pops) {
			for (String modifiedPop2 : pops) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt("ADHESION:" + modifiedPop1, "value", "" + value);
				boxes[0].addTag("ADHESION:" + modifiedPop1, "PARAMETER");
				boxes[0].addAtt("ADHESION:" + modifiedPop2, "scale", "" + scale);
				boxes[0].addTag("ADHESION:" + modifiedPop2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String pop : pops) {
					double expected = POPULATION.getDouble("ADHESION");
					if (pop.equals(modifiedPop1)) { expected = value; }
					if (pop.equals(modifiedPop2)) { expected *= scale; }
					assertEquals(expected, box.getDouble("ADHESION:" + pop), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noAdhesionMultiplePopsNoTags_usesDefaults() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		boxes[1].add("ID", POPULATION_ID_2);
		boxes[2].add("ID", POPULATION_ID_3);
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box1 = series._populations.get(POPULATION_ID_1);
		MiniBox box2 = series._populations.get(POPULATION_ID_2);
		MiniBox box3 = series._populations.get(POPULATION_ID_3);
		
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:*"));
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:" + POPULATION_ID_1));
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:" + POPULATION_ID_2));
		assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:" + POPULATION_ID_3));
		assertEquals(POPULATION.get("ADHESION"), box2.get("ADHESION:*"));
		assertEquals(POPULATION.get("ADHESION"), box2.get("ADHESION:" + POPULATION_ID_1));
		assertEquals(POPULATION.get("ADHESION"), box2.get("ADHESION:" + POPULATION_ID_2));
		assertEquals(POPULATION.get("ADHESION"), box2.get("ADHESION:" + POPULATION_ID_3));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION:*"));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION:" + POPULATION_ID_1));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION:" + POPULATION_ID_2));
		assertEquals(POPULATION.get("ADHESION"), box3.get("ADHESION:" + POPULATION_ID_3));
	}
	
	@Test
	public void updatePopulation_givenAdhesionValuesMultiplePopsNoTags_usesGiven() {
		String[] pops = new String[] { "*", POPULATION_ID_1, POPULATION_ID_2 };
		for (String modifiedPop : pops) {
			Box[] boxes = new Box[] { new Box(), new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[1].add("ID", POPULATION_ID_2);
			
			String value = "" + randomDouble();
			boxes[1].addAtt("ADHESION:" + modifiedPop, "value", value);
			boxes[1].addTag("ADHESION:" + modifiedPop, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box1 = series._populations.get(POPULATION_ID_1);
			MiniBox box2 = series._populations.get(POPULATION_ID_2);
			
			for (String pop : pops) {
				assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:" + pop));
				String expected = (pop.equals(modifiedPop) ? value : POPULATION.get("ADHESION"));
				assertEquals(expected, box2.get("ADHESION:" + pop));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenAdhesionScaledMultiplePopsNoTags_usesGiven() {
		String[] pops = new String[] { "*", POPULATION_ID_1, POPULATION_ID_2 };
		for (String modifiedPop : pops) {
			Box[] boxes = new Box[] { new Box(), new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[1].add("ID", POPULATION_ID_2);
			
			double scale = randomDouble();
			boxes[1].addAtt("ADHESION:" + modifiedPop, "scale", "" + scale);
			boxes[1].addTag("ADHESION:" + modifiedPop, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box1 = series._populations.get(POPULATION_ID_1);
			MiniBox box2 = series._populations.get(POPULATION_ID_2);
			
			for (String pop : pops) {
				assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:" + pop));
				double expected = (pop.equals(modifiedPop) ? scale : 1)*POPULATION.getDouble("ADHESION");
				assertEquals(expected, box2.getDouble("ADHESION:" + pop), EPSILON);
			}
		}
	}
	
	@Test
	public void updatePopulation_givenAdhesionValuesScaledMultiplePopsNoTags_usesGiven() {
		String[] pops = new String[] { "*", POPULATION_ID_1, POPULATION_ID_2 };
		for (String modifiedPop1 : pops) {
			for (String modifiedPop2 : pops) {
				Box[] boxes = new Box[] { new Box(), new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				boxes[1].add("ID", POPULATION_ID_2);
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[1].addAtt("ADHESION:" + modifiedPop1, "value", "" + value);
				boxes[1].addTag("ADHESION:" + modifiedPop1, "PARAMETER");
				boxes[1].addAtt("ADHESION:" + modifiedPop2, "scale", "" + scale);
				boxes[1].addTag("ADHESION:" + modifiedPop2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box1 = series._populations.get(POPULATION_ID_1);
				MiniBox box2 = series._populations.get(POPULATION_ID_2);
				
				for (String pop : pops) {
					assertEquals(POPULATION.get("ADHESION"), box1.get("ADHESION:" + pop));
					
					double expected = POPULATION.getDouble("ADHESION");
					if (pop.equals(modifiedPop1)) { expected = value; }
					if (pop.equals(modifiedPop2)) { expected *= scale; }
					assertEquals(expected, box2.getDouble("ADHESION:" + pop), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noParametersOnePopWithTags_usesDefaults() {
		Box[] boxes = new Box[] { new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		boxes[0].addTag(TAG_ID_1, "TAG");
		boxes[0].addTag(TAG_ID_2, "TAG");
		boxes[0].addAtt(TAG_ID_1,"fraction", "0");
		boxes[0].addAtt(TAG_ID_2,"fraction", "0");
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box = series._populations.get(POPULATION_ID_1);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			assertEquals(POPULATION.get(parameter), box.get(TAG_ID_1 + "/" + parameter));
			assertEquals(POPULATION.get(parameter), box.get(TAG_ID_2 + "/" + parameter));
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesOnePopWithTags_usesGiven() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[0].addTag(TAG_ID_1, "TAG");
			boxes[0].addTag(TAG_ID_2, "TAG");
			boxes[0].addAtt(TAG_ID_1,"fraction", "0");
			boxes[0].addAtt(TAG_ID_2,"fraction", "0");
			
			String value = "" + randomDouble();
			boxes[0].addAtt(populationParameter, "value", value);
			boxes[0].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box = series._populations.get(POPULATION_ID_1);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				String expected = (parameter.equals(populationParameter) ? value : POPULATION.get(parameter));
				assertEquals(expected, box.get(TAG_ID_1 + "/" + parameter));
				assertEquals(expected, box.get(TAG_ID_2 + "/" + parameter));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterScaledOnePopWithTags_updatesDefaults() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[0].addTag(TAG_ID_1, "TAG");
			boxes[0].addTag(TAG_ID_2, "TAG");
			boxes[0].addAtt(TAG_ID_1,"fraction", "0");
			boxes[0].addAtt(TAG_ID_2,"fraction", "0");
			
			double scale = randomDouble();
			boxes[0].addAtt(populationParameter, "scale", "" + scale);
			boxes[0].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box = series._populations.get(POPULATION_ID_1);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				double expected = (parameter.equals(populationParameter) ? scale : 1)*POPULATION.getDouble(parameter);
				assertEquals(expected, box.getDouble(TAG_ID_1 + "/" + parameter), EPSILON);
				assertEquals(expected, box.getDouble(TAG_ID_2 + "/" + parameter), EPSILON);
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesScaledOnePopWithTags_updatesDefaults() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				boxes[0].addTag(TAG_ID_1, "TAG");
				boxes[0].addTag(TAG_ID_2, "TAG");
				boxes[0].addAtt(TAG_ID_1,"fraction", "0");
				boxes[0].addAtt(TAG_ID_2,"fraction", "0");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt(populationParameter1, "value", "" + value);
				boxes[0].addTag(populationParameter1, "PARAMETER");
				boxes[0].addAtt(populationParameter2, "scale", "" + scale);
				boxes[0].addTag(populationParameter2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(expected, box.getDouble(TAG_ID_1 + "/" + parameter), EPSILON);
					assertEquals(expected, box.getDouble(TAG_ID_2 + "/" + parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_noParametersMultiplePopsWithTags_usesDefaults() {
		Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
		boxes[0].add("ID", POPULATION_ID_1);
		boxes[1].add("ID", POPULATION_ID_2);
		boxes[2].add("ID", POPULATION_ID_3);
		boxes[1].addTag(TAG_ID_1, "TAG");
		boxes[1].addTag(TAG_ID_2, "TAG");
		boxes[1].addAtt(TAG_ID_1,"fraction", "0");
		boxes[1].addAtt(TAG_ID_2,"fraction", "0");
		Series series = makeSeriesForPopulation(boxes);
		MiniBox box1 = series._populations.get(POPULATION_ID_1);
		MiniBox box2 = series._populations.get(POPULATION_ID_2);
		MiniBox box3 = series._populations.get(POPULATION_ID_3);
		
		for (String parameter : POPULATION_PARAMETER_NAMES) {
			assertNull(box1.get(TAG_ID_1 + "/" + parameter));
			assertNull(box1.get(TAG_ID_2 + "/" + parameter));
			assertEquals(POPULATION.get(parameter), box2.get(TAG_ID_1 + "/" + parameter));
			assertEquals(POPULATION.get(parameter), box2.get(TAG_ID_2 + "/" + parameter));
			assertNull(box3.get(TAG_ID_1 + "/" + parameter));
			assertNull(box3.get(TAG_ID_2 + "/" + parameter));
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesMultiplePopsWithTags_usesGiven() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[1].add("ID", POPULATION_ID_2);
			boxes[2].add("ID", POPULATION_ID_3);
			boxes[1].addTag(TAG_ID_1, "TAG");
			boxes[1].addTag(TAG_ID_2, "TAG");
			boxes[1].addAtt(TAG_ID_1,"fraction", "0");
			boxes[1].addAtt(TAG_ID_2,"fraction", "0");
			
			String value = "" + randomDouble();
			boxes[1].addAtt(populationParameter, "value", value);
			boxes[1].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box1 = series._populations.get(POPULATION_ID_1);
			MiniBox box2 = series._populations.get(POPULATION_ID_2);
			MiniBox box3 = series._populations.get(POPULATION_ID_3);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				assertNull(box1.get(TAG_ID_1 + "/" + parameter));
				assertNull(box1.get(TAG_ID_2 + "/" + parameter));
				assertNull(box3.get(TAG_ID_1 + "/" + parameter));
				assertNull(box3.get(TAG_ID_2 + "/" + parameter));
				String expected = (parameter.equals(populationParameter) ? value : POPULATION.get(parameter));
				assertEquals(expected, box2.get(TAG_ID_1 + "/" + parameter));
				assertEquals(expected, box2.get(TAG_ID_2 + "/" + parameter));
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterScaledMultiplePopsWithTags_updatesDefaults() {
		for (String populationParameter : POPULATION_PARAMETER_NAMES) {
			Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
			boxes[0].add("ID", POPULATION_ID_1);
			boxes[1].add("ID", POPULATION_ID_2);
			boxes[2].add("ID", POPULATION_ID_3);
			boxes[1].addTag(TAG_ID_1, "TAG");
			boxes[1].addTag(TAG_ID_2, "TAG");
			boxes[1].addAtt(TAG_ID_1,"fraction", "0");
			boxes[1].addAtt(TAG_ID_2,"fraction", "0");
			
			double scale = randomDouble();
			boxes[1].addAtt(populationParameter, "scale", "" + scale);
			boxes[1].addTag(populationParameter, "PARAMETER");
			
			Series series = makeSeriesForPopulation(boxes);
			MiniBox box1 = series._populations.get(POPULATION_ID_1);
			MiniBox box2 = series._populations.get(POPULATION_ID_2);
			MiniBox box3 = series._populations.get(POPULATION_ID_3);
			
			for (String parameter : POPULATION_PARAMETER_NAMES) {
				assertNull(box1.get(TAG_ID_1 + "/" + parameter));
				assertNull(box1.get(TAG_ID_2 + "/" + parameter));
				assertNull(box3.get(TAG_ID_1 + "/" + parameter));
				assertNull(box3.get(TAG_ID_2 + "/" + parameter));
				double expected = (parameter.equals(populationParameter) ? scale : 1)*POPULATION.getDouble(parameter);
				assertEquals(expected, box2.getDouble(TAG_ID_1 + "/" + parameter), EPSILON);
				assertEquals(expected, box2.getDouble(TAG_ID_2 + "/" + parameter), EPSILON);
			}
		}
	}
	
	@Test
	public void updatePopulation_givenParameterValuesScaledMultiplePopsWithTags_updatesDefaults() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				boxes[1].add("ID", POPULATION_ID_2);
				boxes[2].add("ID", POPULATION_ID_3);
				boxes[1].addTag(TAG_ID_1, "TAG");
				boxes[1].addTag(TAG_ID_2, "TAG");
				boxes[1].addAtt(TAG_ID_1,"fraction", "0");
				boxes[1].addAtt(TAG_ID_2,"fraction", "0");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[1].addAtt(populationParameter1, "value", "" + value);
				boxes[1].addTag(populationParameter1, "PARAMETER");
				boxes[1].addAtt(populationParameter2, "scale", "" + scale);
				boxes[1].addTag(populationParameter2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box1 = series._populations.get(POPULATION_ID_1);
				MiniBox box2 = series._populations.get(POPULATION_ID_2);
				MiniBox box3 = series._populations.get(POPULATION_ID_3);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					assertNull(box1.get(TAG_ID_1 + "/" + parameter));
					assertNull(box1.get(TAG_ID_2 + "/" + parameter));
					assertNull(box3.get(TAG_ID_1 + "/" + parameter));
					assertNull(box3.get(TAG_ID_2 + "/" + parameter));
					
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(expected, box2.getDouble(TAG_ID_1 + "/" + parameter), EPSILON);
					assertEquals(expected, box2.getDouble(TAG_ID_2 + "/" + parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_givenTagParametersOnePop_updatesDefaults() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				boxes[0].addTag(TAG_ID_1, "TAG");
				boxes[0].addTag(TAG_ID_2, "TAG");
				boxes[0].addAtt(TAG_ID_1,"fraction", "0");
				boxes[0].addAtt(TAG_ID_2,"fraction", "0");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[0].addAtt(populationParameter1 + ":" + TAG_ID_2, "value", "" + value);
				boxes[0].addTag(populationParameter1 + ":" + TAG_ID_2, "PARAMETER");
				boxes[0].addAtt(populationParameter2 + ":" + TAG_ID_2, "scale", "" + scale);
				boxes[0].addTag(populationParameter2 + ":" + TAG_ID_2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box = series._populations.get(POPULATION_ID_1);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(POPULATION.getDouble(parameter), box.getDouble(TAG_ID_1 + "/" + parameter), EPSILON);
					assertEquals(expected, box.getDouble(TAG_ID_2 + "/" + parameter), EPSILON);
				}
			}
		}
	}
	
	@Test
	public void updatePopulation_givenTagParametersMultiplePops_updatesDefaults() {
		for (String populationParameter1 : POPULATION_PARAMETER_NAMES) {
			for (String populationParameter2 : POPULATION_PARAMETER_NAMES) {
				Box[] boxes = new Box[] { new Box(), new Box(), new Box() };
				boxes[0].add("ID", POPULATION_ID_1);
				boxes[1].add("ID", POPULATION_ID_2);
				boxes[2].add("ID", POPULATION_ID_3);
				boxes[1].addTag(TAG_ID_1, "TAG");
				boxes[1].addTag(TAG_ID_2, "TAG");
				boxes[1].addAtt(TAG_ID_1,"fraction", "0");
				boxes[1].addAtt(TAG_ID_2,"fraction", "0");
				
				double value = randomDouble();
				double scale = randomDouble();
				boxes[1].addAtt(populationParameter1 + ":" + TAG_ID_2, "value", "" + value);
				boxes[1].addTag(populationParameter1 + ":" + TAG_ID_2, "PARAMETER");
				boxes[1].addAtt(populationParameter2 + ":" + TAG_ID_2, "scale", "" + scale);
				boxes[1].addTag(populationParameter2 + ":" + TAG_ID_2, "PARAMETER");
				
				Series series = makeSeriesForPopulation(boxes);
				MiniBox box1 = series._populations.get(POPULATION_ID_1);
				MiniBox box2 = series._populations.get(POPULATION_ID_2);
				MiniBox box3 = series._populations.get(POPULATION_ID_3);
				
				for (String parameter : POPULATION_PARAMETER_NAMES) {
					assertNull(box1.get(TAG_ID_1 + "/" + parameter));
					assertNull(box1.get(TAG_ID_2 + "/" + parameter));
					assertNull(box3.get(TAG_ID_1 + "/" + parameter));
					assertNull(box3.get(TAG_ID_2 + "/" + parameter));
					
					double expected = POPULATION.getDouble(parameter);
					if (parameter.equals(populationParameter1)) { expected = value; }
					if (parameter.equals(populationParameter2)) { expected *= scale; }
					assertEquals(POPULATION.getDouble(parameter), box2.getDouble(TAG_ID_1 + "/" + parameter), EPSILON);
					assertEquals(expected, box2.getDouble(TAG_ID_2 + "/" + parameter), EPSILON);
				}
			}
		}
	}
	
}
