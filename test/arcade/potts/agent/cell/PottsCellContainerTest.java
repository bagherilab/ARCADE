package arcade.potts.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.EnumMap;
import java.util.EnumSet;
import arcade.core.agent.cell.Cell;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.env.loc.PottsLocation;
import static arcade.core.util.Enums.State;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Term;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.agent.cell.PottsCellFactoryTest.*;
import static arcade.core.TestUtilities.*;

public class PottsCellContainerTest {
    @Test
    public void constructor_noRegionsNoTargets_setsFields() {
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int voxels = randomIntBetween(1, 100);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, pop, voxels);
        
        assertEquals(id, cellContainer.id);
        assertEquals(pop, cellContainer.pop);
        assertEquals(0, cellContainer.age);
        assertEquals(State.PROLIFERATIVE, cellContainer.state);
        assertEquals(Phase.PROLIFERATIVE_G1, cellContainer.phase);
        assertEquals(voxels, cellContainer.voxels);
        assertNull(cellContainer.regionVoxels);
        assertEquals(0, cellContainer.targetVolume, EPSILON);
        assertEquals(0, cellContainer.targetSurface, EPSILON);
        assertNull(cellContainer.regionTargetVolume);
        assertNull(cellContainer.regionTargetSurface);
    }
    
    @Test
    public void constructor_withRegionsNoTargets_setsFields() {
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int voxels = randomIntBetween(1, 100);
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, pop, voxels, regionVoxels);
        
        assertEquals(id, cellContainer.id);
        assertEquals(pop, cellContainer.pop);
        assertEquals(0, cellContainer.age);
        assertEquals(State.PROLIFERATIVE, cellContainer.state);
        assertEquals(Phase.PROLIFERATIVE_G1, cellContainer.phase);
        assertEquals(voxels, cellContainer.voxels);
        assertSame(regionVoxels, cellContainer.regionVoxels);
        assertEquals(0, cellContainer.targetVolume, EPSILON);
        assertEquals(0, cellContainer.targetSurface, EPSILON);
        assertNull(cellContainer.regionTargetVolume);
        assertNull(cellContainer.regionTargetSurface);
    }
    
    @Test
    public void constructor_noRegionsWithTargets_setsFields() {
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        double targetVolume = randomDoubleBetween(0, 100);
        double targetSurface = randomDoubleBetween(0, 100);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, pop, age,
                state, phase, voxels, targetVolume, targetSurface);
        
        assertEquals(id, cellContainer.id);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(state, cellContainer.state);
        assertEquals(phase, cellContainer.phase);
        assertEquals(voxels, cellContainer.voxels);
        assertNull(cellContainer.regionVoxels);
        assertEquals(targetVolume, cellContainer.targetVolume, EPSILON);
        assertEquals(targetSurface, cellContainer.targetSurface, EPSILON);
        assertNull(cellContainer.regionTargetVolume);
        assertNull(cellContainer.regionTargetSurface);
    }
    
    @Test
    public void constructor_withRegionsWithTargets_setsFields() {
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        double targetVolume = randomDoubleBetween(0, 100);
        double targetSurface = randomDoubleBetween(0, 100);
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        EnumMap<Region, Double> regionTargetVolume = new EnumMap<>(Region.class);
        EnumMap<Region, Double> regionTargetSurface = new EnumMap<>(Region.class);
                
        PottsCellContainer cellContainer = new PottsCellContainer(id, pop, age,
                state, phase, voxels, regionVoxels, targetVolume, targetSurface,
                regionTargetVolume, regionTargetSurface);
        
        assertEquals(id, cellContainer.id);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(state, cellContainer.state);
        assertEquals(phase, cellContainer.phase);
        assertEquals(voxels, cellContainer.voxels);
        assertSame(regionVoxels, cellContainer.regionVoxels);
        assertEquals(targetVolume, cellContainer.targetVolume, EPSILON);
        assertEquals(targetSurface, cellContainer.targetSurface, EPSILON);
        assertSame(regionTargetVolume, cellContainer.regionTargetVolume);
        assertSame(regionTargetSurface, cellContainer.regionTargetSurface);
    }
    
    @Test
    public void getID_called_returnsValue() {
        int id = randomIntBetween(1, 10);
        PottsCellContainer cellContainer = new PottsCellContainer(id, 0, 0);
        assertEquals(id, cellContainer.getID());
    }
    
    @Test
    public void convert_noRegionsNoTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactoryTest.PottsCellFactoryMock();
        
        int cellID = randomIntBetween(1, 10);
        int cellPop = randomIntBetween(1, 10);
        int cellAge = randomIntBetween(1, 100);
        State cellState = State.random(RANDOM);
        Phase cellPhase = Phase.random(RANDOM);
        EnumMap<Term, Double> criticals = makeEnumMap();
        EnumMap<Term, Double> lambdas = makeEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 10),
                randomDoubleBetween(0, 10)
        };
        MiniBox parameters = mock(MiniBox.class);
        
        factory.popToCriticals.put(cellPop, criticals);
        factory.popToLambdas.put(cellPop, lambdas);
        factory.popToAdhesion.put(cellPop, adhesion);
        factory.popToParameters.put(cellPop, parameters);
        factory.popToRegions.put(cellPop, false);
        
        PottsCellContainer container = new PottsCellContainer(cellID, cellPop, cellAge,
                cellState, cellPhase, 0, null, 0, 0, null, null);
        Cell cell = container.convert(factory, location);
        
        assertTrue(cell instanceof PottsCell);
        assertEquals(location, cell.getLocation());
        assertEquals(cellID, cell.getID());
        assertEquals(cellPop, cell.getPop());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellState, cell.getState());
        assertEquals(parameters, cell.getParameters());
        assertEquals(cellPhase, ((PottsModule) cell.getModule()).getPhase());
        assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
        assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
        assertEquals(lambdas.get(Term.VOLUME), ((PottsCell) cell).getLambda(Term.VOLUME), EPSILON);
        assertEquals(lambdas.get(Term.SURFACE), ((PottsCell) cell).getLambda(Term.SURFACE), EPSILON);
        assertEquals(adhesion[0], ((PottsCell) cell).getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], ((PottsCell) cell).getAdhesion(1), EPSILON);
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        assertEquals(0, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void convert_withRegionsNoTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactoryTest.PottsCellFactoryMock();
        
        int cellID = randomIntBetween(1, 10);
        int cellPop = randomIntBetween(1, 10);
        int cellAge = randomIntBetween(1, 100);
        State cellState = State.random(RANDOM);
        Phase cellPhase = Phase.random(RANDOM);
        EnumMap<Term, Double> criticals = makeEnumMap();
        EnumMap<Term, Double> lambdas = makeEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 10),
                randomDoubleBetween(0, 10)
        };
        MiniBox parameters = mock(MiniBox.class);
        
        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();
        
        EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        factory.popToCriticals.put(cellPop, criticals);
        factory.popToLambdas.put(cellPop, lambdas);
        factory.popToAdhesion.put(cellPop, adhesion);
        factory.popToParameters.put(cellPop, parameters);
        
        factory.popToRegionCriticals.put(cellPop, criticalsRegion);
        factory.popToRegionLambdas.put(cellPop, lambdasRegion);
        factory.popToRegionAdhesion.put(cellPop, adhesionRegion);
        
        factory.popToRegions.put(cellPop, true);
        
        PottsCellContainer container = new PottsCellContainer(cellID, cellPop, cellAge,
                cellState, cellPhase, 0, null, 0, 0, null, null);
        Cell cell = container.convert(factory, location);
        
        assertTrue(cell instanceof PottsCell);
        assertEquals(location, cell.getLocation());
        assertEquals(cellID, cell.getID());
        assertEquals(cellPop, cell.getPop());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellState, cell.getState());
        assertEquals(parameters, cell.getParameters());
        assertEquals(cellPhase, ((PottsModule) cell.getModule()).getPhase());
        assertEquals(criticals.get(Term.VOLUME), cell.getCriticalVolume(), EPSILON);
        assertEquals(criticals.get(Term.SURFACE), cell.getCriticalSurface(), EPSILON);
        assertEquals(lambdas.get(Term.VOLUME), ((PottsCell) cell).getLambda(Term.VOLUME), EPSILON);
        assertEquals(lambdas.get(Term.SURFACE), ((PottsCell) cell).getLambda(Term.SURFACE), EPSILON);
        assertEquals(adhesion[0], ((PottsCell) cell).getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], ((PottsCell) cell).getAdhesion(1), EPSILON);
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        assertEquals(0, cell.getTargetSurface(), EPSILON);
        
        for (Region region : regionList) {
            EnumMap<Term, Double> criticalTerms = criticalsRegion.get(region);
            EnumMap<Term, Double> lambdaTerms = lambdasRegion.get(region);
            
            assertEquals(criticalTerms.get(Term.VOLUME), cell.getCriticalVolume(region), EPSILON);
            assertEquals(criticalTerms.get(Term.SURFACE), cell.getCriticalSurface(region), EPSILON);
            assertEquals(lambdaTerms.get(Term.VOLUME), ((PottsCell) cell).getLambda(Term.VOLUME, region), EPSILON);
            assertEquals(lambdaTerms.get(Term.SURFACE), ((PottsCell) cell).getLambda(Term.SURFACE, region), EPSILON);
            assertEquals(0, cell.getTargetVolume(region), EPSILON);
            assertEquals(0, cell.getTargetSurface(region), EPSILON);
            
            for (Region target : regionList) {
                assertEquals(adhesionRegion.get(region).get(target),
                        ((PottsCell) cell).getAdhesion(region, target), EPSILON);
            }
        }
    }
    
    @Test
    public void convert_noRegionsWithTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactoryTest.PottsCellFactoryMock();
        
        double targetVolume = randomDoubleBetween(1, 100);
        double targetSurface = randomDoubleBetween(1, 100);
        
        factory.popToCriticals.put(1, makeEnumMap());
        factory.popToLambdas.put(1, makeEnumMap());
        factory.popToAdhesion.put(1, new double[2]);
        factory.popToRegions.put(1, false);
        
        PottsCellContainer container = new PottsCellContainer(1, 1, 0,
                State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume, targetSurface);
        PottsCell cell = (PottsCell) container.convert(factory, location);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void convert_withRegionsWithTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactoryTest.PottsCellFactoryMock();
        
        double targetVolume = randomDoubleBetween(1, 100);
        double targetSurface = randomDoubleBetween(1, 100);
        
        factory.popToCriticals.put(1, makeEnumMap());
        factory.popToLambdas.put(1, makeEnumMap());
        factory.popToAdhesion.put(1, new double[2]);
        factory.popToRegions.put(1, true);
        
        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();
        
        EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        factory.popToRegionCriticals.put(1, criticalsRegion);
        factory.popToRegionLambdas.put(1, lambdasRegion);
        factory.popToRegionAdhesion.put(1, adhesionRegion);
        
        EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
        
        for (Region region : regionList) {
            targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
            targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
        }
        
        PottsCellContainer container = new PottsCellContainer(1, 1, 0,
                State.UNDEFINED, Phase.UNDEFINED, 0, null,
                targetVolume, targetSurface, targetRegionVolumes, targetRegionSurfaces);
        PottsCell cell = (PottsCell) container.convert(factory, location);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
        
        for (Region region : regionList) {
            assertEquals(targetRegionVolumes.get(region), cell.getTargetVolume(region), EPSILON);
            assertEquals(targetRegionSurfaces.get(region), cell.getTargetSurface(region),  EPSILON);
        }
    }
    
    @Test
    public void convert_withRegionsMixedTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactoryTest.PottsCellFactoryMock();
        
        double targetVolume = randomDoubleBetween(1, 100);
        double targetSurface = randomDoubleBetween(1, 100);
        
        factory.popToCriticals.put(1, makeEnumMap());
        factory.popToLambdas.put(1, makeEnumMap());
        factory.popToAdhesion.put(1, new double[2]);
        factory.popToRegions.put(1, true);
        
        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();
        
        EnumMap<Region, EnumMap<Term, Double>> criticalsRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        factory.popToRegionCriticals.put(1, criticalsRegion);
        factory.popToRegionLambdas.put(1, lambdasRegion);
        factory.popToRegionAdhesion.put(1, adhesionRegion);
        
        EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
        
        for (Region region : regionList) {
            targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
            targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
        }
        
        PottsCellContainer container1 = new PottsCellContainer(1, 1, 0,
                State.UNDEFINED, Phase.UNDEFINED, 0, null,
                targetVolume, targetSurface, targetRegionVolumes, null);
        PottsCell cell1 = (PottsCell) container1.convert(factory, location);
        
        assertEquals(targetVolume, cell1.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell1.getTargetSurface(), EPSILON);
        
        for (Region region : regionList) {
            assertEquals(0, cell1.getTargetVolume(region), EPSILON);
            assertEquals(0, cell1.getTargetSurface(region),  EPSILON);
        }
        
        PottsCellContainer container2 = new PottsCellContainer(1, 1, 0,
                State.UNDEFINED, Phase.UNDEFINED, 0, null,
                targetVolume, targetSurface, null, targetRegionSurfaces);
        PottsCell cell2 = (PottsCell) container2.convert(factory, location);
        
        assertEquals(targetVolume, cell2.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell2.getTargetSurface(), EPSILON);
        
        for (Region region : regionList) {
            assertEquals(0, cell2.getTargetVolume(region), EPSILON);
            assertEquals(0, cell2.getTargetSurface(region),  EPSILON);
        }
    }
}
