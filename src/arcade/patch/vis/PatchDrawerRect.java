package arcade.patch.vis;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import sim.engine.SimState;
import sim.util.gui.ColorMap;
import arcade.core.vis.Panel;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.component.PatchComponentSites;
import arcade.patch.env.component.PatchComponentSitesPattern;
import arcade.patch.env.component.PatchComponentSitesSource;
import arcade.patch.env.location.Coordinate;
import arcade.patch.env.location.CoordinateXYZ;
import arcade.patch.env.location.PatchLocation;
import arcade.patch.env.location.PatchLocationContainer;
import arcade.patch.env.location.PatchLocationFactory;
import arcade.patch.env.location.PatchLocationFactoryRect;
import arcade.patch.sim.PatchSeries;
import arcade.patch.sim.PatchSimulation;
import static arcade.patch.util.PatchEnums.State;

/** Container for patch-specific {@link arcade.core.vis.Drawer} classes for rectangular patches. */
public abstract class PatchDrawerRect extends PatchDrawer {
    /**
     * Creates a {@link PatchDrawer} for rectangular patch simulations.
     *
     * @param panel the panel the drawer is attached to
     * @param name the name of the drawer
     * @param length the length of array (x direction)
     * @param width the width of array (y direction)
     * @param depth the depth of array (z direction)
     * @param map the color map for the array
     * @param bounds the size of the drawer within the panel
     */
    PatchDrawerRect(
            Panel panel,
            String name,
            int length,
            int width,
            int depth,
            ColorMap map,
            Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, map, bounds);
    }

    /**
     * Creates a {@link PatchDrawer} for rectangular patch simulations.
     *
     * @param panel the panel the drawer is attached to
     * @param name the name of the drawer
     * @param length the length of array (x direction)
     * @param width the width of array (y direction)
     * @param depth the depth of array (z direction)
     * @param bounds the size of the drawer within the panel
     */
    PatchDrawerRect(
            Panel panel, String name, int length, int width, int depth, Rectangle2D.Double bounds) {
        super(panel, name, length, width, depth, null, bounds);
    }

    /** Extension of {@link PatchDrawer} for drawing rectangular cells. */
    public static class PatchCells extends PatchDrawerRect {
        /** Length of the lattice (x direction). */
        private final int length;

        /** Width of the lattice (y direction). */
        private final int width;

        /** Drawing view. */
        private final CellView view;

        /**
         * Creates a {@link PatchDrawer} for drawing rectangular cells.
         *
         * @param panel the panel the drawer is attached to
         * @param name the name of the drawer
         * @param length the length of array (x direction)
         * @param width the width of array (y direction)
         * @param depth the depth of array (z direction)
         * @param map the color map for the array
         * @param bounds the size of the drawer within the panel
         */
        public PatchCells(
                Panel panel,
                String name,
                int length,
                int width,
                int depth,
                ColorMap map,
                Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, map, bounds);
            this.length = length;
            this.width = width;
            String[] split = name.split(":");
            view = CellView.valueOf(split[1]);
        }

        @Override
        public void step(SimState simstate) {
            PatchSimulation sim = (PatchSimulation) simstate;
            double[][] arr = array.field;
            double[][] counter = new double[length][width];

            PatchCell cell;
            PatchLocation location;

            switch (view) {
                case STATE:
                case AGE:
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            arr[i][j] = map.defaultValue();
                        }
                    }
                    break;
                case VOLUME:
                case HEIGHT:
                case COUNTS:
                case POPULATION:
                case ENERGY:
                case DIVISIONS:
                    array.setTo(0);
                    break;
                default:
                    break;
            }

            HashMap<Integer, Integer> indices = new HashMap<>();

            for (Object obj : sim.getGrid().getAllObjects()) {
                cell = (PatchCell) obj;
                location = (PatchLocation) cell.getLocation();

                if (location.getCoordinate().z == 0) {
                    ArrayList<Coordinate> coords = location.getSubcoordinates();

                    int hash = location.hashCode();
                    int index = -1;
                    if (indices.containsKey(hash)) {
                        index = indices.get(hash);
                    }
                    indices.put(hash, ++index);

                    switch (view) {
                        case STATE:
                        case AGE:
                        case POPULATION:
                        case ENERGY:
                        case DIVISIONS:
                            CoordinateXYZ mainCoord = (CoordinateXYZ) coords.get(index);
                            switch (view) {
                                case STATE:
                                    arr[mainCoord.x][mainCoord.y] =
                                            ((State) cell.getState()).ordinal();
                                    break;
                                case AGE:
                                    arr[mainCoord.x][mainCoord.y] = cell.getAge();
                                    break;
                                case POPULATION:
                                    arr[mainCoord.x][mainCoord.y] = cell.getPop();
                                    break;
                                case ENERGY:
                                    arr[mainCoord.x][mainCoord.y] = -cell.getEnergy();
                                    break;
                                case DIVISIONS:
                                    arr[mainCoord.x][mainCoord.y] = cell.getDivisions();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case COUNTS:
                        case VOLUME:
                        case HEIGHT:
                            double volume = cell.getVolume();
                            double height = cell.getHeight();

                            for (Coordinate coord : coords) {
                                CoordinateXYZ rectCoord = (CoordinateXYZ) coord;

                                switch (view) {
                                    case COUNTS:
                                        arr[rectCoord.x][rectCoord.y]++;
                                        break;
                                    case VOLUME:
                                        arr[rectCoord.x][rectCoord.y] += volume;
                                        counter[rectCoord.x][rectCoord.y]++;
                                        break;
                                    case HEIGHT:
                                        arr[rectCoord.x][rectCoord.y] += height;
                                        counter[rectCoord.x][rectCoord.y]++;
                                    default:
                                        break;
                                }
                            }
                        default:
                            break;
                    }
                }
            }

            switch (view) {
                case VOLUME:
                case HEIGHT:
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            arr[i][j] /= counter[i][j];
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /** Extension of {@link PatchDrawer} for drawing rectangular lattices. */
    public static class PatchLayers extends PatchDrawerRect {
        /** Length of the lattice (x direction). */
        private final int length;

        /** Width of the lattice (y direction). */
        private final int width;

        /** Lattice z index to display. */
        private final int index;

        /** Drawing view. */
        private final LatticeView view;

        /** Layer key. */
        private final String key;

        /**
         * Creates a {@link PatchDrawer} for drawing rectangular layers.
         *
         * @param panel the panel the drawer is attached to
         * @param name the name of the drawer
         * @param length the length of array (x direction)
         * @param width the width of array (y direction)
         * @param depth the depth of array (z direction)
         * @param map the color map for the array
         * @param bounds the size of the drawer within the panel
         */
        public PatchLayers(
                Panel panel,
                String name,
                int length,
                int width,
                int depth,
                ColorMap map,
                Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, map, bounds);
            this.length = length;
            this.width = width;
            this.index = ((depth - 1) / 2);
            String[] split = name.split(":");
            view = LatticeView.valueOf(split[1]);
            key = (split.length == 3 ? split[2] : null);
        }

        @Override
        public void step(SimState simstate) {
            PatchSimulation sim = (PatchSimulation) simstate;
            double[][] temp = new double[length][width];

            switch (view) {
                case CONCENTRATION:
                    temp = sim.getLattice(key).getField()[index];
                    break;
                case SITES:
                case DAMAGE:
                    PatchComponentSites component = (PatchComponentSites) sim.getComponent("SITES");

                    if (component instanceof PatchComponentSitesSource) {
                        PatchComponentSitesSource sites = (PatchComponentSitesSource) component;
                        if (view == LatticeView.SITES) {
                            boolean[][][] sources = sites.getSources();
                            convert(sources[index], temp);
                        } else {
                            temp = sites.getDamage()[index];
                        }
                    } else if (component instanceof PatchComponentSitesPattern) {
                        PatchComponentSitesPattern sites = (PatchComponentSitesPattern) component;
                        if (view == LatticeView.SITES) {
                            boolean[][][] pattern = sites.getPatterns();
                            boolean[][][] anchors = sites.getAnchors();
                            convert(pattern[index], temp);
                            convert(anchors[index], temp);
                        } else {
                            temp = sites.getDamage()[index];
                        }
                    }
                    break;
                default:
                    break;
            }

            array.field = temp;
        }
    }

    /** Extension of {@link PatchDrawer} for drawing rectangular patches. */
    public static class PatchGrid extends PatchDrawerRect {
        /** Offsets for rectangles. */
        private static final int[][] OFFSETS = {{0, 0}, {2, 0}, {2, 2}, {0, 2}};

        /** Length of the lattice (x direction). */
        private final int length;

        /** Width of the lattice (y direction). */
        private final int width;

        /** List of patch locations. */
        private ArrayList<PatchLocation> locations;

        /**
         * Creates a {@code PatchGrid} drawer.
         *
         * @param panel the panel the drawer is attached to
         * @param name the name of the drawer
         * @param length the length of array (x direction)
         * @param width the width of array (y direction)
         * @param depth the depth of array (z direction)
         * @param bounds the size of the drawer within the panel
         */
        PatchGrid(
                Panel panel,
                String name,
                int length,
                int width,
                int depth,
                Rectangle2D.Double bounds) {
            super(panel, name, length, width, depth, bounds);
            this.length = length;
            this.width = width;
            field.width = this.length;
            field.height = this.width;
        }

        @Override
        public void step(SimState simstate) {
            PatchSimulation sim = (PatchSimulation) simstate;
            field.clear();
            graph.clear();

            if (locations == null) {
                locations = new ArrayList<>();
                PatchSeries series = (PatchSeries) sim.getSeries();
                PatchLocationFactory factory = new PatchLocationFactoryRect();
                ArrayList<Coordinate> coordinates =
                        factory.getCoordinates(series.radius, series.depth);

                for (Coordinate coordinate : coordinates) {
                    PatchLocationContainer container = new PatchLocationContainer(0, coordinate);
                    PatchLocation location = (PatchLocation) container.convert(factory, null);
                    locations.add(location);
                }
            }

            // Draw rectangular grid.
            for (int i = 0; i <= width; i++) {
                add(field, graph, 1, 0, i, length, i);
            }

            for (int i = 0; i <= length; i++) {
                add(field, graph, 1, i, 0, i, width);
            }

            // Draw rectangular agent locations.
            for (PatchLocation loc : locations) {
                CoordinateXYZ rect = (CoordinateXYZ) loc.getSubcoordinate();
                for (int i = 0; i < 4; i++) {
                    add(
                            field,
                            graph,
                            2,
                            rect.x + OFFSETS[i][0],
                            rect.y + OFFSETS[i][1],
                            rect.x + OFFSETS[(i + 1) % 4][0],
                            rect.y + OFFSETS[(i + 1) % 4][1]);
                }
            }

            // Draw border.
            int radius = ((PatchSeries) sim.getSeries()).radius;
            int ind;
            int r;
            for (PatchLocation loc : locations) {
                CoordinateXYZ coord = (CoordinateXYZ) loc.getCoordinate();
                CoordinateXYZ subcoord = (CoordinateXYZ) loc.getSubcoordinate();

                r = Math.max(Math.abs(coord.x), Math.abs(coord.y)) + 1;

                if (r == radius) {
                    if (coord.x == radius - 1) {
                        ind = 1;
                    } else if (coord.x == 1 - radius) {
                        ind = 3;
                    } else if (coord.y == radius - 1) {
                        ind = 2;
                    } else if (coord.y == 1 - radius) {
                        ind = 0;
                    } else {
                        ind = 0;
                    }

                    add(
                            field,
                            graph,
                            3,
                            subcoord.x + OFFSETS[ind][0],
                            subcoord.y + OFFSETS[ind][1],
                            subcoord.x + OFFSETS[(ind + 1) % 4][0],
                            subcoord.y + OFFSETS[(ind + 1) % 4][1]);

                    if (Math.abs(coord.x) + 1 == r && Math.abs(coord.y) + 1 == r) {
                        if (coord.x == radius - 1 && coord.y == radius - 1) {
                            ind = 2;
                        } else if (coord.x == 1 - radius && coord.y == radius - 1) {
                            ind = 2;
                        } else if (coord.x == radius - 1 && coord.y == 1 - radius) {
                            ind = 0;
                        } else if (coord.x == 1 - radius && coord.y == 1 - radius) {
                            ind = 0;
                        }

                        add(
                                field,
                                graph,
                                3,
                                subcoord.x + OFFSETS[ind][0],
                                subcoord.y + OFFSETS[ind][1],
                                subcoord.x + OFFSETS[(ind + 1) % 4][0],
                                subcoord.y + OFFSETS[(ind + 1) % 4][1]);
                    }
                }
            }
        }
    }
}
