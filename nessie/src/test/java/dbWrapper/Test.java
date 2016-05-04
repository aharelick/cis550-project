package dbWrapper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class Test {

    static AmazonDynamoDBClient dynamoDB;
    static AWSCredentials credentials;

    private static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
    }


	public static void main(String[] args) throws Exception {
        init();
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		Node item = new Node();

		//item.setId(102); //do i need this?
		item.setValue("The information associated with this node?");
		//item.addNeighbor(23049); //
		mapper.save(item);
        System.out.println(item.getId());
	}

}