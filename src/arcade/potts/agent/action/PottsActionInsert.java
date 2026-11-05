package arcade.potts.agent.action;

import java.util.ArrayList;
import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import arcade.core.agent.action.Action;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.core.util.Utilities;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Action} for inserting cell agents.
 *
 * <p>The action is stepped once, at tick {@code TIME_DELAY}. For each registered population, it
 * tries to insert {@code INSERT_NUMBER} new cells. Each new cell starts on one empty interior
 * voxel, chosen at random. If there are not enough empty voxels, fewer cells are inserted, and the
 * shortfall is counted as failures.
 */
public class PottsActionInsert implements Action {
    /** Logger for {@code PottsActionInsert}. */
    private static final Logger LOGGER = Logger.getLogger(PottsActionInsert.class.getName());

    /** Tick at which cells are inserted. */
    private final int timeDelay;

    /** Number of cells to insert for each registered population. */
    private final int insertNumber;

    /** Codes of the registered populations. */
    private final ArrayList<Integer> pops = new ArrayList<>();

    /** Number of cells that could not be inserted. */
    private int failures;

    /**
     * Creates an {@link Action} for inserting cell agents.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code TIME_DELAY} = tick at which cells are inserted
     *   <li>{@code INSERT_NUMBER} = number of cells to insert for each registered population
     * </ul>
     *
     * @param parameters the action parameters dictionary
     */
    public PottsActionInsert(MiniBox parameters) {
        timeDelay = parameters.getInt("TIME_DELAY");
        insertNumber = parameters.getInt("INSERT_NUMBER");
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleOnce(timeDelay, Ordering.ACTIONS.ordinal(), this);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The registered key is a population id (for example {@code MAC}).
     */
    @Override
    public void register(Simulation sim, String population) {
        pops.add(sim.getSeries().populations.get(population).getInt("CODE"));
    }

    @Override
    public void step(SimState simstate) {
        PottsSimulation sim = (PottsSimulation) simstate;
        int[][][] ids = sim.getPotts().ids; // indexed [z][x][y]

        ArrayList<Voxel> empty = new ArrayList<>();
        int zStart = ids.length == 1 ? 0 : 1;
        int zEnd = ids.length == 1 ? 0 : ids.length - 2;
        for (int z = zStart; z <= zEnd; z++) {
            for (int x = 1; x < ids[z].length - 1; x++) {
                for (int y = 1; y < ids[z][x].length - 1; y++) {
                    if (ids[z][x][y] == 0) {
                        empty.add(new Voxel(x, y, z)); // Voxel takes (x, y, z)
                    }
                }
            }
        }
        Utilities.shuffleList(empty, sim.random);

        for (int pop : pops) {
            for (int i = 0; i < insertNumber; i++) {
                if (empty.isEmpty()) {
                    LOGGER.info("no empty voxels left; inserted fewer cells than INSERT_NUMBER");
                    failures += insertNumber - i;
                    break;
                }
                ArrayList<Voxel> seed = new ArrayList<>();
                seed.add(empty.remove(0));
                if (sim.insertCell(pop, seed) == null) {
                    failures++;
                }
            }
        }
    }

    /**
     * Gets the number of cells that could not be inserted.
     *
     * <p>A cell fails when no empty voxels are left or when {@link PottsSimulation#insertCell}
     * rejects it.
     *
     * @return the number of failed insertions
     */
    public int getFailures() {
        return failures;
    }
}
