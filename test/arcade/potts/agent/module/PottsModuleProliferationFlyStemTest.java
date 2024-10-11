package arcade.potts.agent.module;

import arcade.core.util.MiniBox;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import ec.util.MersenneTwisterFast;
import sim.util.distribution.Poisson;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PottsModuleProliferationFlyStemTest {
    private static final double R = 1.0;
    
    private static final int N = 0;
    
    static MersenneTwisterFast random;
    
    static Poisson poissonMock;
    
    static PottsSimulation simMock;
    
    static MiniBox parameters;
    
    static Potts pottsMock;
    
    @BeforeClass
    public static void setupMocks() {
        random = mock(MersenneTwisterFast.class);
        doReturn(R).when(random).nextDouble();
        
        poissonMock = mock(Poisson.class);
        doReturn(N).when(poissonMock).nextInt();
        
        pottsMock = mock(Potts.class);
        
        simMock = mock(PottsSimulation.class);
        doReturn(pottsMock).when(simMock).getPotts();
    }
    
    @Test
    public void addCell_called_dividesCellAddsDaughterToGrid() {
        //TODO: Implement test
    }
}
