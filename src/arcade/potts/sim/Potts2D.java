package arcade.potts.sim;

import java.util.HashSet;
import arcade.potts.sim.hamiltonian.AdhesionHamiltonian2D;
import arcade.potts.sim.hamiltonian.Hamiltonian;
import arcade.potts.sim.hamiltonian.PersistenceHamiltonian;
import arcade.potts.sim.hamiltonian.SurfaceHamiltonian2D;
import arcade.potts.sim.hamiltonian.VolumeHamiltonian;
import static arcade.potts.util.PottsEnums.Term;

/** Extension of {@link Potts} for 2D. */
public final class Potts2D extends Potts {
    /** Number of neighbors. */
    public static final int NUMBER_NEIGHBORS = 4;

    /** List of x direction movements (N, E, S, W). */
    public static final int[] MOVES_X = {0, 1, 0, -1};

    /** List of y direction movements (N, E, S, W). */
    public static final int[] MOVES_Y = {-1, 0, 1, 0};

    /** List of x direction corner movements (NE, SE, SW, NW). */
    private static final int[] CORNER_X = {1, 1, -1, -1};

    /** List of y direction corner movements (NE, SE, SW, NW). */
    private static final int[] CORNER_Y = {-1, 1, 1, -1};

    /**
     * Creates a cellular {@code Potts} model in 2D.
     *
     * @param series the simulation series
     */
    public Potts2D(PottsSeries series) {
        super(series);
    }

    @Override
    Hamiltonian getHamiltonian(Term term, PottsSeries series) {
        switch (term) {
            case ADHESION:
                return new AdhesionHamiltonian2D(series, this);
            case VOLUME:
                return new VolumeHamiltonian(series);
            case SURFACE:
                return new SurfaceHamiltonian2D(series, this);
            case PERSISTENCE:
                return new PersistenceHamiltonian(series);
            default:
                return null;
        }
    }

    @Override
    boolean[][][] getNeighborhood(int id, int x, int y, int z) {
        boolean[][] array = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                array[i][j] = ids[0][i + x - 1][j + y - 1] == id;
            }
        }
        return new boolean[][][] {array};
    }

    @Override
    boolean[][][] getNeighborhood(int id, int region, int x, int y, int z) {
        boolean[][] array = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                array[i][j] =
                        ids[0][i + x - 1][j + y - 1] == id
                                && regions[0][i + x - 1][j + y - 1] == region;
            }
        }
        return new boolean[][][] {array};
    }

    @Override
    boolean getConnectivity(boolean[][][] array, boolean zero) {
        boolean[][] subarray = array[0];
        int links = 0;
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            if (subarray[1 + MOVES_X[i]][1 + MOVES_Y[i]]) {
                links++;
            }
        }

        switch (links) {
            case 1:
                return true;
            case 2:
                return getConnectivityTwoNeighbors(subarray);
            case 3:
                return getConnectivityThreeNeighbors(subarray);
            case 4:
                return zero;
            default:
                return false;
        }
    }

    /**
     * Determines simple connectivity for a position with two neighbors.
     *
     * @param subarray the local neighborhood array
     * @return {@code true} if simply connected, {@code false} otherwise
     */
    private boolean getConnectivityTwoNeighbors(boolean[][] subarray) {
        if (subarray[1][2] && subarray[1][0]) {
            // Check for opposites N / S
            return false;
        } else if (subarray[2][1] && subarray[0][1]) {
            // Check for opposites E / W
            return false;
        } else {
            // Check for corners
            for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
                boolean check1 = subarray[1 + MOVES_X[i]][1 + MOVES_Y[i]];
                boolean check2 =
                        subarray[1 + MOVES_X[(i + 1) % NUMBER_NEIGHBORS]][
                                1 + MOVES_Y[(i + 1) % NUMBER_NEIGHBORS]];
                boolean check3 = subarray[1 + CORNER_X[i]][1 + CORNER_Y[i]];
                if (check1 && check2 && check3) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Determines simple connectivity for a position with three neighbors.
     *
     * @param subarray the local neighborhood array
     * @return {@code true} if simply connected, {@code false} otherwise
     */
    private boolean getConnectivityThreeNeighbors(boolean[][] subarray) {
        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            if (!subarray[1 + MOVES_X[i]][1 + MOVES_Y[i]]) {
                boolean check1 =
                        subarray[1 + CORNER_X[(i + 1) % NUMBER_NEIGHBORS]][
                                1 + CORNER_Y[(i + 1) % NUMBER_NEIGHBORS]];
                boolean check2 =
                        subarray[1 + CORNER_X[(i + 2) % NUMBER_NEIGHBORS]][
                                1 + CORNER_Y[(i + 2) % NUMBER_NEIGHBORS]];
                if (check1 && check2) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public HashSet<Integer> getUniqueIDs(int x, int y, int z) {
        int id = ids[z][x][y];
        HashSet<Integer> unique = new HashSet<>();

        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighbor = ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            if (id != neighbor) {
                unique.add(neighbor);
            }
        }
        return unique;
    }

    @Override
    HashSet<Integer> getUniqueRegions(int x, int y, int z) {
        int id = ids[z][x][y];
        int region = regions[z][x][y];
        HashSet<Integer> unique = new HashSet<>();

        for (int i = 0; i < NUMBER_NEIGHBORS; i++) {
            int neighborID = ids[z][x + MOVES_X[i]][y + MOVES_Y[i]];
            int neighborRegion = regions[z][x + MOVES_X[i]][y + MOVES_Y[i]];

            if (neighborID != id) {
                continue;
            }
            if (region != neighborRegion) {
                unique.add(neighborRegion);
            }
        }

        return unique;
    }
}
