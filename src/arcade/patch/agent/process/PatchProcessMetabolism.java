package arcade.patch.agent.process;

import java.util.List;
import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import arcade.core.sim.Simulation;
import arcade.patch.agent.cell.PatchCell;
import arcade.patch.env.grid.PatchGrid;
import static arcade.core.util.Enums.State;

/** 
 * Implementation of {@link Process} for cell metabolism.
 * <p>
 * The {@code PatchProcessMetabolism} process:
 * <ul>
 *     <li>gets available glucose and oxygen from the environment</li>
 *     <li>calculates energy consumption (in ATP) given cell size and state</li>
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
    /** ID for glucose */
    static final int GLUCOSE = 0;
    
    /** ID for oxygen */
    static final int OXYGEN = 1;
    
    /** Energy from glycolysis  [mol ATP/mol glucose] */
    static final int ENERGY_FROM_GLYC = 2;
    
    /** Energy from oxidative phosphorylation [mol ATP/mol pyruvate] */
    static final int ENERGY_FROM_OXPHOS = 15;
    
    /** Stoichiometric ratio between oxygen and pyruvate [mol oxygen/mol pyruvate] */
    static final int OXY_PER_PYRU = 3;
    
    /** Stoichiometric ratio between pyruvate and glucose [mol pyruvate/mol glucose] */
    static final int PYRU_PER_GLUC = 2;
    
    /** Basal energy requirement */
    private final double BASAL_ENERGY;
    
    /** Additional energy required during proliferation */
    private final double PROLIF_ENERGY;
    
    /** Additional energy required during migration */
    private final double MIGRA_ENERGY;
    
    /** Oxygen solubility in tissue */
    private final double OXY_SOLU_TISSUE;
    
    /** Cell density (in ng/um<sup>3</sup>) */
    final double CELL_DENSITY;
    
    /** Ratio of glucose to biomass (in fmol glucose/ng biomass) */
    final double MASS_TO_GLUC;
    
    /** List of internal names */
    List<String> names;
    
    /** List of internal amounts [fmol] */
    double[] intAmts;
    
    /** List of external amounts [fmol] */
    final double[] extAmts;
    
    /** List of uptake amounts [fmol] */
    final double[] upAmts;
    
    /** Volume fraction */
    protected double f;
    
    /** Cell energy [ATP] */
    double energy;
    
    /** Volume of cell [um<sup>3</sup>] */
    double volume;
    
    /** Dry mass of cell [ng] */
    double mass;
    
    /** Critical cell mass */
    final double critMass;
    
    /** Energy consumed */
    double energyCons;
    
    /** Energy required */
    double energyReq;
    
    /** {@code true} if cell is in proliferative state, {@code false} otherwise. */
    protected boolean isProliferative;
    
    /** {@code true} if cell is in migratory state, {@code false} otherwise. */
    protected boolean isMigratory;
    
    /**
     * Creates a metabolism {@link PatchProcess} for the given cell.
     * <p>
     * Process parameters are specific for the cell population.
     * The process starts with energy at zero and assumes a constant ratio
     * between mass and volume (through density).
     *
     * @param cell  the {@link PatchCell} the process is associated with
     */
    PatchProcessMetabolism(PatchCell cell) {
        super(cell);
        
        // Set parameters.
        this.BASAL_ENERGY = cell.getParameters().getDouble("metabolism/BASAL_ENERGY");
        this.PROLIF_ENERGY = cell.getParameters().getDouble("metabolism/PROLIF_ENERGY");
        this.MIGRA_ENERGY = cell.getParameters().getDouble("metabolism/MIGRA_ENERGY");
        this.CELL_DENSITY = cell.getParameters().getDouble("metabolism/CELL_DENSITY");
        this.MASS_TO_GLUC = cell.getParameters().getDouble( "metabolism/MASS_TO_GLUC");
        this.OXY_SOLU_TISSUE = cell.getParameters().getDouble("metabolism/OXY_SOLU_TISSUE");
        
        // Initialize process.
        this.volume = cell.getVolume();
        this.energy = 0;
        this.mass = cell.getVolume() * CELL_DENSITY;
        this.critMass = cell.getCriticalVolume() * CELL_DENSITY;
        
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
     * Gets the external amounts of glucose and oxygen.
     * <p>
     * Multiply by location volume to get in fmol.
     * 
     * @param sim  the simulation instance
     */
    private void updateExternal(Simulation sim) {
        extAmts[GLUCOSE] = sim.getLattice("GLUCOSE").getAverageValue(location) * location.getVolume();
        extAmts[OXYGEN] = sim.getLattice("OXYGEN").getAverageValue(location) * location.getVolume() * OXY_SOLU_TISSUE;
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
        energyCons = volume * (BASAL_ENERGY +
                (isProliferative ? PROLIF_ENERGY : 0) +
                (isMigratory ? MIGRA_ENERGY : 0));
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