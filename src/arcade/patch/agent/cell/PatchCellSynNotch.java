package arcade.patch.agent.cell;

import sim.engine.SimState;
import sim.util.Bag;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.env.location.Location;
import arcade.core.sim.Simulation;
import arcade.core.util.GrabBag;
import arcade.core.util.Parameters;
import arcade.patch.env.grid.PatchGrid;

public class PatchCellSynNotch extends PatchCellCARTCD8 {
    protected final double synNotchThreshold;
    protected final double bindingConstant;
    protected final double unbindingConstant;
    protected final double carDegradationConstant;
    public final int synnotchs;
    public int boundSynNotch;
    protected int maxCars;
    PoissonFactory poissonFactory;
    private PatchCellTissue boundCell;

    /**
     * Creates a tissue {@code PatchCellSynNotch} agent. *
     *
     * @param location the {@link Location} of the cell
     * @param parameters the dictionary of parameters
     */
    public PatchCellSynNotch(
            PatchCellContainer container, Location location, Parameters parameters) {
        this(container, location, parameters, null);
    }

    public PatchCellSynNotch(
            PatchCellContainer container, Location location, Parameters parameters, GrabBag links) {
        super(container, location, parameters, links);
        bindingConstant = parameters.getDouble("K_SYNNOTCH_ON");
        unbindingConstant = parameters.getDouble("K_SYNNOTCH_OFF");
        carDegradationConstant = parameters.getDouble("K_CAR_DEGRADE");
        synnotchs = parameters.getInt("SYNNOTCHS");
        synNotchThreshold = parameters.getDouble("SYNNOTCH_TRESHOLD") * synnotchs;

        boundCell = null;
        boundSynNotch = 0;
        maxCars = cars;
        cars = 0;
        poissonFactory = Poisson::new;
    }

    public double callPoisson(double lambda, MersenneTwisterFast random) {
        return poissonFactory.createPoisson(lambda, random).nextInt();
    }

    @Override
    public void step(SimState simstate) {
        if (boundCell == null) {
            checkForBinding(simstate);
        } else {
            calculateCARS(simstate.random);
        }
        super.step(simstate);
    }

    private void calculateCARS(MersenneTwisterFast random) {
        int TAU = 60;
        int unboundSynNotch = synnotchs - boundSynNotch;
        double expectedBindingEvents =
                bindingConstant
                        / (volume * 6.0221415e23 * 1e-15)
                        * unboundSynNotch
                        * boundCell.getSynNotchAntigens()
                        * contactFraction
                        * TAU;
        int bindingEvents = poissonFactory.createPoisson(expectedBindingEvents, random).nextInt();
        double expectedUnbindingEvents = unbindingConstant * boundSynNotch * TAU;
        int unbindingEvents =
                poissonFactory.createPoisson(expectedUnbindingEvents, random).nextInt();

        boundSynNotch += bindingEvents;
        boundSynNotch -= unbindingEvents;
        boundCell.updateSynNotchAntigens(unbindingEvents, bindingEvents);

        double n = 4.4;
        int new_cars =
                (int) (maxCars / (1 + Math.pow(synNotchThreshold, n) / Math.pow(boundSynNotch, n)));

        cars = Math.max((int) (cars - (carDegradationConstant * cars * TAU)), new_cars);
    }

    /** A {@code PoissonFactory} object instantiates Poisson distributions. */
    interface PoissonFactory {
        /**
         * Creates instance of Poisson.
         *
         * @param lambda the Poisson distribution lambda
         * @param random the random number generator
         * @return a Poisson distribution instance
         */
        Poisson createPoisson(double lambda, MersenneTwisterFast random);
    }

    private void checkForBinding(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        PatchGrid grid = (PatchGrid) sim.getGrid();

        Bag allAgents = new Bag();
        getTissueAgents(allAgents, grid.getObjectsAtLocation(location));

        if (allAgents.size() > 0) {
            PatchCellTissue randomCell =
                    (PatchCellTissue) allAgents.get(simstate.random.nextInt(allAgents.size()));
            if (randomCell.getSynNotchAntigens() > 0) {
                boundCell = randomCell;
            }
        }
    }

    public void resetBoundCell() {
        if (boundCell != null) {
            return;
        }
        boundCell.updateSynNotchAntigens(boundSynNotch, 0);
        boundSynNotch = 0;
        boundCell = null;
    }
}
