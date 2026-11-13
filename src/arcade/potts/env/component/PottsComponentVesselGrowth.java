package arcade.potts.env.component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sim.engine.Schedule;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.env.component.Component;
import arcade.core.env.lattice.Lattice;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.agent.cell.PottsCellFibroblast;
import arcade.potts.agent.cell.PottsCellMyofibroblast;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import static arcade.potts.env.component.PottsVesselNode.Root;
import static arcade.potts.util.PottsEnums.Ordering;

/**
 * Implementation of {@link Component} for sprouting angiogenesis in a vessel network (V2).
 *
 * <p>The component edits the network of a {@link PottsComponentVessels} component every {@code
 * INTERVAL} ticks. The registered layer is the VEGF layer, read at each point's (x, y) in the slice
 * (the tissue is assumed uniform along z). VEGF occupancy is S(V) = V / ({@code K_V} + V). Each
 * update, with dt the update interval [hours]:
 *
 * <ol>
 *   <li><b>Pruning (R8):</b> perfused segments with integrity below {@code I_STABLE} and occupancy
 *       at their midpoint below {@code S_SURVIVE} are removed with probability 1 - exp(-{@code
 *       K_PRUNE} dt).
 *   <li><b>Maturation (R7):</b> perfused segments relax toward integrity 1 at rate {@code K_MAT}.
 *   <li><b>Dead ends:</b> every non-root node with one segment that is not already a tip becomes a
 *       dormant tip (cut ends at the start, stubs left by pruning later).
 *   <li><b>Regression (R6):</b> a tip whose sprout has not extended for {@code T_REGRESS} is
 *       removed back to its branch point, segment by segment, while the end is a non-root dead end
 *       with an unperfused segment.
 *   <li><b>Initiation (R1):</b> each perfused, non-root node with two segments starts a sprout with
 *       probability 1 - exp(-{@code P_MAX} S dt), unless an active tip lies within {@code D_TIP} or
 *       the node sprouted within {@code T_REFRACT}. Dormant tips activate with the same
 *       probability.
 *   <li><b>Extension (R2, R3, R5):</b> each active tip adds one segment of length {@code V_TIP} dt
 *       in one of 32 directions u, with weights exp(β (V(q) - V(p)) / V(p) + κ u · u_prev) for
 *       target q (the VEGF term is 0 where V(p) = 0, and the persistence term is 0 for the first
 *       step). Only directions whose target lies inside the slice in x and y and within {@code
 *       D_SUPPORT} of a fibroblast or myofibroblast are allowed; with none, the tip stalls. A
 *       sprout of length {@code L_MAX} stops extending.
 *   <li><b>Anastomosis (R4):</b> a new tip within {@code D_ANAST} of a segment outside its own
 *       sprout joins that segment's nearer node, and the sprout ends. Segments touching nodes
 *       within two segments of the sprout's base are not targets, so a sprout does not rejoin its
 *       parent vessel.
 * </ol>
 *
 * <p>New segments start at integrity {@code I_SPROUT}. Perfusion is updated before and after the
 * edits, and the vessel component is refreshed so that delivery and recruitment follow.
 */
public class PottsComponentVesselGrowth implements Component {
    /** Number of candidate directions. */
    static final int DIRECTIONS = 32;

    /** Candidate unit directions on a Fibonacci sphere. */
    static final double[][] UNIT_DIRECTIONS = fibonacci(DIRECTIONS);

    /** Number of segments from the base that are not anastomosis targets. */
    static final int EXCLUDED_HOPS = 2;

    /** Update interval [ticks]. */
    private final int interval;

    /** Maximum sprouting rate [1/hour]. */
    private final double maxRate;

    /** VEGF half-occupancy value [layer units]. */
    private final double halfOccupancy;

    /** Minimum distance between a new sprout and an active tip [um]. */
    private final double tipDistance;

    /** Refractory time after sprouting [hours]. */
    private final double refractory;

    /** Steering strength β on the relative VEGF difference. */
    private final double beta;

    /** Persistence κ. */
    private final double kappa;

    /** Tip speed [um/hour]. */
    private final double speed;

    /** Distance to a supporting cell [um]. */
    private final double supportDistance;

    /** Anastomosis distance [um]. */
    private final double anastomosisDistance;

    /** Maximum sprout length [um]. */
    private final double maxLength;

    /** Time without extension before a blind sprout regresses [hours]. */
    private final double regressTime;

    /** Integrity of new segments. */
    private final double sproutIntegrity;

    /** Maturation rate [1/hour]. */
    private final double maturationRate;

    /** Integrity at or above which segments are not pruned. */
    private final double stableIntegrity;

    /** Occupancy below which immature segments can be pruned. */
    private final double survivalOccupancy;

    /** Pruning rate [1/hour]. */
    private final double pruneRate;

    /** Id of the vessel component. */
    private final String vessels;

    /** Id of the VEGF layer. */
    private String layer;

    /** Tracked tips. */
    private final List<Sprout> sprouts = new ArrayList<>();

    /** Time each node last sprouted [hours]. */
    private final Map<PottsVesselNode, Double> lastSprouted = new HashMap<>();

    /** A sprout: its tip, heading, length, and bookkeeping. */
    static final class Sprout {
        /** Current tip node. */
        PottsVesselNode tip;

        /** Heading of the last step, or {@code null} before the first step. */
        double[] heading;

        /** Length grown [um]. */
        double length;

        /** Time of the last extension (or creation) [hours]. */
        double lastGrowth;

        /** {@code true} until activated, for dead ends. */
        boolean dormant;

        /** {@code true} once the sprout reaches the maximum length. */
        boolean capped;

        /** Nodes of the sprout and near its base, which are not anastomosis targets. */
        final Set<PottsVesselNode> excluded = new HashSet<>();

        /**
         * Creates a sprout.
         *
         * @param tip the tip node
         * @param heading the heading, or {@code null}
         * @param time the creation time [hours]
         * @param dormant {@code true} if the tip is dormant
         */
        Sprout(PottsVesselNode tip, double[] heading, double time, boolean dormant) {
            this.tip = tip;
            this.heading = heading;
            this.lastGrowth = time;
            this.dormant = dormant;
        }
    }

    /**
     * Creates a {@link Component} for vessel growth.
     *
     * <p>Loaded parameters include {@code INTERVAL} [ticks], {@code P_MAX} [1/hour], {@code K_V}
     * [layer units], {@code D_TIP} [um], {@code T_REFRACT} [hours], {@code BETA}, {@code KAPPA},
     * {@code V_TIP} [um/hour], {@code D_SUPPORT} [um], {@code D_ANAST} [um], {@code L_MAX} [um],
     * {@code T_REGRESS} [hours], {@code I_SPROUT}, {@code K_MAT} [1/hour], {@code I_STABLE}, {@code
     * S_SURVIVE}, {@code K_PRUNE} [1/hour], and {@code VESSELS} (vessel component id).
     *
     * @param parameters the component parameters dictionary
     */
    public PottsComponentVesselGrowth(MiniBox parameters) {
        interval = parameters.getInt("INTERVAL");
        maxRate = parameters.getDouble("P_MAX");
        halfOccupancy = parameters.getDouble("K_V");
        tipDistance = parameters.getDouble("D_TIP");
        refractory = parameters.getDouble("T_REFRACT");
        beta = parameters.getDouble("BETA");
        kappa = parameters.getDouble("KAPPA");
        speed = parameters.getDouble("V_TIP");
        supportDistance = parameters.getDouble("D_SUPPORT");
        anastomosisDistance = parameters.getDouble("D_ANAST");
        maxLength = parameters.getDouble("L_MAX");
        regressTime = parameters.getDouble("T_REGRESS");
        sproutIntegrity = parameters.getDouble("I_SPROUT");
        maturationRate = parameters.getDouble("K_MAT");
        stableIntegrity = parameters.getDouble("I_STABLE");
        survivalOccupancy = parameters.getDouble("S_SURVIVE");
        pruneRate = parameters.getDouble("K_PRUNE");
        vessels = parameters.get("VESSELS");
        if (interval < 1
                || !(maxRate >= 0)
                || !(halfOccupancy > 0)
                || !(tipDistance >= 0)
                || !(refractory >= 0)
                || !(speed > 0)
                || !(supportDistance >= 0)
                || !(anastomosisDistance >= 0)
                || !(maxLength > 0)
                || !(regressTime >= 0)
                || !(sproutIntegrity >= 0 && sproutIntegrity <= 1)
                || !(maturationRate >= 0)
                || !(pruneRate >= 0)) {
            throw new IllegalArgumentException(
                    "vessel growth requires INTERVAL >= 1, K_V > 0, V_TIP > 0, L_MAX > 0,"
                            + " I_SPROUT in [0, 1], and non-negative rates and distances");
        }
        if (vessels == null || vessels.isEmpty()) {
            throw new IllegalArgumentException("vessel growth requires a VESSELS component id");
        }
    }

    /**
     * Makes evenly spread unit vectors on a Fibonacci sphere.
     *
     * @param n the number of vectors
     * @return the unit vectors
     */
    static double[][] fibonacci(int n) {
        double golden = Math.PI * (3 - Math.sqrt(5));
        double[][] directions = new double[n][];
        for (int i = 0; i < n; i++) {
            double y = 1 - 2 * (i + 0.5) / n;
            double r = Math.sqrt(1 - y * y);
            double theta = golden * i;
            directions[i] = new double[] {r * Math.cos(theta), y, r * Math.sin(theta)};
        }
        return directions;
    }

    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.COMPONENTS.ordinal(), interval);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The registered layer is the VEGF layer; only one can be registered.
     */
    @Override
    public void register(Simulation sim, String layer) {
        if (this.layer != null) {
            throw new IllegalStateException("vessel growth reads one VEGF layer");
        }
        this.layer = layer;
    }

    /**
     * Gets the tracked tips.
     *
     * @return the list of sprouts
     */
    List<Sprout> getSprouts() {
        return sprouts;
    }

    @Override
    public void step(SimState simstate) {
        Simulation sim = (Simulation) simstate;
        Component component = sim.getComponent(vessels);
        if (!(component instanceof PottsComponentVessels)) {
            throw new IllegalStateException(
                    "vessel growth component [ " + vessels + " ] not found");
        }
        if (layer == null) {
            throw new IllegalStateException("vessel growth has no registered VEGF layer");
        }
        Lattice lattice = sim.getLattice(layer);
        if (lattice == null) {
            throw new IllegalStateException("vessel growth layer [ " + layer + " ] not found");
        }
        PottsComponentVessels vesselComponent = (PottsComponentVessels) component;
        PottsLattice vegf = (PottsLattice) lattice;
        if (vesselComponent.getNetwork() == null) {
            vesselComponent.prepare(sim);
        }
        double dt = interval * vegf.getDt();
        double now = simstate.schedule.getTime() * vegf.getDt();
        boolean[][] support = supportMap(((PottsSimulation) sim).getPotts());
        double[][] field = vegf.getField()[0];

        boolean changed =
                update(
                        vesselComponent.getNetwork(),
                        field,
                        support,
                        vegf.getDs(),
                        dt,
                        now,
                        simstate.random);
        if (changed) {
            vesselComponent.refresh();
        }
    }

    /**
     * Marks the slice voxels occupied by fibroblasts or myofibroblasts.
     *
     * @param potts the potts instance
     * @return the support map, indexed [x][y]
     */
    static boolean[][] supportMap(Potts potts) {
        int[][] ids = potts.ids[0];
        boolean[][] support = new boolean[ids.length][ids[0].length];
        Map<Integer, Boolean> cache = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            for (int j = 0; j < ids[i].length; j++) {
                int id = ids[i][j];
                if (id > 0) {
                    support[i][j] =
                            cache.computeIfAbsent(
                                    id,
                                    key -> {
                                        PottsCell cell = potts.getCell(key);
                                        return cell instanceof PottsCellFibroblast
                                                || cell instanceof PottsCellMyofibroblast;
                                    });
                }
            }
        }
        return support;
    }

    /**
     * Applies one growth update to a network.
     *
     * @param network the vessel network
     * @param field the VEGF values in the slice, indexed [x][y]
     * @param support the voxels occupied by supporting cells, indexed [x][y]
     * @param ds the voxel size [um]
     * @param dt the update interval [hours]
     * @param now the current time [hours]
     * @param random the random number generator
     * @return {@code true} if the network or any integrity changed, {@code false} otherwise
     */
    boolean update(
            PottsVesselNetwork network,
            double[][] field,
            boolean[][] support,
            double ds,
            double dt,
            double now,
            MersenneTwisterFast random) {
        boolean changed = false;
        PottsVesselPerfusion.update(network);

        // R8 pruning and R7 maturation of perfused segments.
        double prune = 1 - Math.exp(-pruneRate * dt);
        double keep = Math.exp(-maturationRate * dt);
        for (PottsVesselEdge segment : network.getSegments()) {
            if (!segment.isPerfused()) {
                continue;
            }
            double integrity = segment.getIntegrity();
            if (integrity < stableIntegrity && prune > 0) {
                double[] mid = midpoint(segment, network.getThickness());
                if (occupancy(value(field, ds, mid)) < survivalOccupancy
                        && random.nextDouble() < prune) {
                    network.removeSegment(segment);
                    changed = true;
                    continue;
                }
            }
            if (integrity < 1 && maturationRate > 0) {
                segment.setIntegrity(1 - (1 - integrity) * keep);
                changed = true;
            }
        }
        if (changed) {
            PottsVesselPerfusion.update(network);
        }

        // Drop tips whose node is gone or no longer a dead end (except unextended sprout bases).
        sprouts.removeIf(
                s ->
                        !network.contains(s.tip)
                                || ((s.dormant || s.length > 0) && network.getDegree(s.tip) != 1));

        // Dead ends become dormant tips.
        Set<PottsVesselNode> tips = new HashSet<>();
        for (Sprout sprout : sprouts) {
            tips.add(sprout.tip);
        }
        for (PottsVesselNode node : network.getNodes()) {
            if (!node.isRoot() && network.getDegree(node) == 1 && !tips.contains(node)) {
                PottsVesselEdge segment = network.getSegments(node).get(0);
                PottsVesselNode other = network.getOther(segment, node);
                double[] heading =
                        unit(
                                PottsVesselGeometry.vector(
                                        other.getPosition(),
                                        node.getPosition(),
                                        network.getThickness()));
                Sprout sprout = new Sprout(node, heading, now, true);
                sprout.excluded.addAll(nearby(network, node, EXCLUDED_HOPS));
                sprouts.add(sprout);
            }
        }

        // R6 regression of blind sprouts that stopped extending.
        List<Sprout> regressed = new ArrayList<>();
        for (Sprout sprout : sprouts) {
            if (now - sprout.lastGrowth >= regressTime && network.contains(sprout.tip)) {
                changed |= regress(network, sprout.tip);
                regressed.add(sprout);
            }
        }
        sprouts.removeAll(regressed);
        // Regression can remove other tips, for example both ends of an isolated fragment.
        sprouts.removeIf(s -> !network.contains(s.tip));

        // R1 initiation of new sprouts and activation of dormant tips.
        double thickness = network.getThickness();
        for (Sprout sprout : sprouts) {
            if (sprout.dormant
                    && random.nextDouble() < chance(field, ds, sprout.tip.getPosition(), dt)) {
                sprout.dormant = false;
            }
        }
        for (PottsVesselNode node : network.getNodes()) {
            if (node.isRoot() || network.getDegree(node) != 2 || !isPerfused(network, node)) {
                continue;
            }
            Double last = lastSprouted.get(node);
            if (last != null && now - last < refractory) {
                continue;
            }
            if (!(random.nextDouble() < chance(field, ds, node.getPosition(), dt))) {
                continue;
            }
            if (nearActiveTip(node.getPosition(), thickness)) {
                continue;
            }
            Sprout sprout = new Sprout(node, null, now, false);
            sprout.excluded.addAll(nearby(network, node, EXCLUDED_HOPS));
            sprouts.add(sprout);
            lastSprouted.put(node, now);
        }

        // R2-R5 extension and R4 anastomosis.
        List<Sprout> joined = new ArrayList<>();
        for (Sprout sprout : new ArrayList<>(sprouts)) {
            if (sprout.dormant || sprout.capped) {
                continue;
            }
            PottsVesselNode target = extend(network, sprout, field, support, ds, dt, random);
            if (target == null) {
                continue;
            }
            changed = true;
            sprout.lastGrowth = now;
            if (anastomose(network, sprout)) {
                joined.add(sprout);
            } else if (sprout.length >= maxLength - 1e-9) {
                sprout.capped = true;
            }
        }
        sprouts.removeAll(joined);

        if (changed) {
            PottsVesselPerfusion.update(network);
        }
        return changed;
    }

    /**
     * Gets the chance of sprouting at a point over one update.
     *
     * @param field the VEGF values
     * @param ds the voxel size [um]
     * @param position the point [um]
     * @param dt the update interval [hours]
     * @return the probability
     */
    double chance(double[][] field, double ds, double[] position, double dt) {
        return 1 - Math.exp(-maxRate * occupancy(value(field, ds, position)) * dt);
    }

    /**
     * Gets the VEGF occupancy.
     *
     * @param v the VEGF value
     * @return the occupancy V / (K_V + V), 0 for non-positive or missing values
     */
    double occupancy(double v) {
        return v > 0 ? v / (halfOccupancy + v) : 0;
    }

    /**
     * Gets the slice value at a point's (x, y).
     *
     * @param field the values, indexed [x][y]
     * @param ds the voxel size [um]
     * @param position the point [um]
     * @return the value, or NaN outside the slice
     */
    static double value(double[][] field, double ds, double[] position) {
        int i = (int) Math.floor(position[0] / ds);
        int j = (int) Math.floor(position[1] / ds);
        if (i < 0 || j < 0 || i >= field.length || j >= field[0].length) {
            return Double.NaN;
        }
        return field[i][j];
    }

    /**
     * Checks if a point's (x, y) lies within a distance of a supporting voxel.
     *
     * @param support the support map, indexed [x][y]
     * @param ds the voxel size [um]
     * @param position the point [um]
     * @param distance the support distance [um]
     * @return {@code true} if supported, {@code false} otherwise
     */
    static boolean isSupported(boolean[][] support, double ds, double[] position, double distance) {
        int i0 = (int) Math.floor((position[0] - distance) / ds);
        int i1 = (int) Math.floor((position[0] + distance) / ds);
        int j0 = (int) Math.floor((position[1] - distance) / ds);
        int j1 = (int) Math.floor((position[1] + distance) / ds);
        for (int i = Math.max(0, i0); i <= Math.min(support.length - 1, i1); i++) {
            for (int j = Math.max(0, j0); j <= Math.min(support[i].length - 1, j1); j++) {
                if (!support[i][j]) {
                    continue;
                }
                double dx = Math.max(0, Math.max(i * ds - position[0], position[0] - (i + 1) * ds));
                double dy = Math.max(0, Math.max(j * ds - position[1], position[1] - (j + 1) * ds));
                if (dx * dx + dy * dy <= distance * distance) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a node touches a perfused segment.
     *
     * @param network the vessel network
     * @param node the node
     * @return {@code true} if perfused, {@code false} otherwise
     */
    static boolean isPerfused(PottsVesselNetwork network, PottsVesselNode node) {
        for (PottsVesselEdge segment : network.getSegments(node)) {
            if (segment.isPerfused()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an active tip lies within {@code D_TIP} of a point.
     *
     * @param position the point [um]
     * @param thickness the slab thickness [um]
     * @return {@code true} if an active tip is near, {@code false} otherwise
     */
    boolean nearActiveTip(double[] position, double thickness) {
        for (Sprout sprout : sprouts) {
            if (sprout.dormant || sprout.capped) {
                continue;
            }
            if (distance(position, sprout.tip.getPosition(), thickness) < tipDistance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extends a sprout by one segment, if any direction is allowed.
     *
     * @param network the vessel network
     * @param sprout the sprout
     * @param field the VEGF values
     * @param support the support map
     * @param ds the voxel size [um]
     * @param dt the update interval [hours]
     * @param random the random number generator
     * @return the new tip node, or {@code null} if the tip stalls
     */
    PottsVesselNode extend(
            PottsVesselNetwork network,
            Sprout sprout,
            double[][] field,
            boolean[][] support,
            double ds,
            double dt,
            MersenneTwisterFast random) {
        double step = Math.min(speed * dt, maxLength - sprout.length);
        double[] p = sprout.tip.getPosition();
        double vp = value(field, ds, p);
        double width = field.length * ds;
        double height = field[0].length * ds;
        double thickness = network.getThickness();

        double[] scores = new double[DIRECTIONS];
        double[][] targets = new double[DIRECTIONS][];
        double best = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < DIRECTIONS; k++) {
            double[] u = UNIT_DIRECTIONS[k];
            double[] q = {p[0] + step * u[0], p[1] + step * u[1], p[2] + step * u[2]};
            scores[k] = Double.NEGATIVE_INFINITY;
            if (q[0] < 0 || q[0] >= width || q[1] < 0 || q[1] >= height) {
                continue;
            }
            if (!isSupported(support, ds, q, supportDistance)) {
                continue;
            }
            if (network.findNode(q[0], q[1], q[2]) != null) {
                continue;
            }
            double score = 0;
            if (vp > 0) {
                double vq = value(field, ds, q);
                score += beta * (vq - vp) / vp;
            }
            if (sprout.heading != null) {
                score +=
                        kappa
                                * (u[0] * sprout.heading[0]
                                        + u[1] * sprout.heading[1]
                                        + u[2] * sprout.heading[2]);
            }
            scores[k] = score;
            targets[k] = q;
            best = Math.max(best, score);
        }
        if (best == Double.NEGATIVE_INFINITY) {
            return null;
        }

        double total = 0;
        double[] weights = new double[DIRECTIONS];
        for (int k = 0; k < DIRECTIONS; k++) {
            weights[k] = targets[k] == null ? 0 : Math.exp(scores[k] - best);
            total += weights[k];
        }
        double pick = random.nextDouble() * total;
        int chosen = -1;
        for (int k = 0; k < DIRECTIONS; k++) {
            if (weights[k] == 0) {
                continue;
            }
            chosen = k;
            pick -= weights[k];
            if (pick < 0) {
                break;
            }
        }

        double[] q = targets[chosen];
        PottsVesselNode node = network.addNode(q[0], q[1], q[2], Root.NONE);
        network.addSegment(sprout.tip, node, sproutIntegrity);
        sprout.tip = node;
        sprout.heading = UNIT_DIRECTIONS[chosen];
        sprout.length += step;
        sprout.excluded.add(node);
        return node;
    }

    /**
     * Joins a sprout tip to a nearby segment outside its own sprout, if there is one.
     *
     * @param network the vessel network
     * @param sprout the sprout
     * @return {@code true} if the sprout joined, {@code false} otherwise
     */
    boolean anastomose(PottsVesselNetwork network, Sprout sprout) {
        double thickness = network.getThickness();
        double[] p = sprout.tip.getPosition();
        PottsVesselEdge nearest = null;
        double min = anastomosisDistance;
        for (PottsVesselEdge segment : network.getSegments()) {
            PottsVesselNode a = segment.getStart();
            PottsVesselNode b = segment.getEnd();
            if (sprout.excluded.contains(a) || sprout.excluded.contains(b)) {
                continue;
            }
            double d =
                    PottsVesselGeometry.pointSegmentDistance(
                            p, a.getPosition(), b.getPosition(), thickness);
            if (d <= min) {
                min = d;
                nearest = segment;
            }
        }
        if (nearest == null) {
            return false;
        }
        PottsVesselNode b = network.getOther(nearest, nearest.getStart());
        PottsVesselNode a = network.getOther(nearest, b);
        double da = distance(p, a.getPosition(), thickness);
        double db = distance(p, b.getPosition(), thickness);
        network.addSegment(sprout.tip, da <= db ? a : b, sproutIntegrity);
        return true;
    }

    /**
     * Removes a blind sprout back to its branch point.
     *
     * @param network the vessel network
     * @param tip the tip node
     * @return {@code true} if any segment was removed, {@code false} otherwise
     */
    static boolean regress(PottsVesselNetwork network, PottsVesselNode tip) {
        boolean removed = false;
        PottsVesselNode node = tip;
        while (network.contains(node) && !node.isRoot() && network.getDegree(node) == 1) {
            PottsVesselEdge segment = network.getSegments(node).get(0);
            if (segment.isPerfused()) {
                break;
            }
            PottsVesselNode other = network.getOther(segment, node);
            network.removeSegment(segment);
            removed = true;
            node = other;
        }
        return removed;
    }

    /**
     * Gets the nodes within a number of segments of a node.
     *
     * @param network the vessel network
     * @param start the start node
     * @param hops the number of segments
     * @return the set of nodes, including the start node
     */
    static Set<PottsVesselNode> nearby(
            PottsVesselNetwork network, PottsVesselNode start, int hops) {
        Set<PottsVesselNode> seen = new HashSet<>();
        seen.add(start);
        ArrayDeque<PottsVesselNode> frontier = new ArrayDeque<>();
        frontier.add(start);
        for (int h = 0; h < hops; h++) {
            ArrayDeque<PottsVesselNode> next = new ArrayDeque<>();
            for (PottsVesselNode node : frontier) {
                for (PottsVesselNode neighbor : network.getNeighbors(node)) {
                    if (seen.add(neighbor)) {
                        next.add(neighbor);
                    }
                }
            }
            frontier = next;
        }
        return seen;
    }

    /**
     * Gets the distance between two points, using the shortest periodic separation along z.
     *
     * <p>Unlike a segment, two points may be half the slab apart along z.
     *
     * @param a the first point [um]
     * @param b the second point [um]
     * @param thickness the slab thickness [um]
     * @return the distance [um]
     */
    static double distance(double[] a, double[] b, double thickness) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = PottsVesselGeometry.minimumImage(b[2] - a[2], thickness);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Gets the midpoint of a segment.
     *
     * @param segment the segment
     * @param thickness the slab thickness [um]
     * @return the midpoint [um]
     */
    static double[] midpoint(PottsVesselEdge segment, double thickness) {
        double[] a = segment.getStart().getPosition();
        double[] v = PottsVesselGeometry.vector(a, segment.getEnd().getPosition(), thickness);
        return new double[] {a[0] + v[0] / 2, a[1] + v[1] / 2, a[2] + v[2] / 2};
    }

    /**
     * Normalizes a vector.
     *
     * @param v the vector
     * @return the unit vector, or {@code null} for a zero vector
     */
    static double[] unit(double[] v) {
        double n = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return n == 0 ? null : new double[] {v[0] / n, v[1] / n, v[2] / n};
    }
}
