package dbWrapper;

import dbWrapper.TreeNode;

import java.util.HashSet;
import java.util.Set;

public class InvertedBuilder {

	public static Set<TreeNode> build(Set<TreeNode> tree) {
		Set<TreeNode> inverted = new HashSet<TreeNode>();
		for (TreeNode t : tree) {
			//We want to tokenize the value, and add a mapping from key->token
			String key = t.getNodeKey();
			String[] tokens = t.getNodeValue().split("\\s");
			for (String s : tokens) {
				TreeNode n = new TreeNode(key, s);
			}
		}
		return inverted;

	}

}