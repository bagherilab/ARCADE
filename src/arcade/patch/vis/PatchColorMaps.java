package arcade.patch.vis;

import java.awt.Color;
import arcade.core.sim.Series;
import arcade.core.util.Colors;
import arcade.core.util.MiniBox;

/**
 * Container class for commonly used color maps.
 * <p>
 * Uses {@link arcade.core.util.Colors} to define mappings.
 */

class PatchColorMaps {
    /**
     * BurgYl colormap from
     * <a href="https://carto.com/carto-colors/">CARTOColors</a>.
     */
    private static final Color[] BURGUNDY_YELLOW = new Color[] {
            new Color(0, 0, 0),
            new Color(112, 40, 74),
            new Color(156, 63, 93),
            new Color(200, 88, 108),
            new Color(220, 113, 118),
            new Color(238, 138, 130),
            new Color(245, 186, 152),
            new Color(251, 230, 197),
    };
    
    /**
     * Dark mint colormap from
     * <a href="https://carto.com/carto-colors/">CARTOColors</a>.
     */
    private static final Color[] DARK_MINT = new Color[] {
            new Color(0, 0, 0),
            new Color(18, 63, 90),
            new Color(35, 93, 114),
            new Color(58, 124, 137),
            new Color(85, 156, 158),
            new Color(123, 188, 176),
            new Color(165, 219, 194),
            new Color(210, 251, 212),
    };
    
    /** Color map for cell state. */
    static final Colors MAP_STATE = new Colors(
            new Color[] {
                    new Color(85, 85, 85),    // undefined
                    new Color(115, 175, 72),  // quiescent
                    new Color(204, 80, 62),   // proliferative
                    new Color(29, 105, 150),  // migratory
                    new Color(237, 173, 8),   // apoptotic
                    new Color(225, 124, 5),   // necrotic
                    new Color(95, 70, 144),   // senescent
                    new Color(148, 52, 110),  // autotic
            }
    );
    
    /** Color map for cell counts. */
    static final Colors MAP_COUNTS = new Colors(
            new Color[] {
                    new Color(0, 0, 0, 0),
                    new Color(253, 212, 158),
                    new Color(253, 187, 132),
                    new Color(252, 141, 89),
                    new Color(239, 101, 72),
                    new Color(215, 48, 31),
                    new Color(153, 0, 0),
            }
    );
    
    /** Color map for cell populations. */
    static final Colors MAP_POPULATION = new Colors(
            new Color[] {
                    new Color(0, 0, 0),
                    new Color(95, 70, 144),
                    new Color(29, 105, 150),
                    new Color(56, 166, 165),
                    new Color(15, 133, 84),
                    new Color(115, 175, 72),
                    new Color(237, 173, 8),
                    new Color(225, 124, 5),
                    new Color(204, 80, 62),
                    new Color(148, 52, 110),
            },
            new double[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }
    );
    
    /** Color map for cell populations. */
    static final Colors MAP_SITES = new Colors(
            new Color[] {
                    new Color(0, 0, 0, 0),
                    new Color(100, 100, 100),
                    new Color(150, 150, 150),
            },
            new double[] { 0, 1, 2 }
    );
    
    /** Color map for cell populations. */
    static final Colors MAP_DAMAGE = new Colors(
            new Color[] {
                    new Color(0, 0, 0, 0),
                    new Color(253, 212, 158),
                    new Color(253, 187, 132),
                    new Color(252, 141, 89),
                    new Color(239, 101, 72),
                    new Color(215, 48, 31),
                    new Color(153, 0, 0),
            },
            new double[] { 0, 5, 10, 15, 20, 25, 30 }
    );
    
    /** Color map for cell age. */
    final Colors mapAge;
    
    /** Color map for cell volume. */
    final Colors mapVolume;
    
    /** Color map for cell height. */
    final Colors mapHeight;
    
    /** Color map for cell energy. */
    final Colors mapEnergy;
    
    /** Color map for cell divisions. */
    final Colors mapDivisions;
    
    /** Color map for glucose concentration. */
    final Colors mapGlucose;
    
    /** Color map for oxygen concentration. */
    final Colors mapOxygen;
    
    /** Color map for TGFa concentration. */
    final Colors mapTGFa;
    
    /**
     * Creates {@code ColorMaps} for the given series.
     *
     * @param series  the simulation series
     */
    PatchColorMaps(Series series) {
        double age = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("CELL_AGE_MAX") > age) {
                age = box.getDouble("CELL_AGE_MAX");
            }
        }
        
        mapAge = new Colors(
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 180),
                0, age);
        
        double volume = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("CELL_VOLUME_MEAN") > volume) {
                volume = box.getDouble("CELL_VOLUME_MEAN");
            }
        }
        
        mapVolume = new Colors(
                BURGUNDY_YELLOW,
                new double[] {
                        0,
                        1. / 7 * volume * 2,
                        2. / 7 * volume * 2,
                        3. / 7 * volume * 2,
                        4. / 7 * volume * 2,
                        5. / 7 * volume * 2,
                        6. / 7 * volume * 2,
                        7. / 7 * volume * 2,
                }
        );
        
        double height = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("CELL_HEIGHT_MEAN") > height) {
                height = box.getDouble("CELL_HEIGHT_MEAN");
            }
        }
        
        mapHeight = new Colors(
                BURGUNDY_YELLOW,
                new double[] {
                        0,
                        1. / 7 * height * 2,
                        2. / 7 * height * 2,
                        3. / 7 * height * 2,
                        4. / 7 * height * 2,
                        5. / 7 * height * 2,
                        6. / 7 * height * 2,
                        7. / 7 * height * 2,
                }
        );
        
        double energy = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("ENERGY_THRESHOLD") > energy) {
                energy = box.getDouble("ENERGY_THRESHOLD");
            }
        }
        
        mapEnergy = new Colors(
                BURGUNDY_YELLOW,
                new double[] {
                        0,
                        1. / 7 * energy,
                        2. / 7 * energy,
                        3. / 7 * energy,
                        4. / 7 * energy,
                        5. / 7 * energy,
                        6. / 7 * energy,
                        energy,
                }
        );
        
        double divisions = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("DIVISION_POTENTIAL") > divisions) {
                divisions = box.getDouble("DIVISION_POTENTIAL");
            }
        }
        
        mapDivisions = new Colors(
                BURGUNDY_YELLOW,
                new double[] {
                        0,
                        1. / 7 * divisions,
                        2. / 7 * divisions,
                        3. / 7 * divisions,
                        4. / 7 * divisions,
                        5. / 7 * divisions,
                        6. / 7 * divisions,
                        divisions,
                }
        );
        
        double glucose = series.layers.get("GLUCOSE").getDouble("generator/CONCENTRATION");
        
        mapGlucose = new Colors(
                DARK_MINT,
                new double[] {
                        0,
                        1. / 7 * glucose,
                        2. / 7 * glucose,
                        3. / 7 * glucose,
                        4. / 7 * glucose,
                        5. / 7 * glucose,
                        6. / 7 * glucose,
                        glucose,
                }
        );
        
        double oxygen = series.layers.get("OXYGEN").getDouble("generator/CONCENTRATION");
        
        mapOxygen = new Colors(
                DARK_MINT,
                new double[] {
                        0,
                        1. / 7 * oxygen,
                        2. / 7 * oxygen,
                        3. / 7 * oxygen,
                        4. / 7 * oxygen,
                        5. / 7 * oxygen,
                        6. / 7 * oxygen,
                        oxygen,
                }
        );
        
        double tgfa = series.layers.get("TGFA").getDouble("INITIAL_CONCENTRATION");
        
        mapTGFa = new Colors(
                DARK_MINT,
                new double[] {
                        0,
                        1. / 7 * tgfa,
                        2. / 7 * tgfa,
                        3. / 7 * tgfa,
                        4. / 7 * tgfa,
                        5. / 7 * tgfa,
                        6. / 7 * tgfa,
                        tgfa,
                }
        );
    }
}
