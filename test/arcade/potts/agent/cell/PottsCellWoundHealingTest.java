package arcade.potts.agent.cell;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.module.Module;
import arcade.core.agent.process.Process;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModuleApoptosis;
import arcade.potts.agent.module.PottsModuleAutosis;
import arcade.potts.agent.module.PottsModuleNecrosis;
import arcade.potts.agent.module.PottsModuleProliferationWoundHealing;
import arcade.potts.agent.module.PottsModuleQuiescence;
import arcade.potts.agent.module.PottsModuleSenescence;
import arcade.potts.env.location.Voxel;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.potts.util.PottsEnums.Domain;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellWoundHealingTest {
    private static final double EPSILON = 1E-10;

    private PottsCellWoundHealingFixture f;

    // Minimal wound healing cell with configurable rules.
    static final class WoundHealingCell extends PottsCellWoundHealing {
        boolean proliferate;

        boolean keep = true;

        int rulesCalls;

        int freeFractionCalls;

        WoundHealingCell(
                PottsCellContainer container,
                Location location,
                Parameters parameters,
                GrabBag links) {
            super(container, location, parameters, links);
        }

        @Override
        public boolean canProliferate(Simulation sim) {
            return proliferate;
        }

        @Override
        boolean stepRules(MersenneTwisterFast random, Simulation sim) {
            rulesCalls++;
            for (int i = 0; i < freeFractionCalls; i++) {
                getFreeFraction(sim);
            }
            return keep;
        }
    }

    @BeforeEach
    public final void setUp() {
        f = new PottsCellWoundHealingFixture();
    }

    // Creates a wound healing cell in the given state.
    private WoundHealingCell make(State state) {
        return new WoundHealingCell(f.container(state), f.location, f.parameters(), null);
    }

    // Creates a simulation state for stepping.
    private SimState simstate() {
        SimState simstate = f.sim;
        simstate.random = f.random;
        return simstate;
    }

    @Test
    public void step_quiescent_appliesRulesThenProcessesThenModule() {
        WoundHealingCell cell = make(State.QUIESCENT);
        Process process = mock(Process.class);
        Module module = mock(Module.class);
        cell.processes.put(Domain.SECRETION, process);
        cell.module = module;

        cell.step(simstate());

        assertEquals(1, cell.rulesCalls);
        assertEquals(11, cell.getAge());
        InOrder order = inOrder(process, module);
        order.verify(process).step(f.random, f.sim);
        order.verify(module).step(f.random, f.sim);
    }

    @Test
    public void step_proliferative_appliesRules() {
        WoundHealingCell cell = make(State.PROLIFERATIVE);
        cell.module = mock(Module.class);
        cell.step(simstate());
        assertEquals(1, cell.rulesCalls);
    }

    @Test
    public void step_senescent_skipsRules() {
        WoundHealingCell cell = make(State.SENESCENT);
        Module module = mock(Module.class);
        cell.module = module;
        cell.step(simstate());
        assertEquals(0, cell.rulesCalls);
        verify(module).step(f.random, f.sim);
    }

    @Test
    public void step_apoptotic_skipsRules() {
        WoundHealingCell cell = make(State.APOPTOTIC);
        cell.module = mock(Module.class);
        cell.step(simstate());
        assertEquals(0, cell.rulesCalls);
    }

    @Test
    public void step_rulesRemoveCell_skipsProcessesAndModule() {
        WoundHealingCell cell = make(State.QUIESCENT);
        cell.keep = false;
        Process process = mock(Process.class);
        Module module = mock(Module.class);
        cell.processes.put(Domain.SECRETION, process);
        cell.module = module;

        cell.step(simstate());

        verify(process, never()).step(any(), any());
        verify(module, never()).step(any(), any());
    }

    @Test
    public void setState_givenState_updatesModule() {
        WoundHealingCell cell = make(State.QUIESCENT);
        assertTrue(cell.getModule() instanceof PottsModuleQuiescence);

        cell.setState(State.PROLIFERATIVE);
        assertTrue(cell.getModule() instanceof PottsModuleProliferationWoundHealing);

        cell.setState(State.SENESCENT);
        assertTrue(cell.getModule() instanceof PottsModuleSenescence);

        cell.setState(State.APOPTOTIC);
        assertTrue(cell.getModule() instanceof PottsModuleApoptosis);

        cell.setState(State.NECROTIC);
        assertTrue(cell.getModule() instanceof PottsModuleNecrosis);

        cell.setState(State.AUTOTIC);
        assertTrue(cell.getModule() instanceof PottsModuleAutosis);

        cell.setState(State.UNDEFINED);
        assertNull(cell.getModule());
    }

    @Test
    public void make_noLinks_createsSamePopulationContainer() {
        WoundHealingCell cell = make(State.PROLIFERATIVE);
        PottsCellContainer container = cell.make(20, State.QUIESCENT, f.random);
        assertEquals(20, container.id);
        assertEquals(PottsCellWoundHealingFixture.ID, container.parent);
        assertEquals(1, container.pop);
        assertEquals(3, container.divisions);
        assertEquals(3, cell.getDivisions());
        assertEquals(State.QUIESCENT, container.state);
        assertNull(container.phase);
    }

    @Test
    public void getMean_givenLayer_returnsMean() {
        f.means.put("PDGF", 0.4);
        assertEquals(0.4, make(State.QUIESCENT).getMean(f.sim, "PDGF"), EPSILON);
    }

    @Test
    public void getMean_missingLayer_returnsZero() {
        assertEquals(0, make(State.QUIESCENT).getMean(f.sim, "MMP"), EPSILON);
    }

    @Test
    public void getFreeFraction_allFree_returnsOne() {
        assertEquals(1, make(State.QUIESCENT).getFreeFraction(f.sim), EPSILON);
    }

    @Test
    public void getFreeFraction_someOccupied_returnsFraction() {
        f.addNeighbor(8, 1, 2, mock(PottsCell.class));
        assertEquals(0.75, make(State.QUIESCENT).getFreeFraction(f.sim), EPSILON);
    }

    @Test
    public void getFreeFraction_atBorder_countsBorderAsOccupied() {
        f.setVoxels(new Voxel(1, 1, 0));
        assertEquals(0.5, make(State.QUIESCENT).getFreeFraction(f.sim), EPSILON);
    }

    @Test
    public void getFreeFraction_noBoundary_returnsZero() {
        f.setVoxels();
        assertEquals(0, make(State.QUIESCENT).getFreeFraction(f.sim), EPSILON);
    }

    @Test
    public void getFreeVoxels_threeDimensionalAtBorder_countsTopAndBottomAsOccupied() {
        int[][][] ids = new int[3][5][5];
        List<Voxel> boundary =
                List.of(
                        new Voxel(1, 2, 1),
                        new Voxel(3, 2, 1),
                        new Voxel(2, 1, 1),
                        new Voxel(2, 3, 1),
                        new Voxel(2, 2, 0),
                        new Voxel(2, 2, 2));
        assertEquals(4, PottsCellWoundHealing.getFreeVoxels(ids, boundary).size());
    }

    @Test
    public void getFreeVoxels_threeDimensionalInside_countsAboveAndBelowAsFree() {
        int[][][] ids = new int[5][5][5];
        ids[1][2][2] = 8;
        List<Voxel> boundary = List.of(new Voxel(2, 2, 1), new Voxel(2, 2, 3), new Voxel(1, 2, 2));
        List<Voxel> free = PottsCellWoundHealing.getFreeVoxels(ids, boundary);
        assertEquals(List.of(new Voxel(2, 2, 3), new Voxel(1, 2, 2)), free);
    }

    @Test
    public void step_helpersCalledTwice_computesBoundaryOncePerStep() {
        WoundHealingCell cell = make(State.QUIESCENT);
        cell.module = mock(Module.class);
        cell.freeFractionCalls = 2;

        cell.step(simstate());
        verify(f.location, times(1)).getVoxels();

        cell.step(simstate());
        verify(f.location, times(2)).getVoxels();
    }

    @Test
    public void getFreeMean_givenFreeVoxels_returnsMeanOverFreeVoxels() {
        f.addNeighbor(8, 1, 2, mock(PottsCell.class));
        f.ecm[0][1][2] = 9;
        f.ecm[0][3][2] = 1;
        f.ecm[0][2][1] = 2;
        f.ecm[0][2][3] = 3;
        assertEquals(2, make(State.QUIESCENT).getFreeMean(f.sim, "ECM"), EPSILON);
    }

    @Test
    public void getFreeMean_noFreeVoxels_returnsZero() {
        f.surround(8);
        f.ecm[0][1][2] = 9;
        assertEquals(0, make(State.QUIESCENT).getFreeMean(f.sim, "ECM"), EPSILON);
    }

    @Test
    public void touchesMacrophage_macrophageNeighbor_returnsTrue() {
        f.addNeighbor(8, 1, 2, mock(PottsCellMacrophage.class));
        assertTrue(make(State.QUIESCENT).touchesMacrophage(f.sim));
    }

    @Test
    public void touchesMacrophage_otherNeighbor_returnsFalse() {
        f.addNeighbor(8, 1, 2, mock(PottsCell.class));
        assertFalse(make(State.QUIESCENT).touchesMacrophage(f.sim));
    }

    @Test
    public void touchesSenescent_senescentNeighbor_returnsTrue() {
        PottsCell neighbor = mock(PottsCell.class);
        doReturn(State.SENESCENT).when(neighbor).getState();
        f.addNeighbor(8, 1, 2, neighbor);
        assertTrue(make(State.QUIESCENT).touchesSenescent(f.sim));
    }

    @Test
    public void touchesSenescent_otherNeighbor_returnsFalse() {
        PottsCell neighbor = mock(PottsCell.class);
        doReturn(State.QUIESCENT).when(neighbor).getState();
        f.addNeighbor(8, 1, 2, neighbor);
        assertFalse(make(State.QUIESCENT).touchesSenescent(f.sim));
    }

    @Test
    public void checkSpontaneousSenescence_zeroRate_doesNotDraw() {
        WoundHealingCell cell = make(State.QUIESCENT);
        assertFalse(cell.checkSpontaneousSenescence(f.random));
        verify(f.random, never()).nextDouble();
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void checkSpontaneousSenescence_drawBelowRate_becomesSenescent() {
        f.box.put("SPONTANEOUS_SENESCENCE", 0.5);
        WoundHealingCell cell = make(State.QUIESCENT);
        assertTrue(cell.checkSpontaneousSenescence(f.random));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void checkBystanderSenescence_contactWithSenescent_becomesSenescent() {
        f.box.put("BYSTANDER_CONTACT", 0.5);
        PottsCell neighbor = mock(PottsCell.class);
        doReturn(State.SENESCENT).when(neighbor).getState();
        f.addNeighbor(8, 1, 2, neighbor);
        WoundHealingCell cell = make(State.QUIESCENT);
        assertTrue(cell.checkBystanderSenescence(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void checkBystanderSenescence_noSenescentNeighbor_doesNotDraw() {
        f.box.put("BYSTANDER_CONTACT", 0.5);
        WoundHealingCell cell = make(State.QUIESCENT);
        assertFalse(cell.checkBystanderSenescence(f.random, f.sim));
        verify(f.random, never()).nextDouble();
    }

    @Test
    public void checkBystanderSenescence_infAboveThreshold_becomesSenescent() {
        f.box.put("BYSTANDER_PARACRINE", 0.5);
        f.box.put("INF_BYSTANDER", 2);
        f.means.put("INF", 3.0);
        WoundHealingCell cell = make(State.QUIESCENT);
        assertTrue(cell.checkBystanderSenescence(f.random, f.sim));
        assertEquals(State.SENESCENT, cell.getState());
    }

    @Test
    public void checkBystanderSenescence_infBelowThreshold_staysQuiescent() {
        f.box.put("BYSTANDER_PARACRINE", 0.5);
        f.box.put("INF_BYSTANDER", 2);
        f.means.put("INF", 1.0);
        WoundHealingCell cell = make(State.QUIESCENT);
        assertFalse(cell.checkBystanderSenescence(f.random, f.sim));
        assertEquals(State.QUIESCENT, cell.getState());
    }

    @Test
    public void isContactInhibited_givenThreshold_comparesFreeFraction() {
        f.box.put("CONTACT_FREE_FRACTION", 0.5);
        WoundHealingCell cell = make(State.QUIESCENT);
        assertTrue(cell.isContactInhibited(0.25));
        assertFalse(cell.isContactInhibited(0.5));
    }

    @Test
    public void isContactInhibited_zeroThreshold_returnsFalse() {
        assertFalse(make(State.QUIESCENT).isContactInhibited(0));
    }
}
