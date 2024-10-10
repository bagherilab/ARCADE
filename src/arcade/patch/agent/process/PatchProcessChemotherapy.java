package abm.agent.module;

import java.util.ArrayList;
import abm.sim.*;
import abm.agent.cell.*;
import abm.env.loc.Location;
import abm.env.lat.Lattice;
import abm.util.MiniBox;
import sim.util.Bag;

public abstract class Chemotherapy extends PatchProcess {
    protected final double CHEMOTHERAPY_THRESHOLD;
    protected ArrayList<MiniBox> drugs;
    protected double[] internal;
    protected int n;
    protected boolean wasChemo;

    /** Cell population index */
    protected final int pop;

    public Chemotherapy(PatchCell cell, Simulation sim) {
        super(cell);

		// Set parameters.
        Series series = sim.getSeries();
        this.pop = cell.getPop();
        this.drugs = ((TreatmentSimulation)sim).drugs;
        this.CHEMOTHERAPY_THRESHOLD = series.getParam(pop, "CHEMOTHERAPY_THRESHOLD");

        // Initialize internal concentration array.
        n = drugs.size();
        internal = new double[n];
    }

    @Override
    public void stepProcess(Simulation sim) {
        Location loc = cell.getLocation();
        
        // Calculate relative surface area of cell.
        Bag bag = sim.getAgents().getObjectsAtLocation(loc);
        double f = cell.getVolume() / PatchCell.calcTotalVolume(bag);
        double area = loc.getArea() * f;
        double surfaceArea = area * 2 + (cell.getVolume() / area) * loc.calcPerimeter(f);

        for (int i = 0; i < n; i++) {
            MiniBox drug = drugs.get(i);
            Lattice lat = sim.getEnvironment(drug.get("id"));

            // Abstracted uptake logic to be implemented by subclasses
            double uptake = calculateUptake(drug, lat, loc, surfaceArea, i, sim);
            internal[i] += uptake;

            // Update environment.
            lat.updateVal(loc, 1.0 - uptake / lat.getAverageVal(loc));

            // Abstracted apoptosis logic to be implemented by subclasses
            if (shouldApoptose(i, sim)) {
                cell.apoptose(sim);
                wasChemo = true;
            }

            // Drug removal (decay of concentration)
            internal[i] = Math.exp(-drug.getDouble("REMOVAL")) * internal[i];
        }

        // Record chemotherapy event if apoptosis happened due to chemotherapy
        logEvent(sim);
    }

    @Override
    public void updateProcess(PatchProcess mod, double f) {
        Chemotherapy chemotherapy = (Chemotherapy) mod;
        for (int i = 0; i < n; i++) {
            this.internal[i] = chemotherapy.internal[i] * f;
            chemotherapy.internal[i] *= (1 - f);
        }
    }

    public double getInternal(String key) {
        for (int i = 0; i < n; i++) {
            if (drugs.get(i).get("id").equals(key)) {
                return internal[i];
            }
        }
        return Double.NaN;
    }

    @Override
    public String toJSON() {
        StringBuilder concs = new StringBuilder();
        for (int i = 0; i < n; i++) {
            concs.append(internal[i]).append(",");
        }
        return "[" + concs.toString().replaceFirst(",$", "") + "]";
    }

    // Abstract methods to be implemented by subclasses
    protected abstract double calculateUptake(MiniBox drug, Lattice lat, Location loc, double surfaceArea, int index, Simulation sim);

    protected abstract boolean shouldApoptose(int drugIndex, Simulation sim);

    protected void logEvent(Simulation sim) {
        if (sim instanceof TreatmentSimulation && wasChemo) {
            StringBuilder cloc = new StringBuilder();
            for (int i : cell.getLocation().getGridLocation()) {
                cloc.append(i).append(",");
            }
            cloc.append(cell.getLocation().getGridZ());
            ((TreatmentSimulation) sim).addEvent("*chemotherapy", pop + ",[" + cloc.toString() + "]");
        }
    }
}