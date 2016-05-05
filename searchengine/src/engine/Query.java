package engine;

import java.util.Set;
import java.util.TreeSet;

import dbWrapper.DBWrapper;
import dbWrapper.DBWrapperInverted;
import dbWrapper.InvertedNode;
import search.GraphNode;
import search.Graphable;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Query {
	
	public static void search(String query) {
		
	}
	
	public static List<String> tokenize(String str) {
		return Arrays.asList(str.split("\\s"));
	}
	
	public static Map<String, List<Graphable>> getResults(Set<String> keywords) {
		DBWrapperInverted idb = DBWrapperInverted.connect();
		DBWrapper 		   db = DBWrapper.connect();
		
		Map<String, List<Graphable>> results = new HashMap<String, List<Graphable>>();
		
		for (String keyword : keywords) {
			Set<String> ids = new TreeSet<String>(); 
			System.out.println("getting " + keyword);
			for (InvertedNode inode : idb.fetchNodesWithValue(keyword)) {
				ids.add(inode.key);
			}
			results.put(keyword, GraphNode.getInstances(ids)); 
		}
		
		return results;
	}

}
