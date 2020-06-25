package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import arcade.env.loc.Location;
import arcade.env.loc.PottsLocation;
import static arcade.sim.Potts.*;

public class PottsCellTest {
	private static final double EPSILON = 0;
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
		// Random volume and surface values.
		locationVolume = (int)(Math.random()*100);
		locationSurface = (int)(Math.random()*100);
		
		// Random lambda values.
		lambdaVolume = Math.random();
		lambdaSurface = Math.random();
		
		// Random adhesion values.
		adhesionTo0 = Math.random();
		adhesionTo1 = Math.random();
		adhesionTo2 = Math.random();
		
		location = mock(PottsLocation.class);
		when(location.getVolume()).thenReturn(locationVolume);
		when(location.getSurface()).thenReturn(locationSurface);
		
		locationTagVolumes = new int[tags];
		locationTagSurfaces = new int[tags];
		
		// Random volumes and surfaces for tagged regions.
		for (int i = 0; i < tags; i++) {
			int tag = -i - 1;
			locationTagVolumes[i] = (int)(Math.random() * 100);
			locationTagSurfaces[i] = (int)(Math.random() * 100);
			when(location.getVolume(tag)).thenReturn(locationTagVolumes[i]);
			when(location.getSurface(tag)).thenReturn(locationTagSurfaces[i]);
		}
		
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
	public void getPop_defaultConstructor_returnsValue() {
		assertEquals(1, cellDefault.getPop());
	}
	
	@Test
	public void getPop_valueAssigned_returnsValue() {
		int cellPop = (int)(Math.random() * 100);
		PottsCell cell = new PottsCell(cellID, cellPop, location, lambdas, adhesion, 0, null, null);
		assertEquals(cellPop, cell.getPop());
	}
	
	@Test
	public void getLocation_defaultConstructor_returnsValue() {
		assertSame(location, cellDefault.getLocation());
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
	public void initialize_givenArray_updatesIDs() {
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
}
