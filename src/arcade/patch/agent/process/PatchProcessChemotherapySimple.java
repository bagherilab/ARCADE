package arcade.patch.agent.process;

import ec.util.MersenneTwisterFast;
import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import static arcade.patch.util.PatchEnums.Flag;
import static arcade.patch.util.PatchEnums.State;

/**
 * Extension of {@link PatchProcessChemotherapy} for simple chemotherapy.
 * <p>
 * {@code PatchProcessChemotherapySimple} assumes a constant drug uptake rate
 * and a threshold for apoptosis.
 */

public class PatchProcessChemotherapySimple extends PatchProcessChemotherapy {
    /** Constant drug uptake rate [TODO: Rate] for the drug. */
    private final double drugUptakeRate;
    
    /** Kill threshold concentration [TODO: Concentration] for the drug. */
    private final double killThreshold;
    
    /** Constant kill rate [cells/min]. */
    private final double killRate;

    /**
     * Creates a simple chemotherapy {@code Process} for the given {@link PatchCell}.
     * <p>
     * Loaded parameters include:
     * <ul>
     *     <li>{@code CONSTANT_DRUG_UPTAKE_RATE} = constant drug uptake rate</li>
     *     <li>{@code KILL_THRESHOLD} = drug kill threshold concentration</li>
     *     <li>{@code KILL_RATE} = constant kill rate</li>
     * </ul>
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    public PatchProcessChemotherapySimple(PatchCell cell) {
        super(cell);
        
        // Load parameters from the MiniBox.
        MiniBox parameters = cell.getParameters();
        drugUptakeRate = parameters.getDouble("chemotherapy/CONSTANT_DRUG_UPTAKE_RATE");
        killThreshold = parameters.getDouble("chemotherapy/KILL_THRESHOLD");
        killRate = parameters.getDouble("chemotherapy/KILL_RATE");
    }
    
    @Override
    public void stepProcess(MersenneTwisterFast random, Simulation sim) {
        double drugInt = intAmt;
        double drugExt = extAmt;

        // Calculate drug uptake rate based on concentration gradient.
        double drugGrad = (extAmt / location.getVolume()) - (drugInt / volume);
        drugGrad *= drugGrad < 1E-10 ? 0 : 1;
        double drugUptake = drugUptakeRate * drugGrad;
        drugInt += drugUptake;

        // If drug concentration exceeds kill threshold, apply kill rate.
        if (cell.getState() == State.PROLIFERATIVE && drugInt > killThreshold) {
            if (random.nextDouble() < killRate) {
                cell.setState(State.APOPTOTIC);
            }
        }

        intAmt = drugInt;
        uptakeAmt = drugUptake;
    }
    
    @Override
    public void update(Process process) {
        PatchProcessChemotherapySimple chemotherapy = (PatchProcessChemotherapySimple) process;
        double split = this.cell.getVolume() / this.volume;
        
        // Update this process as split of given process.
        this.volume = this.cell.getVolume();
        this.intAmt = chemotherapy.intAmt * split;

        chemotherapy.intAmt *= (1 - split);

    }
}