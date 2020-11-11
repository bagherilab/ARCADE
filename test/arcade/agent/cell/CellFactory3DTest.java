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
import static arcade.agent.cell.Cell.Region;
import static arcade.agent.cell.CellFactoryTest.*;

public class CellFactory3DTest {
	@Test
	public void makeCell_noRegions_createsObject() {
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
	public void makeCell_withRegions_createsObject() {
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
		
		EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
		doReturn(regionList).when(location).getRegions();
		
		EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
		EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
		
		Cell cell = factory.makeCell(cellID, cellPop, cellAge, cellState, location, parameters,
				criticals, lambdas, adhesion,
				criticalsRegion, lambdasRegion, adhesionRegion);
		
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