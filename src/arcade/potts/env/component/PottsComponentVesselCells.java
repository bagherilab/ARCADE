package arcade.potts.env.component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import sim.engine.Schedule;
import sim.engine.SimState;
import ec.util.MersenneTwisterFast;
import arcade.core.env.component.Component;
import arcade.core.sim.Simulation;
import arcade.core.util.MiniBox;
import arcade.potts.agent.cell.PottsCell;
import arcade.potts.env.lattice.PottsLattice;
import arcade.potts.env.location.PottsLocation;
import arcade.potts.env.location.Voxel;
import arcade.potts.sim.Potts;
import arcade.potts.sim.PottsSimulation;
import arcade.potts.sim.hamiltonian.VesselHamiltonian;
import static arcade.potts.env.component.PottsComponentVesselGrowth.Sprout;
import static arcade.potts.env.component.PottsVesselPieces.Piece;
import static arcade.potts.util.PottsEnums.Ordering;
import static arcade.potts.util.PottsEnums.Term;

/**
 * Implementation of {@link Component} for vessel cells that occupy the plane section of a vessel
 * network.
 *
 * <p>On its first step, the network of the {@code VESSELS} component is cut into pieces ({@link
 * PottsVesselPieces}), and each piece becomes one cell of the {@code POPULATION} population on the
 * voxels of the piece. The voxels are taken from the cells that own them, and a cell left with no
 * voxels is removed. Each vessel cell gets the piece size as its target volume and is anchored to
 * its piece through the {@code vessel} Hamiltonian term, with anchor strength {@code
 * LAMBDA_ANCHOR_MIN} + ({@code LAMBDA_ANCHOR_MAX} - {@code LAMBDA_ANCHOR_MIN}) I, where I is the
 * mean integrity of the segments of the piece. The outer radius of a segment is {@code R_MATURE} +
 * ({@code R_SPROUT} - {@code R_MATURE}) (1 - I).
 *
 * <p>Every tick (after the Potts step), if the network version changed, the pieces are updated
 * without recutting: a cell keeps its piece while all its segments exist and their integrities are
 * unchanged. A cell whose segments changed keeps the segments that still exist, with voxels, anchor
 * and target volume from those segments, and a cell with none left retires. Segments with no cell
 * are cut into new pieces among themselves and get new cells. A new cell never takes voxels from
 * another vessel or tip cell: it starts on the largest connected part of its piece not held by one
 * (at least {@code N_MIN} voxels), with the whole piece as its anchor and target. The same rule
 * holds on the first build, for a piece that is not connected. A retiring cell has a target volume
 * of 0 and no anchor, and is removed once it has shrunk to one voxel (the Potts step cannot take
 * the last voxel of a cell) or has not shrunk for {@link #RETIRE_TICKS} ticks (for example, when it
 * is split into pieces that cannot vanish). Then drift (a centroid farther than {@code
 * D_DRIFT_FOLD} times the outer radius from every segment of the cell) and fragmentation (a cell
 * that is not connected) are counted, and non-zero counts are logged.
 *
 * <p><b>Tips.</b> If the {@code GROWTH} component exists, it is coupled to tip cells ({@link
 * PottsComponentVesselGrowth#couple}): a tip is in the plane if the footprint of its proposed
 * segment, at the outer radius of a new sprout, is not empty. Every tick, for each active tip with
 * an in-plane aim:
 *
 * <ol>
 *   <li>A tip with no tip cell buds: {@code N_TIP0} voxels are taken ({@link #carve}) from the
 *       nearest vessel cell within {@code D_BUD_FOLD} times the outer radius of the tip's segments,
 *       on the side facing the aim, and a cell of the {@code TIP_POPULATION} population is
 *       inserted. With no such cell (or if the carve fails), the tip cell is seeded on the largest
 *       connected part, of at least {@code N_TIP0} voxels, of the aim's footprint not held by a
 *       vessel or tip cell, taken from its owners.
 *   <li>The tip cell's anchor is the footprint from the tip node to the aim's end, its guide is the
 *       footprint of the aim (weight {@code LAMBDA_GUIDE}), both without voxels of vessel cell
 *       pieces, at the anchor strength of a new sprout. Its target volume grows by {@code G_TIP}
 *       per hour since its last append (or bud), up to one hour of growth.
 *   <li>The front is the centre of the tip cell's leading end along the aim ({@link #front}). When
 *       the growth component reports a segment is due ({@link
 *       PottsComponentVesselGrowth#appendPoint}, with half a voxel of tolerance; only forward
 *       progress along the aim is credited, and a front more than the sprout's outer radius to the
 *       side of the aim gets none), it is appended there. The tip cell's voxels in the new
 *       segment's footprint, except the leading ones it needs to keep {@code N_TIP0} voxels, become
 *       a vessel cell for the new segment (if at least {@code N_MIN} voxels and connected), and the
 *       tip cell's target drops by as much.
 *   <li>If the tip joined another vessel, the tip cell is replaced by a vessel cell on the same
 *       voxels for the joining segment.
 * </ol>
 *
 * <p>Tip cells of tips that are no longer active in the plane (regressed, capped, removed, or out
 * of the plane) retire. Without the growth component there are no tip cells and the network does
 * not change here.
 *
 * <p>The slice is 2D: vessel cells lie in the plane z = 0.
 */
public class PottsComponentVesselCells implements Component {
    /** Logger for {@code PottsComponentVesselCells}. */
    private static final Logger LOGGER =
            Logger.getLogger(PottsComponentVesselCells.class.getName());

    /** Moves to the four in-plane neighbors. */
    private static final int[][] MOVES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    /** Moves to the eight in-plane neighbors, including diagonals. */
    private static final int[][] MOVES_DIAGONAL = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    /** Outer radius of a mature segment (I = 1) [um]. */
    private final double rMature;

    /** Outer radius of a sprout (I = 0) [um]. */
    private final double rSprout;

    /** Largest in-plane length of a piece [um]. */
    private final double lPiece;

    /** Smallest number of voxels of a piece or a vessel cell. */
    private final int nMin;

    /** Anchor strength at integrity 0. */
    private final double lambdaAnchorMin;

    /** Anchor strength at integrity 1. */
    private final double lambdaAnchorMax;

    /** Drift distance as a multiple of the outer radius. */
    private final double driftFold;

    /** Id of the vessel component. */
    private final String vesselsKey;

    /** Name of the vessel cell population. */
    private final String population;

    /** Name of the tip cell population. */
    private final String tipPopulation;

    /** Id of the growth component, which may be absent. */
    private final String growthKey;

    /** Guide strength of tip cells. */
    private final double lambdaGuide;

    /** Growth of the tip cell target area [um^2/hour]. */
    private final double tipGrowth;

    /** Number of voxels a tip cell starts with when it buds. */
    private final int nTip0;

    /** Bud distance as a multiple of the outer radius. */
    private final double budFold;

    /** Number of ticks a retiring cell may go without shrinking before it is removed. */
    static final int RETIRE_TICKS = 4;

    /** A tip cell and its growth bookkeeping. */
    static final class Tip {
        /** The tip cell. */
        final PottsCell cell;

        /** Target volume at the last bud or append [voxels]. */
        double base;

        /** Time of the last bud or append [hours]. */
        double since;

        /**
         * Creates a tip.
         *
         * @param cell the tip cell
         * @param base the target volume [voxels]
         * @param since the time [hours]
         */
        Tip(PottsCell cell, double base, double since) {
            this.cell = cell;
            this.base = base;
            this.since = since;
        }
    }

    /** Tip cells of sprouts, in order of budding. */
    private final Map<Sprout, Tip> tips = new LinkedHashMap<>();

    /** Integrities of the segments of each piece when it was set, by cell. */
    private final Map<PottsCell, double[]> integrities = new HashMap<>();

    /** Random number generator for connectivity checks, apart from the simulation stream. */
    private final MersenneTwisterFast checkRandom = new MersenneTwisterFast(0);

    /** Vessel cells and their pieces, in order of creation. */
    private final Map<PottsCell, Piece> pieces = new LinkedHashMap<>();

    /** Vessel cell of each segment. */
    private final Map<PottsVesselEdge, PottsCell> cells = new IdentityHashMap<>();

    /** Cells shrinking away, with their smallest volume and the ticks since it fell. */
    private final Map<PottsCell, int[]> retiring = new LinkedHashMap<>();

    /** Potts instance of the simulation, set on the first step. */
    private Potts potts;

    /** {@code true} once the vessel cells are built, {@code false} otherwise. */
    private boolean built;

    /** Network version the pieces were built for. */
    private int version;

    /** Number of vessel cells that drifted from their segments, at the last check. */
    private int driftCount;

    /** Number of vessel cells that are not connected, at the last check. */
    private int fragmentCount;

    /** Keys of all voxels of vessel cell pieces, or {@code null} if the pieces changed. */
    private Set<Long> heldKeys;

    /** Number of in-plane tip updates (one per tip cell per tick). */
    private int tipUpdates;

    /** Number of in-plane tip updates with no append for more than an hour. */
    private int tipStalls;

    /** Number of segments appended by tip cells. */
    private int tipAppends;

    /** Tip counts at the last daily log: updates, stalls, appends. */
    private final int[] tipLogged = new int[3];

    /** Time of the next daily tip log [hours], or {@code NaN} before the first tip update. */
    private double tipLogTime = Double.NaN;

    /** Interval between tip logs [hours]. */
    static final double TIP_LOG_INTERVAL = 24;

    /**
     * Creates a {@link Component} for vessel cells.
     *
     * <p>Loaded parameters include:
     *
     * <ul>
     *   <li>{@code R_MATURE} = outer radius of a mature segment [um]
     *   <li>{@code R_SPROUT} = outer radius of a sprout [um]
     *   <li>{@code L_PIECE} = largest in-plane length of a piece [um]
     *   <li>{@code N_MIN} = smallest number of voxels of a piece
     *   <li>{@code LAMBDA_ANCHOR_MIN} = anchor strength at integrity 0
     *   <li>{@code LAMBDA_ANCHOR_MAX} = anchor strength at integrity 1
     *   <li>{@code D_DRIFT_FOLD} = drift distance as a multiple of the outer radius
     *   <li>{@code VESSELS} = id of the vessel component
     *   <li>{@code POPULATION} = name of the vessel cell population
     *   <li>{@code TIP_POPULATION} = name of the tip cell population
     *   <li>{@code GROWTH} = id of the growth component (tips only if it exists)
     *   <li>{@code LAMBDA_GUIDE} = guide strength of tip cells
     *   <li>{@code G_TIP} = growth of the tip cell target area [um^2/hour]
     *   <li>{@code N_TIP0} = number of voxels a tip cell buds with
     *   <li>{@code D_BUD_FOLD} = bud distance as a multiple of the outer radius
     * </ul>
     *
     * @param parameters the component parameters dictionary
     */
    public PottsComponentVesselCells(MiniBox parameters) {
        rMature = parameters.getDouble("R_MATURE");
        rSprout = parameters.getDouble("R_SPROUT");
        lPiece = parameters.getDouble("L_PIECE");
        nMin = parameters.getInt("N_MIN");
        lambdaAnchorMin = parameters.getDouble("LAMBDA_ANCHOR_MIN");
        lambdaAnchorMax = parameters.getDouble("LAMBDA_ANCHOR_MAX");
        driftFold = parameters.getDouble("D_DRIFT_FOLD");
        vesselsKey = parameters.get("VESSELS");
        population = parameters.get("POPULATION");
        tipPopulation = parameters.get("TIP_POPULATION");
        growthKey = parameters.get("GROWTH");
        lambdaGuide = parameters.getDouble("LAMBDA_GUIDE");
        tipGrowth = parameters.getDouble("G_TIP");
        nTip0 = parameters.getInt("N_TIP0");
        budFold = parameters.getDouble("D_BUD_FOLD");
        if (!(lambdaGuide >= 0) || !(tipGrowth >= 0) || nTip0 < 1 || !(budFold >= 0)) {
            throw new IllegalArgumentException(
                    "vessel cells require LAMBDA_GUIDE, G_TIP, D_BUD_FOLD >= 0 and N_TIP0 >= 1");
        }
        if (!(rMature > 0) || !(rSprout > 0) || !(lPiece > 0) || nMin < 1 || !(driftFold > 0)) {
            throw new IllegalArgumentException(
                    "vessel cells require R_MATURE, R_SPROUT, L_PIECE, D_DRIFT_FOLD > 0"
                            + " and N_MIN >= 1");
        }
        if (!(lambdaAnchorMin >= 0) || !(lambdaAnchorMax >= 0)) {
            throw new IllegalArgumentException(
                    "vessel cells require LAMBDA_ANCHOR_MIN and LAMBDA_ANCHOR_MAX >= 0");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The component steps every tick at the components ordering. It also couples the growth
     * component, if there is one, once at the start of the run (time 0, potts ordering, before any
     * other step), so that no growth update runs uncoupled.
     */
    @Override
    public void schedule(Schedule schedule) {
        schedule.scheduleRepeating(this, Ordering.COMPONENTS.ordinal(), 1);
        schedule.scheduleOnce(Schedule.EPOCH, Ordering.POTTS.ordinal(), this::coupleGrowth);
    }

    /**
     * Couples the growth component to tip cells, if it exists and is not coupled yet.
     *
     * @param simstate the simulation instance
     */
    void coupleGrowth(SimState simstate) {
        PottsSimulation sim = (PottsSimulation) simstate;
        PottsComponentVesselGrowth growth = getGrowth(sim);
        if (growth != null && !growth.isCoupled()) {
            couple(sim, getVessels(sim), growth);
        }
    }

    @Override
    public void register(Simulation sim, String layer) {}

    /**
     * Gets the outer radius of a segment.
     *
     * @param segment the segment
     * @return the outer radius [um]
     */
    public double rOut(PottsVesselEdge segment) {
        return rMature + (rSprout - rMature) * (1 - segment.getIntegrity());
    }

    /**
     * Gets the name of the vessel cell population.
     *
     * @return the population name
     */
    public String getPopulation() {
        return population;
    }

    /**
     * Gets the name of the tip cell population.
     *
     * @return the tip population name (the population may not exist)
     */
    public String getTipPopulation() {
        return tipPopulation;
    }

    /**
     * Gets the vessel cell of a segment.
     *
     * @param segment the segment
     * @return the vessel cell, or {@code null} if the segment has none
     */
    public PottsCell getCell(PottsVesselEdge segment) {
        return cells.get(segment);
    }

    /**
     * Gets the vessel cells and their pieces.
     *
     * @return an unmodifiable view of the vessel cells and their pieces, in order of creation
     */
    public Map<PottsCell, Piece> getPieces() {
        return Collections.unmodifiableMap(pieces);
    }

    /**
     * Gets the tip cell of a sprout.
     *
     * @param sprout the sprout
     * @return the tip cell, or {@code null} if the sprout has none
     */
    PottsCell getTipCell(Sprout sprout) {
        Tip tip = tips.get(sprout);
        return tip == null ? null : tip.cell;
    }

    /**
     * Gets the cells shrinking away.
     *
     * @return an unmodifiable view of the retiring cells
     */
    Set<PottsCell> getRetiring() {
        return Collections.unmodifiableSet(retiring.keySet());
    }

    /**
     * Gets the number of vessel cells that drifted from their segments, at the last check.
     *
     * @return the number of drifted cells
     */
    public int getDriftCount() {
        return driftCount;
    }

    /**
     * Gets the number of vessel cells that are not connected, at the last check.
     *
     * @return the number of fragmented cells
     */
    public int getFragmentCount() {
        return fragmentCount;
    }

    /**
     * Gets the tip counts since the start: in-plane tip updates (one per tip cell per tick),
     * stalled updates (no append for more than an hour), and appends.
     *
     * @return the updates, stalls, and appends
     */
    public int[] getTipCounts() {
        return new int[] {tipUpdates, tipStalls, tipAppends};
    }

    /**
     * Gets the vessel component.
     *
     * @param sim the simulation instance
     * @return the vessel component
     */
    private PottsComponentVessels getVessels(Simulation sim) {
        Component component = sim.getComponent(vesselsKey);
        if (!(component instanceof PottsComponentVessels)) {
            throw new IllegalStateException(
                    "vessel cells require a vessels component [ " + vesselsKey + " ]");
        }
        return (PottsComponentVessels) component;
    }

    /**
     * Gets the vessel Hamiltonian term.
     *
     * @param model the potts instance
     * @return the vessel Hamiltonian term
     */
    private static VesselHamiltonian getHamiltonian(Potts model) {
        VesselHamiltonian hamiltonian = (VesselHamiltonian) model.getHamiltonian(Term.VESSEL);
        if (hamiltonian == null) {
            throw new IllegalStateException(
                    "vessel cells require the vessel Hamiltonian term"
                            + " (<potts.term id=\"VESSEL\" />)");
        }
        return hamiltonian;
    }

    /**
     * Gets the code of a population.
     *
     * @param sim the simulation instance
     * @param name the population name
     * @param required {@code true} if the population must exist, {@code false} otherwise
     * @return the population code, or -1 if the population does not exist and is not required
     */
    private static int getPop(Simulation sim, String name, boolean required) {
        MiniBox box = name == null ? null : sim.getSeries().populations.get(name);
        if (box == null) {
            if (required) {
                throw new IllegalStateException(
                        "vessel cells require the population [ " + name + " ]");
            }
            return -1;
        }
        return box.getInt("CODE");
    }

    /**
     * Gets the growth component.
     *
     * @param sim the simulation instance
     * @return the growth component, or {@code null} if there is none
     */
    private PottsComponentVesselGrowth getGrowth(Simulation sim) {
        Component component = growthKey == null ? null : sim.getComponent(growthKey);
        return component instanceof PottsComponentVesselGrowth
                ? (PottsComponentVesselGrowth) component
                : null;
    }

    @Override
    public void step(SimState simstate) {
        PottsSimulation sim = (PottsSimulation) simstate;
        PottsComponentVessels vessels = getVessels(sim);
        potts = sim.getPotts();
        VesselHamiltonian hamiltonian = getHamiltonian(potts);
        PottsComponentVesselGrowth growth = getGrowth(sim);

        if (!built) {
            build(sim, vessels, hamiltonian);
        } else if (vessels.getVersion() != version) {
            rebuild(sim, vessels, hamiltonian);
        }

        if (growth != null) {
            if (!growth.isCoupled()) {
                couple(sim, vessels, growth); // components stepped without the schedule
            }
            if (tips(sim, vessels, hamiltonian, growth)) {
                vessels.refresh();
                rebuild(sim, vessels, hamiltonian);
            }
        }

        // Remove shrinking cells that are down to their last voxel or no longer shrink.
        for (PottsCell cell : new ArrayList<>(retiring.keySet())) {
            int volume = (int) cell.getLocation().getVolume();
            int[] state = retiring.get(cell);
            if (volume < state[0]) {
                state[0] = volume;
                state[1] = 0;
            } else {
                state[1]++;
            }
            if (volume <= 1 || state[1] >= RETIRE_TICKS) {
                removeCell(sim, cell);
            }
        }

        check(vessels.getNetwork().getThickness(), vessels.getReferenceLattice(sim).getDs());
    }

    /**
     * Builds the vessel cells on the first step.
     *
     * @param sim the simulation instance
     * @param vessels the vessel component
     * @param hamiltonian the vessel Hamiltonian term
     */
    private void build(
            PottsSimulation sim, PottsComponentVessels vessels, VesselHamiltonian hamiltonian) {
        int pop = getPop(sim, population, true);
        int tipPop = getPop(sim, tipPopulation, false);
        for (Object object : sim.getGrid().getAllObjects()) {
            int cellPop = ((PottsCell) object).getPop();
            if (cellPop == pop || cellPop == tipPop) {
                throw new IllegalStateException(
                        "vessel cells already exist; vessel cells are built only on a state"
                                + " without them");
            }
        }

        for (Piece piece : buildPieces(sim, vessels, null)) {
            addCell(sim, hamiltonian, pop, piece);
        }
        built = true;
        version = vessels.getVersion();
    }

    /**
     * Updates the pieces after the network changed, keeping the pieces of unchanged segments.
     *
     * @param sim the simulation instance
     * @param vessels the vessel component
     * @param hamiltonian the vessel Hamiltonian term
     */
    private void rebuild(
            PottsSimulation sim, PottsComponentVessels vessels, VesselHamiltonian hamiltonian) {
        int pop = getPop(sim, population, true);
        PottsVesselNetwork network = vessels.getNetwork();
        PottsLattice lattice = vessels.getReferenceLattice(sim);
        Map<PottsVesselEdge, List<int[]>> owned = null;

        for (PottsCell cell : new ArrayList<>(pieces.keySet())) {
            Piece old = pieces.get(cell);
            List<PottsVesselEdge> kept = new ArrayList<>();
            for (PottsVesselEdge segment : old.segments) {
                if (network.contains(segment)) {
                    kept.add(segment);
                }
            }
            if (kept.size() == old.segments.size()
                    && java.util.Arrays.equals(integrities.get(cell), integritiesOf(kept))) {
                continue;
            }
            if (owned == null) {
                owned =
                        PottsVesselPieces.owned(
                                network,
                                this::rOut,
                                lattice.getDs(),
                                lattice.getLength(),
                                lattice.getWidth());
            }
            Piece piece = new Piece();
            piece.segments.addAll(kept);
            for (PottsVesselEdge segment : kept) {
                piece.voxels.addAll(owned.getOrDefault(segment, List.of()));
            }
            piece.voxels.sort(
                    (u, w) ->
                            u[0] != w[0]
                                    ? Integer.compare(u[0], w[0])
                                    : Integer.compare(u[1], w[1]));
            if (piece.voxels.isEmpty()) {
                retire(cell, hamiltonian);
                continue;
            }
            if (!sameVoxels(old, piece)) {
                cell.setTargets(piece.voxels.size(), cell.getLocation().getSurface());
            }
            hamiltonian.setAnchor(cell.getID(), keysOf(piece), anchorOf(piece));
            track(cell, piece);
        }

        // Rebuild the segment map, then give segments without a cell new pieces.
        cells.clear();
        for (Map.Entry<PottsCell, Piece> entry : pieces.entrySet()) {
            for (PottsVesselEdge segment : entry.getValue().segments) {
                cells.put(segment, entry.getKey());
            }
        }
        Set<PottsVesselEdge> uncovered = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PottsVesselEdge segment : network.getSegments()) {
            if (!cells.containsKey(segment)) {
                uncovered.add(segment);
            }
        }
        if (!uncovered.isEmpty()) {
            for (Piece piece : buildPieces(sim, vessels, uncovered)) {
                addCell(sim, hamiltonian, pop, piece);
            }
        }
        version = vessels.getVersion();
    }

    /**
     * Gets the integrities of segments.
     *
     * @param segments the segments
     * @return the integrities
     */
    private static double[] integritiesOf(List<PottsVesselEdge> segments) {
        double[] values = new double[segments.size()];
        for (int k = 0; k < values.length; k++) {
            values[k] = segments.get(k).getIntegrity();
        }
        return values;
    }

    /**
     * Builds the pieces of the current network.
     *
     * @param sim the simulation instance
     * @param vessels the vessel component
     * @param include the segments to build pieces of, or {@code null} for all
     * @return the pieces
     */
    private List<Piece> buildPieces(
            PottsSimulation sim, PottsComponentVessels vessels, Set<PottsVesselEdge> include) {
        PottsVesselNetwork network = vessels.getNetwork();
        if (network == null) {
            vessels.prepare(sim);
            network = vessels.getNetwork();
        }
        PottsLattice lattice = vessels.getReferenceLattice(sim);
        return PottsVesselPieces.build(
                network,
                this::rOut,
                lPiece,
                nMin,
                lattice.getDs(),
                lattice.getLength(),
                lattice.getWidth(),
                include);
    }

    /**
     * Checks if a cell is a vessel or tip cell.
     *
     * @param cell the cell
     * @return {@code true} if the cell is a vessel or tip cell, {@code false} otherwise
     */
    private boolean isVessel(PottsCell cell) {
        return pieces.containsKey(cell) || retiring.containsKey(cell) || isTip(cell);
    }

    /**
     * Checks if a cell is a tip cell.
     *
     * @param cell the cell
     * @return {@code true} if the cell is a tip cell, {@code false} otherwise
     */
    private boolean isTip(PottsCell cell) {
        for (Tip tip : tips.values()) {
            if (tip.cell == cell) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a vessel cell on the voxels of a piece, taking them from their owners.
     *
     * <p>Voxels held by vessel or tip cells are not taken. If the piece is not connected (on the
     * first build or later, for example a piece of new segments whose voxels are split by an
     * existing vessel), or if some of its voxels are held, the cell starts on the largest connected
     * part of the voxels not held, if it has at least {@code N_MIN} voxels. The anchor and target
     * are the whole piece.
     *
     * @param sim the simulation instance
     * @param hamiltonian the vessel Hamiltonian term
     * @param pop the vessel population code
     * @param piece the piece
     */
    private void addCell(PottsSimulation sim, VesselHamiltonian hamiltonian, int pop, Piece piece) {
        ArrayList<Voxel> voxels = new ArrayList<>();
        for (int[] v : piece.voxels) {
            voxels.add(new Voxel(v[0], v[1], 0));
        }
        boolean connected = isConnected(new HashSet<>(voxels));
        if (!connected) {
            LOGGER.fine(
                    "vessel piece of "
                            + voxels.size()
                            + " voxels is not connected; cell on its largest part");
        }

        // Leave voxels held by vessel and tip cells.
        ArrayList<Voxel> free = new ArrayList<>();
        for (Voxel v : voxels) {
            int id = potts.ids[v.z][v.x][v.y];
            if (id == 0 || !isVessel(potts.getCell(id))) {
                free.add(v);
            }
        }
        if (free.size() < voxels.size() || !connected) {
            free = largestComponent(free);
            if (free.size() < nMin) {
                LOGGER.fine("vessel piece of " + voxels.size() + " voxels is held; no cell");
                return;
            }
        }

        take(sim, free);
        PottsCell cell = sim.insertCell(pop, free);
        if (cell == null) {
            throw new IllegalStateException("unable to insert vessel cell");
        }
        cell.setTargets(voxels.size(), cell.getLocation().getSurface());
        hamiltonian.setVessel(cell.getID());
        hamiltonian.setAnchor(cell.getID(), keysOf(piece), anchorOf(piece));
        track(cell, piece);
    }

    /**
     * Takes voxels from their owners, removing owners left with no voxels.
     *
     * <p>An owner left in more than one piece keeps its largest connected part; its other voxels
     * are released to the medium. A cell in pieces never finishes {@code PottsLocation.split} when
     * it divides.
     *
     * @param sim the simulation instance
     * @param voxels the voxels
     */
    private void take(PottsSimulation sim, List<Voxel> voxels) {
        Set<PottsCell> owners = new LinkedHashSet<>();
        for (Voxel v : voxels) {
            int id = potts.ids[v.z][v.x][v.y];
            if (id == 0) {
                continue;
            }
            PottsCell owner = potts.getCell(id);
            release(owner, v);
            owners.add(owner);
        }
        for (PottsCell owner : owners) {
            if (owner.getLocation().getVolume() == 0) {
                removeCell(sim, owner);
                continue;
            }
            ArrayList<Voxel> left = ((PottsLocation) owner.getLocation()).getVoxels();
            if (!isConnected(new HashSet<>(left))) {
                Set<Voxel> keep = new HashSet<>(largestComponent(left));
                for (Voxel v : left) {
                    if (!keep.contains(v)) {
                        release(owner, v);
                    }
                }
            }
        }
    }

    /**
     * Releases one voxel of a cell to the medium.
     *
     * @param owner the cell
     * @param v the voxel
     */
    private void release(PottsCell owner, Voxel v) {
        ((PottsLocation) owner.getLocation()).remove(v.x, v.y, v.z);
        potts.ids[v.z][v.x][v.y] = 0;
        potts.regions[v.z][v.x][v.y] = 0;
    }

    /**
     * Gets the largest connected subset of voxels; ties go to the subset found first.
     *
     * @param voxels the voxels
     * @return the largest connected subset, in the order of the voxels
     */
    static ArrayList<Voxel> largestComponent(List<Voxel> voxels) {
        Set<Voxel> all = new HashSet<>(voxels);
        Set<Voxel> seen = new HashSet<>();
        Set<Voxel> best = new HashSet<>();
        for (Voxel start : voxels) {
            if (!seen.add(start)) {
                continue;
            }
            Set<Voxel> component = new HashSet<>();
            component.add(start);
            ArrayDeque<Voxel> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                Voxel v = queue.poll();
                for (int[] m : MOVES) {
                    Voxel n = new Voxel(v.x + m[0], v.y + m[1], v.z);
                    if (all.contains(n) && seen.add(n)) {
                        component.add(n);
                        queue.add(n);
                    }
                }
            }
            if (component.size() > best.size()) {
                best = component;
            }
        }
        ArrayList<Voxel> result = new ArrayList<>();
        for (Voxel v : voxels) {
            if (best.contains(v)) {
                result.add(v);
            }
        }
        return result;
    }

    /**
     * Gets the voxels of a set connected to given voxels.
     *
     * @param seeds the voxels to start from
     * @param set the set
     * @return the voxels of the set connected to the seeds (including the seeds in the set)
     */
    static Set<Voxel> connectedTo(List<Voxel> seeds, Set<Voxel> set) {
        Set<Voxel> seen = new HashSet<>();
        ArrayDeque<Voxel> queue = new ArrayDeque<>();
        for (Voxel seed : seeds) {
            if (set.contains(seed) && seen.add(seed)) {
                queue.add(seed);
            }
        }
        while (!queue.isEmpty()) {
            Voxel v = queue.poll();
            for (int[] m : MOVES) {
                Voxel n = new Voxel(v.x + m[0], v.y + m[1], v.z);
                if (set.contains(n) && seen.add(n)) {
                    queue.add(n);
                }
            }
        }
        return seen;
    }

    /**
     * Records a vessel cell and its piece.
     *
     * @param cell the vessel cell
     * @param piece the piece
     */
    private void track(PottsCell cell, Piece piece) {
        pieces.put(cell, piece);
        heldKeys = null;
        integrities.put(cell, integritiesOf(piece.segments));
        for (PottsVesselEdge segment : piece.segments) {
            cells.put(segment, cell);
        }
    }

    /**
     * Retires a cell: no piece, no anchor, a target volume of 0.
     *
     * @param cell the cell
     * @param hamiltonian the vessel Hamiltonian term
     */
    private void retire(PottsCell cell, VesselHamiltonian hamiltonian) {
        Piece piece = pieces.remove(cell);
        heldKeys = null;
        integrities.remove(cell);
        if (piece != null) {
            for (PottsVesselEdge segment : piece.segments) {
                cells.remove(segment, cell);
            }
        }
        cell.setTargets(0, 0);
        hamiltonian.clear(cell.getID());
        retiring.put(cell, new int[] {(int) cell.getLocation().getVolume(), 0});
    }

    /**
     * Removes a cell from the lattice, the grid, the Hamiltonian, and the schedule.
     *
     * @param sim the simulation instance
     * @param cell the cell
     */
    private void removeCell(PottsSimulation sim, PottsCell cell) {
        ((PottsLocation) cell.getLocation()).clear(potts.ids, potts.regions);
        sim.getGrid().removeObject(cell, null);
        potts.deregister(cell);
        cell.stop();

        VesselHamiltonian hamiltonian = (VesselHamiltonian) potts.getHamiltonian(Term.VESSEL);
        if (hamiltonian != null) {
            hamiltonian.forget(cell.getID());
        }
        retiring.remove(cell);
        integrities.remove(cell);
        tips.values().removeIf(tip -> tip.cell == cell);
        Piece piece = pieces.remove(cell);
        heldKeys = null;
        if (piece != null) {
            for (PottsVesselEdge segment : piece.segments) {
                cells.remove(segment, cell);
            }
        }
    }

    /**
     * Couples the growth component to tip cells: a proposed segment is in the plane if its
     * footprint, at the outer radius of a new sprout, is not empty.
     *
     * @param sim the simulation instance
     * @param vessels the vessel component
     * @param growth the growth component
     */
    private void couple(
            PottsSimulation sim, PottsComponentVessels vessels, PottsComponentVesselGrowth growth) {
        PottsLattice lattice = vessels.getReferenceLattice(sim);
        double ds = lattice.getDs();
        int nx = lattice.getLength();
        int ny = lattice.getWidth();
        double r = sproutRadius(growth);
        growth.couple(
                (a, b) ->
                        !PottsVesselFootprint.of(
                                        a, b, r, ds, vessels.getNetwork().getThickness(), nx, ny)
                                .isEmpty());
    }

    /**
     * Gets the outer radius of a new sprout segment.
     *
     * @param growth the growth component
     * @return the outer radius [um]
     */
    private double sproutRadius(PottsComponentVesselGrowth growth) {
        return rMature + (rSprout - rMature) * (1 - growth.getSproutIntegrity());
    }

    /**
     * Runs the tip cells of the in-plane tips for one tick.
     *
     * @param sim the simulation instance
     * @param vessels the vessel component
     * @param hamiltonian the vessel Hamiltonian term
     * @param growth the growth component
     * @return {@code true} if the network changed, {@code false} otherwise
     */
    private boolean tips(
            PottsSimulation sim,
            PottsComponentVessels vessels,
            VesselHamiltonian hamiltonian,
            PottsComponentVesselGrowth growth) {
        PottsVesselNetwork network = vessels.getNetwork();
        PottsLattice lattice = vessels.getReferenceLattice(sim);
        double ds = lattice.getDs();
        double now = sim.schedule.getTime() * lattice.getDt();
        boolean changed = false;

        List<Sprout> active = growth.getActiveInPlaneSprouts();
        Set<Sprout> current = Collections.newSetFromMap(new IdentityHashMap<>());
        current.addAll(active);

        // Tip cells of tips no longer active in the plane retire.
        for (Map.Entry<Sprout, Tip> entry : new ArrayList<>(tips.entrySet())) {
            if (!current.contains(entry.getKey())) {
                tips.remove(entry.getKey());
                retire(entry.getValue().cell, hamiltonian);
            }
        }

        for (Sprout sprout : active) {
            Tip tip = tips.get(sprout);
            if (tip == null) {
                tip = bud(sim, network, growth, sprout, ds, now);
                if (tip == null) {
                    continue;
                }
                tips.put(sprout, tip);
            }
            steer(tip, sprout, network, hamiltonian, growth, lattice, now);
            tipUpdates++;

            double[] front = front(tip.cell, sprout, ds, sproutRadius(growth));
            growth.setFront(sprout, front[0], front[1]);
            double[] point =
                    growth.appendPoint(
                            sprout, front[0], front[1], now, ds / 2, sproutRadius(growth));
            PottsVesselNode from = sprout.tip;
            PottsVesselNode node =
                    point == null
                            ? null
                            : growth.append(network, sprout, point[0], point[1], point[2], now);
            if (node == null) {
                if (now - tip.since > PottsComponentVesselGrowth.AIM_TIME) {
                    tipStalls++;
                }
                continue;
            }
            tipAppends++;
            changed = true;
            PottsVesselEdge segment = segmentBetween(network, from, node);
            stalk(sim, tip, segment, lattice, network.getThickness(), hamiltonian);
            tip.since = now;
            if (sprout.joined) {
                tips.remove(sprout);
                join(sim, tip, network, node, segment, lattice, hamiltonian);
            }
        }
        logTips(now);
        return changed;
    }

    /**
     * Logs the tip counts of the last day: in-plane tip updates, stalled updates (no append for
     * more than an hour), and appends.
     *
     * @param now the current time [hours]
     */
    private void logTips(double now) {
        if (Double.isNaN(tipLogTime)) {
            tipLogTime = now + TIP_LOG_INTERVAL;
        }
        if (now < tipLogTime) {
            return;
        }
        int updates = tipUpdates - tipLogged[0];
        int stalls = tipStalls - tipLogged[1];
        int appends = tipAppends - tipLogged[2];
        LOGGER.info(
                String.format(
                        "vessel tips (day to %.0f h): %d in-plane tip updates, %d stalled"
                                + " (no append for over 1 h, fraction %.3f), %d appends",
                        now,
                        updates,
                        stalls,
                        updates == 0 ? 0.0 : (double) stalls / updates,
                        appends));
        tipLogged[0] = tipUpdates;
        tipLogged[1] = tipStalls;
        tipLogged[2] = tipAppends;
        tipLogTime += TIP_LOG_INTERVAL;
    }

    /**
     * Gets the segment between two nodes.
     *
     * @param network the network
     * @param a one node
     * @param b the other node
     * @return the segment
     */
    private static PottsVesselEdge segmentBetween(
            PottsVesselNetwork network, PottsVesselNode a, PottsVesselNode b) {
        for (PottsVesselEdge segment : network.getSegments(b)) {
            if (network.getOther(segment, b) == a) {
                return segment;
            }
        }
        throw new IllegalStateException("appended segment not found");
    }

    /**
     * Buds a tip cell for a sprout, from the nearest vessel cell or, failing that, by seeding.
     *
     * @param sim the simulation instance
     * @param network the network
     * @param growth the growth component
     * @param sprout the sprout
     * @param ds the voxel size [um]
     * @param now the current time [hours]
     * @return the tip, or {@code null} if no tip cell could be made
     */
    private Tip bud(
            PottsSimulation sim,
            PottsVesselNetwork network,
            PottsComponentVesselGrowth growth,
            Sprout sprout,
            double ds,
            double now) {
        double[] t = sprout.tip.getPosition();
        double thickness = network.getThickness();
        double radius = sproutRadius(growth);
        for (PottsVesselEdge segment : network.getSegments(sprout.tip)) {
            radius = Math.max(radius, rOut(segment));
        }
        double reach = budFold * radius;

        // Nearest vessel cell within the bud distance of the tip node.
        PottsCell parent = null;
        double nearest = Double.POSITIVE_INFINITY;
        for (PottsCell cell : pieces.keySet()) {
            for (Voxel v : ((PottsLocation) cell.getLocation()).getVoxels()) {
                double dx = (v.x + 0.5) * ds - t[0];
                double dy = (v.y + 0.5) * ds - t[1];
                double dz = PottsVesselGeometry.minimumImage(-t[2], thickness);
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d < nearest) {
                    nearest = d;
                    parent = cell;
                }
            }
        }

        List<Voxel> voxels = null;
        if (parent != null && nearest <= reach) {
            double[] towards = {sprout.aimEnd[0] / ds - 0.5, sprout.aimEnd[1] / ds - 0.5};
            voxels = carve(parent, nTip0, towards, sim.random);
        }
        if (voxels == null) {
            voxels = seed(sim, sprout, network, growth, ds);
        }
        if (voxels == null) {
            return null;
        }
        PottsCell cell = sim.insertCell(getPop(sim, tipPopulation, true), new ArrayList<>(voxels));
        if (cell == null) {
            throw new IllegalStateException("unable to insert tip cell");
        }
        cell.setTargets(voxels.size(), cell.getLocation().getSurface());
        getHamiltonian(potts).setTip(cell.getID());
        return new Tip(cell, voxels.size(), now);
    }

    /**
     * Seeds a tip cell on the footprint of a sprout's aim, taking the voxels from their owners.
     *
     * @param sim the simulation instance
     * @param sprout the sprout
     * @param network the network
     * @param growth the growth component
     * @param ds the voxel size [um]
     * @return the emptied voxels, or {@code null} if fewer than {@code N_TIP0} are available
     */
    private List<Voxel> seed(
            PottsSimulation sim,
            Sprout sprout,
            PottsVesselNetwork network,
            PottsComponentVesselGrowth growth,
            double ds) {
        int[][] ids = potts.ids[0];
        List<int[]> footprint =
                PottsVesselFootprint.of(
                        sprout.aimStart,
                        sprout.aimEnd,
                        sproutRadius(growth),
                        ds,
                        network.getThickness(),
                        ids.length,
                        ids[0].length);
        ArrayList<Voxel> free = new ArrayList<>();
        for (int[] v : footprint) {
            int id = ids[v[0]][v[1]];
            if (id == 0 || !isVessel(potts.getCell(id))) {
                free.add(new Voxel(v[0], v[1], 0));
            }
        }
        free = largestComponent(free);
        if (free.size() < nTip0) {
            return null;
        }
        take(sim, free);
        return free;
    }

    /**
     * Sets the anchor, guide and target volume of a tip cell.
     *
     * @param tip the tip
     * @param sprout the sprout
     * @param network the network
     * @param hamiltonian the vessel Hamiltonian term
     * @param growth the growth component
     * @param lattice the reference lattice
     * @param now the current time [hours]
     */
    private void steer(
            Tip tip,
            Sprout sprout,
            PottsVesselNetwork network,
            VesselHamiltonian hamiltonian,
            PottsComponentVesselGrowth growth,
            PottsLattice lattice,
            double now) {
        double ds = lattice.getDs();
        double r = sproutRadius(growth);
        double thickness = network.getThickness();
        int nx = lattice.getLength();
        int ny = lattice.getWidth();
        Set<Long> held = pieceKeys();

        Set<Long> guide = new HashSet<>();
        for (int[] v :
                PottsVesselFootprint.of(sprout.aimStart, sprout.aimEnd, r, ds, thickness, nx, ny)) {
            guide.add(VesselHamiltonian.key(v[0], v[1]));
        }
        guide.removeAll(held);
        Set<Long> anchor = new HashSet<>(guide);
        for (int[] v :
                PottsVesselFootprint.of(
                        sprout.tip.getPosition(), sprout.aimEnd, r, ds, thickness, nx, ny)) {
            anchor.add(VesselHamiltonian.key(v[0], v[1]));
        }
        anchor.removeAll(held);

        double integrity = growth.getSproutIntegrity();
        int id = tip.cell.getID();
        hamiltonian.setAnchor(
                id, anchor, lambdaAnchorMin + (lambdaAnchorMax - lambdaAnchorMin) * integrity);
        hamiltonian.setGuide(id, guide, lambdaGuide);

        double hours = Math.max(0, Math.min(now - tip.since, PottsComponentVesselGrowth.AIM_TIME));
        double target = tip.base + tipGrowth / (ds * ds) * hours;
        tip.cell.setTargets(target, tip.cell.getLocation().getSurface());
    }

    /**
     * Gets the keys of all voxels of vessel cell pieces.
     *
     * <p>The keys are computed once and kept until the pieces change.
     *
     * @return the keys (not modifiable)
     */
    private Set<Long> pieceKeys() {
        if (heldKeys == null) {
            Set<Long> keys = new HashSet<>();
            for (Piece piece : pieces.values()) {
                keys.addAll(keysOf(piece));
            }
            heldKeys = Collections.unmodifiableSet(keys);
        }
        return heldKeys;
    }

    /**
     * Gets the front of a tip cell: the centre of its leading end along the aim.
     *
     * <p>The leading layer is the tip cell voxels within half a voxel of the farthest along the
     * aim. The front is the mean position of the leading layer, moved back along the aim by the
     * sprout radius less half a voxel (the leading layer of a filled footprint lies that far beyond
     * the end of its axis), but not behind the projection of the cell's centroid. With a vertical
     * aim, the front is the centroid.
     *
     * @param cell the tip cell
     * @param sprout the sprout
     * @param ds the voxel size [um]
     * @param radius the sprout radius [um]
     * @return the front {x, y} [um]
     */
    static double[] front(PottsCell cell, Sprout sprout, double ds, double radius) {
        double ux = sprout.aimEnd[0] - sprout.aimStart[0];
        double uy = sprout.aimEnd[1] - sprout.aimStart[1];
        double norm = Math.hypot(ux, uy);
        List<Voxel> voxels = ((PottsLocation) cell.getLocation()).getVoxels();
        int n = voxels.size();
        double[] xs = new double[n];
        double[] ys = new double[n];
        double[] projections = new double[n];
        double max = Double.NEGATIVE_INFINITY;
        double cx = 0;
        double cy = 0;
        for (int k = 0; k < n; k++) {
            xs[k] = (voxels.get(k).x + 0.5) * ds;
            ys[k] = (voxels.get(k).y + 0.5) * ds;
            cx += xs[k] / n;
            cy += ys[k] / n;
            projections[k] = norm == 0 ? 0 : (xs[k] * ux + ys[k] * uy) / norm;
            max = Math.max(max, projections[k]);
        }
        if (norm == 0) {
            return new double[] {cx, cy};
        }
        double x = 0;
        double y = 0;
        int count = 0;
        for (int k = 0; k < n; k++) {
            if (projections[k] >= max - ds / 2) {
                x += xs[k];
                y += ys[k];
                count++;
            }
        }
        x /= count;
        y /= count;
        double lead = (x * ux + y * uy) / norm;
        double centre = (cx * ux + cy * uy) / norm;
        double back = Math.min(Math.max(0, radius - ds / 2), Math.max(0, lead - centre));
        return new double[] {x - back * ux / norm, y - back * uy / norm};
    }

    /**
     * Makes a vessel cell for a new segment from the trailing voxels of a tip cell.
     *
     * <p>The tip cell keeps its voxels beyond the new tip node along the segment (its leading end),
     * and at least the {@code N_TIP0} leading voxels; parts of the kept voxels cut off from the
     * leading voxel are not kept. The other voxels, which lie along the new segment, are taken.
     * While the taken or the kept voxels are not connected, the leading taken voxel is kept
     * instead. If at least {@code N_MIN} voxels are taken, they become a vessel cell for the
     * segment, with the segment's footprint (without voxels of other pieces) as its piece, and the
     * tip cell's target drops by as much. Otherwise the segment is left to {@link #rebuild}.
     *
     * @param sim the simulation instance
     * @param tip the tip
     * @param segment the new segment
     * @param lattice the reference lattice
     * @param thickness the slab thickness [um]
     * @param hamiltonian the vessel Hamiltonian term
     */
    private void stalk(
            PottsSimulation sim,
            Tip tip,
            PottsVesselEdge segment,
            PottsLattice lattice,
            double thickness,
            VesselHamiltonian hamiltonian) {
        double ds = lattice.getDs();
        Piece piece = pieceOf(segment, lattice, thickness);
        if (piece.voxels.isEmpty()) {
            return;
        }

        // Order the tip cell's voxels from the leading end, by projection along the segment.
        double[] a = segment.getStart().getPosition();
        double[] b = segment.getEnd().getPosition();
        double length = Math.hypot(b[0] - a[0], b[1] - a[1]);
        double ux = length == 0 ? 0 : (b[0] - a[0]) / length;
        double uy = length == 0 ? 0 : (b[1] - a[1]) / length;
        PottsLocation location = (PottsLocation) tip.cell.getLocation();
        List<Voxel> voxels = location.getVoxels();
        Map<Voxel, Double> projections = new HashMap<>();
        for (Voxel v : voxels) {
            projections.put(v, ((v.x + 0.5) * ds - a[0]) * ux + ((v.y + 0.5) * ds - a[1]) * uy);
        }
        voxels.sort(
                (u, w) -> {
                    int c = Double.compare(projections.get(w), projections.get(u));
                    if (c != 0) {
                        return c;
                    }
                    return u.x != w.x ? Integer.compare(u.x, w.x) : Integer.compare(u.y, w.y);
                });

        // Keep the leading end, connected to the leading voxel; take the rest.
        Set<Voxel> kept = new HashSet<>();
        for (Voxel v : voxels) {
            if (kept.size() < nTip0 || projections.get(v) > length) {
                kept.add(v);
            }
        }
        kept = connectedTo(List.of(voxels.get(0)), kept);
        List<Voxel> taken = new ArrayList<>();
        for (Voxel v : voxels) {
            if (!kept.contains(v)) {
                taken.add(v);
            }
        }
        while (taken.size() >= nMin && !(isConnected(new HashSet<>(taken)) && isConnected(kept))) {
            kept.add(taken.remove(0));
        }
        if (taken.size() < nMin || kept.size() < nTip0) {
            return;
        }

        for (Voxel v : taken) {
            location.remove(v.x, v.y, v.z);
            potts.ids[v.z][v.x][v.y] = 0;
            potts.regions[v.z][v.x][v.y] = 0;
        }
        PottsCell cell = sim.insertCell(getPop(sim, population, true), new ArrayList<>(taken));
        if (cell == null) {
            throw new IllegalStateException("unable to insert vessel cell");
        }
        cell.setTargets(piece.voxels.size(), cell.getLocation().getSurface());
        hamiltonian.setVessel(cell.getID());
        hamiltonian.setAnchor(cell.getID(), keysOf(piece), anchorOf(piece));
        track(cell, piece);
        tip.base = Math.max(nTip0, tip.cell.getTargetVolume() - taken.size());
        tip.cell.setTargets(tip.base, tip.cell.getLocation().getSurface());
    }

    /**
     * Gets the piece of a single new segment: its footprint without voxels of other pieces.
     *
     * @param segment the segment
     * @param lattice the reference lattice
     * @param thickness the slab thickness [um]
     * @return the piece
     */
    private Piece pieceOf(PottsVesselEdge segment, PottsLattice lattice, double thickness) {
        Set<Long> held = pieceKeys();
        Piece piece = new Piece();
        piece.segments.add(segment);
        for (int[] v :
                PottsVesselFootprint.of(
                        segment.getStart().getPosition(),
                        segment.getEnd().getPosition(),
                        rOut(segment),
                        lattice.getDs(),
                        thickness,
                        lattice.getLength(),
                        lattice.getWidth())) {
            if (!held.contains(VesselHamiltonian.key(v[0], v[1]))) {
                piece.voxels.add(v);
            }
        }
        return piece;
    }

    /**
     * Replaces a tip cell that joined another vessel with a vessel cell on the same voxels.
     *
     * <p>The new cell's piece is the joining segment (the other segment at the new tip node), or,
     * if that has no footprint, the new segment if it has no cell. With neither, the tip cell
     * retires.
     *
     * @param sim the simulation instance
     * @param tip the tip
     * @param network the network
     * @param node the new tip node
     * @param segment the new segment
     * @param lattice the reference lattice
     * @param hamiltonian the vessel Hamiltonian term
     */
    private void join(
            PottsSimulation sim,
            Tip tip,
            PottsVesselNetwork network,
            PottsVesselNode node,
            PottsVesselEdge segment,
            PottsLattice lattice,
            VesselHamiltonian hamiltonian) {
        Piece piece = null;
        for (PottsVesselEdge other : network.getSegments(node)) {
            if (other != segment) {
                piece = pieceOf(other, lattice, network.getThickness());
            }
        }
        if ((piece == null || piece.voxels.isEmpty()) && !cells.containsKey(segment)) {
            piece = pieceOf(segment, lattice, network.getThickness());
        }
        if (piece == null || piece.voxels.isEmpty()) {
            retire(tip.cell, hamiltonian);
            return;
        }
        ArrayList<Voxel> voxels = ((PottsLocation) tip.cell.getLocation()).getVoxels();
        removeCell(sim, tip.cell);
        PottsCell cell = sim.insertCell(getPop(sim, population, true), voxels);
        if (cell == null) {
            throw new IllegalStateException("unable to insert vessel cell");
        }
        cell.setTargets(piece.voxels.size(), cell.getLocation().getSurface());
        hamiltonian.setVessel(cell.getID());
        hamiltonian.setAnchor(cell.getID(), keysOf(piece), anchorOf(piece));
        track(cell, piece);
    }

    /**
     * Gets the anchor strength of a piece from the mean integrity of its segments.
     *
     * @param piece the piece
     * @return the anchor strength
     */
    double anchorOf(Piece piece) {
        double sum = 0;
        for (PottsVesselEdge segment : piece.segments) {
            sum += segment.getIntegrity();
        }
        double integrity = sum / piece.segments.size();
        return lambdaAnchorMin + (lambdaAnchorMax - lambdaAnchorMin) * integrity;
    }

    /**
     * Gets the anchor keys of the voxels of a piece.
     *
     * @param piece the piece
     * @return the keys
     */
    private static Set<Long> keysOf(Piece piece) {
        Set<Long> keys = new HashSet<>();
        for (int[] v : piece.voxels) {
            keys.add(VesselHamiltonian.key(v[0], v[1]));
        }
        return keys;
    }

    /**
     * Checks if two pieces have the same voxels.
     *
     * @param a one piece
     * @param b the other piece
     * @return {@code true} if the voxels are the same, {@code false} otherwise
     */
    private static boolean sameVoxels(Piece a, Piece b) {
        if (a.voxels.size() != b.voxels.size()) {
            return false;
        }
        for (int k = 0; k < a.voxels.size(); k++) {
            int[] u = a.voxels.get(k);
            int[] w = b.voxels.get(k);
            if (u[0] != w[0] || u[1] != w[1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts drifted and fragmented vessel cells, logging non-zero counts.
     *
     * @param thickness the slab thickness [um]
     * @param ds the voxel size [um]
     */
    private void check(double thickness, double ds) {
        driftCount = 0;
        fragmentCount = 0;
        for (Map.Entry<PottsCell, Piece> entry : pieces.entrySet()) {
            PottsLocation location = (PottsLocation) entry.getKey().getLocation();
            if (!location.isConnected(checkRandom)) {
                fragmentCount++;
            }
            double[] centroid = location.getCentroid();
            double[] point = {(centroid[0] + 0.5) * ds, (centroid[1] + 0.5) * ds, 0};
            boolean near = false;
            for (PottsVesselEdge segment : entry.getValue().segments) {
                double[] a = segment.getStart().getPosition();
                double[] b = segment.getEnd().getPosition();
                double d = PottsVesselGeometry.pointSegmentDistance(point, a, b, thickness);
                if (d <= driftFold * rOut(segment)) {
                    near = true;
                    break;
                }
            }
            if (!near) {
                driftCount++;
            }
        }
        if (driftCount > 0 || fragmentCount > 0) {
            LOGGER.info(
                    String.format(
                            "vessel cells: %d drifted, %d fragmented, of %d",
                            driftCount, fragmentCount, pieces.size()));
        }
    }

    /**
     * Takes connected boundary voxels from a vessel cell, on the side closest to a point, and
     * empties them.
     *
     * <p>Boundary voxels are voxels of the cell with an in-plane neighbor, including diagonal
     * neighbors, within the lattice that is not in the cell. Diagonals are included so that the
     * boundary of a curved edge (a staircase of voxels) is connected through side neighbors, as the
     * taken voxels must be for a new cell. Starting from each boundary voxel in order of distance
     * to {@code towards}, a set is grown by adding the closest boundary voxel next to it until it
     * has {@code n} voxels. The first set that leaves the cell connected is taken: its voxels are
     * removed from the cell and set to empty (id 0), ready for {@link
     * PottsSimulation#insertCell(int, ArrayList)}. The anchor and target of the cell are unchanged.
     *
     * @param vessel the vessel cell
     * @param n the number of voxels to take
     * @param towards the point to take voxels toward, {x, y} in voxels
     * @param random the seeded random number generator (unused; ties are broken by position)
     * @return the voxels taken, or {@code null} if the cell would have fewer than {@code N_MIN}
     *     voxels or no connected set of {@code n} boundary voxels leaves the cell connected
     */
    public List<Voxel> carve(
            PottsCell vessel, int n, double[] towards, MersenneTwisterFast random) {
        PottsLocation location = (PottsLocation) vessel.getLocation();
        List<Voxel> voxels = location.getVoxels();
        if (n < 1 || voxels.size() - n < nMin) {
            return null;
        }
        if (potts == null) {
            throw new IllegalStateException("vessel cells are not built yet");
        }
        int[][] ids = potts.ids[0];
        int id = vessel.getID();

        List<Voxel> boundary = new ArrayList<>();
        for (Voxel v : voxels) {
            for (int[] m : MOVES_DIAGONAL) {
                int x = v.x + m[0];
                int y = v.y + m[1];
                if (x >= 0 && y >= 0 && x < ids.length && y < ids[0].length && ids[x][y] != id) {
                    boundary.add(v);
                    break;
                }
            }
        }
        Map<Voxel, Double> distances = new HashMap<>();
        for (Voxel v : boundary) {
            distances.put(v, Math.hypot(v.x - towards[0], v.y - towards[1]));
        }
        boundary.sort(
                (u, w) -> {
                    int c = Double.compare(distances.get(u), distances.get(w));
                    if (c != 0) {
                        return c;
                    }
                    return u.x != w.x ? Integer.compare(u.x, w.x) : Integer.compare(u.y, w.y);
                });

        for (Voxel seed : boundary) {
            List<Voxel> taken = grow(seed, n, boundary);
            if (taken == null) {
                continue;
            }
            Set<Voxel> rest = new HashSet<>(voxels);
            taken.forEach(rest::remove);
            if (!isConnected(rest)) {
                continue;
            }
            for (Voxel v : taken) {
                location.remove(v.x, v.y, v.z);
                potts.ids[v.z][v.x][v.y] = 0;
                potts.regions[v.z][v.x][v.y] = 0;
            }
            return taken;
        }
        return null;
    }

    /**
     * Grows a connected set of boundary voxels from a seed, adding the closest next voxel first.
     *
     * @param seed the first voxel
     * @param n the number of voxels
     * @param ordered the boundary voxels, closest first
     * @return the set in order of adding, or {@code null} if fewer than {@code n} are reachable
     */
    private static List<Voxel> grow(Voxel seed, int n, List<Voxel> ordered) {
        List<Voxel> taken = new ArrayList<>();
        Set<Voxel> set = new HashSet<>();
        taken.add(seed);
        set.add(seed);
        while (taken.size() < n) {
            Voxel next = null;
            for (Voxel v : ordered) {
                if (set.contains(v)) {
                    continue;
                }
                for (Voxel t : taken) {
                    if (Math.abs(t.x - v.x) + Math.abs(t.y - v.y) == 1) {
                        next = v;
                        break;
                    }
                }
                if (next != null) {
                    break;
                }
            }
            if (next == null) {
                return null;
            }
            taken.add(next);
            set.add(next);
        }
        return taken;
    }

    /**
     * Checks if a set of voxels is connected through in-plane neighbors.
     *
     * @param voxels the voxels
     * @return {@code true} if the voxels are connected, {@code false} otherwise
     */
    static boolean isConnected(Set<Voxel> voxels) {
        if (voxels.isEmpty()) {
            return true;
        }
        Set<Voxel> seen = new HashSet<>();
        ArrayDeque<Voxel> queue = new ArrayDeque<>();
        Voxel first = voxels.iterator().next();
        queue.add(first);
        seen.add(first);
        while (!queue.isEmpty()) {
            Voxel v = queue.poll();
            for (int[] m : MOVES) {
                Voxel neighbor = new Voxel(v.x + m[0], v.y + m[1], v.z);
                if (voxels.contains(neighbor) && seen.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return seen.size() == voxels.size();
    }
}
