package dbWrapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.*;


public class DBWrapper {
    private long readCapacityUnits = 50L;
    private long writeCapacityUnits = 50L;

    private Region usWest2 = Region.getRegion(Regions.US_EAST_1);
    private AmazonDynamoDBClient client;
    private DynamoDBMapper mapper;
    private AWSCredentials credentials;

    public DBWrapper() {
        try {
            credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Couldn't Load Credentials", e);
        }

        client = new AmazonDynamoDBClient(credentials);
        client.withEndpoint("http://localhost:8000");
        mapper = new DynamoDBMapper(client);
    }

    // Check if table with 'name' exists
    public boolean tableExists(String name) {
        for (String tableName: client.listTables().getTableNames()) {
            if (tableName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void printAllNodes() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<TreeNode> scanResult = mapper.scan(TreeNode.class, scanExpression);
        for (TreeNode node : scanResult) {
            System.out.println(node);
        }
    }

    // Insert all nodes in the given set
    public void insertNodes(Set<TreeNode> nodes) {
        try {
            mapper.batchWrite(new ArrayList<>(nodes), new ArrayList<TreeNode>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TreeNode fetchNodeWithId(String id) {
        try {
            return mapper.load(TreeNode.class, id);
        } catch (ResourceNotFoundException e) {
            System.out.println("No nodes with given id found");
            return null;
        }
    }

    public Set<TreeNode> fetchNodesWithIds(Set<String> ids) {
        Map<Class<?>, List<KeyPair>> keyPairMap = new HashMap<>();
        List<KeyPair> keyPairList = new ArrayList<>();
        for (String id : ids) {
            KeyPair keyPair = new KeyPair();
            keyPair.setHashKey(id);
            keyPairList.add(keyPair);
        }
        keyPairMap.put(TreeNode.class, keyPairList);
        Set<TreeNode> matchedSet;
        try {
            matchedSet = new HashSet<>();
            for(List<Object> treeList : mapper.batchLoad(keyPairMap).values()) {
                for (Object obj : treeList) {
                    matchedSet.add((TreeNode) obj);
                }
            }
        } catch (ResourceNotFoundException e) {
            System.out.println("No nodes with given ids found");
            return null;
        }

        return matchedSet;
    }

    // Get all nodes with a given key
    public Set<TreeNode> fetchNodesWithKey(String key) {
        return fetchNodes("NodeKey", key, "NodeKey-Index");
    }

    // Get all nodes with a given value
    public Set<TreeNode> fetchNodesWithValue(String value) {
        return fetchNodes("NodeValue", value, "NodeValue-Index");
    }

    // Helper function to retrieve nodes
    private Set<TreeNode> fetchNodes(String attrKey, String attrVal, String indexName) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":"+ attrKey, new AttributeValue().withS(attrVal));


        DynamoDBQueryExpression<TreeNode> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression = queryExpression.withKeyConditionExpression(attrKey + "= :" + attrKey);
        queryExpression = queryExpression.withExpressionAttributeValues(eav);
        queryExpression.withIndexName(indexName);
        queryExpression.withConsistentRead(false);

        List<TreeNode> nodes = null;
        try {
            nodes = mapper.query(TreeNode.class, queryExpression);
        } catch (ResourceNotFoundException e) {
            System.out.println("Couldn't find any trees matching key");
        }

        return new HashSet<>(nodes);
    }

    // Delete a table with a given name
    public void deleteTable(String tableName) {
        client.deleteTable(tableName);
    }

    // Create the table with the TreeNode Schema
    public void createTableFromTreeNode() {
        try {
            CreateTableRequest request = mapper.generateCreateTableRequest(TreeNode.class);
            request  = request.withProvisionedThroughput( new ProvisionedThroughput()
                    .withReadCapacityUnits(readCapacityUnits)
                    .withWriteCapacityUnits(writeCapacityUnits));


            ArrayList<GlobalSecondaryIndex> globalSecondaryIndexes = new ArrayList<>();
            globalSecondaryIndexes.add(new GlobalSecondaryIndex()
                    .withIndexName("NodeKey-Index")
                    .withKeySchema(
                            new KeySchemaElement().withAttributeName("NodeKey").withKeyType(KeyType.HASH))
                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                                                                          .withWriteCapacityUnits(writeCapacityUnits)));

            globalSecondaryIndexes.add(new GlobalSecondaryIndex()
                    .withIndexName("NodeValue-Index")
                    .withKeySchema(new KeySchemaElement().withAttributeName("NodeValue").withKeyType(KeyType.HASH))
                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                                                                          .withWriteCapacityUnits(writeCapacityUnits)));
            request.setGlobalSecondaryIndexes(globalSecondaryIndexes);

            client.createTable(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
