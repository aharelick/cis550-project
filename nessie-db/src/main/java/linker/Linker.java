package linker;

import dbWrapper.DBWrapper;
import dbWrapper.TreeNode;

import java.util.List;
import java.util.Set;

/**
 * Created by joeraso on 5/3/16.
 *
 * Linker will pull trees from dynamoDB and create linkages between them
 */
public class Linker {

    public void createLinks(Set<TreeNode> tree) throws Exception {
        DBWrapper dbWrapper = new DBWrapper();

        for (TreeNode node: tree) {

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
