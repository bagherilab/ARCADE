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
	Location location;
	int locationVolume;
	int locationSurface;
	int cellID = 1;
	
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
		
		lambdas = new double[2];
		lambdas[LAMBDA_VOLUME] = lambdaVolume;
		lambdas[LAMBDA_SURFACE] = lambdaSurface;
		
		adhesion = new double[] { adhesionTo0, adhesionTo1, adhesionTo2 };
	}
	
	@Test
	public void getID_defaultConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(cellID, cell.getID());
	}
	
	@Test
	public void getPop_defaultConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(1, cell.getPop());
	}
	
	@Test
	public void getPop_valueAssigned_returnsValue() {
		int cellPop = (int)Math.random()*100;
		PottsCell cell = new PottsCell(cellID, 2, location, lambdas, adhesion);
		assertEquals(2, cell.getPop());
	}
	
	@Test
	public void getLocation_defaultConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertSame(location, cell.getLocation());
	}
	
	@Test
	public void getVolume_defaultConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(locationVolume, cell.getVolume());
	}
	
	@Test
	public void getSurface_defaultConstructor_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(locationSurface, cell.getSurface());
	}
	
	@Test
	public void getTargetVolume_beforeInitialize_returnsZero() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(0, cell.getTargetVolume(), EPSILON);
	}
	
	public void getTargetVolume_afterInitialize_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][5][5]);
		assertEquals(locationVolume, cell.getTargetVolume(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_beforeInitialize_returnsZero() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(0, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getTargetSurface_afterInitialize_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		cell.initialize(new int[1][5][5]);
		assertEquals(locationSurface, cell.getTargetSurface(), EPSILON);
	}
	
	@Test
	public void getLambda_givenTerm_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(lambdaVolume, cell.getLambda(LAMBDA_VOLUME), EPSILON);
		assertEquals(lambdaSurface, cell.getLambda(LAMBDA_SURFACE), EPSILON);
	}
	
	@Test
	public void getAdhesion_givenPop_returnsValue() {
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		assertEquals(adhesionTo0, cell.getAdhesion(0), EPSILON);
		assertEquals(adhesionTo1, cell.getAdhesion(1), EPSILON);
		assertEquals(adhesionTo2, cell.getAdhesion(2), EPSILON);
	}
	
	@Test
	public void initialize_givenArray_updatesArray() {
		ArrayList<Location.Voxel> voxels = new ArrayList<>();
		voxels.add(new Location.Voxel(1, 0, 0));
		voxels.add(new Location.Voxel(2, 0, 0));
		voxels.add(new Location.Voxel(2, 1, 0));
		PottsLocation location = new PottsLocation(voxels);
		
		PottsCell cell = new PottsCell(cellID, location, lambdas, adhesion);
		int[][][] array = new int[1][3][3];
		cell.initialize(array);
		
		assertArrayEquals(new int[] { 0, 0, 0 }, array[0][0]);
		assertArrayEquals(new int[] { 1, 0, 0 }, array[0][1]);
		assertArrayEquals(new int[] { 1, 1, 0 }, array[0][2]);
	}
}
