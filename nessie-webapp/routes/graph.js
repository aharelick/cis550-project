'use strict';

var Graph = function() {
	this.nodes = [];
	this.root = null;
};

Graph.prototype.insert = function(key, value) {
	var newNode = new Node(key, value);
	this.nodes.push(newNode);
	if (!this.root) {
		this.root = newNode;
	}

	for (var node of this.nodes) {
		if (node.isNeighbor(newNode)) {
			node.neighbors.push(newNode);
			newNode.neighbors.push(node);
		};
	}

	return newNode;
};

Graph.prototype.toString = function() {
	console.log('String representation of node');
};

Graph.prototype.isEmpty = function() {
	return this.nodes.length === 0;
};

var Node = function(key, value) {
	this.value = value;
	this.neighbors = [];
};

Node.prototype.isNeighbor = function(node) {
	return node.value === this.value;
}

module.exports = Graph;