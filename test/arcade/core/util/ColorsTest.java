package arcade.core.util;

import java.awt.Color;

import org.junit.Test;
import static org.junit.Assert.*;

public class ColorsTest {

    @Test
    public void getRGB_calledIndices_returnsColors() {
        Color[] colors = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors1 = new Colors(colors);
        assertEquals(colors[0].getRGB(), colors1.getRGB(0));
        assertEquals(colors[1].getRGB(), colors1.getRGB(1));
    }

    @Test
    public void getRGB_calledDoubles_returnsRoundedDownColor() {
        Color[] colors = new Color[] {new Color(1, 1, 1, 1), new Color(255, 255, 255, 1)};
        Colors colors1 = new Colors(colors);
        assertEquals(colors[0].getRGB(), colors1.getRGB(0.5));
        assertEquals(colors[0].getRGB(), colors1.getRGB(0.99));
        assertEquals(colors[1].getRGB(), colors1.getRGB(1.1));
    }

    @Test
    public void getAlpha_called_returnsValid() {
        Color color1 = new Color(0, 0, 0, 0);
        assertEquals(0, color1.getAlpha());
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
    public void getRGB_calledInvalidNumber_returnsEmptyColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertEquals(new Color(0, 0, 0, 0).getRGB(), colors.getRGB(-1.0));
        assertEquals(new Color(0, 0, 0, 0).getRGB(), colors.getRGB(-1.5));
        assertEquals(new Color(0, 0, 0, 0).getRGB(), colors.getRGB(-2.0));
    }

    @Test
    public void getRGB_calledBetweenMinAndDefault_returnsMinColor()  {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(255, 255, 255, 1), 0.0, 1.0);
        assertEquals(new Color(1, 1, 1, 1).getRGB(), colors.getRGB(-0.5));
        assertEquals(new Color(1, 1, 1, 1).getRGB(), colors.getRGB(-0.1));
        assertEquals(new Color(1, 1, 1, 1).getRGB(), colors.getRGB(0.0));
    }

    @Test
    public void getRGB_calledLargerThanMax_returnsMaxColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(200, 200, 200, 1), 0.0, 1.0);
        assertEquals(new Color(200, 200, 200, 1).getRGB(), colors.getRGB(1.0));
        assertEquals(new Color(200, 200, 200, 1).getRGB(), colors.getRGB(2.0));
        assertEquals(new Color(200, 200, 200, 1).getRGB(), colors.getRGB(1.5));
    }

    @Test
    public void getRGB_calledBetweenMinAndMax_returnsInterpolatedSingleColor() {
        Colors colors = new Colors(new Color(1, 0, 0, 1), new Color(201, 0, 0, 1), 0.0, 1.0);
        assertEquals(new Color(51, 0, 0, 1).getRGB(), colors.getRGB(0.25));
        assertEquals(new Color(101, 0, 0, 1).getRGB(), colors.getRGB(0.5));
        assertEquals(new Color(151, 0, 0, 1).getRGB(), colors.getRGB(0.75));
    }

    @Test
    public void getRGB_calledBetweenMinAndMax_returnsInterpolatedColor() {
        Colors colors = new Colors(new Color(1, 1, 1, 1), new Color(201, 201, 201, 1), 0.0, 1.0);
        assertEquals(new Color(51, 51, 51, 1).getRGB(), colors.getRGB(0.25));
        assertEquals(new Color(101, 101, 101, 1).getRGB(), colors.getRGB(0.5));
        assertEquals(new Color(151, 151, 151, 1).getRGB(), colors.getRGB(0.75));
    }

    @Test
    public void getRGB_calledWithMod_returnsCorrectColor() {
        Colors colors = new Colors(new Color[] {
            new Color(0, 0, 0, 0),
            new Color(100, 100, 100, 0),
            new Color(200, 200, 200, 200),
        }, 3);
        assertEquals(new Color(0, 0, 0, 0).getRGB(), colors.getRGB(3));
        assertEquals(new Color(100, 100, 100, 0).getRGB(), colors.getRGB(4));
        assertEquals(new Color(200, 200, 200, 200).getRGB(), colors.getRGB(5));
    }

    @Test
    public void getRGB_NonlinearColorsCalledBetweenMinAndMax_returnsInterpolatedColor() {
        Colors colors = new Colors(new Color[] {
            new Color(0, 0, 0, 0),
            new Color(100, 100, 100, 0),
            new Color(200, 200, 200, 200),
        }, new double[] { 0, 0.1, 1});

        assertEquals(new Color(0, 0, 0, 0).getRGB(), colors.getRGB(0));

        assertEquals(new Color(50, 50, 50, 0).getRGB(), colors.getRGB(0.05));
        assertEquals(new Color(100, 100, 100, 0).getRGB(), colors.getRGB(0.1));
        assertEquals(new Color(200, 200, 200, 125).getRGB(), colors.getRGB(0.55));

        assertEquals(new Color(200, 200, 200, 200).getRGB(), colors.getRGB(1));
    }

}
