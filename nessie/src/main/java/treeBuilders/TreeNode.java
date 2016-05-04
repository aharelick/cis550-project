package treeBuilders;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by joeraso on 5/3/16.
 */
public class TreeNode {
    int id;
    String    key;
    String    val;
    Set<TreeNode> adj;

    public TreeNode(String key, String val) {
        this.key = key;
        this.val = val;
        this.adj = new HashSet<TreeNode>();
    }

    public boolean equals(TreeNode other) {
        if (other == null) return false;
        else               return id == other.id;
    }

    public String toString() {
        return key + ": " + val;
    }
}
