package arcade.potts.sim.output;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import arcade.core.sim.Series;

public class PottsOutputLoaderTest {
	@Test
	public void constructor_initializesObjects() {
		Series series = mock(Series.class);
		PottsOutputLoader loader = new PottsOutputLoader(series, "", false, false);
		assertNotNull(loader.gson);
	}
}