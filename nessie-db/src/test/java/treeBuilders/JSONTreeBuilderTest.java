package treeBuilders;

import dbWrapper.TreeNode;

import java.io.File;
import java.util.Set;

/**
 * Created by joeraso on 5/3/16.
 */
public class JSONTreeBuilderTest {

    public static void main(String[] args) {
        DFSRunner dfsRunner = new DFSRunner();
        TreeBuilder treeBuilder = new JSONTreeBuilder();
        Set<TreeNode> graph = treeBuilder.build(new File("data/sample.json"));
        dfsRunner.traverse(graph);
    }

}
