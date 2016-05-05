package search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Graph {
	
	public static List<List<Graphable>> shortestPaths(Graphable start, Graphable end) {
		
		Queue<Graphable> queue = new LinkedList<Graphable>();
		start.depth = 0;
		queue.add(start);
		
		boolean found = false;
		
		while (!queue.isEmpty()) {
			Graphable n = queue.poll();
			
			System.out.println(n);
			
			for (Graphable adj : n.getAdjacent()) {
				if (adj.equals(end)) found = true;
				
				if (adj.depth == Integer.MAX_VALUE) {
					adj.depth = n.depth + 1;
					adj.parents.add(n);
					queue.add(adj);
				} else if (adj.depth == n.depth + 1) {
					adj.parents.add(n);
				}
			}
		}
		System.out.println(found);
		if (!found) return new ArrayList<List<Graphable>>();
		else 		return generatePaths(end);
	}

	private static List<List<Graphable>> generatePaths(Graphable end) {
		List<List<Graphable>> paths = new ArrayList<List<Graphable>>();
		
		if (end.parents.size() == 0) {
			List<Graphable> path = new ArrayList<Graphable>();
			path.add(end);
			paths.add(path);
		} else {
			for (Graphable parent : end.parents) {
				List<List<Graphable>> stubs = generatePaths(parent);
				for (List<Graphable> path : stubs) {
					path.add(end);
				}
				paths.addAll(stubs);
			}	
		}
	
		return paths;
	}

}
