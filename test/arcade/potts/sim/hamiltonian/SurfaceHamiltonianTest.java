package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;

public class SurfaceHamiltonianTest {
    private static final double EPSILON = 1E-10;
    private static final double LAMBDA = randomDoubleBetween(0, 10);
    private static final double REGION_LAMBDA = randomDoubleBetween(0, 10);
    
    static class SurfaceHamiltonianMock extends SurfaceHamiltonian {
        SurfaceHamiltonianMock(Potts potts) { super(potts); }
        
        @Override
        int[] calculateChange(int sourceID, int targetID, int x, int y, int z) {
            return new int[] { (sourceID == 1 ? 1 : -1), (targetID == 1 ? 1 : -1) };
        }

        @Override
        int[] calculateChange(int id, int sourceRegion, int targetRegion, int x, int y, int z) {
            if (sourceRegion == Region.DEFAULT.ordinal()) {
                return new int[] { 2, 2 };
            } else {
                return new int[] { -3, -3 };
            }
        }
    }
    
    static Potts makePottsMock() {
        Potts potts = mock(Potts.class);
        
        // Surfaces for each cell domain.
        int[] surfaces = new int[] { 8, 6, 8 };
        double[] targetSurfaces = new double[] { 10, 10, 8 };
        
        // Surfaces for each cell domain region.
        int[][] subsurfaces = new int[][] { { 6, 4 }, { 6, 0 }, { 4, 0 } };
        double[] targetSubsurfaces = new double[] { 8, 5 };
        
        int nCells = 3;
        doReturn(null).when(potts).getCell(0);
        
        for (int i = 0; i < nCells; i++) {
            PottsCell c = mock(PottsCell.class);
            
            // Assign surfaces for the cell domain.
            doReturn(surfaces[i]).when(c).getSurface();
            doReturn(targetSurfaces[i]).when(c).getTargetSurface();
            
            // Assign surfaces values for cell domain.
            doReturn(LAMBDA).when(c).getLambda(Term.SURFACE);
            
            // Assign volumes for cell regions.
            doReturn(subsurfaces[i][0]).when(c).getSurface(Region.DEFAULT);
            doReturn(subsurfaces[i][1]).when(c).getSurface(Region.NUCLEUS);
            doReturn(targetSubsurfaces[0]).when(c).getTargetSurface(Region.DEFAULT);
            doReturn(targetSubsurfaces[1]).when(c).getTargetSurface(Region.NUCLEUS);
            
            // Assign lambda values for cell regions.
            doReturn(REGION_LAMBDA).when(c).getLambda(Term.SURFACE, Region.DEFAULT);
            doReturn(REGION_LAMBDA).when(c).getLambda(Term.SURFACE, Region.NUCLEUS);
            
            doReturn(c).when(potts).getCell(i + 1);
        }
        
        return potts;
    }
    
    @Test
    public void constructor_called_setsObject() {
        Potts potts = mock(Potts.class);
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        assertEquals(potts, shm.potts);
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        Potts potts = makePottsMock();
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        
        double cell1 = Math.pow(8 - 10, 2);
        double cell1plus1 = Math.pow(8 - 10 + 1, 2);
        double cell2 = Math.pow(6 - 10, 2);
        double cell2minus1 = Math.pow(6 - 10 - 1, 2);
        
        assertEquals(LAMBDA * (cell1plus1 - cell1 + cell2minus1 - cell2), shm.getDelta(1, 2, 0, 0, 0), EPSILON);
        assertEquals(LAMBDA * (cell2minus1 - cell2 + cell1plus1 - cell1), shm.getDelta(2, 1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_calculatesValue() {
        Potts potts = makePottsMock();
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        
        double subcell2 = Math.pow(4 - 5, 2);
        double subcell2minus3 = Math.pow(4 - 5 - 3, 2);
        double subcell2plus2 = Math.pow(4 - 5 + 2, 2);
        
        assertEquals(REGION_LAMBDA * (subcell2minus3 - subcell2),
                shm.getDelta(1, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0), EPSILON);
        assertEquals(REGION_LAMBDA * (subcell2plus2 - subcell2),
                shm.getDelta(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getSurface_validIDsNotZero_calculatesValue() {
        Potts potts = makePottsMock();
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        
        assertEquals(LAMBDA * Math.pow(8 - 10, 2), shm.getSurface(1, 0), EPSILON);
        assertEquals(LAMBDA * Math.pow(8 - 10 + 1, 2), shm.getSurface(1, 1), EPSILON);
        assertEquals(LAMBDA * Math.pow(8 - 10 - 1, 2), shm.getSurface(1, -1), EPSILON);
    }
    
    @Test
    public void getSurface_validRegionsNotZero_calculatesValue() {
        Potts potts = makePottsMock();
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        
        assertEquals(REGION_LAMBDA * Math.pow(4 - 5, 2), shm.getSurface(1, Region.NUCLEUS.ordinal(), 0), EPSILON);
        assertEquals(REGION_LAMBDA * Math.pow(4 - 5 + 1, 2), shm.getSurface(1, Region.NUCLEUS.ordinal(), 1), EPSILON);
        assertEquals(REGION_LAMBDA * Math.pow(4 - 5 - 1, 2), shm.getSurface(1, Region.NUCLEUS.ordinal(), -1), EPSILON);
    }
    
    @Test
    public void getSurface_zeroID_returnsZero() {
        Potts potts = makePottsMock();
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        
        assertEquals(0, shm.getSurface(0, 1), EPSILON);
        assertEquals(0, shm.getSurface(0, 0), EPSILON);
        assertEquals(0, shm.getSurface(0, -1), EPSILON);
    }
    
    @Test
    public void getSurface_defaultRegion_returnsZero() {
        Potts potts = makePottsMock();
        SurfaceHamiltonianMock shm = new SurfaceHamiltonianMock(potts);
        
        assertEquals(0, shm.getSurface(0, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, shm.getSurface(0, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, shm.getSurface(0, Region.DEFAULT.ordinal(), -1), EPSILON);
        assertEquals(0, shm.getSurface(1, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, shm.getSurface(1, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, shm.getSurface(1, Region.DEFAULT.ordinal(), -1), EPSILON);
    }
}
