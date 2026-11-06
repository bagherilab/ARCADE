package arcade.potts.env.component;

import arcade.core.util.Graph;

/**
 * Segment in a {@link PottsVesselNetwork}.
 *
 * <p>The direction of the segment is stored orientation only; blood flow is not computed. The
 * segment holds its integrity and whether it is perfused, which are kept on the segment object
 * rather than on nodes.
 */
public class PottsVesselEdge extends Graph.Edge {
    /** Integrity of the segment, in [0, 1]. */
    private double integrity;

    /** {@code true} if the segment is perfused, {@code false} otherwise. */
    private boolean perfused;

    /**
     * Creates a {@code PottsVesselEdge} between two nodes.
     *
     * @param from the start node
     * @param to the end node
     * @param integrity the integrity, in [0, 1]
     */
    public PottsVesselEdge(PottsVesselNode from, PottsVesselNode to, double integrity) {
        super(from, to);
        setIntegrity(integrity);
    }

    /**
     * Gets the integrity of the segment.
     *
     * @return the integrity, in [0, 1]
     */
    public double getIntegrity() {
        return integrity;
    }

    /**
     * Sets the integrity of the segment.
     *
     * @param integrity the integrity, in [0, 1]
     */
    public void setIntegrity(double integrity) {
        if (!(integrity >= 0 && integrity <= 1)) {
            throw new IllegalArgumentException("segment integrity must be in [0, 1]");
        }
        this.integrity = integrity;
    }

    /**
     * Checks if the segment is perfused.
     *
     * @return {@code true} if the segment is perfused, {@code false} otherwise
     */
    public boolean isPerfused() {
        return perfused;
    }

    /**
     * Sets whether the segment is perfused.
     *
     * @param perfused {@code true} if the segment is perfused, {@code false} otherwise
     */
    void setPerfused(boolean perfused) {
        this.perfused = perfused;
    }

    /**
     * Gets the start node of the segment.
     *
     * @return the start node
     */
    public PottsVesselNode getStart() {
        return (PottsVesselNode) getFrom();
    }

    /**
     * Gets the end node of the segment.
     *
     * @return the end node
     */
    public PottsVesselNode getEnd() {
        return (PottsVesselNode) getTo();
    }
}
