package arcade.potts.env.location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import arcade.core.env.grid.Grid;
import arcade.core.util.Vector;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.util.PottsEnums.Direction;
import sim.util.Bag;

import static arcade.potts.util.PottsEnums.Direction;

/** Concrete implementation of {@link PottsLocation} for 2D. */
public final class PottsLocation2D extends PottsLocation implements Location2D {
    /**
     * Creates a 2D {@link PottsLocation} for a list of voxels.
     *
     * @param voxels the list of voxels
     */
    public PottsLocation2D(ArrayList<Voxel> voxels) {
        super(voxels);
    }

    @Override
    PottsLocation makeLocation(ArrayList<Voxel> voxels) {
        return new PottsLocation2D(voxels);
    }

    @Override
    ArrayList<Voxel> getNeighbors(Voxel focus) {
        return Location2D.getNeighbors(focus);
    }

    @Override
    public double convertSurface(double volume, double height) {
        return Location2D.convertSurface(volume, height);
    }

    @Override
    int calculateSurface() {
        return Location2D.calculateSurface(voxels);
    }

    @Override
    int calculateHeight() {
        return Location2D.calculateHeight(voxels);
    }

    @Override
    int updateSurface(Voxel voxel) {
        return Location2D.updateSurface(voxels, voxel);
    }

    @Override
    int updateHeight(Voxel voxel) {
        return Location2D.updateHeight(voxels, voxel);
    }

    @Override
    HashMap<Direction, Integer> getDiameters() {
        return Location2D.getDiameters(voxels, getCenter());
    }

    @Override
    Direction getSlice(Direction direction, HashMap<Direction, Integer> diameters) {
        return Location2D.getSlice(direction, diameters);
    }

    @Override
    ArrayList<Voxel> getSelected(Voxel focus, double n) {
        return Location2D.getSelected(voxels, focus, n);
    }

    @Override
    HashSet<PottsCell> getCellNeighbors(Grid g) {

        HashSet<Voxel> myVox = new HashSet<Voxel>(this.getVoxels());
        HashSet<Voxel> enlargedVoxels = new HashSet<Voxel>(myVox.size() * 4);

        // Need to check 4 directions for 2D
        final int[][] OFFSETS_4 = {
            { 1, 0}, {-1, 0},
            { 0, 1}, { 0,-1}
        };

        // Create a set of 'enlarged' voxels that are 1 voxel bigger than the original cell.
        for (Voxel v : myVox) {
            enlargedVoxels.add(v);
            for (int[] d : OFFSETS_4) {
                enlargedVoxels.add(new Voxel(v.x + d[0], v.y + d[1], v.z));
            }
        }

        Bag all = g.getAllObjects();
        HashSet<PottsCell> neighborCells = new HashSet<>();

        iteratePossibleNeighbors:
        for (Object other : all) {
            // need to add line to make sure the current cell isn't the same cell as the one we're looking for neighbors for

            PottsCell otherCell = (PottsCell) other;
            PottsLocation2D otherLocation = (PottsLocation2D) otherCell.getLocation();

            // If the cell we're checking is actually this cell, we skip it (otherwise the cell will be considered a neighbor of itself)
            if (this.equals(otherLocation)) continue;

            ArrayList<Voxel> otherVoxels = otherLocation.getVoxels();
            
            // Iterate through all the voxels and check if the neighbors are in set of enlarged voxels
            for (Voxel v : otherVoxels) {
                for (int[] d : OFFSETS_4) {

                    // TODO: need to check w sophia if this is correct
                    Voxel enlargedVoxel = new Voxel(v.x + d[0], v.y + d[1], v.z);
                    if (enlargedVoxels.contains(enlargedVoxel)) {
                        neighborCells.add(otherCell);

                        // We can quit early because we don't need to check other voxels in this neighboring cell
                        continue iteratePossibleNeighbors;
                    }
                }
            }
        }

        return neighborCells;
    }

    public Voxel getOffsetInApicalFrame2D(ArrayList<Integer> offsets, Vector apicalAxis) {
        if (voxels.isEmpty()) return null;
        if (offsets == null || offsets.size() != 2)
            throw new IllegalArgumentException("Offsets must be 2 integers.");

        // Normalize axes
        Vector yAxis = Vector.normalizeVector(apicalAxis);
        Vector xAxis = Vector.normalizeVector(new Vector(apicalAxis.getY(), -apicalAxis.getX(), 0));

        // Project voxels onto apical axis and group by rounded projection
        HashMap<Integer, ArrayList<Voxel>> apicalBands = new HashMap<>();
        ArrayList<Integer> apicalKeys = new ArrayList<>();

        for (Voxel v : voxels) {
            Vector pos = new Vector(v.x, v.y, 0);
            double apicalProj = Vector.dotProduct(pos, yAxis);
            int roundedProj = (int) Math.round(apicalProj);
            apicalBands.computeIfAbsent(roundedProj, k -> new ArrayList<>()).add(v);
            apicalKeys.add(roundedProj);
        }

        // Sort apical keys and choose percentile
        Collections.sort(apicalKeys);
        int yIndex =
                Math.min(
                        apicalKeys.size() - 1,
                        (int) ((offsets.get(1) / 100.0) * apicalKeys.size()));
        int targetApicalKey = apicalKeys.get(yIndex);

        ArrayList<Voxel> band = apicalBands.get(targetApicalKey);
        if (band == null || band.isEmpty()) return null;

        // Project to orthogonal axis within the band and sort
        band.sort(
                Comparator.comparingDouble(v -> Vector.dotProduct(new Vector(v.x, v.y, 0), xAxis)));
        int xIndex = Math.min(band.size() - 1, (int) ((offsets.get(0) / 100.0) * band.size()));
        return band.get(xIndex);
    }
}
