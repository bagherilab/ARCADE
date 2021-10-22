package arcade.potts.sim.hamiltonian;

import org.junit.Test;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.sim.Potts;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;

public class VolumeHamiltonianTest {
    private static final double EPSILON = 1E-10;
    private static final double LAMBDA = randomDoubleBetween(0, 10);
    private static final double REGION_LAMBDA = randomDoubleBetween(0, 10);
    
    static Potts makePottsMock() {
        Potts potts = mock(Potts.class);
        
        // Volumes for each cell domain.
        int[] volumes = new int[] { 4, 2, 4 };
        double[] targetVolumes = new double[] { 2, 3, 3 };
        
        // Volumes for each cell domain region.
        int[][] subvolumes = new int[][] { { 3, 1 }, { 2, 0 }, { 1, 1 } };
        double[] targetSubvolumes = new double[] { 2, 2 };
        
        int nCells = 3;
        doReturn(null).when(potts).getCell(0);
        
        for (int i = 0; i < nCells; i++) {
            PottsCell c = mock(PottsCell.class);
            
            // Assign volumes for the cell domain.
            doReturn(volumes[i]).when(c).getVolume();
            doReturn(targetVolumes[i]).when(c).getTargetVolume();
            
            // Assign lambda values for cell domain.
            doReturn(LAMBDA).when(c).getLambda(Term.VOLUME);
            
            // Assign volumes for cell regions.
            doReturn(subvolumes[i][0]).when(c).getVolume(Region.DEFAULT);
            doReturn(subvolumes[i][1]).when(c).getVolume(Region.NUCLEUS);
            doReturn(targetSubvolumes[0]).when(c).getTargetVolume(Region.DEFAULT);
            doReturn(targetSubvolumes[1]).when(c).getTargetVolume(Region.NUCLEUS);
            
            // Assign lambda values for cell regions.
            doReturn(REGION_LAMBDA).when(c).getLambda(Term.VOLUME, Region.DEFAULT);
            doReturn(REGION_LAMBDA).when(c).getLambda(Term.VOLUME, Region.NUCLEUS);
            
            doReturn(c).when(potts).getCell(i + 1);
        }
        
        return potts;
    }
    
    @Test
    public void getDelta_validIDs_calculatesValue() {
        Potts potts = makePottsMock();
        VolumeHamiltonian vh = new VolumeHamiltonian(potts);
        
        double cell1 = Math.pow(4 - 2, 2);
        double cell1plus1 = Math.pow(4 - 2 + 1, 2);
        double cell1minus1 = Math.pow(4 - 2 - 1, 2);
        double cell2 = Math.pow(2 - 3, 2);
        double cell2plus1 = Math.pow(2 - 3 + 1, 2);
        double cell2minus1 = Math.pow(2 - 3 - 1, 2);
        
        assertEquals(LAMBDA * (cell1minus1 - cell1 + cell2plus1 - cell2), vh.getDelta(1, 2, 0, 0, 0), EPSILON);
        assertEquals(LAMBDA * (cell2minus1 - cell2 + cell1plus1 - cell1), vh.getDelta(2, 1, 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getDelta_validRegions_calculatesValue() {
        Potts potts = makePottsMock();
        VolumeHamiltonian vh = new VolumeHamiltonian(potts);
        
        double subcell2 = Math.pow(1 - 2, 2);
        double subcell2plus1 = Math.pow(1 - 2 + 1, 2);
        double subcell2minus1 = Math.pow(1 - 2 - 1, 2);
        
        assertEquals(REGION_LAMBDA * (subcell2plus1 - subcell2),
                vh.getDelta(1, Region.DEFAULT.ordinal(), Region.NUCLEUS.ordinal(), 0, 0, 0), EPSILON);
        assertEquals(REGION_LAMBDA * (subcell2minus1 - subcell2),
                vh.getDelta(1, Region.NUCLEUS.ordinal(), Region.DEFAULT.ordinal(), 0, 0, 0), EPSILON);
    }
    
    @Test
    public void getVolume_validIDsNotZero_calculatesValue() {
        Potts potts = makePottsMock();
        VolumeHamiltonian vh = new VolumeHamiltonian(potts);
        
        assertEquals(LAMBDA * Math.pow(4 - 2, 2), vh.getVolume(1, 0), EPSILON);
        assertEquals(LAMBDA * Math.pow(4 - 2 + 1, 2), vh.getVolume(1, 1), EPSILON);
        assertEquals(LAMBDA * Math.pow(4 - 2 - 1, 2), vh.getVolume(1, -1), EPSILON);
    }

    @Test
    public void getVolume_validRegionsNotZero_calculatesValue() {
        Potts potts = makePottsMock();
        VolumeHamiltonian vh = new VolumeHamiltonian(potts);
        
        assertEquals(REGION_LAMBDA * Math.pow(1 - 2, 2), vh.getVolume(1, Region.NUCLEUS.ordinal(), 0), EPSILON);
        assertEquals(REGION_LAMBDA * Math.pow(1 - 2 + 1, 2), vh.getVolume(1, Region.NUCLEUS.ordinal(), 1), EPSILON);
        assertEquals(REGION_LAMBDA * Math.pow(1 - 2 - 1, 2), vh.getVolume(1, Region.NUCLEUS.ordinal(), -1), EPSILON);
    }
    
    @Test
    public void getVolume_zeroID_returnsZero() {
        Potts potts = makePottsMock();
        VolumeHamiltonian vh = new VolumeHamiltonian(potts);
        
        assertEquals(0, vh.getVolume(0, 1), EPSILON);
        assertEquals(0, vh.getVolume(0, 0), EPSILON);
        assertEquals(0, vh.getVolume(0, -1), EPSILON);
    }
    
    @Test
    public void getVolume_defaultRegion_returnsZero() {
        Potts potts = makePottsMock();
        VolumeHamiltonian vh = new VolumeHamiltonian(potts);
        
        assertEquals(0, vh.getVolume(0, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, vh.getVolume(0, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, vh.getVolume(0, Region.DEFAULT.ordinal(), -1), EPSILON);
        assertEquals(0, vh.getVolume(1, Region.DEFAULT.ordinal(), 1), EPSILON);
        assertEquals(0, vh.getVolume(1, Region.DEFAULT.ordinal(), 0), EPSILON);
        assertEquals(0, vh.getVolume(1, Region.DEFAULT.ordinal(), -1), EPSILON);
    }
}
