package arcade.potts.agent.action;

import java.util.ArrayList;
import java.util.List;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.action.Action;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Action} for recruiting cell agents from implicit vasculature.
 *
 * <p>Each tick, cells of each registered population are recruited from supplied sites, the voxels
 * with a positive value m in the supply mask layer (the same mask as the oxygen source). The
 * expected number per tick is the sum over sites of {@code R_BASE} m f dA dt, where dA is the voxel
 * area and f = S / ({@code K_RECRUIT} + S) for the summed value S of the {@code SIGNALS} layers at
 * the site (f = 1 if there are no signal layers). The number is drawn from a Poisson distribution,
 * and sites are chosen in proportion to their rates. Each new cell is inserted on one free voxel
 * within {@code D_RECRUIT} of its site, chosen at random. If there is no free voxel, or the
 * insertion is rejected, the attempt is counted as a failure.
 *
 * <p>The mask is read once, on the first step, and must have no operations.
 */
public class PottsActionRecruit implements Action {
    /** Recruitment rate at full supply and signal [cells/hour/mm^2]. */
    private final double rate;

    /** Ids of the signal layers. */
    private final List<String> signals;

    /** Half-saturation of the signal [layer units]. */
    private final double halfSaturation;

    /** Distance from a site that cells are inserted within [um]. */
    private final double distance;

    /** Id of the mask layer. */
    private final String mask;

    /** Codes of the registered populations. */
    private final ArrayList<Integer> pops = new ArrayList<>();

    /** Supplied sites, read on the first step. */
    private List<Voxel> sites;

    /** Mask values of the supplied sites. */
    private double[] siteMask;

    /** Voxel area [mm^2]. */
    private double area;

    /** Tick length [hours]. */
    private double tick;

    /** Number of cells recruited. */
    private int recruited;

    /** Number of recruitment attempts that failed. */
    private int failures;

    /**
     * Creates an {@link Action} for recruiting cell agents.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code R_BASE} = recruitment rate at full supply and signal [cells/hour/mm^2]
     *   <li>{@code SIGNALS} = comma-separated ids of signal layers, or {@code NONE}
     *   <li>{@code K_RECRUIT} = half-saturation of the summed signal [layer units]
     *   <li>{@code D_RECRUIT} = distance from a site that cells are inserted within [um]
     *   <li>{@code MASK} = id of the supply mask layer
     * </ul>
     *
     * @param parameters the action parameters dictionary
     */
    public PottsActionRecruit(MiniBox parameters) {
        rate = parameters.getDouble("R_BASE");
        halfSaturation = parameters.getDouble("K_RECRUIT");
        distance = parameters.getDouble("D_RECRUIT");
        mask = parameters.get("MASK");

        signals = new ArrayList<>();
        String signalList = parameters.get("SIGNALS");
        if (signalList != null && !signalList.equalsIgnoreCase("NONE")) {
            for (String signal : signalList.split(",")) {
                if (!signal.trim().isEmpty()) {
                    signals.add(signal.trim());
                }
            }
        }

        if (!(rate >= 0) || !(distance >= 0)) {
            throw new IllegalArgumentException("recruit requires R_BASE >= 0 and D_RECRUIT >= 0");
        }
        if (!signals.isEmpty() && !(halfSaturation > 0)) {
            throw new IllegalArgumentException("recruit with signals requires K_RECRUIT > 0");
        }
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.ACTIONS.ordinal(), 1);
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

    /**
     * Reads the supplied sites from the mask layer.
     *
     * @param sim the simulation instance
     */
    void readMask(Simulation sim) {
        Lattice maskLattice = sim.getLattice(mask);
        if (maskLattice == null) {
            throw new IllegalStateException("recruit mask layer [ " + mask + " ] not found");
        }
        PottsLattice lattice = (PottsLattice) maskLattice;
        for (Category category : Category.values()) {
            if (lattice.getOperation(category) != null) {
                throw new IllegalStateException(
                        "recruit mask layer [ " + mask + " ] must have no operations");
            }
        }

        double ds = lattice.getDs();
        area = ds * ds * 1E-6;
        tick = lattice.getDt();

        sites = new ArrayList<>();
        ArrayList<Double> values = new ArrayList<>();
        double[][][] field = lattice.getField();
        for (int k = 0; k < field.length; k++) {
            for (int i = 0; i < field[k].length; i++) {
                for (int j = 0; j < field[k][i].length; j++) {
                    double value = field[k][i][j];
                    if (value < 0) {
                        throw new IllegalStateException(
                                "recruit mask layer [ " + mask + " ] has negative values");
                    }
                    if (value > 0) {
                        sites.add(new Voxel(i, j, k));
                        values.add(value);
                    }
                }
            }
        }
        siteMask = values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Gets the recruitment rate of each site for the current tick.
     *
     * @param sim the simulation instance
     * @return the expected number of cells recruited from each site this tick
     */
    double[] siteRates(Simulation sim) {
        if (sites == null) {
            readMask(sim);
        }

        double[][][][] signalFields = new double[signals.size()][][][];
        for (int s = 0; s < signals.size(); s++) {
            Lattice lattice = sim.getLattice(signals.get(s));
            if (lattice == null) {
                throw new IllegalStateException(
                        "recruit signal layer [ " + signals.get(s) + " ] not found");
            }
            signalFields[s] = lattice.getField();
        }

        double[] rates = new double[sites.size()];
        for (int n = 0; n < sites.size(); n++) {
            Voxel v = sites.get(n);
            double f = 1;
            if (signalFields.length > 0) {
                double signal = 0;
                for (double[][][] field : signalFields) {
                    signal += field[v.z][v.x][v.y];
                }
                signal = Math.max(signal, 0);
                f = signal / (halfSaturation + signal);
            }
            rates[n] = rate * siteMask[n] * f * area * tick;
        }
        return rates;
    }

    /**
     * Gets the expected number of cells recruited per population this tick.
     *
     * @param sim the simulation instance
     * @return the expected number of cells
     */
    double expected(Simulation sim) {
        double total = 0;
        for (double r : siteRates(sim)) {
            total += r;
        }
        return total;
    }

    /**
     * Chooses a site in proportion to its rate.
     *
     * @param cumulative the cumulative site rates
     * @param random the random number generator
     * @return the index of the chosen site
     */
    static int chooseSite(double[] cumulative, MersenneTwisterFast random) {
        double target = random.nextDouble() * cumulative[cumulative.length - 1];
        int low = 0;
        int high = cumulative.length - 1;
        while (low < high) {
            int mid = (low + high) / 2;
            if (cumulative[mid] > target) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * Gets the free interior voxels within the insertion distance of a site.
     *
     * @param ids the potts array of cell ids, indexed [z][x][y]
     * @param site the site
     * @param ds the voxel size [um]
     * @return the list of free voxels
     */
    List<Voxel> freeVoxelsNear(int[][][] ids, Voxel site, double ds) {
        int reach = (int) Math.floor(distance / ds);
        boolean flat = ids.length == 1;
        List<Voxel> free = new ArrayList<>();
        for (int dz = flat ? 0 : -reach; dz <= (flat ? 0 : reach); dz++) {
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dy = -reach; dy <= reach; dy++) {
                    if ((dx * dx + dy * dy + dz * dz) * ds * ds > distance * distance) {
                        continue;
                    }
                    int x = site.x + dx;
                    int y = site.y + dy;
                    int z = site.z + dz;
                    boolean insideZ = flat ? z == 0 : z >= 1 && z <= ids.length - 2;
                    if (!insideZ
                            || x < 1
                            || x > ids[z].length - 2
                            || y < 1
                            || y > ids[z][x].length - 2) {
                        continue;
                    }
                    if (ids[z][x][y] == 0) {
                        free.add(new Voxel(x, y, z));
                    }
                }
            }
        }
        return free;
    }

    @Override
    public void step(SimState simstate) {
        if (rate == 0 || pops.isEmpty()) {
            return;
        }
        PottsSimulation sim = (PottsSimulation) simstate;
        double[] rates = siteRates(sim);
        double[] cumulative = new double[rates.length];
        double total = 0;
        for (int n = 0; n < rates.length; n++) {
            total += rates[n];
            cumulative[n] = total;
        }
        if (!(total > 0)) {
            return;
        }

        double ds = Math.sqrt(area * 1E6);
        int[][][] ids = sim.getPotts().ids;
        for (int pop : pops) {
            int number = new Poisson(total, sim.random).nextInt();
            for (int c = 0; c < number; c++) {
                Voxel site = sites.get(chooseSite(cumulative, sim.random));
                List<Voxel> free = freeVoxelsNear(ids, site, ds);
                if (free.isEmpty()) {
                    failures++;
                    continue;
                }
                ArrayList<Voxel> seed = new ArrayList<>();
                seed.add(free.get(sim.random.nextInt(free.size())));
                if (sim.insertCell(pop, seed) == null) {
                    failures++;
                } else {
                    recruited++;
                }
            }
        }
    }

    /**
     * Gets the number of cells recruited.
     *
     * @return the number of cells recruited
     */
    public int getRecruited() {
        return recruited;
    }

    /**
     * Gets the number of recruitment attempts that failed.
     *
     * <p>An attempt fails when there is no free voxel near its site or when {@link
     * PottsSimulation#insertCell} rejects it.
     *
     * @return the number of failed attempts
     */
    public int getFailures() {
        return failures;
    }
}
