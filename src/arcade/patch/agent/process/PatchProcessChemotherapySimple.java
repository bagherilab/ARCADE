package abm.agent.module;

import abm.sim.*;
import abm.env.loc.Location;
import abm.env.lat.Lattice;
import abm.util.MiniBox;

public class SimpleChemotherapy extends Chemotherapy {

    public SimpleChemotherapy(PatchCell cell, Simulation sim) {
        super(cell, sim);
    }

    @Override
    protected double calculateUptake(MiniBox drug, Lattice lat, Location loc, double surfaceArea, int index, Simulation sim) {
        double external = lat.getAverageVal(loc) * loc.getVolume();
        double gradient = (external / loc.getVolume()) - (internal[index] / cell.getVolume());
        gradient *= gradient < 1E-10 ? 0 : 1;
        return drug.getDouble("UPTAKE") * surfaceArea * gradient;
    }

    @Override
    protected boolean shouldApoptose(int drugIndex, Simulation sim) {
        return internal[drugIndex] > CHEMOTHERAPY_THRESHOLD;
    }
}