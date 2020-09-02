package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import sim.engine.*;
import arcade.sim.PottsSimulation;
import arcade.env.loc.*;
import arcade.agent.module.*;
import static arcade.agent.cell.Cell.*;
import static arcade.sim.Potts.*;
import static arcade.sim.Simulation.*;

public class PottsCellTest {
	private static final double EPSILON = 1E-5;
	private static final int TAG_ADDITIONAL = TAG_DEFAULT - 1;
	private static final double VOLUME_SURFACE_RATIO = Math.random();
	double lambdaVolume;
	double lambdaSurface;
	double adhesionTo0, adhesionTo1, adhesionTo2;
	double[] lambdas;
	double[] adhesion;
	double[][] lambdasTag;
	double[][] adhesionTag;
	Location location;
	int locationVolume;
	int locationSurface;
	int[] locationTagVolumes;
	int[] locationTagSurfaces;
	int cellID = (int)(Math.random()*10) + 1;
	int cellPop = (int)(Math.random()*10) + 1;
	int tags = 3;
	PottsCellMock cellDefault;
	PottsCellMock cellWithTags;
	PottsCellMock cellWithoutTags;
	
	@Before
	public void setupMocks() {
		// Random lambda values.
		lambdaVolume = Math.random();
		lambdaSurface = Math.random();
		
		// Random adhesion values.
		adhesionTo0 = Math.random();
		adhesionTo1 = Math.random();
		adhesionTo2 = Math.random();
		
		location = mock(Location.class);
		
		locationTagVolumes = new int[tags];
		locationTagSurfaces = new int[tags];
		
		// Random volumes and surfaces for tagged regions.
		for (int i = 0; i < tags; i++) {
			int tag = -i - 1;
			locationTagVolumes[i] = (int)(Math.random() * 100);
			locationTagSurfaces[i] = (int)(Math.random() * 100);
			when(location.getVolume(tag)).thenReturn(locationTagVolumes[i]);
			when(location.getSurface(tag)).thenReturn(locationTagSurfaces[i]);
			
			locationVolume += locationTagVolumes[i];
			locationSurface += locationTagSurfaces[i];
		}
		
		when(location.getVolume()).thenReturn(locationVolume);
		when(location.getSurface()).thenReturn(locationSurface);
		
		lambdas = new double[2];
		lambdas[LAMBDA_VOLUME] = lambdaVolume;
		lambdas[LAMBDA_SURFACE] = lambdaSurface;
		
		// Random lambda values for tagged regions.
		lambdasTag = new double[2][tags];
		for (int i = 0; i < tags; i++) {
			lambdasTag[LAMBDA_VOLUME][i] = (int)(Math.random() * 100);
			lambdasTag[LAMBDA_SURFACE][i] = (int)(Math.random() * 100);
		}
		
		adhesion = new double[] { adhesionTo0, adhesionTo1, adhesionTo2 };
		
		// Random adhesion values for tagged regions.
		adhesionTag = new double[tags][tags];
		for (int i = 0; i < tags; i++) {
			for (int j = 0; j < tags; j++) {
				adhesionTag[i][j] = (int)(Math.random() * 100);
			}
		}
		
		cellDefault = new PottsCellMock(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		cellWithTags = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cellWithoutTags = new PottsCellMock(cellID, 1, location, lambdas, adhesion, 0, null, null);
	}
	
	public static class PottsCellMock extends PottsCell {
		public PottsCellMock(int id, Location location,
							 double[] lambdas, double[] adhesion) {
			super(id, location, lambdas, adhesion);
		}
		
		public PottsCellMock(int id, int pop, Location location,
							 double[] lambdas, double[] adhesion, int tags,
							 double[][] lambdasTag, double[][] adhesionsTag) {
			super(id, pop, location, lambdas, adhesion, tags, lambdasTag, adhesionsTag);
		}
		
		public PottsCellMock(int id, int pop, int state, int age, Location location,
							 double[] lambdas, double[] adhesion, int tags,
							 double[][] lambdasTag, double[][] adhesionsTag) {
			super(id, pop, state, age, location, lambdas, adhesion, tags, lambdasTag, adhesionsTag);
		}
		
		PottsCell makeCell(int id, int state, Location location) {
			return new PottsCellMock(id, pop, state, 0, location,
					lambdas, adhesion, tags, lambdasTag, adhesionTag);
		}
		
		@Override
		double convert(double volume) { return volume *= VOLUME_SURFACE_RATIO; }
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
		PottsCellMock cell = new PottsCellMock(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		assertEquals(cellPop, cell.getPop());
	}
	
	@Test
	public void getState_defaultConstructor_returnsValue() {
		assertEquals(STATE_PROLIFERATIVE, cellDefault.getState());
	}
	
	@Test
	public void getState_valueAssigned_returnsValue() {
		int cellState = STATE_QUIESCENT;
		PottsCellMock cell = new PottsCellMock(cellID, 0, cellState, 0, location, lambdas, adhesion, 0, null, null);
		assertEquals(cellState, cell.getState());
	}
	
	@Test
	public void getAge_defaultConstructor_returnsValue() {
		assertEquals(0, cellDefault.getAge());
	}
	
	@Test
	public void getAge_valueAssigned_returnsValue() {
		int cellAge = (int)(Math.random() * 100);
		PottsCellMock cell = new PottsCellMock(cellID, 0, 0, cellAge, location, lambdas, adhesion, 0, null, null);
		assertEquals(cellAge, cell.getAge());
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
		assertEquals(locationTagVolumes[0], cellWithTags.getVolume(-1));
		assertEquals(locationTagVolumes[1], cellWithTags.getVolume(-2));
		assertEquals(locationTagVolumes[2], cellWithTags.getVolume(-3));
	}
	
	@Test
	public void getVolume_invalidTags_returnsZero() {
		assertEquals(0, cellWithTags.getVolume(0));
		assertEquals(0, cellWithTags.getVolume(-4));
	}
	
	@Test
	public void getVolume_noTags_returnsZero() {
		assertEquals(0, cellWithoutTags.getVolume(-1));
		assertEquals(0, cellWithoutTags.getVolume(-2));
		assertEquals(0, cellWithoutTags.getVolume(-3));
	}
	
	@Test
	public void getSurface_defaultConstructor_returnsValue() {
		assertEquals(locationSurface, cellDefault.getSurface());
	}
	
	@Test
	public void getSurface_validTags_returnsValue() {
		assertEquals(locationTagSurfaces[0], cellWithTags.getSurface(-1));
		assertEquals(locationTagSurfaces[1], cellWithTags.getSurface(-2));
		assertEquals(locationTagSurfaces[2], cellWithTags.getSurface(-3));
	}
	
	@Test
	public void getSurface_invalidTags_returnsZero() {
		assertEquals(0, cellWithTags.getSurface(0));
		assertEquals(0, cellWithTags.getSurface(-4));
	}
	
	@Test
	public void getSurface_noTags_returnsZero() {
		assertEquals(0, cellWithoutTags.getSurface(-1));
		assertEquals(0, cellWithoutTags.getSurface(-2));
		assertEquals(0, cellWithoutTags.getSurface(-3));
	}
	
	@Test
	public void getTargetVolume_beforeInitialize_returnsZero() {
		assertEquals(0, cellDefault.getTargetVolume(), EPSILON);
	}
	
	@Test
	public void getTargetVolume_beforeInitializeValidTag_returnsZero() {
		assertEquals(0, cellWithTags.getTargetVolume(-1), EPSILON);
		assertEquals(0, cellWithTags.getTargetVolume(-2), EPSILON);
		assertEquals(0, cellWithTags.getTargetVolume(-3), EPSILON);
	}
	
	@Test
	public void getTargetVolume_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getTargetVolume(0), EPSILON);
		assertEquals(0, cellWithTags.getTargetVolume(-4), EPSILON);
	}
	
	@Test
	public void getTargetVolume_beforeInitializeNoTags_returnsZero() {
		assertEquals(0, cellWithoutTags.getTargetVolume(-1), EPSILON);
		assertEquals(0, cellWithoutTags.getTargetVolume(-2), EPSILON);
		assertEquals(0, cellWithoutTags.getTargetVolume(-3), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagVolumes[0], cell.getTargetVolume(-1), EPSILON);
		assertEquals(locationTagVolumes[1], cell.getTargetVolume(-2), EPSILON);
		assertEquals(locationTagVolumes[2], cell.getTargetVolume(-3), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetVolume(0), EPSILON);
		assertEquals(0, cell.getTargetVolume(-4), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetVolume(-1), EPSILON);
		assertEquals(0, cell.getTargetVolume(-2), EPSILON);
		assertEquals(0, cell.getTargetVolume(-3), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitialize_returnsZero() {
		assertEquals(0, cellDefault.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitializeValidTag_returnsZero() {
		assertEquals(0, cellWithTags.getTargetSurface(-1), EPSILON);
		assertEquals(0, cellWithTags.getTargetSurface(-2), EPSILON);
		assertEquals(0, cellWithTags.getTargetSurface(-3), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getTargetSurface(0), EPSILON);
		assertEquals(0, cellWithTags.getTargetSurface(-4), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitializeNoTags_returnsZero() {
		assertEquals(0, cellWithoutTags.getTargetSurface(-1), EPSILON);
		assertEquals(0, cellWithoutTags.getTargetSurface(-2), EPSILON);
		assertEquals(0, cellWithoutTags.getTargetSurface(-3), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagSurfaces[0], cell.getTargetSurface(-1), EPSILON);
		assertEquals(locationTagSurfaces[1], cell.getTargetSurface(-2), EPSILON);
		assertEquals(locationTagSurfaces[2], cell.getTargetSurface(-3), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetSurface(0), EPSILON);
		assertEquals(0, cell.getTargetSurface(-4), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetSurface(-1), EPSILON);
		assertEquals(0, cell.getTargetSurface(-2), EPSILON);
		assertEquals(0, cell.getTargetSurface(-3), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_beforeInitialize_returnsZero() {
		assertEquals(0, cellDefault.getCriticalVolume(), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_beforeInitializeValidTag_returnsZero() {
		assertEquals(0, cellWithTags.getCriticalVolume(-1), EPSILON);
		assertEquals(0, cellWithTags.getCriticalVolume(-2), EPSILON);
		assertEquals(0, cellWithTags.getCriticalVolume(-3), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getCriticalVolume(0), EPSILON);
		assertEquals(0, cellWithTags.getCriticalVolume(-4), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_beforeInitializeNoTags_returnsZero() {
		assertEquals(0, cellWithoutTags.getCriticalVolume(-1), EPSILON);
		assertEquals(0, cellWithoutTags.getCriticalVolume(-2), EPSILON);
		assertEquals(0, cellWithoutTags.getCriticalVolume(-3), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationVolume, cell.getCriticalVolume(), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagVolumes[0], cell.getCriticalVolume(-1), EPSILON);
		assertEquals(locationTagVolumes[1], cell.getCriticalVolume(-2), EPSILON);
		assertEquals(locationTagVolumes[2], cell.getCriticalVolume(-3), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalVolume(0), EPSILON);
		assertEquals(0, cell.getCriticalVolume(-4), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalVolume(-1), EPSILON);
		assertEquals(0, cell.getCriticalVolume(-2), EPSILON);
		assertEquals(0, cell.getCriticalVolume(-3), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_beforeInitialize_returnsZero() {
		assertEquals(0, cellDefault.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_beforeInitializeValidTag_returnsZero() {
		assertEquals(0, cellWithTags.getCriticalSurface(-1), EPSILON);
		assertEquals(0, cellWithTags.getCriticalSurface(-2), EPSILON);
		assertEquals(0, cellWithTags.getCriticalSurface(-3), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_beforeInitializeInvalidTag_returnsZero() {
		assertEquals(0, cellWithTags.getCriticalSurface(0), EPSILON);
		assertEquals(0, cellWithTags.getCriticalSurface(-4), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_beforeInitializeNoTags_returnsZero() {
		assertEquals(0, cellWithoutTags.getCriticalSurface(-1), EPSILON);
		assertEquals(0, cellWithoutTags.getCriticalSurface(-2), EPSILON);
		assertEquals(0, cellWithoutTags.getCriticalSurface(-3), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitialize_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationSurface, cell.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeValidTag_returnsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagSurfaces[0], cell.getCriticalSurface(-1), EPSILON);
		assertEquals(locationTagSurfaces[1], cell.getCriticalSurface(-2), EPSILON);
		assertEquals(locationTagSurfaces[2], cell.getCriticalSurface(-3), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeInvalidTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalSurface(0), EPSILON);
		assertEquals(0, cell.getCriticalSurface(-4), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeNoTag_returnsZero() {
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalSurface(-1), EPSILON);
		assertEquals(0, cell.getCriticalSurface(-2), EPSILON);
		assertEquals(0, cell.getCriticalSurface(-3), EPSILON);
	}
	
	@Test
	public void getLambda_givenTerm_returnsValue() {
		assertEquals(lambdaVolume, cellDefault.getLambda(LAMBDA_VOLUME), EPSILON);
		assertEquals(lambdaSurface, cellDefault.getLambda(LAMBDA_SURFACE), EPSILON);
	}
	
	@Test
	public void getLambda_givenTermValidTags_returnsValue() {
		assertEquals(lambdasTag[LAMBDA_VOLUME][0], cellWithTags.getLambda(LAMBDA_VOLUME, -1), EPSILON);
		assertEquals(lambdasTag[LAMBDA_SURFACE][0], cellWithTags.getLambda(LAMBDA_SURFACE, -1), EPSILON);
		assertEquals(lambdasTag[LAMBDA_VOLUME][1], cellWithTags.getLambda(LAMBDA_VOLUME, -2), EPSILON);
		assertEquals(lambdasTag[LAMBDA_SURFACE][1], cellWithTags.getLambda(LAMBDA_SURFACE, -2), EPSILON);
		assertEquals(lambdasTag[LAMBDA_VOLUME][2], cellWithTags.getLambda(LAMBDA_VOLUME, -3), EPSILON);
		assertEquals(lambdasTag[LAMBDA_SURFACE][2], cellWithTags.getLambda(LAMBDA_SURFACE, -3), EPSILON);
	}
	
	@Test
	public void getLambda_givenTermInvalidTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithTags.getLambda(LAMBDA_VOLUME, 0), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getLambda(LAMBDA_SURFACE, 0), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getLambda(LAMBDA_VOLUME, -4), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getLambda(LAMBDA_SURFACE, -4), EPSILON);
	}
	
	@Test
	public void getLambda_givenTermNoTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithoutTags.getLambda(LAMBDA_VOLUME, -1), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getLambda(LAMBDA_SURFACE, -1), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getLambda(LAMBDA_VOLUME, -2), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getLambda(LAMBDA_SURFACE, -2), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getLambda(LAMBDA_VOLUME, -3), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getLambda(LAMBDA_SURFACE, -3), EPSILON);
	}
	
	@Test
	public void getAdhesion_givenPop_returnsValue() {
		assertEquals(adhesionTo0, cellDefault.getAdhesion(0), EPSILON);
		assertEquals(adhesionTo1, cellDefault.getAdhesion(1), EPSILON);
		assertEquals(adhesionTo2, cellDefault.getAdhesion(2), EPSILON);
	}
	
	@Test
	public void getAdhesion_validTags_returnsValue() {
		assertEquals(adhesionTag[0][0], cellWithTags.getAdhesion(-1, -1), EPSILON);
		assertEquals(adhesionTag[0][1], cellWithTags.getAdhesion(-1, -2), EPSILON);
		assertEquals(adhesionTag[0][2], cellWithTags.getAdhesion(-1, -3), EPSILON);
		assertEquals(adhesionTag[1][0], cellWithTags.getAdhesion(-2, -1), EPSILON);
		assertEquals(adhesionTag[1][1], cellWithTags.getAdhesion(-2, -2), EPSILON);
		assertEquals(adhesionTag[1][2], cellWithTags.getAdhesion(-2, -3), EPSILON);
		assertEquals(adhesionTag[2][0], cellWithTags.getAdhesion(-3, -1), EPSILON);
		assertEquals(adhesionTag[2][1], cellWithTags.getAdhesion(-3, -2), EPSILON);
		assertEquals(adhesionTag[2][2], cellWithTags.getAdhesion(-3, -3), EPSILON);
	}
	
	@Test
	public void getAdhesion_invalidTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithTags.getAdhesion(-1, 0), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getAdhesion(-1, -4), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getAdhesion(0, -1), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getAdhesion(-4, -1), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getAdhesion(0, 0), EPSILON);
		assertEquals(Double.NaN, cellWithTags.getAdhesion(-4, -4), EPSILON);
	}
	
	@Test
	public void getAdhesion_noTags_returnsNaN() {
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-1, -1), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-1, -2), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-1, -3), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-2, -1), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-2, -2), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-2, -3), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-3, -1), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-3, -2), EPSILON);
		assertEquals(Double.NaN, cellWithoutTags.getAdhesion(-3, -3), EPSILON);
	}
	
	@Test
	public void setState_givenState_assignsValue() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		
		cell.setState(STATE_QUIESCENT);
		assertEquals(STATE_QUIESCENT, cell.getState());
		
		cell.setState(STATE_PROLIFERATIVE);
		assertEquals(STATE_PROLIFERATIVE, cell.getState());
		
		cell.setState(STATE_APOPTOTIC);
		assertEquals(STATE_APOPTOTIC, cell.getState());
		
		cell.setState(STATE_NECROTIC);
		assertEquals(STATE_NECROTIC, cell.getState());
		
		cell.setState(STATE_AUTOTIC);
		assertEquals(STATE_AUTOTIC, cell.getState());
	}
	
	@Test
	public void setState_givenState_updatesModule() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		
		cell.setState(STATE_QUIESCENT);
		assertTrue(cell.module instanceof QuiescenceModule);
		
		cell.setState(STATE_PROLIFERATIVE);
		assertTrue(cell.module instanceof ProliferationModule);
		
		cell.setState(STATE_APOPTOTIC);
		assertTrue(cell.module instanceof ApoptosisModule);
		
		cell.setState(STATE_NECROTIC);
		assertTrue(cell.module instanceof NecrosisModule);
		
		cell.setState(STATE_AUTOTIC);
		assertTrue(cell.module instanceof AutosisModule);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void setState_invalidState_throwsException() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.setState(-1);
	}
	
	@Test
	public void make_givenCell_setsFields() {
		int id = (int)(Math.random()*100) + cellID;
		Cell cell1 = new PottsCellMock(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		Cell cell2 = cell1.make(id, STATE_APOPTOTIC, null);
		assertEquals(id, cell2.getID());
		assertEquals(cellPop, cell2.getPop());
		assertEquals(STATE_APOPTOTIC, cell2.getState());
		assertEquals(0, cell2.getAge());
	}
	
	@Test
	public void make_withoutTags_updatesValues() {
		int volume = (int)(Math.random()*100);
		int surface = (int)(Math.random()*100);
		
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume);
		when(location.getSurface()).thenReturn(surface);
		
		PottsCellMock cell1 = new PottsCellMock(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		cell1.initialize(new int[1][3][3], new int[1][3][3]);
		Cell cell2 = cell1.make(cellID + 1, STATE_QUIESCENT, null);
		
		assertEquals(cell1.getCriticalVolume(), cell2.getCriticalVolume(), EPSILON);
		assertEquals(cell1.getCriticalSurface(), cell2.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void make_withTags_updatesValues() {
		int volume1 = (int)(Math.random()*100);
		int volume2 = (int)(Math.random()*100);
		int surface1 = (int)(Math.random()*100);
		int surface2 = (int)(Math.random()*100);
		
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume1 + volume2);
		when(location.getVolume(TAG_DEFAULT)).thenReturn(volume1);
		when(location.getVolume(TAG_ADDITIONAL)).thenReturn(volume2);
		when(location.getSurface()).thenReturn(surface1 + surface2);
		when(location.getSurface(TAG_DEFAULT)).thenReturn(surface1);
		when(location.getSurface(TAG_ADDITIONAL)).thenReturn(surface2);
		
		PottsCellMock cell1 = new PottsCellMock(cellID, cellPop, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell1.initialize(new int[1][3][3], new int[1][3][3]);
		Cell cell2 = cell1.make(cellID + 1, STATE_QUIESCENT, null);
		
		assertEquals(cell1.getCriticalVolume(TAG_DEFAULT), cell2.getCriticalVolume(TAG_DEFAULT), EPSILON);
		assertEquals(cell1.getCriticalVolume(TAG_ADDITIONAL), cell2.getCriticalVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(cell1.getCriticalSurface(TAG_DEFAULT), cell2.getCriticalSurface(TAG_DEFAULT), EPSILON);
		assertEquals(cell1.getCriticalSurface(TAG_ADDITIONAL), cell2.getCriticalSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void schedule_validInput_callsMethod() {
		Schedule schedule = spy(mock(Schedule.class));
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.schedule(schedule);
		
		verify(schedule).scheduleRepeating(cell, ORDERING_CELLS, 1);
	}
	
	@Test
	public void schedule_validInput_assignStopper() {
		Schedule schedule = spy(mock(Schedule.class));
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		when(schedule.scheduleRepeating(cell, ORDERING_CELLS, 1)).thenReturn(mock(Stoppable.class));
		cell.schedule(schedule);
		assertNotNull(cell.stopper);
	}
	
	@Test
	public void initialize_withoutTags_callsMethod() {
		Location location = spy(mock(Location.class));
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array, null);
		
		verify(location).update(cellID, array, null);
	}
	
	@Test
	public void initialize_withTags_callsMethod() {
		Location location = spy(mock(Location.class));
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
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
		
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		
		assertEquals(volume, cell.getTargetVolume(), EPSILON);
		assertEquals(surface, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
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
		when(location.getVolume(TAG_DEFAULT)).thenReturn(volume1);
		when(location.getSurface(TAG_DEFAULT)).thenReturn(surface1);
		when(location.getVolume(TAG_ADDITIONAL)).thenReturn(volume2);
		when(location.getSurface(TAG_ADDITIONAL)).thenReturn(surface2);
		
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(volume1 + volume2, cell.getTargetVolume(), EPSILON);
		assertEquals(surface1 + surface2, cell.getTargetSurface(), EPSILON);
		assertEquals(volume1, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(surface1, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		assertEquals(volume2, cell.getTargetVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(surface2, cell.getTargetSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void initialize_withoutTags_updatesCriticals() {
		int volume = (int)(Math.random()*100);
		int surface = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume);
		when(location.getSurface()).thenReturn(surface);
		
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		
		assertEquals(volume, cell.getCriticalVolume(), EPSILON);
		assertEquals(surface, cell.getCriticalSurface(), EPSILON);
		assertEquals(0, cell.getCriticalVolume(TAG_DEFAULT), EPSILON);
		assertEquals(0, cell.getCriticalSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void initialize_withTags_updatesCriticals() {
		int volume1 = (int)(Math.random()*100);
		int volume2 = (int)(Math.random()*100);
		int surface1 = (int)(Math.random()*100);
		int surface2 = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume1 + volume2);
		when(location.getSurface()).thenReturn(surface1 + surface2);
		when(location.getVolume(TAG_DEFAULT)).thenReturn(volume1);
		when(location.getSurface(TAG_DEFAULT)).thenReturn(surface1);
		when(location.getVolume(TAG_ADDITIONAL)).thenReturn(volume2);
		when(location.getSurface(TAG_ADDITIONAL)).thenReturn(surface2);
		
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(volume1 + volume2, cell.getCriticalVolume(), EPSILON);
		assertEquals(surface1 + surface2, cell.getCriticalSurface(), EPSILON);
		assertEquals(volume1, cell.getCriticalVolume(TAG_DEFAULT), EPSILON);
		assertEquals(surface1, cell.getCriticalSurface(TAG_DEFAULT), EPSILON);
		assertEquals(volume2, cell.getCriticalVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(surface2, cell.getCriticalSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void reset_withoutTags_callsMethod() {
		Location location = spy(mock(Location.class));
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array, null);
		cell.reset(array, null);
		
		verify(location, times(2)).update(cellID, array, null);
	}
	
	@Test
	public void reset_withTags_callsMethod() {
		Location location = spy(mock(Location.class));
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		int[][][] array1 = new int[1][3][3];
		int[][][] array2 = new int[1][3][3];
		cell.initialize(array1, array2);
		cell.reset(array1, array2);
		
		verify(location, times(2)).update(cellID, array1, array2);
	}
	
	@Test
	public void reset_withoutTags_updatesTargets() {
		int volume = (int)(Math.random()*100);
		int surface = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume);
		when(location.getSurface()).thenReturn(surface);
		
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		cell.updateTarget(Math.random(), Math.random());
		cell.reset(new int[1][3][3], null);
		
		assertEquals(volume, cell.getTargetVolume(), EPSILON);
		assertEquals(surface, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void reset_withTags_updatesTargets() {
		int volume1 = (int)(Math.random()*100);
		int volume2 = (int)(Math.random()*100);
		int surface1 = (int)(Math.random()*100);
		int surface2 = (int)(Math.random()*100);
		Location location = mock(Location.class);
		when(location.getVolume()).thenReturn(volume1 + volume2);
		when(location.getSurface()).thenReturn(surface1 + surface2);
		when(location.getVolume(TAG_DEFAULT)).thenReturn(volume1);
		when(location.getSurface(TAG_DEFAULT)).thenReturn(surface1);
		when(location.getVolume(TAG_ADDITIONAL)).thenReturn(volume2);
		when(location.getSurface(TAG_ADDITIONAL)).thenReturn(surface2);
		
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		cell.updateTarget(TAG_DEFAULT, Math.random(), Math.random());
		cell.updateTarget(TAG_ADDITIONAL, Math.random(), Math.random());
		cell.reset(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(volume1 + volume2, cell.getTargetVolume(), EPSILON);
		assertEquals(surface1 + surface2, cell.getTargetSurface(), EPSILON);
		assertEquals(volume1, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(surface1, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		assertEquals(volume2, cell.getTargetVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(surface2, cell.getTargetSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void step_singleStep_updatesAge() {
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		PottsSimulation sim = mock(PottsSimulation.class);
		cell.module = mock(Module.class);
		
		cell.step(sim);
		assertEquals(1, cell.getAge(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoNoTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
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
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + rate*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = targetVolume - locationTagVolumes[1] - locationTagVolumes[2];
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroNoTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
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
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + rate*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = targetVolume - locationTagVolumes[1] - locationTagVolumes[2];
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoNoTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 2);
		cell.updateTarget(rate, 2);
		
		double targetVolumeOld = locationVolume + (rate + delta)*(locationVolume)*DT;
		double targetVolumeNew = targetVolumeOld + rate*(locationVolume)*DT;
		assertEquals(targetVolumeNew, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolumeNew;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoWithTagsModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 2);
		cell.updateTarget(rate, 2);
		
		double targetVolumeOld = locationVolume + (rate + delta)*(locationVolume)*DT;
		double targetVolumeNew = targetVolumeOld + rate*(locationVolume)*DT;
		assertEquals(targetVolumeNew, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolumeNew;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = targetVolumeNew - locationTagVolumes[1] - locationTagVolumes[2];
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroNoTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 0);
		cell.updateTarget(rate, 0);
		
		double targetVolumeOld = locationVolume + (rate + delta)*(-locationVolume)*DT;
		double targetVolumeNew = targetVolumeOld + rate*(-locationVolume)*DT;
		assertEquals(targetVolumeNew, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolumeNew;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroWithTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate + delta, 0);
		cell.updateTarget(rate, 0);
		
		double targetVolumeOld = locationVolume + (rate + delta)*(-locationVolume)*DT;
		double targetVolumeNew = targetVolumeOld + rate*(-locationVolume)*DT;
		assertEquals(targetVolumeNew, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolumeNew;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = targetVolumeNew - locationTagVolumes[1] - locationTagVolumes[2];
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleTwoWithTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(TAG_DEFAULT, rate, 2);
		
		double targetTagVolume = locationTagVolumes[0] + rate*(locationTagVolumes[0])*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		
		double targetVolume = targetTagVolume + locationTagVolumes[1] + locationTagVolumes[2];
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleZeroWithTag_updatesValues() {
		double rate = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(TAG_DEFAULT, rate, 0);
		
		double targetTagVolume = locationTagVolumes[0] + rate*(-locationTagVolumes[0])*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetTagVolume;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		
		double targetVolume = targetTagVolume + locationTagVolumes[1] + locationTagVolumes[2];
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleTwoWithTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(TAG_DEFAULT,rate + delta, 2);
		cell.updateTarget(TAG_DEFAULT,rate, 2);
		
		double targetVolumeOld = locationTagVolumes[0] + (rate + delta)*(locationTagVolumes[0])*DT;
		double targetVolumeNew = targetVolumeOld + rate*(locationTagVolumes[0])*DT;
		assertEquals(targetVolumeNew, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetVolumeNew;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		
		double targetVolume = targetVolumeNew + locationTagVolumes[1] + locationTagVolumes[2];
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleZeroWithTagModified_updatesValues() {
		double rate = Math.random();
		double delta = Math.random();
		PottsCellMock cell = new PottsCellMock(cellID, 0, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(TAG_DEFAULT,rate + delta, 0);
		cell.updateTarget(TAG_DEFAULT,rate, 0);
		
		double targetVolumeOld = locationTagVolumes[0] + (rate + delta)*(-locationTagVolumes[0])*DT;
		double targetVolumeNew = targetVolumeOld + rate*(-locationTagVolumes[0])*DT;
		assertEquals(targetVolumeNew, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = VOLUME_SURFACE_RATIO*targetVolumeNew;
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		
		double targetVolume = targetVolumeNew + locationTagVolumes[1] + locationTagVolumes[2];
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = VOLUME_SURFACE_RATIO*targetVolume;
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
}
