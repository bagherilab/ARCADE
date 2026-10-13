package arcade.potts.agent.process;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.process.PottsProcessWound.Phenotype;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSeries;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Domain;
import arcade.potts.util.PottsEnums.State;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PottsProcessWoundTest {
    static final int ID = 7;

    static final int MAC_CODE = 2;

    PottsCell cell;

    State[] state;

    MiniBox pop;

    PottsProcessSecretion secretion;

    PottsSimulation sim;

    HashMap<String, Double> layers;

    int[][][] ids;

    double[][][] ecmField;

    MersenneTwisterFast random;

    double time;

    int tickOffset;

    @BeforeEach
    public final void setUp() throws Exception {
        pop = new MiniBox();
        pop.put("wound/ROLE", "FIBROBLAST");
        pop.put("wound/MACROPHAGE", "MAC");
        pop.put("wound/PDGF_PROLIFERATION", 0.1);
        pop.put("wound/PDGF_DIFFERENTIATION", 0.1);
        pop.put("wound/ECM_DIFFERENTIATION", 0.5);
        pop.put("wound/ECM_TENSION", 0.2);
        pop.put("wound/INF_APOPTOSIS", 5);
        pop.put("wound/DIFFERENTIATION_RATE", 0.2);
        pop.put("wound/MATURATION", 3);
        pop.put("wound/SENESCENCE_RATE", 0.02);
        pop.put("wound/T_NIS", 2);
        pop.put("wound/CLEARANCE", 0.1);
        pop.put("wound/DRUG_START", 10);
        pop.put("wound/DRUG_DURATION", 5);
        pop.put("wound/DRUG_KILL", 0.0);
        pop.put("wound/FIB_ECM", 1.0);
        pop.put("wound/MYOF_ECM", 2.0);
        pop.put("wound/MYOF_PDGF", 3.0);
        pop.put("wound/SEN_PDGF", 4.0);
        pop.put("wound/SEN_ECM", 5.0);
        pop.put("wound/SEN_INF", 6.0);
        pop.put("wound/SEN_MMP", 7.0);
        pop.put("wound/SPONTANEOUS_SENESCENCE", 0.0);
        pop.put("wound/BYSTANDER_CONTACT", 0.0);
        pop.put("wound/BYSTANDER_PARACRINE", 0.0);
        pop.put("wound/INF_BYSTANDER", 1000);
        pop.put("wound/BASAL_CLEARANCE", 0.0);
        pop.put("wound/CONTACT_FREE_FRACTION", 0.0);
        pop.put("wound/ECM_TISSUE_GROWTH", 0.0);
        for (String layer : new String[] {"PDGF", "ECM", "INF", "MMP"}) {
            pop.put("secretion/RATE_" + layer, 9.0);
            pop.put("secretion/REGION_" + layer, "BOUNDARY");
        }

        cell = mock(PottsCell.class);
        PottsLocation location = mock(PottsLocation.class);
        doReturn(new ArrayList<>(List.of(new Voxel(2, 2, 0)))).when(location).getVoxels();
        doReturn(location).when(cell).getLocation();
        doReturn(ID).when(cell).getID();
        doReturn(new Parameters(pop, null, null)).when(cell).getParameters();
        state = new State[] {State.QUIESCENT};
        doAnswer(i -> state[0]).when(cell).getState();
        doAnswer(
                        i -> {
                            state[0] = i.getArgument(0);
                            return null;
                        })
                .when(cell)
                .setState(any());
        secretion = new PottsProcessSecretion(cell);
        doReturn(secretion).when(cell).getProcess(Domain.SECRETION);

        layers = new HashMap<>();
        ecmField = new double[1][5][5];
        ids = new int[1][5][5];
        ids[0][2][2] = ID;
        sim = mock(PottsSimulation.class);
        for (String layer : new String[] {"PDGF", "ECM", "INF"}) {
            PottsLattice lattice = mock(PottsLattice.class);
            String l = layer;
            doAnswer(i -> layers.getOrDefault(l, 0.0)).when(lattice).getAverageValue(any());
            if (layer.equals("ECM")) {
                doReturn(ecmField).when(lattice).getField();
            }
            doReturn(lattice).when(sim).getLattice(layer);
        }
        Potts potts = mock(Potts.class);
        potts.ids = ids;
        doReturn(potts).when(sim).getPotts();
        PottsCell macrophage = mock(PottsCell.class);
        doReturn(MAC_CODE).when(macrophage).getPop();
        doReturn(macrophage).when(potts).getCell(9);
        PottsCell other = mock(PottsCell.class);
        doReturn(1).when(other).getPop();
        doReturn(other).when(potts).getCell(8);
        Schedule schedule = mock(Schedule.class);
        doAnswer(i -> time).when(schedule).getTime();
        doReturn(schedule).when(sim).getSchedule();
        PottsSeries series = mock(PottsSeries.class);
        series.populations = new HashMap<>();
        MiniBox macPop = new MiniBox();
        macPop.put("CODE", MAC_CODE);
        series.populations.put("MAC", macPop);
        doReturn(series).when(sim).getSeries();
        random = mock(MersenneTwisterFast.class);
        doReturn(0.0).when(random).nextDouble();
    }

    private PottsProcessWound make() {
        return new PottsProcessWound(cell);
    }

    private void setTickOffset(int offset) throws Exception {
        Field f = PottsSeries.class.getDeclaredField("tickOffset");
        f.setAccessible(true);
        f.setInt(sim.getSeries(), offset);
    }

    private PottsProcessWound senescent(int age) {
        PottsProcessWound p = make();
        p.phenotype = Phenotype.SENESCENT;
        p.senescentAge = age;
        return p;
    }

    @Test
    public void step_macrophageRole_forcesQuiescent() {
        pop.put("wound/ROLE", "MACROPHAGE");
        state[0] = State.PROLIFERATIVE;
        layers.put("PDGF", 10.0);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.MACROPHAGE, p.getPhenotype());
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_apoptotic_untouched() {
        state[0] = State.APOPTOTIC;
        layers.put("PDGF", 10.0);
        layers.put("ECM", 10.0);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
        assertEquals(State.APOPTOTIC, state[0]);
        assertEquals(9.0, secretion.getRate("ECM"), 0);
    }

    @Test
    public void step_proliferationHysteresis_switchesBothWays() {
        PottsProcessWound p = make();
        layers.put("PDGF", 0.11);
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
        // Between half and full threshold: stays proliferative.
        layers.put("PDGF", 0.07);
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
        layers.put("PDGF", 0.04);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_fibroblastDifferentiation_needsBothThresholds() {
        PottsProcessWound p = make();
        layers.put("PDGF", 0.2);
        layers.put("ECM", 0.1);
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
        layers.put("PDGF", 0.05);
        layers.put("ECM", 1.0);
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
        layers.put("PDGF", 0.2);
        p.step(random, sim);
        assertEquals(Phenotype.MYOFIBROBLAST, p.getPhenotype());
        assertEquals(0, p.getMyofibroblastAge());
        assertEquals(2.0, secretion.getRate("ECM"), 0);
        assertEquals(3.0, secretion.getRate("PDGF"), 0);
        assertEquals(0.0, secretion.getRate("INF"), 0);
    }

    @Test
    public void step_fibroblastSecretion_setsEcmOnly() {
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(1.0, secretion.getRate("ECM"), 0);
        assertEquals(0.0, secretion.getRate("PDGF"), 0);
        assertEquals(0.0, secretion.getRate("MMP"), 0);
    }

    @Test
    public void step_myofibroblastLowEcmAfterMaturation_apoptotic() {
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        layers.put("ECM", 0.1);
        for (int i = 0; i < 3; i++) {
            p.step(random, sim);
            assertNotEquals(State.APOPTOTIC, state[0]);
        }
        p.step(random, sim);
        assertEquals(State.APOPTOTIC, state[0]);
    }

    @Test
    public void step_myofibroblastHighInf_apoptoticImmediately() {
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        layers.put("ECM", 1.0);
        layers.put("INF", 6.0);
        p.step(random, sim);
        assertEquals(State.APOPTOTIC, state[0]);
    }

    @Test
    public void step_myofibroblastAfterMaturation_becomesSenescent() {
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        p.myofibroblastAge = 3;
        state[0] = State.PROLIFERATIVE;
        layers.put("ECM", 1.0);
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(0, p.getSenescentAge());
        assertEquals(State.QUIESCENT, state[0]);
        assertEquals("SENESCENT_FIBROGENIC", p.getPhenotypeLabel());
        assertEquals(4.0, secretion.getRate("PDGF"), 0);
    }

    @Test
    public void step_myofibroblastBeforeMaturation_notSenescent() {
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        layers.put("ECM", 1.0);
        p.step(random, sim);
        assertEquals(Phenotype.MYOFIBROBLAST, p.getPhenotype());
    }

    @Test
    public void step_senescent_switchesPhaseAtTnis() {
        PottsProcessWound p = senescent(0);
        state[0] = State.PROLIFERATIVE;
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        assertEquals(4.0, secretion.getRate("PDGF"), 0);
        assertEquals(5.0, secretion.getRate("ECM"), 0);
        assertEquals(0.0, secretion.getRate("INF"), 0);
        assertEquals(0.0, secretion.getRate("MMP"), 0);
        p.step(random, sim);
        assertEquals(4.0, secretion.getRate("PDGF"), 0);
        p.step(random, sim);
        assertEquals(3, p.getSenescentAge());
        assertEquals(0.0, secretion.getRate("PDGF"), 0);
        assertEquals(0.0, secretion.getRate("ECM"), 0);
        assertEquals(6.0, secretion.getRate("INF"), 0);
        assertEquals(7.0, secretion.getRate("MMP"), 0);
        assertEquals("SENESCENT_FIBROLYTIC", p.getPhenotypeLabel());
    }

    @Test
    public void step_clearance_onlyWhenFibrolyticAndTouchingMacrophage() {
        ids[0][1][2] = 9;
        // Fibrogenic: not cleared.
        PottsProcessWound p = senescent(0);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        // Fibrolytic and touching: cleared.
        p = senescent(5);
        p.step(random, sim);
        assertEquals(State.APOPTOTIC, state[0]);
        // Fibrolytic, touching only a non-macrophage: not cleared.
        state[0] = State.QUIESCENT;
        ids[0][1][2] = 8;
        p = senescent(5);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        // Fibrolytic, touching macrophage, but the roll fails.
        ids[0][1][2] = 9;
        doReturn(0.5).when(random).nextDouble();
        p = senescent(5);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_clearance_noMacrophagePopulation_neverClears() {
        pop.put("wound/MACROPHAGE", "NONE");
        ids[0][1][2] = 9;
        PottsProcessWound p = senescent(5);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_senolytic_killsOnlyInsideWindow() throws Exception {
        pop.put("wound/DRUG_KILL", 0.5);
        setTickOffset(4);
        doReturn(0.0).when(random).nextDouble();
        // Absolute tick 4 + 5 = 9: before window.
        time = 5;
        senescent(0).step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        // 4 + 6 = 10: window start (inclusive).
        time = 6;
        senescent(0).step(random, sim);
        assertEquals(State.APOPTOTIC, state[0]);
        // 4 + 11 = 15: window end (exclusive).
        state[0] = State.QUIESCENT;
        time = 11;
        senescent(0).step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        // Inside window but roll fails.
        time = 8;
        doReturn(0.9).when(random).nextDouble();
        senescent(0).step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_noSecretionProcess_skipsRates() {
        doReturn(null).when(cell).getProcess(Domain.SECRETION);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(9.0, secretion.getRate("ECM"), 0);
        assertEquals(9.0, secretion.getRate("PDGF"), 0);
    }

    @Test
    public void step_missingLayer_treatedAsZero() {
        doReturn(null).when(sim).getLattice("PDGF");
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_layerNotSecreted_isSkipped() {
        pop.put("secretion/RATE_PDGF", 1.0);
        MiniBox only = new MiniBox();
        for (String key : pop.getKeys()) {
            if (!key.equals("secretion/REGION_PDGF")
                    && !key.equals("secretion/REGION_MMP")
                    && !key.equals("secretion/REGION_INF")) {
                only.put(key, pop.get(key));
            }
        }
        doReturn(new Parameters(only, null, null)).when(cell).getParameters();
        secretion = new PottsProcessSecretion(cell);
        doReturn(secretion).when(cell).getProcess(Domain.SECRETION);
        PottsProcessWound p = senescent(0);
        p.step(random, sim);
        assertEquals(5.0, secretion.getRate("ECM"), 0);
        assertNull(secretion.getRate("PDGF"));
    }

    private void addSenescentNeighbour() {
        ids[0][3][2] = 6;
        PottsCell neighbour = mock(PottsCell.class);
        PottsProcessWound np = new PottsProcessWound(neighbour);
        np.phenotype = Phenotype.SENESCENT;
        doReturn(np).when(neighbour).getProcess(Domain.WOUND);
        Potts potts = sim.getPotts();
        doReturn(neighbour).when(potts).getCell(6);
    }

    @Test
    public void step_spontaneousSenescence_fibrolyticImmediately() {
        pop.put("wound/SPONTANEOUS_SENESCENCE", 0.5);
        doReturn(0.1).when(random).nextDouble();
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(3, p.getSenescentAge());
        assertEquals("SENESCENT_FIBROLYTIC", p.getPhenotypeLabel());
        assertEquals(State.QUIESCENT, state[0]);
        assertEquals(6.0, secretion.getRate("INF"), 0);
        assertEquals(7.0, secretion.getRate("MMP"), 0);
        assertEquals(0.0, secretion.getRate("ECM"), 0);
    }

    @Test
    public void step_spontaneousSenescenceDrawAbove_unchanged() {
        pop.put("wound/SPONTANEOUS_SENESCENCE", 0.5);
        doReturn(0.9).when(random).nextDouble();
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
    }

    @Test
    public void step_spontaneousSenescence_notForMyofibroblast() {
        pop.put("wound/SPONTANEOUS_SENESCENCE", 0.5);
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        layers.put("ECM", 1.0);
        p.step(random, sim);
        assertEquals(Phenotype.MYOFIBROBLAST, p.getPhenotype());
    }

    @Test
    public void step_juxtacrineWithSenescentNeighbour_becomesFibrogenicSenescent() {
        pop.put("wound/BYSTANDER_CONTACT", 0.5);
        addSenescentNeighbour();
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        layers.put("ECM", 1.0);
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(0, p.getSenescentAge());
        assertEquals("SENESCENT_FIBROGENIC", p.getPhenotypeLabel());
        assertEquals(State.QUIESCENT, state[0]);
        assertEquals(4.0, secretion.getRate("PDGF"), 0);
    }

    @Test
    public void step_juxtacrineWithoutSenescentNeighbour_unchanged() {
        pop.put("wound/BYSTANDER_CONTACT", 0.5);
        ids[0][3][2] = 8; // non-wound neighbour
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
    }

    @Test
    public void step_juxtacrineNeighbourNotSenescent_unchanged() {
        pop.put("wound/BYSTANDER_CONTACT", 0.5);
        addSenescentNeighbour();
        PottsProcessWound np =
                (PottsProcessWound) sim.getPotts().getCell(6).getProcess(Domain.WOUND);
        np.phenotype = Phenotype.MYOFIBROBLAST;
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
    }

    @Test
    public void step_paracrineAboveThreshold_becomesSenescent() {
        pop.put("wound/BYSTANDER_PARACRINE", 0.5);
        pop.put("wound/INF_BYSTANDER", 2);
        layers.put("INF", 3.0);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(0, p.getSenescentAge());
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_paracrineAtOrBelowThreshold_unchanged() {
        pop.put("wound/BYSTANDER_PARACRINE", 0.5);
        pop.put("wound/INF_BYSTANDER", 2);
        layers.put("INF", 2.0);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
    }

    @Test
    public void step_basalClearance_killsSenescent() {
        pop.put("wound/BASAL_CLEARANCE", 0.5);
        PottsProcessWound p = senescent(0);
        p.step(random, sim);
        assertEquals(State.APOPTOTIC, state[0]);
    }

    @Test
    public void step_basalClearanceDrawAbove_survives() {
        pop.put("wound/BASAL_CLEARANCE", 0.5);
        doReturn(0.9).when(random).nextDouble();
        PottsProcessWound p = senescent(0);
        p.step(random, sim);
        assertNotEquals(State.APOPTOTIC, state[0]);
    }

    @Test
    public void step_defaultsZero_noNewRoutesAndNoExtraDraws() {
        addSenescentNeighbour();
        layers.put("INF", 100.0);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.FIBROBLAST, p.getPhenotype());
        PottsProcessWound s = senescent(5);
        s.step(random, sim);
        assertNotEquals(State.APOPTOTIC, state[0]);
        verify(random, never()).nextDouble();
    }

    private void fillNeighbours(int count) {
        int[][] pos = {{1, 2}, {3, 2}, {2, 1}, {2, 3}};
        for (int i = 0; i < count; i++) {
            ids[0][pos[i][0]][pos[i][1]] = 8;
        }
    }

    @Test
    public void step_blockedFibroblast_doesNotStartProliferating() {
        pop.put("wound/CONTACT_FREE_FRACTION", 0.5);
        fillNeighbours(3); // 1/4 free
        layers.put("PDGF", 0.5);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        assertFalse(p.isProliferating());
    }

    @Test
    public void step_unblockedFibroblast_startsProliferating() {
        pop.put("wound/CONTACT_FREE_FRACTION", 0.5);
        fillNeighbours(1); // 3/4 free
        layers.put("PDGF", 0.5);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
    }

    @Test
    public void step_proliferatingFibroblast_stopsWhenBlocked() {
        pop.put("wound/CONTACT_FREE_FRACTION", 0.5);
        fillNeighbours(3);
        state[0] = State.PROLIFERATIVE;
        layers.put("PDGF", 0.5);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        assertFalse(p.isProliferating());
    }

    @Test
    public void step_blockedMyofibroblast_stopsProliferating() {
        pop.put("wound/CONTACT_FREE_FRACTION", 0.5);
        fillNeighbours(4);
        state[0] = State.PROLIFERATIVE;
        layers.put("PDGF", 0.5);
        layers.put("ECM", 1.0);
        PottsProcessWound p = make();
        p.phenotype = Phenotype.MYOFIBROBLAST;
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_contactFractionZero_ignoresCrowding() {
        fillNeighbours(4);
        layers.put("PDGF", 0.5);
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
        layers.put("PDGF", 0.07);
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
        layers.put("PDGF", 0.04);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    private PottsProcessWound tissue() {
        pop.put("wound/ROLE", "TISSUE");
        return make();
    }

    @Test
    public void tissue_role_setsPhenotypeAndLabel() {
        PottsProcessWound p = tissue();
        assertEquals(Phenotype.TISSUE, p.getPhenotype());
        assertEquals("TISSUE", p.getPhenotypeLabel());
    }

    @Test
    public void step_tissueWithFreeSpace_proliferatesWithoutPdgf() {
        fillNeighbours(3);
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
        assertTrue(p.isProliferating());
    }

    @Test
    public void step_tissueFullySurrounded_staysQuiescent() {
        fillNeighbours(4);
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissueProliferating_stopsWhenSpaceLost() {
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
        fillNeighbours(4);
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissueBelowContactFraction_blocked() {
        pop.put("wound/CONTACT_FREE_FRACTION", 0.5);
        fillNeighbours(3);
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        state[0] = State.PROLIFERATIVE;
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissueEcmGate_bareFreeSpace_staysQuiescent() {
        pop.put("wound/ECM_TISSUE_GROWTH", 0.5);
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissueEcmGate_matrixInFreeSpace_proliferates() {
        pop.put("wound/ECM_TISSUE_GROWTH", 0.5);
        ecmField[0][1][2] = 0.5;
        ecmField[0][3][2] = 0.5;
        ecmField[0][2][1] = 0.5;
        ecmField[0][2][3] = 0.5;
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
    }

    @Test
    public void step_tissueEcmGate_proliferating_revertsWhenMatrixLost() {
        pop.put("wound/ECM_TISSUE_GROWTH", 0.5);
        state[0] = State.PROLIFERATIVE;
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        assertFalse(p.isProliferating());
    }

    @Test
    public void step_tissueEcmGateZero_ignoresMatrix() {
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.PROLIFERATIVE, state[0]);
    }

    @Test
    public void step_tissueEcmGate_ignoresEcmUnderOwnVoxels() {
        pop.put("wound/ECM_TISSUE_GROWTH", 0.5);
        ecmField[0][2][2] = 5.0;
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissueEcmGate_noFreeVoxels_staysQuiescent() {
        pop.put("wound/ECM_TISSUE_GROWTH", 0.5);
        fillNeighbours(4);
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissueEcmGate_noEcmLattice_staysQuiescent() {
        pop.put("wound/ECM_TISSUE_GROWTH", 0.5);
        doReturn(null).when(sim).getLattice("ECM");
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
    }

    @Test
    public void step_tissue_neverBecomesMyofibroblast() {
        layers.put("PDGF", 10.0);
        layers.put("ECM", 10.0);
        PottsProcessWound p = tissue();
        for (int i = 0; i < 5; i++) {
            p.step(random, sim);
        }
        assertEquals(Phenotype.TISSUE, p.getPhenotype());
    }

    @Test
    public void step_tissueSecretesNothingUntilSenescent() {
        PottsProcessWound p = tissue();
        p.step(random, sim);
        for (String layer : new String[] {"PDGF", "ECM", "INF", "MMP"}) {
            assertEquals(0.0, secretion.getRate(layer), 0);
        }
    }

    @Test
    public void step_tissueSpontaneousSenescence_entersFibrolyticPhase() {
        pop.put("wound/SPONTANEOUS_SENESCENCE", 0.5);
        doReturn(0.1).when(random).nextDouble();
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(3, p.getSenescentAge());
        assertEquals("SENESCENT_FIBROLYTIC", p.getPhenotypeLabel());
        assertEquals(State.QUIESCENT, state[0]);
        assertEquals(6.0, secretion.getRate("INF"), 0);
        assertEquals(7.0, secretion.getRate("MMP"), 0);
        assertEquals(0.0, secretion.getRate("PDGF"), 0);
    }

    @Test
    public void step_tissueJuxtacrine_entersFibrolyticPhase() {
        pop.put("wound/BYSTANDER_CONTACT", 0.5);
        addSenescentNeighbour();
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(3, p.getSenescentAge());
    }

    @Test
    public void step_tissueParacrine_entersFibrolyticPhase() {
        pop.put("wound/BYSTANDER_PARACRINE", 0.5);
        pop.put("wound/INF_BYSTANDER", 1.0);
        layers.put("INF", 2.0);
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(3, p.getSenescentAge());
    }

    @Test
    public void step_tissueBelowSenescenceDraw_unchanged() {
        pop.put("wound/SPONTANEOUS_SENESCENCE", 0.5);
        doReturn(0.9).when(random).nextDouble();
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(Phenotype.TISSUE, p.getPhenotype());
    }

    @Test
    public void step_tissueApoptotic_untouched() {
        state[0] = State.APOPTOTIC;
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.APOPTOTIC, state[0]);
        assertEquals(Phenotype.TISSUE, p.getPhenotype());
    }

    @Test
    public void step_fibroblastJuxtacrine_detectsSenescentNeighbourOfTissueOrigin() {
        pop.put("wound/BYSTANDER_CONTACT", 0.5);
        addSenescentNeighbour();
        // Neighbour started as a TISSUE cell and senesced into the fibrolytic phase.
        PottsProcessWound np =
                (PottsProcessWound) sim.getPotts().getCell(6).getProcess(Domain.WOUND);
        np.senescentAge = 3;
        PottsProcessWound p = make();
        p.step(random, sim);
        assertEquals(Phenotype.SENESCENT, p.getPhenotype());
        assertEquals(0, p.getSenescentAge());
    }

    @Test
    public void step_tissueTouchingPaddingOnly_blocked() {
        PottsLocation location = mock(PottsLocation.class);
        doReturn(new ArrayList<>(List.of(new Voxel(1, 1, 0)))).when(location).getVoxels();
        doReturn(location).when(cell).getLocation();
        secretion = new PottsProcessSecretion(cell);
        doReturn(secretion).when(cell).getProcess(Domain.SECRETION);
        ids[0][2][2] = 0;
        ids[0][1][1] = ID;
        ids[0][2][1] = 8;
        ids[0][1][2] = 8;
        // Neighbours (0,1) and (1,0) are padding (id 0) and must not count as free.
        PottsProcessWound p = tissue();
        p.step(random, sim);
        assertEquals(State.QUIESCENT, state[0]);
        assertFalse(p.isProliferating());
    }
}
