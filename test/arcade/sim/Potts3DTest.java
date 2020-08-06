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
	public void getConnectivity_twoNeighborsCornerAdjacentNoCornerXY_returnsFalse() {
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
	public void getConnectivity_twoNeighborsCornerAdjacentNoCornerYZ_returnsFalse() {
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
	public void getConnectivity_twoNeighborsCornerAdjacentNoCornerZX_returnsFalse() {
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
	public void getConnectivity_twoNeighborsCornerAdjacentWithCornerXY_returnsTrue() {
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
	public void getConnectivity_twoNeighborsCornerAdjacentWithCornerYZ_returnsTrue() {
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
	public void getConnectivity_twoNeighborsCornerAdjacentWithCornerZX_returnsTrue() {
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
}
