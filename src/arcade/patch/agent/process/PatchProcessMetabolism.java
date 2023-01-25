package arcade.patch.agent.process;

import java.util.List;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;
import static arcade.core.util.Enums.State;

/**
 * Implementation of {@link Process} for cell metabolism.
 * <p>
 * The {@code PatchProcessMetabolism} process:
 * <ul>
 *     <li>gets available glucose and oxygen from the environment</li>
 *     <li>calculates energy consumption (ATP) given cell size and state</li>
 *     <li>steps the metabolism process (implemented with different complexities)
 *     to determine changes to energy and volume</li>
 *     <li>updates glucose and oxygen environment with consumption</li>
 * </ul>
 * <p>
 * All metabolism complexities use both the glycolysis and oxidative
 * phosphorylation pathways to generate energy.
 * Several stoichiometric ratios for glucose, oxygen, and pyruvate in glycolysis
 * and oxidative phosphorylation are provided.
 * Glucose and oxygen are numbered constants, codes may be added for additional
 * molecules.
 */

public abstract class PatchProcessMetabolism extends PatchProcess {
    /** ID for glucose. */
    static final int GLUCOSE = 0;
    
    /** ID for oxygen. */
    static final int OXYGEN = 1;
    
    /** Energy from glycolysis  [mol ATP/mol glucose]. */
    static final int ENERGY_FROM_GLYC = 2;
    
    /** Energy from oxidative phosphorylation [mol ATP/mol pyruvate]. */
    static final int ENERGY_FROM_OXPHOS = 15;
    
    /** Stoichiometric ratio for oxygen:pyruvate [mol oxygen/mol pyruvate]. */
    static final int OXY_PER_PYRU = 3;
    
    /** Stoichiometric ratio for pyruvate:glucose [mol pyruvate/mol glucose]. */
    static final int PYRU_PER_GLUC = 2;
    
    /** Basal energy requirement [fmol ATP/um<sup>3</sup> cell/min]. */
    private final double basalEnergy;
    
    /** Additional energy required for proliferation [fmol ATP/um<sup>3</sup> cell/min]. */
    private final double proliferationEnergy;
    
    /** Additional energy required for migration [fmol ATP/um<sup>3</sup> cell/min]. */
    private final double migrationEnergy;
    
    /** Oxygen solubility in tissue [fmol O2/um<sup>3</sup>/mmHg]. */
    private final double oxygenSolubilityTissue;
    
    /** Cell density [ng/um<sup>3</sup>]. */
    final double cellDensity;
    
    /** Ratio of glucose to biomass [fmol glucose/ng biomass]. */
    final double ratioGlucoseBiomass;
    
    /** List of internal names. */
    List<String> names;
    
    /** List of internal amounts [fmol]. */
    double[] intAmts;
    
    /** List of external amounts [fmol]. */
    final double[] extAmts;
    
    /** List of uptake amounts [fmol]. */
    final double[] upAmts;
    
    /** Volume fraction. */
    protected double f;
    
    /** Cell energy [ATP]. */
    double energy;
    
    /** Volume of cell [um<sup>3</sup>]. */
    double volume;
    
    /** Dry mass of cell [ng]. */
    double mass;
    
    /** Critical cell mass. */
    final double critMass;
    
    /** Energy consumed. */
    double energyCons;
    
    /** Energy required. */
    double energyReq;
    
    /** {@code true} if cell is in proliferative state, {@code false} otherwise. */
    protected boolean isProliferative;
    
    /** {@code true} if cell is in migratory state, {@code false} otherwise. */
    protected boolean isMigratory;
    
    /**
     * Creates a metabolism {@link PatchProcess} for the given cell.
     * <p>
     * Process parameters are specific for the cell population. Loaded
     * parameters include:
     * <ul>
     *     <li>{@code BASAL_ENERGY} = basal energy requirement</li>
     *     <li>{@code PROLIFERATION_ENERGY} = additional energy required for proliferation</li>
     *     <li>{@code MIGRATION_ENERGY} = additional energy required for migration</li>
     *     <li>{@code CELL_DENSITY} = cell density</li>
     *     <li>{@code RATIO_GLUCOSE_BIOMASS} = ratio of glucose to biomass</li>
     *     <li>{@code OXYGEN_SOLUBILITY_TISSUE} = oxygen solubility in tissue</li>
     * </ul>
     * <p>
     * The process starts with energy at zero and assumes a constant ratio
     * between mass and volume (through density).
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    PatchProcessMetabolism(PatchCell cell) {
        super(cell);
        
        // Set parameters.
        MiniBox parameters = cell.getParameters();
        basalEnergy = parameters.getDouble("metabolism/BASAL_ENERGY");
        proliferationEnergy = parameters.getDouble("metabolism/PROLIFERATION_ENERGY");
        migrationEnergy = parameters.getDouble("metabolism/MIGRATION_ENERGY");
        cellDensity = parameters.getDouble("metabolism/CELL_DENSITY");
        ratioGlucoseBiomass = parameters.getDouble("metabolism/RATIO_GLUCOSE_BIOMASS");
        oxygenSolubilityTissue = parameters.getDouble("metabolism/OXYGEN_SOLUBILITY_TISSUE");
        
        // Initialize process.
        volume = cell.getVolume();
        energy = 0;
        mass = cell.getVolume() * cellDensity;
        critMass = cell.getCriticalVolume() * cellDensity;
        
        // Initialize external and uptake concentration arrays;
        extAmts = new double[2];
        upAmts = new double[2];
    }
    
    /**
     * Steps the metabolism process.
     *
     * @param random  the random number generator
     * @param sim  the simulation instance
     */
    abstract void stepProcess(MersenneTwisterFast random, Simulation sim);
    
    /**
     * Gets the external amounts of glucose and oxygen in fmol.
     *
     * @param sim  the simulation instance
     */
    private void updateExternal(Simulation sim) {
        extAmts[GLUCOSE] = sim.getLattice("GLUCOSE").getAverageValue(location)
                * location.getVolume();
        extAmts[OXYGEN] = sim.getLattice("OXYGEN").getAverageValue(location)
                * location.getVolume() * oxygenSolubilityTissue;
    }
    
    @Override
    public void step(MersenneTwisterFast random, Simulation sim) {
        // Calculate fraction of volume occupied by cell.
        Bag bag = ((PatchGrid) sim.getGrid()).getObjectsAtLocation(location);
        double totalVolume = PatchCell.calculateTotalVolume(bag);
        f = volume / totalVolume;
        
        updateExternal(sim);
        
        // Check cell state.
        isProliferative = cell.getState() == State.PROLIFERATIVE;
        isMigratory = cell.getState() == State.MIGRATORY;
        
        // Calculate energy consumption.
        energyCons = volume * (basalEnergy
                + (isProliferative ? proliferationEnergy : 0)
                + (isMigratory ? migrationEnergy : 0));
        energyReq = energyCons - energy;
        
        // Modify energy and volume.
        stepProcess(random, sim);
        
        // Update environment.
        sim.getLattice("GLUCOSE").updateValue(location, 1.0 - upAmts[GLUCOSE] / extAmts[GLUCOSE]);
        sim.getLattice("OXYGEN").updateValue(location, 1.0 - upAmts[OXYGEN] / extAmts[OXYGEN]);
        
        // Update cell agent.
        cell.setVolume(volume);
        cell.setEnergy(energy);
    }
    
    /**
     * Creates a {@code PatchProcessMetabolism} for given version.
     *
     * @param cell  the {@link PatchCell} the process is associated with
     * @param version  the process version
     * @return  the process instance
     */
    public static PatchProcess make(PatchCell cell, String version) {
        switch (version.toUpperCase()) {
            case "RANDOM":
                return new PatchProcessMetabolismRandom(cell);
            case "SIMPLE":
                return new PatchProcessMetabolismSimple(cell);
            case "MEDIUM":
                return new PatchProcessMetabolismMedium(cell);
            case "COMPLEX":
                return new PatchProcessMetabolismComplex(cell);
            default:
                return null;
        }
    }
}
