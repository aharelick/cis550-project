package linker;

import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;

import java.util.Set;

/**
 * Created by joeraso on 5/5/16.
 */
public class JSONLinker implements Linker {

    public void createLinks(Set<TreeNode> tree) {
        DBWrapper dbWrapper = new DBWrapper();

        for (TreeNode node: tree) {
            // Object and array aren't meaningful
            if (node.value.equals("object") || node.value.equals("array")) {
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
