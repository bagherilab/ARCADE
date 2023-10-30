package arcade.patch.agent.cell;

import sim.util.Bag;
import ec.util.MersenneTwisterFast;
import javafx.stage.PopupWindow.AnchorLocation;
import arcade.core.agent.cell.Cell;
import arcade.core.agent.cell.CellState;
import arcade.core.env.location.Location;
import arcade.core.util.MiniBox;
import arcade.patch.env.grid.PatchGrid;
import arcade.patch.env.location.PatchLocation;
import arcade.core.sim.Simulation;
import static arcade.patch.util.PatchEnums.State;

import static arcade.patch.util.PatchEnums.AntigenFlag;

/**
 * Implementation of {@link Cell} for generic CART cell.
 * 
 * <p>
 * {@code PatchCellCART} agents exist in one of thirteen states: neutral, apoptotic,
 * migratory, proliferative, senescent, cytotoxic (CD8), stimulatory (CD4),
 * exhausted, anergic, starved, or paused.
 * The neutral state is an transition state for "undecided" cells, and does not
 * have any biological analog.
 * <p>
 * 
 * {@code PatchCellCART} agents have two required {@link Process} domains:
 * metabolism and inflammation. Metabolism controls changes in cell energy and
 * volume. Inflammation controls effector functions.
 * <p>
 * 
 * General order of rules for the {@code PatchCellCART} step:
 * <ul>
 *     <li>update age</li>
 *     <li>check lifespan (possible change to apoptotic)</li>
 *     <li>step metabolism module</li>
 *     <li>check energy status (possible change to starved, apoptotic)</li>
 *     <li>step inflammation module</li>
 *     <li>check if neutral or paused (change to proliferative, migratory, senescent, 
 *     		cytotoxic, stimulatory, exhausted, anergic)</li>
 * </ul>
 * 
 * <p>
 * Cells that become senescent, exhausted, anergic, or proliferative have a change to become apoptotic
 * instead  {@code SENESCENT_FRACTION}, ({@code EXHAUSTED_FRACTION}, ({@code ANERGIC_FRACTION}, and ({@code PROLIFERATIVE_FRACTION},
 * respectively).
 * <p>
 * 
 * Cell parameters are tracked using a map between the parameter name and value.
 * Daughter cell parameter values are drawn from a distribution centered on the
 * parent cell parameter with the specified amount of heterogeneity
 * ({@code HETEROGENEITY}).
 */

public abstract class PatchCellCART extends PatchCell {

     /** Fraction of exhausted cells that become apoptotic. */
     protected final double exhaustedFraction;
    
     /** Fraction of senescent cells that become apoptotic. */
     protected final double senescentFraction;

     /** Fraction of anergic cells that become apoptotic. */
     protected final double anergicFraction;

      /** Fraction of proliferative cells that become apoptotic. */
     protected final double proliferativeFraction;
 
     /** Cell surface antigen count */
     protected int carAntigens;
     
     /** Cell surface PDL1 count */
     protected final int selfTargets;

     /** Cell binding flag */
     protected AntigenFlag binding;

     /** Cell activation flag */
     protected boolean activated;

    /** lack of documentation so these parameters are TBD */
     protected int selfReceptors;
     protected int selfReceptorsStart;
     protected int boundAntigensCount;
     protected int boundSelfCount;

     /** lack of documentation so these loaded parameters are TBD */
     protected final double searchAbility;
     protected final double carAffinity;
     protected final double carAlpha;
     protected final double carBeta;
     protected final double selfReceptorAffinity;
     protected final double selfAlpha;
     protected final double selfBeta;
     protected final double contactFraction;
     protected final int maxAntigenBinding;
     protected final int cars;
     protected int lastActiveTicker;

     /**
      * Creates a tissue {@code PatchCellCART} agent.
      * * <p>
      * Loaded parameters include:
      * <ul>
      *     <li>{@code EXHAUSTED_FRACTION} = fraction of exhausted cells that
      *         become apoptotic</li>
      *     <li>{@code SENESCENT_FRACTION} = fraction of senescent cells that
      *         become apoptotic</li>
      *     <li>{@code ANERGIC_FRACTION} = fraction of anergic cells that
      *         become apoptotic</li>
      *     <li>{@code PROLIFERATIVE_FRACTION} = fraction of proliferative cells that
      *         become apoptotic</li>
      *     <li>{@code CAR_ANTIGENS} = Cell surface antigen count </li>
      *     <li>{@code SELF_TARGETS} = Cell surface PDL1 count </li>
      *
      *     <li>{@code SEARCH_ABILITY} = TBD </li>
      *     <li>{@code CAR_AFFINITY} = TBD </li>
      *     <li>{@code CAR_ALPHA} = TBD </li>
      *     <li>{@code CAR_BETA} = TBD </li>
      *     <li>{@code SELF_RECEPTOR_AFFINITY} = TBD </li>
      *     <li>{@code SELF_ALPHA} = TBD </li>
      *     <li>{@code SELF_BETA} = TBD </li>
      *     <li>{@code CONTACT_FRACTION} = TBD </li>
      *     <li>{@code MAX_ANTIGEN_BINDING} = TBD </li>
      *     <li>{@code CARS} = TBD </li>
      *     <li>{@code SELF_RECEPTORS} = TBD </li>
      *     <li>{@code SELF_RECEPTORS_START} = TBD </li>
      * </ul>
      * < THERE IS A LACK OF DOCUMENTATION FOR THE REST OF THE LOADED PARAMS SO THEY ARE TBD>
      *
      * @param id  the cell ID
      * @param parent  the parent ID
      * @param pop  the cell population index
      * @param state  the cell state
      * @param age  the cell age
      * @param divisions  the number of cell divisions
      * @param location  the {@link Location} of the cell
      * @param parameters  the dictionary of parameters
      * @param volume  the cell volume
      * @param height  the cell height
      * @param criticalVolume  the critical cell volume
      * @param criticalHeight  the critical cell height
      */
     public PatchCellCART(int id, int parent, int pop, CellState state, int age, int divisions,
                            Location location, MiniBox parameters, double volume, double height,
                            double criticalVolume, double criticalHeight) {
         super(id, parent, pop, state, age, divisions, location, parameters,
                 volume, height, criticalVolume, criticalHeight);
         
         //initialized non-loaded parameters
         boundAntigensCount = 0;
         boundSelfCount = 0;
         lastActiveTicker = 0;
         binding = AntigenFlag.UNDEFINED;
         activated = true;

         // Set loaded parameters.
         exhaustedFraction = parameters.getDouble(  "EXHAUSTED_FRACTION");
         senescentFraction = parameters.getDouble("SENESCENT_FRACTION");
         anergicFraction = parameters.getDouble("ANERGIC_FRACTION");
         proliferativeFraction = parameters.getDouble("PROLIFERATIVE_FRACTION");
         carAntigens = parameters.getInt("CAR_ANTIGENS");
         selfTargets = parameters.getInt("SELF_TARGETS");
         selfReceptors = parameters.getInt("SELF_RECEPTORS");
         selfReceptorsStart = parameters.getInt("SELF_RECEPTORS_START");
         searchAbility = parameters.getDouble("SEARCH_ABILITY");
         carAffinity = parameters.getDouble("CAR_AFFINITY");
         carAlpha = parameters.getDouble("CAR_ALPHA");
         carBeta = parameters.getDouble("CAR_BETA");
         selfReceptorAffinity = parameters.getDouble("SELF_RECEPTOR_AFFINITY");
         selfAlpha = parameters.getDouble("SELF_ALPHA");
         selfBeta = parameters.getDouble("SELF_BETA");
         contactFraction = parameters.getDouble("CONTACT_FRACTION");
         maxAntigenBinding = parameters.getInt("MAX_ANTIGEN_BINDING");
         cars = parameters.getInt("CARS");
     }

    /* need to implement bindTarget equivalent here*/
    /**
	 * Determines if CAR T cell agent is bound to neighbor through receptor-target binding.
	 * <p>
	 * Searches the number of allowed neighbors in series, calculates bound probability to antigen
	 * and self receptors, compares values to random variable. Sets flags accordingly
	 * and returns a target cell if one was bound by antigen or self receptor.
	 * 
	 * @param state  the MASON simulation state
	 * @param loc  the location of the CAR T-cell
     * @param random  random seed
	 */

    public void bindTarget(Simulation sim, PatchLocation loc, MersenneTwisterFast random) {
        double KDCAR = carAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
		double KDSelf = selfReceptorAffinity * (loc.getVolume() * 1e-15 * 6.022E23);
        PatchGrid grid = (PatchGrid) sim.getGrid();

        //get all agents from this location 
        Bag allAgents = new Bag(grid.getObjectsAtLocation(loc));

        //get all agents from neighboring locations
        for (Location neighborLocation : loc.getNeighbors()) {
            Bag bag = new Bag(grid.getObjectsAtLocation(neighborLocation));
            for (Object b : bag) {
                //add all agents from neighboring locations 
                if (!allAgents.contains(b)) allAgents.add(b);
            }
        } 

        //remove self
        allAgents.remove(this);

        //shuffle bag
        allAgents.shuffle(random);

        //get number of neighbors
        int neighbors = allAgents.size();

        // Bind target with some probability if a nearby cell has targets to bind.
        int maxSearch = 0;
		if (neighbors == 0) {
			binding = AntigenFlag.UNBINDED;
		} else {
            if (neighbors < searchAbility) {
                maxSearch = neighbors;
            } else {
                maxSearch = (int) searchAbility;
            }
        }

        // Within maximum search vicinity, search for neighboring cells to bind to 
        for (int i = 0; i < maxSearch; i++) {
            Cell cell = (Cell) allAgents.get(i);
            if (!(cell instanceof PatchCellCART) && cell.getState() != State.APOPTOTIC && cell.getState() != State.NECROTIC) {
                PatchCellTissue tissueCell = (PatchCellTissue) cell;
				double CARAntigens = tissueCell.carAntigens;
				double selfTargets = tissueCell.selfTargets;

                double hillCAR = (CARAntigens*contactFraction/ (KDCAR*carBeta + CARAntigens*contactFraction))*(cars/50000)*carAlpha;
				double hillSelf = (selfTargets*contactFraction / (KDSelf*selfBeta + selfTargets*contactFraction))*(selfReceptors/selfReceptorsStart)*selfAlpha;

                double logCAR = 2*(1/(1 + Math.exp(-1*hillCAR))) - 1;
				double logSelf = 2*(1/(1 + Math.exp(-1*hillSelf))) - 1;

                double randomAntigen = random.nextDouble();
				double randomSelf = random.nextDouble();

                if (logCAR >= randomAntigen && logSelf < randomSelf ) {
                    // cell binds to antigen receptor
                    binding = AntigenFlag.BOUND_ANTIGEN;
                    boundAntigensCount++;
                    selfReceptors += (int)((double)selfReceptorsStart * (0.95 + random.nextDouble()/10));
                } else if ( logCAR >= randomAntigen && logSelf >= randomSelf ) {
                    // cell binds to antigen receptor and self
                    binding = AntigenFlag.BOUND_ANTIGEN_CELL_RECEPTOR;
                    boundAntigensCount++;
                    boundSelfCount++;
                    selfReceptors += (int)((double)selfReceptorsStart * (0.95 + random.nextDouble()/10));
                } else if ( logCAR < randomAntigen && logSelf >= randomSelf ) {
                    // cell binds to self
                    binding = AntigenFlag.BOUND_CELL_RECEPTOR;
                    boundSelfCount++;
                } else { 
                    // cell doesn't bind to anything
                    binding = AntigenFlag.UNBINDED;
                }
            }
        }
    }
    
    //this method may be unnecessary if only subclasses can set antigen flag
    //can non-T cells set the antigen binding properties of CART cell?

    /**
     * Sets the cell binding flag.
     *
     * @param antigenFlag  the target cell antigen binding state
     */
    public void setAntigenFlag(AntigenFlag flag) { this.binding = flag; }
    
}
