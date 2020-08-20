package arcade.env.loc;

import java.util.Comparator;
import arcade.env.loc.Location.Voxel;

public class LocationTest {
	public static final Comparator<Voxel> COMPARATOR = (v1, v2) ->
			v1.z != v2.z ? Integer.compare(v1.z, v2.z) :
			v1.x != v2.x ? Integer.compare(v1.x, v2.x) :
					Integer.compare(v1.y, v2.y);
}
