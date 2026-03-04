package arcade.potts.agent.cell;

import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;

public abstract class PottsCellFly extends PottsCell {
    private double prospero;

    public PottsCellFly(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        this.prospero = 0;
    }

    public double getProspero() {return prospero;}

    public void setProspero(double prospero) {this.prospero = prospero;}


}
