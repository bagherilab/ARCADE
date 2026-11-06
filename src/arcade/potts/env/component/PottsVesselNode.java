package arcade.potts.env.component;

import arcade.core.util.Graph;

/**
 * Node in a {@link PottsVesselNetwork}.
 *
 * <p>Coordinates are integers in units of {@link #UNIT} um. A node can be a root, where blood
 * enters (arterial) or leaves (venous) the network. {@link #duplicate()} copies the root tag, so it
 * survives the copies that {@link Graph} makes of each node. The graph holds several copies of each
 * node, so node data must not change after the node is added to a network.
 */
public class PottsVesselNode extends Graph.Node {
    /** Size of one coordinate unit [um]. */
    public static final double UNIT = 0.1;

    /** Root types of vessel nodes. */
    public enum Root {
        /** Not a root. */
        NONE,

        /** Root where blood enters the network. */
        ARTERIAL,

        /** Root where blood leaves the network. */
        VENOUS
    }

    /** Root type of the node. */
    private final Root root;

    /**
     * Creates a {@code PottsVesselNode} at integer coordinates.
     *
     * @param x the x coordinate [units]
     * @param y the y coordinate [units]
     * @param z the z coordinate [units]
     * @param root the root type
     */
    public PottsVesselNode(int x, int y, int z, Root root) {
        super(x, y, z);
        this.root = root == null ? Root.NONE : root;
    }

    /**
     * Gets the root type of the node.
     *
     * @return the root type
     */
    public Root getRoot() {
        return root;
    }

    /**
     * Checks if the node is a root.
     *
     * @return {@code true} if the node is arterial or venous, {@code false} otherwise
     */
    public boolean isRoot() {
        return root != Root.NONE;
    }

    /**
     * Gets the position of the node.
     *
     * @return the position {x, y, z} [um]
     */
    public double[] getPosition() {
        return new double[] {getX() * UNIT, getY() * UNIT, getZ() * UNIT};
    }

    @Override
    public Graph.Node duplicate() {
        return new PottsVesselNode(getX(), getY(), getZ(), root);
    }
}
