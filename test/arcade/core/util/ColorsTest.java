package arcade.core.util;

import java.awt.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorsTest {

    @Test
    public void getColor_calledWithIndices_returnsColors() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertAll(
                () -> assertEquals(colorArray[0], colors.getColor(0)),
                () -> assertEquals(colorArray[1], colors.getColor(1)));
    }

    @Test
    public void getRGB_calledWithIndices_returnsRGB() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertAll(
                () -> assertEquals(colorArray[0].getRGB(), colors.getRGB(0)),
                () -> assertEquals(colorArray[1].getRGB(), colors.getRGB(1)));
    }

    @Test
    public void getColor_calledWithDoubles_returnsRoundedDownColor() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertAll(
                () -> assertEquals(colorArray[0], colors.getColor(0.0)),
                () -> assertEquals(colorArray[0], colors.getColor(0.1)),
                () -> assertEquals(colorArray[0], colors.getColor(0.49)),
                () -> assertEquals(colorArray[0], colors.getColor(0.5)),
                () -> assertEquals(colorArray[0], colors.getColor(0.99)),
                () -> assertEquals(colorArray[1], colors.getColor(1.1)));
    }

    @Test
    public void getRGB_calledWithDoubles_returnsRoundedDownColor() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertAll(
                () -> assertEquals(0, colors.getRGB(-1)),
                () -> assertEquals(colorArray[0].getRGB(), colors.getRGB(0.5)),
                () -> assertEquals(colorArray[0].getRGB(), colors.getRGB(0.99)),
                () -> assertEquals(colorArray[1].getRGB(), colors.getRGB(1.1)));
    }

    @Test
    public void getAlpha_calledWithLevel_returnsCorrectAlpha() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 255)};
        Colors colors = new Colors(colorArray);
        assertAll(
                () -> assertEquals(255, colors.getAlpha(1)),
                () -> assertEquals(1, colors.getAlpha(0)),
                () -> assertEquals(0, colors.getAlpha(-1)));
    }

    @Test
    public void getRGB_calledWithLevel_returnsCorrectRGB() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertAll(
                () -> assertEquals(new Color(255, 255, 255, 1).getRGB(), colors.getRGB(1.0)),
                () -> assertEquals(new Color(1, 1, 1, 1).getRGB(), colors.getRGB(0.0)));
    }

    @Test
    public void getColor_smallerThanDefault_returnsEmptyColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertAll(
                () -> assertEquals(new Color(0, 0, 0, 0), colors.getColor(-1.0)),
                () -> assertEquals(new Color(0, 0, 0, 0), colors.getColor(-1.5)),
                () -> assertEquals(new Color(0, 0, 0, 0), colors.getColor(-2.0)));
    }

    @Test
    public void getColor_calledBetweenMinAndDefault_returnsMinColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertAll(
                () -> assertEquals(new Color(1, 1, 1, 1), colors.getColor(-0.5)),
                () -> assertEquals(new Color(1, 1, 1, 1), colors.getColor(-0.1)),
                () -> assertEquals(new Color(1, 1, 1, 1), colors.getColor(0.0)));
    }

    @Test
    public void getColor_calledLargerThanMax_returnsMaxColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(200, 200, 200, 1), 0.0, 1.0);
        assertAll(
                () -> assertEquals(new Color(200, 200, 200, 1), colors.getColor(1.5)),
                () -> assertEquals(new Color(200, 200, 200, 1), colors.getColor(2.0)),
                () -> assertEquals(new Color(200, 200, 200, 1), colors.getColor(2.5)));
    }

    @Test
    public void getColor_calledBetweenMinAndMax_returnsInterpolatedColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(201, 201, 201, 1), 0.0, 1.0);
        assertAll(
                () -> assertEquals(new Color(1, 1, 1, 1), colors.getColor(0.0)),
                () -> assertEquals(new Color(51, 51, 51, 1), colors.getColor(0.25)),
                () -> assertEquals(new Color(101, 101, 101, 1), colors.getColor(0.5)),
                () -> assertEquals(new Color(151, 151, 151, 1), colors.getColor(0.75)),
                () -> assertEquals(new Color(201, 201, 201, 1), colors.getColor(1.0)));
    }

    @Test
    public void getColor_calledWithMod_returnsCorrectColor() {
        Colors colors =
                new Colors(
                        new Color[] {
                            new Color(0, 0, 0, 0),
                            new Color(100, 100, 100, 0),
                            new Color(200, 200, 200, 200),
                        },
                        3);
        assertAll(
                () -> assertEquals(new Color(0, 0, 0, 0), colors.getColor(3)),
                () -> assertEquals(new Color(100, 100, 100, 0), colors.getColor(4)),
                () -> assertEquals(new Color(200, 200, 200, 200), colors.getColor(5)));
    }

    @Test
    public void getColor_NonlinearColorsCalledBetweenMinAndMax_returnsInterpolatedColor() {
        Colors colors =
                new Colors(
                        new Color[] {
                            new Color(0, 0, 0, 0),
                            new Color(100, 100, 100, 0),
                            new Color(200, 200, 200, 200),
                        },
                        new double[] {0, 0.1, 1});
        assertAll(
                () -> assertEquals(new Color(0, 0, 0, 0), colors.getColor(0)),
                () -> assertEquals(new Color(48, 48, 48, 0), colors.getColor(0.05)),
                () -> assertEquals(new Color(100, 100, 100, 0), colors.getColor(0.1)),
                () -> assertEquals(new Color(150, 150, 150, 100), colors.getColor(0.55)),
                () -> assertEquals(new Color(200, 200, 200, 200), colors.getColor(1)));
    }

    @Test
    public void validLevel_calledWithInvalidLevel_returnsFalse() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);

        assertAll(
                () -> assertFalse(colors.validLevel(-1.0)),
                () -> assertFalse(colors.validLevel(-0.5)),
                () -> assertFalse(colors.validLevel(1.1)),
                () -> assertFalse(colors.validLevel(2.0)));
    }

    @Test
    public void validLevel_calledWithValidLevel_returnsTrue() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);

        assertAll(
                () -> assertTrue(colors.validLevel(0.0)),
                () -> assertTrue(colors.validLevel(0.5)),
                () -> assertTrue(colors.validLevel(1.0)));
    }

    @Test
    public void defaultValue_called_returnsDefaultValue() {
        Colors colorsArray =
                new Colors(new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)});
        Colors colorsValues = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 4, 10);
        Colors colorsValueArray =
                new Colors(
                        new Color[] {
                            new Color(0, 0, 0, 0),
                            new Color(100, 100, 100, 0),
                            new Color(200, 200, 200, 200),
                        },
                        new double[] {0, 0.1, 1});

        assertAll(
                () -> assertEquals(-1.0, colorsArray.defaultValue()),
                () -> assertEquals(3.0, colorsValues.defaultValue()),
                () -> assertEquals(-1.0, colorsValueArray.defaultValue()));
    }
}
