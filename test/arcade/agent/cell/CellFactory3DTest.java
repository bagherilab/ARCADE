package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.EnumSet;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.sim.Potts.Term;
import static arcade.agent.cell.Cell.State;
import static arcade.agent.cell.Cell.Tag;
import static arcade.agent.cell.CellFactoryTest.*;

public class CellFactory3DTest {
	@Test
	public void makeCell_noTags_createsObject() {
		CellFactory3D factory = new CellFactory3D();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		State cellState = randomState();
		Location location = mock(Location.class);
		MiniBox parameters = mock(MiniBox.class);
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] { random(), random() };
		
		Cell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, parameters, criticals, lambdas, adhesion);
		
		assertTrue(cell instanceof PottsCell3D);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellState, cell.getState());
		assertEquals(location, cell.getLocation());
		assertEquals(parameters, cell.getParameters());
		assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas.get(Term.VOLUME), cell.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas.get(Term.SURFACE), cell.getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
	}
	
	@Test
	public void makeCell_withTags_createsObject() {
		CellFactory3D factory = new CellFactory3D();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		int cellAge = (int)random();
		State cellState = randomState();
		Location location = mock(Location.class);
		MiniBox parameters = mock(MiniBox.class);
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] { random(), random() };
		
		EnumSet<Tag> tagList = EnumSet.of(Tag.NUCLEUS, Tag.UNDEFINED);
		doReturn(tagList).when(location).getTags();
		
		EnumMap<Tag, EnumMap<Term, Double>> criticalsTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Term, Double>> lambdasTag = makeEnumMapTag(tagList);
		EnumMap<Tag, EnumMap<Tag, Double>> adhesionTag = makeEnumMapTarget(tagList);
		
		Cell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, parameters,
				criticals, lambdas, adhesion,
				criticalsTag, lambdasTag, adhesionTag);
		
		assertTrue(cell instanceof PottsCell3D);
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(cellState , cell.getState());
		assertEquals(location, cell.getLocation());
		assertEquals(parameters, cell.getParameters());
		assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas.get(Term.VOLUME), cell.getLambda(Term.VOLUME), EPSILON);
		assertEquals(lambdas.get(Term.SURFACE), cell.getLambda(Term.SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		
		for (Tag tag : tagList) {
			assertEquals(criticalsTag.get(tag).get(Term.VOLUME), cell.getCriticalVolume(tag), EPSILON);
			assertEquals(criticalsTag.get(tag).get(Term.SURFACE), cell.getCriticalSurface(tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.VOLUME), cell.getLambda(Term.VOLUME, tag), EPSILON);
			assertEquals(lambdasTag.get(tag).get(Term.SURFACE), cell.getLambda(Term.SURFACE, tag), EPSILON);
			
			for (Tag target : tagList) {
				assertEquals(adhesionTag.get(tag).get(target), cell.getAdhesion(tag, target), EPSILON);
			}
		}
	}
}