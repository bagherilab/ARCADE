package arcade.sim;

import org.junit.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static arcade.sim.Potts.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.Voxel;
import static arcade.sim.PottsSimulationTest.*;

public class PottsSimulation3DTest {
	private static final double EPSILON = 1E-4;
	
	private static final Comparator<int[]> COMPARATOR = (v1, v2) ->
			v1[2] != v2[2] ? Integer.compare(v1[2], v2[2]) :
			v1[0] != v2[0] ? Integer.compare(v1[0], v2[0]) :
					Integer.compare(v1[1], v2[1]);
	
	@Test
	public void makePotts_mockSeries_initializesPotts() {
		Series series = mock(Series.class);
		PottsSimulation3D sim = new PottsSimulation3D(RANDOM_SEED, series);
		Potts potts = sim.makePotts();
		assertTrue(potts instanceof Potts3D);
	}
	
	@Test
	public void convert_exactOddCubes_calculateValue() {
		// TODO
	}
	
	@Test
	public void convert_exactEvenCubes_calculateValue() {
		// TODO
	}
	
	@Test
	public void convert_inexactOddCubes_calculateValue() {
		// TODO
	}
	
	@Test
	public void convert_inexactEvenCubes_calculateValue() {
		// TODO
	}
	
	@Test
	public void increase_exactTarget_updatesList() {
		// TODO
	}
	
	@Test
	public void increase_inexactTarget_updatesList() {
		// TODO
	}
	
	@Test
	public void decrease_exactTarget_updatesList() {
		// TODO
	}
	
	@Test
	public void decrease_inexactTarget_updatesList() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationOneSideExactEqualSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationOneSideExactUnequalSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationOneSideInexactEqualSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationOneSideInexactUnequalSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideExactEqualSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideExactUnequalSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideInexactEqualSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_onePopulationThreeSideInexactUnequalSize_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeCenters_multiplePopulations_createsCenters() {
		// TODO
	}
	
	@Test
	public void makeLocation_noTags_createsObject() {
		// TODO
	}
	
	@Test
	public void makeLocation_withTags_createsObject() {
		// TODO
	}
	
	@Test
	public void makeCell_onePopulationNoTags_createsObject() {
		// TODO
	}
	
	@Test
	public void makeCell_multiplePopulationsNoTags_createsObject() {
		// TODO
	}
	
	@Test
	public void makeCell_onePopulationWithTags_createsObject() {
		// TODO
	}
	
	@Test
	public void makeCell_multiplePopulationsWithTags_createsObject() {
		// TODO
	}
}
