package arcade.potts.vis;

import java.awt.Color;
import arcade.core.sim.Series;
import arcade.core.util.Colors;
import arcade.core.util.MiniBox;

/**
 * Container class for commonly used color maps.
 *
 * <p>Uses {@link arcade.core.util.Colors} to define mappings.
 */
public class PottsColorMaps {
    /** Color map for cell state. */
    static final Colors MAP_STATE =
            new Colors(
                    new Color[] {
                        new Color(0, 0, 0),
                        // new Color(43, 136, 158),
                        new Color(115, 175, 72), // proliferative : G1
                        new Color(82, 161, 76), // proliferative : S
                        new Color(48, 147, 80), // proliferative : G2
                        new Color(15, 133, 84), // proliferative : M
                        new Color(237, 173, 8), // apoptosis : early
                        new Color(119, 87, 4), // apoptosis : late
                        new Color(99, 67, 4), // apoptosis : apoptosed
                        // new Color(225, 124, 5),
                        // new Color(204, 80, 62)
                    },
                    new double[] {0, 1, 2, 3, 4, 5, 6, 7});

    /** Color map for cell populations. */
    static final Colors MAP_POPULATION =
            new Colors(
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
                    new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

    /** Color map for cell region overlay. */
    public static final Colors MAP_OVERLAY =
            new Colors(
                    new Color[] {
                        new Color(255, 255, 255, 0),
                        new Color(255, 255, 255, 50),
                        new Color(255, 255, 255, 100),
                    },
                    new double[] {0, 1, 2});

    /** Color map for cell cytoplasm region. */
    public static final Colors MAP_CYTOPLASM =
            new Colors(
                    new Color[] {
                        new Color(0, 0, 0, 0),
                        new Color(255, 0, 255, 50),
                        new Color(255, 0, 255, 255),
                    },
                    new double[] {0, 0.1, 1});

    /** Color map for cell nucleus region. */
    public static final Colors MAP_NUCLEUS =
            new Colors(
                    new Color[] {
                        new Color(0, 0, 0, 0),
                        new Color(0, 255, 255, 50),
                        new Color(0, 255, 255, 255),
                    },
                    new double[] {0, 0.1, 1});

    /** Color map for cell volume. */
    final Colors mapVolume;

    /** Color map for cell height. */
    final Colors mapHeight;

    /**
     * Creates {@code ColorMaps} for the given series.
     *
     * @param series the simulation series
     */
    PottsColorMaps(Series series) {
        double volume = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("CRITICAL_VOLUME") > volume) {
                volume = box.getDouble("CRITICAL_VOLUME");
            }
        }

        mapVolume =
                new Colors(
                        new Color[] {
                            new Color(0, 0, 0),
                            new Color(112, 40, 74),
                            new Color(156, 63, 93),
                            new Color(200, 88, 108),
                            new Color(220, 113, 118),
                            new Color(238, 138, 130),
                            new Color(245, 186, 152),
                            new Color(251, 230, 197),
                        },
                        new double[] {
                            0,
                            1. / 7 * volume * 2,
                            2. / 7 * volume * 2,
                            3. / 7 * volume * 2,
                            4. / 7 * volume * 2,
                            5. / 7 * volume * 2,
                            6. / 7 * volume * 2,
                            7. / 7 * volume * 2
                        });

        double height = 0;
        for (MiniBox box : series.populations.values()) {
            if (box.getDouble("CRITICAL_HEIGHT") > height) {
                height = box.getDouble("CRITICAL_HEIGHT");
            }
        }

        mapHeight =
                new Colors(
                        new Color[] {
                            new Color(0, 0, 0),
                            new Color(251, 230, 197),
                            new Color(245, 186, 152),
                            new Color(238, 138, 130),
                            new Color(220, 113, 118),
                            new Color(200, 88, 108),
                            new Color(156, 63, 93),
                            new Color(112, 40, 74),
                        },
                        new double[] {
                            0,
                            1. / 7 * height * 4,
                            2. / 7 * height * 4,
                            3. / 7 * height * 4,
                            4. / 7 * height * 4,
                            5. / 7 * height * 4,
                            6. / 7 * height * 4,
                            7. / 7 * height * 4
                        });
    }
}
