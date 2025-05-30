package arcade.patch.env.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.patch.env.lattice.PatchLattice;
import arcade.patch.sim.PatchSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatchOperationDecayerTest {
    PatchLattice latticeMock;
    PatchSimulation simMock;
    MersenneTwisterFast randomMock;

    @BeforeEach
    public void setUp() {
        latticeMock = mock(PatchLattice.class);

        simMock = mock(PatchSimulation.class);
        randomMock = mock(MersenneTwisterFast.class);
    }

    @Test
    public void step_called_updatesLattice() {
        MiniBox parameters = new MiniBox();
        parameters.put("decayer/DECAY_RATE", "0.1");

        when(latticeMock.getParameters()).thenReturn(parameters);
        when(latticeMock.getLength()).thenReturn(3);
        when(latticeMock.getWidth()).thenReturn(3);
        when(latticeMock.getHeight()).thenReturn(3);
        double[][][] ones = createValuesArrays(1, 3);
        double[][][] expected = createValuesArrays(0.9, 3);

        when(latticeMock.getField()).thenReturn(ones);

        PatchOperationDecayer operation = new PatchOperationDecayer(latticeMock);
        operation.step(randomMock, simMock);

        // assert the values in the lattice have been updated
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    assertEquals(latticeMock.getField()[k][i][j], expected[k][i][j]);
                }
            }
        }
    }

    private double[][][] createValuesArrays(double value, int size) {
        double[][][] ones = new double[size][size][size];
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    ones[k][i][j] = value;
                }
            }
        }
        return ones;
    }
}
