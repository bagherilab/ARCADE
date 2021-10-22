package arcade.potts.agent.cell;

import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.Test;
import ec.util.MersenneTwisterFast;
import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.env.loc.PottsLocation;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.core.ARCADETestUtilities.*;
import static arcade.core.util.Enums.Region;
import static arcade.core.util.Enums.State;
import static arcade.potts.agent.cell.PottsCellFactoryTest.*;
import static arcade.potts.util.PottsEnums.Phase;
import static arcade.potts.util.PottsEnums.Term;

public class PottsCellContainerTest {
    private static final double EPSILON = 1E-10;
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast(randomSeed());
    
    @Test
    public void constructor_noRegionsNoTargets_setsFields() {
        int id = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 10);
        int divisions = randomIntBetween(1, 100);
        int voxels = randomIntBetween(1, 100);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, pop, age, divisions, voxels);
        
        assertEquals(id, cellContainer.id);
        assertEquals(0, cellContainer.parent);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(divisions, cellContainer.divisions);
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
        int age = randomIntBetween(1, 10);
        int divisions = randomIntBetween(1, 100);
        int voxels = randomIntBetween(1, 100);
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, pop, age, divisions, voxels, regionVoxels);
        
        assertEquals(id, cellContainer.id);
        assertEquals(0, cellContainer.parent);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(divisions, cellContainer.divisions);
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
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        double targetVolume = randomDoubleBetween(0, 100);
        double targetSurface = randomDoubleBetween(0, 100);
        
        PottsCellContainer cellContainer = new PottsCellContainer(id, parent, pop, age, divisions,
                state, phase, voxels, targetVolume, targetSurface);
        
        assertEquals(id, cellContainer.id);
        assertEquals(parent, cellContainer.parent);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(divisions, cellContainer.divisions);
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
        int parent = randomIntBetween(1, 10);
        int pop = randomIntBetween(1, 10);
        int age = randomIntBetween(1, 100);
        int divisions = randomIntBetween(1, 100);
        State state = State.random(RANDOM);
        Phase phase = Phase.random(RANDOM);
        int voxels = randomIntBetween(1, 100);
        double targetVolume = randomDoubleBetween(0, 100);
        double targetSurface = randomDoubleBetween(0, 100);
        EnumMap<Region, Integer> regionVoxels = new EnumMap<>(Region.class);
        EnumMap<Region, Double> regionTargetVolume = new EnumMap<>(Region.class);
        EnumMap<Region, Double> regionTargetSurface = new EnumMap<>(Region.class);
                
        PottsCellContainer cellContainer = new PottsCellContainer(id, parent, pop, age, divisions,
                state, phase, voxels, regionVoxels, targetVolume, targetSurface,
                regionTargetVolume, regionTargetSurface);
        
        assertEquals(id, cellContainer.id);
        assertEquals(parent, cellContainer.parent);
        assertEquals(pop, cellContainer.pop);
        assertEquals(age, cellContainer.age);
        assertEquals(divisions, cellContainer.divisions);
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
        PottsCellContainer cellContainer = new PottsCellContainer(id, 0, 0, 0, 0);
        assertEquals(id, cellContainer.getID());
    }
    
    @Test
    public void convert_noRegionsNoTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactory();
        
        int cellID = randomIntBetween(1, 10);
        int cellParent = randomIntBetween(1, 10);
        int cellPop = randomIntBetween(1, 10);
        int cellAge = randomIntBetween(1, 100);
        int cellDivisions = randomIntBetween(1, 100);
        State cellState = State.random(RANDOM);
        Phase cellPhase = Phase.random(RANDOM);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        EnumMap<Term, Double> lambdas = makeTermEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 10),
                randomDoubleBetween(0, 10)
        };
        MiniBox parameters = mock(MiniBox.class);
        
        factory.popToCriticalVolumes.put(cellPop, criticalVolume);
        factory.popToCriticalHeights.put(cellPop, criticalHeight);
        factory.popToLambdas.put(cellPop, lambdas);
        factory.popToAdhesion.put(cellPop, adhesion);
        factory.popToParameters.put(cellPop, parameters);
        factory.popToRegions.put(cellPop, false);
        
        PottsCellContainer container = new PottsCellContainer(cellID, cellParent, cellPop, cellAge,
                cellDivisions, cellState, cellPhase, 0, null, 0, 0, null, null);
        PottsCell cell = (PottsCell) container.convert(factory, location);
        
        assertEquals(location, cell.getLocation());
        assertEquals(cellID, cell.getID());
        assertEquals(cellParent, cell.getParent());
        assertEquals(cellPop, cell.getPop());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellDivisions, cell.getDivisions());
        assertEquals(cellState, cell.getState());
        assertEquals(parameters, cell.getParameters());
        assertEquals(cellPhase, ((PottsModule) cell.getModule()).getPhase());
        assertEquals(criticalVolume, cell.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell.getCriticalHeight(), EPSILON);
        assertEquals(lambdas.get(Term.VOLUME), cell.getLambda(Term.VOLUME), EPSILON);
        assertEquals(lambdas.get(Term.SURFACE), cell.getLambda(Term.SURFACE), EPSILON);
        assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        assertEquals(0, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void convert_withRegionsNoTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactory();
        
        int cellID = randomIntBetween(1, 10);
        int cellParent = randomIntBetween(1, 10);
        int cellPop = randomIntBetween(1, 10);
        int cellAge = randomIntBetween(1, 100);
        int cellDivisions = randomIntBetween(1, 100);
        State cellState = State.random(RANDOM);
        Phase cellPhase = Phase.random(RANDOM);
        double criticalVolume = randomDoubleBetween(10, 100);
        double criticalHeight = randomDoubleBetween(10, 100);
        EnumMap<Term, Double> lambdas = makeTermEnumMap();
        double[] adhesion = new double[] {
                randomDoubleBetween(0, 10),
                randomDoubleBetween(0, 10)
        };
        MiniBox parameters = mock(MiniBox.class);
        
        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();
        
        EnumMap<Region, Double> criticalVolumesRegion = makeRegionEnumMap();
        EnumMap<Region, Double> criticalHeightsRegion = makeRegionEnumMap();
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        factory.popToCriticalVolumes.put(cellPop, criticalVolume);
        factory.popToCriticalHeights.put(cellPop, criticalHeight);
        factory.popToLambdas.put(cellPop, lambdas);
        factory.popToAdhesion.put(cellPop, adhesion);
        factory.popToParameters.put(cellPop, parameters);
        
        factory.popToRegionCriticalVolumes.put(cellPop, criticalVolumesRegion);
        factory.popToRegionCriticalHeights.put(cellPop, criticalHeightsRegion);
        factory.popToRegionLambdas.put(cellPop, lambdasRegion);
        factory.popToRegionAdhesion.put(cellPop, adhesionRegion);
        
        factory.popToRegions.put(cellPop, true);
        
        PottsCellContainer container = new PottsCellContainer(cellID, cellParent, cellPop, cellAge,
                cellDivisions, cellState, cellPhase, 0, null, 0, 0, null, null);
        PottsCell cell = (PottsCell) container.convert(factory, location);
        
        assertEquals(location, cell.getLocation());
        assertEquals(cellID, cell.getID());
        assertEquals(cellParent, cell.getParent());
        assertEquals(cellPop, cell.getPop());
        assertEquals(cellAge, cell.getAge());
        assertEquals(cellDivisions, cell.getDivisions());
        assertEquals(cellState, cell.getState());
        assertEquals(parameters, cell.getParameters());
        assertEquals(cellPhase, ((PottsModule) cell.getModule()).getPhase());
        assertEquals(criticalVolume, cell.getCriticalVolume(), EPSILON);
        assertEquals(criticalHeight, cell.getCriticalHeight(), EPSILON);
        assertEquals(lambdas.get(Term.VOLUME), cell.getLambda(Term.VOLUME), EPSILON);
        assertEquals(lambdas.get(Term.SURFACE), cell.getLambda(Term.SURFACE), EPSILON);
        assertEquals(adhesion[0], cell.getAdhesion(0), EPSILON);
        assertEquals(adhesion[1], cell.getAdhesion(1), EPSILON);
        assertEquals(0, cell.getTargetVolume(), EPSILON);
        assertEquals(0, cell.getTargetSurface(), EPSILON);
        
        for (Region region : regionList) {
            double criticalVolumeRegion = criticalVolumesRegion.get(region);
            double criticalHeightRegion = criticalHeightsRegion.get(region);
            EnumMap<Term, Double> lambdaTerms = lambdasRegion.get(region);
            
            assertEquals(criticalVolumeRegion, cell.getCriticalVolume(region), EPSILON);
            assertEquals(criticalHeightRegion, cell.getCriticalHeight(region), EPSILON);
            assertEquals(lambdaTerms.get(Term.VOLUME), cell.getLambda(Term.VOLUME, region), EPSILON);
            assertEquals(lambdaTerms.get(Term.SURFACE), cell.getLambda(Term.SURFACE, region), EPSILON);
            assertEquals(0, cell.getTargetVolume(region), EPSILON);
            assertEquals(0, cell.getTargetSurface(region), EPSILON);
            
            for (Region target : regionList) {
                assertEquals(adhesionRegion.get(region).get(target),
                        cell.getAdhesion(region, target), EPSILON);
            }
        }
    }
    
    @Test
    public void convert_noRegionsWithTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactory();
        
        double targetVolume = randomDoubleBetween(1, 100);
        double targetSurface = randomDoubleBetween(1, 100);
        
        factory.popToCriticalVolumes.put(1, randomDoubleBetween(1, 10));
        factory.popToCriticalHeights.put(1, randomDoubleBetween(1, 10));
        factory.popToLambdas.put(1, makeTermEnumMap());
        factory.popToAdhesion.put(1, new double[2]);
        factory.popToRegions.put(1, false);
        
        PottsCellContainer container = new PottsCellContainer(1, 0, 1, 0, 0,
                State.UNDEFINED, Phase.UNDEFINED, 0, targetVolume, targetSurface);
        PottsCell cell = (PottsCell) container.convert(factory, location);
        
        assertEquals(targetVolume, cell.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell.getTargetSurface(), EPSILON);
    }
    
    @Test
    public void convert_withRegionsWithTarget_createsObject() {
        Location location = mock(PottsLocation.class);
        PottsCellFactory factory = new PottsCellFactory();
        
        double targetVolume = randomDoubleBetween(1, 100);
        double targetSurface = randomDoubleBetween(1, 100);
        
        factory.popToCriticalVolumes.put(1, randomDoubleBetween(1, 10));
        factory.popToCriticalHeights.put(1, randomDoubleBetween(1, 10));
        factory.popToLambdas.put(1, makeTermEnumMap());
        factory.popToAdhesion.put(1, new double[2]);
        factory.popToRegions.put(1, true);
        
        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();
        
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        factory.popToRegionCriticalVolumes.put(1, makeRegionEnumMap());
        factory.popToRegionCriticalHeights.put(1, makeRegionEnumMap());
        factory.popToRegionLambdas.put(1, lambdasRegion);
        factory.popToRegionAdhesion.put(1, adhesionRegion);
        
        EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
        
        for (Region region : regionList) {
            targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
            targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
        }
        
        PottsCellContainer container = new PottsCellContainer(1, 0, 1, 0, 0,
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
        PottsCellFactory factory = new PottsCellFactory();
        
        double targetVolume = randomDoubleBetween(1, 100);
        double targetSurface = randomDoubleBetween(1, 100);
        
        factory.popToCriticalVolumes.put(1, randomDoubleBetween(1, 10));
        factory.popToCriticalHeights.put(1, randomDoubleBetween(1, 10));
        factory.popToLambdas.put(1, makeTermEnumMap());
        factory.popToAdhesion.put(1, new double[2]);
        factory.popToRegions.put(1, true);
        
        EnumSet<Region> regionList = EnumSet.of(Region.NUCLEUS, Region.UNDEFINED);
        doReturn(regionList).when(location).getRegions();
        
        EnumMap<Region, EnumMap<Term, Double>> lambdasRegion = makeEnumMapRegion(regionList);
        EnumMap<Region, EnumMap<Region, Double>> adhesionRegion = makeEnumMapTarget(regionList);
        
        factory.popToRegionCriticalVolumes.put(1, makeRegionEnumMap());
        factory.popToRegionCriticalHeights.put(1, makeRegionEnumMap());
        factory.popToRegionLambdas.put(1, lambdasRegion);
        factory.popToRegionAdhesion.put(1, adhesionRegion);
        
        EnumMap<Region, Double> targetRegionVolumes = new EnumMap<>(Region.class);
        EnumMap<Region, Double> targetRegionSurfaces = new EnumMap<>(Region.class);
        
        for (Region region : regionList) {
            targetRegionVolumes.put(region, randomDoubleBetween(1, 100));
            targetRegionSurfaces.put(region, randomDoubleBetween(1, 100));
        }
        
        PottsCellContainer container1 = new PottsCellContainer(1, 0, 1, 0, 0,
                State.UNDEFINED, Phase.UNDEFINED, 0, null,
                targetVolume, targetSurface, targetRegionVolumes, null);
        PottsCell cell1 = (PottsCell) container1.convert(factory, location);
        
        assertEquals(targetVolume, cell1.getTargetVolume(), EPSILON);
        assertEquals(targetSurface, cell1.getTargetSurface(), EPSILON);
        
        for (Region region : regionList) {
            assertEquals(0, cell1.getTargetVolume(region), EPSILON);
            assertEquals(0, cell1.getTargetSurface(region),  EPSILON);
        }
        
        PottsCellContainer container2 = new PottsCellContainer(1, 0, 1, 0, 0,
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
