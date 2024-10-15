package arcade.core.util;

import org.junit.Test;
import java.awt.Color;
import static org.junit.Assert.*;

public class ColorsTest {
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

}
