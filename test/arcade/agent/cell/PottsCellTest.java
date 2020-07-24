package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import sim.engine.*;
import arcade.sim.PottsSimulation;
import arcade.env.loc.*;
import arcade.agent.module.*;
import arcade.agent.module.Module;
import static arcade.agent.cell.Cell.*;
import static arcade.agent.cell.PottsCell.*;
import static arcade.sim.Potts.*;
import static arcade.sim.Simulation.*;

public class PottsCellTest {
	private static final double EPSILON = 1E-5;
	private static final int TAG_ADDITIONAL = TAG_DEFAULT - 1;
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
	int cellID = 1;
	int tags = 3;
	PottsCell cellDefault;
	PottsCell cellWithTags;
	PottsCell cellWithoutTags;
	
	@Before
	public void setupMocks() {
		
		
		// Random lambda values.
		lambdaVolume = Math.random();
		lambdaSurface = Math.random();
		
		// Random adhesion values.
		adhesionTo0 = Math.random();
		adhesionTo1 = Math.random();
		adhesionTo2 = Math.random();
		
		location = mock(PottsLocation.class);
		
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
		
		cellDefault = new PottsCell(cellID, location, lambdas, adhesion);
		cellWithTags = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cellWithoutTags = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
	}
	
	@Test
	public void getID_defaultConstructor_returnsValue() {
		assertEquals(cellID, cellDefault.getID());
	}
	
	@Test
	public void getID_parentConstructor_returnsValue() {
		int id = (int)(Math.random()*100) + cellID;
		PottsCell cell = new PottsCell(cellDefault, id, STATE_PROLIFERATIVE, null);
		assertEquals(id, cell.getID());
	}
	
	@Test
	public void getPop_defaultConstructor_returnsValue() {
		assertEquals(1, cellDefault.getPop());
	}
	
	@Test
	public void getPop_parentConstructor_returnsValue() {
		int cellPop = (int)(Math.random() * 100);
		PottsCell cell1 = new PottsCell(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		PottsCell cell2 = new PottsCell(cell1, cellID + 1, STATE_PROLIFERATIVE, null);
		assertEquals(cellPop, cell2.getPop());
	}
	
	@Test
	public void getPop_valueAssigned_returnsValue() {
		int cellPop = (int)(Math.random() * 100);
		PottsCell cell = new PottsCell(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		assertEquals(cellPop, cell.getPop());
	}
	
	@Test
	public void getState_defaultConstructor_returnsValue() {
		assertEquals(STATE_PROLIFERATIVE, cellDefault.getState());
	}
	
	@Test
	public void getState_parentConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellDefault, cellID + 1, STATE_APOPTOTIC, null);
		assertEquals(STATE_APOPTOTIC, cell.getState());
	}
	
	@Test
	public void getState_valueAssigned_returnsValue() {
		int cellState = STATE_QUIESCENT;
		PottsCell cell = new PottsCell(cellID, 0, cellState, 0, location, lambdas, adhesion, 0, null, null);
		assertEquals(cellState, cell.getState());
	}
	
	@Test
	public void getAge_defaultConstructor_returnsValue() {
		assertEquals(0, cellDefault.getAge());
	}
	
	@Test
	public void getAge_parentConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellDefault, cellID + 1, STATE_APOPTOTIC, null);
		assertEquals(0, cell.getAge());
	}
	
	@Test
	public void getAge_valueAssigned_returnsValue() {
		int cellAge = (int)(Math.random() * 100);
		PottsCell cell = new PottsCell(cellID, 0, 0, cellAge, location, lambdas, adhesion, 0, null, null);
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeValidTag_returnsValue() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagVolumes[0], cell.getTargetVolume(-1), EPSILON);
		assertEquals(locationTagVolumes[1], cell.getTargetVolume(-2), EPSILON);
		assertEquals(locationTagVolumes[2], cell.getTargetVolume(-3), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeInvalidTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetVolume(0), EPSILON);
		assertEquals(0, cell.getTargetVolume(-4), EPSILON);
	}
	
	@Test
	public void getTargetVolume_afterInitializeNoTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeValidTag_returnsValue() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagSurfaces[0], cell.getTargetSurface(-1), EPSILON);
		assertEquals(locationTagSurfaces[1], cell.getTargetSurface(-2), EPSILON);
		assertEquals(locationTagSurfaces[2], cell.getTargetSurface(-3), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeInvalidTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetSurface(0), EPSILON);
		assertEquals(0, cell.getTargetSurface(-4), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitializeNoTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell.initialize(null, null);
		assertEquals(0, cell.getTargetSurface(-1), EPSILON);
		assertEquals(0, cell.getTargetSurface(-2), EPSILON);
		assertEquals(0, cell.getTargetSurface(-3), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_parentConstructorWithoutTags_returnsValues() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell1 = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell1.initialize(new int[1][3][3], new int[1][3][3]);
		PottsCell cell2 = new PottsCell(cell1, cellID + 1, STATE_QUIESCENT, null);
		
		assertEquals(cell1.getCriticalVolume(), cell2.getCriticalVolume(), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_parentConstructorWithTags_returnsValues() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell1 = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell1.initialize(new int[1][3][3], new int[1][3][3]);
		PottsCell cell2 = new PottsCell(cell1, cellID + 1, STATE_QUIESCENT, null);
		
		assertEquals(cell1.getCriticalVolume(TAG_DEFAULT), cell2.getCriticalVolume(TAG_DEFAULT), EPSILON);
		assertEquals(cell1.getCriticalVolume(TAG_ADDITIONAL), cell2.getCriticalVolume(TAG_ADDITIONAL), EPSILON);
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationVolume, cell.getCriticalVolume(), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeValidTag_returnsValue() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagVolumes[0], cell.getCriticalVolume(-1), EPSILON);
		assertEquals(locationTagVolumes[1], cell.getCriticalVolume(-2), EPSILON);
		assertEquals(locationTagVolumes[2], cell.getCriticalVolume(-3), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeInvalidTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalVolume(0), EPSILON);
		assertEquals(0, cell.getCriticalVolume(-4), EPSILON);
	}
	
	@Test
	public void getCriticalVolume_afterInitializeNoTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalVolume(-1), EPSILON);
		assertEquals(0, cell.getCriticalVolume(-2), EPSILON);
		assertEquals(0, cell.getCriticalVolume(-3), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_parentConstructorWithoutTags_returnsValues() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell1 = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
		cell1.initialize(new int[1][3][3], new int[1][3][3]);
		PottsCell cell2 = new PottsCell(cell1, cellID + 1, STATE_QUIESCENT, null);
		
		assertEquals(cell1.getCriticalSurface(), cell2.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_parentConstructorWithTags_returnsValues() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell1 = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell1.initialize(new int[1][3][3], new int[1][3][3]);
		PottsCell cell2 = new PottsCell(cell1, cellID + 1, STATE_QUIESCENT, null);
		
		assertEquals(cell1.getCriticalSurface(TAG_DEFAULT), cell2.getCriticalSurface(TAG_DEFAULT), EPSILON);
		assertEquals(cell1.getCriticalSurface(TAG_ADDITIONAL), cell2.getCriticalSurface(TAG_ADDITIONAL), EPSILON);
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		assertEquals(locationSurface, cell.getCriticalSurface(), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeValidTag_returnsValue() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(locationTagSurfaces[0], cell.getCriticalSurface(-1), EPSILON);
		assertEquals(locationTagSurfaces[1], cell.getCriticalSurface(-2), EPSILON);
		assertEquals(locationTagSurfaces[2], cell.getCriticalSurface(-3), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeInvalidTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		assertEquals(0, cell.getCriticalSurface(0), EPSILON);
		assertEquals(0, cell.getCriticalSurface(-4), EPSILON);
	}
	
	@Test
	public void getCriticalSurface_afterInitializeNoTag_returnsZero() {
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, 0, null, null);
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		
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
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.setState(-1);
	}
	
	@Test
	public void schedule_validInput_callsMethod() {
		Schedule schedule = spy(mock(Schedule.class));
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.schedule(schedule);
		
		verify(schedule).scheduleRepeating(cell, ORDERING_CELLS, 1);
	}
	
	@Test
	public void schedule_validInput_assignStopper() {
		Schedule schedule = spy(mock(Schedule.class));
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		when(schedule.scheduleRepeating(cell, ORDERING_CELLS, 1)).thenReturn(mock(Stoppable.class));
		cell.schedule(schedule);
		assertNotNull(cell.stopper);
	}
	
	@Test
	public void initialize_withoutTags_updatesArray() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array, null);
		
		assertArrayEquals(new int[] { 0, 0, 0 }, array[0][0]);
		assertArrayEquals(new int[] { 1, 0, 0 }, array[0][1]);
		assertArrayEquals(new int[] { 1, 1, 0 }, array[0][2]);
	}
	
	@Test
	public void initialize_withTags_updatesArray() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		int[][][] array = new int[1][3][3];
		cell.initialize(new int[1][3][3], array);
		
		assertArrayEquals(new int[] { 0, 0, 0 }, array[0][0]);
		assertArrayEquals(new int[] { TAG_DEFAULT, 0, TAG_ADDITIONAL }, array[0][1]);
		assertArrayEquals(new int[] { TAG_DEFAULT, TAG_DEFAULT, TAG_ADDITIONAL }, array[0][2]);
	}
	
	@Test
	public void initialize_withoutTags_updatesTargets() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		
		assertEquals(3, cell.getTargetVolume(), EPSILON);
		assertEquals(8, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void initialize_withTags_updatesTargets() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(5, cell.getTargetVolume(), EPSILON);
		assertEquals(12, cell.getTargetSurface(), EPSILON);
		assertEquals(3, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(8, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		assertEquals(2, cell.getTargetVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(6, cell.getTargetSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void initialize_withoutTags_updatesCriticals() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		
		assertEquals(3, cell.getCriticalVolume(), EPSILON);
		assertEquals(8, cell.getCriticalSurface(), EPSILON);
		assertEquals(0, cell.getCriticalVolume(TAG_DEFAULT), EPSILON);
		assertEquals(0, cell.getCriticalSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void initialize_withTags_updatesCriticals() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(5, cell.getCriticalVolume(), EPSILON);
		assertEquals(12, cell.getCriticalSurface(), EPSILON);
		assertEquals(3, cell.getCriticalVolume(TAG_DEFAULT), EPSILON);
		assertEquals(8, cell.getCriticalSurface(TAG_DEFAULT), EPSILON);
		assertEquals(2, cell.getCriticalVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(6, cell.getCriticalSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void reset_withoutTags_updatesArray() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array, null);
		
		cell.getLocation().add(1, 1, 0);
		cell.reset(array, null);
		
		assertArrayEquals(new int[] { 0, 0, 0 }, array[0][0]);
		assertArrayEquals(new int[] { 1, 1, 0 }, array[0][1]);
		assertArrayEquals(new int[] { 1, 1, 0 }, array[0][2]);
	}
	
	@Test
	public void reset_withTags_updatesArray() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		int[][][] array = new int[1][3][3];
		cell.initialize(new int[1][3][3], array);
		
		cell.getLocation().add(1, 1, 0);
		cell.getLocation().add(TAG_ADDITIONAL, 0, 0, 0);
		cell.reset(new int[1][3][3], array);
		
		assertArrayEquals(new int[] { TAG_ADDITIONAL, 0, 0 }, array[0][0]);
		assertArrayEquals(new int[] { TAG_DEFAULT, TAG_DEFAULT, TAG_ADDITIONAL }, array[0][1]);
		assertArrayEquals(new int[] { TAG_DEFAULT, TAG_DEFAULT, TAG_ADDITIONAL }, array[0][2]);
	}
	
	@Test
	public void reset_withoutTags_updatesTargets() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][3][3], null);
		cell.updateTarget(Math.random(), Math.random());
		
		cell.reset(new int[1][3][3], null);
		
		assertEquals(3, cell.getTargetVolume(), EPSILON);
		assertEquals(8, cell.getTargetSurface(), EPSILON);
		assertEquals(0, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(0, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void reset_withTags_updatesTargets() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocations location = new PottsLocations(voxels);
		
		location.add(TAG_ADDITIONAL, 1, 2, 0);
		location.add(TAG_ADDITIONAL, 2, 2, 0);
		
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion,
				2, new double[][] { { 0, 0 }, { 0, 0 } }, new double[][] { { 0, 0 }, {0, 0} });
		cell.initialize(new int[1][3][3], new int[1][3][3]);
		cell.updateTarget(TAG_DEFAULT, Math.random(), Math.random());
		cell.updateTarget(TAG_ADDITIONAL, Math.random(), Math.random());
		
		cell.reset(new int[1][3][3], new int[1][3][3]);
		
		assertEquals(5, cell.getTargetVolume(), EPSILON);
		assertEquals(12, cell.getTargetSurface(), EPSILON);
		assertEquals(3, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		assertEquals(8, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		assertEquals(2, cell.getTargetVolume(TAG_ADDITIONAL), EPSILON);
		assertEquals(6, cell.getTargetSurface(TAG_ADDITIONAL), EPSILON);
	}
	
	@Test
	public void step_singleStep_updatesAge() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		PottsSimulation sim = mock(PottsSimulation.class);
		cell.module = mock(Module.class);
		
		cell.step(sim);
		assertEquals(1, cell.getAge(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoNoTag_updatesValues() {
		double rate = Math.random();
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + rate*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleTwoWithTags_updatesValues() {
		double rate = 1;//Math.random();
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate, 2);
		
		double targetVolume = locationVolume + rate*(locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = targetVolume - locationTagVolumes[1] - locationTagVolumes[2];
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetTagVolume);
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroNoTag_updatesValues() {
		double rate = Math.random();
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(null, null);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + rate*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_untaggedScaleZeroWithTag_updatesValues() {
		double rate = Math.random();
		PottsCell cell = new PottsCell(cellID, 1, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(rate, 0);
		
		double targetVolume = locationVolume + rate*(-locationVolume)*DT;
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
		
		double targetTagVolume = targetVolume - locationTagVolumes[1] - locationTagVolumes[2];
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetTagVolume);
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleTwoWithTag_updatesValues() {
		double rate = Math.random();
		PottsCell cell = new PottsCell(cellID, 0, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(TAG_DEFAULT, rate, 2);
		
		double targetTagVolume = locationTagVolumes[0] + rate*(locationTagVolumes[0])*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetTagVolume);
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		
		double targetVolume = targetTagVolume + locationTagVolumes[1] + locationTagVolumes[2];
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void updateTarget_scaleZeroWithTag_updatesValues() {
		double rate = Math.random();
		PottsCell cell = new PottsCell(cellID, 0, location, lambdas, adhesion, tags, lambdasTag, adhesionTag);
		cell.initialize(null, null);
		cell.updateTarget(TAG_DEFAULT, rate, 0);
		
		double targetTagVolume = locationTagVolumes[0] + rate*(-locationTagVolumes[0])*DT;
		assertEquals(targetTagVolume, cell.getTargetVolume(TAG_DEFAULT), EPSILON);
		
		double targetTagSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetTagVolume);
		assertEquals(targetTagSurface, cell.getTargetSurface(TAG_DEFAULT), EPSILON);
		
		double targetVolume = targetTagVolume + locationTagVolumes[1] + locationTagVolumes[2];
		assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
		
		double targetSurface = SURFACE_VOLUME_MULTIPLIER*Math.sqrt(targetVolume);
		assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
	}
}
