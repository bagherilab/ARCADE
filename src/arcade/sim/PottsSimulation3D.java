package arcade.sim;

import java.util.ArrayList;
import java.util.HashSet;
import ec.util.MersenneTwisterFast;
import arcade.agent.cell.*;
import arcade.env.loc.*;
import arcade.util.MiniBox;
import static arcade.sim.Potts.*;
import static arcade.agent.cell.Cell.*;
import static arcade.env.loc.Location.*;

public class PottsSimulation3D extends PottsSimulation {
	public PottsSimulation3D(long seed, Series series) { super(seed, series); }
	
	/**
	 * Converts volume to voxels per square side.
	 *
	 * @param volume  the target volume
	 * @return  the voxels per side
	 */
	static int convert(double volume) {
		// TODO
		return 0;
	}
	
	/**
	 * Increases the number of voxels by adding from a given list of voxels.
	 *
	 * @param random  the seeded random number generator
	 * @param allVoxels  the list of all possible voxels
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	static void increase(MersenneTwisterFast random, ArrayList<Voxel> allVoxels, ArrayList<Voxel> voxels, int target) {
		// TODO
	}
	
	/**
	 * Decreases the number of voxels by removing.
	 *
	 * @param random  the seeded random number generator
	 * @param voxels  the list of selected voxels
	 * @param target  the target number of voxels
	 */
	static void decrease(MersenneTwisterFast random, ArrayList<Voxel> voxels, int target) {
		// TODO
	}
	
	Potts makePotts() { return new Potts3D(series, agents); }
	
	ArrayList<int[]> makeCenters() {
		// TODO
		
		return null;
	}
	
	Location makeLocation(MiniBox population, int[] center) {
		// TODO
		return null;
	}
	
	public Cell makeCell(int id, MiniBox population, int[] center) {
		// TODO
		return null;
	}
}