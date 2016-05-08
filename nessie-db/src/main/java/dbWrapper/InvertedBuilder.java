package dbWrapper;

import java.util.HashSet;
import java.util.Set;

public class InvertedBuilder {

	public static Set<InvertedNode> build(Set<TreeNode> tree) {
		Set<InvertedNode> inverted = new HashSet<InvertedNode>();
		for (TreeNode t : tree) {
			//We want to tokenize the value, and add a mapping from key->token
			String key = t.getId();
			String[] tokens = t.getNodeValue().split("\\s");
			for (String s : tokens) {
				if (s.isEmpty()) continue;
				InvertedNode n = new InvertedNode(key, s);
				inverted.add(n);
			}
		}
		return inverted;
	}

}