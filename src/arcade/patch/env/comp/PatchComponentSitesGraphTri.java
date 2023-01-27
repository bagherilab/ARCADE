package arcade.patch.env.comp;

import java.util.ArrayList;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSitesGraph} for triangular geometry.
 * <p>
 * For pattern layout, the graph is given by:
 * <pre>
 *                         ___ ___
 *                       /         \
 *                      /           \
 *             ___ ___ /             \ ___ ___
 *           /         \             /         \
 *          /           \           /           \
 * ___ ___ /             \ ___ ___ /             \ ___ ___
 *         \             /         \             /
 *          \           /           \           /
 *           \ ___ ___ /             \ ___ ___ /
 *                     \             /
 *                      \           /
 *                       \ ___ ___ /
 * </pre>
 * <p>
 * For root layouts, each node has six possible orientations for the edge: left,
 * right, up left, up right, down left, and down right. When initializing roots
 * from a border, only certain orientations are possible:
 * <ul>
 *     <li>left border = right, up right, down right</li>
 *     <li>right border = left, up left, down left</li>
 *     <li>top border = down right, down left</li>
 *     <li>bottom border = up right, up left</li>
 * </ul>
 */

public abstract class PatchComponentSitesGraphTri extends PatchComponentSitesGraph {
    /**
     * Creates a {@link PatchComponentSitesGraph} for triangular geometry.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     * @param random  the random number generator
     */
    public PatchComponentSitesGraphTri(Series series, MiniBox parameters,
                                       MersenneTwisterFast random) {
        super(series, parameters, random);
    }
    
    @Override
    public PatchComponentSitesGraphFactory makeGraphFactory(Series series) {
        return new PatchComponentSitesGraphFactoryTri(series);
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphTri} for simple
     * hemodynamics.
     */
    public static class Simple extends PatchComponentSitesGraphTri {
        /**
         * Creates a {@link PatchComponentSitesGraphTri} with simple step.
         *
         * @param series  the simulation series
         * @param parameters  the component parameters dictionary
         * @param random  the random number generator
         */
        public Simple(Series series, MiniBox parameters, MersenneTwisterFast random) {
            super(series, parameters, random);
        }
        
        @Override
        public void step(SimState state) {
            super.simpleStep();
        }
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphTri} for complex
     * hemodynamics.
     */
    public static class Complex extends PatchComponentSitesGraphTri {
        /**
         * Creates a {@link PatchComponentSitesGraphTri} with complex step.
         *
         * @param series  the simulation series
         * @param parameters  the component parameters dictionary
         * @param random  the random number generator
         */
        public Complex(Series series, MiniBox parameters, MersenneTwisterFast random) {
            super(series, parameters, random);
        }
        
        @Override
        public void step(SimState state) {
            super.complexStep(state.random);
        }
    }
    
    @Override
    public ArrayList<int[]> getSpan(SiteNode from, SiteNode to) {
        ArrayList<int[]> s = new ArrayList<>();
        
        int z = from.getZ();
        int x0 = from.getX();
        int y0 = from.getY();
        int x1 = to.getX();
        int y1 = to.getY();
        
        // Calculate deltas.
        int dX = x1 - x0;
        int dY = y1 - y0;
        
        // Check direction of arrow and update deltas to absolute.
        boolean sX = dX < 0;
        boolean sY = dY < 0;
        
        dX = Math.abs(dX);
        dY = Math.abs(dY);
        
        if (x0 == x1) { // Check if line is vertical.
            for (int d = 0; d < dY; d++) {
                checkSite(s, x0 - 1, y0 + (sY ? -(d + 1) : d), z);
            }
        } else if (y0 == y1) { // Check if line is horizontal.
            for (int d = 0; d < dX; d += 2) {
                checkSite(s, x0 + (sX ? -(d + 2) : d), y0, z);
                checkSite(s, x0 + (sX ? -(d + 2) : d), y0 - 1, z);
            }
        } else if ((float) dX / (float) dY == 3) { // Check for upper diagonals (30 degrees).
            for (int d = 0; d < dX - 1; d += 3) {
                checkSite(s, x0 + (sX ? -(d + 2) : d), y0 + (sY ? -(d / 3 + 1) : d / 3), z);
                checkSite(s, x0 + (sX ? -(d + 3) : d + 1), y0 + (sY ? -(d / 3 + 1) : d / 3), z);
            }
        } else if (dX == dY) { // Check for lower diagonals (60 degrees).
            for (int d = 0; d < dX; d++) {
                checkSite(s, x0 + (sX ? -(d + 2) : d), y0 + (sY ? -(d + 1) : d), z);
                checkSite(s, x0 + (sX ? -(d + 1) : d - 1), y0 + (sY ? -(d + 1) : d), z);
            }
        } else {
            // Calculate starting and ending triangles.
            int startx = x0 - (dY < dX ? (sX ? 2 : 0) : 1);
            int starty = y0 - (sY ? 1 : 0);
            int endx = x1 - (dY < dX ? (sX ? 0 : 2) : 1);
            int endy = y1 - (sY ? 0 : 1);
            
            // Calculate new deltas based on triangle.
            int dx = Math.abs(endx - startx);
            int dy = Math.abs(endy - starty);
            
            // Initial conditions.
            int x = startx;
            int y = starty;
            int e = 0;
            
            // Add start triangle.
            checkSite(s, x, y, z);
            
            // Track if triangle is even (point down) or odd (point up).
            boolean even;
            
            // Iterate until the ending triangle is reached.
            while (x != endx || y != endy) {
                even = ((x + y) & 1) == 0;
                
                if (e > 3 * dx) {
                    if (!sX && !sY) {
                        if (even) {
                            checkSite(s, --x, y++, z);
                        } else {
                            checkSite(s, x--, ++y, z);
                        }
                    } else if (!sX && sY) {
                        if (even) {
                            checkSite(s, x--, --y, z);
                        } else {
                            checkSite(s, --x, y--, z);
                        }
                    } else if (sX && !sY) {
                        if (even) {
                            checkSite(s, ++x, y++, z);
                        } else {
                            checkSite(s, x++, ++y, z);
                        }
                    } else if (sX && sY) {
                        if (even) {
                            checkSite(s, x++, --y, z);
                        } else {
                            checkSite(s, ++x, y--, z);
                        }
                    }
                    e -= (2 * dy + 2 * dx);
                } else if (e >= 2 * dx) {
                    if (!sY) {
                        y++;
                    } else {
                        y--;
                    }
                    e -= 2 * dx;
                } else {
                    e += 2 * dy;
                    if (e >= dx) {
                        if (!sX && !sY) {
                            if (even) {
                                checkSite(s, ++x, y++, z);
                            } else {
                                checkSite(s, x++, ++y, z);
                            }
                        } else if (!sX && sY) {
                            if (even) {
                                checkSite(s, x++, --y, z);
                            } else {
                                checkSite(s, ++x, y--, z);
                            }
                        } else if (sX && !sY) {
                            if (even) {
                                checkSite(s, --x, y++, z);
                            } else {
                                checkSite(s, x--, ++y, z);
                            }
                        } else if (sX && sY) {
                            if (even) {
                                checkSite(s, x--, --y, z);
                            } else {
                                checkSite(s, --x, y--, z);
                            }
                        }
                        e -= 2 * dx;
                    } else {
                        if (!sX) {
                            x++;
                        } else {
                            x--;
                        }
                    }
                }
                
                checkSite(s, x, y, z);
            }
        }
        
        return s;
    }
}
