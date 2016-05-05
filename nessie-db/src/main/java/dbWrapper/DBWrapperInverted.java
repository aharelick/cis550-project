package dbWrapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.*;

public class DBWrapperInverted {
    private long readCapacityUnits = 50L;
    private long writeCapacityUnits = 50L;

    private Region usEast = Region.getRegion(Regions.US_EAST_1);
    private AmazonDynamoDBClient client;
    private DynamoDBMapper mapper;
    private AWSCredentials credentials;
    
    private static DBWrapperInverted instance;
    
    public static DBWrapperInverted connect() {
    	if (instance != null) return instance;
    	
    	try {
    		instance = new DBWrapperInverted();
    	} catch (Exception e) {
    		return null;
    	}
    	return instance;
    }

    public DBWrapperInverted() throws Exception {
        try {
            credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Couldn't Load Credentials", e);
        }

        client = new AmazonDynamoDBClient(credentials);
        client.setRegion(usEast);
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

    public Set<InvertedNode> fetchNodesWithValue(String value) {
        return fetchNodes("NodeValue", value, "NodeValue-Index");
    }

    private Set<InvertedNode> fetchNodes(String attrKey, String attrVal, String indexName) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":" + attrKey, new AttributeValue().withS(attrVal));


        DynamoDBQueryExpression<InvertedNode> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression = queryExpression.withKeyConditionExpression(attrKey + "= :" + attrKey);
        queryExpression = queryExpression.withExpressionAttributeValues(eav);
        queryExpression.withIndexName(indexName);
        queryExpression.withConsistentRead(false);

        List<InvertedNode> nodes = null;
        try {
            nodes = mapper.query(InvertedNode.class, queryExpression);
        } catch (ResourceNotFoundException e) {
            System.out.println("Couldn't find any trees matching key");
        }

        return new HashSet<>(nodes);
    }

    public void deleteTable(String tableName) {
        client.deleteTable(tableName);
    }

    public void createTableFromInvertedNode() {
        try {
            CreateTableRequest request = mapper.generateCreateTableRequest(InvertedNode.class);
            request  = request.withProvisionedThroughput( new ProvisionedThroughput()
                    .withReadCapacityUnits(readCapacityUnits)
                    .withWriteCapacityUnits(writeCapacityUnits));


            ArrayList<GlobalSecondaryIndex> globalSecondaryIndexes = new ArrayList<>();
            globalSecondaryIndexes.add(new GlobalSecondaryIndex()
                    .withIndexName("NodeValue-Index")
                    .withKeySchema(
                            new KeySchemaElement().withAttributeName("NodeValue").withKeyType(KeyType.HASH))
                    .withProjection(new Projection() .withProjectionType(ProjectionType.ALL))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                                                                          .withWriteCapacityUnits(writeCapacityUnits)));
            request.setGlobalSecondaryIndexes(globalSecondaryIndexes);

            client.createTable(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertNodes(Set<InvertedNode> nodes) {
        try {
            mapper.batchWrite(new ArrayList<>(nodes), new ArrayList<InvertedNode>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
