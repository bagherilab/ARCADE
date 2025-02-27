package arcade.potts.agent.module;

import org.junit.jupiter.api.Test;
import arcade.potts.agent.cell.PottsCellFlyNeuron;
import arcade.potts.util.PottsEnums.Phase;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class PottsModuleQuiescenceTest {

    @Test
    public void constructor_called_createsInstance() {
        PottsCellFlyNeuron cell = mock(PottsCellFlyNeuron.class);
        PottsModuleQuiescence module = new PottsModuleQuiescence(cell);
        assertNotNull(module);
        assert (module.getPhase() == Phase.UNDEFINED);
    }
}
