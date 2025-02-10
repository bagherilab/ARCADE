package arcade.patch.agent.cell;

import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.randomDoubleBetween;
import static arcade.core.ARCADETestUtilities.randomIntBetween;

public class PatchCellCARTTest {

    private PatchCellCART patchCellCART;

    private Parameters parameters;

    private PatchLocation location;

    private PatchCellContainer container;

    private static Simulation sim;

    private static PatchLocation loc;

    private static MersenneTwisterFast random;

    private static PatchGrid grid;

    private static PatchCellTissue tissueCell;

    private static Bag bag;

    static class PatchCellMock extends PatchCellCART {
        PatchCellMock(PatchCellContainer container, Location location, Parameters parameters) {
            super(container, location, parameters, null);
        }

        @Override
        public PatchCellContainer make(int newID, CellState newState, MersenneTwisterFast random) {
            return new PatchCellContainer(
                    newID,
                    id,
                    pop,
                    age,
                    divisions,
                    newState,
                    volume,
                    height,
                    criticalVolume,
                    criticalHeight);
        }

        @Override
        public void step(SimState state) {}
    }

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        parameters = spy(new Parameters(new MiniBox(), null, null));
        location = mock(PatchLocation.class);

        doReturn(0.0).when(parameters).getDouble(any(String.class));
        doReturn(0).when(parameters).getInt(any(String.class));

        int id = 1;
        int parentId = 1;
        int pop = 1;
        int age = randomIntBetween(1, 120950);
        int divisions = 10;
        double volume = randomDoubleBetween(100, 200);
        double height = randomDoubleBetween(4, 10);
        double criticalVolume = randomDoubleBetween(100, 200);
        double criticalHeight = randomDoubleBetween(4, 10);
        PatchEnums.State state = PatchEnums.State.UNDEFINED;

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

        patchCellCART = new PatchCellMock(container, location, parameters);
    }

    @BeforeAll
    public static void setupMocks() {
        sim = mock(Simulation.class);
        loc = mock(PatchLocation.class);
        random = mock(MersenneTwisterFast.class);
        grid = mock(PatchGrid.class);
        bag = new Bag();
        when(sim.getGrid()).thenReturn(grid);
        when(grid.getObjectsAtLocation(loc)).thenReturn(bag);
        when(loc.getNeighbors()).thenReturn(new ArrayList<Location>());
        when(loc.getVolume()).thenReturn(6000.0);
        tissueCell = mock(PatchCellTissue.class);
    }

    @Test
    public void bindTarget_noNeighbors_doesNotBind() {
        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        assertNull(result);
        assertEquals(PatchEnums.AntigenFlag.UNBOUND, patchCellCART.getBindingFlag());
    }

    @Test
    public void bindTarget_withSelfBinding_bindsToCell() {
        when(tissueCell.getCarAntigens()).thenReturn(10);
        when(tissueCell.getSelfAntigens()).thenReturn(10000000);
        when(random.nextDouble()).thenReturn(0.0000005);

        bag.add(tissueCell);

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        bag.clear();
        assertNotNull(result);
        assertEquals(PatchEnums.AntigenFlag.BOUND_CELL_RECEPTOR, patchCellCART.getBindingFlag());
    }

    @Test
    public void bindTarget_withSelfAndAntigenBinding_bindsToCell() {
        when(tissueCell.getCarAntigens()).thenReturn(5000);
        when(tissueCell.getSelfAntigens()).thenReturn(50000000);
        when(random.nextDouble()).thenReturn(0.0000005);

        bag.add(tissueCell);

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        bag.clear();
        assertNotNull(result);
        assertEquals(
                PatchEnums.AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR, patchCellCART.getBindingFlag());
    }

    @Test
    public void bindTarget_bindsToCell() {
        when(tissueCell.getCarAntigens()).thenReturn(5000);
        when(tissueCell.getSelfAntigens()).thenReturn(5000);
        when(random.nextDouble()).thenReturn(0.0000005);

        bag.add(tissueCell);

        PatchCellTissue result = patchCellCART.bindTarget(sim, loc, random);
        bag.clear();
        assertNotNull(result);
        assertEquals(PatchEnums.AntigenFlag.BOUND_ANTIGEN, patchCellCART.getBindingFlag());
    }

    @Test
    public void getActivationStatus_returnsStatus()
            throws NoSuchFieldException, IllegalAccessException {
        Field activation = PatchCellCART.class.getDeclaredField("activated");
        activation.setAccessible(true);
        activation.set(patchCellCART, true);
        assertTrue(patchCellCART.getActivationStatus());
    }
}
