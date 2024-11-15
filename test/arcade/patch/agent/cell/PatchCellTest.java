package arcade.patch.agent.cell;

import org.junit.BeforeClass;
import org.junit.Test;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.util.MiniBox;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.util.PatchEnums.Domain;
import arcade.patch.util.PatchEnums.Flag;
import arcade.patch.util.PatchEnums.State;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static arcade.core.ARCADETestUtilities.*;

public class PatchCellTest {
    private static final double EPSILON = 1E-8;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());

    static int cellID = randomIntBetween(1, 10);

    static int cellParent = randomIntBetween(1, 10);

    static int cellPop = randomIntBetween(1, 10);

    static int cellAge = randomIntBetween(1, 1000);

    static int cellDivisions = randomIntBetween(1, 100);

    static double cellVolume = randomDoubleBetween(10, 100);

    static double cellHeight = randomDoubleBetween(10, 100);

    static double cellCriticalVolume = randomDoubleBetween(10, 100);

    static double cellCriticalHeight = randomDoubleBetween(10, 100);

    static State cellState = State.QUIESCENT;

    static PatchCellTissue cellDefault;

    static MiniBox parametersMock;
    static PatchLocation locationMock;

    @BeforeClass
    public static void setupMocks() {
        parametersMock = mock(MiniBox.class);
        locationMock = mock(PatchLocation.class);

        cellDefault =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
    }

    @Test
    public void getID_defaultConstructor_returnsValue() {
        assertEquals(cellID, cellDefault.getID());
    }

    @Test
    public void getParent_defaultConstructor_returnsValue() {
        assertEquals(cellParent, cellDefault.getParent());
    }

    @Test
    public void getParent_valueAssigned_returnsValue() {
        int parent = randomIntBetween(0, 100);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        parent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        assertEquals(parent, cell.getParent());
    }

    @Test
    public void getPop_defaultConstructor_returnsValue() {
        assertEquals(cellPop, cellDefault.getPop());
    }

    @Test
    public void getPop_valueAssigned_returnsValue() {
        int pop = randomIntBetween(0, 100);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        pop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        assertEquals(pop, cell.getPop());
    }

    @Test
    public void getState_defaultConstructor_returnsValue() {
        assertEquals(cellState, cellDefault.getState());
    }

    @Test
    public void getState_valueAssigned_returnsValue() {
        State state = State.random(RANDOM);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        cellPop,
                        state,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        assertEquals(state, cell.getState());
    }

    @Test
    public void getAge_defaultConstructor_returnsValue() {
        assertEquals(cellAge, cellDefault.getAge());
    }

    @Test
    public void getAge_valueAssigned_returnsValue() {
        int age = randomIntBetween(0, 100);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        age,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        assertEquals(age, cell.getAge());
    }

    @Test
    public void getDivisions_defaultConstructor_returnsValue() {
        assertEquals(cellDivisions, cellDefault.getDivisions());
    }

    @Test
    public void getDivisions_valueAssigned_returnsValue() {
        int divisions = randomIntBetween(0, 100);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        divisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        assertEquals(divisions, cell.getDivisions());
    }

    @Test
    public void getLocation_defaultConstructor_returnsObject() {
        assertSame(locationMock, cellDefault.getLocation());
    }

    @Test
    public void getModule_defaultConstructor_returnsNull() {
        assertEquals(cellDefault.getModule(), null);
    }

    @Test
    public void getProcess_defaultConstructor_returnsNull() {
        assertNull(cellDefault.getProcess(Domain.UNDEFINED));
    }

    @Test
    public void getParameters_defaultConstructor_returnsObject() {
        assertSame(parametersMock, cellDefault.getParameters());
    }

    @Test
    public void getVolume_defaultConstructor_returnsValue() {
        assertEquals(cellVolume, cellDefault.getVolume(), EPSILON);
    }

    @Test
    public void getHeight_defaultConstructor_returnsValue() {
        assertEquals(cellHeight, cellDefault.getHeight(), EPSILON);
    }

    @Test
    public void getCriticalVolume_defaultConstructor_returnsValue() {
        assertEquals(cellCriticalVolume, cellDefault.getCriticalVolume(), EPSILON);
    }

    @Test
    public void getCriticalHeight_defaultConstructor_returnsValue() {
        assertEquals(cellCriticalHeight, cellDefault.getCriticalHeight(), EPSILON);
    }

    @Test
    public void getEnergy_defaultConstructor_returnsValue() {
        assertEquals(0, cellDefault.getEnergy(), EPSILON);
    }

    @Test
    public void setEnergy_returnsValue() {
        double energy = randomDoubleBetween(0, 100);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        cell.setEnergy(energy);
        assertEquals(energy, cell.getEnergy(), EPSILON);
    }

    @Test
    public void setFlag_valueAssigned_returnsValue() {
        Flag flag = Flag.random(RANDOM);
        PatchCellTissue cell =
                new PatchCellTissue(
                        cellID,
                        cellParent,
                        cellPop,
                        cellState,
                        cellAge,
                        cellDivisions,
                        locationMock,
                        parametersMock,
                        cellVolume,
                        cellHeight,
                        cellCriticalVolume,
                        cellCriticalHeight);
        cell.setFlag(flag);
        assertEquals(flag, cell.flag);
    }

    @Test
    public void convert_createsContainer() {
        PatchLocation location = mock(PatchLocation.class);
        MiniBox parameters = mock(MiniBox.class);

        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.PROLIFERATIVE;
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        double volume = randomDoubleBetween(10, 100);
        double height = randomDoubleBetween(10, 100);

        PatchCellTissue cell =
                new PatchCellTissue(
                        id,
                        parent,
                        pop,
                        state,
                        age,
                        divisions,
                        location,
                        parameters,
                        volume,
                        height,
                        criticalVolume,
                        criticalHeight);

        PatchCellContainer container = (PatchCellContainer) cell.convert();

        assertEquals(id, container.id);
        assertEquals(parent, container.parent);
        assertEquals(pop, container.pop);
        assertEquals(age, container.age);
        assertEquals(divisions, container.divisions);
        assertEquals(state, container.state);
        assertEquals(criticalVolume, container.criticalVolume, EPSILON);
        assertEquals(criticalHeight, container.criticalHeight, EPSILON);
    }

    @Test
    public void calculate_total_volume_returnsValue() {
        Bag cells = new Bag();
        double runningSum = 0;
        for (int i = 0; i < randomIntBetween(1, 10); i++) {
            double v = randomDoubleBetween(10, 100);
            PatchCellTissue cell =
                    new PatchCellTissue(
                            cellID,
                            cellParent,
                            cellPop,
                            cellState,
                            cellAge,
                            cellDivisions,
                            locationMock,
                            parametersMock,
                            v,
                            cellHeight,
                            cellCriticalVolume,
                            cellCriticalHeight);
            cells.add(cell);
            runningSum += v;
        }
        assertEquals(runningSum, PatchCell.calculateTotalVolume(cells), EPSILON);
    }

    // once implemented, make sure location methods and step is working

}
