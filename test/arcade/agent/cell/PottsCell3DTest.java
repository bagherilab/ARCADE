package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.EnumSet;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.agent.cell.PottsCell3D.*;
import static arcade.agent.cell.Cell.Tag;
import static arcade.agent.cell.Cell.State;
import static arcade.sim.Potts.Term;

public class PottsCell3DTest {
	private static final double EPSILON = 1E-5;
	static EnumMap<Term, Double> criticals;
	static EnumMap<Term, Double> lambdas;
	static double[] adhesion;
	static EnumMap<Tag, EnumMap<Term, Double>> criticalsTag;
	static EnumMap<Tag, EnumMap<Term, Double>> lambdasTag;
	static EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag;
	static int cellID = (int)(Math.random()*10) + 1;
	static int cellPop = (int)(Math.random()*10) + 1;
	static MiniBox parameters = mock(MiniBox.class);
	
	@BeforeClass
	public static void setupArrays() {
		int n = (int)(Math.random()*10) + 2;
		criticals = new EnumMap<>(Term.class);
		lambdas = new EnumMap<>(Term.class);
		adhesion = new double[n];
		
		criticalsTag = new EnumMap<>(Tag.class);
		lambdasTag = new EnumMap<>(Tag.class);
		adhesionTag = new EnumMap<>(Tag.class);
		
		for (int i = 0; i < n; i++) { adhesion[i] = Math.random(); }
		
		for (Tag tag : Tag.values()) {
			criticalsTag.put(tag, new EnumMap<>(Term.class));
			lambdasTag.put(tag, new EnumMap<>(Term.class));
			adhesionTag.put(tag, new EnumMap<>(Tag.class));
		}
	}
	
	@Test
	public void defaultConstructor_withoutTags_setsFields() {
		Location location = mock(Location.class);
		PottsCell3D cell = new PottsCell3D(cellID, cellPop, location, parameters,
				criticals, lambdas, adhesion);
		
		assertEquals(cellID, cell.id);
		assertEquals(cellPop, cell.pop);
		assertEquals(0, cell.getAge());
		assertFalse(cell.hasTags);
		assertEquals(location, cell.getLocation());
		assertEquals(cell.parameters, parameters);
	}
	
	@Test
	public void defaultConstructor_withTags_setsFields() {
		Location location = mock(Location.class);
		when(location.getTags()).thenReturn(EnumSet.of(Tag.DEFAULT, Tag.NUCLEUS));
		PottsCell3D cell = new PottsCell3D(cellID, cellPop, location, parameters,
				criticals, lambdas, adhesion, criticalsTag, lambdasTag, adhesionTag);
		
		assertEquals(cellID, cell.id);
		assertEquals(cellPop, cell.pop);
		assertEquals(0, cell.getAge());
		assertTrue(cell.hasTags);
		assertEquals(location, cell.getLocation());
		assertEquals(cell.parameters, parameters);
	}
	
	@Test
	public void make_givenCell_setsFields() {
		Location location1 = mock(Location.class);
		Location location2 = mock(Location.class);
		PottsCell3D cell1 = new PottsCell3D(cellID, cellPop, location1, parameters, criticals, lambdas, adhesion);
		PottsCell cell2 = cell1.make(cellID + 1, State.QUIESCENT, location2);
		
		assertEquals(cellID + 1, cell2.id);
		assertEquals(cellPop, cell2.pop);
		assertEquals(0, cell2.getAge());
		assertFalse(cell2.hasTags);
		assertEquals(location2, cell2.getLocation());
		assertTrue(cell2 instanceof PottsCell3D);
		assertEquals(cell2.parameters, parameters);
	}
	
	@Test
	public void convert_givenValue_calculatesValue() {
		double volume = Math.random()*100;
		Location location = mock(Location.class);
		PottsCell3D cell = new PottsCell3D(cellID, cellPop, location, parameters, criticals, lambdas, adhesion);
		assertEquals(SURFACE_VOLUME_MULTIPLIER*Math.pow(volume, 2./3), cell.convert(volume), EPSILON);
	}
}
