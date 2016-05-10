
this.onmessage = function(e) {
  console.log(e);
}

var helper = function(nodes, invertedNodes, callback) {
  async.eachSeries(nodes, function(node, callbackTwo) {
    async.waterfall([
      // Get all of the inverted neighbors of this node
      async.apply(function(node, callbackThree) {
        console.log('Getting inverted neighbors');
        terms = node.key.split(' ');
        InvertedNode.find({term: {$in: terms}}, function(err, invertedNeighbors) {
          if (err) {
            return callbackThree(err);
          } else {
            callbackThree(null, node, invertedNeighbors);
          }
        });
      }, node),
      // Using the inverted neighbor ids, get the neighbor nodes
      function(node, invertedNeighbors, callbackThree) {
        console.log('Getting neighbors');
        neighborIds = [];
        invertedNeighbors.forEach(function(invertedNeighbor, index) {
          neighborIds.push(invertedNeighbor.nodeId);
        });

        Node.find({_id: {$in: neighborIds}}, function(err, neighbors) {
          if (err) {
            return callbackThree(err);
          } else {
            callbackThree(null, node, neighbors)
          }
        });
      },
      // Add neighbors to the adjacency lists
      function(node, neighbors, callbackThree) {
        console.log('Adding neightbors to the adjacency list');
        neighbors.forEach(function(neighbor, index) {
          if (!(neighbor._id in node.neighbors) && !(node._id in neighbor.neighbors)) {
            node.neighbors.push(neighbor._id);
            neighbor.neighbors.push(node._id);
            neighbor.save();
          }
        })
        callbackThree(null);
      }], function(err, result) {
        Node.insertMany(nodes);
        InvertedNode.insertMany(invertedNodes);
        callbackTwo(err);
      });
  }, function(err) {
    callback(err);
  });
};