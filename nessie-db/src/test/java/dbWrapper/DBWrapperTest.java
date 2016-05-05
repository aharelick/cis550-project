package dbWrapper;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import treeBuilders.DFSRunner;
import treeBuilders.JSONTreeBuilder;
import treeBuilders.TreeBuilder;

import java.io.File;
import java.util.Set;

public class DBWrapperTest {

    static AmazonDynamoDBClient dynamoDB;

	public static void main(String[] args) throws Exception {
        DFSRunner dfsRunner = new DFSRunner();
        DBWrapper dbWrapper = new DBWrapper();
        TreeBuilder builder = new JSONTreeBuilder();

        //dbWrapper.deleteTable("NessieData");

        // If the table doesn't exist then delete it
        if (!dbWrapper.tableExists("NessieDataTest")) {
            dbWrapper.createTableFromTreeNode();
        }

        //Set<TreeNode> tree = builder.build(new File("data/sample.json"));
        //dbWrapper.insertNodes(tree);
        Set<TreeNode> fetchedTree = dbWrapper.fetchNodesWithKey("sample.json");
        dbWrapper.insertNodes(fetchedTree);

        Set<TreeNode> fetchedNodes = dbWrapper.fetchNodesWithValue("object");
        dfsRunner.traverse(fetchedNodes);

    }

}