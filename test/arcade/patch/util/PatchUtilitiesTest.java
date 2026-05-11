package arcade.patch.util;

import org.junit.jupiter.api.Test;
import sim.util.Bag;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellTissue;
import arcade.patch.env.location.PatchLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatchUtilitiesTest {

    @Test
    public void constructor_called_throwsException() {
        assertThrows(UnsupportedOperationException.class, PatchUtilities::new);
    }

    @Test
    public void checkLocationHeight_emptyBag_returnsTrue() {
        PatchLocation loc = mock(PatchLocation.class);
        Bag bag = new Bag();

        assertTrue(PatchUtilities.checkLocationHeight(loc, bag, 0, null, false));
    }

    @Test
    public void checkLocationHeight_proposedHeightExceedsMaxHeight_returnsFalse() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(1.0).when(loc).getArea();
        PatchCell cell = mock(PatchCell.class);
        doReturn(10.0).when(cell).getVolume();
        Bag bag = new Bag();
        bag.add(cell);
        assertFalse(PatchUtilities.checkLocationHeight(loc, bag, 1, 10.0, false));
    }

    @Test
    public void checkLocationHeight_checkAllCriticalHeights_exceedsCriticalHeight_returnsFalse() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(1.0).when(loc).getArea();
        PatchCell cell = mock(PatchCell.class);
        doReturn(10.0).when(cell).getVolume();
        doReturn(1.0).when(cell).getCriticalHeight();
        Bag bag = new Bag();
        bag.add(cell);
        assertFalse(PatchUtilities.checkLocationHeight(loc, bag, 0, null, true));
    }

    @Test
    public void checkLocationHeight_checkAllCriticalHeights_belowCriticalHeight_returnsTrue() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(1.0).when(loc).getArea();
        PatchCell cell = mock(PatchCell.class);
        doReturn(10.0).when(cell).getVolume();
        doReturn(10.0).when(cell).getCriticalHeight();
        Bag bag = new Bag();
        bag.add(cell);
        assertTrue(PatchUtilities.checkLocationHeight(loc, bag, 0, null, true));
    }

    @Test
    public void checkLocationHeight_checkAllCriticalHeightsFalse_nonTissueCell_returnsTrue() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(1.0).when(loc).getArea();
        PatchCell cell = mock(PatchCell.class);
        doReturn(10.0).when(cell).getVolume();
        doReturn(1.0).when(cell).getCriticalHeight();
        Bag bag = new Bag();
        bag.add(cell);
        assertTrue(PatchUtilities.checkLocationHeight(loc, bag, 0, null, false));
    }

    @Test
    public void checkLocationHeight_checkAllCriticalHeightsFalse_tissueCell_checksHeight() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(1.0).when(loc).getArea();
        PatchCellTissue cell = mock(PatchCellTissue.class);
        doReturn(10.0).when(cell).getVolume();
        doReturn(9.0).when(cell).getCriticalHeight();
        Bag bag = new Bag();
        bag.add(cell);
        assertFalse(PatchUtilities.checkLocationHeight(loc, bag, 0, null, false));
    }

    @Test
    public void checkLocationDensity_atMaxDensity_returnsFalse() {
        PatchCell cell1 = mock(PatchCell.class);
        PatchCell cell2 = mock(PatchCell.class);
        PatchCell cell3 = mock(PatchCell.class);
        doReturn(1).when(cell1).getPop();
        doReturn(0).when(cell2).getPop();
        doReturn(1).when(cell2).getPop();
        Bag bag = new Bag();
        bag.add(cell1);
        bag.add(cell2);
        bag.add(cell3);
        assertFalse(PatchUtilities.checkLocationDensity(bag, 1, 2));
    }

    @Test
    public void checkLocationDensity_belowMaxDensity_returnsTrue() {
        PatchCell cell = mock(PatchCell.class);
        doReturn(1).when(cell).getPop();
        Bag bag = new Bag();
        bag.add(cell);
        assertTrue(PatchUtilities.checkLocationDensity(bag, 1, 2));
    }

    @Test
    public void checkLocationOccupancy_emptyBag_returnsTrue() {
        PatchLocation loc = mock(PatchLocation.class);
        Bag bag = new Bag();

        assertTrue(PatchUtilities.checkLocationOccupancy(bag, loc, null, 50.0));
    }

    @Test
    public void checkLocationOccupancy_atMaxOccupancy_returnsFalse() {
        PatchLocation loc = mock(PatchLocation.class);
        Bag bag = new Bag();
        bag.add(mock(PatchCell.class));
        bag.add(mock(PatchCell.class));
        assertFalse(PatchUtilities.checkLocationOccupancy(bag, loc, 2, 0));
    }

    @Test
    public void checkLocationOccupancy_volumeExceeded_returnsFalse() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(100.0).when(loc).getVolume();
        PatchCell cell = mock(PatchCell.class);
        doReturn(10.0).when(cell).getVolume();
        Bag bag = new Bag();
        bag.add(cell);
        assertFalse(PatchUtilities.checkLocationOccupancy(bag, loc, null, 100));
    }

    @Test
    public void checkLocationOccupancy_volumeNotExceeded_returnsTrue() {
        PatchLocation loc = mock(PatchLocation.class);
        doReturn(110.0).when(loc).getVolume();
        PatchCell cell = mock(PatchCell.class);
        doReturn(10.0).when(cell).getVolume();
        Bag bag = new Bag();
        bag.add(cell);
        assertTrue(PatchUtilities.checkLocationOccupancy(bag, loc, null, 100));
    }
}
