package arcade.potts.agent.cell;

import arcade.core.agent.cell.CellContainer;
import arcade.core.env.location.Location;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.potts.agent.module.PottsModule;
import arcade.potts.util.PottsEnums;

import java.util.EnumMap;

public abstract class PottsCellFly extends PottsCell {
    private double prospero;

    public PottsCellFly(
            PottsCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        this.prospero = 0;
    }

    public double getProspero() { return prospero; }

    public void setProspero(double prospero) { this.prospero = prospero; }

    @Override
    public CellContainer convert() {
        PottsCellContainer container = (PottsCellContainer) super.convert();
        return new PottsCellContainer(
                container.id,
                container.parent,
                container.pop,
                container.age,
                container.divisions,
                container.state,
                container.phase,
                container.voxels,
                container.regionVoxels,
                container.criticalVolume,
                container.criticalHeight,
                container.criticalRegionVolumes,
                container.criticalRegionHeights,
                this.getProspero()
        );
    }
}
