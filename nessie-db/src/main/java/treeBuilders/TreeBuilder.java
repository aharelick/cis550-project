package treeBuilders;

import dbWrapper.TreeNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

public interface TreeBuilder {
    public Set<TreeNode> build(File file);
    public Set<TreeNode> build(InputStream stream, String filename);
}
