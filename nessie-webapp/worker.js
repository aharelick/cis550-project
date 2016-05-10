var kue = require('kue');
var url = require('url');
var async = require('async');
var InvertedNode = require('./models/InvertedNode');
var Node = require('./models/Node');
var mongoose = require('mongoose');
var redis = require('kue/node_modules/redis');


mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/nessie');
mongoose.connection.on('error', function() {
  console.error('MongoDB Connection Error. Please make sure that MongoDB is running.');
});


kue.redis.createClient = function() {
  var redisUrl = url.parse(process.env.REDIS_URL || 'redis://localhost:6379');
  var client = redis.createClient(redisUrl.port, redisUrl.hostname);
  if (redisUrl.auth) {
    client.auth(redisUrl.auth.split(":")[1]);
  }
  return client;
};

var jobs = kue.createQueue();

// see https://github.com/learnBoost/kue/ for how to do more than one job at a time
jobs.process('upload', function(job, done) {
	var nodes = job.data.nodes;
	var invertedNodes = job.data.invertedNodes;
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
	          	console.log(invertedNeighbors);
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
		console.log("done");
	   	done();
	});
});
