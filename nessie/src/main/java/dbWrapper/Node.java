package dbWrapper;

import java.util.Set;
import java.util.HashSet;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="NessieData")
public class Node {

    private Integer id;
    private String value;
    private Set<Integer> neighbors;

    @DynamoDBHashKey(attributeName="Id")
    public Integer getId() { return id;}
    public void setId(Integer id) {this.id = id;}

    @DynamoDBAttribute(attributeName="Value")
    public String getValue() {return value; }
    public void setValue(String value) { this.value = value; }

    @DynamoDBAttribute(attributeName = "Neighbors")
    public Set<Integer> getNeighbors() { return neighbors; }
    public void setNeighbors(Set<Integer> neighbors) { this.neighbors = neighbors; }
    public void addNeighbor(Integer neighbor) {
    	if (this.neighbors == null) {
    		this.neighbors = new HashSet<Integer>();
    	}
    	this.neighbors.add(neighbor);
    }
}