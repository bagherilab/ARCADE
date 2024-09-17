package arcade.potts.agent.module;

import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Direction;
import arcade.potts.util.PottsEnums.State;
import arcade.potts.util.PottsFlyEnums.StemDaughter;
import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static arcade.potts.util.PottsEnums.State;
import static arcade.potts.util.PottsEnums.Direction;
import static arcade.potts.util.PottsFlyEnums.StemDaughter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


public class PottsModuleProliferationFlyStemTest {

    private PottsModuleProliferationFlyStem flyStemModule;
    private PottsCellFlyStem mockFlyStemCell;
    private MersenneTwisterFast mockRandom;
    private PottsSimulation mockSimulation;
    private PottsLocation2D mockLocation;
    private Potts mockPotts;
    private MiniBox mockParameters;

    @Before
    public void setUp() {
        // Initialize mocks
        mockFlyStemCell = mock(PottsCellFlyStem.class);
        mockRandom = mock(MersenneTwisterFast.class);
        mockSimulation = mock(PottsSimulation.class);
        mockLocation = mock(PottsLocation2D.class);
        mockPotts = mock(Potts.class);
        mockParameters = mock(MiniBox.class);

        // Stub the necessary methods for MiniBox and the cell
        when(mockFlyStemCell.getParameters()).thenReturn(mockParameters);

        // Stub the parameters needed in the constructor of PottsModuleProliferationSimple
        when(mockParameters.getDouble(anyString())).thenReturn(1.0);  // Return a default value for all doubles
        when(mockParameters.getInt(anyString())).thenReturn(1);        // Return a default value for all integers

        // Stub the necessary methods for simulation and Potts
        when(mockSimulation.getPotts()).thenReturn(mockPotts);
        when(mockSimulation.getID()).thenReturn(1);  // Stub ID generation
        when(mockFlyStemCell.getLocation()).thenReturn(mockLocation);  // Return mocked location

        // Instantiate the module with the mocked cell
        flyStemModule = new PottsModuleProliferationFlyStem(mockFlyStemCell);
    }

    @Test
    public void testAddCell_callsCorrectMethods() {
        // Arrange
        when(mockFlyStemCell.splitOffsetPercent).thenReturn(50);  // Mock int return
        when(mockFlyStemCell.splitDirection).thenReturn(Direction.ZX_PLANE);
        when(mockFlyStemCell.getStemDaughter()).thenReturn(StemDaughter.APICAL);

        // Mock creation of a new cell and new location
        PottsCell mockNewCell = mock(PottsCell.class);
        when(mockFlyStemCell.make(anyInt(), any(State.class), any(Location.class), any(MersenneTwisterFast.class)))
                .thenReturn(mockNewCell);

        Location mockNewLocation = mock(Location.class);
        when(mockLocation.splitFly(any(MersenneTwisterFast.class), anyInt(), any(Direction.class), any(StemDaughter.class)))
                .thenReturn(mockNewLocation);

        // Act
        flyStemModule.addCell(mockRandom, mockSimulation);

        // Assert: Check interactions
        verify(mockLocation).splitFly(eq(mockRandom), eq(50), eq(Direction.ZX_PLANE), eq(StemDaughter.APICAL));
        verify(mockFlyStemCell).reset(mockPotts.ids, mockPotts.regions);
        verify(mockFlyStemCell).make(anyInt(), eq(State.PROLIFERATIVE), eq(mockNewLocation), eq(mockRandom));
        verify(mockSimulation.getGrid()).addObject(eq(mockNewCell), isNull());
        verify(mockPotts).register(eq(mockNewCell));
        verify(mockNewCell).reset(mockPotts.ids, mockPotts.regions);
        verify(mockNewCell).schedule(mockSimulation.getSchedule());
    }
}