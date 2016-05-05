package dbWrapper;

import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName="NessieData")
public class TreeNode {

    public String id;
    public String key;
    public String value;
    public Set<String> adj;

    public TreeNode() {
        this("","");
    }

    public TreeNode(String key, String value) {
        this.key = key;
        this.value = value;
        this.id = UUID.randomUUID().toString();
        adj = new HashSet<>();
    }

    @DynamoDBHashKey(attributeName="Id")
    public String getId() { return id;}
    public void setId(String id) {this.id = id;}

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "NodeKey-Index", attributeName = "NodeKey")
    public String getNodeKey() {return this.key;}
    public void setNodeKey(String key) {this.key = key;}

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "NodeValue-Index", attributeName="NodeValue")
    public String getNodeValue() {return value; }
    public void setNodeValue(String value) { this.value = value; }

    @DynamoDBAttribute(attributeName = "Neighbors")
    public Set<String> getAdj() { return adj; }
    public void setAdj(Set<String> neighbors) { this.adj = neighbors; }
    public void addNeighbor(String neighbor) {
    	if (this.adj == null) {
    		this.adj = new HashSet<String>();
    	}
    	this.adj.add(neighbor);
    }

    public String toString() {
        return this.key + ": " + this.value;
    }
}