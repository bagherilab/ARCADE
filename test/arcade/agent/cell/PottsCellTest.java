package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.EnumSet;
import sim.engine.*;
import arcade.sim.PottsSimulation;
import arcade.env.loc.*;
import arcade.agent.module.*;
import static arcade.agent.cell.Cell.Tag;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.cell.CellFactoryTest.*;
import static arcade.sim.Potts.Term;
import static arcade.sim.Simulation.*;

public class PottsCellTest {
	private static final double EPSILON = 1E-5;
	private static final double VOLUME_SURFACE_RATIO = Math.random();
	static double lambdaVolume;
	static double lambdaSurface;
	static double adhesionTo0, adhesionTo1, adhesionTo2;
	static EnumMap<Term, Double> criticals;
	static EnumMap<Term, Double> lambdas;
	static double[] adhesion;
	static EnumMap<Tag, EnumMap<Term, Double>> criticalsTag;
	static EnumMap<Tag, EnumMap<Term, Double>> lambdasTag;
	static EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag;
	static Location location;
	static int locationVolume;
	static int locationSurface;
	static EnumMap<Tag, Integer> locationTagVolumes;
	static EnumMap<Tag, Integer> locationTagSurfaces;
	static int cellID = (int)(Math.random()*10) + 1;
	static int cellPop = (int)(Math.random()*10) + 1;
	static PottsCellMock cellDefault;
	static PottsCellMock cellWithTags;
	static PottsCellMock cellWithoutTags;
	static EnumSet<Tag> tagList;
	
	@BeforeClass
	public static void setupMocks() {
		// Random lambda values.
		lambdaVolume = Math.random();
		lambdaSurface = Math.random();
		
		// Random adhesion values.
		adhesionTo0 = Math.random();
		adhesionTo1 = Math.random();
		adhesionTo2 = Math.random();
		
		location = mock(Location.class);
		tagList = EnumSet.of(Tag.DEFAULT, Tag.NUCLEUS);
		when(location.getTags()).thenReturn(tagList);
		
		locationTagVolumes = new EnumMap<>(Tag.class);
		locationTagSurfaces = new EnumMap<>(Tag.class);
		
		// Random volumes and surfaces for tagged regions.
		for (Tag tag : tagList) {
			locationTagVolumes.put(tag, (int)(Math.random() * 100));
			locationTagSurfaces.put(tag, (int)(Math.random() * 100));
			
			when(location.getVolume(tag)).thenReturn(locationTagVolumes.get(tag));
			when(location.getSurface(tag)).thenReturn(locationTagSurfaces.get(tag));
			
			locationVolume += locationTagVolumes.get(tag);
			locationSurface += locationTagSurfaces.get(tag);
		}
		
		when(location.getVolume()).thenReturn(locationVolume);
		when(location.getSurface()).thenReturn(locationSurface);
		
		criticals = new EnumMap<>(Term.class);
		criticals.put(Term.VOLUME, (double)locationVolume);
		criticals.put(Term.SURFACE, (double)locationSurface);
		
		// Tagged region criticals.
		criticalsTag = new EnumMap<>(Tag.class);
		for (Tag tag : tagList) {
			EnumMap<Term, Double> criticalsTagTerms = new EnumMap<>(Term.class);
			criticalsTagTerms.put(Term.VOLUME, (double)locationTagVolumes.get(tag));
			criticalsTagTerms.put(Term.SURFACE, (double)locationTagSurfaces.get(tag));
			criticalsTag.put(tag, criticalsTagTerms);
		}
		
		lambdas = new EnumMap<>(Term.class);
		lambdas.put(Term.VOLUME, lambdaVolume);
		lambdas.put(Term.SURFACE, lambdaSurface);
		
		// Random lambda values for tagged regions.
		lambdasTag = new EnumMap<>(Tag.class);
		for (Tag tag : tagList) {
			EnumMap<Term, Double> lambdasTagTerms = new EnumMap<>(Term.class);
			lambdasTagTerms.put(Term.VOLUME, Math.random() * 100);
			lambdasTagTerms.put(Term.SURFACE, Math.random() * 100);
			lambdasTag.put(tag, lambdasTagTerms);
		}
		
		adhesion = new double[] { adhesionTo0, adhesionTo1, adhesionTo2 };
		
		// Random adhesion values for tagged regions.
		adhesionTag = new EnumMap<>(Tag.class);
		for (Tag tag : tagList) {
			EnumMap<Tag, Double> adhesionTagTarget = new EnumMap<>(Tag.class);
			for (Tag target : tagList) {
				adhesionTagTarget.put(target, Math.random() * 100);
			}
			adhesionTag.put(tag, adhesionTagTarget);
		}
		
		cellDefault = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		cellWithTags = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cellWithoutTags = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion);
	}
	
	public static class PottsCellMock extends PottsCell {
		public PottsCellMock(int id, int pop, Location location,
						   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion) {
			super(id, pop, location, criticals, lambdas, adhesion);
		}
		
		public PottsCellMock(int id, int pop, Location location,
						   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
						   EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
						   EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
			super(id, pop, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		}
		
		public PottsCellMock(int id, int pop, State state, int age, Location location, boolean tags,
						   EnumMap<Term, Double> criticals, EnumMap<Term, Double> lambdas, double[] adhesion,
						   EnumMap<Tag, EnumMap<Term, Double>> criticalsTag, EnumMap<Tag, EnumMap<Term, Double>> lambdasTag,
						   EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag) {
			super(id, pop, state, age, location, tags, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		}
		
		public PottsCell make(int id, State state, Location location) {
			return new PottsCellMock(id, pop, state, 0, location, hasTags,
					criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		}
		
		double convert(double volume) { return volume * VOLUME_SURFACE_RATIO; }
	}
	
	@Test
	public void getID_defaultConstructor_returnsValue() {
		assertEquals(cellID, cellDefault.getID());
	}
	
	@Test
	public void getPop_defaultConstructor_returnsValue() {
		assertEquals(cellPop, cellDefault.getPop());
	}
	
	@Test
	public void getPop_valueAssigned_returnsValue() {
		int cellPop = (int)(Math.random() * 100);
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		assertEquals(cellPop, cell.getPop());
	}
	
	@Test
	public void getState_defaultConstructor_returnsValue() {
		assertEquals(State.PROLIFERATIVE, cellDefault.getState());
	}
	
	@Test
	public void getState_valueAssigned_returnsValue() {
		State cellState = randomState();
		PottsCellMock cell = new PottsCellMock(cellID, 0, cellState, 0, location, false, criticals, lambdas, adhesion, null, null, null);
		assertEquals(cellState, cell.getState());
	}
	
	@Test
	public void getAge_defaultConstructor_returnsValue() {
		assertEquals(0, cellDefault.getAge());
	}
	
	@Test
	public void getAge_valueAssigned_returnsValue() {
		int cellAge = (int)(Math.random() * 100);
		PottsCellMock cell = new PottsCellMock(cellID, 0, State.QUIESCENT, cellAge, location, false, criticals, lambdas, adhesion, null, null, null);
		assertEquals(cellAge, cell.getAge());
	}
	
	@Test
	public void hasTags_withoutTags_returnsFalse() {
		assertFalse(cellWithoutTags.hasTags());
	}
	
	@Test
	public void hasTags_withTags_returnsTrue() {
		assertTrue(cellWithTags.hasTags());
	}
	
	@Test
	public void getLocation_defaultConstructor_returnsObject() {
		assertSame(location, cellDefault.getLocation());
	}
	
	@Test
	public void getModule_defaultConstructor_returnsObject() {
		assertTrue(cellDefault.getModule() instanceof ProliferationModule);
	}
	
	@Test
	public void getVolume_defaultConstructor_returnsValue() {
		assertEquals(locationVolume, cellDefault.getVolume());
	}
	
	@Test
	public void getVolume_validTags_returnsValue() {
		for (Tag tag : tagList) {
			assertEquals((int)locationTagVolumes.get(tag), cellWithTags.getVolume(tag));
		}
	}
	
	@Test
	public void getVolume_nullTag_returnsZero() {
		assertEquals(0, cellWithTags.getVolume(null));
	}
	
	@Test
	public void getVolume_noTags_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithoutTags.getVolume(tag));
		}
	}
	
	@Test
	public void getSurface_defaultConstructor_returnsValue() {
		assertEquals(locationSurface, cellDefault.getSurface());
	}
	
	@Test
	public void getSurface_validTags_returnsValue() {
		for (Tag tag : tagList) {
			assertEquals((int)locationTagSurfaces.get(tag), cellWithTags.getSurface(tag));
		}
	}
	
	@Test
	public void getSurface_nullTag_returnsZero() {
		assertEquals(0, cellWithTags.getSurface(null));
	}
	
	@Test
	public void getSurface_noTags_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithoutTags.getSurface(tag));
		}
	}
	
	@Test
	public void getTargetVolume_beforeInitialize_returnsZero() {
		assertEquals(0, cellDefault.getTargetVolume(), EPSILON);
	}
	
	@Test
	public void getTargetVolume_beforeInitializeValidTag_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithTags.getTargetVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetVolume_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getTargetVolume(null), EPSILON);
	}
	
	@Test
	public void getTargetVolume_beforeInitializeNoTags_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithoutTags.getTargetVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetVolume_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(locationTagVolumes.get(tag), cell.getTargetVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetVolume_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetVolume(null), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(0, cell.getTargetVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetSurface_beforeInitialize_returnsZero() {
		assertEquals(0, cellDefault.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitializeValidTag_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithTags.getTargetSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetSurface_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getTargetSurface(null), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitializeNoTags_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithoutTags.getTargetSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetSurface_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(locationTagSurfaces.get(tag), cell.getTargetSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getTargetSurface_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetSurface(null), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(0, cell.getTargetSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalVolume_beforeInitialize_returnsValue() {
		assertEquals(locationVolume, cellDefault.getCriticalVolume(), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_beforeInitializeValidTag_returnsValue() {
		for (Tag tag : tagList) {
			assertEquals(locationTagVolumes.get(tag), cellWithTags.getCriticalVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalVolume_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getCriticalVolume(null), EPSILON);
		assertEquals(0, cellWithTags.getCriticalVolume(Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_beforeInitializeNoTags_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithoutTags.getCriticalVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalVolume_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationVolume, cell.getCriticalVolume(), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(locationTagVolumes.get(tag), cell.getCriticalVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalVolume_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalVolume(null), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(0, cell.getCriticalVolume(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalSurface_beforeInitialize_returnsZero() {
		assertEquals(locationSurface, cellDefault.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_beforeInitializeValidTag_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(locationTagSurfaces.get(tag), cellWithTags.getCriticalSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalSurface_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getCriticalSurface(null), EPSILON);
		assertEquals(0, cellWithTags.getCriticalSurface(Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_beforeInitializeNoTags_returnsZero() {
		for (Tag tag : tagList) {
			assertEquals(0, cellWithoutTags.getCriticalSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalSurface_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationSurface, cell.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(locationTagSurfaces.get(tag), cell.getCriticalSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getCriticalSurface_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalSurface(null), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		for (Tag tag : tagList) {
			assertEquals(0, cell.getCriticalSurface(tag), EPSILON);
		}
	}
	
	@Test
	public void getLambda_givenTerm_returnsValue() {
		assertEquals(lambdaVolume, cellDefault.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdaSurface, cellDefault.getLambda(Term.SURFACE), EPSILON);
	}
	
	@Test
	public void getLambda_givenTermValidTags_returnsValue() {
		for (Tag tag : tagList) {
			assertEquals(lambdasTag.get(tag).get(Term.VOLUME), cellWithTags.getLambda(Term.VOLUME, tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.SURFACE), cellWithTags.getLambda(Term.SURFACE, tag), EPSILON);
		}
	}
	
	@Test
	public void getLambda_givenTermInvalidTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithTags.getLambda(Term.VOLUME, null), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getLambda(Term.SURFACE, null), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getLambda(Term.VOLUME, Tag.UNDEFINED), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getLambda(Term.SURFACE, Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void getLambda_givenTermNoTags_returnsNaN() {
		for (Tag tag : tagList) {
			assertEquals(Double.NaN, cellWithoutTags.getLambda(Term.VOLUME, tag), EPSILON);
			assertEquals(Double.NaN, cellWithoutTags.getLambda(Term.SURFACE, tag), EPSILON);
		}
	}
	
	@Test
	public void getAdhesion_givenPop_returnsValue() {
		assertEquals(adhesionTo0, cellDefault.getAdhesion(0), EPSILON);
		assertEquals(adhesionTo1, cellDefault.getAdhesion(1), EPSILON);
		assertEquals(adhesionTo2, cellDefault.getAdhesion(2), EPSILON);
	}
	
	@Test
	public void getAdhesion_validTags_returnsValue() {
		for (Tag tag : tagList) {
			for (Tag target : tagList) {
				assertEquals(adhesionTag.get(tag).get(target), cellWithTags.getAdhesion(tag, target), EPSILON);
			}
		}
	}
	
	@Test
	public void getAdhesion_nullTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithTags.getAdhesion(null, null), EPSILON);
		for (Tag tag : tagList) {
			assertEquals(Double.NaN, cellWithTags.getAdhesion(tag, null), EPSILON);
			assertEquals(Double.NaN, cellWithTags.getAdhesion(null, tag), EPSILON);
		}
	}
	
	@Test
	public void getAdhesion_invalidTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithTags.getAdhesion(Tag.UNDEFINED, Tag.UNDEFINED), EPSILON);
		for (Tag tag : tagList) {
			assertEquals(Double.NaN, cellWithTags.getAdhesion(tag, Tag.UNDEFINED), EPSILON);
			assertEquals(Double.NaN, cellWithTags.getAdhesion(Tag.UNDEFINED, tag), EPSILON);
		}
	}
	
	@Test
	public void getAdhesion_noTags_returnsNaN() {
		for (Tag tag : tagList) {
			for (Tag target : tagList) {
				assertEquals(Double.NaN, cellWithoutTags.getAdhesion(tag, target), EPSILON);
			}
		}
	}
	
	@Test
	public void setState_givenState_assignsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		
		cell.setState(State.QUIESCENT);
		assertEquals(State.QUIESCENT, cell.getState());
		
		cell.setState(State.PROLIFERATIVE);
		assertEquals(State.PROLIFERATIVE, cell.getState());
		
		cell.setState(State.APOPTOTIC);
		assertEquals(State.APOPTOTIC, cell.getState());
		
		cell.setState(State.NECROTIC);
		assertEquals(State.NECROTIC, cell.getState());
		
		cell.setState(State.AUTOTIC);
		assertEquals(State.AUTOTIC, cell.getState());
	}
	
	@Test
	public void setState_givenState_updatesModule() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, criticals, lambdas, adhesion);
		
		cell.setState(State.QUIESCENT);
		assertTrue(cell.module instanceof QuiescenceModule);
		
		cell.setState(State.PROLIFERATIVE);
		assertTrue(cell.module instanceof ProliferationModule);
		
		cell.setState(State.APOPTOTIC);
		assertTrue(cell.module instanceof ApoptosisModule);
		
		cell.setState(State.NECROTIC);
		assertTrue(cell.module instanceof NecrosisModule);
		
		cell.setState(State.AUTOTIC);
		assertTrue(cell.module instanceof AutosisModule);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void setState_invalidState_throwsException() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.setState(State.UNDEFINED);
	}
	
	@Test
	public void schedule_validInput_callsMethod() {
		Schedule schedule = spy(mock(Schedule.class));
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, ORDERING_CELLS, 1);
		cell.schedule(schedule);
		verify(schedule).scheduleRepeating(cell, ORDERING_CELLS, 1);
	}
	
	@Test
	public void schedule_validInput_assignStopper() {
		Schedule schedule = spy(mock(Schedule.class));
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		doReturn(mock(Stoppable.class)).when(schedule).scheduleRepeating(cell, ORDERING_CELLS, 1);
		cell.schedule(schedule);
		assertNotNull(cell.stopper);
	}
	
	@Test
	public void initialize_withoutTags_callsMethod() {
		Location location = spy(mock(Location.class));
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array, null);
		
		verify(location).update(cellID, array, null);
	}
	
	@Test
	public void initialize_withTags_callsMethod() {
		Location location = spy(mock(Location.class));
		when(location.getTags()).thenReturn(tagList);
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		int[][][] array1 = new int[1][3][3];
		int[][][] array2 = new int[1][3][3];
		cell.initialize(array1, array2);
		
		verify(location).update(cellID, array1, array2);
	}
	
	@Test
	public void initialize_withoutTags_updatesTargets() {
		int volume = (int)(Math.random()*100);
		int surface = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume);
		when(location.getSurface()).thenReturn(surface);
		
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		
		assertEquals(volume, cell.getTargetVolume(), EPSILON);
		assertEquals(surface, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void initialize_withTags_updatesTargets() {
		int volume1 = (int)(Math.random()*100);
		int volume2 = (int)(Math.random()*100);
		int surface1 = (int)(Math.random()*100);
		int surface2 = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume1 + volume2);
		when(location.getSurface()).thenReturn(surface1 + surface2);
		when(location.getVolume(Tag.DEFAULT)).thenReturn(volume1);
		when(location.getSurface(Tag.DEFAULT)).thenReturn(surface1);
		when(location.getVolume(Tag.NUCLEUS)).thenReturn(volume2);
		when(location.getSurface(Tag.NUCLEUS)).thenReturn(surface2);
		when(location.getTags()).thenReturn(tagList);
		
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(volume1 + volume2, cell.getTargetVolume(), EPSILON);
		assertEquals(surface1 + surface2, cell.getTargetSurface(), EPSILON);
		assertEquals(volume1, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(surface1, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		assertEquals(volume2, cell.getTargetVolume(Tag.NUCLEUS), EPSILON);
		assertEquals(surface2, cell.getTargetSurface(Tag.NUCLEUS), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.UNDEFINED), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void initialize_targetsSetWithoutTags_doesNothing() {
		int volume = (int)(Math.random()*100);
		int surface = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume);
		when(location.getSurface()).thenReturn(surface);
		
		int targetVolume = (int)(Math.random()*100) + 1;
		int targetSurface = (int)(Math.random()*100) + 1;
		
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.setTargets(targetVolume, targetSurface);
		cell.initialize(new int[1][3][3], null);
		
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void initialize_targetsSetWithTags_updatesTargets() {
		int volume1 = (int)(Math.random()*100);
		int volume2 = (int)(Math.random()*100);
		int surface1 = (int)(Math.random()*100);
		int surface2 = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume1 + volume2);
		when(location.getSurface()).thenReturn(surface1 + surface2);
		when(location.getVolume(Tag.DEFAULT)).thenReturn(volume1);
		when(location.getSurface(Tag.DEFAULT)).thenReturn(surface1);
		when(location.getVolume(Tag.NUCLEUS)).thenReturn(volume2);
		when(location.getSurface(Tag.NUCLEUS)).thenReturn(surface2);
		when(location.getTags()).thenReturn(tagList);
		
		int targetVolume = (int)(Math.random()*100) + 1;
		int targetSurface = (int)(Math.random()*100) + 1;
		int targetTagVolume1 = (int)(Math.random()*100) + 1;
		int targetTagSurface1 = (int)(Math.random()*100) + 1;
		int targetTagVolume2 = (int)(Math.random()*100) + 1;
		int targetTagSurface2 = (int)(Math.random()*100) + 1;
		
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.setTargets(targetVolume, targetSurface);
		cell.setTargets(Tag.DEFAULT, targetTagVolume1, targetTagSurface1);
		cell.setTargets(Tag.NUCLEUS, targetTagVolume2, targetTagSurface2);
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		assertEquals(targetTagVolume1, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(targetTagSurface1, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		assertEquals(targetTagVolume2, cell.getTargetVolume(Tag.NUCLEUS), EPSILON);
		assertEquals(targetTagSurface2, cell.getTargetSurface(Tag.NUCLEUS), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.UNDEFINED), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.UNDEFINED), EPSILON);
	}
	
	@Test
	public void initialize_targetsMixed_updatesTargets() {
		int volume = (int)(Math.random()*100);
		int surface = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume);
		when(location.getSurface()).thenReturn(surface);
		
		PottsCellMock cell1 = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell1.setTargets(0, (int)(Math.random()*100));
		cell1.initialize(new int[1][3][3], null);
		
		assertEquals(volume, cell1.getTargetVolume(), EPSILON);
		assertEquals(surface, cell1.getTargetSurface(), EPSILON);
		
		PottsCellMock cell2 = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell2.setTargets((int)(Math.random()*100), 0);
		cell2.initialize(new int[1][3][3], null);
		
		assertEquals(volume, cell2.getTargetVolume(), EPSILON);
		assertEquals(surface, cell2.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void reset_withoutTags_callsMethod() {
		Location location = spy(mock(Location.class));
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array, null);
		cell.reset(array, null);
		
		verify(location, times(2)).update(cellID, array, null);
	}
	
	@Test
	public void reset_withTags_callsMethod() {
		Location location = spy(mock(Location.class));
		when(location.getTags()).thenReturn(tagList);
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		int[][][] array1 = new int[1][3][3];
		int[][][] array2 = new int[1][3][3];
		cell.initialize(array1, array2);
		cell.reset(array1, array2);
		
		verify(location, times(2)).update(cellID, array1, array2);
	}
	
	@Test
	public void reset_withoutTags_updatesTargets() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		cell.updateTarget(Math.random(), Math.random());
		cell.reset(new int[1][3][3], null);
		
		assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
		assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void reset_withTags_updatesTargets() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		cell.updateTarget(Tag.DEFAULT, Math.random(), Math.random());
		cell.updateTarget(Tag.NUCLEUS, Math.random(), Math.random());
		cell.reset(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
		assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
		assertEquals(locationTagVolumes.get(Tag.DEFAULT), cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(locationTagSurfaces.get(Tag.DEFAULT), cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		assertEquals(locationTagVolumes.get(Tag.NUCLEUS), cell.getTargetVolume(Tag.NUCLEUS), EPSILON);
		assertEquals(locationTagSurfaces.get(Tag.NUCLEUS), cell.getTargetSurface(Tag.NUCLEUS), EPSILON);
	}
	
	@Test
	public void step_singleStep_updatesAge() {
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		PottsSimulation sim = mock(PottsSimulation.class);
		cell.module = mock(Module.class);
		
		cell.step(sim);
		assertEquals(1, cell.getAge(), EPSILON);
	}
	
	@Test
	public void setTargets_noTags_updateValues() {
		double targetVolume = Math.random();
		double targetSurface = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, cellPop,
				location, criticals, lambdas, adhesion);
		cell.setTargets(targetVolume, targetSurface);
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void setTargets_withTags_updateValues() {
		double targetVolume = Math.random();
		double targetSurface = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 1,
				location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.setTargets(Tag.NUCLEUS, targetVolume, targetSurface);
		
		assertEquals(targetVolume, cell.getTargetVolume(Tag.NUCLEUS), EPSILON);
		assertEquals(targetSurface, cell.getTargetSurface(Tag.NUCLEUS), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.UNDEFINED), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.UNDEFINED), EPSILON);
		assertEquals(0, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoNoTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, cellPop,
				location, criticals, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + rate*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoWithTags_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 1,
				location, criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + rate*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) - criticals.get(Term.VOLUME) + targetVolume;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroNoTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + rate*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroWithTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + rate*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) - criticals.get(Term.VOLUME) + targetVolume;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoNoTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 2);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + (rate + delta)*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoWithTagsModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 2);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + (rate + delta)*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) - criticals.get(Term.VOLUME) + targetVolume;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroNoTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location,
				criticals, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 0);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + (rate + delta)*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroWithTagModified_updatesValues() {
		double rate = Math.random() + 1;
		double delta = Math.random() + 1;
		PottsCellMock cell = new PottsCellMock(cellID, 1, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 0);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + (rate + delta)*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) - criticals.get(Term.VOLUME) + targetVolume;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleTwoWithTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(Tag.DEFAULT, rate, 2);
		
		double targetTagVolume = locationTagVolumes.get(Tag.DEFAULT) + rate*(locationTagVolumes.get(Tag.DEFAULT))*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		
		double targetVolume = criticals.get(Term.VOLUME) - criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) + targetTagVolume;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleZeroWithTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(Tag.DEFAULT, rate, 0);
		
		double targetTagVolume = locationTagVolumes.get(Tag.DEFAULT) + rate*(-locationTagVolumes.get(Tag.DEFAULT))*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		
		double targetVolume = criticals.get(Term.VOLUME) - criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) + targetTagVolume;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleTwoWithTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(Tag.DEFAULT,rate + delta, 2);
		cell.updateTarget(Tag.DEFAULT, rate, 2);
		
		double targetTagVolume = locationTagVolumes.get(Tag.DEFAULT) + (rate + delta)*(locationTagVolumes.get(Tag.DEFAULT))*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		
		double targetVolume = criticals.get(Term.VOLUME) - criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) + targetTagVolume;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleZeroWithTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(Tag.DEFAULT,rate + delta, 0);
		cell.updateTarget(Tag.DEFAULT,rate, 0);
		
		double targetTagVolume = locationTagVolumes.get(Tag.DEFAULT) + (rate + delta)*(-locationTagVolumes.get(Tag.DEFAULT))*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(Tag.DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(Tag.DEFAULT), EPSILON);
		
		double targetVolume = criticals.get(Term.VOLUME) - criticalsTag.get(Tag.DEFAULT).get(Term.VOLUME) + targetTagVolume;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
}
