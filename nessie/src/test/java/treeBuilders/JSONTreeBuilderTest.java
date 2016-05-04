package treeBuilders;

import java.io.File;

/**
 * Created by joeraso on 5/3/16.
 */
public class JSONTreeBuilderTest {

    public static void main(String[] args) {
        DFSRunner dfsRunner = new DFSRunner();
        TreeBuilder treeBuilder = new JSONTreeBuilder();
        TreeNode root = treeBuilder.build(new File("data/sample.json"));
        dfsRunner.traverse(root);
    }

}
