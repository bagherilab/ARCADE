package arcade.sim;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import static arcade.sim.Potts2D.*;
import arcade.agent.cell.Cell;
import arcade.env.grid.Grid;

public class Potts3DTest {
	Potts3D potts;
	
	@Before
	public void setupGrid() {
		Grid grid = mock(Grid.class);
		Series series = mock(Series.class);
		potts = new Potts3D(series, grid);
	}
	
	@Test
	public void getConnectivity_zeroNeighbors_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_oneNeighbor_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoLinkXY_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoLinkYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentNoLinkX_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithLinkXY_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithLinkYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_twoNeighborsCornerAdjacentWithLinkZX_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherLinkXY_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneLinkXY_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothLinksXY_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherLinkYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneLinkYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothLinksYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNeitherLinkZX_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOnlyOneLinkZX_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsBothLinksZX_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsNoLinksXYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsOneLinkXYZ_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsTwoLinksXYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_threeNeighborsAllLinksXYZ_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneTwoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneThreeLinks_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsPlaneAllLinks_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZNoLinks_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZOneLink_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
		
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksPlane_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwoLinksPlane_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksPlane_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwoLinksOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksOpposite_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksAdjacent_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwLinksAdjacent_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksAdjacent_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXTwoLinksCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYTwoLinksCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZTwoLinksCorner_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXFourLinksPlane_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYFourLinksPlane_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZFourLinksPlane_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXFourLinksCornerValid_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYFourLinksCornerValid_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZFourLinksCornerValid_returnsTrue() {
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertTrue(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraXFourLinksCornerInvalid_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{  true,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraYFourLinksCornerInvalid_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false, false, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
	}
	
	@Test
	public void getConnectivity_fourNeighborsTetraZFourLinksCornerInvalid_returnsFalse() {
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{  true,  true, false },
				{  true,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{  true,  true, false },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false,  true,  true },
				{ false,  true,  true },
				{ false, false, false }
			},
			{
				{ false,  true, false },
				{ false,  true,  true },
				{ false, false, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true,  true }
			},
			{
				{ false, false, false },
				{ false,  true,  true },
				{ false,  true, false }
			}
		}, false));
		
		// --------------------------------------------------
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			}
		}, false));
		
		assertFalse(potts.getConnectivity(new boolean[][][] {
			{
				{ false, false, false },
				{ false,  true, false },
				{ false, false, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{  true,  true, false }
			},
			{
				{ false, false, false },
				{  true,  true, false },
				{ false,  true, false }
			}
		}, false));
	}
}
