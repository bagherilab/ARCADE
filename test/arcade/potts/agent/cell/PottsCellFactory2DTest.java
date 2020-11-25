package arcade.potts.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.EnumSet;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.State;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;
import static arcade.potts.agent.cell.PottsCellFactoryTest.*;
import static arcade.core.TestUtilities.*;

public class PottsCellFactory2DTest {
	@Test
	public void makeCell_noRegions_createsObject() {
		PottsCellFactory2D factory = new PottsCellFactory2D();
		
		int cellID = randomIntBetween(1, 10);
		int cellPop = randomIntBetween(1, 10);
		int cellAge = randomIntBetween(1, 100);
		State cellState = State.random(RANDOM);
		Location location = mock(PottsLocation.class);
		MiniBox parameters = mock(MiniBox.class);
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		
		PottsCell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, parameters, criticals, lambdas, adhesion);
		
		assertTrue(cell instanceof PottsCell2D);
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
	public void makeCell_withRegions_createsObject() {
		PottsCellFactory2D factory = new PottsCellFactory2D();
		
		int cellID = randomIntBetween(1, 10);
		int cellPop = randomIntBetween(1, 10);
		int cellAge = randomIntBetween(1, 100);
		State cellState = State.random(RANDOM);
		Location location = mock(PottsLocation.class);
		MiniBox parameters = mock(MiniBox.class);
		EnumMap<Term, Double> criticals = makeEnumMap();
		EnumMap<Term, Double> lambdas = makeEnumMap();
		double[] adhesion = new double[] {
				randomDoubleBetween(0, 10),
				randomDoubleBetween(0, 10)
		};
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		PottsCell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, parameters,
				criticals, lambdas, adhesion,
				criticalsRegion, lambdasRegion, adhesionRegion);
		
		assertTrue(cell instanceof PottsCell2D);
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
		
		for (Region region : regionList) {
			assertEquals(criticalsRegion.get(region).get(Term.VOLUME), cell.getCriticalVolume(region), EPSILON);
			assertEquals(criticalsRegion.get(region).get(Term.SURFACE), cell.getCriticalSurface(region), EPSILON);
			assertEquals(lambdasRegion.get(region).get(Term.VOLUME), cell.getLambda(Term.VOLUME, region), EPSILON);
			assertEquals(lambdasRegion.get(region).get(Term.SURFACE), cell.getLambda(Term.SURFACE, region), EPSILON);
			
			for (Region target : regionList) {
				assertEquals(adhesionRegion.get(region).get(target), cell.getAdhesion(region, target), EPSILON);
			}
		}
	}
}