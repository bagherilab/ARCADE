package arcade.potts.env.lattice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import arcade.core.util.MiniBox;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.env.operation.PottsOperationDecayer;
import arcade.potts.env.operation.PottsOperationDiffuser;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsLatticeTest {
    private static final double EPSILON = 1E-12;

    static PottsLattice makeLattice(int length, int width) {
        return new PottsLattice(length, width, 1, 3.0, 1.0 / 60, new MiniBox());
    }

    static PottsLocation mockLocation(Voxel... voxels) {
        PottsLocation location = mock(PottsLocation.class);
        doReturn(new ArrayList<>(List.of(voxels))).when(location).getVoxels();
        return location;
    }

    @Test
    public void constructor_called_setsSizesAndZeroFields() {
        PottsLattice lattice = makeLattice(5, 4);
        assertEquals(5, lattice.getLength());
        assertEquals(4, lattice.getWidth());
        assertEquals(1, lattice.getHeight());
        assertEquals(3.0, lattice.getDs(), EPSILON);
        assertEquals(1.0 / 60, lattice.getDt(), EPSILON);
        assertEquals(1, lattice.getField().length);
        assertEquals(5, lattice.getField()[0].length);
        assertEquals(4, lattice.getField()[0][0].length);
        assertEquals(0, lattice.getField()[0][2][3], EPSILON);
    }

    @Test
    public void getTotalAndAverage_givenVoxels_usesOnlyThoseVoxels() {
        PottsLattice lattice = makeLattice(5, 5);
        lattice.getField()[0][1][1] = 2;
        lattice.getField()[0][1][2] = 4;
        lattice.getField()[0][3][3] = 100;
        PottsLocation location = mockLocation(new Voxel(1, 1, 0), new Voxel(1, 2, 0));
        assertEquals(6, lattice.getTotalValue(location), EPSILON);
        assertEquals(3, lattice.getAverageValue(location), EPSILON);
    }

    @Test
    public void getAverageValue_emptyLocation_returnsZero() {
        PottsLattice lattice = makeLattice(5, 5);
        assertEquals(0, lattice.getAverageValue(mockLocation()), EPSILON);
    }

    @Test
    public void updateIncrementSet_givenLocation_changesOnlyThoseVoxels() {
        PottsLattice lattice = makeLattice(5, 5);
        lattice.setField(2.0);
        PottsLocation location = mockLocation(new Voxel(2, 2, 0));
        lattice.updateValue(location, 0.5);
        assertEquals(1.0, lattice.getField()[0][2][2], EPSILON);
        lattice.incrementValue(location, 3.0);
        assertEquals(4.0, lattice.getField()[0][2][2], EPSILON);
        lattice.setValue(location, 7.0);
        assertEquals(7.0, lattice.getField()[0][2][2], EPSILON);
        assertEquals(2.0, lattice.getField()[0][0][0], EPSILON);
    }

    @Test
    public void updateValue_nan_doesNothing() {
        PottsLattice lattice = makeLattice(3, 3);
        lattice.setField(2.0);
        lattice.updateValue(mockLocation(new Voxel(1, 1, 0)), Double.NaN);
        assertEquals(2.0, lattice.getField()[0][1][1], EPSILON);
    }

    static MiniBox operationParameters(String... operations) {
        MiniBox parameters = new MiniBox();
        for (String operation : operations) {
            parameters.put("(OPERATION)" + TAG_SEPARATOR + operation, "");
        }
        parameters.put("diffuser/DIFFUSIVITY", 10.0);
        parameters.put("diffuser/MAX_SUBSTEP", 2.0);
        parameters.put("decayer/DECAY_RATE", 0.3);
        return parameters;
    }

    @Test
    public void constructor_noOperations_hasNoOperations() {
        PottsLattice lattice = makeLattice(3, 3);
        assertNull(lattice.getOperation(Category.DIFFUSER));
        assertNull(lattice.getOperation(Category.DECAYER));
    }

    @Test
    public void constructor_diffuserAndDecayer_createsOperations() {
        MiniBox parameters = operationParameters("DIFFUSER", "DECAYER");
        PottsLattice lattice = new PottsLattice(3, 3, 1, 3.0, 1.0 / 60, parameters);
        assertTrue(lattice.getOperation(Category.DIFFUSER) instanceof PottsOperationDiffuser);
        assertTrue(lattice.getOperation(Category.DECAYER) instanceof PottsOperationDecayer);
    }

    @Test
    public void constructor_unsupportedOperation_throws() {
        MiniBox undefined = operationParameters("UNDEFINED");
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsLattice(3, 3, 1, 3.0, 1.0 / 60, undefined));
        MiniBox unknown = operationParameters("FOO");
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsLattice(3, 3, 1, 3.0, 1.0 / 60, unknown));
    }

    @Test
    public void schedule_called_schedulesAtLattices() {
        PottsLattice lattice = makeLattice(3, 3);
        Schedule schedule = mock(Schedule.class);
        lattice.schedule(schedule);
        verify(schedule).scheduleRepeating(lattice, Ordering.LATTICES.ordinal(), 1);
    }

    @Test
    public void step_diffuserAndDecayer_diffusesThenDecays() {
        PottsLattice lattice =
                new PottsLattice(
                        8, 6, 1, 3.0, 1.0 / 60, operationParameters("DIFFUSER", "DECAYER"));
        PottsLattice expected =
                new PottsLattice(8, 6, 1, 3.0, 1.0 / 60, operationParameters("DIFFUSER"));
        Random rng = new Random(4);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                double v = rng.nextDouble();
                lattice.getField()[0][i][j] = v;
                expected.getField()[0][i][j] = v;
            }
        }

        lattice.step(mock(PottsSimulation.class));
        expected.getOperation(Category.DIFFUSER).step(null, null);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                double value = expected.getField()[0][i][j] * Math.exp(-0.3);
                assertEquals(value, lattice.getField()[0][i][j], EPSILON * value);
            }
        }
    }
}
