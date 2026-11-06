package arcade.potts.env.component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Perfusion of vessel segments, without hemodynamics.
 *
 * <p>A segment is perfused only if blood could pass through it, not merely if it is connected to a
 * root. All roots are merged into one outside node, where blood enters and leaves. A segment is
 * perfused if it is not a bridge (a segment whose removal would split the network) and it is in the
 * same 2-edge-connected component as the outside node, so it lies on a loop that blood can enter
 * and leave. Dead-end branches, such as sprouts and cut stubs, are therefore unperfused until a
 * loop closes. Bridges are found with Tarjan's algorithm, written as an iterative depth-first
 * search so that large networks cannot overflow the stack.
 */
public final class PottsVesselPerfusion {
    /** Hidden utility class constructor. */
    private PottsVesselPerfusion() {}

    /**
     * Sets the perfused flag of every segment in the network.
     *
     * @param network the vessel network
     * @return the number of perfused segments
     */
    public static int update(PottsVesselNetwork network) {
        List<PottsVesselNode> nodes = network.getNodes();
        List<PottsVesselEdge> segments = network.getSegments();

        // Map nodes to indices, with all roots merged into one outside node.
        int outside = 0;
        Map<PottsVesselNode, Integer> index = new HashMap<>();
        int count = 1;
        for (PottsVesselNode node : nodes) {
            index.put(node, node.isRoot() ? outside : count++);
        }

        int m = segments.size();
        int[] ends = new int[2 * m];
        List<List<Integer>> adjacent = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            adjacent.add(new ArrayList<>());
        }
        for (int e = 0; e < m; e++) {
            PottsVesselEdge segment = segments.get(e);
            int u = index.get(segment.getStart());
            int v = index.get(segment.getEnd());
            ends[2 * e] = u;
            ends[2 * e + 1] = v;
            adjacent.get(u).add(e);
            if (v != u) {
                adjacent.get(v).add(e);
            }
        }

        boolean[] bridge = findBridges(count, ends, adjacent);

        // Find the nodes joined to the outside node through segments that are not bridges.
        boolean[] reached = new boolean[count];
        boolean hasRoots = !network.getRoots().isEmpty();
        if (hasRoots) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            reached[outside] = true;
            queue.add(outside);
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (int e : adjacent.get(u)) {
                    if (bridge[e]) {
                        continue;
                    }
                    int v = ends[2 * e] == u ? ends[2 * e + 1] : ends[2 * e];
                    if (!reached[v]) {
                        reached[v] = true;
                        queue.add(v);
                    }
                }
            }
        }

        int perfused = 0;
        for (int e = 0; e < m; e++) {
            boolean isPerfused = hasRoots && !bridge[e] && reached[ends[2 * e]];
            segments.get(e).setPerfused(isPerfused);
            if (isPerfused) {
                perfused++;
            }
        }
        return perfused;
    }

    /**
     * Finds the bridges of an undirected multigraph.
     *
     * <p>Self-loops are never bridges, and parallel segments are distinct, so neither is a bridge.
     *
     * @param count the number of nodes
     * @param ends the two end nodes of each segment, as {u0, v0, u1, v1, ...}
     * @param adjacent the segments at each node
     * @return {@code true} for each segment that is a bridge
     */
    static boolean[] findBridges(int count, int[] ends, List<List<Integer>> adjacent) {
        int m = ends.length / 2;
        boolean[] bridge = new boolean[m];
        int[] discovered = new int[count];
        int[] low = new int[count];
        Arrays.fill(discovered, -1);

        int[] parentEdge = new int[count];
        int[] next = new int[count];
        int time = 0;

        for (int start = 0; start < count; start++) {
            if (discovered[start] >= 0) {
                continue;
            }
            ArrayDeque<Integer> stack = new ArrayDeque<>();
            discovered[start] = time;
            low[start] = time;
            time++;
            parentEdge[start] = -1;
            next[start] = 0;
            stack.push(start);

            while (!stack.isEmpty()) {
                int u = stack.peek();
                List<Integer> edges = adjacent.get(u);
                if (next[u] < edges.size()) {
                    int e = edges.get(next[u]++);
                    if (e == parentEdge[u]) {
                        continue;
                    }
                    int v = ends[2 * e] == u ? ends[2 * e + 1] : ends[2 * e];
                    if (v == u) {
                        continue;
                    }
                    if (discovered[v] < 0) {
                        discovered[v] = time;
                        low[v] = time;
                        time++;
                        parentEdge[v] = e;
                        next[v] = 0;
                        stack.push(v);
                    } else {
                        low[u] = Math.min(low[u], discovered[v]);
                    }
                } else {
                    stack.pop();
                    int e = parentEdge[u];
                    if (e >= 0) {
                        int p = ends[2 * e] == u ? ends[2 * e + 1] : ends[2 * e];
                        low[p] = Math.min(low[p], low[u]);
                        if (low[u] > discovered[p]) {
                            bridge[e] = true;
                        }
                    }
                }
            }
        }
        return bridge;
    }
}
