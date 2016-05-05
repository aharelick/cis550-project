package search;

import java.util.HashSet;
import java.util.Set;


public abstract class Graphable {
	
	int depth = Integer.MAX_VALUE;
	Set<Graphable> parents = new HashSet<Graphable>();
	
	public abstract Set<Graphable> getAdjacent();
}
