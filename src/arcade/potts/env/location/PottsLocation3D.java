package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import arcade.core.env.grid.Grid;
import arcade.potts.agent.cell.PottsCell;
import sim.util.Bag;

import static arcade.potts.util.PottsEnums.Direction;

/** Concrete implementation of {@link PottsLocation} for 3D. */
public final class PottsLocation3D extends PottsLocation implements Location3D {
    /**
     * Creates a 3D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels the list of voxels
     */
    public PottsLocation3D(ArrayList<Voxel> voxels) {
        super(voxels);
    }

    @Override
    PottsLocation makeLocation(ArrayList<Voxel> voxels) {
        return new PottsLocation3D(voxels);
    }

    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location3D.getNeighbors(focus);
    }

    @Override
    public double convertSurface(double volume, double height) {
        return Location3D.convertSurface(volume, height);
    }

    @Override
    int calculateSurface() {
        return Location3D.calculateSurface(voxels);
    }

    @Override
    int calculateHeight() {
        return Location3D.calculateHeight(voxels);
    }

    @Override
    int updateSurface(Voxel voxel) {
        return Location3D.updateSurface(voxels, voxel);
    }

    @Override
    int updateHeight(Voxel voxel) {
        return Location3D.updateHeight(voxels, voxel);
    }

    @Override
    HashMap<Direction, Integer> getDiameters() {
        return Location3D.getDiameters(voxels, getCenter());
    }

    @Override
    Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        return Location3D.getSlice(direction, diameters);
    }

    @Override
    ArrayList<Voxel> getSelected(Voxel focus, double n) {
        return Location3D.getSelected(voxels, focus, n);
    }

    @Override // Finds the cell neighbors in 3D
    HashSet<PottsCell> getCellNeighbors(Grid g) {

        HashSet<Voxel> myVox = new HashSet<>(this.getVoxels());
        HashSet<Voxel> enlargedVoxels = new HashSet<>(myVox.size() * 8);

        // Check 6 directions (face-adjacent) for 3D
        final int[][] OFFSETS_6 = {
            { 1, 0, 0}, {-1, 0, 0},
            { 0, 1, 0}, { 0,-1, 0},
            { 0, 0, 1}, { 0, 0,-1}
        };

        // Create a set of 'enlarged' voxels one voxel bigger in all six directions
        for (Voxel v : myVox) {
            enlargedVoxels.add(v);
            for (int[] d : OFFSETS_6) {
                enlargedVoxels.add(new Voxel(v.x + d[0], v.y + d[1], v.z + d[2]));
            }
        }

        Bag all = g.getAllObjects(); // Get all the cells on the grid
        HashSet<PottsCell> neighborCells = new HashSet<>();

        iteratePossibleNeighbors:
        for (Object other : all) {

            PottsCell otherCell = (PottsCell) other;
            PottsLocation3D otherLocation = (PottsLocation3D) otherCell.getLocation();

            // Skip self
            if (this.equals(otherLocation)) continue;

            ArrayList<Voxel> otherVoxels = otherLocation.getVoxels();

            // Check for voxel adjacency
            for (Voxel v : otherVoxels) {
                if (enlargedVoxels.contains(v)) {
                    neighborCells.add(otherCell);
                    continue iteratePossibleNeighbors; // No need to check the rest of this cell’s voxels
                }
            }
        }

        return neighborCells;
    }
}
