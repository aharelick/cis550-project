package linker;

import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;

import java.util.Set;


public class XMLLinker implements Linker {

    public void createLinks(Set<TreeNode> tree) {
        DBWrapper dbWrapper = new DBWrapper();

        for (TreeNode node: tree) {
            // Object and array aren't meaningful
            if (node.key.equals("tag") || node.value.equals("array")) {
                continue;
            }

            // Case 1: One node's value equals another node's key
            Set<TreeNode> fetchedNodes = dbWrapper.fetchNodesWithKey(node.value);
            for (TreeNode fetchedNode: fetchedNodes) {
                fetchedNode.adj.add(node.id);
                node.adj.add(fetchedNode.id);
            }
            dbWrapper.insertNodes(fetchedNodes);

            // Case 2: One node's value equals another node's value
            fetchedNodes = dbWrapper.fetchNodesWithValue(node.value);
            for (TreeNode fetchedNode: fetchedNodes) {
                fetchedNode.adj.add(node.id);
                node.adj.add(fetchedNode.id);
            }
            dbWrapper.insertNodes(fetchedNodes);
        }
    }

}
