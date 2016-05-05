package treeBuilders;

import dbWrapper.TreeNode;

import java.io.File;
import java.util.Set;

public interface TreeBuilder {
    public Set<TreeNode> build(File file);
}
