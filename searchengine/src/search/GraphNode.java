package search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbWrapper.DBWrapper;
import dbWrapper.DBWrapperInverted;
import dbWrapper.InvertedNode;
import dbWrapper.TreeNode;

public class GraphNode extends Graphable {
	public String id;
    public String key;
    public String value;
    public Set<String> adjIds;
    public Set<Graphable> adj;
    
    private static Map<String, GraphNode> cache = new HashMap<String, GraphNode>();
    
    public static GraphNode getInstance(String id) {
    	if (cache.containsKey(id)) return cache.get(id);
    	DBWrapper db = DBWrapper.connect();
    	TreeNode  tnode = db.fetchNodeWithId(id);
    	if (tnode == null) return null;
    	else 	 		   return new GraphNode(tnode);
    }
    
    public static List<Graphable> getInstances(Set<String> ids) {
    	DBWrapper db = DBWrapper.connect();
    	DBWrapperInverted idb = DBWrapperInverted.connect();
    	
    	Set<TreeNode> tnodes = db.fetchNodesWithIds(ids);
    	List<Graphable> gnodes = new ArrayList<Graphable>();
    	if (tnodes == null) return null;
    	for (TreeNode tnode : tnodes) {
    		gnodes.add(new GraphNode(tnode));
    	}
    	return gnodes;
    }
	
	private GraphNode(TreeNode tnode) {
		this.id 	= tnode.id;
		this.key    = tnode.key;
		this.value  = tnode.value;
		this.adjIds = tnode.adj;
		cache.put(id, this);
	}
	
	public Set<Graphable> getAdjacent() {
		if (adj != null) return adj;
		DBWrapper db = DBWrapper.connect();
		adj = new HashSet<Graphable>();
		
		Set<TreeNode> adjTree = db.fetchNodesWithIds(adjIds);
		if (adj == null) return null;
		for (TreeNode tnode : adjTree) {
			adj.add(new GraphNode(tnode));
		}
		
		return adj;
	}
	
	public String toString() {
		return "(" + key + ", " + value + ")";
	}
}
