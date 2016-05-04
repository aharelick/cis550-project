package dbWrapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import treeBuilders.JSONTreeBuilder;
import treeBuilders.TreeBuilder;

import java.io.File;
import java.util.List;
import java.util.Set;

public class DBWrapperTest {

    static AmazonDynamoDBClient dynamoDB;

	public static void main(String[] args) throws Exception {
        DBWrapper dbWrapper = new DBWrapper();
        TreeBuilder builder = new JSONTreeBuilder();


        // If the table doesn't exist then delete it
        if (!dbWrapper.tableExists("NessieData")) {
            dbWrapper.createTableFromTreeNode();
        }


        Set<TreeNode> tree = builder.build(new File("data/sample.json"));
        dbWrapper.insertGraph(tree);
        List<TreeNode> fetchedTree = dbWrapper.fetchWithKey("sample.json");
        System.out.println("Made it");
    }

}