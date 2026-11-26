package arcade.potts.agent.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCellWoundHealing;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.agent.module.PottsModule.PoissonFactory;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.State;

public class PottsModuleProliferationWoundHealingTest {
    private PottsCellWoundHealing cell;

    private PottsSimulation sim;

    private MersenneTwisterFast random;

    @BeforeEach
    public final void setUp() {
        MiniBox box = new MiniBox();
        box.put("proliferation/SIZE_TARGET", 2);
        for (String id : new String[] {"G1", "S", "G2", "M"}) {
            box.put("proliferation/RATE_" + id, 1);
            box.put("proliferation/STEPS_" + id, 1);
        }
        box.put("proliferation/CELL_GROWTH_RATE", 0);
        box.put("proliferation/NUCLEUS_GROWTH_RATE", 0);
        box.put("proliferation/BASAL_APOPTOSIS_RATE", 0);
        box.put("proliferation/NUCLEUS_CONDENSATION_FRACTION", 0);

        cell = mock(PottsCellWoundHealing.class);
        doReturn(new Parameters(box, null, null)).when(cell).getParameters();
        sim = mock(PottsSimulation.class);
        random = mock(MersenneTwisterFast.class);
        doReturn(0.5).when(random).nextDouble();
    }

    // Creates a module whose Poisson draws return the given number of steps.
    private PottsModuleProliferationWoundHealing make(int steps) {
        PottsModuleProliferationWoundHealing module =
                new PottsModuleProliferationWoundHealing(cell);
        Poisson poisson = mock(Poisson.class);
        doReturn(steps).when(poisson).nextInt();
        PoissonFactory factory = mock(PoissonFactory.class);
        doReturn(poisson).when(factory).createPoisson(anyDouble(), any());
        module.poissonFactory = factory;
        return module;
    }

    @Test
    public void constructor_givenCell_startsInG1() {
        assertEquals(Phase.PROLIFERATIVE_G1, make(0).getPhase());
    }

    @Test
    public void step_g1CannotProliferate_becomesQuiescent() {
        doReturn(false).when(cell).canProliferate(sim);
        PottsModuleProliferationWoundHealing module = make(1);
        module.step(random, sim);
        verify(cell).setState(State.QUIESCENT);
        assertEquals(Phase.PROLIFERATIVE_G1, module.getPhase());
    }

    @Test
    public void step_g1CanProliferate_advancesCycle() {
        doReturn(true).when(cell).canProliferate(sim);
        PottsModuleProliferationWoundHealing module = make(1);
        module.step(random, sim);
        verify(cell, never()).setState(any());
        assertEquals(Phase.PROLIFERATIVE_S, module.getPhase());
    }

    @Test
    public void step_sCannotProliferate_continuesCycle() {
        doReturn(false).when(cell).canProliferate(sim);
        PottsModuleProliferationWoundHealing module = make(1);
        module.setPhase(Phase.PROLIFERATIVE_S);
        module.step(random, sim);
        verify(cell, never()).setState(any());
        verify(cell, never()).canProliferate(any());
        assertEquals(Phase.PROLIFERATIVE_G2, module.getPhase());
    }
}
