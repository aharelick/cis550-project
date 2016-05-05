package dbWrapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.*;

/**
 * Created by joeraso on 5/3/16.
 */
public class DBWrapper {
    private long readCapacityUnits = 10L;
    private long writeCapacityUnits = 5L;

    private Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    private AmazonDynamoDBClient client;
    private DynamoDBMapper mapper;
    private AWSCredentials credentials;

    public DBWrapper() throws Exception {
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Couldn't Load Credentials", e);
        }

        client = new AmazonDynamoDBClient(credentials);
        client.setRegion(usWest2);
        mapper = new DynamoDBMapper(client);
    }

    public boolean tableExists(String name) {
        for (String tableName: client.listTables().getTableNames()) {
            if (tableName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Set<TreeNode> fetchNodesWithKey(String key) {
        return fetchNodes("NodeKey", key, "NodeKey-Index");
    }

    public Set<TreeNode> fetchNodesWithValue(String value) {
        return fetchNodes("NodeValue", value, "NodeValue-Index");
    }

    private Set<TreeNode> fetchNodes(String attrKey, String attrVal, String indexName) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(attrKey, new AttributeValue().withS(attrVal));


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

    public void deleteTable(String tableName) {
        client.deleteTable("NessieData");
    }

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
                    .withProjection(new Projection() .withProjectionType(ProjectionType.KEYS_ONLY))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                                                                          .withWriteCapacityUnits(writeCapacityUnits)));
            request.setGlobalSecondaryIndexes(globalSecondaryIndexes);

            client.createTable(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertNodes(Set<TreeNode> nodes) {
        try {
            mapper.batchWrite(new ArrayList<>(nodes), new ArrayList<TreeNode>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
