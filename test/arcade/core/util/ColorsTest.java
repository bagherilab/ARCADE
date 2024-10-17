package arcade.core.util;

import java.awt.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorsTest {

    @Test
    public void getColor_calledIndices_returnsColors() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertEquals(colorArray[0], colors.getColor(0));
        assertEquals(colorArray[1], colors.getColor(1));
    }

    @Test
    public void getRGB_calledIndices_returnsRGB() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertEquals(colorArray[0].getRGB(), colors.getRGB(0));
        assertEquals(colorArray[1].getRGB(), colors.getRGB(1));
    }

    @Test
    public void getColor_calledDoubles_returnsRoundedDownColor() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertEquals(colorArray[0], colors.getColor(0.5));
        assertEquals(colorArray[0], colors.getColor(0.99));
        assertEquals(colorArray[1], colors.getColor(1.1));
    }

    @Test
    public void getRGB_calledDoubles_returnsRoundedDownColor() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertEquals(colorArray[0].getRGB(), colors.getRGB(0.5));
        assertEquals(colorArray[0].getRGB(), colors.getRGB(0.99));
        assertEquals(colorArray[1].getRGB(), colors.getRGB(1.1));
    }

    @Test
    public void getAlpha_called_returnsValid() {
        Color[] colorArray = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors = new Colors(colorArray);
        assertEquals(1, colors.getAlpha(0.0));
    }

    @Test
    public void getColor_called_returnsValid() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertEquals(new Color(255, 255, 255, 1), colors.getColor(1.0));
    }

    @Test
    public void getRGB_called_returnsValid() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertEquals(new Color(255, 255, 255, 1).getRGB(), colors.getRGB(1.0));
        assertEquals(new Color(1, 1, 1, 1).getRGB(), colors.getRGB(0.0));
    }

    @Test
    public void getColor_smallerThanDefault_returnsEmptyColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertEquals(new Color(0, 0, 0, 0), colors.getColor(-1.0));
        assertEquals(new Color(0, 0, 0, 0), colors.getColor(-1.5));
        assertEquals(new Color(0, 0, 0, 0), colors.getColor(-2.0));
    }

    @Test
    public void getColor_calledBetweenMinAndDefault_returnsMinColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertEquals(new Color(1, 1, 1, 1), colors.getColor(-0.5));
        assertEquals(new Color(1, 1, 1, 1), colors.getColor(-0.1));
        assertEquals(new Color(1, 1, 1, 1), colors.getColor(0.0));
    }

    @Test
    public void getColor_calledLargerThanMax_returnsMaxColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(200, 200, 200, 1), 0.0, 1.0);
        assertEquals(new Color(200, 200, 200, 1), colors.getColor(1.0));
        assertEquals(new Color(200, 200, 200, 1), colors.getColor(2.0));
        assertEquals(new Color(200, 200, 200, 1), colors.getColor(1.5));
    }

    @Test
    public void getColor_calledBetweenMinAndMax_returnsInterpolatedColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(201, 201, 201, 1), 0.0, 1.0);
        assertEquals(new Color(51, 51, 51, 1), colors.getColor(0.25));
        assertEquals(new Color(101, 101, 101, 1), colors.getColor(0.5));
        assertEquals(new Color(151, 151, 151, 1), colors.getColor(0.75));
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
        assertEquals(new Color(0, 0, 0, 0), colors.getColor(3));
        assertEquals(new Color(100, 100, 100, 0), colors.getColor(4));
        assertEquals(new Color(200, 200, 200, 200), colors.getColor(5));
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

        assertEquals(new Color(0, 0, 0, 0), colors.getColor(0));
        // assertEquals(new Color(50, 50, 50, 0), colors.getColor(0.05));
        assertEquals(new Color(100, 100, 100, 0), colors.getColor(0.1));
        assertEquals(new Color(150, 150, 150, 100), colors.getColor(0.55));
        assertEquals(new Color(200, 200, 200, 200), colors.getColor(1));
    }
}
