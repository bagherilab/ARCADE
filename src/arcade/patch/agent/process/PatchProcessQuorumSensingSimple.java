package arcade.patch.agent.process;

import java.util.Arrays;

import arcade.core.agent.process.Process;
import arcade.core.sim.Simulation;
import arcade.core.util.Parameters;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.agent.cell.PatchCellSynNotch;
import ec.util.MersenneTwisterFast;


public class PatchProcessQuorumSensingSimple extends PatchProcessQuorumSensing {

    final protected double auxinProductionRate;
    final protected double auxinDegradationRate;
    final protected double auxinUptakeRate;

      public PatchProcessQuorumSensingSimple(PatchCell cell) {
        super(cell);

        // Mapping for internal concentration access.
        String[] intNames = new String[1];
        intNames[AUXIN] = "auxin";
        names = Arrays.asList(intNames);

        // Set loaded parameters.
        Parameters parameters = cell.getParameters();
        auxinProductionRate = parameters.getDouble("quorum/AUXIN_PRODUCTION_RATE"); // units in fmol auxin/um^3/min/M receptors
        auxinDegradationRate = parameters.getDouble("quorum/AUXIN_DEGRADATION_RATE"); // units in fmol auxin/um^3/min/M receptors
        auxinUptakeRate = parameters.getDouble("quorum/CONSTANT_AUXIN_UPTAKE_RATE"); // units in fmol auxin/um^3/min/M receptors

        // Initial internal concentrations.
        intAmts = new double[1];
        intAmts[AUXIN] =
                parameters.getDouble("quorum/INITIAL_AUXIN_CONCENTRATION") * volume;
    }

    @Override
    void stepProcess(MersenneTwisterFast random, Simulation sim) {
        PatchCellSynNotch cell = (PatchCellSynNotch) this.cell;
        double auxinInt = intAmts[AUXIN]; // [fmol]
        double auxinExt = extAmts[AUXIN]; // [fmol]
        double auxGrad = (auxinExt / location.getVolume()) - (auxinInt / volume);
        auxGrad  *= auxGrad  < 1E-10 ? 0 : 1;
        double auxUptake = auxinUptakeRate * auxGrad;
        auxinInt += auxUptake;
        double totalReceptors = (cell.boundAntigensCount - cell.synNotchAntigensBound) / (6.022E23 * cell.getVolume());
        double synNotchReceptors = (cell.synNotchAntigensBound) / (6.022E23 * cell.getVolume());
        double auxinExpressed = auxinProductionRate * totalReceptors - auxinDegradationRate * synNotchReceptors;
        double auxinSecreted = auxinExpressed - auxUptake;
        if (auxUptake > auxinExpressed) {
            auxUptake = auxinExpressed;
            auxinSecreted = 0;
        }
        sim.getLattice("AUXIN").setValue(location, auxinSecreted);
    }

    @Override
    public void update(Process process) {
        PatchProcessQuorumSensingSimple quorum = (PatchProcessQuorumSensingSimple) process;
        double split = (this.cell.getVolume() / this.volume);
        this.intAmts[AUXIN] = quorum.intAmts[AUXIN] * split;
        quorum.intAmts[AUXIN] *= (1 - split);
    }
    
}
