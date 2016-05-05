package test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import search.Graph;
import search.Graphable;

public class GraphTester {
	
	public static class TestNode extends Graphable {
		
		String key;
		Set<Graphable> adj = new HashSet<Graphable>();
		
		public TestNode(String key) {
			this.key = key;
		}

		public void addAdj(Graphable node) { 
			this.adj.add(node);
		}
		
		@Override
		public Set<Graphable> getAdjacent() {
			return this.adj;
		}
		
		public String toString() {
			return key;
		}
		public boolean equals(TestNode t) {
			return key.equals(t.key);
			
		}
		
	}
	
	public static void main(String[] args) {
		TestNode a = new TestNode("a");
		TestNode b = new TestNode("b");
		TestNode c = new TestNode("c");
		TestNode d = new TestNode("d");
		TestNode e = new TestNode("e");
		
		a.addAdj(b);
		a.addAdj(c);
		a.addAdj(d);
		
		b.addAdj(a);
		b.addAdj(c);
		b.addAdj(e);
		
		c.addAdj(a);
		c.addAdj(b);
		c.addAdj(e);
		
		d.addAdj(a);
		d.addAdj(e);
		
		e.addAdj(b);
		e.addAdj(c);
		e.addAdj(d);
		
		List<List<Graphable>> paths = Graph.shortestPaths(d, a);
		System.out.println("Starting.");
		System.out.println(paths.size());
		for (List<Graphable> path : paths) {
			System.out.println(path);
		}
		
		
		
	}

}
