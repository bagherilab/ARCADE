package arcade.patch.vis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.DoubleGrid2D;
import sim.field.network.Network;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Portrayal;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.AbstractShapePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.gui.ColorMap;
import arcade.core.util.Graph.Edge;
import arcade.core.util.Graph.Node;
import arcade.core.vis.Drawer;
import arcade.core.vis.Panel;
import arcade.patch.env.component.PatchComponentSitesGraph;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteEdge;
import arcade.patch.env.component.PatchComponentSitesGraph.SiteNode;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.vis.PatchColorMaps.*;

/** Container for patch-specific {@link Drawer} classes. */
public abstract class PatchDrawer extends Drawer {
    /** Array holding values. */
    DoubleGrid2D array;

    /** Graph holding edges. */
    Network graph;

    /** Field holding nodes. */
    Continuous2D field;

    /** View options for cells. */
    enum CellView {
        /** Code for encoding visualization by agent state. */
        STATE,

        /** Code for encoding visualization by agent age. */
        AGE,

        /** Code for encoding visualization by agent volume. */
        VOLUME,

        /** Code for encoding visualization by agent height. */
        HEIGHT,

        /** Code for encoding visualization by agent counts. */
        COUNTS,

        /** Code for encoding visualization by agent population. */
        POPULATION,

        /** Code for encoding visualization by agent energy. */
        ENERGY,

        /** Code for encoding visualization by agent divisions. */
        DIVISIONS
    }

    /** View options for lattices. */
    enum LatticeView {
        /** Code for encoding visualization by lattice concentrations. */
        CONCENTRATION,

        /** Code for encoding visualization by component sites. */
        SITES,

        /** Code for encoding visualization by component damage. */
        DAMAGE
    }

    /**
     * Creates a {@link Drawer} for patch simulations.
     *
     * @param panel the panel the drawer is attached to
     * @param name the name of the drawer
     * @param length the length of array (x direction)
     * @param width the width of array (y direction)
     * @param depth the depth of array (z direction)
     * @param map the color map for the array
     * @param bounds the size of the drawer within the panel
     */
    PatchDrawer(
            Panel panel,
            String name,
            int length,
            int width,
            int depth,
            ColorMap map,
            Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }

    @Override
    public Portrayal makePort() {
        String[] split = name.split(":");
        graph = new Network(true);
        field = new Continuous2D(1.0, 1, 1);

        switch (split[0]) {
            case "label":
                return new LabelFieldPortrayal2D(length, width, "", 12);
            case "grid":
                SimpleEdgePortrayal2D sepg = new SimpleEdgePortrayal2DGridWrapper();
                NetworkPortrayal2D gridPort = new NetworkPortrayal2D();
                gridPort.setField(new SpatialNetwork2D(field, graph));
                gridPort.setPortrayalForAll(sepg);
                return gridPort;
            case "agents":
            case "environment":
                array = new DoubleGrid2D(length, width, map.defaultValue());
                ValueGridPortrayal2D valuePort = new FastValueGridPortrayal2D();
                valuePort.setField(array);
                valuePort.setMap(map);
                return valuePort;
            case "edges":
                String feature = split[1];
                SimpleEdgePortrayal2D sepe = new SimpleEdgePortrayal2DEdgeWrapper(feature);
                NetworkPortrayal2D edgePort = new NetworkPortrayal2D();
                edgePort.setField(new SpatialNetwork2D(field, graph));
                edgePort.setPortrayalForAll(sepe);
                return edgePort;
            case "nodes":
                ContinuousPortrayal2D nodePort = new ContinuousPortrayal2D();
                nodePort.setField(field);
                OvalPortrayal2DWrapper op = new OvalPortrayal2DWrapper();
                nodePort.setPortrayalForAll(op);
                return nodePort;
            default:
                return null;
        }
    }

    /** Wrapper for MASON class that changes thickness and color of edges. */
    private static class SimpleEdgePortrayal2DGridWrapper extends SimpleEdgePortrayal2D {
        /** Color for low weights. */
        private final Color lowColor = new Color(255, 255, 255, 30);

        /** Color for high weights. */
        private final Color highColor = new Color(255, 255, 255, 100);

        /** Creates {@code SimpleEdgePortrayal2D} wrapper with no scaling. */
        SimpleEdgePortrayal2DGridWrapper() {
            setScaling(NEVER_SCALE);
        }

        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge) object;
            int weight = (Integer) edge.getInfo();
            if (weight == 1) {
                shape = SHAPE_THIN_LINE;
                fromPaint = lowColor;
                toPaint = lowColor;
            } else if (weight == 3) {
                shape = SHAPE_LINE_BUTT_ENDS;
                fromPaint = highColor;
                toPaint = highColor;
            } else {
                shape = SHAPE_LINE_BUTT_ENDS;
                fromPaint = lowColor;
                toPaint = lowColor;
            }
            super.draw(object, graphics, info);
        }

        @Override
        protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge) object;
            return (Integer) edge.getInfo();
        }
    }

    /** Wrapper for MASON class that changes edge colors based on weight. */
    private static class SimpleEdgePortrayal2DEdgeWrapper extends SimpleEdgePortrayal2D {
        /** Edge feature to display. */
        private final String feature;

        /**
         * Creates {@code SimpleEdgePortrayal2D} wrapper with rounded edges.
         *
         * @param feature the name of the feature to display
         */
        SimpleEdgePortrayal2DEdgeWrapper(String feature) {
            setShape(SimpleEdgePortrayal2D.SHAPE_LINE_ROUND_ENDS);
            this.feature = feature;
        }

        /**
         * {@inheritDoc}
         *
         * <p>Gets color of edge based on drawing code.
         *
         * @return the edge weight
         */
        protected double getPositiveWeight(Object object, EdgeDrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge) object;
            SiteEdge siteEdge = (SiteEdge) (edge.getInfo());

            if (feature.equalsIgnoreCase("radius")) {
                return siteEdge.getRadius() / 25 + 0.1;
            } else if (feature.equalsIgnoreCase("wall")) {
                return (siteEdge.getRadius() + siteEdge.getWall() * 2) / 25.0 + 0.1;
            } else {
                return 2;
            }
        }

        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            sim.field.network.Edge edge = (sim.field.network.Edge) object;
            SiteEdge siteEdge = (SiteEdge) (edge.getInfo());

            if (feature.equalsIgnoreCase("radius")) {
                fromPaint = MAP_EDGE_RADIUS.getColor(siteEdge.getRadius() * siteEdge.getSign());
            } else if (feature.equalsIgnoreCase("wall")) {
                fromPaint = MAP_EDGE_WALL.getColor(siteEdge.getWall());
                if (siteEdge.getWall() <= 0.5) {
                    fromPaint = new Color(255, 255, 255);
                }
            } else {
                fromPaint = new Color(100, 100, 100);
            }

            toPaint = fromPaint;
            super.draw(object, graphics, info);
        }
    }

    /** Wrapper for MASON class that change node colors. */
    private static class OvalPortrayal2DWrapper extends AbstractShapePortrayal2D {
        /** Node portrayal scaling. */
        private static final double OVAL_SCALE = 0.7;

        /** Creates {@code AbstractShapePortrayal2D} wrapper. */
        OvalPortrayal2DWrapper() {
            super();
        }

        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            graphics.setPaint(new Color(255, 255, 255));
            SiteNode node = (SiteNode) object;

            if (node.getRoot()) {
                graphics.setPaint(new Color(255, 255, 0));
                Rectangle2D.Double draw = info.draw;
                double s = Math.min(draw.width, draw.height) / OVAL_SCALE;
                final int x = (int) (draw.x - s / 2.0);
                final int y = (int) (draw.y - s / 2.0);
                int w = (int) (s);
                int h = (int) (s);
                graphics.fillOval(x - 1, y - 1, w + 2, h + 2);
            }
            graphics.setPaint(MAP_NODE_PRESSURE.getColor(node.getPressure()));
            if (node.getPressure() <= 0) {
                graphics.setPaint(new Color(0, 255, 255));
            }

            Rectangle2D.Double draw = info.draw;
            double s = Math.min(draw.width, draw.height) / OVAL_SCALE;
            final int x = (int) (draw.x - s / 2.0);
            final int y = (int) (draw.y - s / 2.0);
            int w = (int) (s);
            int h = (int) (s);
            graphics.fillOval(x, y, w, h);
        }
    }

    /** Wrapper for MASON class that changes font style. */
    private static class LabelFieldPortrayal2D extends FieldPortrayal2D {
        /** General font size. */
        static final int FONT_SIZE = 12;

        /** Offset for label. */
        static final int OFFSET = 5;

        /** Offset in x direction. */
        final double xoffset;

        /** Offset in y direction. */
        final double yoffset;

        /** Label font size. */
        int fontSize;

        /** Label text. */
        String string;

        /**
         * Creates {@code FieldPortrayal2D} wrapper.
         *
         * @param xoffset the offset in x direction
         * @param yoffset the offset in y direction
         * @param string the text to draw
         * @param fontSize the size of font in points
         */
        LabelFieldPortrayal2D(int xoffset, int yoffset, String string, int fontSize) {
            super();
            this.string = string;
            this.xoffset = xoffset / 100.0;
            this.yoffset = yoffset / 100.0;
            this.fontSize = fontSize;
        }

        @Override
        public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            int x = (int) (info.draw.x + xoffset * info.draw.width + OFFSET);
            int y = (int) (info.draw.y + yoffset * info.draw.height + FONT_SIZE + OFFSET);
            graphics.setPaint(new Color(255, 255, 255));
            graphics.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            graphics.drawString(string, x, y);
        }
    }

    /**
     * Adds edges to graph.
     *
     * @param field the field to add nodes to
     * @param graph the graph to add edges to
     * @param weight the edge weight
     * @param x1 the x position of the from node
     * @param y1 the y position of the from node
     * @param x2 the x position of the to node
     * @param y2 the y position of the to node
     */
    static void add(Continuous2D field, Network graph, int weight, int x1, int y1, int x2, int y2) {
        Double2D a = new Double2D(x1, y1);
        Double2D b = new Double2D(x2, y2);
        field.setObjectLocation(a, a);
        field.setObjectLocation(b, b);
        graph.addEdge(a, b, weight);
    }

    /**
     * Converts a boolean array to a double array.
     *
     * @param fromArray the array to convert
     * @param toArray the array to store results
     */
    static void convert(boolean[][] fromArray, double[][] toArray) {
        for (int i = 0; i < fromArray.length; i++) {
            for (int j = 0; j < fromArray[0].length; j++) {
                toArray[i][j] += (fromArray[i][j] ? 1 : 0);
            }
        }
    }

    /** Extension of {@link Drawer} for drawing graphs. */
    public static class Graph extends PatchDrawer {
        /** Length of the lattice (x direction). */
        private final int length;

        /** Width of the lattice (y direction). */
        private final int width;

        /**
         * Creates a {@code Graph} drawer.
         *
         * @param panel the panel the drawer is attached to
         * @param name the name of the drawer
         * @param length the length of array (x direction)
         * @param width the width of array (y direction)
         * @param offset the offset for array geometry
         * @param bounds the size of the drawer within the panel
         */
        Graph(
                Panel panel,
                String name,
                int length,
                int width,
                int offset,
                Rectangle2D.Double bounds) {
            super(panel, name, length, width, 0, null, bounds);
            this.length = length + offset;
            this.width = width;
            field.width = length;
            field.height = width;
        }

        @Override
        public void step(SimState simstate) {
            PatchSimulation sim = (PatchSimulation) simstate;
            PatchComponentSitesGraph component =
                    (PatchComponentSitesGraph) sim.getComponent("SITES");

            field.clear();
            graph.clear();

            // Iterate through all edges in the sites bag.
            Bag bag = component.getGraph().getAllEdges();
            for (Object obj : bag) {
                Edge e = (Edge) obj;
                Node from = e.getFrom();
                Node to = e.getTo();
                field.setObjectLocation(from, new Double2D(from.getX(), from.getY()));
                field.setObjectLocation(to, new Double2D(to.getX(), to.getY()));
                graph.addEdge(from, to, e);
            }
        }
    }

    /** Extension of {@link Drawer} for drawing labels. */
    public static class Label extends PatchDrawer {
        /** Label text, timestamp shown if null. */
        String string;

        /**
         * Creates a {@code Label} drawer for the given string.
         *
         * @param panel the panel the drawer is attached to
         * @param name the name of the drawer
         * @param xoffset the offset in x direction
         * @param yoffset the offset in y direction
         * @param string the text to draw, set to null to show timestamp
         */
        public Label(Panel panel, String name, int xoffset, int yoffset, String string) {
            super(panel, name, xoffset, yoffset, 0, null, null);
            this.string = string;

            LabelFieldPortrayal2D port = (LabelFieldPortrayal2D) this.getPortrayal();
            if (string == null) {
                port.fontSize = 20;
            }
        }

        @Override
        public void step(SimState simstate) {
            LabelFieldPortrayal2D port = (LabelFieldPortrayal2D) this.getPortrayal();
            if (string == null) {
                double ticks = simstate.schedule.getTime();
                int days = (int) Math.floor(ticks / 60 / 24);
                int hours = (int) Math.floor((ticks - days * 60 * 24) / 60);
                int minutes = (int) ticks - days * 24 * 60 - hours * 60;
                port.string = String.format("%02d:%02d:%02d", days, hours, minutes);
            } else {
                port.string = string;
            }
        }
    }
}
