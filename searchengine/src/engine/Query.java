package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import dbWrapper.DBWrapper;
import dbWrapper.DBWrapperInverted;
import dbWrapper.InvertedNode;
import search.Graph;
import search.GraphNode;
import search.Graphable;

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
			for (InvertedNode inode : idb.fetchNodesWithValue(keyword)) {
				ids.add(inode.key);
			}
			results.put(keyword, GraphNode.getInstances(ids)); 
		}
		
		return results;
	}
	
	public static Iterable<Tuple<Graphable, Graphable>> getCartisian(final Map<String, List<Graphable>> nodes) {
		
		final List<String> keys = new ArrayList<String>();
		keys.addAll(nodes.keySet());
		
		return new Iterable<Tuple<Graphable, Graphable>>() {

			@Override
			public Iterator<Tuple<Graphable, Graphable>> iterator() {
				return new Iterator<Tuple<Graphable, Graphable>>() {
					int from	 = 0;
					int fromNode = 0;
					int to	     = 1;
					int toNode   = 0;

					@Override
					public boolean hasNext() {
						return from < keys.size();
					}

					@Override
					public Tuple<Graphable, Graphable> next() {
						List<Graphable> fromlist = nodes.get(keys.get(from));
						List<Graphable> tolist   = nodes.get(keys.get(to));
						
						Graphable a = fromlist.get(fromNode);
						Graphable b = tolist.get(toNode);
						
						do {
							toNode = (toNode + 1) % tolist.size();
							if (toNode == 0) {
								fromNode = (fromNode + 1) % fromlist.size();
								if (fromNode == 0) {
									to = (to + 1) % keys.size();
									if (to == 0) {
										from = (from + 1) % keys.size();
									}
								}
							}
							
							 if (from == 0 && to == 0) {
								from = Integer.MAX_VALUE;
								break;
							}
							
							
						} while (from == to);
						return new Tuple<Graphable, Graphable>(a, b);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
			
		};
	}
	
	public static List<List<Graphable>> kShortestPaths(Iterable<Tuple<Graphable, Graphable>> pairs, int k) {
		PriorityQueue<List<Graphable>> paths = new PriorityQueue<List<Graphable>>(k, new Comparator<List<Graphable>>() {
			@Override
			public int compare(List<Graphable> o1, List<Graphable> o2) {
				return o1.size() - o2.size();
			}
		});
		
		for (Tuple<Graphable, Graphable> pair : pairs) {
			for (List<Graphable> path : Graph.shortestPaths(pair.a, pair.b)) {
				paths.add(path);
			}
		}
		
		List<List<Graphable>> kpaths = new ArrayList<List<Graphable>>();
		for (int i = 0; i < k && !paths.isEmpty(); i++) {
			kpaths.add(paths.poll());
		}
		return kpaths;
		
	}
	
	public static class Tuple<A, B> {
		A a;
		B b;
		
		Tuple(A a, B b) {
			this.a = a;
			this.b = b;
		}
		
		public String toString() {
			return "(" + a + ", " + b + ")";
		}
	}

}
