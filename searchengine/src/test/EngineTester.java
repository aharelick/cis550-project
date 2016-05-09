package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import engine.Query;
import engine.Query.Tuple;
import search.Graphable;
import test.GraphTester.TestNode;

public class EngineTester {
	
	public static void main(String[] args) {
		
		TestNode a = new TestNode("a");
		TestNode b = new TestNode("b");
		TestNode c = new TestNode("c");
		TestNode d = new TestNode("d");
		TestNode e = new TestNode("e");
		TestNode f = new TestNode("f");
		
		a.addAdj(b);
		
		b.addAdj(a);
		b.addAdj(c);
		b.addAdj(d);
		
		c.addAdj(b);
		c.addAdj(d);
		
		d.addAdj(b);
		d.addAdj(c);
		d.addAdj(e);
		
		e.addAdj(d);
		e.addAdj(f);
		
		f.addAdj(e);
		
		Map<String, List<Graphable>> words = new HashMap<String, List<Graphable>>();
		List<Graphable> list = new ArrayList<Graphable>();
		list.add(a);
		list.add(b);
		words.put("A", list);
		list = new ArrayList<Graphable>();
		list.add(f);
		list.add(c);
		words.put("B", list);
		list = new ArrayList<Graphable>();
		list.add(d);
		words.put("C", list);	
		
		System.out.println("Cross Product:");
		for (Tuple<Graphable, Graphable> pair : Query.getCartisian(words)) {
			System.out.println(pair);
		}
		
		
		System.out.println("Paths:");
		for (List<Graphable> path : Query.kShortestPaths(Query.getCartisian(words), 1000)) {
			System.out.println(path);
		}
		
		
	}

}
