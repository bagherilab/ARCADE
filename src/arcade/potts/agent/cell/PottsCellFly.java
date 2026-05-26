package arcade.potts.agent.cell;

import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

/**
 * Implementation of {@link PottsCell} for Potts Fly models.
 *
 * <p>Cells follow {@link PottsCell} rules, but additionally keep track of the amount of prospero.
 *
 * <p>[TODO: fix class comment]
 */
public abstract class PottsCellFly extends PottsCell {

    /** Amount of prospero in cell. */
    private double prospero;

    /** Amount of deadpan in cell. */
    private double deadpan;

    public PottsCellFly(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        this.prospero = 0;
        this.deadpan = 0;
    }

    /**
     * Gets the amount of prospero in the cell.
     *
     * @return the amount of prospero in the cell.
     */
    public double getProspero() {
        return prospero;
    }

    /**
     * Sets the amount of prospero for the cell.
     *
     * @param prospero the amount of prospero in the cell.
     */
    public void setProspero(double prospero) {
        this.prospero = prospero;
    }

    /**
     * Gets the amount of deadpan in the cell.
     *
     * @return the amount of deadpan in the cell.
     */
    public double getDeadpan() {
        return deadpan;
    }

    /**
     * Sets the amount of deadpan for the cell.
     *
     * @param deadpan the amount of deadpan in the cell.
     */
    public void setDeadpan(double deadpan) {
        this.deadpan = deadpan;
    }
}
