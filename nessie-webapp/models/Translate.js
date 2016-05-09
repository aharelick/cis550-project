var formattedObj = {}; //The object that should be passed into searchResults
var edgeCount = 0;

var translate = function(paths) {
	formattedObj.nodes = [];
	formattedObj.edges = [];

	for (var i in paths) {
		var prevId = null;
		for (var j = 0; j < paths[i].length; j++) {
			var currID = paths[i][j]._id;

			//Make the node if it is new
			if (!nodeExists(currID)) {
				var node = {};
				node.id = currID;
				node.docID = paths[i][j].docId;
				node.label = paths[i][j].key;
				formattedObj.nodes.push(node);
			}

			//Add the edge to prev node
			if (j > 0) {
				//add an edge to preceeding node
				var edge = {};
				edge.id = edgeCount;
				edgeCount++;
				edge.source = prevId;
				edge.target = currID;
				formattedObj.edges.push(edge);
			}

			//Update the previous pointer in path
			prevId = currID;
		}
	}
}

//See if the node has been created already
var nodeExists = function(id) {
	if (formattedObj != null) {
		if (formattedObj.nodes != null) {
			for (var i in formattedObj.nodes) {
				if (formattedObj.nodes[i].id === id) {
					return true;
				}
			}
		}
	}
	return false;
}

translate([
	[{"_id" : "a", "parent" : "b", "docId" : "http://", "key" : "apple", "neighbors" : "['a', 'b']"},
	 {"_id" : "b", "parent" : "b", "docId" : "http://", "key" : "banana", "neighbors" : "['a', 'b']"},
	 {"_id" : "c", "parent" : "b", "docId" : "http://", "key" : "clementine", "neighbors" : "['a', 'b']"}],
	[{"_id" : "a", "parent" : "b", "docId" : "http://", "key" : "apple", "neighbors" : "['a', 'b']"},
	 {"_id" : "d", "parent" : "b", "docId" : "http://", "key" : "dog", "neighbors" : "['a', 'b']"},
	 {"_id" : "f", "parent" : "b", "docId" : "http://", "key" : "fruit", "neighbors" : "['a', 'b']"},
	 {"_id" : "b", "parent" : "b", "docId" : "http://", "key" : "banana", "neighbors" : "['a', 'b']"}
	]]);
console.log(formattedObj);