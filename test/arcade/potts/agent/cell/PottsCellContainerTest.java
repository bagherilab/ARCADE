package arcade.potts.agent.cell;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.potts.env.location.PottsLocation;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Region;
import static arcade.potts.util.PottsEnums.State;

public class PottsCellContainerTest {
    private static final double EPSILON = 1E-10;

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());

    @Test
    public void constructor_noRegions_setsFields() {
        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        double criticalVolume = randomDoubleBetween(0, 100);
        double criticalHeight = randomDoubleBetween(0, 100);

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        id,
                        parent,
                        pop,
                        age,
                        divisions,
                        state,
                        phase,
                        voxels,
                        criticalVolume,
                        criticalHeight);

        assertEquals(id, cellContainer.id);
        assertEquals(parent, cellContainer.parent);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(divisions, cellContainer.divisions);
        assertEquals(state, cellContainer.state);
        assertEquals(phase, cellContainer.phase);
        assertEquals(voxels, cellContainer.voxels);
        assertNull(cellContainer.regionVoxels);
        assertEquals(criticalVolume, cellContainer.criticalVolume, EPSILON);
        assertEquals(criticalHeight, cellContainer.criticalHeight, EPSILON);
        assertNull(cellContainer.criticalRegionVolumes);
        assertNull(cellContainer.criticalRegionHeights);
    }

    @Test
    public void constructor_withRegions_setsFields() {
        int id = randomIntBetween(1, 10);
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        double criticalVolume = randomDoubleBetween(0, 100);
        double criticalHeight = randomDoubleBetween(0, 100);
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalRegionHeights = new EnumMap<>(Region.class);

        PottsCellContainer cellContainer =
                new PottsCellContainer(
                        id,
                        parent,
                        pop,
                        age,
                        divisions,
                        state,
                        phase,
                        voxels,
                        regionVoxels,
                        criticalVolume,
                        criticalHeight,
                        criticalRegionVolumes,
                        criticalRegionHeights);

        assertEquals(id, cellContainer.id);
        assertEquals(parent, cellContainer.parent);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(divisions, cellContainer.divisions);
        assertEquals(state, cellContainer.state);
        assertEquals(phase, cellContainer.phase);
        assertEquals(voxels, cellContainer.voxels);
        assertSame(regionVoxels, cellContainer.regionVoxels);
        assertEquals(criticalVolume, cellContainer.criticalVolume, EPSILON);
        assertEquals(criticalHeight, cellContainer.criticalHeight, EPSILON);
        assertSame(criticalRegionVolumes, cellContainer.criticalRegionVolumes);
        assertSame(criticalRegionHeights, cellContainer.criticalRegionHeights);
    }

    @Test
    public void getID_called_returnsValue() {
        int id = randomIntBetween(1, 10);
        PottsCellContainer cellContainer =
                new PottsCellContainer(id, 0, 0, 0, 0, null, null, 0, 0, 0);
        assertEquals(id, cellContainer.getID());
    }

    @Test
    public void convert_noRegions_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactory();

        int cellID = randomIntBetween(1, 10);
        int cellParent = randomIntBetween(1, 10);
        int cellPop = randomIntBetween(1, 10);
        int cellAge = randomIntBetween(1, 100);
        int cellDivisions = randomIntBetween(1, 100);
        State cellState = State.UNDEFINED;
        Phase cellPhase = Phase.UNDEFINED;
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = new MiniBox();
        parameters.put("CLASS", "");

        factory.popToParameters.put(cellPop, parameters);
        factory.popToRegions.put(cellPop, false);

        PottsCellContainer container =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        cellState,
                        cellPhase,
                        0,
                        criticalVolume,
                        criticalHeight);
        PottsCell cell = (PottsCell) container.convert(factory, location, RANDOM);

        assertEquals(location, cell.getLocation());
        assertEquals(cellID, cell.getID());
        assertEquals(cellParent, cell.getParent());
        assertEquals(cellPop, cell.getPop());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellDivisions, cell.getDivisions());
        assertEquals(cellState, cell.getState());
        assertEquals(parameters, cell.getParameters().popParameters);
        assertEquals(criticalVolume, cell.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell.getCriticalHeight(), EPSILON);
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        assertEquals(0, cell.getTargetSurface(), EPSILON);
    }

    @Test
    public void convert_withRegions_createsObject() {
        PottsLocation location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactory();

        int cellID = randomIntBetween(1, 10);
        int cellParent = randomIntBetween(1, 10);
        int cellPop = randomIntBetween(1, 10);
        int cellAge = randomIntBetween(1, 100);
        int cellDivisions = randomIntBetween(1, 100);
        State cellState = State.UNDEFINED;
        Phase cellPhase = Phase.UNDEFINED;
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        MiniBox parameters = new MiniBox();
        parameters.put("CLASS", "");

        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();

        EnumMap<Region, Double> criticalRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> criticalRegionHeights = new EnumMap<>(Region.class);

        Arrays.stream(Region.values())
                .forEach(region -> criticalRegionVolumes.put(region, randomDoubleBetween(0, 100)));
        Arrays.stream(Region.values())
                .forEach(region -> criticalRegionHeights.put(region, randomDoubleBetween(0, 100)));

        factory.popToParameters.put(cellPop, parameters);
        factory.popToRegions.put(cellPop, true);

        PottsCellContainer container =
                new PottsCellContainer(
                        cellID,
                        cellParent,
                        cellPop,
                        cellAge,
                        cellDivisions,
                        cellState,
                        cellPhase,
                        0,
                        new EnumMap<>(Region.class),
                        criticalVolume,
                        criticalHeight,
                        criticalRegionVolumes,
                        criticalRegionHeights);
        PottsCell cell = (PottsCell) container.convert(factory, location, RANDOM);

        assertEquals(location, cell.getLocation());
        assertEquals(cellID, cell.getID());
        assertEquals(cellParent, cell.getParent());
        assertEquals(cellPop, cell.getPop());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellDivisions, cell.getDivisions());
        assertEquals(cellState, cell.getState());
        assertEquals(parameters, cell.getParameters().popParameters);
        assertEquals(criticalVolume, cell.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell.getCriticalHeight(), EPSILON);
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        assertEquals(0, cell.getTargetSurface(), EPSILON);

        for (Region region : regionList) {
            double criticalVolumeRegion = criticalRegionVolumes.get(region);
            double criticalHeightRegion = criticalRegionHeights.get(region);
            assertEquals(criticalVolumeRegion, cell.getCriticalVolume(region), EPSILON);
            assertEquals(criticalHeightRegion, cell.getCriticalHeight(region), EPSILON);
            assertEquals(0, cell.getTargetVolume(region), EPSILON);
            assertEquals(0, cell.getTargetSurface(region), EPSILON);
        }
    }
}
