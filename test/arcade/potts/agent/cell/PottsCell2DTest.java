package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.EnumSet;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.agent.cell.PottsCell2D.*;
import static arcade.agent.cell.Cell.Region;
import static arcade.agent.cell.Cell.State;
import static arcade.sim.Potts.Term;

public class PottsCell2DTest {
	private static final double EPSILON = 1E-5;
	static EnumMap<Term, Double> criticals;
	static EnumMap<Term, Double> lambdas;
	static double[] adhesion;
	static EnumMap<Region, EnumMap<Term, Double>> criticalsRegion;
	static EnumMap<Region, EnumMap<Term, Double>> lambdasRegion;
	static EnumMap<Region, EnumMap<Region, Double>> adhesionRegion;
	static int cellID = (int)(Math.random()*10) + 1;
	static int cellPop = (int)(Math.random()*10) + 1;
	static MiniBox parameters = mock(MiniBox.class);
	
	@BeforeClass
	public static void setupArrays() {
		int n = (int)(Math.random()*10) + 2;
		criticals = new EnumMap<>(Term.class);
		lambdas = new EnumMap<>(Term.class);
		adhesion = new double[n];
		
		criticalsRegion = new EnumMap<>(Region.class);
		lambdasRegion = new EnumMap<>(Region.class);
		adhesionRegion = new EnumMap<>(Region.class);
		
		for (int i = 0; i < n; i++) { adhesion[i] = Math.random(); }
		
		for (Region region : Region.values()) {
			criticalsRegion.put(region, new EnumMap<>(Term.class));
			lambdasRegion.put(region, new EnumMap<>(Term.class));
			adhesionRegion.put(region, new EnumMap<>(Region.class));
		}
	}
	
	@Test
	public void defaultConstructor_withoutRegions_setsFields() {
		Location location = mock(Location.class);
		PottsCell2D cell = new PottsCell2D(cellID, cellPop, location, parameters,
				criticals, lambdas, adhesion);
		
		assertEquals(cellID, cell.id);
		assertEquals(cellPop, cell.pop);
		assertEquals(0, cell.getAge());
		assertFalse(cell.hasRegions);
		assertEquals(location, cell.getLocation());
		assertEquals(cell.parameters, parameters);
	}
	
	@Test
	public void defaultConstructor_withRegions_setsFields() {
		Location location = mock(Location.class);
		when(location.getRegions()).thenReturn(EnumSet.of(Region.DEFAULT, Region.NUCLEUS));
		PottsCell2D cell = new PottsCell2D(cellID, cellPop, location, parameters,
				criticals, lambdas, adhesion, criticalsRegion, lambdasRegion, adhesionRegion);
		
		assertEquals(cellID, cell.id);
		assertEquals(cellPop, cell.pop);
		assertEquals(0, cell.getAge());
		assertTrue(cell.hasRegions);
		assertEquals(location, cell.getLocation());
		assertEquals(cell.parameters, parameters);
	}
	
	@Test
	public void make_givenCell_setsFields() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCell2D cell1 = new PottsCell2D(cellID, cellPop, location1, parameters, criticals, lambdas, adhesion);
		PottsCell cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2);
		
		assertEquals(cellID + 1, cell2.id);
		assertEquals(cellPop, cell2.pop);
		assertEquals(0, cell2.getAge());
		assertFalse(cell2.hasRegions);
		assertEquals(location2, cell2.getLocation());
		assertTrue(cell2 instanceof PottsCell2D);
		assertEquals(cell2.parameters, parameters);
	}
	
	@Test
	public void convert_givenValue_calculatesValue() {
		double volume = Math.random()*100;
		Location location = mock(Location.class);
		PottsCell2D cell = new PottsCell2D(cellID, cellPop, location, parameters, criticals, lambdas, adhesion);
		assertEquals(SURFACE_VOLUME_MULTIPLIER*Math.sqrt(volume), cell.convert(volume), EPSILON);
	}
}
