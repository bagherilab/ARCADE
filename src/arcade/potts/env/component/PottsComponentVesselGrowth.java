package arcade.potts.env.component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
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
 * <p><b>Injury cue.</b> If {@code INJURY_WEIGHT} w is above 0, every rule above reads the signal V
 * + w I instead of V, where I is the {@code INJURY_SIGNAL} layer: the early, wound-released
 * angiogenic factors (FGF2 and PDGF from platelets, the clot and damaged tissue) that start wound
 * sprouting before hypoxic VEGF rises. If {@code INJURY_TAU} τ is above 0, the weight fades as w
 * exp(-t / τ) with t the time since the start [hours], since FGF2 is released at injury and is
 * largely gone within about 3 days; τ = 0 keeps w constant. With w = 0 (the default) the VEGF layer
 * is read alone, and unwounded tissue (I = 0) is unaffected at any w.
 *
 * <p><b>Mature sources.</b> If {@code SPROUT_MIN_INTEGRITY} m is above 0, a node starts a sprout
 * (R1) only if every segment at it has integrity at least m, and a dormant tip activates only if
 * its segment does. Integrity is the vessel's maturation clock (R7): new segments start at {@code
 * I_SPROUT} and mature while perfused, so new vessels cannot sprout until they have matured, as
 * sprouting angiogenesis starts from established, pericyte-covered vessels. With m = 0 (the
 * default) any perfused node can sprout.
 *
 * <p><b>Fibrin support.</b> If {@code FIBRIN_SUPPORT} f is above 0, voxels where the provisional
 * matrix ({@code WOUND_MATRIX} layer, the fibrin clot) is above f also count as support for R3, as
 * well as fibroblasts and myofibroblasts: sprout tips migrate on fibrin through integrin αvβ3 and
 * enter the clot ahead of the fibroblast front. With f = 0 (the default) only cells support, and
 * unwounded tissue (no clot) is unaffected at any f.
 *
 * <p>New segments start at integrity {@code I_SPROUT}. Perfusion is updated before and after the
 * edits, and the vessel component is refreshed so that delivery and recruitment follow.
 *
 * <p><b>Coupling to vessel cells.</b> Without coupling (the default), the component behaves as
 * above. Once a vessel cell component calls {@link #couple}, extension (R2, R3) changes for tips
 * whose proposed segment is in the plane (the coupling predicate is true for it): the direction is
 * chosen as above, but from the tip's front (the in-plane position its tip cell reached, if set),
 * and the segment is not added. Instead it is stored as the tip's aim ({@link #propose}), and the
 * vessel cell component appends a segment ({@link #append}) when its tip cell has moved far enough
 * ({@link #appendPoint}). R4 and R5 are checked at append time, and R6 counts time since the last
 * append. A tip with no allowed direction keeps its aim. Tips whose proposed segment is out of the
 * plane extend as above.
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

    /** Weight w of the injury signal added to VEGF (0 for VEGF alone). */
    private final double injuryWeight;

    /** Provisional-matrix value above which a voxel supports extension (0 for cells only). */
    private final double fibrinSupport;

    /** Provisional matrix layer read when the fibrin support threshold is above 0. */
    static final String MATRIX_LAYER = "WOUND_MATRIX";

    /** Integrity a vessel needs to be a sprout source (0 for any). */
    private final double sproutMinIntegrity;

    /** Fading time τ of the injury weight [hours] (0 for a constant weight). */
    private final double injuryTau;

    /** Injury signal layer read when the weight is above 0. */
    static final String INJURY_LAYER = "INJURY_SIGNAL";

    /** Tracked tips. */
    private final List<Sprout> sprouts = new ArrayList<>();

    /** Time each node last sprouted [hours]. */
    private final Map<PottsVesselNode, Double> lastSprouted = new HashMap<>();

    /** Time over which a tip advances one aim length [hours]. */
    static final double AIM_TIME = 1;

    /**
     * Coupling predicate: {@code true} if the segment from the first to the second point is in the
     * plane, or {@code null} without coupling.
     */
    private BiPredicate<double[], double[]> inPlane;

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

        /** In-plane front {x, y} [um] reached by the tip cell, or {@code null} if not set. */
        double[] front;

        /** Start of the aim (the proposed segment) [um], or {@code null} with no aim. */
        double[] aimStart;

        /** End of the aim [um], or {@code null} with no aim. */
        double[] aimEnd;

        /** {@code true} once the sprout joined another vessel at an append. */
        boolean joined;

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
     * S_SURVIVE}, {@code K_PRUNE} [1/hour], {@code VESSELS} (vessel component id), and optionally
     * {@code INJURY_WEIGHT} (weight of the injury signal added to VEGF; 0 if absent), {@code
     * FIBRIN_SUPPORT} (provisional matrix above which a voxel supports extension; 0 if absent),
     * {@code INJURY_TAU} (fading time of the injury weight [hours]; 0 if absent), and {@code
     * SPROUT_MIN_INTEGRITY} (integrity a vessel needs to sprout; 0 if absent).
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
        injuryWeight =
                parameters.contains("INJURY_WEIGHT") ? parameters.getDouble("INJURY_WEIGHT") : 0;
        fibrinSupport =
                parameters.contains("FIBRIN_SUPPORT") ? parameters.getDouble("FIBRIN_SUPPORT") : 0;
        if (!(fibrinSupport >= 0) || Double.isInfinite(fibrinSupport)) {
            throw new IllegalArgumentException(
                    "vessel growth requires a finite FIBRIN_SUPPORT >= 0");
        }
        sproutMinIntegrity =
                parameters.contains("SPROUT_MIN_INTEGRITY")
                        ? parameters.getDouble("SPROUT_MIN_INTEGRITY")
                        : 0;
        if (!(sproutMinIntegrity >= 0 && sproutMinIntegrity <= 1)) {
            throw new IllegalArgumentException(
                    "vessel growth requires SPROUT_MIN_INTEGRITY in [0, 1]");
        }
        injuryTau = parameters.contains("INJURY_TAU") ? parameters.getDouble("INJURY_TAU") : 0;
        if (!(injuryTau >= 0) || Double.isInfinite(injuryTau)) {
            throw new IllegalArgumentException("vessel growth requires a finite INJURY_TAU >= 0");
        }
        if (!(injuryWeight >= 0) || Double.isInfinite(injuryWeight)) {
            throw new IllegalArgumentException(
                    "vessel growth requires a finite INJURY_WEIGHT >= 0");
        }
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
     * Checks if every segment at a node is mature enough to be a sprout source.
     *
     * @param network the vessel network
     * @param node the node
     * @return {@code true} if the threshold is 0 or every segment at the node reaches it
     */
    boolean isMature(PottsVesselNetwork network, PottsVesselNode node) {
        if (sproutMinIntegrity == 0) {
            return true;
        }
        for (PottsVesselEdge segment : network.getSegments(node)) {
            if (segment.getIntegrity() < sproutMinIntegrity) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the injury weight at a time.
     *
     * @param time the time since the start [hours]
     * @return w exp(-t / τ), or w if τ is 0
     */
    double injuryWeightAt(double time) {
        return injuryTau == 0 ? injuryWeight : injuryWeight * Math.exp(-time / injuryTau);
    }

    /**
     * Gets the signal the growth rules read: VEGF plus the weighted injury signal.
     *
     * @param vegf the VEGF values in the slice, indexed [x][y]
     * @param injury the injury signal values in the slice, or {@code null} if there is no layer
     * @param weight the injury weight w
     * @return the VEGF array itself if w is 0, otherwise a new array of V + w I
     */
    static double[][] signal(double[][] vegf, double[][] injury, double weight) {
        if (weight == 0) {
            return vegf;
        }
        if (injury == null) {
            throw new IllegalStateException(
                    "vessel growth INJURY_WEIGHT > 0 needs layer [ " + INJURY_LAYER + " ]");
        }
        double[][] combined = new double[vegf.length][];
        for (int i = 0; i < vegf.length; i++) {
            combined[i] = new double[vegf[i].length];
            for (int j = 0; j < vegf[i].length; j++) {
                combined[i][j] = vegf[i][j] + weight * injury[i][j];
            }
        }
        return combined;
    }

    /**
     * Gets the tracked tips.
     *
     * @return the list of sprouts
     */
    List<Sprout> getSprouts() {
        return sprouts;
    }

    /**
     * Couples extension of in-plane tips to tip cells.
     *
     * @param inPlane {@code true} if the segment from the first to the second point [um] is in the
     *     plane
     */
    public void couple(BiPredicate<double[], double[]> inPlane) {
        this.inPlane = inPlane;
    }

    /**
     * Checks if extension is coupled to tip cells.
     *
     * @return {@code true} if coupled, {@code false} otherwise
     */
    public boolean isCoupled() {
        return inPlane != null;
    }

    /**
     * Gets the tip speed.
     *
     * @return the tip speed [um/hour]
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the integrity of new segments.
     *
     * @return the integrity
     */
    public double getSproutIntegrity() {
        return sproutIntegrity;
    }

    /**
     * Gets the aim length, the distance a tip advances in {@link #AIM_TIME}.
     *
     * @return the aim length [um]
     */
    public double getAimLength() {
        return speed * AIM_TIME;
    }

    /**
     * Gets the active tips with an in-plane aim.
     *
     * @return the sprouts that are not dormant or capped and have an aim, in order of creation
     */
    public List<Sprout> getActiveInPlaneSprouts() {
        List<Sprout> active = new ArrayList<>();
        for (Sprout sprout : sprouts) {
            if (!sprout.dormant && !sprout.capped && sprout.aimEnd != null) {
                active.add(sprout);
            }
        }
        return active;
    }

    /**
     * Sets the in-plane front of a tip, from which it is aimed.
     *
     * @param sprout the sprout
     * @param x the front x [um]
     * @param y the front y [um]
     */
    public void setFront(Sprout sprout, double x, double y) {
        sprout.front = new double[] {x, y};
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
        Lattice matrix = fibrinSupport > 0 ? sim.getLattice(MATRIX_LAYER) : null;
        addFibrin(support, matrix == null ? null : matrix.getField()[0], fibrinSupport);
        Lattice injury = injuryWeight > 0 ? sim.getLattice(INJURY_LAYER) : null;
        double[][] field =
                signal(
                        vegf.getField()[0],
                        injury == null ? null : injury.getField()[0],
                        injuryWeightAt(now));

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
     * Marks voxels whose provisional matrix is above the threshold as support.
     *
     * @param support the support map, indexed [x][y], changed in place
     * @param matrix the provisional matrix values in the slice, or {@code null} if there is no
     *     layer
     * @param threshold the fibrin support threshold (0 for none)
     */
    static void addFibrin(boolean[][] support, double[][] matrix, double threshold) {
        if (threshold == 0) {
            return;
        }
        if (matrix == null) {
            throw new IllegalStateException(
                    "vessel growth FIBRIN_SUPPORT > 0 needs layer [ " + MATRIX_LAYER + " ]");
        }
        for (int i = 0; i < support.length; i++) {
            for (int j = 0; j < support[i].length; j++) {
                if (matrix[i][j] > threshold) {
                    support[i][j] = true;
                }
            }
        }
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
                    && isMature(network, sprout.tip)
                    && random.nextDouble() < chance(field, ds, sprout.tip.getPosition(), dt)) {
                sprout.dormant = false;
            }
        }
        for (PottsVesselNode node : network.getNodes()) {
            if (node.isRoot()
                    || network.getDegree(node) != 2
                    || !isPerfused(network, node)
                    || !isMature(network, node)) {
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
            PottsVesselNode target =
                    inPlane == null
                            ? extend(network, sprout, field, support, ds, dt, random)
                            : aim(network, sprout, field, support, ds, dt, random);
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
        int chosen = choose(network, sprout, p, step, field, support, ds, random);
        if (chosen < 0) {
            return null;
        }

        double[] u = UNIT_DIRECTIONS[chosen];
        double[] q = {p[0] + step * u[0], p[1] + step * u[1], p[2] + step * u[2]};
        PottsVesselNode node = network.addNode(q[0], q[1], q[2], Root.NONE);
        network.addSegment(sprout.tip, node, sproutIntegrity);
        sprout.tip = node;
        sprout.heading = UNIT_DIRECTIONS[chosen];
        sprout.length += step;
        sprout.excluded.add(node);
        return node;
    }

    /**
     * Chooses the direction of a step from a point (R2, R3).
     *
     * @param network the vessel network
     * @param sprout the sprout
     * @param p the point the step starts from [um]
     * @param step the step length [um]
     * @param field the VEGF values
     * @param support the support map
     * @param ds the voxel size [um]
     * @param random the random number generator
     * @return the index of the chosen direction in {@link #UNIT_DIRECTIONS}, or -1 if no direction
     *     is allowed
     */
    int choose(
            PottsVesselNetwork network,
            Sprout sprout,
            double[] p,
            double step,
            double[][] field,
            boolean[][] support,
            double ds,
            MersenneTwisterFast random) {
        double vp = value(field, ds, p);
        double width = field.length * ds;
        double height = field[0].length * ds;

        double[] scores = new double[DIRECTIONS];
        double[][] targets = new double[DIRECTIONS][];
        double best = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < DIRECTIONS; k++) {
            double[] u = UNIT_DIRECTIONS[k];
            double[] q = {p[0] + step * u[0], p[1] + step * u[1], p[2] + step * u[2]};
            scores[k] = Double.NEGATIVE_INFINITY;
            if (!allowed(network, q, width, height, support, ds)) {
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
            return -1;
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
        return chosen;
    }

    /**
     * Checks if a step may end at a point (R3): inside the slice in x and y, within {@code
     * D_SUPPORT} of a supporting cell, and not at an existing node.
     *
     * @param network the vessel network
     * @param q the point [um]
     * @param width the slice width along x [um]
     * @param height the slice height along y [um]
     * @param support the support map
     * @param ds the voxel size [um]
     * @return {@code true} if allowed, {@code false} otherwise
     */
    private boolean allowed(
            PottsVesselNetwork network,
            double[] q,
            double width,
            double height,
            boolean[][] support,
            double ds) {
        if (q[0] < 0 || q[0] >= width || q[1] < 0 || q[1] >= height) {
            return false;
        }
        if (!isSupported(support, ds, q, supportDistance)) {
            return false;
        }
        return network.findNode(q[0], q[1], q[2]) == null;
    }

    /**
     * Aims a coupled tip (R2, R3), or extends it if its proposed segment is out of the plane.
     *
     * <p>The direction is chosen once, from the tip's front (at the tip's z) if it has one, or from
     * the tip. A proposal in the plane (from the front, or from the tip along the same direction)
     * becomes the tip's aim ({@link #propose}) and no segment is added. Otherwise the tip extends
     * out of the plane exactly as in R3: one step of {@code V_TIP} dt from the tip along the chosen
     * direction, if that end is allowed (in the slice, supported, not at a node); if not, it stalls
     * this update. A tip with no allowed direction keeps its aim.
     *
     * @param network the vessel network
     * @param sprout the sprout
     * @param field the VEGF values
     * @param support the support map
     * @param ds the voxel size [um]
     * @param dt the update interval [hours]
     * @param random the random number generator
     * @return the new tip node if the tip extended out of the plane, or {@code null} otherwise
     */
    PottsVesselNode aim(
            PottsVesselNetwork network,
            Sprout sprout,
            double[][] field,
            boolean[][] support,
            double ds,
            double dt,
            MersenneTwisterFast random) {
        double step = Math.min(speed * dt, maxLength - sprout.length);
        double[] t = sprout.tip.getPosition();
        double[] p =
                sprout.front == null ? t : new double[] {sprout.front[0], sprout.front[1], t[2]};
        int chosen = choose(network, sprout, p, step, field, support, ds, random);
        if (chosen < 0) {
            return null;
        }

        double[] u = UNIT_DIRECTIONS[chosen];
        double[] q = {p[0] + step * u[0], p[1] + step * u[1], p[2] + step * u[2]};
        // The segment that would be added out of the plane starts at the tip, as in R3.
        double[] r =
                p == t
                        ? q
                        : new double[] {t[0] + step * u[0], t[1] + step * u[1], t[2] + step * u[2]};
        if (inPlane.test(p, q) || (r != q && inPlane.test(t, r))) {
            propose(sprout, p, u, step);
            return null;
        }

        // Out of the plane: extend from the tip as without coupling, if the end is allowed there.
        if (r != q && !allowed(network, r, field.length * ds, field[0].length * ds, support, ds)) {
            return null;
        }
        PottsVesselNode node = network.addNode(r[0], r[1], r[2], Root.NONE);
        network.addSegment(sprout.tip, node, sproutIntegrity);
        sprout.length += step;
        sprout.tip = node;
        sprout.heading = u;
        sprout.excluded.add(node);
        sprout.front = null;
        sprout.aimStart = null;
        sprout.aimEnd = null;
        return node;
    }

    /**
     * Stores the aim of an in-plane tip: a segment from a point along a direction.
     *
     * @param sprout the sprout
     * @param start the start of the aim [um]
     * @param direction the unit direction
     * @param length the aim length [um]
     */
    void propose(Sprout sprout, double[] start, double[] direction, double length) {
        sprout.aimStart = start.clone();
        sprout.aimEnd =
                new double[] {
                    start[0] + length * direction[0],
                    start[1] + length * direction[1],
                    start[2] + length * direction[2]
                };
        sprout.heading = direction;
    }

    /**
     * Gets the point at which an aimed tip should append a segment, given its front.
     *
     * <p>Only forward progress is credited ({@link #credit}): the front's distance from the tip
     * node along the in-plane aim direction, up to {@code V_TIP} times the time since the last
     * append (or creation) and up to the aim's end. A front behind the node, or farther than {@code
     * maxOffset} to the side of the aim line, gets no credit. A segment is due when the credit is
     * at least the aim length, less a tolerance. The aim length here is shortened to the forward
     * distance from the tip node to the aim's end, if that is shorter (an aim with a z part reaches
     * less far in the plane), and to the length left before {@code L_MAX}. The point is the tip
     * node moved by the credit along the in-plane aim direction, at the z of the aim's end. An aim
     * with no in-plane part gets no point.
     *
     * @param sprout the sprout
     * @param x the front x [um]
     * @param y the front y [um]
     * @param now the current time [hours]
     * @param tolerance the tolerance on the front's position [um]
     * @param maxOffset the largest sideways offset of the front from the aim line [um]
     * @return the point {x, y, z} [um], or {@code null} if no segment is due
     */
    public double[] appendPoint(
            Sprout sprout, double x, double y, double now, double tolerance, double maxOffset) {
        if (sprout.aimEnd == null) {
            return null;
        }
        double[] t = sprout.tip.getPosition();
        double ux = sprout.aimEnd[0] - sprout.aimStart[0];
        double uy = sprout.aimEnd[1] - sprout.aimStart[1];
        double norm = Math.hypot(ux, uy);
        if (!(norm > 1e-9)) {
            return null;
        }
        ux /= norm;
        uy /= norm;
        double reach = Math.max(0, (sprout.aimEnd[0] - t[0]) * ux + (sprout.aimEnd[1] - t[1]) * uy);
        double cap = Math.min(speed * (now - sprout.lastGrowth), reach);
        double credit = credit(t, sprout.aimStart, sprout.aimEnd, x, y, cap, maxOffset);
        double due = Math.min(Math.min(getAimLength(), reach), maxLength - sprout.length);
        if (credit == 0 || !(credit >= due - tolerance - 1e-9)) {
            return null;
        }
        return new double[] {t[0] + credit * ux, t[1] + credit * uy, sprout.aimEnd[2]};
    }

    /**
     * Gets the forward credit of a tip's front.
     *
     * <p>With û the in-plane unit direction of the aim, the credit is {@code max(0, (front - node)
     * · û)}, capped at {@code cap}. A front whose sideways distance from the aim line (through the
     * aim's start along û) is more than {@code maxOffset} gets no credit, as does an aim with no
     * in-plane part.
     *
     * @param node the tip node {x, y, ...} [um]
     * @param aimStart the start of the aim {x, y, ...} [um]
     * @param aimEnd the end of the aim {x, y, ...} [um]
     * @param x the front x [um]
     * @param y the front y [um]
     * @param cap the largest credit [um]
     * @param maxOffset the largest sideways offset [um]
     * @return the credit [um]
     */
    static double credit(
            double[] node,
            double[] aimStart,
            double[] aimEnd,
            double x,
            double y,
            double cap,
            double maxOffset) {
        double ux = aimEnd[0] - aimStart[0];
        double uy = aimEnd[1] - aimStart[1];
        double norm = Math.hypot(ux, uy);
        if (!(norm > 1e-9)) {
            return 0;
        }
        ux /= norm;
        uy /= norm;
        double side = Math.abs((x - aimStart[0]) * uy - (y - aimStart[1]) * ux);
        if (side > maxOffset) {
            return 0;
        }
        double forward = (x - node[0]) * ux + (y - node[1]) * uy;
        return Math.max(0, Math.min(forward, cap));
    }

    /**
     * Appends a segment to an aimed tip at a point (R3), then checks anastomosis (R4) and the
     * length cap (R5).
     *
     * <p>The sprout's time of last growth is set, so R6 counts from now. A sprout that joins
     * another vessel is marked {@code joined} and no longer tracked. Perfusion is updated; the
     * caller refreshes the vessel component.
     *
     * @param network the vessel network
     * @param sprout the sprout
     * @param x the point x [um]
     * @param y the point y [um]
     * @param z the point z [um]
     * @param now the current time [hours]
     * @return the new tip node, or {@code null} if the sprout is not active, its tip is no longer a
     *     dead end, or a node is there
     */
    public PottsVesselNode append(
            PottsVesselNetwork network, Sprout sprout, double x, double y, double z, double now) {
        if (!sprouts.contains(sprout) || sprout.dormant || sprout.capped) {
            return null;
        }
        if (sprout.length > 0 && network.getDegree(sprout.tip) != 1) {
            return null;
        }
        if (network.findNode(x, y, z) != null) {
            return null;
        }
        double[] a = sprout.tip.getPosition();
        PottsVesselNode node = network.addNode(x, y, z, Root.NONE);
        network.addSegment(sprout.tip, node, sproutIntegrity);
        double thickness = network.getThickness();
        double[] heading = unit(PottsVesselGeometry.vector(a, node.getPosition(), thickness));
        if (heading != null) {
            sprout.heading = heading;
        }
        sprout.length += distance(a, node.getPosition(), thickness);
        sprout.tip = node;
        sprout.excluded.add(node);
        sprout.lastGrowth = now;
        if (anastomose(network, sprout)) {
            sprout.joined = true;
            sprouts.remove(sprout);
        } else if (sprout.length >= maxLength - 1e-9) {
            sprout.capped = true;
        }
        PottsVesselPerfusion.update(network);
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
