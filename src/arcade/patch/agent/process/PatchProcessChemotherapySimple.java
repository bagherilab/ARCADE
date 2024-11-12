package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchProcessChemotherapy} for simple chemotherapy.
 *
 * <p>{@code PatchProcessChemotherapySimple} assumes a constant drug uptake rate and a threshold for
 * apoptosis.
 */
public class PatchProcessChemotherapySimple extends PatchProcessChemotherapy {
    /**
     * Creates a simple chemotherapy {@code Process} for the given {@link PatchCell}.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code CONSTANT_DRUG_UPTAKE_RATE} = constant drug uptake rate
     *   <li>{@code KILL_THRESHOLD} = drug kill threshold concentration
     *   <li>{@code KILL_RATE} = constant kill rate
     * </ul>
     *
     * @param cell the {@link PatchCell} the process is associated with
     */
    public PatchProcessChemotherapySimple(PatchCell cell) {
        super(cell);
    }

    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double drugInt = intAmt;
        double drugExt = extAmt;

        // Calculate drug uptake rate based on concentration gradient.
        double area = location.getArea() * f;
        double surfaceArea = area * 2 + (volume / area) * location.getPerimeter(f);
        double drugGrad = (extAmt / location.getVolume()) - (drugInt / volume);
        drugGrad *= drugGrad < 1E-10 ? 0 : 1;
        double drugUptake = drugUptakeRate * drugGrad * surfaceArea;
        drugInt += drugUptake;

        // If drug concentration exceeds kill threshold kill cells with probability based on drug
        // concentration.
        if (cell.getState() == State.PROLIFERATIVE && drugInt > chemotherapyThreshold) {
            double oxygen = sim.getLattice("OXYGEN").getAverageValue(location);
            double p = Math.pow(oxygen, 2) / (Math.pow(oxygen, 2) + Math.pow(drugInt, 2));

            // TODO: Update probability
            if (random.nextDouble() < p) {
                cell.setState(State.APOPTOTIC);
                wasChemo = true;
            }
        }
        intAmt = Math.exp(-drugRemovalRate) * drugInt;
        // intAmt = drugInt;
        uptakeAmt = drugUptake;
    }

    @Override
    public void update(Process process) {
        PatchProcessChemotherapySimple chemotherapy = (PatchProcessChemotherapySimple) process;
        double split = this.cell.getVolume() / this.volume;

        // Update this process as split of given process.
        this.volume = this.cell.getVolume();
        this.intAmt = chemotherapy.intAmt * split;

        chemotherapy.volume = chemotherapy.cell.getVolume();
        chemotherapy.intAmt *= (1 - split);
    }
}
