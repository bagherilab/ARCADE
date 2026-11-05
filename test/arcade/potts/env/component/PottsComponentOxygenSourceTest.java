package arcade.potts.env.component;

import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.sim.PottsSimulation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;
import static arcade.potts.util.PottsEnums.Ordering;

public class PottsComponentOxygenSourceTest {
    private static final double EPSILON = 1E-12;

    static MiniBox parameters(double permeability, double blood) {
        MiniBox parameters = new MiniBox();
        parameters.put("PERMEABILITY", permeability);
        parameters.put("BLOOD_O2", blood);
        parameters.put("MASK", "SUPPLY");
        return parameters;
    }

    // Creates a 4 x 3 lattice, with or without a diffuser.
    static PottsLattice makeLattice(boolean diffuser) {
        MiniBox parameters = new MiniBox();
        if (diffuser) {
            parameters.put("(OPERATION)" + TAG_SEPARATOR + "DIFFUSER", "");
            parameters.put("diffuser/DIFFUSIVITY", 10.0);
            parameters.put("diffuser/MAX_SUBSTEP", 2.0);
        }
        return new PottsLattice(4, 3, 1, 3.0, 1.0 / 60, parameters);
    }

    // Creates a mock simulation with an O2 layer and a SUPPLY mask layer.
    static PottsSimulation makeSim(PottsLattice oxygen, PottsLattice mask) {
        PottsSimulation sim = mock(PottsSimulation.class);
        doReturn(oxygen).when(sim).getLattice("O2");
        doReturn(mask).when(sim).getLattice("SUPPLY");
        return sim;
    }

    @Test
    public void schedule_called_schedulesAtComponents() {
        Schedule schedule = mock(Schedule.class);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.5, 60));
        source.schedule(schedule);
        verify(schedule).scheduleRepeating(source, Ordering.COMPONENTS.ordinal(), 1);
    }

    @Test
    public void step_givenMask_addsExchangeScaledByMask() {
        PottsLattice oxygen = makeLattice(true);
        PottsLattice mask = makeLattice(false);
        mask.getField()[0][0][0] = 1.0;
        mask.getField()[0][1][2] = 0.5;
        PottsSimulation sim = makeSim(oxygen, mask);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.4, 60));
        source.register(sim, "O2");

        source.step(sim);

        assertEquals(0.4, oxygen.getExchange()[0][0][0], EPSILON);
        assertEquals(0.4 * 60, oxygen.getExchangeTarget()[0][0][0], EPSILON);
        assertEquals(0.2, oxygen.getExchange()[0][1][2], EPSILON);
        assertEquals(0.2 * 60, oxygen.getExchangeTarget()[0][1][2], EPSILON);
        assertEquals(0, oxygen.getExchange()[0][2][1], EPSILON);
        assertEquals(0, oxygen.getField()[0][0][0], EPSILON);
    }

    @Test
    public void step_maskChangedAfterFirstStep_usesFirstMask() {
        PottsLattice oxygen = makeLattice(true);
        PottsLattice mask = makeLattice(false);
        mask.getField()[0][0][0] = 1.0;
        PottsSimulation sim = makeSim(oxygen, mask);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.4, 60));
        source.register(sim, "O2");

        source.step(sim);
        mask.getField()[0][3][2] = 1.0;
        source.step(sim);

        assertEquals(0.8, oxygen.getExchange()[0][0][0], EPSILON);
        assertEquals(0, oxygen.getExchange()[0][3][2], EPSILON);
    }

    @Test
    public void step_zeroPermeability_addsNothingAndNeedsNoLayers() {
        PottsSimulation sim = mock(PottsSimulation.class);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0, 60));
        source.register(sim, "O2");
        assertDoesNotThrow(() -> source.step(sim));
    }

    @Test
    public void step_layerWithoutDiffuser_throws() {
        PottsLattice mask = makeLattice(false);
        mask.getField()[0][0][0] = 1.0;
        PottsSimulation sim = makeSim(makeLattice(false), mask);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.4, 60));
        source.register(sim, "O2");

        Exception e = assertThrows(IllegalStateException.class, () -> source.step(sim));
        assertTrue(e.getMessage().contains("O2"));
    }

    @Test
    public void step_missingMask_throws() {
        PottsSimulation sim = makeSim(makeLattice(true), null);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.4, 60));
        source.register(sim, "O2");

        Exception e = assertThrows(IllegalStateException.class, () -> source.step(sim));
        assertTrue(e.getMessage().contains("SUPPLY"));
    }

    @Test
    public void step_maskWithOperations_throws() {
        PottsSimulation sim = makeSim(makeLattice(true), makeLattice(true));
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.4, 60));
        source.register(sim, "O2");

        assertThrows(IllegalStateException.class, () -> source.step(sim));
    }

    @Test
    public void step_negativeMask_throws() {
        PottsLattice mask = makeLattice(false);
        mask.getField()[0][1][1] = -1.0;
        PottsSimulation sim = makeSim(makeLattice(true), mask);
        PottsComponentOxygenSource source = new PottsComponentOxygenSource(parameters(0.4, 60));
        source.register(sim, "O2");

        assertThrows(IllegalStateException.class, () -> source.step(sim));
    }

    @Test
    public void constructor_negativePermeability_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PottsComponentOxygenSource(parameters(-1, 60)));
    }
}
