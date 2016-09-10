var assert = require('chai').assert;
var Graph = require('../routes/graph');

var graph;

beforeEach(function() {
	graph = new Graph();
});

describe('Graph', function() {
  describe('initial state: ', function() {
  	it ('nodes is initially empty', function() {
  		assert.equal(graph.nodes.length, 0);
  	});

  	it ('root is initially null', function() {
  		assert.isNull(graph.root);
  	});
  });

  describe('first insertion:', function() {
  	it('root is not null after first insert', function() {
  		graph.insert('one', 1);
		assert.isNotNull(graph.root);
		assert.isFalse(graph.isEmpty());
  	});

  	it('there is one node in the graph after insert', function() {
  		graph.insert('one', 1);
  		assert.isTrue(graph.nodes.length === 1);
  	});
  });

  describe('general insert', function(){
  		var nodeOne;
  		var nodeTwo;
  		var nodeThree;

  		beforeEach(function() {
  			nodeOne = graph.insert('dog1', 'chihuahua');
  			nodeTwo = graph.insert('dog2', 'chihuahua');
  			nodeThree = graph.insert('flavor1', 'chocolate');
  		});

  		it('nodes one and two are neighbors', function() {
  			assert.equal(nodeTwo.neighbors.indexOf(nodeOne), 0);
  		});

  		it('nodes two and three are not neighbors', function() {
  			assert.equal(nodeThree.neighbors.indexOf(nodeTwo), -1);
  		});
  });
});
