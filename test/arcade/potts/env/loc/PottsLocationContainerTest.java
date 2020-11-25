package arcade.potts.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.EnumMap;
import java.util.ArrayList;
import static arcade.core.util.Enums.Region;
import static arcade.core.TestUtilities.*;

public class PottsLocationContainerTest {
	@Test
	public void constructor_setsFields() {
		int id = randomIntBetween(1, 10);
		ArrayList<Voxel> voxels = new ArrayList<>();
		Voxel center = new Voxel(0, 0, 0);
		EnumMap<Region, ArrayList<Voxel>> regions = new EnumMap<>(Region.class);
		
		PottsLocationContainer locationContainer = new PottsLocationContainer(id, center, voxels, regions);
		
		assertEquals(id, locationContainer.id);
		assertSame(center, locationContainer.center);
		assertSame(voxels, locationContainer.voxels);
		assertSame(regions, locationContainer.regions);
	}
}
