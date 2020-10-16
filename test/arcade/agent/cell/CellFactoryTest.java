package arcade.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.sim.Series.TARGET_SEPARATOR;
import static arcade.util.MiniBox.TAG_SEPARATOR;

public class CellFactoryTest {
	static final double EPSILON = 1E-10;
	
	static double random() { return Math.random()*100; }
	
	static class CellFactoryMock extends CellFactory {
		public CellFactoryMock() { super(); }
		
		Cell makeCell(int id, int pop, Location location,
					  double[] criticals, double[] lambdas, double[] adhesion) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getLocation()).thenReturn(location);
			
			when(cell.getCriticalVolume()).thenReturn(criticals[0]);
			when(cell.getCriticalSurface()).thenReturn(criticals[1]);
			
			when(cell.getLambda(TERM_VOLUME)).thenReturn(lambdas[0]);
			when(cell.getLambda(TERM_SURFACE)).thenReturn(lambdas[1]);
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			return cell;
		}
		
		Cell makeCell(int id, int pop, Location location,
					  double[] criticals, double[] lambdas, double[] adhesion, int tags,
					  double[][] criticalsTag, double[][] lambdasTag, double[][] adhesionsTag) {
			PottsCell cell = mock(PottsCell.class);
			
			when(cell.getID()).thenReturn(id);
			when(cell.getPop()).thenReturn(pop);
			when(cell.getLocation()).thenReturn(location);
			
			when(cell.getCriticalVolume()).thenReturn(criticals[0]);
			when(cell.getCriticalSurface()).thenReturn(criticals[1]);
			
			when(cell.getLambda(TERM_VOLUME)).thenReturn(lambdas[0]);
			when(cell.getLambda(TERM_SURFACE)).thenReturn(lambdas[1]);
			
			for (int i = 0; i < adhesion.length; i++) {
				when(cell.getAdhesion(i)).thenReturn(adhesion[i]);
			}
			
			for (int i = 0; i < tags; i++) {
				when(cell.getCriticalVolume(-i - 1)).thenReturn(criticalsTag[0][i]);
				when(cell.getCriticalSurface(-i - 1)).thenReturn(criticalsTag[1][i]);
				
				when(cell.getLambda(TERM_VOLUME, -i - 1)).thenReturn(lambdasTag[0][i]);
				when(cell.getLambda(TERM_SURFACE, -i - 1)).thenReturn(lambdasTag[1][i]);
				
				for (int j = 0; j < tags; j++) {
					when(cell.getAdhesion(-i - 1, -j - 1)).thenReturn(adhesionsTag[i][j]);
				}
			}
			
			return cell;
		}
	}
	
	@Test
	public void make_onePopulationNoTags_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:A", adhesion[1]);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		HashSet<String> keys = new HashSet<>();
		keys.add("A");
		HashMap<String, MiniBox> populations = mock(HashMap.class);
		when(populations.keySet()).thenReturn(keys);
		when(populations.get("A")).thenReturn(mock(MiniBox.class));
		when(populations.get("A").getInt("CODE")).thenReturn(1);
		Cell cell = factory.make(cellID, population, location, populations);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
	}
	
	@Test
	public void makeCell_multiplePopulationsNoTags_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random(), random(), random() };
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:B", adhesion[1]);
		population.put("ADHESION:C", adhesion[2]);
		population.put("ADHESION:D", adhesion[3]);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		HashSet<String> keys = new HashSet<>();
		keys.add("B"); keys.add("C"); keys.add("D");
		HashMap<String, MiniBox> populations = mock(HashMap.class);
		when(populations.keySet()).thenReturn(keys);
		when(populations.get("B")).thenReturn(mock(MiniBox.class));
		when(populations.get("B").getInt("CODE")).thenReturn(1);
		when(populations.get("C")).thenReturn(mock(MiniBox.class));
		when(populations.get("C").getInt("CODE")).thenReturn(2);
		when(populations.get("D")).thenReturn(mock(MiniBox.class));
		when(populations.get("D").getInt("CODE")).thenReturn(3);
		Cell cell = factory.make(cellID, population, location, populations);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(adhesion[2], cell.getAdhesion(2), EPSILON);
		assertEquals(adhesion[3], cell.getAdhesion(3), EPSILON);
	}
	
	@Test
	public void makeCell_onePopulationWithTags_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random() };
		
		double[][] criticalsTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] lambdasTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] adhesionTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:A", adhesion[1]);
		population.put("TAG/a", 0);
		population.put("TAG/b", 0);
		population.put("TAG/c", 0);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		int tags = 3;
		String[] _tags = new String[] { "a", "b", "c" };
		for (int i = 0; i < tags; i++) {
			String tag = _tags[i];
			population.put(tag + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasTag[1][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsTag[1][i]);
			
			for (int j = 0; j < tags; j++) {
				population.put(tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + _tags[j], adhesionTag[i][j]);
			}
		}
		
		HashSet<String> keys = new HashSet<>();
		keys.add("A");
		HashMap<String, MiniBox> populations = mock(HashMap.class);
		when(populations.keySet()).thenReturn(keys);
		when(populations.get("A")).thenReturn(mock(MiniBox.class));
		when(populations.get("A").getInt("CODE")).thenReturn(1);
		Cell cell = factory.make(cellID, population, location, populations);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		
		for (int i = 0; i < tags; i++) {
			assertEquals(criticalsTag[0][i], cell.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			
			for (int j = 0; j < tags; j++) {
				assertEquals(adhesionTag[i][j], cell.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
	
	@Test
	public void makeCell_multiplePopulationsWithTags_createsObject() {
		Location location = mock(Location.class);
		CellFactory factory = new CellFactoryMock();
		
		int cellID = (int)random() + 1;
		int cellPop = (int)random() + 1;
		double[] criticals = new double[] { random(), random() };
		double[] lambdas = new double[] { random(), random() };
		double[] adhesion = new double[] { random(), random(), random(), random() };
		
		double[][] criticalsTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] lambdasTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		double[][] adhesionTag = new double[][] {
				{ random(), random(), random() },
				{ random(), random(), random() },
				{ random(), random(), random() }
		};
		
		MiniBox population = new MiniBox();
		population.put("CODE", cellPop);
		population.put("ADHESION:*", adhesion[0]);
		population.put("ADHESION:B", adhesion[1]);
		population.put("ADHESION:C", adhesion[2]);
		population.put("ADHESION:D", adhesion[3]);
		population.put("TAG/a", 0);
		population.put("TAG/b", 0);
		population.put("TAG/c", 0);
		population.put("LAMBDA_VOLUME", lambdas[0]);
		population.put("LAMBDA_SURFACE", lambdas[1]);
		population.put("CRITICAL_VOLUME", criticals[0]);
		population.put("CRITICAL_SURFACE", criticals[1]);
		
		int tags = 3;
		String[] _tags = new String[] { "a", "b", "c" };
		for (int i = 0; i < tags; i++) {
			String tag = _tags[i];
			population.put(tag + TAG_SEPARATOR + "LAMBDA_VOLUME", lambdasTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "LAMBDA_SURFACE", lambdasTag[1][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_VOLUME", criticalsTag[0][i]);
			population.put(tag + TAG_SEPARATOR + "CRITICAL_SURFACE", criticalsTag[1][i]);
			
			for (int j = 0; j < tags; j++) {
				population.put(tag + TAG_SEPARATOR + "ADHESION" + TARGET_SEPARATOR + _tags[j], adhesionTag[i][j]);
			}
		}
		
		HashSet<String> keys = new HashSet<>();
		keys.add("B"); keys.add("C"); keys.add("D");
		HashMap<String, MiniBox> populations = mock(HashMap.class);
		when(populations.keySet()).thenReturn(keys);
		when(populations.get("B")).thenReturn(mock(MiniBox.class));
		when(populations.get("B").getInt("CODE")).thenReturn(1);
		when(populations.get("C")).thenReturn(mock(MiniBox.class));
		when(populations.get("C").getInt("CODE")).thenReturn(2);
		when(populations.get("D")).thenReturn(mock(MiniBox.class));
		when(populations.get("D").getInt("CODE")).thenReturn(3);
		Cell cell = factory.make(cellID, population, location, populations);
		
		assertTrue(cell instanceof PottsCell);
		assertEquals(location, cell.getLocation());
		assertEquals(cellID, cell.getID());
		assertEquals(cellPop, cell.getPop());
		assertEquals(criticals[0], cell.getCriticalVolume(), EPSILON);
		assertEquals(criticals[1], cell.getCriticalSurface(), EPSILON);
		assertEquals(lambdas[0], cell.getLambda(TERM_VOLUME), EPSILON);
		assertEquals(lambdas[1], cell.getLambda(TERM_SURFACE), EPSILON);
		assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
		assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
		assertEquals(adhesion[2], cell.getAdhesion(2), EPSILON);
		assertEquals(adhesion[3], cell.getAdhesion(3), EPSILON);
		
		for (int i = 0; i < tags; i++) {
			assertEquals(criticalsTag[0][i], cell.getCriticalVolume(-i - 1), EPSILON);
			assertEquals(criticalsTag[1][i], cell.getCriticalSurface(-i - 1), EPSILON);
			assertEquals(lambdasTag[0][i], cell.getLambda(TERM_VOLUME, -i - 1), EPSILON);
			assertEquals(lambdasTag[1][i], cell.getLambda(TERM_SURFACE, -i - 1), EPSILON);
			
			for (int j = 0; j < tags; j++) {
				assertEquals(adhesionTag[i][j], cell.getAdhesion(-i - 1, -j - 1), EPSILON);
			}
		}
	}
	
	@Test
	public void getIDs_validRangeSingleCall_returnsList() {
		CellFactory factory = spy(new CellFactoryMock());
		
		MiniBox population = new MiniBox();
		population.put("FRACTION", 0.5);
		
		int n = (int)random() + 1;
		ArrayList<Integer> ids = factory.getIDs(2*n, population);
		HashSet<Integer> set = new HashSet<>(ids);
		
		assertEquals(n, set.size());
	}
	
	@Test
	public void getIDs_validRangeMultipleCalls_returnsList() {
		CellFactory factory = spy(new CellFactoryMock());
		
		MiniBox populationA = new MiniBox();
		MiniBox populationB = new MiniBox();
		populationA.put("FRACTION", 0.25);
		populationB.put("FRACTION", 0.5);
		
		int n = (int)random() + 1;
		ArrayList<Integer> idsA = factory.getIDs(4*n, populationA);
		ArrayList<Integer> idsB = factory.getIDs(4*n, populationB);
		
		HashSet<Integer> set = new HashSet<>();
		set.addAll(idsA);
		set.addAll(idsB);
		
		assertEquals(3*n, set.size());
	}
	
	@Test
	public void getIDs_invalidRange_skipsExtra() {
		CellFactory factory = spy(new CellFactoryMock());
		
		MiniBox population = new MiniBox();
		population.put("FRACTION", 2.0);
		
		int n = (int)random() + 1;
		ArrayList<Integer> ids = factory.getIDs(n, population);
		
		HashSet<Integer> set = new HashSet<>(ids);
		assertEquals(n, set.size());
	}
}
