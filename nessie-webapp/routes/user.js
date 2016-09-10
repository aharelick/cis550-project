'use strict';

var fs = require('fs');
var util = require('util');
var express = require('express');
var router = express.Router();
var User = require('../models/User');
var Upload = require('../models/Upload');
var Node = require('../models/Node');
var InvertedNode = require('../models/InvertedNode');
var formidable = require('formidable');
var search = require('./search.js');
var flow = require('./flow.js');

/* GET index page. */
router.get('/', function(req, res, next) {
  return res.redirect('/user/dashboard');
});

/* GET dashboard page. */
router.get('/dashboard', function(req, res, next) {
  return res.render('dashboard', { title: 'Nessie' });
});

/* GET search page */
router.get('/search', function(req, res, next) {
  return res.render('search', { title: 'Search' });
});

router.post('/search', function(req, res, next) {
  var query = req.body.searchTerms;
  search(query, function(paths) {
    res.json(paths);
  });
})

router.post('/create-upload', function(req, res, next) {
  flow.parseForm(req)
  .then(flow.readFilePromise)
  .then(flow.parseFileContents)
  .then(flow.createFileUpload)
  .then(flow.createGraph)
  .finally(function() {
    console.log('Callback hell avoided');
  });

    // Look for the document in the database, if it isn't already present then add it
    /* async.waterfall([
      // Create the nodes
      function(docId, callback) {
        var nodes = [];
        var invertedNodes = [];
        var parentNode = addNode(fields.name, null, docId, nodes, invertedNodes);
        createNodes(contents, parentNode, docId, nodes, invertedNodes);

        var numWorkers = 4;
        var nodeLists = [];

        for (var i = 0; i < numWorkers; i++) {
          nodeLists.push([]);
        }
        for (var i = 0; i < nodes.length; i++) {
          nodeLists[i%numWorkers].push(nodes[i]);
        }

        var workerJobs = [];
        for (var i = 0; i < numWorkers; i++) {
          var job = jobs.create('upload', {
            nodes:nodeLists[i]
          }).on('complete', function(result) {
            Upload.update({ _id: docId }, { $set: { status: 'Linking Complete' }}, {}, function(err, numAffected) {});
          }).save();
        }
        InvertedNode.insertMany(invertedNodes);

        callback(null, nodes, invertedNodes);
      },
      // For each node start the next pipeline
      function(nodes, invertedNodes, callback) {
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
      }
      ], function(err, result) {
        if (err) {
          return res.json({message: err});
        }
        return res.json({message: 'Success'});
      });
  
    */
});


// Iterate through the dataItem and add all entries to an inverted index
var createNodes = function(value, parent, fileId, nodes, invertedNodes) {
  if (Array.isArray(value)) {
    value.forEach(function(currentVal, index) {
      createNodes(currentVal, parent, fileId, nodes, invertedNodes);
    });
  }
  else if (typeof value === 'object' && value != null) {
    Object.keys(value).forEach(function(key, index, array) {
      var node = addNode(key, parent, fileId, nodes, invertedNodes);
      createNodes(value[key], node, fileId, nodes, invertedNodes);
    });
  } else {
    addNode(value, parent, fileId, nodes, invertedNodes);
  }
}

var addNode = function(key, parent, fileId, nodes, invertedNodes) {
  key = '' + key;
  var node = new Node({
    key: key.toLowerCase(),
    docId: fileId,
    neighbors: [],
    parent: parent == null ? null : parent._id
  });

  if (parent != null) {
    node.neighbors.push(parent._id);
    parent.neighbors.push(node._id);
  }

  key.split(' ').forEach(function(term, index) {
    var invertedNode = new InvertedNode({
      term: term.toLowerCase(),
      nodeId: node._id
    });
    invertedNodes.push(invertedNode);
  });

  nodes.push(node);

  return node;
}


router.post('/update-upload-status', function(req, res, next) {
  // TODO: validate these params, make sure user is editing own upload
  var id = req.body.id;
  var status = req.body.status;
  Upload.findOne({ _id: id }, function(err, upload) {
    if (err) {
      return res.sendStatus(500);
    }
    upload.status = status;
    upload.save(function(err) {
      if (err) {
        return res.sendStatus(500);
      }
    });
    return res.sendStatus(200);
  });
});

router.get('/edit-upload/:id', function(req, res, next) {
  // TODO: validate this id and make sure it belongs to the current user
  var id = req.params.id;
  Upload.findById(id, function(err, upload) {
    if (err) {
      return res.redirect('/user/dashboard');
    } else {
      return res.render('edit_upload', { upload: upload, title: 'Edit Upload' });
    }
  });
});

router.post('/edit-upload/:id', function(req, res, next) {
  // TODO: validate id parameter, make sure user is allowed to edit it
  var id = req.params.id;
  Upload.findById(id, function(err, upload) {
    if (err) {
      return res.redirect('/user/dashboard');
    }
    upload.public = (req.body.public !== undefined);
    upload.save(function(err) {
      if (err) {
        return next(err);
      }
      return res.redirect('/user/dashboard');
    });
  });
});

router.get('/get-uploads', function(req, res, next) {
  Upload.find({ user: req.user.id }, function(err, uploads) {
    if (err) {
      return res.sendStatus(500);
    }
    return res.json(uploads);
  });
});



module.exports = router;
