package arcade.env.loc;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import ec.util.MersenneTwisterFast;
import arcade.env.loc.Location.Voxel;

public class PottsLocation3DTest {
	@Test
	public void makeLocation_givenList_createsObject() {
		// TODO
	}
	
	@Test
	public void checkVoxels_connectedVoxels_returnsNull() {
		// TODO
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerVisited_returnsList() {
		// TODO
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithoutUpdateLargerUnvisited_returnsList() {
		// TODO
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdateLargerVisited_updatesList() {
		// TODO
	}
	
	@Test
	public void checkVoxels_unconnectedVoxelsWithUpdateLargerUnvisited_updatesList() {
		// TODO
	}
	
	@Test
	public void connectVoxels_bothListsConnected_doesNothing() {
		// TODO
	}
	
	@Test
	public void connectVoxels_oneListUnconnected_updatesLists() {
		// TODO
	}
	
	@Test
	public void balanceVoxels_balancedLists_doesNothing() {
		// TODO
	}
	
	@Test
	public void balanceVoxels_unbalancedLists_updatesLists() {
		// TODO
	}
	
	@Test
	public void balanceVoxels_unconnectedLists_updatesLists() {
		// TODO
	}
	
	@Test
	public void split_balanceableLocationRandomZero_returnsList() {
		// TODO
	}
	
	@Test
	public void split_balanceableLocationRandomOne_returnsList() {
		// TODO
	}
}