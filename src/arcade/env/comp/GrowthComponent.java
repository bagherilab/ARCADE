package arcade.env.comp;

import static arcade.env.comp.GraphSitesUtilities.calcStress;
import static arcade.env.comp.GraphSitesUtilities.updateGraph;

import java.util.ArrayList;
import java.util.logging.Logger;

import arcade.env.comp.GraphSites.SiteEdge;
import arcade.env.comp.GraphSites.SiteNode;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.sim.Simulation;
import arcade.util.Graph;
import arcade.util.MiniBox;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;


/**
 * Implementation of {@link Component} for degrading graph edges.
 * <p>
 * This component can only be used with {@link GraphSites}. The
 * component is stepped every {@code DEGRADATION_INTERVAL} ticks. wall thickness
 * of edges that are adjacent to a location with cancerous cells is decreased
 * ({@code DEGRADATION_RATE}). Edges that are below a minimum wall thickness and
 * have a shear stress below the shear threshold ({@code SHEAR_THRESHOLD}) are
 * removed from the graph. At the end of a step, if no edges have been removed
 * from the graph, then only the stresses in the graph are recalculated.
 * Otherwise, all hemodynamic properties are recalculated.
 */
public class GrowthComponent implements Component {
    private static Logger LOGGER = Logger.getLogger(GrowthComponent.class.getName());

    private int interval;
    private final double migrationRate;
    private final double vegfThreshold;
    private final String walkType;

    /** The associated {@link GraphSites} object. */
    private GraphSites sites;

    /** The {@link Graph} object representing the sites. */
    private Graph graph;

    private int[][] offsets;
    private final int level = 1;
    private final int type = 0;

    private final MiniBox specs;

    /**
     * @param series
     *            the simulation series
     * @param parameters
     *            the component parameters dictionary
     */
    public GrowthComponent(MiniBox component) {
        // Set loaded parameters.
        migrationRate = component.getDouble("MIGRATION_RATE");
        vegfThreshold = component.getDouble("VEGF_THRESHOLD");
        walkType = component.get("WALK_TYPE");

		// Get list of specifications.
		specs = new MiniBox();
		String[] specList = new String[] { "MIGRATION_RATE", "VEGF_THRESHOLD", "WALK_TYPE" };
		for (String spec : specList) { specs.put(spec, component.get(spec)); }

    }

	/**
	 * Component does not have a relevant field; returns {@code null}.
	 *
	 * @return  {@code null}
	 */
	public double[][][] getField() { return null; }

    @Override
    public void scheduleComponent(Simulation sim) {
		Component comp = sim.getEnvironment("sites").getComponent("sites");
		if (!(comp instanceof GraphSites)) {
			LOGGER.warning("cannot schedule GROWTH component for non-graph sites");
			return;
		}

		sites = (GraphSites) comp;
        offsets = sites.getOffsets();

        interval = calculateInterval();
        ((SimState)sim).schedule.scheduleRepeating(1, Simulation.ORDERING_COMPONENT - 1, this, interval);
        ((SimState)sim).schedule.scheduleOnce((state) -> graph = sites.getGraph(), Simulation.ORDERING_COMPONENT - 1);
    }

    private int calculateInterval() {
        double edgeSize = sites.getGridSize();
        if (migrationRate < edgeSize) {
            return 60;
        } else {
            return 30;
        }
    }

	/**
	 * {@inheritDoc}
	 * <p>
	 * Degradation component does not use this method.
	 */
	public void updateComponent(Simulation sim, Location oldLoc, Location newLoc) { }


    @Override
    public void step(final SimState state) {
        final Simulation sim = (Simulation) state;
        final Lattice vegf_lattice = sim.getEnvironment("VEGF");
        final MersenneTwisterFast random = state.random;

        final double tick = sim.getTime();
        boolean updated = false;

        for (final Object nodeObj : graph.getAllNodes()) {
            final SiteNode node = (SiteNode) nodeObj;
            if (tick - node.lastUpdate < migrationRate && !node.isSprout()) {
                continue;
            }

            final int skip = sites.getDirection(node, node.prev, level);
            final ArrayList<ArrayList<Double>> vegfList = getVEGFList(vegf_lattice, node);

            if (node.isTip() || sumListArrays(vegfList) > vegfThreshold) {
                updated = true;
                final ArrayList<Double> vegfAverages = getListAverages(vegfList);
                vegfAverages.remove(skip);

                switch (walkType.toUpperCase()) {
                    case "RANDOM":
                        performRandomWalk(random, node, vegfAverages, tick);
                    case "BIASED":
                        performBiasedWalk(random, node, vegfAverages, tick);
                    case "MAX":
                        performDeterministicWalk(random, node, vegfAverages, tick);
                    default:
                        LOGGER.warning("invalid walk type: " + walkType + "; using default of BIASED.");
                        performBiasedWalk(random, node, vegfAverages, tick);
                }
            }
        }

        // If any edges are removed, update the graph edges that are ignored.
        // Otherwise, recalculate calculate stresses.
        if (updated) { updateGraph(graph, sites);
        } else { calcStress(graph);
        }
    }

    private void performRandomWalk(final MersenneTwisterFast random, final SiteNode node,
            final ArrayList<Double> valList, final double tick) {
        final int randDir = random.nextInt(offsets.length);
        createNewEdge(randDir, node, tick);
    }

    private void performBiasedWalk(final MersenneTwisterFast random, final SiteNode node,
            final ArrayList<Double> valList, final double tick) {
        final ArrayList<Double> seqList = normalizeSequentialList(valList);
        final double val = random.nextDouble();
        for (int i=0; i < offsets.length; i++) {
            if (val < seqList.get(i)) {
                createNewEdge(i, node, tick);
                break;
            }
        }
    }

    private void performDeterministicWalk(final MersenneTwisterFast random, final SiteNode node,
            final ArrayList<Double> seqList, final double tick) {
        final int maxDir = getMaxKey(seqList);
        createNewEdge(maxDir, node, tick);
    }

    private ArrayList<ArrayList<Double>> getVEGFList(final Lattice lattice, final SiteNode node) {
        final ArrayList<ArrayList<Double>> vegfList = new ArrayList<>();
        for (int i=0; i < offsets.length; i++) {
            SiteNode proposed = sites.offsetNode(node, i, level);
            if (sites.checkNode(proposed)) {
                final ArrayList<int[]> span = sites.getSpan(node, proposed);
                vegfList.add(i, new ArrayList<>());
                for (final int[] coor : span) {
                    vegfList.get(i).add(lattice.getAverageVal(sites.location.toLocation(coor)));
                }
            } else {
                vegfList.add(i, new ArrayList<>(0));
            }
        }
        return vegfList;
    }

    private int getMaxKey(final ArrayList<Double> map) {
        int maxDir = 0;
        double maxVal = 0;
        for (int i=0; i < offsets.length; i++) {
            if (map.get(i) > maxVal) {
                maxDir = i;
                maxVal = map.get(i);
            }
        }
        return maxDir;
    }

    private ArrayList<Double> getListAverages(final ArrayList<ArrayList<Double>> map) {
        final ArrayList<Double> averageList = new ArrayList<>();
        for (int i=0; i < offsets.length; i++) {
            double sum = 0;
            for (final double value : map.get(i)) {
                sum += value;
            }
            averageList.add(i, sum / map.get(i).size());
        }
        return averageList;
    }

    private ArrayList<Double> normalizeSequentialList(final ArrayList<Double> map) {
        final ArrayList<Double> normalizedList = new ArrayList<>();
        final double norm = sumList(map);
        double prev = 0;
        for (int i=0; i < offsets.length; i++) {
            normalizedList.add(i, prev + map.get(i) / norm);
            prev = prev + map.get(i) / norm;
        }
        return normalizedList;
    }

    private double sumList(final ArrayList<Double> map) {
        double sum = 0;
        for (int i=0; i < offsets.length; i++) {
            sum += map.get(i);
        }
        return sum;
    }

    private double sumListArrays(final ArrayList<ArrayList<Double>> map) {
        double sum = 0;
        for (int i=0; i < offsets.length; i++) {
            for (final double value : map.get(i)) {
                sum += value;
            }
        }
        return sum;
    }

    private void createNewEdge(final int i, final SiteNode node, final double tick) {
        final SiteNode proposed = sites.offsetNode(node, i, level);
        proposed.lastUpdate = tick;
        if (sites.checkNode(proposed)) {
            node.isTip = false;
            node.isSprout = true;
            proposed.isTip = true;
            proposed.isSprout = false;
            final SiteEdge edge = new SiteEdge(node, proposed, type, level);
            graph.addEdge(edge);
        }
    }

    /**
	 * {@inheritDoc}
	 * <p>
	 * The JSON is formatted as:
	 * <pre>
	 *     {
	 *         "type": "GROWTH",
	 *         "interval": interval,
	 *         "specs" : {
	 *             "SPEC_NAME": spec value,
	 *             "SPEC_NAME": spec value,
	 *             ...
	 *         }
	 *     }
	 * </pre>
	 */
	public String toJSON() {
		String format = "{ " + "\"type\": \"DEGRADE\", " + "\"interval\": %d, " + "\"specs\": %s " + "}";
		return String.format(format, interval, specs.toJSON());
	}
}


