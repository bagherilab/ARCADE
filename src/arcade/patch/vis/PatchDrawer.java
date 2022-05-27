package arcade.patch.vis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.util.Double2D;
import sim.util.gui.ColorMap;
import arcade.core.vis.Drawer;
import arcade.core.vis.Panel;

/**
 * Container for patch-specific {@link Drawer} classes.
 */

public abstract class PatchDrawer extends Drawer {
    /** Graph holding edges */
    Network graph;
    
    /** Field holding nodes */
    Continuous2D field;

    /**
     * Creates a {@link Drawer} for potts simulations.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param map  the color map for the array
     * @param bounds  the size of the drawer within the panel
     */
    PatchDrawer(Panel panel, String name, int length, int width, int depth,
                ColorMap map, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }
    
    /**
     * Creates a {@link Drawer} for potts simulations.
     *
     * @param panel  the panel the drawer is attached to
     * @param name  the name of the drawer
     * @param length  the length of array (x direction)
     * @param width  the width of array (y direction)
     * @param depth  the depth of array (z direction)
     * @param bounds  the size of the drawer within the panel
     */
    PatchDrawer(Panel panel, String name, int length, int width, int depth,
                Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }

    public Portrayal makePort() {
        String[] split = name.split(":");
    
        switch (split[0]) {
            case "grid":
                graph = new Network(true);
                field = new Continuous2D(1.0, 1, 1);
                SimpleEdgePortrayal2D sep = new SimpleEdgePortrayal2DGridWrapper();
                NetworkPortrayal2D gridPort = new NetworkPortrayal2D();
                gridPort.setField(new SpatialNetwork2D(field, graph));
                gridPort.setPortrayalForAll(sep);
                return gridPort;
            default:
                return null;
        }
    }
    
    /** Wrapper for MASON class that changes thickness and color of edges. */
    private static class SimpleEdgePortrayal2DGridWrapper extends SimpleEdgePortrayal2D {
        /** Color for low weights */
        private Color lowColor = new Color(255,255,255,30);
        
        /** Color for high weights */
        private Color highColor = new Color(255,255,255,100);
        
        /**
         * Creates {@code SimpleEdgePortrayal2D} wrapper with no scaling.
         */
        SimpleEdgePortrayal2DGridWrapper() { setScaling(NEVER_SCALE); }
        
        /**
         * Draws the edge.
         */
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge)object;
            int weight = (Integer)edge.getInfo();
            if (weight == 1) {
                shape = SHAPE_THIN_LINE;
                fromPaint = lowColor;
                toPaint = lowColor;
            }
            else if (weight == 3) {
                shape = SHAPE_LINE_BUTT_ENDS;
                fromPaint = highColor;
                toPaint = highColor;
            }
            else {
                shape = SHAPE_LINE_BUTT_ENDS;
                fromPaint = lowColor;
                toPaint = lowColor;
            }
            super.draw(object, graphics, info);
        }
        
        /**
         * Gets the weight of an edge.
         *
         * @return  the edge weight
         */
        protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge)object;
            return (Integer)edge.getInfo();
        }
    }
    
    /** Wrapper for MASON class that changes font style. */
    private static class LabelFieldPortrayal2D extends FieldPortrayal2D {
        /** General font size */
        static final int FONT_SIZE = 12;
        
        /** Offset for label */
        static final int OFFSET = 5;
        
        /** Offset in x direction */
        final double xoffset;
        
        /** Offset in y direction */
        final double yoffset;
        
        /** Label font size */
        int fontSize;
        
        /** Label text */
        String string;
        
        /**
         * Creates {@code FieldPortrayal2D} wrapper.
         *
         * @param xoffset  the offset in x direction
         * @param yoffset  the offset in y direction
         * @param string  the text to draw
         * @param fontSize  the size of font in points
         */
        LabelFieldPortrayal2D(int xoffset, int yoffset, String string, int fontSize) {
            super();
            this.string = string;
            this.xoffset = xoffset/100.0;
            this.yoffset = yoffset/100.0;
            this.fontSize = fontSize;
        }
        
        /**
         * Draws the label.
         */
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            int x = (int)(info.draw.x + xoffset * info.draw.width + OFFSET);
            int y = (int)(info.draw.y + yoffset * info.draw.height + FONT_SIZE + OFFSET);
            graphics.setPaint(new Color(255,255,255));
            graphics.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            graphics.drawString(string, x, y);
        }
    }
    
    /**
     * Adds edges to graph.
     *
     * @param field  the field to add nodes to
     * @param graph  the graph to add edges to
     * @param weight  the edge weight
     * @param x1  the x position of the from node
     * @param y1  the y position of the from node
     * @param x2  the x position of the to node
     * @param y2  the y position of the to node
     */
    static void add(Continuous2D field, Network graph, int weight,
                            int x1, int y1, int x2, int y2) {
        Double2D a = new Double2D(x1, y1);
        Double2D b = new Double2D(x2, y2);
        field.setObjectLocation(a, a);
        field.setObjectLocation(b, b);
        graph.addEdge(a, b, weight);
    }
    
    /**
     * Extension of {@link Drawer} for drawing labels.
     */
    public static class Label extends PatchDrawer {
        /** Label text, timestamp shown if null. */
        String string;
        
        /**
         * Creates a {@code Label} drawer for the given string.
         *
         * @param panel  the panel the drawer is attached to
         * @param name  the name of the drawer
         * @param xoffset  the offset in x direction
         * @param yoffset  the offset in y direction
         * @param string  the text to draw, set to null to show timestamp
         */
        public Label(Panel panel, String name, int xoffset, int yoffset, String string) {
            super(panel, name, xoffset, yoffset, 0, null, null);
            this.string = string;
            
            LabelFieldPortrayal2D port = (LabelFieldPortrayal2D)this.getPortrayal();
            if (string == null) { port.fontSize = 20; }
        }
        
        @Override
        public Portrayal makePort() {
            return new LabelFieldPortrayal2D(length, width, "", 12);
        }
        
        /**
         * Steps the drawer to add a label.
         *
         * @param state  the MASON simulation state
         */
        public void step(SimState state) {
            LabelFieldPortrayal2D port = (LabelFieldPortrayal2D)this.getPortrayal();
            if (string == null) {
                double ticks = state.schedule.getTime();
                int days = (int) Math.floor(ticks / 60 / 24);
                int hours = (int) Math.floor((ticks - days * 60 * 24) / 60);
                int minutes = (int) ticks - days * 24 * 60 - hours * 60;
                port.string = String.format("%02d:%02d:%02d", days, hours, minutes);
            }
            else { port.string = string; }
        }
    }
}