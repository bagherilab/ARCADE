package arcade.potts.agent.module;

import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCellFlyStem;
import arcade.potts.env.location.PottsLocation2D;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import ec.util.MersenneTwisterFast;
import org.junit.Before;

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
}