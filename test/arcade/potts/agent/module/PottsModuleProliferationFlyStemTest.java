package arcade.potts.agent.module;

import arcade.core.env.grid.Grid;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.State;
import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class PottsModuleProliferationFlyStemTest {

    private PottsModuleProliferationFlyStem flyStemModule;
    private PottsCellFlyStem mockFlyStemCell;
    private MersenneTwisterFast mockRandom;
    private PottsSimulation mockSimulation;
    private PottsLocation2D mockLocation;
    private Potts mockPotts;
    private MiniBox mockParameters;
    private Grid mockGrid;

    @Before
    public void setUp() {
        // Initialize mocks
        mockFlyStemCell = mock(PottsCellFlyStem.class);
        mockRandom = mock(MersenneTwisterFast.class);
        mockSimulation = mock(PottsSimulation.class);
        mockLocation = mock(PottsLocation2D.class);
        mockPotts = mock(Potts.class);
        mockParameters = mock(MiniBox.class);
        mockGrid = mock(Grid.class);

        // Stub the necessary methods for MiniBox and the cell
        when(mockFlyStemCell.getParameters()).thenReturn(mockParameters);

        // Stub parameters needed in the constructor of PottsModuleProliferationSimple
        when(mockParameters.getDouble(anyString())).thenReturn(1.0);  // Default double value
        when(mockParameters.getInt(anyString())).thenReturn(1);       // Default integer value

        // Stub methods for simulation
        when(mockSimulation.getPotts()).thenReturn(mockPotts);
        when(mockSimulation.getID()).thenReturn(1);  // Stub ID generation
        when(mockSimulation.getGrid()).thenReturn(mockGrid);

        // Stub methods for the cell
        when(mockFlyStemCell.getLocation()).thenReturn(mockLocation);

        // Instantiate the module with the mocked cell
        flyStemModule = new PottsModuleProliferationFlyStem(mockFlyStemCell);
    }

    @Test
    public void addCell_validInput_createsAndSchedulesNewCell() {

        ArrayList<Integer> splitOffsetPercent = new ArrayList<>(Arrays.asList(50, 50));
        Direction splitDirection = Direction.XY_PLANE;
        double splitProbability = 0.5;

        when(mockFlyStemCell.getSplitOffsetPercent()).thenReturn(splitOffsetPercent);
        when(mockFlyStemCell.getSplitDirection()).thenReturn(splitDirection);
        when(mockFlyStemCell.getVoxelGroupSelectionProbability()).thenReturn(splitProbability);

        // Mock the split method to return a new location
        PottsLocation2D newLocation = mock(PottsLocation2D.class);
        when(mockLocation.split(
                eq(mockRandom),
                eq(splitOffsetPercent),
                eq(splitDirection),
                eq(splitProbability))
        ).thenReturn(newLocation);

        // Use reflection to set 'ids' and 'regions' fields on mockPotts
        try {
            // Set 'ids' field
            Field idsField = Potts.class.getDeclaredField("ids");
            idsField.setAccessible(true);
            idsField.set(mockPotts, new int[1][1][1]); // Adjust size as needed

            // Set 'regions' field
            Field regionsField = Potts.class.getDeclaredField("regions");
            regionsField.setAccessible(true);
            regionsField.set(mockPotts, new int[1][1][1]); // Adjust size as needed
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            fail("Failed to set fields on mockPotts");
        }

        // Mock the creation of a new cell
        PottsCell newCell = mock(PottsCell.class);
        when(mockFlyStemCell.make(
                anyInt(),
                eq(State.PROLIFERATIVE),
                eq(newLocation),
                eq(mockRandom))
        ).thenReturn(newCell);

        when(mockSimulation.getSchedule()).thenReturn(null);

        flyStemModule.addCell(mockRandom, mockSimulation);

        // Verify that the current cell's reset method was called with the correct ids and regions
        verify(mockFlyStemCell).reset(eq(mockPotts.ids), eq(mockPotts.regions));

        // Verify that cell.make(...) was called with the correct arguments
        verify(mockFlyStemCell).make(eq(1), eq(State.PROLIFERATIVE), eq(newLocation), eq(mockRandom));

        // Verify that the new cell was added to the grid
        verify(mockGrid).addObject(eq(newCell), isNull());

        // Verify that potts.register(newCell) was called
        verify(mockPotts).register(eq(newCell));

        // Verify that newCell.reset(...) was called
        verify(newCell).reset(eq(mockPotts.ids), eq(mockPotts.regions));

        // Verify that newCell.schedule(...) was called with null (since schedule is null)
        verify(newCell).schedule(isNull());
    }
}
