package arcade.patch.env.lat;

import arcade.core.env.loc.Location;
import arcade.core.util.MiniBox;
import arcade.patch.env.loc.CoordinateTri;
import arcade.patch.env.loc.PatchLocation;

/**
 * Concrete implementation of {@link PatchLattice} for triangular coordinates.
 */

public class PatchLatticeTri extends PatchLattice {
   /**
    * Creates a triangular {@code PatchLattice} environment.
    *
    * @param length  the length of array (x direction)
    * @param width  the width of array (y direction)
    * @param height  the height of array (z direction)
    * @param parameters  the dictionary of parameters
    */
   public PatchLatticeTri(int length, int width, int height, MiniBox parameters) {
      super(length, width, height, parameters);
   }
   
   @Override
   public double getTotalValue(Location location) {
      PatchLocation patchLocation = (PatchLocation) location;
      return patchLocation.getSubcoordinates().stream()
              .map(e -> (CoordinateTri) e)
              .mapToDouble(c -> field[c.z][c.x][c.y])
              .sum();
   }
   
   @Override
   public double getAverageValue(Location location) {
      PatchLocation patchLocation = (PatchLocation) location;
      return patchLocation.getSubcoordinates().stream()
              .map(e -> (CoordinateTri) e)
              .mapToDouble(c -> field[c.z][c.x][c.y])
              .sum() / patchLocation.getMaximum();
   }
   
   @Override
   public void updateValue(Location location, double fraction) {
      if (!Double.isNaN(fraction)) {
         PatchLocation patchLocation = (PatchLocation) location;
         patchLocation.getSubcoordinates().stream()
                 .map(e -> (CoordinateTri) e)
                 .forEach(c -> field[c.z][c.x][c.y] *= fraction);
      }
   }
   
   @Override
   public void incrementValue(Location location, double increment) {
      PatchLocation patchLocation = (PatchLocation) location;
      patchLocation.getSubcoordinates().stream()
              .map(e -> (CoordinateTri) e)
              .forEach(c -> field[c.z][c.x][c.y] += increment);
   }
   
    @Override
    public void setValue(Location location, double value) {
       PatchLocation patchLocation = (PatchLocation) location;
       patchLocation.getSubcoordinates().stream()
               .map(e -> (CoordinateTri) e)
               .forEach(c -> field[c.z][c.x][c.y] = value);
    }
}
