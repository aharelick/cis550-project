package treeBuilders;

import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;

import java.util.*;

/**
 * Created by joeraso on 5/3/16.
 */
public class DFSRunner {
    private DBWrapper dbWrapper;

    private Map<String, TreeNode> createMap(Set<TreeNode> graph) throws Exception {
        dbWrapper = new DBWrapper();
        Map<String, TreeNode> graphMap = new HashMap<>();
        for (TreeNode vertex : graph) {
            graphMap.put(vertex.id, vertex);
        }
        return graphMap;
    }

    public void traverse(Set<TreeNode> graph) {
        Map<String, TreeNode> graphMap;
        try {
            graphMap = createMap(graph);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        TreeNode root = graphMap.values().iterator().next();
        List<String> discovered = new ArrayList<>();
        Stack<TreeNode> stack = new Stack();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode v = stack.pop();
            if (!discovered.contains(v.id)) {
                System.out.println(v);
                discovered.add(v.id);
                Set<TreeNode> adjacent = dbWrapper.fetchNodesWithIds(v.adj);
                for (TreeNode adj : adjacent) {
                    stack.push(adj);
                }
            }
        }
    }
}
