var Node = function(value) {
	this.value = value;
	this.neighbors = [];
};

Node.prototype.isNeighbor = function(node) {
	return node.value === this.value;
}

module.exports = Node;