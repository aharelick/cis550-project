package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import engine.Query;
import search.Graphable;
import test.GraphTester.TestNode;

public class EngineTester {
	
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
		
		Map<String, List<Graphable>> words = new HashMap<String, List<Graphable>>();
		List<Graphable> list = new ArrayList<Graphable>();
		list.add(a);
		list.add(b);
		list.add(c);
		words.put("A", list);
		list = new ArrayList<Graphable>();
		list.add(d);
		list.add(e);
		words.put("B", list);
		
		System.out.println("Paths:");
		for (List<Graphable> path : Query.kShortestPaths(Query.getCartisian(words), 1000)) {
			System.out.println(path);
		}
		
	}

}
