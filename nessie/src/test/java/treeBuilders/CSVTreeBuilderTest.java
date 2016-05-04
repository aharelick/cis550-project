package treeBuilders;

import dbWrapper.TreeNode;

import java.io.File;
import java.util.Set;

/**
 * Created by joeraso on 5/4/16.
 */
public class CSVTreeBuilderTest {

    public static void main(String[] args) {
        DFSRunner dfsRunner = new DFSRunner();
        TreeBuilder treeBuilder = new CSVTreeBuilder();
        Set<TreeNode> graph = treeBuilder.build(new File("data/sample.csv"));
        dfsRunner.traverse(graph);
        System.out.println("end");
    }
}
