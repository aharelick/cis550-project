package test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import engine.Query;
import search.Graphable;

public class EngineTester {
	
	public static void main(String[] args) {
		Set<String> words = new HashSet<String>();
		words.add("Obamacare");
		Map<String, List<Graphable>> results = Query.getResults(words);
		System.out.println("\n\n\n");
		for (Graphable node : results.get("Obamacare")) {
			System.out.println(node);
		}
		System.out.println("\n\n\n");
	}

}
