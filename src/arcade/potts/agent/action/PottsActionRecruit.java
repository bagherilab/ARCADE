package arcade.potts.agent.action;

import java.util.ArrayList;
import java.util.List;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.util.distribution.Poisson;
import ec.util.MersenneTwisterFast;
import arcade.core.agent.action.Action;
import arcade.core.env.component.Component;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.env.component.PottsComponentVessels;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.util.PottsEnums.Category;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Action} for recruiting cell agents from vasculature.
 *
 * <p>Each tick, cells of each registered population are recruited from sites, which come from one
 * of two sources set by {@code SOURCE}:
 *
 * <ul>
 *   <li>{@code MASK} (implicit vasculature, V0): the voxels with a positive value m in the supply
 *       mask layer (the same mask as the oxygen source), each with rate {@code R_BASE} m dA, where
 *       dA is the voxel area. The mask is read once, on the first step, and must have no
 *       operations.
 *   <li>{@code VESSELS} (vessel network, V1): the recruitment sites of the vessel component named
 *       by {@code VESSELS}, each with rate {@code R_VESSEL} w, where w is the integrity times the
 *       perfused vessel length near the slice. Sites are re-read when the component recomputes
 *       them.
 * </ul>
 *
 * <p>Each site rate is multiplied by f = S / ({@code K_RECRUIT} + S) for the summed value S of the
 * {@code SIGNALS} layers at the site (f = 1 if there are no signal layers) and by the tick length.
 * The number recruited per tick is drawn from a Poisson distribution with the summed rate, and
 * sites are chosen in proportion to their rates. Each new cell is inserted on one free voxel within
 * {@code D_RECRUIT} of its site, chosen at random. If there is no free voxel, or the insertion is
 * rejected, the attempt is counted as a failure.
 */
public class PottsActionRecruit implements Action {
    /** Sources of recruitment sites. */
    public enum Source {
        /** Sites from the supply mask layer (implicit vasculature). */
        MASK,

        /** Sites from a vessel component. */
        VESSELS
    }

    /** Source of recruitment sites. */
    private final Source source;

    /** Recruitment rate at full signal [cells/hour/mm^2 for MASK, cells/hour/mm for VESSELS]. */
    private final double rate;

    /** Id of the vessel component. */
    private final String vessels;

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

    /** Recruitment sites. */
    private List<Voxel> sites;

    /** Weight of each site [mm^2 of supplied tissue for MASK, mm of vessel for VESSELS]. */
    private double[] siteWeight;

    /** Vessel site weights the sites were read from, to detect when they change. */
    private double[][] vesselSource;

    /** Voxel size [um]. */
    private double ds;

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
     *   <li>{@code SOURCE} = source of sites, {@code MASK} or {@code VESSELS}
     *   <li>{@code R_BASE} = recruitment rate from the mask at full supply and signal
     *       [cells/hour/mm^2]
     *   <li>{@code R_VESSEL} = recruitment rate from vessels at full integrity and signal
     *       [cells/hour/mm of vessel]
     *   <li>{@code VESSELS} = id of the vessel component
     *   <li>{@code SIGNALS} = comma-separated ids of signal layers, or {@code NONE}
     *   <li>{@code K_RECRUIT} = half-saturation of the summed signal [layer units]
     *   <li>{@code D_RECRUIT} = distance from a site that cells are inserted within [um]
     *   <li>{@code MASK} = id of the supply mask layer
     * </ul>
     *
     * @param parameters the action parameters dictionary
     */
    public PottsActionRecruit(MiniBox parameters) {
        String sourceName = parameters.get("SOURCE");
        source = sourceName == null ? Source.MASK : Source.valueOf(sourceName.toUpperCase());
        rate =
                source == Source.MASK
                        ? parameters.getDouble("R_BASE")
                        : parameters.getDouble("R_VESSEL");
        vessels = parameters.get("VESSELS");
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
            throw new IllegalArgumentException(
                    "recruit requires a rate (R_BASE or R_VESSEL) >= 0 and D_RECRUIT >= 0");
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
     * Reads the sites from the mask layer.
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

        ds = lattice.getDs();
        tick = lattice.getDt();
        double area = ds * ds * 1E-6;

        sites = new ArrayList<>();
        ArrayList<Double> weights = new ArrayList<>();
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
                        weights.add(value * area);
                    }
                }
            }
        }
        siteWeight = weights.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Gets the vessel component.
     *
     * @param sim the simulation instance
     * @return the vessel component
     */
    PottsComponentVessels getVessels(Simulation sim) {
        Component component = sim.getComponent(vessels);
        if (!(component instanceof PottsComponentVessels)) {
            throw new IllegalStateException(
                    "recruit vessel component [ " + vessels + " ] not found");
        }
        return (PottsComponentVessels) component;
    }

    /**
     * Reads the sites from the vessel component, if they changed since last read.
     *
     * @param sim the simulation instance
     */
    void readVessels(Simulation sim) {
        PottsComponentVessels component = getVessels(sim);
        double[][] weights = component.getSiteWeights(sim);
        if (weights == vesselSource) {
            return;
        }
        PottsLattice lattice = component.getReferenceLattice(sim);
        ds = lattice.getDs();
        tick = lattice.getDt();

        sites = new ArrayList<>();
        ArrayList<Double> values = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                if (weights[i][j] > 0) {
                    sites.add(new Voxel(i, j, 0));
                    values.add(weights[i][j] * 1E-3);
                }
            }
        }
        siteWeight = values.stream().mapToDouble(Double::doubleValue).toArray();
        vesselSource = weights;
    }

    /**
     * Gets the recruitment rate of each site for the current tick.
     *
     * @param sim the simulation instance
     * @return the expected number of cells recruited from each site this tick
     */
    double[] siteRates(Simulation sim) {
        if (source == Source.MASK) {
            if (sites == null) {
                readMask(sim);
            }
        } else {
            readVessels(sim);
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
            rates[n] = rate * siteWeight[n] * f * tick;
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
