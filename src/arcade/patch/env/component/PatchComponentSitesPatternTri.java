package arcade.patch.env.component;

import java.util.EnumMap;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSitesPattern} for triangular geometry.
 * <p>
 * The pattern unit cell is given by:
 * <pre>
 *    -----------------------------
 *    \   / \   / \ 2 / \   / \   /
 *     \ /   \ / 1 \ / 1 \ / 1 \ /
 *      -------------------------
 *     / \   / \ 1 / \ 2 / \ 2 / \
 *    /   \ /   \ / 2 \ /   \ /   \
 *    -----------------------------
 *    \   / \ 1 / \   / \   / \   /
 *     \ /   \ / 2 \ /   \ /   \ /
 *      -------------------------
 *     / \ 1 / \   / \   / \   / \
 *    / 1 \ / 2 \ /   \ /   \ /   \
 *    -----------------------------
 *    \ 2 / \ 2 / \   / \   / \   /
 *     \ / 1 \ /   \ /   \ /   \ /
 *      -------------------------
 *     / \   / \ 2 / \   / \   / \
 *    /   \ / 1 \ /   \ /   \ /   \
 *    -----------------------------
 * </pre>
 */

public class PatchComponentSitesPatternTri extends PatchComponentSitesPattern {
    /**
     * Creates a {@link PatchComponentSitesPattern} for triangular geometry.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     */
    public PatchComponentSitesPatternTri(Series series, MiniBox parameters) {
        super(series, parameters);
    }
    
    /**
     * Calculate the offset based on the layer index.
     *
     * @param k  the index in the z direction
     * @return  the lattice offset
     */
    private int calcOffset(int k) {
        return (latticeHeight - k / 2 - 1) % 3;
    }
    
    /**
     * Calculates column of the triangular pattern based on offset and index.
     *
     * @param i  the index in the x direction
     * @param offset  the lattice offset
     * @return  the column index
     */
    private int calcCol(int i, int offset) {
        return (i + 6 * offset) % 9;
    }
    
    /**
     * Calculates row of the triangular pattern based on offset and index.
     *
     * @param i  the index in the x direction
     * @param j  the index in the y direction
     * @param offset  the lattice offset
     * @return  the row index
     */
    private int calcRow(int i, int j, int offset) {
        return (j + (((i + 6 * offset) / 9 & 1) == 0 ? 0 : 3)) % 6;
    }
    
    @Override
    double calculateAverage(int i, int j, int k, double[][] delta) {
        double average = delta[i][j];
        int offset = calcOffset(k);
        
        switch (calcCol(i, offset)) {
            case 0:
            case 7:
            case 5:
                if (j != latticeWidth - 1) {
                    average += delta[i][j + (anchors[k][i][j] ? 1 : -1)];
                    average /= 2;
                }
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                if (i != latticeLength - 1 && !(i == 0 && patterns[k][i][j] && !anchors[k][i][j])) {
                    average += delta[i + (anchors[k][i][j] ? 1 : -1)][j];
                    average /= 2;
                }
                break;
            default:
                break;
        }
        
        return average;
    }
    
    @Override
    public void calculateFlow(int i, int j, int k, double[][] flow, double[][] delta,
                              EnumMap<Border, Boolean> borders) {
        double total = 0;
        double val = calculateAverage(i, j, k, delta);
        int offset = calcOffset(k);
        
        switch (calcCol(i, offset)) {
            case 0:
            case 7:
                if (i == 0 || i == 1) {
                    total = val;
                } else {
                    total = flow[i - 2][j] + val;
                }
                flow[i][j + (borders.get(Border.BOTTOM) ? 0 : 1)] = total;
                break;
            case 1:
                total = flow[i - 1][j] + val;
                flow[i + (borders.get(Border.RIGHT) ? 0 : 1)][j] = total;
                break;
            case 5:
                total = (flow[i - 1][j + (1 - (borders.get(Border.TOP) ? 0 : 1))]
                        + flow[i - 1][j + (j > latticeWidth - 3 ? 0 : 1)]) / 2 + val;
                flow[i][j + (borders.get(Border.BOTTOM) ? 0 : 1)] = total;
                break;
            case 2:
            case 3:
                switch (calcRow(i, j, offset)) {
                    case 1:
                    case 2:
                        if (j != latticeWidth - 1 && i != 0) {
                            total = flow[i - 1][j + 1] + val;
                        }
                        break;
                    case 0:
                    case 5:
                        if (j != 0 && i != 0) {
                            total = flow[i - 1][j - 1] + val;
                        }
                        break;
                    default:
                        break;
                }
                flow[i + (borders.get(Border.RIGHT) ? 0 : 1)][j] = total;
                break;
            default:
                break;
        }
        
        flow[i][j] = total;
    }
    
    @Override
    public void calculateDamage(int i, int j, int k) {
        double total = damageSingle[k][i][j];
        int offset = calcOffset(k);
        
        switch (calcCol(i, offset)) {
            case 0:
            case 7:
            case 5:
                if (j != latticeWidth - 1) {
                    total += damageSingle[k][i][j + 1];
                    total /= 2;
                    damageTotal[k][i][j + 1] = total;
                }
                break;
            case 1:
            case 2:
            case 3:
                if (i != latticeLength - 1) {
                    total += damageSingle[k][i + 1][j];
                    total /= 2;
                    damageTotal[k][i + 1][j] = total;
                }
                break;
            default:
                break;
        }
        
        damageTotal[k][i][j] = total;
    }
    
    @Override
    void initializePatternArray() {
        byte[][] unit = {
                { 0, 0, 0, 1, 2, 1, 0, 1, 0 },
                { 0, 0, 0, 1, 2, 2, 0, 2, 0 },
                { 0, 0, 1, 2, 0, 0, 0, 0, 0 },
                { 1, 1, 2, 0, 0, 0, 0, 0, 0 },
                { 2, 1, 2, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 1, 2, 0, 0, 0, 0, 0 }
        };
        
        for (int k = 0; k < latticeHeight; k += 2) {
            int offset = calcOffset(k);
            for (int i = 0; i < latticeLength; i++) {
                for (int j = 0; j < latticeWidth; j++) {
                    int col = calcCol(i, offset);
                    int row = calcRow(i, j, offset);
                    patterns[k][i][j] = (unit[row][col] != 0);
                    anchors[k][i][j] = (unit[row][col] == 1);
                }
            }
        }
    }
}
