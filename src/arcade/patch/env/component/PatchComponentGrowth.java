package arcade.env.comp;

import static arcade.env.comp.GraphSites.CAP_RADIUS;
import static arcade.env.comp.GraphSites.CAP_RADIUS_MIN;
import static arcade.env.comp.GraphSites.CAP_RADIUS_MAX;

import static arcade.env.comp.GraphSitesUtilities.calcThickness;
import static arcade.env.comp.GraphSitesUtilities.path;
import static arcade.env.comp.GraphSitesUtilities.reversePressures;
import static arcade.env.comp.GraphSitesUtilities.getPath;
import static arcade.env.comp.GraphSitesUtilities.updateGraph;
import static arcade.env.comp.GraphSitesUtilities.calcFlows;
import static arcade.env.comp.GraphSitesUtilities.calcPressure;
import static arcade.env.comp.GraphSitesUtilities.calcPressures;
import static arcade.env.comp.GraphSitesUtilities.calcStress;
import static arcade.env.comp.GraphSitesUtilities.calculateLocalFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Logger;

import arcade.env.comp.GraphSites.Root;
import arcade.env.comp.GraphSites.SiteEdge;
import arcade.env.comp.GraphSites.SiteNode;
import arcade.env.lat.Lattice;
import arcade.env.loc.Location;
import arcade.sim.Simulation;
import arcade.util.Graph;
import arcade.util.MiniBox;
import arcade.util.Solver;
import arcade.util.Solver.Function;
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

    /** Calculation strategies. */
    public enum Calculation {
        /** Code for upstream calculation strategy. */
        COMPENSATE,

        /** Code for downstream direction strategy. */
        DIVERT
    }

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
    private final int default_type = 0; //Capillary type

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
        ((SimState)sim).schedule.scheduleRepeating(1, Simulation.ORDERING_COMPONENT - 3, this, interval);
        ((SimState)sim).schedule.scheduleOnce((state) -> graph = sites.getGraph(), Simulation.ORDERING_COMPONENT - 3);
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

        // if (((int) tick - 1) % (12*60) == 0) {
        //     LOGGER.info((int)(tick - 1) + " | number of nodes to check: " +  graph.getAllNodes().size());
        // }

		LinkedHashSet<SiteNode> set = new LinkedHashSet<>();


		for (Object obj : graph.getAllEdges()) {
			SiteEdge edge = (SiteEdge)obj;
			if (edge.isIgnored) { continue; }
			SiteNode from = edge.getFrom();
			SiteNode to = edge.getTo();
			from.id = -1;
			to.id = -1;

			if ((graph.getDegree(from) < 3) && !from.isRoot && !(graph.getInDegree(from) == 0 && graph.getOutDegree(from) == 1)) { set.add(from); }
			if ((graph.getDegree(to) < 3) && !to.isRoot && !(graph.getInDegree(to) == 1 && graph.getOutDegree(to) == 0)) { set.add(to); }
		}

        for (SiteNode node : set) {
            if (checkNodeSkipStatus(node, tick)) { continue; }

            ArrayList<ArrayList<Double>> vegfList = getVEGFList(vegf_lattice, node);

            if (averageListArrays(vegfList) > vegfThreshold) {
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

            if (checkForIgnoredEdges(keyNode)) {
                nodesToRemove.add(keyNode);
                continue;
            }

            ArrayList<SiteEdge> edgeList = entry.getValue();
            SiteNode tipNode;
            SiteEdge newEdge;
            if (edgeList.size() > 0) {
                tipNode = edgeList.get(edgeList.size() - 1).getTo();
            }
            else { tipNode = keyNode; }

            if (tick - tipNode.lastUpdate < migrationRate) { continue; }

            newEdge = createNewEdge(keyNode.sproutDir, tipNode, tick);

            if (edgeList.size() > maxEdges || newEdge == null || graph.getDegree(keyNode) > 3 ) {
                // LOGGER.info("Removing " + keyNode + " from angiomap, unsuccessful perfusion.");
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
            // LOGGER.info("*****Adding edges to graph.****** Time: " + tick);
            // LOGGER.info("Current graph size: " + graph.getAllEdges().size());
            for (SiteNode sproutNode : angioMap.keySet()) {
                if (nodesToRemove.contains(sproutNode)) { continue; }
                if (sproutNode.anastomosis) {
                    int leadingIndex = angioMap.get(sproutNode).size() - 1;
                    if (leadingIndex < 0) {
                        nodesToRemove.add(sproutNode);
                        continue;
                    }
                    SiteNode finalNode = angioMap.get(sproutNode).get(leadingIndex).getTo();
                    SiteNode init, fin;

                    calcPressures(graph);
                    boolean reversed = reversePressures(graph);
                    if (reversed) { calcPressures(graph); }
                    calcFlows(graph, sites);
                    calcStress(graph);

                    // maybe try to redo by iterating through list rather than using final node
                    if (!graph.containsNode(finalNode)){
                        // LOGGER.info("CONNECTING TWO ANGIOGENIC NODES");
                        SiteNode targetNode = findKeyNodeInMap(finalNode, sproutNode);
                        if (targetNode == null) {
                            // LOGGER.info("Likely removed node - skipping");
                            sproutNode.anastomosis = false;
                            continue;
                        }
                        // path(graph, targetNode, sproutNode);
                        if (sproutNode.pressure < targetNode.pressure) {
                            // LOGGER.info("SWAPPING SPROUT NODE");
                            reverseAllEdges(sproutNode);
                            init = targetNode;
                            fin = sproutNode;
                        } else {
                            // LOGGER.info("SWAPPING TARGET NODE");
                            reverseAllEdges(targetNode);
                            init = sproutNode;
                            fin = targetNode;
                        }
                        angioMap.get(sproutNode).addAll(angioMap.get(targetNode));

                        nodesToRemove.add(sproutNode);
                        nodesToRemove.add(targetNode);
                    }
                    else {
                        if (sproutNode.pressure == 0) {
                            if (graph.getEdgesOut(sproutNode) != null) {sproutNode = ((SiteEdge) graph.getEdgesOut(sproutNode).get(0)).getFrom();}
                        }
                        if (finalNode.pressure == 0) {
                            if (graph.getEdgesOut(finalNode) != null) {finalNode = ((SiteEdge) graph.getEdgesOut(finalNode).get(0)).getFrom();}
                        }
                        // path(graph, finalNode, sproutNode);
                        if (sproutNode.pressure < finalNode.pressure) {
                            // LOGGER.info("SWAPPING NODE");
                            reverseAllEdges(sproutNode);
                            init = finalNode;
                            fin = sproutNode;
                        } else {
                            // LOGGER.info("DID NOT SWAP");
                            init = sproutNode;
                            fin = finalNode;
                        }
                        nodesToRemove.add(sproutNode);
                    }
                    if (init.pressure == 0 || fin.pressure == 0) {
                        // LOGGER.info("Pressure is 0, skipping");
                        continue;
                    }
                    addAngioEdges(angioMap.get(sproutNode), init, fin, tick, Calculation.COMPENSATE);
                }
            }
        }

        for (SiteNode n : nodesToRemove) {
            angioMap.remove(n);
        }
        // If any edges are removed, update the graph edges that are ignored.
        // Otherwise, recalculate calculate stresses.
        if (!added.isEmpty()) {
            // LOGGER.info("*****Updating graph.****** Time: " + tick);
            updateGraph(graph, sites, added);
        }
    }

    private boolean checkForIgnoredEdges(SiteNode node){
        Bag in = graph.getEdgesIn(node);
        Bag out = graph.getEdgesOut(node);
        if (in != null){
            for (Object edge : in){
                SiteEdge inEdge = (SiteEdge)edge;
                if (inEdge.isIgnored) { return true; }
            }
        }
        if (out != null){
            for (Object edge : out){
                SiteEdge outEdge = (SiteEdge)edge;
                if (outEdge.isIgnored) { return true; }
            }
        }
        return false;
    }

    private boolean checkNodeSkipStatus(SiteNode node, double tick) {
            if (angioMap.keySet().contains(node)) { return true; }
            if (node.isRoot) {return true; }
            if ((tick - node.addTime ) < (72*60)) { return true; }
            return false;
    }


    private void reverseAllEdges(SiteNode node) {
        for (SiteEdge edge : angioMap.get(node)) {
            edge.reverse();
        }
    }


    private SiteNode findKeyNodeInMap(SiteNode targetNode, SiteNode skipNode){
        for (SiteNode keyNode : angioMap.keySet()) {
            if (keyNode == skipNode) { continue; }
            if (keyNode == targetNode ) { return keyNode; }
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
        addEdgeList(list, updateProperties, default_type);
    }

    private void addAngioEdges(ArrayList<SiteEdge> list, SiteNode start, SiteNode end, double tick, Calculation calc) {

        ArrayList<SiteEdge> added = new ArrayList<>();

        //check for cycle
        path(graph, end, start);
        if (end.prev != null) {
            // LOGGER.info("Cycle detected, not adding edge");
            return;
        }

        Graph tempG = sites.newGraph();
        for (SiteEdge e : list){
            tempG.addEdge(e);
        }
        // LOGGER.info("" + tempG);
        path(tempG, start, end);
        SiteNode n = end;
        while (n != start){
            added.add(new SiteEdge(n.prev, n, 0, level, false));
            n = n.prev;
            if (n != start) {
                if (n== null) {
                    return;
                }
                n.addTime = tick;
            }
        }


        double otherRadius = 0;
        Bag outEdges = graph.getEdgesOut(start);
        if (outEdges != null){
                otherRadius = ((SiteEdge)outEdges.get(0)).radius;
        }
        else { return; }

        for (SiteEdge edge : added) {
            edge.isAngiogenic = true;
            edge.radius = (otherRadius > CAP_RADIUS) ? CAP_RADIUS : calculateEvenSplitRadius((SiteEdge) outEdges.get(0));
            edge.wall = calcThickness(edge.radius);
            edge.span = sites.getSpan(edge.getFrom(), edge.getTo());
            edge.length = sites.getLength(edge, 1);

            //update later (updateSpans method should take care of most of these, need to check for perfusion first)
            edge.isPerfused = true;
            edge.fraction = new double[sites.NUM_MOLECULES];
            edge.transport = new double[sites.NUM_MOLECULES];
            for (int[] coor: edge.span){ //i don't think i need this?
                int i = coor[0];
                int j = coor[1];
                int k = coor[2];
                sites.sites[k][i][j]++;
            }
        }


        if (start.pressure * end.pressure <= 0) {
            return;
        }

        addEdgeList(added);

        switch (calc) {
            case COMPENSATE:
                updateRootsAndRadii(added, start, end);
                break;
            case DIVERT:
                SiteNode intersection = (SiteNode) graph.findDownstreamIntersection((SiteEdge) outEdges.get(0), (SiteEdge) added.get(0));
                if (intersection != null){
                    recalcRadii(added, start, end, intersection);
                }
                else {
                    removeEdgeList(added);
                }
                break;
        }

    }

    private double calculateEvenSplitRadius(SiteEdge edge){
        double radius = edge.radius;
        double length = edge.length;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double flow = calculateLocalFlow(radius, length, deltaP);
        double newRadius = Solver.bisection((double r) -> flow - 2 * calculateLocalFlow(r, length, deltaP), 1E-6, 5*CAP_RADIUS_MAX, 1E-6);
        // LOGGER.info("splitting radius, for checking if it happens directly before bisection failing");
        // double newRadius = Solver.bisection((double r) -> Math.pow(flow - 2 * calculateLocalFlow(r, length, deltaP), 2), 0, CAP_RADIUS_MAX);
        // double newRadius = Solver.boundedGradientDescent((double r) -> Math.pow(flow - 2 * calculateLocalFlow(r, length, deltaP), 2), radius, 1E-17, CAP_RADIUS_MIN, CAP_RADIUS_MAX);
        return newRadius;
    }

    private void updateRootsAndRadii(ArrayList<SiteEdge> addedEdges, SiteNode start, SiteNode end) {
        updateGraph(graph, sites);
        ArrayList<Double> oldRadii = new ArrayList<>();
        ArrayList<SiteEdge> updatedEdges = new ArrayList<>();
        Boolean failed = false;

        Bag edges = graph.getEdgesOut(start);

        if (edges == null) {
            return;
        }
        if (edges.size() < 2 ) {
            return;
        }

        Double deltaP = start.pressure - end.pressure;
        Double newFlow = calculateLocalFlow(CAP_RADIUS, addedEdges, deltaP);

        ArrayList<Root> arteries = sites.arteries;
        Integer num_arteries = 0;
        ArrayList<Root> veins = sites.veins;
        Integer num_veins = 0;

        ArrayList<ArrayList<SiteEdge>> pathsArteries = new ArrayList<>();
        for (Root artery : arteries) {
            ArrayList<SiteEdge> path = getPath(graph, artery.node, start);
            if (path.isEmpty()) { continue; }
            pathsArteries.add(path);
            num_arteries++;
        }

        Double arteryFlow = newFlow / num_arteries;
        for (ArrayList<SiteEdge> path : pathsArteries) {
            if (!path.get(0).getFrom().isRoot) {
                throw new ArithmeticException("Root is not the start of the path");
            }

            updatedEdges.addAll(path);
            for (SiteEdge e : path) {
                oldRadii.add(e.radius);
            }

            SiteEdge rootEdge = path.remove(0);

            if (calculateArteryRootRadius(rootEdge, arteryFlow, false) == -1) {
                failed = true;
                break;
            }
            if (updateRadiiOfEdgeList(path, arteryFlow, false) == -1) {
                failed = true;
                break;
            }
        }

        if (failed) {
            resetRadii(updatedEdges, oldRadii);
            return;
        }

        ArrayList<ArrayList<SiteEdge>> pathsVeins = new ArrayList<>();
        for (Root vein : veins) {
            ArrayList<SiteEdge> path = getPath(graph, end, vein.node);
            if (path.isEmpty()) { continue; }
            pathsVeins.add(path);
            num_veins++;
        }

        Double veinFlow = newFlow / num_veins;

        for (ArrayList<SiteEdge> path : pathsVeins) {
            if (!path.get(0).getFrom().isRoot) {
                throw new ArithmeticException("Root is not the start of the path");
            }
            SiteEdge rootEdge = path.remove(path.size() - 1);
            oldRadii.add(rootEdge.radius);
            updatedEdges.add(rootEdge);
            if (calculateArteryRootRadius(rootEdge, veinFlow, false) == -1) {
                failed = true;
                break;
            }
            if (updateRadiiOfEdgeList(path, veinFlow, false) == -1) {
                failed = true;
                break;
            }
        }

        if (num_arteries == 0 || num_veins == 0) {
            LOGGER.info("No arteries or veins found, not updating roots");
            failed = true;
        }

        if (failed) {
            resetRadii(updatedEdges, oldRadii);
            return;
        }
    }

    private void recalcRadii(ArrayList<SiteEdge> ignoredEdges, SiteNode start, SiteNode end, SiteNode intersection){

        updateGraph(graph, sites);
        Bag edges = graph.getEdgesOut(start);

        if (edges == null) {
            return;
        }
        if (edges.size() < 2 ) {
            return;
        }
        // if (((SiteEdge) edges.get(0)).isIgnored || ((SiteEdge) edges.get(1)).isIgnored) {
        //     return;
        // }

        Integer angioIndex = ignoredEdges.contains(edges.get(0)) ? 0 : 1;
        Integer nonAngioIndex = angioIndex ^ 1;
        double deltaP = start.pressure - end.pressure;
        // double deltaP = ((SiteNode) graph.lookupNode(start)).pressure - ((SiteNode) graph.lookupNode(end)).pressure;
        Double divertedFlow = calculateLocalFlow(CAP_RADIUS, ignoredEdges, deltaP);
        Double originalFlow = ((SiteEdge) edges.get(nonAngioIndex)).flow;
        if (divertedFlow > originalFlow) {return ;}
        if (intersection != null) {
            if (intersection.isRoot) {
                updateRadiusToRoot((SiteEdge) edges.get(angioIndex), sites.veins.get(0).node, divertedFlow, false, ignoredEdges);
                return;
                // updateRadiusToRoot((SiteEdge) edges.get(angioIndex), intersection, divertedFlow, false, ignoredEdges);
                // updateRadiusToRoot((SiteEdge) edges.get(nonAngioIndex), intersection, divertedFlow, true, ignoredEdges);
            }

            if (updateRadius((SiteEdge) edges.get(nonAngioIndex), intersection, divertedFlow, true, ignoredEdges) == -1){
                return;
            };

            if (updateRadius((SiteEdge) edges.get(angioIndex), intersection, divertedFlow, false, ignoredEdges) == -1){
                // LOGGER.info("Failed to update radius when increasing size, something seems up");
            };
        }
        else {
            // maybe also TODO: check for perfusion first
            // TODO: check to add flow to radius with new flow after changes to other potential edge, need to do this math out?
            // this should only work for single vein simulations
            SiteNode boundary = sites.veins.get(0).node;
            path(graph, start, boundary);
            if (boundary.prev != null && ((SiteEdge) edges.get(angioIndex)).radius > CAP_RADIUS_MIN) {
                // LOGGER.info("Calculating additional flow to vein");
                updateRadiusToRoot((SiteEdge) edges.get(angioIndex), sites.veins.get(0).node, divertedFlow, false, ignoredEdges);
            }
            else{
                return;
            }
            // updateRadiusToRoot((SiteEdge) edges.get(nonAngioIndex), intersection, divertedFlow, true, ignoredEdges);
        }

    }

    private int updateRadius(SiteEdge edge, SiteNode intersection, double flow, boolean decrease, ArrayList<SiteEdge> ignored){
        ArrayList<SiteEdge> edgesToUpdate = getPath(graph, edge.getTo(), intersection);
        edgesToUpdate.add(0, edge);

        return updateRadiiOfEdgeList(edgesToUpdate, flow, decrease, ignored);
    }

    private int updateRadiiOfEdgeList(ArrayList<SiteEdge> edges, double flow, boolean decrease) {
        return updateRadiiOfEdgeList(edges, flow, decrease, new ArrayList<>());
    }

    private int updateRadiiOfEdgeList(ArrayList<SiteEdge> edges, double flow, boolean decrease, ArrayList<SiteEdge> ignored){
        ArrayList<Double> oldRadii = new ArrayList<>();
        for (SiteEdge e : edges){
            oldRadii.add(e.radius);
            if (ignored.contains(e)) { continue; }
            if (calculateRadius(e, flow, decrease) == -1) {
                resetRadii(edges, oldRadii);
                return -1;
            }
        }
        return 0;
    }

    private int calculateRadius(SiteEdge edge, double flow, boolean decrease){
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);
        Function f = (double r) -> originalFlow + sign * flow - calculateLocalFlow(r, edge.length, deltaP);
        double newRadius;
        newRadius = Solver.bisection(f, 1E-6, 5*CAP_RADIUS_MAX, 1E-6);
        if (newRadius == 1E-6) {
            return -1;
        }
        edge.radius = newRadius;
        return 0;
    }

    private int calculateVeinRootRadius(SiteEdge edge, double flow, boolean decrease){
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f = (double r) -> originalFlow + sign * flow - calculateLocalFlow(r, edge.length, edge.getFrom().pressure - calcPressure(r, edge.type));

        double newRadius = Solver.bisection(f, .5*originalRadius, 1.5*originalRadius, 1E-6);

        if (newRadius == .5*originalRadius || newRadius == Double.NaN) {
            return -1;
        }

        edge.radius = newRadius;
        edge.getTo().pressure = calcPressure(newRadius, edge.type);
        return 0;
    }

    private int calculateArteryRootRadius(SiteEdge edge, double flow, boolean decrease) {
        int sign = decrease ? -1 : 1;
        double originalRadius = edge.radius;
        double deltaP = edge.getFrom().pressure - edge.getTo().pressure;
        double originalFlow = calculateLocalFlow(originalRadius, edge.length, deltaP);

        Function f = (double r) -> originalFlow + sign * flow - calculateLocalFlow(r, edge.length, calcPressure(r, edge.type) - edge.getTo().pressure);

        double newRadius = Solver.bisection(f, .5*originalRadius, 1.5*originalRadius, 1E-6);
        if (newRadius == .5*originalRadius || newRadius == Double.NaN || newRadius ==  1.5*originalRadius) {
            return -1;
        }

        edge.radius = newRadius;
        edge.getFrom().pressure = calcPressure(newRadius, edge.type);
        return 0;
    }

    private void updateRadiusToRoot(SiteEdge edge, SiteNode intersection, double flow, boolean decrease, ArrayList<SiteEdge> ignored){
        ArrayList<Root> veins = sites.veins;
        ArrayList<Double> oldRadii = new ArrayList<>();
        for (Root vein: veins){
            ArrayList<SiteEdge> path = getPath(graph, edge.getTo(), vein.node);
            if (path.isEmpty()) {continue;}
            path.add(0, edge);
            for (SiteEdge e: path){
                oldRadii.add(e.radius);
                if (ignored.contains(e)) { continue; }
                if (e.getTo().isRoot){
                    if (calculateVeinRootRadius(e, flow, decrease) == -1) {
                        resetRadii(path, oldRadii);
                        return;
                    }
                }
                else {
                    if (calculateRadius(e, flow, decrease) == -1) {
                        resetRadii(path, oldRadii);
                        return;
                    }
                }
            }
            break;
        }
    }

    private void resetRadii(ArrayList<SiteEdge> edges, ArrayList<Double> oldRadii){
        for (int i = 0; i < oldRadii.size(); i++){
            edges.get(i).radius = oldRadii.get(i);
        }
    }

    private void addEdgeList(final ArrayList<SiteEdge> list, boolean updateProperties, int edgeType) {
        for (SiteEdge edge : list) {
            graph.addEdge(edge);
        }
    }

    private SiteEdge createNewEdge(final int dir, final SiteNode node, final double tick) {
        SiteNode proposed = sites.offsetNode(node, dir, level);
        proposed.lastUpdate = tick;
        if (sites.checkNode(proposed) && graph.getDegree(node) < 3) {
            SiteEdge edge;
            if (graph.containsNode(proposed)){
                if (graph.getDegree(proposed) > 2 || graph.getEdgesOut(proposed) == null || graph.getEdgesIn(proposed) == null) { return null; }
                SiteNode existing = (SiteNode) graph.lookupNode(proposed);
                edge = new SiteEdge(node, existing, default_type, level, false);
                edge.isAnastomotic = true;
                return edge;
            }
            edge = new SiteEdge(node, proposed, default_type, level, false);

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


