package arcade.potts.agent.cell;

import java.util.*;
import arcade.core.agent.cell.*;
import static arcade.core.util.Enums.State;
import static arcade.core.util.Enums.Region;
import static arcade.potts.util.PottsEnums.Phase;

/**
 * Container class for loading a {@link PottsCell}.
 */
public class PottsCellContainer extends CellContainer {
	public final State state;
	public final Phase phase;
	public final int voxels;
	public final EnumMap<Region, Integer> regionVoxels;
	public final double targetVolume;
	public final double targetSurface;
	public final EnumMap<Region, Double> regionTargetVolume;
	public final EnumMap<Region, Double> regionTargetSurface;
	
	public PottsCellContainer(int id, int pop, int voxels) {
		this(id, pop, 0, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels, null, 0, 0, null, null);
	}
	
	public PottsCellContainer(int id, int pop, int voxels, EnumMap<Region, Integer> regionVoxels) {
		this(id, pop, 0, State.PROLIFERATIVE, Phase.PROLIFERATIVE_G1, voxels, regionVoxels, 0, 0, null, null);
	}
	
	public PottsCellContainer(int id, int pop, int age, State state, Phase phase, int voxels,
						 double targetVolume, double targetSurface) {
		this(id, pop, age, state, phase, voxels, null, targetVolume, targetSurface, null, null);
	}
	
	public PottsCellContainer(int id, int pop, int age, State state, Phase phase, int voxels,
						 EnumMap<Region, Integer> regionVoxels,
						 double targetVolume, double targetSurface,
						 EnumMap<Region, Double> regionTargetVolume, EnumMap<Region, Double> regionTargetSurface) {
		super(id, pop, age);
		this.state = state;
		this.phase = phase;
		this.voxels = voxels;
		this.regionVoxels = regionVoxels;
		this.targetVolume = targetVolume;
		this.targetSurface = targetSurface;
		this.regionTargetVolume = regionTargetVolume;
		this.regionTargetSurface = regionTargetSurface;
	}
}