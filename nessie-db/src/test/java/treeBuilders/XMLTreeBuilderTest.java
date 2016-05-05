package treeBuilders;

import dbWrapper.TreeNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by joeraso on 5/3/16.
 */
public class XMLTreeBuilderTest {

    public static void main(String[] args) {
        DFSRunner dfsRunner = new DFSRunner();
        TreeBuilder treeBuilder = new XMLTreeBuilder();
        Set<TreeNode> graph = treeBuilder.build(new File("data/sample.xml"));
        dfsRunner.traverse(graph);
    }
}
