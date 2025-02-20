package arcade.patch.agent.cell;

import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.Schedule;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.agent.module.PatchModule;
import arcade.patch.agent.process.PatchProcessInflammation;
import arcade.patch.agent.process.PatchProcessMetabolism;
import arcade.patch.agent.process.PatchProcessSignaling;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.sim.PatchSimulation;
import arcade.patch.util.PatchEnums.Domain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;
import static arcade.core.ARCADETestUtilities.randomIntBetween;
import static arcade.patch.util.PatchEnums.AntigenFlag;
import static arcade.patch.util.PatchEnums.State;

public class PatchCellCARTCD4Test {

    private Parameters parameters;
    private PatchLocation location;
    private PatchCellContainer container;
    private PatchCellCARTCD4 cell;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        parameters = spy(new Parameters(new MiniBox(), null, null));
        location = mock(PatchLocation.class);

        int id = 1;
        int parentId = 1;
        int pop = 1;
        int age = randomIntBetween(1, 120950);
        int divisions = 10;
        double volume = randomDoubleBetween(100, 200);
        double height = randomDoubleBetween(4, 10);
        double criticalVolume = randomDoubleBetween(100, 200);
        double criticalHeight = randomDoubleBetween(4, 10);
        State state = State.UNDEFINED;
        ;

        container =
                new PatchCellContainer(
                        id,
                        parentId,
                        pop,
                        age,
                        divisions,
                        state,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);
        doReturn(0.0).when(parameters).getDouble(any(String.class));
        doReturn(0).when(parameters).getInt(any(String.class));
        when(parameters.getDouble("HETEROGENEITY")).thenReturn(0.0);
        when(parameters.getDouble("ENERGY_THRESHOLD")).thenReturn(1.0);

        when(parameters.getDouble("NECROTIC_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("EXHAU_FRAC")).thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("SENESCENT_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("ANERGIC_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getDouble("PROLIFERATIVE_FRACTION"))
                .thenReturn(randomIntBetween(40, 100) / 100.0);
        when(parameters.getInt("SELF_RECEPTORS")).thenReturn(randomIntBetween(100, 200));
        when(parameters.getDouble("SEARCH_ABILITY")).thenReturn(1.0);
        when(parameters.getDouble("CAR_AFFINITY")).thenReturn(10 * Math.pow(10, -7));
        when(parameters.getDouble("CAR_ALPHA")).thenReturn(3.0);
        when(parameters.getDouble("CAR_BETA")).thenReturn(0.01);
        when(parameters.getDouble("SELF_RECEPTOR_AFFINITY")).thenReturn(7.8E-6);
        when(parameters.getDouble("SELF_ALPHA")).thenReturn(3.0);
        when(parameters.getDouble("SELF_BETA")).thenReturn(0.02);
        when(parameters.getDouble("CONTACT_FRAC")).thenReturn(7.8E-6);
        when(parameters.getInt("MAX_ANTIGEN_BINDING")).thenReturn(10);
        when(parameters.getInt("CARS")).thenReturn(50000);

        when(parameters.getInt("APOPTOSIS_AGE")).thenReturn(120960);
        when(parameters.getInt("MAX_DENSITY")).thenReturn(54);

        cell = spy(new PatchCellCARTCD4(container, location, parameters));
        Field apoptosisAge = PatchCell.class.getDeclaredField("apoptosisAge");
        apoptosisAge.setAccessible(true);
        apoptosisAge.set(cell, 120958);

        Field maxDensity = PatchCell.class.getDeclaredField("maxDensity");
        maxDensity.setAccessible(true);
        maxDensity.set(cell, 54);
    }

    @Test
    public void step_increasesAge() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);
        int initialAge = cell.getAge();
        cell.step(sim);
        assertEquals(initialAge + 1, cell.getAge());
    }

    @Test
    public void step_whenEnergyIsLow_setsStateToApoptotic() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setEnergy(-1 * randomIntBetween(2, 5));
        cell.step(sim);

        assertEquals(State.APOPTOTIC, cell.getState());
        assertEquals(AntigenFlag.UNBOUND, cell.getBindingFlag());
        assertFalse(cell.getActivationStatus());
    }

    @Test
    public void step_whenEnergyIsNegativeAndMoreThanThreshold_setsStateToStarved() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setEnergy(-0.5);
        cell.step(sim);

        assertEquals(State.STARVED, cell.getState());
        assertEquals(AntigenFlag.UNBOUND, cell.getBindingFlag());
    }

    @Test
    public void step_whenEnergyIsNegativeAndLessThanThreshold_setsStateToApoptotic() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setEnergy(-1.5);
        cell.step(sim);

        assertEquals(State.APOPTOTIC, cell.getState());
    }

    @Test
    public void step_whenDivisionsAreZero_setsStateToSenescent() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.divisions = 0;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.step(sim);

        assertTrue(cell.getState() == State.APOPTOTIC || cell.getState() == State.SENESCENT);
        assertEquals(AntigenFlag.UNBOUND, cell.getBindingFlag());
        assertFalse(cell.getActivationStatus());
    }

    @Test
    public void step_whenBoundToBothAntigenAndSelf_setsStateToAnergic() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.setBindingFlag(AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR);
        cell.step(sim);

        assertTrue(cell.getState() == State.APOPTOTIC || cell.getState() == State.ANERGIC);
        assertEquals(AntigenFlag.UNBOUND, cell.getBindingFlag());
        assertFalse(cell.getActivationStatus());
    }

    @Test
    public void step_whenBoundToAntigen_setsStateToStimulatory() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.boundCARAntigensCount = 0;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);
        cell.setBindingFlag(AntigenFlag.BOUND_ANTIGEN);

        Schedule schedule = mock(Schedule.class);
        doReturn(true).when(schedule).scheduleOnce(any(Steppable.class));
        doReturn(schedule).when(sim).getSchedule();

        cell.step(sim);

        assertEquals(State.STIMULATORY, cell.getState());
        assertTrue(cell.getActivationStatus());
    }

    @Test
    public void step_whenActivated_setsStateToProliferative() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.activated = true;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.step(sim);

        assertEquals(State.PROLIFERATIVE, cell.getState());
    }

    @Test
    public void step_whenNotActivated_setsStateToMigratory() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.activated = false;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);

        cell.step(sim);

        assertTrue(cell.getState() == State.MIGRATORY || cell.getState() == State.PROLIFERATIVE);
    }

    @Test
    public void step_whenOverstimulated_setsStateToExhausted() {
        PatchSimulation sim = mock(PatchSimulation.class);
        cell.processes.put(Domain.METABOLISM, mock(PatchProcessMetabolism.class));
        cell.processes.put(Domain.SIGNALING, mock(PatchProcessSignaling.class));
        cell.processes.put(Domain.INFLAMMATION, mock(PatchProcessInflammation.class));
        PatchModule module = mock(PatchModule.class);
        MersenneTwisterFast random = mock(MersenneTwisterFast.class);
        doAnswer(
                        invocationOnMock -> {
                            cell.state = invocationOnMock.getArgument(0);
                            cell.boundCARAntigensCount = cell.maxAntigenBinding + 1;
                            cell.module = module;
                            return null;
                        })
                .when(cell)
                .setState(any(State.class));
        doReturn(new PatchCellTissue(container, location, parameters))
                .when(cell)
                .bindTarget(
                        any(Simulation.class),
                        any(PatchLocation.class),
                        any(MersenneTwisterFast.class));
        sim.random = random;
        cell.setState(State.UNDEFINED);
        cell.setBindingFlag(AntigenFlag.BOUND_ANTIGEN);

        cell.step(sim);

        assertTrue(cell.getState() == State.APOPTOTIC || cell.getState() == State.EXHAUSTED);
        assertEquals(AntigenFlag.UNBOUND, cell.getBindingFlag());
        assertFalse(cell.getActivationStatus());
    }
}
