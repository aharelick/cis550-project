package treeBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by joeraso on 5/3/16.
 */
public class DFSRunner {

    public void traverse(TreeNode root) {
        List<TreeNode> discovered = new ArrayList<>();
        Stack<TreeNode> stack = new Stack();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode v = stack.pop();
            if (!discovered.contains(v)) {
                System.out.println(v);
                discovered.add(v);
                for (TreeNode adj : v.adj) {
                    stack.push(adj);
                }
            }
        }
    }
}
