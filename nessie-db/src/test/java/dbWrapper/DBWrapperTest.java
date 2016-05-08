package dbWrapper;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import linker.CSVLinker;
import linker.Linker;
import treeBuilders.CSVTreeBuilder;
import treeBuilders.TreeBuilder;

import java.io.File;
import java.util.Set;

public class DBWrapperTest {

    static AmazonDynamoDBClient dynamoDB;

	public static void main(String[] args) throws Exception {
        Linker linker = new CSVLinker();
        DBWrapper dbWrapper = new DBWrapper();
        DBWrapperInverted dbWrapperInverted = new DBWrapperInverted();
        TreeBuilder builder = new CSVTreeBuilder();


        // If the table doesn't exist then delete it
        if (!dbWrapper.tableExists("NessieDataTest")) {
            dbWrapper.createTableFromTreeNode();
        }

        // If the table doesn't exist then delete it
        if (!dbWrapperInverted.tableExists("InvertedDataTest")) {
            dbWrapperInverted.createTableFromInvertedNode();
        }


        //BdbWrapper bdbWrapper = new BdbWrapper(new File("/Users/joeraso/Desktop/persist"));


        Set<TreeNode> tree = builder.build(new File("data/philly_educator_information.csv"));
        //dbWrapper.insertNodes(tree);
        //dbWrapper.printAllNodes();

        System.out.println("Started linking");
        linker.createLinks(tree);
        System.out.println("Ended linking");

        System.out.println("Started Inserting");
        dbWrapper.insertNodes(tree);
        System.out.println("Ended Inserting");




        //dbWrapper.insertNodes(tree);
        //Set<TreeNode> fetchedTree = dbWrapper.fetchNodesWithKey("sample.json");
        //dbWrapper.insertNodes(fetchedTree);

        //Set<TreeNode> fetchedNodes = dbWrapper.fetchNodesWithValue("object");
        //dfsRunner.traverse(fetchedNodes);

    }

}