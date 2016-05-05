package treeBuilders;

import dbWrapper.TreeNode;

import java.util.*;

/**
 * Created by joeraso on 5/3/16.
 */
public class DFSRunner {

    private Map<String, TreeNode> createMap(Set<TreeNode> graph) {
        Map<String, TreeNode> graphMap = new HashMap<>();
        for (TreeNode vertex : graph) {
            graphMap.put(vertex.id, vertex);
        }
        return graphMap;
    }

    public void traverse(Set<TreeNode> graph) {
        Map<String, TreeNode> graphMap = createMap(graph);

        TreeNode root = graphMap.values().iterator().next();
        List<TreeNode> discovered = new ArrayList<>();
        Stack<TreeNode> stack = new Stack();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode v = stack.pop();
            if (!discovered.contains(v)) {
                System.out.println(v);
                discovered.add(v);
                for (String adj : v.adj) {
                    stack.push(graphMap.get(adj));
                }
            }
        }
    }
}
