package arcade.patch.env.comp;

import java.util.ArrayList;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Series;
import arcade.core.util.MiniBox;

/**
 * Extension of {@link PatchComponentSitesGraph} for rectangular geometry.
 */

public abstract class PatchComponentSitesGraphRect extends PatchComponentSitesGraph {
    /**
     * Creates a {@link PatchComponentSitesGraph} for rectangular geometry.
     *
     * @param series  the simulation series
     * @param parameters  the component parameters dictionary
     * @param random  the random number generator
     */
    public PatchComponentSitesGraphRect(Series series, MiniBox parameters,
                                        MersenneTwisterFast random) {
        super(series, parameters, random);
    }
    
    @Override
    public PatchComponentSitesGraphFactory makeGraphFactory(Series series) {
        return new PatchComponentSitesGraphFactoryRect(series);
    }
    
    /**
     * Extension of {@link PatchComponentSitesGraphRect} for simple
     * hemodynamics.
     */
    public static class Simple extends PatchComponentSitesGraphRect {
        /**
         * Creates a {@link PatchComponentSitesGraphRect} with simple step.
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
     * Extension of {@link PatchComponentSitesGraphRect} for complex
     * hemodynamics.
     */
    public static class Complex extends PatchComponentSitesGraphRect {
        /**
         * Creates a {@link PatchComponentSitesGraphRect} with complex step.
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
                checkSite(s, x0, y0 + (sY ? -(d + 1) : d), z);
                checkSite(s, x0 - 1, y0 + (sY ? -(d + 1) : d), z);
            }
        } else if (y0 == y1) { // Check if line is horizontal.
            for (int d = 0; d < dX; d++) {
                checkSite(s, x0 + (sX ? -(d + 1) : d), y0, z);
                checkSite(s, x0 + (sX ? -(d + 1) : d), y0 - 1, z);
            }
        } else if ((float) dX / (float) dY == 1) { // Check for diagonals.
            for (int d = 0; d < dX; d++) {
                checkSite(s, x0 + (sX ? -(d + 1) : d), y0 + (sY ? -(d + 1) : d), z);
            }
        } else {
            // Calculate starting and ending squares.
            int startx = x0 - (sX ? 1 : 0);
            int starty = y0 - (sY ? 1 : 0);
            int endx = x1 - (sX ? 0 : 1);
            int endy = y1 - (sY ? 0 : 1);
            
            // Calculate new deltas based on squares.
            int dx = Math.abs(endx - startx);
            int dy = Math.abs(endy - starty);
            
            // Initial conditions.
            int x = startx;
            int y = starty;
            int e = dx - dy;
            
            // Add start square.
            checkSite(s, x, y, z);
            
            // Calculate increments.
            int incX = (x1 > x0 ? 1 : -1);
            int incY = (y1 > y0 ? 1 : -1);
            
            // Iterate until the ending square is reached.
            while (x != endx || y != endy) {
                if (e > 0) {
                    x += incX;
                    e -= 2 * dy;
                } else {
                    y += incY;
                    e += 2 * dx;
                }
                
                checkSite(s, x, y, z);
            }
        }
        
        return s;
    }
}
