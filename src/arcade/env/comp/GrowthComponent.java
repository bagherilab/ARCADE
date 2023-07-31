package arcade.env.comp;

import static arcade.env.comp.GraphSites.CAP_RADIUS;

import static arcade.env.comp.GraphSitesUtilities.calcThickness;
import static arcade.env.comp.GraphSitesUtilities.path;
import static arcade.env.comp.GraphSitesUtilities.updateGraph;
import static arcade.env.comp.GraphSitesUtilities.calcPressure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import arcade.env.comp.GraphSites.SiteEdge;
import arcade.env.comp.GraphSites.SiteNode;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.sim.Simulation;
import arcade.util.Graph;
import arcade.util.MiniBox;
import ec.util.MersenneTwisterFast;
import sim.util.Bag;
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


    private final double migrationRate;
    private final double vegfThreshold;
    private final String walkType;
    private final double maxLength;

    private int maxEdges;
    private int interval;
    private double edgeSize;
    /** The associated {@link GraphSites} object. */
    private GraphSites sites;

    /** The {@link Graph} object representing the sites. */
    private Graph graph;

    private HashMap<SiteNode, ArrayList<SiteEdge>> angioMap;
    private ArrayList<SiteEdge> tempEdges;

    private int[][] offsets;
    private final int level = 1;
    private final int type = 0; //Capillary type

    private final MiniBox specs;

    private ArrayList<SiteEdge> added = new ArrayList<>();

    public GrowthComponent(MiniBox component) {
        // Set loaded parameters.
        migrationRate = component.getDouble("MIGRATION_RATE");
        vegfThreshold = component.getDouble("VEGF_THRESHOLD");
        walkType = component.get("WALK_TYPE");
        maxLength = component.getDouble("MAX_LENGTH");
        angioMap = new HashMap<>();

		// Get list of specifications.
		specs = new MiniBox();
		String[] specList = new String[] { "MIGRATION_RATE", "VEGF_THRESHOLD", "WALK_TYPE", "MAX_LENGTH" };
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
        edgeSize = sites.getGridSize();
        maxEdges = (int) Math.floor(maxLength / edgeSize);
        interval = calculateInterval();
        ((SimState)sim).schedule.scheduleRepeating(1, Simulation.ORDERING_COMPONENT - 1, this, interval);
        ((SimState)sim).schedule.scheduleOnce((state) -> graph = sites.getGraph(), Simulation.ORDERING_COMPONENT - 1);
    }

    private int calculateInterval() {
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
        final Lattice vegf_lattice = sim.getEnvironment("vegf");
        final MersenneTwisterFast random = state.random;
        ArrayList<SiteNode> nodesToRemove = new ArrayList<>();

        final double tick = sim.getTime();
        boolean updated = false;

        if (((int) tick - 1) % (12*60) == 0) {
            LOGGER.info((int)(tick - 1) + " | number of nodes to check: " +  graph.getAllNodes().size());
        }


        for (final Object nodeObj : graph.getAllNodes()) {

            final SiteNode node = (SiteNode) nodeObj;
            if (checkNodeSkipStatus(node)) { continue; }

            ArrayList<ArrayList<Double>> vegfList = getVEGFList(vegf_lattice, node);

            if (averageListArrays(vegfList) > vegfThreshold) {
                node.isAngio = true;
                angioMap.put(node, new ArrayList<>());
                ArrayList<Integer> skipDirList = new ArrayList<Integer>();

                Bag in = graph.getEdgesIn(node);
                Bag out = graph.getEdgesOut(node);
                if (in != null){
                    for (Object edge : in){
                        SiteEdge inEdge = (SiteEdge)edge;
                        skipDirList.add(sites.getOppositeDirection(inEdge, inEdge.level));
                    }
                }
                if (out != null){
                    for (Object edge : out){
                        SiteEdge outEdge = (SiteEdge)edge;
                        skipDirList.add(sites.getDirection(outEdge, outEdge.level));
                    }
                }

                ArrayList<Double> vegfAverages = getListAverages(vegfList);

                int newDir;
                switch (walkType.toUpperCase()) {
                    case "RANDOM":
                        newDir = performRandomWalk(random, node, vegfAverages, tick, skipDirList);
                        break;
                    case "BIASED":
                        newDir = performBiasedWalk(random, node, vegfAverages, tick, skipDirList);
                        break;
                    case "MAX":
                        newDir = performDeterministicWalk(random, node, vegfAverages, tick, skipDirList);
                        break;
                    default:
                        LOGGER.warning("invalid walk type: " + walkType + "; using default of MAX.");
                        newDir = performDeterministicWalk(random, node, vegfAverages, tick, skipDirList);
                }
                node.sproutDir = newDir;
            }
        }

        boolean addFlag = false;

        addTemporaryEdges();

        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angioMap.entrySet()){
            //grab final node in each list and add edge, check for perfusion
            SiteNode keyNode = entry.getKey();
            ArrayList<SiteEdge> edgeList = entry.getValue();
            SiteNode tipNode;
            SiteEdge newEdge;
            if (edgeList.size() > 0) {
                tipNode = edgeList.get(edgeList.size() - 1).getTo();
            }
            else { tipNode = keyNode; }

            if (tick - tipNode.lastUpdate < migrationRate) { continue; }

            newEdge = createNewEdge(keyNode.sproutDir, tipNode, tick);

            if (edgeList.size() > maxEdges || newEdge == null) {
                LOGGER.info("Removing " + keyNode + " from angiomap, unsuccessful perfusion.");
                nodesToRemove.add(keyNode);
            }
            else {
                edgeList.add(newEdge);
                if (newEdge.isAnastomotic){
                    keyNode.anastomosis = true;
                    addFlag = true;
                }
            }
        }

        removeTemporaryEdges();

        if (addFlag) {
            added.clear();
            LOGGER.info("*****Adding edges to graph.****** Time: " + tick);
            LOGGER.info("Current graph size: " + graph.getAllEdges().size());
            for (SiteNode sproutNode : angioMap.keySet()) {
                if (nodesToRemove.contains(sproutNode)) { continue; }
                if (sproutNode.anastomosis) {
                    SiteNode finalNode = angioMap.get(sproutNode).get(angioMap.get(sproutNode).size() - 1).getTo();

                    // maybe try to redo by iterating through list rather than using final node
                    if (finalNode.isAngio){
                        LOGGER.info("CONNECTING TWO ANGIOGENIC NODES");
                        SiteNode targetNode = findKeyNodeInMap(finalNode);
                        path(graph, targetNode, sproutNode);
                        if (sproutNode.prev != null) {
                            LOGGER.info("SWAPPING SPROUT NODE");
                            reverseAllEdges(sproutNode);
                        } else {
                            LOGGER.info("SWAPPING TARGET NODE");
                            reverseAllEdges(targetNode);
                        }
                        angioMap.get(sproutNode).addAll(angioMap.get(targetNode));
                        addEdgeList(angioMap.get(sproutNode), true);
                        updated = true;
                        nodesToRemove.add(sproutNode);
                        nodesToRemove.add(targetNode);
                    }
                    else {
                        path(graph, finalNode, sproutNode);
                        if (sproutNode.prev != null) {
                            LOGGER.info("SWAPPING NODE");
                            reverseAllEdges(sproutNode);
                        } else {
                            LOGGER.info("DID NOT SWAP");
                        }
                        addEdgeList(angioMap.get(sproutNode), true);
                        updated = true;
                        nodesToRemove.add(sproutNode);
                    }
                }
            }
        }

        for (SiteNode n : nodesToRemove) {
            angioMap.remove(n);
        }
        // If any edges are removed, update the graph edges that are ignored.
        // Otherwise, recalculate calculate stresses.
        if (updated) {
            LOGGER.info("" + angioMap);
            updateGraph(graph, sites, added);
        }
    }

    private boolean checkNodeSkipStatus(SiteNode node) {
            if (graph.getInDegree(node) == 0 || graph.getOutDegree(node) == 0) { return true; }
            if (graph.getDegree(node) > 2) { return true; }
            if (angioMap.keySet().contains(node)) { return true; }
            return false;
    }


    private void reverseAllEdges(SiteNode node) {
        for (SiteEdge edge : angioMap.get(node)) {
            edge.reverse();
        }
    }


    private SiteNode findKeyNodeInMap(SiteNode targetNode){
        for (SiteNode keyNode : angioMap.keySet()) {
            if (edgeListContainsNode(angioMap.get(keyNode), targetNode)) {
                return keyNode;
            }
        }
        return null;
    }

    private boolean edgeListContainsNode(ArrayList<SiteEdge> edgeList, SiteNode targetNode){
        for (SiteEdge edge : edgeList) {
            if (edge.getTo() == targetNode) {
                return true;
            }
        }
        return false;

    }

    private void addTemporaryEdges() {
        tempEdges = new ArrayList<>();
        for (Map.Entry<SiteNode, ArrayList<SiteEdge>> entry : angioMap.entrySet()){
            ArrayList<SiteEdge> edgeList = entry.getValue();
            tempEdges.addAll(edgeList);
            addEdgeList(edgeList);
        }
    }

    private void removeTemporaryEdges() {
        if (tempEdges.isEmpty()) { return; }
        removeEdgeList(tempEdges);
        tempEdges.clear();
    }

    private void removeEdgeList(ArrayList<SiteEdge> edgeList) {
        for (SiteEdge edge : edgeList) {
            graph.removeEdge(edge);
        }
    }

    private int performRandomWalk(final MersenneTwisterFast random, final SiteNode node,
            final ArrayList<Double> valList, final double tick, ArrayList<Integer> skipList) {
        int randDir;
        do{
            randDir = random.nextInt(offsets.length);
        } while (!skipList.contains(randDir));
        return randDir;
    }

    private int performBiasedWalk(final MersenneTwisterFast random, final SiteNode node,
            final ArrayList<Double> valList, final double tick, ArrayList<Integer> skipList) {
        for (final int dir : skipList) {
            valList.set(dir, 0.0);
        }
        final ArrayList<Double> seqList = normalizeSequentialList(valList);
        final double val = random.nextDouble();
        for (int i=0; i < offsets.length; i++) {
            if (val < seqList.get(i)) {
                return i;
            }
        }
        return offsets.length - 1;
    }

    private int performDeterministicWalk(final MersenneTwisterFast random, final SiteNode node,
            final ArrayList<Double> valList, final double tick, ArrayList<Integer> skipList) {
        for (final int dir : skipList) {
            valList.set(dir, 0.0);
        }
        final int maxDir = getMaxKey(valList);
        return maxDir;
    }

    private ArrayList<ArrayList<Double>> getVEGFList(final Lattice lattice, final SiteNode node) {
        double[][][] field = lattice.getField();
        final ArrayList<ArrayList<Double>> vegfList = new ArrayList<>();
        for (int dir=0; dir < offsets.length; dir++) {
            SiteNode proposed = sites.offsetNode(node, dir, level);
            if (sites.checkNode(proposed)) {
                final ArrayList<int[]> span = sites.getSpan(node, proposed);
                vegfList.add(dir, new ArrayList<>());
                for (final int[] coords : span) {
                    int i = coords[0];
                    int j = coords[1];
                    int k = coords[2];
                    vegfList.get(dir).add(field[k][i][j]);
                }
            } else {
                vegfList.add(dir, new ArrayList<>(0));
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

    private double averageListArrays(final ArrayList<ArrayList<Double>> map) {
        double sum = 0;
        int count = 0;
        for (int i=0; i < offsets.length; i++) {
            for (final double value : map.get(i)) {
                sum += value;
                count++;
            }
        }
        return sum / count;
    }

    private void addEdgeList(final ArrayList<SiteEdge> list) {
        addEdgeList(list, false);
    }

    private void addEdgeList(final ArrayList<SiteEdge> list, boolean updateProperties) {
        addEdgeList(list, updateProperties, type);
    }

    private void addEdgeList(final ArrayList<SiteEdge> list, boolean updateProperties, int edgeType) {
        for (SiteEdge edge : list) {
            if (updateProperties){
                LOGGER.info("BEFORE | to: " + edge.getTo().pressure + "from: " + edge.getFrom().pressure);
                edge.type = edgeType;
                edge.isAngiogenic = true;
                edge.radius = CAP_RADIUS;
                edge.wall = calcThickness(edge.radius);
                edge.span = sites.getSpan(edge.getFrom(), edge.getTo());
                edge.length = sites.getLength(edge, 1);
                edge.getTo().pressure = calcPressure(edge.radius, type);
                edge.getFrom().pressure = calcPressure(edge.radius, type);
                LOGGER.info("AFTER | to: " + edge.getTo().pressure + "from: " + edge.getFrom().pressure);
                edge.fraction = new double[sites.NUM_MOLECULES];
                edge.transport = new double[sites.NUM_MOLECULES];
                for (int[] coor: edge.span){ //i don't think i need this?
                    int i = coor[0];
                    int j = coor[1];
                    int k = coor[2];
                    sites.sites[k][i][j]++;
                }
                added.add(edge);
            }
            graph.addEdge(edge);
        }
    }

    private SiteEdge createNewEdge(final int dir, final SiteNode node, final double tick) {
        final SiteNode proposed = sites.offsetNode(node, dir, level);
        proposed.lastUpdate = tick;
        if (sites.checkNode(proposed) && graph.getDegree(node) < 4) {
            SiteEdge edge = new SiteEdge(node, proposed, type, level);
            edge.getTo().isAngio = true;
            if (graph.containsNode(edge.getTo())){
                edge.isAnastomotic = true;
            }
            return edge;
        }
        return null;
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
		String format = "{ " + "\"type\": \"GROWTH\", " + "\"interval\": %d, " + "\"specs\": %s " + "}";
		return String.format(format, interval, specs.toJSON());
	}
}


