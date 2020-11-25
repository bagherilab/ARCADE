package arcade.potts.env.loc;

import java.util.ArrayList;
import java.util.EnumMap;
import arcade.core.env.loc.*;
import static arcade.core.agent.cell.Cell.*;

/**
 * Container class for loading a {@link PottsLocation}.
 */
public class PottsLocationContainer extends LocationContainer {
	public final ArrayList<Voxel> voxels;
	public final Voxel center;
	public final EnumMap<Region, ArrayList<Voxel>> regions;
	
	public PottsLocationContainer(int id, Voxel center, ArrayList<Voxel> voxels,
							 EnumMap<Region, ArrayList<Voxel>> regions) {
		super(id);
		this.center = center;
		this.voxels = voxels;
		this.regions = regions;
	}
}