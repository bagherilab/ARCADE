package arcade.potts.agent.cell;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.EnumMap;
import static arcade.core.util.Enums.State;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Phase;
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
}
